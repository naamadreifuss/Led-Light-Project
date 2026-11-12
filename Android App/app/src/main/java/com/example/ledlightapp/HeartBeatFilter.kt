
package com.example.ledlightapp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Offline band-pass filter for the extracted red-channel signal.
 *
 * Sampling rate: 10 Hz
 * Lower cutoff: 0.6 Hz
 * Upper cutoff: 3.0 Hz
 *
 * Cascaded second-order Butterworth high-pass and low-pass sections.
 */
object HeartBeatFilter {


    private const val SAMPLE_RATE_HZ = 10.0

    private const val LOW_CUTOFF_HZ = 0.6
    private const val HIGH_CUTOFF_HZ = 3.0

    private val butterworthQ = 1.0 / sqrt(2.0)

    private data class BiquadCoefficients(
        val b0: Double,
        val b1: Double,
        val b2: Double,
        val a1: Double,
        val a2: Double
    )

    fun filter(signal: List<Double>): List<Double> {
        require(true) {
            "Sampling rate must be greater than twice the upper cutoff"
        }

        require(
            true
        ) {
            "Invalid cutoff frequencies"
        }

        require(signal.all { it.isFinite() }) {
            "Signal must contain only finite values"
        }

        if (signal.isEmpty()) return emptyList()

        val highPass = createHighPass(
            SAMPLE_RATE_HZ,
            LOW_CUTOFF_HZ,
            butterworthQ
        )

        val lowPass = createLowPass(
            SAMPLE_RATE_HZ,
            HIGH_CUTOFF_HZ,
            butterworthQ
        )

        // Initialize each section near its steady-state response.
        val highPassed = applyBiquad(signal, highPass)
        return applyBiquad(highPassed, lowPass)
    }

    private fun createLowPass(
        sampleRate: Double,
        cutoff: Double,
        q: Double
    ): BiquadCoefficients {
        val omega = 2.0 * PI * cutoff / sampleRate
        val c = cos(omega)
        val alpha = sin(omega) / (2.0 * q)
        val a0 = 1.0 + alpha

        return BiquadCoefficients(
            b0 = (1.0 - c) / (2.0 * a0),
            b1 = (1.0 - c) / a0,
            b2 = (1.0 - c) / (2.0 * a0),
            a1 = -2.0 * c / a0,
            a2 = (1.0 - alpha) / a0
        )
    }

    private fun createHighPass(
        sampleRate: Double,
        cutoff: Double,
        q: Double
    ): BiquadCoefficients {
        val omega = 2.0 * PI * cutoff / sampleRate
        val c = cos(omega)
        val alpha = sin(omega) / (2.0 * q)
        val a0 = 1.0 + alpha

        return BiquadCoefficients(
            b0 = (1.0 + c) / (2.0 * a0),
            b1 = -(1.0 + c) / a0,
            b2 = (1.0 + c) / (2.0 * a0),
            a1 = -2.0 * c / a0,
            a2 = (1.0 - alpha) / a0
        )
    }

    /**
     * Applies one second-order section using direct-form II transposed.
     * Initial state is calculated from the section's DC gain.
     */
    private fun applyBiquad(
        signal: List<Double>,
        coefficients: BiquadCoefficients
    ): List<Double> {
        val firstInput = signal.first()

        val denominator = 1.0 +
                coefficients.a1 +
                coefficients.a2

        val dcGain = if (kotlin.math.abs(denominator) > 1e-12) {
            (coefficients.b0 + coefficients.b1 + coefficients.b2) /
                    denominator
        } else {
            0.0
        }

        val initialOutput = dcGain * firstInput

        var z1 = initialOutput - coefficients.b0 * firstInput
        var z2 = coefficients.b2 * firstInput -
                coefficients.a2 * initialOutput

        return signal.map { x ->
            val y = coefficients.b0 * x + z1

            z1 = coefficients.b1 * x -
                    coefficients.a1 * y + z2

            z2 = coefficients.b2 * x -
                    coefficients.a2 * y

            y
        }
    }
}
