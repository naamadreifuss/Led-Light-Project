package com.example.ledlightapp

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MusicBeatsScreen(navController: NavController) {

    val context = LocalContext.current

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "music beats mode",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "activate music reactive LED mode"
                )

                Spacer(modifier = Modifier.height(20.dp))

               /* Button(
                    onClick = {

                        FirebaseManager.updateStatus(
                            mode = "music",
                            content = "ACTIVE"
                        )

                        FirebaseManager.musicMode(
                            musicMode = "expand"
                        )

                        Toast.makeText(
                            context,
                            "Music mode activated!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {

                    Text("expansion mode")
                }

                Spacer(modifier = Modifier.height(10.dp)) */
                val musicModes = listOf(
                    "expand",
                    "pulse",
                    "bars"
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(musicModes) { mode ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    FirebaseManager.musicMode(mode)
                                    FirebaseManager.updateStatus(
                                        mode = "music",
                                        content = "ACTIVE"
                                    )
                                    Toast.makeText(
                                        context,
                                        "Music mode: $mode",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }



            Button(
                onClick = {
                    navController.popBackStack()
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