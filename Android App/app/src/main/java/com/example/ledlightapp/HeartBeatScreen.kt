package com.example.ledlightapp

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val HEART_BEAT_TAG = "HeartBeatDebug"

@Composable
fun HeartBeatScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var camera by remember { mutableStateOf<Camera?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }

    var isFlashOn by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready to measure") }

    var isSessionActive by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf<Int?>(null) }
    var recordingSecondsRemaining by remember { mutableStateOf<Int?>(null) }


    var measuredBpm by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            recording?.stop()
            camera?.cameraControl?.enableTorch(false)
        }
    }

    fun startVideoRecording() {
        val capture = videoCapture

        if (capture == null) {
            isSessionActive = false
            isFlashOn = false
            camera?.cameraControl?.enableTorch(false)
            status = "Camera is not ready"
            Log.e(HEART_BEAT_TAG, "Cannot start recording: camera is not ready")
            return
        }

        val videoFile = File(context.cacheDir, "HeartBeat_${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        try {
            Log.d(HEART_BEAT_TAG, "Starting 30-second recording to cache")

            recording = capture.output
                .prepareRecording(context, outputOptions)
                .start(ContextCompat.getMainExecutor(context)) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            isRecording = true
                            recordingSecondsRemaining = 30
                            status = "Recording..."
                            Log.d(HEART_BEAT_TAG, "Recording started")
                        }

                        is VideoRecordEvent.Finalize -> {
                            isRecording = false
                            recording = null
                            recordingSecondsRemaining = null
                            isFlashOn = false
                            isSessionActive = false
                            camera?.cameraControl?.enableTorch(false)

                            val videoUri = Uri.fromFile(videoFile)
                            val durationMs = event.recordingStats.recordedDurationNanos / 1_000_000L

                            Log.d(
                                HEART_BEAT_TAG,
                                "Recording finalized: hasError=${event.hasError()}, durationMs=$durationMs, uri=$videoUri"
                            )

                            if (!event.hasError()) {
                                status = "Processing signal..."
                                isProcessing = true
                                measuredBpm = null

                                coroutineScope.launch {
                                    try {
                                        Log.d(HEART_BEAT_TAG, "Starting signal extraction: uri=$videoUri")

                                        val samples = HeartBeatSignalExtractor.extractRedSignal(
                                            context = context,
                                            videoUri = videoUri
                                        )

                                        val rawSignal = samples.map { it.redMean }
                                        val filteredSignal = HeartBeatFilter.filter(rawSignal)
                                        val result = HeartBeatPeakDetector.detect(
                                            samples = samples,
                                            filteredSignal = filteredSignal
                                        )

                                        if (result.bpm != null) {
                                            val bpm = result.bpm.toInt()
                                            measuredBpm = bpm
                                            status = "Estimated heart rate: $bpm BPM"
                                        } else {
                                            status = "Signal processed, but BPM could not be estimated"
                                        }

                                        if (videoFile.exists()) {
                                            videoFile.delete()
                                        }

                                    } catch (exception: Exception) {
                                        Log.e(HEART_BEAT_TAG, "Signal processing failed", exception)
                                        status = "Signal processing failed"
                                    } finally {
                                        isProcessing = false
                                    }
                                }
                            } else {
                                status = "Recording failed: ${event.error}"
                                Log.e(HEART_BEAT_TAG, "Recording failed with error ${event.error}")
                            }
                        }
                    }
                }
        } catch (exception: Exception) {
            isRecording = false
            recording = null
            recordingSecondsRemaining = null
            isFlashOn = false
            isSessionActive = false
            camera?.cameraControl?.enableTorch(false)
            status = "Could not start recording"
            Log.e(HEART_BEAT_TAG, "Could not start recording", exception)
        }
    }

    LaunchedEffect(countdownSeconds) {
        val seconds = countdownSeconds ?: return@LaunchedEffect

        if (seconds > 1) {
            delay(1_000L)
            if (countdownSeconds == seconds) {
                countdownSeconds = seconds - 1
            }
        } else if (seconds == 1) {
            delay(1_000L)
            if (countdownSeconds == 1) {
                countdownSeconds = null
                startVideoRecording()
            }
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            for (seconds in 29 downTo 0) {
                delay(1_000L)
                if (!isRecording) {
                    return@LaunchedEffect
                }
                recordingSecondsRemaining = seconds
            }

            if (isRecording) {
                Log.d(HEART_BEAT_TAG, "30-second countdown completed; stopping recording")
                recording?.stop()
            }
        }
    }
    //UI
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "heart beat mode",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Place your finger over the rear camera and flash")

                Spacer(modifier = Modifier.height(72.dp))

                if (isSessionActive) {
                    HeartBeatCamera(
                        flashOn = isFlashOn,
                        onCameraReady = { activeCamera, capture ->
                            camera = activeCamera
                            videoCapture = capture
                        },
                        modifier = Modifier
                            .width(240.dp)
                            .height(180.dp)
                    )
                } else {
                    Spacer(
                        modifier = Modifier
                            .width(240.dp)
                            .height(180.dp)
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))

                if (!isProcessing) {
                    Button(
                        enabled = true,
                        onClick = {
                            when {
                                isSessionActive && countdownSeconds != null -> {
                                    countdownSeconds = null
                                    isSessionActive = false
                                    isFlashOn = false
                                    camera?.cameraControl?.enableTorch(false)
                                    status = "Recording cancelled"
                                }

                                isRecording -> {
                                    recording?.stop()
                                    status = "Saving video..."
                                }

                                else -> {
                                    isSessionActive = true
                                    isFlashOn = true
                                    countdownSeconds = 5
                                    status = "Get ready"
                                    measuredBpm = null
                                }
                            }
                        }
                    ) {
                        Text(
                            when {
                                countdownSeconds != null -> "Cancel"
                                isRecording -> "Stop recording"
                                else -> "Start recording"
                            }
                        )
                    }
                }
                //button for firebase,shows after analysis
              if (measuredBpm != null && !isProcessing && !isRecording && countdownSeconds == null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val bpm = measuredBpm ?: return@Button

                            FirebaseManager.sendHeartRate(
                                bpm = bpm,
                                onSuccess = {
                                    Log.d(HEART_BEAT_TAG, "Heart rate sent to Firebase: $bpm BPM")
                                    Toast.makeText(
                                        context,
                                        "Heart rate sent successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onFailure = { exception ->
                                    Log.e(HEART_BEAT_TAG, "Failed to send heart rate to Firebase", exception)
                                    Toast.makeText(
                                        context,
                                        "Failed to send heart rate, try again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    ) {
                        Text("Send")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    countdownSeconds != null -> {
                        Text(
                            "Place your finger on the camera.\n" +
                                    "Recording starts in $countdownSeconds..."
                        )
                    }

                    isRecording -> {
                        Text(
                            "Keep your finger in place.\n" +
                                    "${recordingSecondsRemaining ?: 30} seconds remaining"
                        )
                    }

                    isProcessing -> {
                        Text(
                            text = "Processing data, please wait",
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Cursive,
                            color = androidx.compose.ui.graphics.Color.Red
                        )
                    }

                    else -> {
                        Text(status)
                    }
                }
            }

            Button(
                onClick = {
                    camera?.cameraControl?.enableTorch(false)
                    navController.navigate("menu") {
                        popUpTo("menu") {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text("Back to menu")
            }
        }
    }
}

