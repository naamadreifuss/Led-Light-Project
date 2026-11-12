
package com.example.ledlightapp

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun App() {
    val navController = rememberNavController()

    // Get the currently displayed screen
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    // Return to the home screen when Back is pressed on another screen
    BackHandler(enabled = currentRoute != "menu") {
        navController.navigate("menu") {
            popUpTo("menu") {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = "menu"
    ) {
        composable("menu") { MenuScreen(navController) }
        composable("colors") { ColorsScreen(navController) }
        composable("text") { TextScreen(navController) }
        composable("draw") { DrawingIconsScreen(navController) }
        composable("music") { MusicBeatsScreen(navController)}
        composable("karaoke") { KaraokeScreen(navController) }
        composable("image") { ImageModeScreen(navController) }
        composable("galleryImage") { GalleryImageScreen(navController) }
        composable("builtInImages") { BuiltInImagesScreen(navController) }
        composable("heart") { HeartBeatScreen(navController) }
        composable("aboutUs") { AboutUsScreen(navController) }
        composable("firstSetup") { FirstSetupScreen(navController) }
        composable("customIcon") { CustomIconScreen(navController) }
    }
}
