
package com.example.ledlightapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
fun AboutUsScreen(navController: NavController) {
    val backgroundColor = Color.White
    val accentColor = Color(0xFF3988D6)
    val secondaryColor = Color(0xFF8064C8)
    val titleColor = Color(0xFF202938)
    val bodyColor = Color(0xFF596579)
    val cardColor = Color(0xFFF7F9FC)

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .verticalScroll(rememberScrollState()) //scrolling screen
                .padding(24.dp)
        ) {
            //arrow back to menu screen
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    tint = titleColor
                )
            }

            Spacer(modifier = Modifier.height(0.dp))

            Text(
                text = "ABOUT US",
                fontSize = 36.sp,
                color = Color(0xFF5146C8),
                fontFamily = FontFamily.Cursive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(0.dp))

            Spacer(modifier = Modifier.height(16.dp))
            //general description
            Text(
                text =
                        "This is an IoT project that allows users to interact with a smart LED " +
                        "system through a dedicated mobile application. "+
                        "Users can choose the features they want to display and send " +
                        "their requests to the cloud. "+
                        "An embedded system processes these requests and "+
                        "brings them to life using a LED matrix and a laser "+
                        "for drawing icons. "+
                        ""+

                        "Among the many features, you can send your loved ones a picture, "+
                        "display a personal message such as “I miss you,” "+
                        "or even send them a karaoke song so you can sing together from afar.",
                color = bodyColor,
                fontSize = 16.sp,
                lineHeight = 25.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Project information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "💡",
                        fontSize = 30.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Our Project",
                        color = titleColor,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Control the matrix from a mobile app, display colors, text, " +
                                "images and animations, and explore the connection between electronics " +
                                "and software.",
                        color = bodyColor,
                        fontSize = 15.sp,
                        lineHeight = 23.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Team information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "👥",
                        fontSize = 30.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Meet the Team",
                        color = titleColor,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "We are third-year Electrical Engineering and Physics students at the Technion. " +
                                "This project is part of our first project course, where we explored " +
                                "the connection between hardware and software " +
                                "by working with electronic components and " +
                                "developing an embedded system based on the ESP32.",
                        color = bodyColor,
                        fontSize = 15.sp,
                        lineHeight = 23.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Developed by Na'ama Dreifuss and Eilon Shany, under the guidance of Boaz Mizrachi ",
                        color = secondaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
