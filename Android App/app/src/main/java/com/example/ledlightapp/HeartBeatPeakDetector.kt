
package com.example.ledlightapp

import android.util.Log
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Result of peak detection and heart-rate estimation.
 */
data class HeartBeatPeakResult(
    val bpm: Double?,
    val peakTimesSeconds: List<Double>,
    val validIntervalsSeconds: List<Double>,
    val prominenceThreshold: Double,
    val message: String
)

object HeartBeatPeakDetector {

    private const val TAG = "HeartBeatDebug"

    private const val MIN_HEART_RATE_BPM = 40.0
    private const val MAX_HEART_RATE_BPM = 180.0

    private const val MIN_PEAK_DISTANCE_SECONDS =
        60.0 / MAX_HEART_RATE_BPM

    private const val MIN_INTERVAL_SECONDS =
        60.0 / MAX_HEART_RATE_BPM

    private const val MAX_INTERVAL_SECONDS =
        60.0 / MIN_HEART_RATE_BPM

    private const val PROMINENCE_WINDOW_SECONDS = 0.5

    // Ignore the startup portion of the filtered signal.
    private const val STARTUP_IGNORE_SECONDS = 1.0

    fun detect(
        samples: List<HeartBeatSignalSample>,
        filteredSignal: List<Double>
    ): HeartBeatPeakResult {

        require(samples.size == filteredSignal.size) {
            "Samples and filtered signal must have the same length"
        }

        if (samples.size < 5) {
            return emptyResult("Not enough samples to detect peaks.")
        }

        require(samples.all { it.timestampSeconds.isFinite() }) {
            "Sample timestamps must be finite"
        }

        require(filteredSignal.all { it.isFinite() }) {
            "Filtered signal must contain only finite values"
        }

        require(samples.zipWithNext().all { (a, b) ->
            b.timestampSeconds > a.timestampSeconds
        }) {
            "Sample timestamps must be strictly increasing"
        }

        val values = filteredSignal
        val times = samples.map { it.timestampSeconds }

        val sortedValues = values.sorted()
        val percentile10 = percentile(sortedValues, 0.10)
        val percentile90 = percentile(sortedValues, 0.90)
        val robustRange = percentile90 - percentile10

        val mean = values.average()
        val variance = values.map {
            val difference = it - mean
            difference * difference
        }.average()
        val standardDeviation = sqrt(variance)

        // Adaptive threshold that is less sensitive to isolated outliers.
        val prominenceThreshold = max(
            0.08 * robustRange,
            0.35 * standardDeviation
        )

        Log.d(
            TAG,
            "Peak detector: robustRange=$robustRange, " +
                    "std=$standardDeviation, " +
                    "threshold=$prominenceThreshold"
        )

        if (robustRange <= 0.0 || prominenceThreshold <= 0.0) {
            return emptyResult(
                "The filtered signal has too little variation.",
                prominenceThreshold
            )
        }

        val candidates = mutableListOf<PeakCandidate>()

        for (i in 1 until values.lastIndex) {
            if (times[i] - times.first() < STARTUP_IGNORE_SECONDS) {
                continue
            }

            val isLocalMaximum =
                values[i] > values[i - 1] &&
                        values[i] >= values[i + 1]

            if (!isLocalMaximum) continue

            val prominence = calculateProminence(
                values = values,
                peakIndex = i,
                times = times
            )

            if (prominence >= prominenceThreshold) {
                candidates.add(
                    PeakCandidate(
                        index = i,
                        timeSeconds = times[i],
                        prominence = prominence
                    )
                )
            }
        }

        // Prefer stronger peaks when candidates are too close together.
        val selectedPeaks = mutableListOf<PeakCandidate>()

        for (candidate in candidates.sortedByDescending {
            it.prominence
        }) {
            val isTooClose = selectedPeaks.any {
                abs(it.timeSeconds - candidate.timeSeconds) <
                        MIN_PEAK_DISTANCE_SECONDS
            }

            if (!isTooClose) {
                selectedPeaks.add(candidate)
            }
        }

        val orderedPeaks = selectedPeaks.sortedBy {
            it.timeSeconds
        }

        val peakTimes = orderedPeaks.map {
            it.timeSeconds
        }

        val intervals = peakTimes.zipWithNext { first, second ->
            second - first
        }

        val validIntervals = intervals.filter {
            it in MIN_INTERVAL_SECONDS..MAX_INTERVAL_SECONDS
        }

        Log.d(
            TAG,
            "Peak detector candidates=${candidates.size}, " +
                    "selectedPeaks=${peakTimes.size}, " +
                    "validIntervals=${validIntervals.size}"
        )

        Log.d(TAG, "Detected peak times: $peakTimes")
        Log.d(TAG, "Detected intervals: $intervals")
        Log.d(TAG, "Valid intervals: $validIntervals")

        if (validIntervals.size < 2) {
            return HeartBeatPeakResult(
                bpm = null,
                peakTimesSeconds = peakTimes,
                validIntervalsSeconds = validIntervals,
                prominenceThreshold = prominenceThreshold,
                message = "Not enough reliable peak intervals."
            )
        }

        val averageInterval = validIntervals.average()
        val bpm = 60.0 / averageInterval

        return HeartBeatPeakResult(
            bpm = bpm,
            peakTimesSeconds = peakTimes,
            validIntervalsSeconds = validIntervals,
            prominenceThreshold = prominenceThreshold,
            message = "Heart-rate estimate calculated."
        )
    }

    /**
     * Estimates peak prominence using the minima in a time window
     * on either side of the candidate peak.
     */
    private fun calculateProminence(
        values: List<Double>,
        peakIndex: Int,
        times: List<Double>
    ): Double {
        val peakTime = times[peakIndex]

        var leftMinimum = values[peakIndex]
        var rightMinimum = values[peakIndex]

        var leftIndex = peakIndex - 1
        while (
            leftIndex >= 0 &&
            peakTime - times[leftIndex] <= PROMINENCE_WINDOW_SECONDS
        ) {
            leftMinimum = minOf(leftMinimum, values[leftIndex])
            leftIndex--
        }

        var rightIndex = peakIndex + 1
        while (
            rightIndex < values.size &&
            times[rightIndex] - peakTime <= PROMINENCE_WINDOW_SECONDS
        ) {
            rightMinimum = minOf(rightMinimum, values[rightIndex])
            rightIndex++
        }

        val higherSideMinimum = max(leftMinimum, rightMinimum)

        return values[peakIndex] - higherSideMinimum
    }

    private fun percentile(
        sortedValues: List<Double>,
        fraction: Double
    ): Double {
        if (sortedValues.isEmpty()) return 0.0

        val position = fraction * (sortedValues.size - 1)
        val lowerIndex = position.toInt()
        val upperIndex = (lowerIndex + 1).coerceAtMost(
            sortedValues.lastIndex
        )
        val weight = position - lowerIndex

        return sortedValues[lowerIndex] * (1.0 - weight) +
                sortedValues[upperIndex] * weight
    }

    private data class PeakCandidate(
        val index: Int,
        val timeSeconds: Double,
        val prominence: Double
    )

    private fun emptyResult(
        message: String,
        threshold: Double = 0.0
    ) = HeartBeatPeakResult(
        bpm = null,
        peakTimesSeconds = emptyList(),
        validIntervalsSeconds = emptyList(),
        prominenceThreshold = threshold,
        message = message
    )
}
