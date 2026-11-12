package com.example.ledlightapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import com.github.skydoves.colorpicker.compose.*

@Composable
fun ColorsScreen(navController: NavController) {

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {

        val controller = rememberColorPickerController()
        val context = LocalContext.current

        var selectedColor by remember {
            mutableStateOf(Color.White)
        }

        LaunchedEffect(Unit) {

            FirebaseManager.readColor { r, g, b ->

                selectedColor = Color(
                    red = r / 255f,
                    green = g / 255f,
                    blue = b / 255f
                )
            }
        }

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
                    text = "colors palette mode",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "choose LED color:"
                )

                Spacer(modifier = Modifier.height(20.dp))

                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(260.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope ->

                        selectedColor = colorEnvelope.color
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(30.dp),
                    controller = controller
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {

                        val r =
                            (selectedColor.red * 255).toInt()

                        val g =
                            (selectedColor.green * 255).toInt()

                        val b =
                            (selectedColor.blue * 255).toInt()

                        FirebaseManager.setColor(
                            r = r,
                            g = g,
                            b = b,

                            onSuccess = {

                                FirebaseManager.updateStatus(
                                    mode = "color",
                                    content = "RGB($r,$g,$b)"
                                )

                                Toast.makeText(
                                    context,
                                    "Color sent successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },

                            onFailure = {

                                Toast.makeText(
                                    context,
                                    "Failed to send color, try again",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                ) {
                    Text("Save Color")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(selectedColor)
                )
            }

            Button(
                onClick = {
                    navController.navigate("menu")
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
            ) {
                Text("back to menu")
            }
        }
    }
}