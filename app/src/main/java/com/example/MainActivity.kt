package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.camera.CameraScreen
import com.example.ui.camera.CameraViewModel
import com.example.ui.preview.PreviewScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "camera",
        modifier = modifier
    ) {
        // 1. Live Camera / Main Action Screen
        composable("camera") {
            CameraScreen(
                viewModel = cameraViewModel,
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToPreview = { uri ->
                    val encodedUri = Uri.encode(uri.toString())
                    navController.navigate("preview?uri=$encodedUri")
                }
            )
        }

        // 2. Settings Preference Manager Screen
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 3. Output Saved File Preview & Sharing Sheet Screen
        composable(
            route = "preview?uri={uri}",
            arguments = listOf(
                navArgument("uri") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("uri") ?: ""
            val fileUri = Uri.parse(Uri.decode(encodedUri))

            PreviewScreen(
                fileUri = fileUri,
                viewModel = cameraViewModel,
                onRetakeAll = {
                    cameraViewModel.resetIdFlow()
                    navController.navigate("camera") {
                        popUpTo("camera") { inclusive = true }
                    }
                },
                onDone = {
                    cameraViewModel.resetIdFlow()
                    navController.navigate("camera") {
                        popUpTo("camera") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
