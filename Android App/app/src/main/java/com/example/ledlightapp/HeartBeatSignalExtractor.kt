
package com.example.ledlightapp

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * One measurement extracted from a video frame.
 *
 * timestampSeconds: time from the beginning of the video.
 * redMean: average intensity of the red channel (0..255).
 */
data class HeartBeatSignalSample(
    val timestampSeconds: Double,
    val redMean: Double
)

/**
 * Extracts the mean red-channel intensity from video frames.
 *
 * This function only extracts raw measurements. Filtering and BPM
 * calculation are performed by other components.
 */
object HeartBeatSignalExtractor {

    private const val TAG = "HeartBeatDebug"

    suspend fun extractRedSignal(
        context: Context,
        videoUri: Uri,
        sampleIntervalMs: Long = 100L
    ): List<HeartBeatSignalSample> = withContext(Dispatchers.IO) {
        require(sampleIntervalMs > 0L) {
            "sampleIntervalMs must be greater than zero"
        }

        val retriever = MediaMetadataRetriever()
        val samples = mutableListOf<HeartBeatSignalSample>()

        try {
            retriever.setDataSource(context, videoUri)

            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: throw IllegalArgumentException(
                    "Could not read video duration"
                )

            var timestampMs = 0L

            while (timestampMs < durationMs) {
                val bitmap = retriever.getFrameAtTime(
                    timestampMs * 1_000L,
                    MediaMetadataRetriever.OPTION_CLOSEST
                )

                if (bitmap != null) {
                    try {
                        samples += HeartBeatSignalSample(
                            timestampSeconds = timestampMs / 1_000.0,
                            redMean = calculateMeanRed(bitmap)
                        )
                    } finally {
                        bitmap.recycle()
                    }
                }

                timestampMs += sampleIntervalMs
            }

            // Log the extracted signal statistics.
            if (samples.size >= 2) {
                val firstTimestamp = samples.first().timestampSeconds
                val lastTimestamp = samples.last().timestampSeconds
                val totalDuration = lastTimestamp - firstTimestamp
                val averageSamplingRate =
                    (samples.size - 1) / totalDuration

                Log.d(TAG, "Extracted samples: ${samples.size}")
                Log.d(TAG, "Signal duration: $totalDuration seconds")
                Log.d(
                    TAG,
                    "Estimated sampling rate: $averageSamplingRate Hz"
                )
            } else {
                Log.w(
                    TAG,
                    "Not enough samples to estimate sampling rate: ${samples.size}"
                )
            }

            // Check intervals between consecutive requested timestamps.
            if (samples.size >= 2) {
                val intervals = samples.zipWithNext { first, second ->
                    second.timestampSeconds - first.timestampSeconds
                }

                val minInterval = intervals.minOrNull()!!
                val maxInterval = intervals.maxOrNull()!!
                val meanInterval = intervals.average()

                val variance = intervals.map { interval ->
                    (interval - meanInterval) *
                            (interval - meanInterval)
                }.average()

                val standardDeviation = kotlin.math.sqrt(variance)

                Log.d(
                    TAG,
                    "Minimum sample interval: $minInterval seconds"
                )
                Log.d(
                    TAG,
                    "Maximum sample interval: $maxInterval seconds"
                )
                Log.d(
                    TAG,
                    "Mean sample interval: $meanInterval seconds"
                )
                Log.d(
                    TAG,
                    "Interval standard deviation: $standardDeviation seconds"
                )
            }

            // Log the red-channel intensity range.
            if (samples.isNotEmpty()) {
                val redValues = samples.map { it.redMean }

                Log.d(TAG, "Red signal minimum: ${redValues.minOrNull()}")
                Log.d(TAG, "Red signal maximum: ${redValues.maxOrNull()}")
                Log.d(TAG, "Red signal standard deviation: ${standardDeviation(redValues)}")
                Log.d(TAG, "First 40 red values: ${redValues.take(40)}")
            }

            samples
        } finally {
            retriever.release()
        }
    }

    /**
     * Calculates the average red intensity across all pixels in a frame.
     */
    private fun calculateMeanRed(bitmap: Bitmap): Double {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)

        bitmap.getPixels(
            pixels,
            0,
            width,
            0,
            0,
            width,
            height
        )

        var redSum = 0L

        for (pixel in pixels) {
            // Extract the red channel (bits 16-23).
            redSum += (pixel shr 16) and 0xFF
        }

        return redSum.toDouble() / pixels.size
    }

    private fun standardDeviation(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0

        val mean = values.average()
        val variance = values.map { value ->
            val difference = value - mean
            difference * difference
        }.average()

        return kotlin.math.sqrt(variance)
    }
}
