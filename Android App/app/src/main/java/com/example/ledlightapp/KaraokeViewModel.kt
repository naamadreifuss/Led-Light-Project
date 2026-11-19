package com.example.ledlightapp

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.math.abs

class KaraokeViewModel : ViewModel() {

    var youtubeLink by mutableStateOf("")
        private set

    var videoId by mutableStateOf<String?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var videoTitle by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var lyricsText by mutableStateOf("")
        private set

    private var parsedLyrics = listOf<LrcLine>()

    var currentLineText by mutableStateOf("")
        private set

    private var youtubeDurationSeconds: Int = 0

    // Message if couldn't find lyrics.
    var errorMessage by mutableStateOf("")
        private set



    // Updates the stored YouTube link entered by the user.
    fun onLinkChanged(newLink: String) {
        youtubeLink = newLink
    }

    //Starts the karaoke process by extracting the YouTube video ID
    // and fetching the video details
    fun startKaraoke(): Boolean {

        // Debug log: Check that the karaoke process started.
        Log.d("KaraokeDebug", "startKaraoke called")
        // Debug log: Show the YouTube link entered by the user.
        Log.d("KaraokeDebug", "YouTube link: $youtubeLink")

        val extractedId = extractYoutubeVideoId(youtubeLink)

        // Debug log: Show the extracted YouTube video ID.
        Log.d("KaraokeDebug", "Extracted video ID: $extractedId")

        if (extractedId != null) {
            videoId = extractedId
            isLoading = true
            isPlaying = false
            videoTitle = ""
            lyricsText = "Loading lyrics..."
            currentLineText = ""
            parsedLyrics = emptyList()
            errorMessage = ""

            fetchVideoDetails(extractedId)
            return true
        }

        // Debug log: The YouTube link could not be converted into a video ID.
        Log.d("KaraokeDebug", "Invalid YouTube link - no video ID found")
        return false
    }



    //Fetches video information from YouTube API: cleaned title,duration
    private fun fetchVideoDetails(id: String) {

        // Debug log: Start requesting the YouTube video details.
        Log.d("KaraokeDebug", "Fetching YouTube video details...")

        viewModelScope.launch {
            try {

                val response = YouTubeClient.api.getVideoDetails(
                    videoId = id,
                    apiKey = BuildConfig.YOUTUBE_API_KEY
                )

                // Debug log: YouTube API responded.
                Log.d("KaraokeDebug", "YouTube response received")

                // Debug log: Show how many videos were returned.
                Log.d(
                    "KaraokeDebug",
                    "Number of videos: ${response.items.size}"
                )

                if (response.items.isNotEmpty()) {

                    val video = response.items[0]

                    videoTitle = video.snippet.title

                    // Debug log: Show the original YouTube title.
                    Log.d(
                        "KaraokeDebug",
                        "Video title: $videoTitle"
                    )

                    val cleanedTitle =
                        cleanSongTitle(videoTitle)

                    // Debug log: Show the title after cleaning.
                    Log.d(
                        "KaraokeDebug",
                        "Cleaned title: $cleanedTitle"
                    )

                    youtubeDurationSeconds =
                        Duration.parse(video.contentDetails.duration)
                            .seconds
                            .toInt()

                    // Debug log: Show the YouTube video duration.
                    Log.d(
                        "KaraokeDebug",
                        "Duration: $youtubeDurationSeconds seconds"
                    )

                    // Wait until the lyrics are loaded before starting playback.
                    fetchLyrics(cleanedTitle)

                } else {

                    // Debug log: YouTube returned no videos.
                    Log.d(
                        "KaraokeDebug",
                        "YouTube returned no videos"
                    )

                    videoTitle = "Video not found"
                    lyricsText = "Could not load lyrics"
                    isLoading = false
                }

            } catch (e: Exception) {

                Log.e(
                    "KaraokeDebug",
                    "Error fetching YouTube video details"
                )

                Log.e(
                    "KaraokeDebug",
                    "Exception type: ${e::class.java.name}"
                )

                Log.e(
                    "KaraokeDebug",
                    "Exception message: ${e.message}"
                )

                e.printStackTrace()

                videoTitle = "Error fetching video"
                lyricsText = "Error loading video"
                isLoading = false
            }
        }
    }


    /**
     * Searches LRCLIB for song lyrics and selects the best result according to
     * 1. Only synchronized lyrics are considered.
     * 2. Prefer results matching the artist and song title.
     * 3. Among matching results, choose the closest duration.
     * 4. If there is no exact match, use the best synchronized result.
     */
    private fun fetchLyrics(query: String) {

        // Debug log: Show exactly what is being searched in LRCLIB.
        Log.d(
            "KaraokeDebug",
            "Searching LRCLIB for: $query"
        )
        Log.d(
            "KaraokeDebug",
            "LRCLIB query = $query"
        )

        viewModelScope.launch {
            try {
                val results = LrcLibClient.api.searchLyrics(query)
                // Debug log: Show how many LRCLIB results were returned.
                Log.d(
                    "KaraokeDebug",
                    "LRCLIB results: ${results.size}"
                )
                if (results.isNotEmpty()) {

                    // Print every returned result for debugging.
                    results.forEachIndexed { index, result ->
                        Log.d(
                            "KaraokeDebug",
                            "Result #$index: " +
                                    "track=${result.trackName}, " +
                                    "artist=${result.artistName}, " +
                                    "duration=${result.duration}, " +
                                    "synced=${!result.syncedLyrics.isNullOrEmpty()}"
                        )
                    }

                    // ---------------------------------------------------------
                    // STEP 1:
                    // Keep only results that contain synchronized lyrics.
                    // ---------------------------------------------------------
                    val syncedResults =
                        results.filter {
                            !it.syncedLyrics.isNullOrEmpty()
                        }

                    Log.d(
                        "KaraokeDebug",
                        "Synchronized results: ${syncedResults.size}"
                    )
                    if (syncedResults.isEmpty()) {
                        Log.d(
                            "KaraokeDebug",
                            "No synchronized lyrics found"
                        )
                        errorMessage =
                            "Lyrics were found, but synchronized lyrics are not available."
                        lyricsText = ""
                        currentLineText = ""
                        isLoading = false
                        isPlaying = false
                        videoId = null
                        return@launch
                    }

                    // ---------------------------------------------------------
                    // STEP 2:
                    // Extract artist and title from the YouTube query.
                    // ---------------------------------------------------------
                    val queryParts =
                        query.split("-", limit = 2)

                    val queryArtist =
                        queryParts
                            .getOrNull(0)
                            ?.trim()
                            ?.lowercase()
                            ?: ""

                    val queryTitle =
                        queryParts
                            .getOrNull(1)
                            ?.trim()
                            ?.lowercase()
                            ?: query.lowercase()

                    Log.d(
                        "KaraokeDebug",
                        "Parsed artist: $queryArtist"
                    )
                    Log.d(
                        "KaraokeDebug",
                        "Parsed title: $queryTitle"
                    )

                    // ---------------------------------------------------------
                    // STEP 3:
                    // Give each synchronized result a matching score:
                    // Artist match = +2, Title match  = +2
                    // ---------------------------------------------------------
                    val scoredResults =
                        syncedResults.map { result ->

                            val resultArtist =
                                result.artistName
                                    ?.trim()
                                    ?.lowercase()
                                    ?: ""

                            val resultTitle =
                                result.trackName
                                    ?.trim()
                                    ?.lowercase()
                                    ?: ""
                            var score = 0
                            // Check artist.
                            if (
                                queryArtist.isNotEmpty() &&
                                (
                                        resultArtist == queryArtist ||
                                                resultArtist.contains(queryArtist) ||
                                                queryArtist.contains(resultArtist)
                                        )
                            ) {
                                score += 2
                            }
                            // Check title.
                            if (
                                queryTitle.isNotEmpty() &&
                                (
                                        resultTitle == queryTitle ||
                                                resultTitle.contains(queryTitle) ||
                                                queryTitle.contains(resultTitle)
                                        )
                            ) {
                                score += 2
                            }

                            // Calculate duration difference.
                            val duration =
                                result.duration ?: Double.MAX_VALUE

                            val durationDifference =
                                abs(
                                    duration -
                                            youtubeDurationSeconds
                                )

                            Log.d(
                                "KaraokeDebug",
                                "Candidate: " +
                                        "track=${result.trackName}, " +
                                        "artist=${result.artistName}, " +
                                        "score=$score, " +
                                        "durationDifference=$durationDifference"
                            )
                            ScoredLrcResult(
                                result = result,
                                score = score,
                                durationDifference = durationDifference
                            )
                        }

                    // ---------------------------------------------------------
                    // STEP 4:
                    // First choose the best artist/title match.
                    // Then use duration to choose between those matches
                    // ---------------------------------------------------------

                    val bestScore =
                        scoredResults.maxOf {
                            it.score
                        }
                    val bestCandidates =
                        scoredResults.filter {
                            it.score == bestScore
                        }
                    Log.d(
                        "KaraokeDebug",
                        "Best matching score: $bestScore"
                    )
                    Log.d(
                        "KaraokeDebug",
                        "Candidates with best score: ${bestCandidates.size}"
                    )
                    val selectedCandidate =
                        bestCandidates.minByOrNull {
                            it.durationDifference
                        }
                    val song =
                        selectedCandidate?.result

                    // ---------------------------------------------------------
                    // STEP 5:
                    // Log the final selection.
                    // ---------------------------------------------------------
                    if (selectedCandidate != null) {
                        Log.d(
                            "KaraokeDebug",
                            "Selected LRCLIB result: " +
                                    "#${results.indexOf(selectedCandidate.result)} " +
                                    "${song?.trackName}"
                        )
                        Log.d(
                            "KaraokeDebug",
                            "Selected artist: ${song?.artistName}"
                        )
                        Log.d(
                            "KaraokeDebug",
                            "Selected duration: ${song?.duration}"
                        )
                        Log.d(
                            "KaraokeDebug",
                            "YouTube duration: $youtubeDurationSeconds"
                        )
                        Log.d(
                            "KaraokeDebug",
                            "Duration difference: " +
                                    "${selectedCandidate.durationDifference}"
                        )
                        Log.d(
                            "KaraokeDebug",
                            "Match score: ${selectedCandidate.score}"
                        )
                    }

                    // ---------------------------------------------------------
                    // STEP 6:
                    // Use the selected synchronized lyrics.
                    // ---------------------------------------------------------

                    if (song != null) {

                        val syncedLyrics =
                            song.syncedLyrics

                        if (syncedLyrics.isNullOrEmpty()) {

                            errorMessage =
                                "Lyrics were found, but synchronized lyrics are not available."

                            lyricsText = ""
                            currentLineText = ""

                            isLoading = false
                            isPlaying = false
                            videoId = null

                            return@launch
                        }

                        lyricsText = syncedLyrics

                        parsedLyrics =
                            LrcParser.parse(syncedLyrics)

                        Log.d(
                            "KaraokeDebug",
                            "Parsed lyrics lines: ${parsedLyrics.size}"
                        )

                        if (parsedLyrics.isEmpty()) {

                            errorMessage =
                                "Sorry, we couldn't find synchronized lyrics for this song."

                            lyricsText = ""
                            currentLineText = ""

                            isLoading = false
                            isPlaying = false
                            videoId = null

                            return@launch
                        }

                        // Do not show the first line before its timestamp.
                        currentLineText = ""

                        // Clear the current line in Firebase.
                        Log.d(
                            "KaraokeDebug",
                            "Clearing current line in Firebase"
                        )

                        FirebaseManager.sendCurrentLine(
                            line = "",
                            scrollSpeed = 0.0
                        )

                        // Lyrics are ready - start playback.
                        isLoading = false
                        isPlaying = true

                        Log.d(
                            "KaraokeDebug",
                            "Lyrics ready - starting playback"
                        )

                    } else {

                        Log.d(
                            "KaraokeDebug",
                            "No suitable LRCLIB result found"
                        )

                        errorMessage =
                            "Sorry, we couldn't find synchronized lyrics for this song."

                        lyricsText = ""
                        currentLineText = ""

                        isLoading = false
                        isPlaying = false
                        videoId = null
                    }

                } else {

                    Log.d(
                        "KaraokeDebug",
                        "LRCLIB returned no results"
                    )

                    errorMessage =
                        "Sorry, we couldn't find synchronized lyrics for this song."

                    lyricsText = ""
                    currentLineText = ""

                    isLoading = false
                    isPlaying = false
                    videoId = null
                }

            } catch (e: Exception) {

                // Debug log: Show the LRCLIB error.
                Log.e(
                    "KaraokeDebug",
                    "Error loading lyrics from LRCLIB",
                    e
                )

                errorMessage =
                    "Something went wrong while loading lyrics."

                lyricsText = ""
                currentLineText = ""

                isLoading = false
                isPlaying = false
                videoId = null
            }
        }
    }



    /**
     * Holds an LRCLIB result together with its matching score
     * and its duration difference from the YouTube video.
     */
    private data class ScoredLrcResult(
        val result: LrcSongResponse,
        val score: Int,
        val durationDifference: Double
    )



    //Updates the currently active lyric line according to
    //the current playback time and calculates the required
    //scrolling speed for the LED matrix.
    fun updateTime(currentSeconds: Float) {

        if (parsedLyrics.isEmpty()) return

        val currentMs =
            (currentSeconds * 1000).toLong()

        // Find the index of the last lyric line whose timestamp
        // is less than or equal to the current playback time.
        val currentIndex =
            parsedLyrics.indexOfLast {
                it.timestampMs <= currentMs
            }

        // No lyric line should be displayed yet.
        if (currentIndex == -1) return

        val activeLine =
            parsedLyrics[currentIndex]

        // Get the next lyric line, if it exists.
        val nextLine =
            parsedLyrics.getOrNull(currentIndex + 1)

        // Calculate how long the current line should stay on screen.
        val durationMs =
            if (nextLine != null) {
                nextLine.timestampMs - activeLine.timestampMs
            } else {
                3000L
            }

        // Calculate the scrolling speed needed for this line.
        val scrollSpeed =
            calculateScrollSpeed(
                activeLine.text,
                durationMs
            )

        // Update only when the active line actually changes.
        if (currentLineText != activeLine.text) {

            currentLineText =
                activeLine.text

            // Debug log: Show the line and scrolling speed being sent.
            Log.d(
                "KaraokeDebug",
                "Sending line: '${activeLine.text}', speed: $scrollSpeed"
            )

            FirebaseManager.sendCurrentLine(
                line = activeLine.text,
                scrollSpeed = scrollSpeed.toDouble()
            )
        }
    }



    //Resets all karaoke data and clears the current state.
    fun resetKaraoke() {
        // Debug log: Karaoke state is being reset.
        Log.d(
            "KaraokeDebug",
            "Resetting karaoke"
        )

        FirebaseManager.sendCurrentLine(
            line = "",
            scrollSpeed = 0.0
        )

        isPlaying = false
        videoId = null
        videoTitle = ""
        lyricsText = ""
        currentLineText = ""
        parsedLyrics = emptyList()
        youtubeLink = ""
        isLoading = false
        youtubeDurationSeconds = 0
        errorMessage = ""
    }



    //Extracts the YouTube video ID from different URL formats,
    //including regular links, youtu.be links, shorts and embeds.
    fun extractYoutubeVideoId(url: String): String? {

        return try {

            val uri = Uri.parse(url)

            val videoId = when {

                uri.getQueryParameter("v") != null ->
                    uri.getQueryParameter("v")

                uri.host?.contains("youtu.be") == true ->
                    uri.pathSegments.firstOrNull()

                uri.pathSegments.firstOrNull() == "shorts" ->
                    uri.pathSegments.getOrNull(1)

                uri.pathSegments.firstOrNull() == "embed" ->
                    uri.pathSegments.getOrNull(1)

                else ->
                    null
            }

            videoId?.takeIf {
                it.length == 11
            }

        } catch (e: Exception) {

            // Debug log: Show an error if the YouTube URL cannot be parsed.
            Log.e(
                "KaraokeDebug",
                "Error extracting YouTube video ID",
                e
            )
            null
        }
    }



    //Cleans the YouTube title by removing unnecessary metadata
    private fun cleanSongTitle(title: String): String {

        var cleaned = title

        val removePatterns = listOf(
            "\\(official.*?\\)",
            "\\[official.*?\\]",
            "\\(lyrics.*?\\)",
            "\\[lyrics.*?\\]",
            "\\(audio.*?\\)",
            "\\[audio.*?\\]",
            "\\(4k.*?\\)",
            "\\[4k.*?\\]",
            "\\(hd.*?\\)",
            "\\[hd.*?\\]",
            "\\(hq.*?\\)",
            "\\[hq.*?\\]",
            "\\(remastered.*?\\)",
            "\\[remastered.*?\\]",
            "\\(live.*?\\)",
            "\\[live.*?\\]"
        )

        removePatterns.forEach { pattern ->
            cleaned = cleaned.replace(
                Regex(pattern, RegexOption.IGNORE_CASE),
                ""
            )
        }

        cleaned = cleaned
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()

        return cleaned
    }



    //Calculates the scrolling speed (pixels per second)
    //required for the current lyric line so it finishes
    //scrolling when the next lyric line starts.

    private fun calculateScrollSpeed(
        text: String,
        durationMs: Long
    ): Float {

        val matrixWidth = 32
        val characterWidth = 6

        val textWidth =
            text.length * characterWidth

        val totalDistance =
            matrixWidth + textWidth

        return totalDistance /
                (durationMs / 1000f)
    }
}