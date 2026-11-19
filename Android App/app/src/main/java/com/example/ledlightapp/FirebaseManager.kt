package com.example.ledlightapp

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

object FirebaseManager {

    // Firebase Realtime Database instance.
    private val database = FirebaseDatabase.getInstance()

    fun setColor(
        r: Int,
        g: Int,
        b: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val colorMap = mapOf(
            "r" to r,
            "g" to g,
            "b" to b
        )

        database.getReference("commands/colors")
            .setValue(colorMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Read saved color from Firebase.
    fun readColor(onResult: (Int, Int, Int) -> Unit) {
        Log.d("FirebaseDebug", "readColor called")

        database.getReference("commands/colors")
            .get()
            .addOnSuccessListener {
                val r = it.child("r").getValue(Int::class.java) ?: 255
                val g = it.child("g").getValue(Int::class.java) ?: 255
                val b = it.child("b").getValue(Int::class.java) ?: 255

                onResult(r, g, b)
            }
    }

    fun enterText(
        string: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        database.getReference("commands/text")
            .setValue(string.trim())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Send the current karaoke line and its scroll speed to Firebase.
    fun sendCurrentLine(
        line: String,
        scrollSpeed: Double
    ) {
        val data = mapOf(
            "currentLine" to line,
            "scrollSpeed" to scrollSpeed
        )

        database.getReference("commands/currentLine")
            .setValue(data)
    }

    fun sendShape(
        string: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        database.getReference("commands/icon")
            .setValue(string)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Send LED image pixel data (32x16 = 512 ints) to Firebase.
    fun sendImagePixels(
        pixels: List<Int>,
        width: Int = 32,
        height: Int = 16,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = mapOf(
            "pixels" to pixels,
            "width" to width,
            "height" to height
        )

        database.getReference("commands/image")
            .setValue(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Send the selected music mode to Firebase.
    fun musicMode(musicMode: String) {
        database.getReference("commands/musicMode")
            .setValue(musicMode)
    }

    // Update the current matrix status.
    fun updateStatus(
        mode: String,
        content: String
    ) {
        val status = mapOf(
            "activeMode" to mode,
            "content" to content
        )

        database.getReference("status")
            .setValue(status)
    }

    // Send the measured heart rate and update the active mode and content together.
    fun sendHeartRate(
        bpm: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updates = mapOf<String, Any>(
            "commands/bpm" to bpm,
            "status/activeMode" to "heart rate",
            "status/content" to bpm
        )

        database.reference
            .updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Send a custom drawing with a dynamic number of points to Firebase.
    fun sendCustomDrawing(
        points: List<DrawingPoint>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Convert the points into a map with sequential numeric keys.
        val pointsMap = points.mapIndexed { index, point ->
            index.toString() to mapOf(
                "x" to point.x,
                "y" to point.y
            )
        }.toMap()

        // Prepare the complete drawing data.
        val drawingData = mapOf(
            "pointCount" to points.size,
            "points" to pointsMap
        )

        // Update the drawing data and active mode together.
        val updates = mapOf<String, Any>(
            "commands/customIcon" to drawingData,
            "status/activeMode" to "custom icon"
        )

        database.reference.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("FirebaseDebug", "Custom drawing sent successfully")
                Log.d("FirebaseDebug", "Number of points: ${points.size}")
                Log.d("FirebaseDebug", "Active mode updated to iconDraw")
                onSuccess()
            }
            .addOnFailureListener {
                Log.e("FirebaseDebug", "Failed to send custom drawing", it)
                onFailure(it)
            }
    }

    fun readStatus(
        onResult: (String, String) -> Unit
    ) {
        database.getReference("status")
            .get()
            .addOnSuccessListener {
                val mode = it.child("activeMode")
                    .getValue(String::class.java)
                    ?: "UNKNOWN"

                // Convert content to String because some modes store numeric content (such as BPM).
                val content = it.child("content").value?.toString() ?: ""

                onResult(mode, content)
            }
    }
}
