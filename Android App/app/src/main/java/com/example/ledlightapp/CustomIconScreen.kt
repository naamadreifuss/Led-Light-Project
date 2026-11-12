
package com.example.ledlightapp

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.hypot
import kotlin.math.roundToInt

// Represents a normalized drawing point
data class DrawingPoint(
    val x: Int,
    val y: Int
)

private const val TAG = "CustomIconScreen"

@Composable
fun CustomIconScreen(navController: NavController) {

    // Get the current context for Toast messages
    val context = LocalContext.current

    // Store drawing points in their original order
    val points = remember { mutableStateListOf<Offset>() }

    // Store the normalized points after saving
    var savedPoints by remember {
        mutableStateOf<List<DrawingPoint>>(emptyList())
    }

    // Store the canvas dimensions
    var canvasSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    // Track whether the user is currently drawing
    var isDrawing by remember { mutableStateOf(false) }

    // Track whether the drawing has been completed
    var drawingCompleted by remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(20.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Create your own icon",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        points.isEmpty() -> "Draw with one continuous stroke"
                        isDrawing -> "Drawing..."
                        drawingCompleted -> "Drawing completed!"
                        else -> "Draw with one continuous stroke"
                    },
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Drawing canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .onSizeChanged {
                            canvasSize = it
                        }
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (points.isEmpty() && !drawingCompleted &&
                                        offset.x in 0f..canvasSize.width.toFloat() &&
                                        offset.y in 0f..canvasSize.height.toFloat()
                                    ) {
                                        points.add(offset)
                                        isDrawing = true
                                    }
                                },

                                onDrag = { change, _ ->
                                    if (isDrawing) {
                                        val restrictedX = change.position.x.coerceIn(
                                            0f,
                                            canvasSize.width.toFloat()
                                        )
                                        val restrictedY = change.position.y.coerceIn(
                                            0f,
                                            canvasSize.height.toFloat()
                                        )

                                        points.add(Offset(restrictedX, restrictedY))
                                        change.consume()
                                    }
                                },

                                onDragEnd = {
                                    isDrawing = false
                                    drawingCompleted = points.isNotEmpty()
                                },

                                onDragCancel = {
                                    isDrawing = false
                                    drawingCompleted = points.isNotEmpty()
                                }
                            )
                        }
                ) {

                    if (points.isNotEmpty()) {

                        // Build the drawing path
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)

                            points.drop(1).forEach { point ->
                                lineTo(point.x, point.y)
                            }
                        }

                        // Draw the continuous line
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(
                                width = 6.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Mark the starting point in green
                        drawCircle(
                            color = Color(0xFF2EAD65),
                            radius = 5.dp.toPx(),
                            center = points.first()
                        )

                        // Mark the ending point in red
                        if (drawingCompleted) {
                            drawCircle(
                                color = Color(0xFFE05252),
                                radius = 5.dp.toPx(),
                                center = points.last()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start and end point legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "● Start",
                        color = Color(0xFF2EAD65)
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    Text(
                        text = "● End",
                        color = Color(0xFFE05252)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Clear the drawing and allow a new stroke
                Button(
                    onClick = {
                        points.clear()
                        savedPoints = emptyList()
                        isDrawing = false
                        drawingCompleted = false
                    },
                    enabled = points.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear drawing")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save the simplified drawing and send it to Firebase
                Button(
                    onClick = {

                        Log.d(TAG, "=== 1. Original Points (${points.size}) ===")

                        // Apply RDP algorithm
                        val simplifiedPoints = RPD(points, epsilon = 3.0)

                        Log.d(
                            TAG,
                            "=== 2. Points After RDP (${simplifiedPoints.size}) ==="
                        )

                        // Normalize the points to the range 0-99
                        val pointsToSend = simplifiedPoints.map { point ->
                            DrawingPoint(
                                x = (point.x / canvasSize.width.toFloat() * 99f)
                                    .roundToInt()
                                    .coerceIn(0, 99),

                                y = (point.y / canvasSize.height.toFloat() * 99f)
                                    .roundToInt()
                                    .coerceIn(0, 99)
                            )
                        }

                        savedPoints = pointsToSend

                        // Print the final points
                        savedPoints.forEachIndexed { index, point ->
                            Log.d(
                                TAG,
                                "Saved Point [$index]: x=${point.x}, y=${point.y}"
                            )
                        }

                        // Send the drawing to Firebase
                        FirebaseManager.sendCustomDrawing(
                            points = pointsToSend,

                            onSuccess = {
                                Log.d(TAG, "Drawing uploaded to Firebase!")

                                Toast.makeText(
                                    context,
                                    "Custom icon sent successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },

                            onFailure = { exception ->
                                Log.e(TAG, "Drawing upload failed", exception)

                                Toast.makeText(
                                    context,
                                    "Failed to send custom icon, try again",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    enabled = drawingCompleted && canvasSize != IntSize.Zero,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save drawing")
                }
            }

            // Back button
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
            ) {
                Text("Back to menu")
            }
        }
    }
}

// ==========================================
// Ramer–Douglas–Peucker (RDP) Algorithm Implementation
// ==========================================

fun RPD(pointList: List<Offset>, epsilon: Double): List<Offset> {
    if (pointList.size < 3) return pointList

    var maxDistance = 0.0
    var index = 0
    val end = pointList.size - 1

    for (i in 1 until end) {
        val distance = perpendicularDistance(
            pointList[i],
            pointList[0],
            pointList[end]
        )

        if (distance > maxDistance) {
            maxDistance = distance
            index = i
        }
    }

    return if (maxDistance > epsilon) {
        val leftRec = RPD(
            pointList.subList(0, index + 1),
            epsilon
        )

        val rightRec = RPD(
            pointList.subList(index, pointList.size),
            epsilon
        )

        leftRec.dropLast(1) + rightRec
    } else {
        listOf(pointList.first(), pointList.last())
    }
}

fun perpendicularDistance(p: Offset, p1: Offset, p2: Offset): Double {
    val dx = (p2.x - p1.x).toDouble()
    val dy = (p2.y - p1.y).toDouble()

    val normalLength = hypot(dx, dy)

    if (normalLength == 0.0) {
        return hypot(
            (p.x - p1.x).toDouble(),
            (p.y - p1.y).toDouble()
        )
    }

    return kotlin.math.abs(
        (p.y - p1.y) * dx - (p.x - p1.x) * dy
    ) / normalLength
}
