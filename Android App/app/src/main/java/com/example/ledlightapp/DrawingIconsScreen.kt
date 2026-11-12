
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
fun DrawingIconsScreen(navController: NavController) {

    val context = LocalContext.current

    // Load the last successfully sent icon
    val prefs = remember {
        context.getSharedPreferences(
            "led_light_app",
            android.content.Context.MODE_PRIVATE
        )
    }

    var lastSentIcon by remember {
        mutableStateOf(prefs.getString("last_sent_icon", "") ?: "")
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Drawing icons laser mode",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Display the last successfully sent icon
                if (lastSentIcon.isNotEmpty()) {

                    Text(
                        text = "Last sent: $lastSentIcon",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "To change the icon, choose another icon.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Choose an icon:")

                Spacer(modifier = Modifier.height(16.dp))

                val icons = listOf(
                    "❤️", "⭐", "🌸", "⭕",
                    "△", "🙂", "➡", "⬜"
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(icons) { icon ->
                        IconCard(icon) { sentIcon ->

                            // Update the last sent icon
                            lastSentIcon = sentIcon

                            prefs.edit()
                                .putString("last_sent_icon", sentIcon)
                                .apply()
                        }
                    }
                }

                // Open the custom drawing screen
                Button(
                    onClick = {
                        navController.navigate("customIcon")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBE9B7B)
                    )
                ) {
                    Text(
                        text = "Create your own icon",
                        color = Color.White
                    )
                }
            }

            // Return to the main menu
            Button(
                onClick = {
                    navController.navigate("menu")
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
            ) {
                Text("Back to menu")
            }
        }
    }
}

@Composable
fun IconCard(
    icon: String,
    onIconSent: (String) -> Unit
) {

    val context = LocalContext.current

    Card(
        modifier = Modifier
            .size(70.dp)
            .clickable {

                val shape = when (icon) {
                    "❤️" -> "heart"
                    "⭐" -> "star"
                    "🌸" -> "flower"
                    "⭕" -> "circle"
                    "△" -> "triangle"
                    "🙂" -> "smiley"
                    "➡" -> "arrow"
                    "⬜" -> "square"
                    else -> "unknown"
                }

                FirebaseManager.sendShape(
                    shape,
                    onSuccess = {

                        // Update the last sent icon only after successful sending
                        onIconSent(icon)

                        Toast.makeText(
                            context,
                            "Icon sent successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onFailure = {
                        Toast.makeText(
                            context,
                            "Failed to send icon, send again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

                FirebaseManager.updateStatus(
                    mode = "icon",
                    content = shape
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFBE9B7B)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }
    }
}
