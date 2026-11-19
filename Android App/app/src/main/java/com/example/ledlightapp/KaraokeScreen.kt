package com.example.ledlightapp

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


@Composable
fun KaraokeScreen(
    navController: NavController,
    viewModel: KaraokeViewModel = viewModel()
) {

    val context = LocalContext.current
    val lyricsScrollState = rememberScrollState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {

            // Show only a loading screen while lyrics are being prepared.
            if (viewModel.isLoading) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.background
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "🎤 Preparing your karaoke",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Finding synchronized lyrics...\nPlease wait.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }


            } else {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "play music mode",
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))


                    if (viewModel.isPlaying && viewModel.videoId != null) {

                        Text(
                            text = viewModel.videoTitle,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    bottom = 12.dp,
                                    start = 8.dp,
                                    end = 8.dp
                                )
                        )


                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),

                            factory = { ctx ->

                                YouTubePlayerView(ctx).apply {

                                    addYouTubePlayerListener(
                                        object : AbstractYouTubePlayerListener() {

                                            // Start the video after lyrics are ready.
                                            override fun onReady(
                                                youTubePlayer: YouTubePlayer
                                            ) {

                                                // Debug log: YouTube Player is ready.
                                                Log.d(
                                                    "KaraokeDebug",
                                                    "YouTube Player is ready"
                                                )

                                                // Debug log: Show which video is being loaded.
                                                Log.d(
                                                    "KaraokeDebug",
                                                    "Loading video: ${viewModel.videoId}"
                                                )

                                                youTubePlayer.loadVideo(
                                                    viewModel.videoId!!,
                                                    0f
                                                )
                                            }


                                            // Update lyrics according to video time.
                                            override fun onCurrentSecond(
                                                youTubePlayer: YouTubePlayer,
                                                second: Float
                                            ) {
                                                viewModel.updateTime(second)
                                            }
                                        }
                                    )
                                }
                            }
                        )


                        Spacer(modifier = Modifier.height(16.dp))


                        // Display current lyric line.
                        if (viewModel.currentLineText.isNotEmpty()) {

                            Text(
                                text = viewModel.currentLineText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 8.dp,
                                        horizontal = 16.dp
                                    )
                            )
                        }


                        Spacer(modifier = Modifier.height(8.dp))


                       /* Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(horizontal = 8.dp),

                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant
                                        .copy(alpha = 0.5f)
                            )
                        ) {

                            Text(
                                text = viewModel.lyricsText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(lyricsScrollState),
                                textAlign = TextAlign.Center
                            )
                        } */


                        Spacer(modifier = Modifier.height(16.dp))


                        Button(
                            onClick = {
                                viewModel.resetKaraoke()
                            }
                        ) {

                            Text("Choose Another Song")
                        }


                    } else {

                        Text(
                            text = "choose a song for karaoke:"
                        )

                        // Show an error message if lyrics could not be found.
                        if (viewModel.errorMessage.isNotEmpty()) {

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = viewModel.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }


                        Spacer(modifier = Modifier.height(20.dp))


                        Button(
                            onClick = {

                                val youtubeUri =
                                    Uri.parse("https://www.youtube.com")

                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        youtubeUri
                                    )

                                context.startActivity(intent)
                            },

                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text("open youtube to copy a link")
                        }


                        Spacer(modifier = Modifier.height(16.dp))


                        OutlinedTextField(
                            value = viewModel.youtubeLink,
                            onValueChange = {
                                viewModel.onLinkChanged(it)
                            },
                            label = {
                                Text("paste your youtube link here")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )


                        Spacer(modifier = Modifier.height(16.dp))


                        Button(
                            onClick = {

                                // Debug log: User pressed the Start Karaoke button.
                                Log.d(
                                    "KaraokeDebug",
                                    "Start Karaoke button clicked"
                                )

                                val isLinkValid =
                                    viewModel.startKaraoke()

                                // Debug log: Show whether the YouTube link was valid.
                                Log.d(
                                    "KaraokeDebug",
                                    "YouTube link valid: $isLinkValid"
                                )

                                if (!isLinkValid) {

                                    Toast.makeText(
                                        context,
                                        "Invalid Youtube Link",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }


                                // Debug log: Update the active application mode.
                                Log.d(
                                    "KaraokeDebug",
                                    "Setting active mode to karaoke"
                                )

                                FirebaseManager.updateStatus(
                                    mode = "karaoke",
                                    content = ""
                                )
                            },

                            enabled = viewModel.youtubeLink.isNotEmpty(),

                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text("start karaoke")
                        }
                    }


                    Spacer(modifier = Modifier.height(20.dp))
                }


                // Back button.
                Button(
                    onClick = {

                        // Debug log: User is leaving the karaoke screen.
                        Log.d(
                            "KaraokeDebug",
                            "Leaving karaoke screen"
                        )

                        navController.navigate("menu") {

                            popUpTo("menu") {
                                inclusive = true
                            }

                            FirebaseManager.sendCurrentLine(
                                line = "",
                                scrollSpeed = 0.0
                            )
                        }
                    },

                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {

                    Text("back to menu")
                }
            }
        }
    }
}