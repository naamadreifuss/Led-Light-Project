
package com.example.ledlightapp

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
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
import kotlinx.coroutines.launch

@Composable
fun MenuScreen(navController: NavController) {

    val context = LocalContext.current

    // Current matrix status from Firebase
    var activeMode by remember { mutableStateOf("Loading...") }
    var content by remember { mutableStateOf("") }

    // Read status whenever entering the menu screen
    LaunchedEffect(Unit) {
        FirebaseManager.readStatus { mode, currentContent ->
            activeMode = mode
            content = currentContent
        }
    }

    // State for the navigation drawer
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val coroutineScope = rememberCoroutineScope()

    // Close the drawer when the system Back button is pressed
    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {

                    // About Us screen
                    NavigationDrawerItem(
                        label = {
                            Text("About Us")
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            navController.navigate("aboutUs")
                        },
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 2.dp
                        )
                    )

                    // First Setup Wi-Fi screen
                    NavigationDrawerItem(
                        label = {
                            Text("First Setup")
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            navController.navigate("firstSetup")
                        },
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 2.dp
                        )
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                // Existing home screen content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LED Light App - Project A",
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(text = "Choose mode:")

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeatureCard("🎨", Color(0xFFffb3ba)) {
                            navController.navigate("colors")
                        }
                        FeatureCard("🎤️", Color(0xFFffdfba)) {
                            navController.navigate("karaoke")
                        }
                        FeatureCard("⭐", Color(0xFFf5d9e7)) {
                            navController.navigate("draw")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeatureCard("🎵", Color(0xFFcfe2d6)) {
                            navController.navigate("music")
                        }
                        FeatureCard("📝", Color(0xFFbae1ff)) {
                            navController.navigate("text")
                        }
                        FeatureCard("🖼️", Color(0xFFf7e7b4)) {
                            navController.navigate("image")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeatureCard("❤️", Color(0xFFB3B7EE)) {
                            navController.navigate("heart")
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Matrix status card
                    Card(
                        modifier = Modifier.fillMaxWidth(0.70f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(
                                text = "Matrix Status: $activeMode",
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Cursive
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            /*
                            Text(
                                text = "Content: $content"
                            )
                            */

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    FirebaseManager.updateStatus(
                                        mode = "OFF",
                                        content = ""
                                    )

                                    // Update UI immediately
                                    activeMode = "OFF"
                                    content = ""

                                    Toast.makeText(
                                        context,
                                        "Matrix turned off",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Turn Off Matrix")
                            }
                        }
                    }
                }

                // Button that opens the drawer
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, top = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Open navigation menu"
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    emoji: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = color
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
        }
    }
}
