
package com.example.ledlightapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FirstSetupScreen(navController: NavController) {

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {

            // Back button
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
                Spacer(modifier = Modifier.height(0.dp))


                Text(
                    text = "FIRST SETUP",
                    fontSize = 36.sp,
                    color = Color(0xFF5146C8),
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "First connection to Wi-Fi",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Follow these steps to connect your system " +
                        "to your Wi-Fi network for the first time.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            SetupStep(
                number = "1",
                title = "Connect to the setup network",
                description = "Open the Wi-Fi settings on your phone " +
                        "and look for the network \"MyESP32-Setup\""
            )

            SetupStep(
                number = "2",
                title = "Open the Wi-Fi setup page",
                description = "Open the Wi-Fi setup page and look for the heading " +
                        "\"Router Management\". Tap it to continue."
            )

            SetupStep(
                number = "3",
                title = "Choose your Wi-Fi network",
                description = "Select your home Wi-Fi network and enter " +
                        "its password."
            )

            SetupStep(
                number = "4",
                title = "Wait for the connection",
                description = "Save your settings and wait for them to be updated. " +
                        "Once the update is complete, you can close the browser " +
                        "and start controlling the system through the app."
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Note: Your phone should have an internet " +
                        "connection when using the app" ,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SetupStep(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = MaterialTheme.shapes.large,
            color = Color(0xFFEAE8FF)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5146C8)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 15.sp,
                color = Color.DarkGray,
                lineHeight = 21.sp
            )
        }
    }
}
