
package com.example.ledlightapp

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TextScreen(navController: NavController) {

    val context = LocalContext.current

    // Load the last successfully sent text
    val prefs = remember {
        context.getSharedPreferences(
            "led_light_app",
            android.content.Context.MODE_PRIVATE
        )
    }

    var lastSentText by remember {
        mutableStateOf(prefs.getString("last_sent_text", "") ?: "")
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "text mode",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(text = "Enter text to display:")

                // Display the last successfully sent text
                if (lastSentText.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Last sent: $lastSentText",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "To change the message, enter a different text.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Creating free text
                var text by remember { mutableStateOf("") }

                TextField(
                    value = text,
                    onValueChange = {
                        if (it.length <= 20) {
                            text = it
                        }
                    },
                    label = { Text("Enter new text") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sending text via FirebaseManager
                Button(
                    onClick = {

                        if (text.isNotBlank()) {

                            // Keep a copy of the exact text being sent
                            val sentText = text

                            Log.d(
                                "TextScreenDebug",
                                "Button clicked. Attempting to send text: '$sentText'"
                            )

                            FirebaseManager.enterText(
                                sentText,
                                onSuccess = {

                                    Log.d(
                                        "TextScreenDebug",
                                        "Firebase enterText SUCCESS. Updating status mode..."
                                    )

                                    FirebaseManager.updateStatus(
                                        mode = "text",
                                        content = sentText
                                    )

                                    // Save the last successfully sent text
                                    lastSentText = sentText

                                    prefs.edit()
                                        .putString("last_sent_text", sentText)
                                        .apply()

                                    Toast.makeText(
                                        context,
                                        "Message sent successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    text = ""
                                },
                                onFailure = { exception ->

                                    Log.e(
                                        "TextScreenDebug",
                                        "Firebase enterText FAILED!",
                                        exception
                                    )

                                    Toast.makeText(
                                        context,
                                        "Failed to send message, try again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )

                        } else {

                            Log.w(
                                "TextScreenDebug",
                                "Button clicked, but text is blank. Execution skipped."
                            )
                        }
                    }
                ) {
                    Text("Send")
                }
            }

            // Return to the main menu
            Button(
                onClick = {
                    navController.navigate("menu") {
                        popUpTo("menu") {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text("Back to menu")
            }
        }
    }
}
