package com.example.ledlightapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun HeartBeatCamera(
    flashOn: Boolean,
    onCameraReady: (Camera?, VideoCapture<Recorder>?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(hasPermission, previewView, lifecycleOwner) {
        if (!hasPermission) {
            onCameraReady(null, null)
            onDispose { }
        } else {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            var disposed = false
            var boundProvider: ProcessCameraProvider? = null

            providerFuture.addListener({
                if (disposed) return@addListener

                try {
                    val provider = providerFuture.get()
                    boundProvider = provider

                    val preview = Preview.Builder()
                        .build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    val recorder = Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HD))
                        .build()

                    val videoCapture = VideoCapture.withOutput(recorder)

                    provider.unbindAll()

                    val activeCamera = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        videoCapture
                    )

                    camera = activeCamera
                    onCameraReady(activeCamera, videoCapture)
                } catch (exception: Exception) {
                    camera = null
                    onCameraReady(null, null)
                }
            }, ContextCompat.getMainExecutor(context))

            onDispose {
                disposed = true
                camera?.cameraControl?.enableTorch(false)
                boundProvider?.unbindAll()
                camera = null
                onCameraReady(null, null)
            }
        }
    }

    LaunchedEffect(camera, flashOn) {
        val activeCamera = camera ?: return@LaunchedEffect
        if (activeCamera.cameraInfo.hasFlashUnit()) {
            activeCamera.cameraControl.enableTorch(flashOn)
        }
    }

    Box(modifier = modifier) {
        if (hasPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("Grant camera permission")
            }
        }
    }
}