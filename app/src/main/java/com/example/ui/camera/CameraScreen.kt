package com.example.ui.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreviewX
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.utils.PermissionUtils
import java.io.File
import java.util.Locale

@SuppressLint("RestrictedApi")
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToPreview: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current

    // Observe state flows
    val frontImage by viewModel.frontImage.collectAsState()
    val backImage by viewModel.backImage.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val cameraMode by viewModel.cameraMode.collectAsState()
    val isRecordingVideo by viewModel.isRecordingVideo.collectAsState()
    val locationData by viewModel.locationData.collectAsState()
    val customText by viewModel.customText.collectAsState()
    val cameraPermissionGranted by viewModel.cameraPermissionGranted.collectAsState()
    val locationPermissionGranted by viewModel.locationPermissionGranted.collectAsState()
    val goToPreviewUri by viewModel.navigationToPreview.collectAsState()

    // CameraX helper variables
    val previewView = remember { PreviewView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        scaleType = PreviewView.ScaleType.FILL_CENTER
    }}

    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var recordingTimer by remember { mutableLongStateOf(0L) }
    var isCameraFallback by remember { mutableStateOf(false) }
    var flashState by remember { mutableStateOf(viewModel.settings.flashMode) }
    var activeCamera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    val imageCapture = remember { ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build() 
    }

    val videoCapture = remember {
        val recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(context))
            .build()
        VideoCapture.withOutput(recorder)
    }

    // Handle navigation to Preview screen
    LaunchedEffect(goToPreviewUri) {
        goToPreviewUri?.let { uri ->
            onNavigateToPreview(uri)
            viewModel.clearNavigation()
        }
    }

    // Permission Launchers
    val requestCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setCameraPermissionGranted(granted)
    }

    val requestLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionMap ->
        val fineGranted = permissionMap[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissionMap[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.setLocationPermissionGranted(fineGranted || coarseGranted)
    }

    // Active checking layout
    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
        if (!viewModel.cameraPermissionGranted.value) {
            requestCameraLauncher.launch(PermissionUtils.CAMERA_PERMISSION)
        }
        if (!viewModel.locationPermissionGranted.value) {
            requestLocationLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
        }
        // Synchronize state preferences from SettingsManager when screen gains focus
        viewModel.setCameraMode(viewModel.settings.cameraMode)
        viewModel.setCustomText(viewModel.settings.customText)
        flashState = viewModel.settings.flashMode
    }

    // Bind and Unbind CameraX usecases cleanly when Mode changes
    LaunchedEffect(cameraMode, cameraPermissionGranted) {
        if (!cameraPermissionGranted) return@LaunchedEffect

        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = cameraProviderFuture.get()

            val preview = CameraPreviewX.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val backAvailable = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            val frontAvailable = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)

            if (!backAvailable && !frontAvailable) {
                isCameraFallback = true
                activeCamera = null
            } else {
                val cameraSelector = if (backAvailable) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
                cameraProvider.unbindAll()
                val boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                activeCamera = boundCamera
                isCameraFallback = false
            }
        } catch (e: Exception) {
            Log.e("CameraScreen", "Binding CameraX use cases failed, using emulator mock simulation", e)
            isCameraFallback = true
            activeCamera = null
        }
    }

    // Dynamically toggle torch state when flashState changes or camera binds
    LaunchedEffect(activeCamera, flashState) {
        val camera = activeCamera ?: return@LaunchedEffect
        try {
            val enableTorch = (flashState == "ALWAYS")
            camera.cameraControl.enableTorch(enableTorch)
        } catch (e: Exception) {
            Log.e("CameraScreen", "Failed to set torch state", e)
        }
    }

    // Cleanup active recording on exit
    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            activeRecording = null
        }
    }

    // Timer effect for Video Mode
    LaunchedEffect(isRecordingVideo) {
        if (isRecordingVideo) {
            recordingTimer = 0L
            while (true) {
                kotlinx.coroutines.delay(1000)
                recordingTimer++
            }
        } else {
            recordingTimer = 0L
        }
    }

    if (!cameraPermissionGranted) {
        // Build robust modern permission rationale layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Camera Permission Required",
                    tint = Color(0xFF1A73E8),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Camera Access Required",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "DiviCam needs camera access to capture high-definition photos for automated stamping.",
                    color = Color(0xFF9E9E9E),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { requestCameraLauncher.launch(PermissionUtils.CAMERA_PERMISSION) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("grant_camera_button")
                ) {
                    Text("Grant Camera Permission", fontSize = 16.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.setCameraPermissionGranted(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("simulate_camera_button")
                ) {
                    Text("Bypass / Simulate Camera Feed", fontSize = 15.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D0D0D),
        bottomBar = { Spacer(modifier = Modifier.navigationBarsPadding()) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val screenWidthPx = constraints.maxWidth.toFloat()
            val screenHeightPx = constraints.maxHeight.toFloat()
            // Live Camera Preview block
            if (isCameraFallback) {
                MockCameraPreview(
                    cameraMode = cameraMode,
                    currentStep = currentStep,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ID Viewer Grid Overlay (Draw Rounded bounds for ID frame)
            GuideOverlay(
                isIdMode = (cameraMode == "ID"),
                currentStep = currentStep
            )

            // FLOATING CORNER MINIMAP PREVIEW IS REMOVED FROM FRONT SCREEN PREVIEW (STAMPED ONLY)

            // Top Header App Bar Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Title Display
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DIVI",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 0.8.sp,
                                color = Color.White
                            )
                            Text(
                                text = "CAM",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val hasGps = locationPermissionGranted && locationData != null
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (hasGps) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Text(
                                text = if (hasGps) "GPS LOCKED" else "GPS LOGGING ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // Top Bar settings block
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Location Status / Refresh Location Trigger
                        IconButton(
                            onClick = {
                                if (locationPermissionGranted) {
                                    viewModel.refreshLocation()
                                } else {
                                    requestLocationLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
                                }
                            },
                            modifier = Modifier.testTag("gps_status_button")
                        ) {
                            Icon(
                                imageVector = if (locationPermissionGranted) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                                contentDescription = "GPS Status Indicator",
                                tint = if (locationPermissionGranted && locationData != null) Color(0xFF00E676) else if (locationPermissionGranted) Color(0xFFFFD600) else Color(0xFFFF1744)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Quick Flash Toggle Button
                        val currentFlash = flashState
                        val iconVector = if (currentFlash == "OFF") Icons.Default.FlashOff else Icons.Default.FlashOn
                        val iconTint = when (currentFlash) {
                            "ALWAYS" -> Color(0xFFFFD600)   // Bright glowing yellow for always-on torch
                            "ON_CLICK", "ON" -> Color.White       // Clean white for standard click-flash
                            else -> Color.White.copy(alpha = 0.4f) // Dim/faint for off
                        }
                        IconButton(
                            onClick = {
                                val newVal = when (currentFlash) {
                                    "OFF" -> "ON_CLICK"
                                    "ON_CLICK", "ON" -> "ALWAYS"
                                    else -> "OFF"
                                }
                                viewModel.settings.flashMode = newVal
                                flashState = newVal
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .testTag("quick_flash_toggle_button")
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = "Quick Flash Toggle: $currentFlash",
                                tint = iconTint
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Settings screen redirect button (gear icon)
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings Icon Toggle",
                                tint = Color.White
                            )
                        }
                    }
                }

                // GPS Information Rationale Banner (if location is missing or permission is denied)
                AnimatedVisibility(
                    visible = !locationPermissionGranted,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC1A73E8)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Location Access Needed",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enhance photos with GPS Stamps",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Location is added to your photo timestamp for document trail verification.",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENABLE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(vertical = 6.dp, horizontal = 12.dp)
                                    .clickable {
                                        requestLocationLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
                                    }
                            )
                        }
                    }
                }

                // ID Mode Progress tracker (shows Step Indicator Step 1 of 2 or Step 2 of 2)
                if (cameraMode == "ID") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (currentStep == 1) "STEP 1 OF 2  •  FRONT SIDE" else "STEP 2 OF 2  •  BACK SIDE",
                                    color = if (currentStep == 1) Color(0xFFFFD600) else Color(0xFF00E676),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Flow Controls Block (Kept completely clear and sleek for unobstructed view)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFF0D0D0D).copy(alpha = 0.85f)) // Sleek premium tinted glass container
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // In-front 2-button Selector for Camera Modes (ID Card vs. Single Photo)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val modes = listOf(
                        Pair("ID", "ID Card Mode"),
                        Pair("SINGLE", "Single Photo")
                    )
                    
                    modes.forEach { (modeKey, modeName) ->
                        val isSelected = (cameraMode == modeKey)
                        val icon = if (modeKey == "ID") Icons.Default.CreditCard else Icons.Default.PhotoCamera
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1A73E8) else Color.Transparent)
                                .clickable {
                                    if (!isCapturing) {
                                        viewModel.setCameraMode(modeKey)
                                    }
                                }
                                .testTag("front_tab_mode_${modeKey.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = modeName,
                                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = modeName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                // Action controls row (Thumbnail preview / capture button / retake and shift)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left corner slot: step progress preview thumbnail OR video toggle
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cameraMode == "ID" && frontImage != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    bitmap = frontImage!!.asImageBitmap(),
                                    contentDescription = "Front Side Thumbnail Done",
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Green checkmark animation wrapper
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Check",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } else {
                            // High contrast glass gradient mockup if no image
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (cameraMode == "VIDEO") Icons.Default.Videocam else Icons.Default.PhotoCamera,
                                    contentDescription = "Camera placeholder",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Middle slot: Primary Pulse Capture Button
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val buttonScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.88f else 1.0f,
                        animationSpec = tween(durationMillis = 100),
                        label = "CaptureButtonClickPulse"
                    )

                    Box(
                        modifier = Modifier
                            .scale(buttonScale)
                            .size(76.dp)
                            .border(androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.White, CircleShape)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                if (isCapturing) return@clickable

                                if (isCameraFallback) {
                                    val mockBitmap = createMockCapturedBitmap(context, cameraMode, currentStep)
                                    viewModel.handlePhotoCaptured(mockBitmap, screenWidthPx, screenHeightPx)
                                } else {
                                    // Set CameraX Flash mode from settings dynamically
                                    imageCapture.flashMode = when (viewModel.settings.flashMode) {
                                        "ON_CLICK", "ON" -> ImageCapture.FLASH_MODE_ON
                                        else -> ImageCapture.FLASH_MODE_OFF
                                    }

                                    // Image capture trigger
                                    val executor = ContextCompat.getMainExecutor(context)
                                    imageCapture.takePicture(
                                        executor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                val rotation = image.imageInfo.rotationDegrees
                                                val originalBitmap = image.toBitmap()
                                                image.close()

                                                // Correct portrait rotation of camera raw file
                                                val uprightBitmap = if (rotation != 0) {
                                                    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                                                    Bitmap.createBitmap(
                                                        originalBitmap,
                                                        0,
                                                        0,
                                                        originalBitmap.width,
                                                        originalBitmap.height,
                                                        matrix,
                                                        true
                                                    )
                                                } else originalBitmap

                                                 viewModel.handlePhotoCaptured(uprightBitmap, screenWidthPx, screenHeightPx)
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                Log.e("CameraScreen", "Image capture failure", exception)
                                            }
                                        }
                                    )
                                }
                            }
                            .border(androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF0D0D0D)), CircleShape)
                            .testTag("capture_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF1A73E8))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Capture icon style",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp).align(Alignment.Center)
                            )
                        }
                    }

                    // Right corner slot: RETAKE side button OR Switch/Flip button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cameraMode == "ID" && (frontImage != null || backImage != null)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        if (currentStep == 2) {
                                            // Let user retake Front or Reset all
                                            viewModel.resetIdFlow()
                                        } else {
                                            viewModel.retakeCurrentStep()
                                        }
                                    }
                                    .testTag("retake_side_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retake current steps",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentStep == 2) "Reset" else "Retake",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(52.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gesture Bar Home Indicator (Android Bar Design)
                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                )
            }

            // Spinner Loading Overlay shown while generating/saving composition images
            AnimatedVisibility(
                visible = isCapturing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.width(260.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cached,
                                contentDescription = "Rendering composition icon",
                                tint = Color(0xFF1A73E8),
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Stamping & Saving",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Applying metadata watermark details directly to files.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CornerMiniMap(
    latitude: Double,
    longitude: Double,
    opacity: Float,
    modifier: Modifier = Modifier
) {
    val mapHtml = remember(latitude, longitude) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    width: 100%;
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background-color: #0d0d0d;
                }
                .leaflet-control-zoom, .leaflet-control-attribution {
                    display: none !important;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    center: [$latitude, $longitude],
                    zoom: 14,
                    zoomControl: false,
                    attributionControl: false
                });
                L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                    maxZoom: 20
                }).addTo(map);
                L.circle([$latitude, $longitude], {
                    color: '#1A73E8',
                    fillColor: '#1A73E8',
                    fillOpacity: 0.45,
                    radius: 120
                }).addTo(map);
                L.marker([$latitude, $longitude]).addTo(map);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.15f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        modifier = modifier
            .size(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .alpha(opacity)
    ) {
        AndroidView(
            factory = { ctx ->
                android.webkit.WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    setOnTouchListener { _, _ -> true }
                    loadDataWithBaseURL("https://basemaps.cartocdn.com/", mapHtml, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://basemaps.cartocdn.com/", mapHtml, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun MockCameraPreview(
    cameraMode: String,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1419)),
        contentAlignment = Alignment.Center
    ) {
        val anim = remember { androidx.compose.animation.core.Animatable(0f) }
        LaunchedEffect(Unit) {
            while (true) {
                anim.animateTo(1f, androidx.compose.animation.core.tween(2500, easing = androidx.compose.animation.core.LinearEasing))
                anim.animateTo(0f, androidx.compose.animation.core.tween(2500, easing = androidx.compose.animation.core.LinearEasing))
            }
        }
        val scannerProgress = anim.value
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            Icon(
                imageVector = if (cameraMode == "ID") Icons.Default.CreditCard else Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = Color(0xFF1E88E5).copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (cameraMode == "ID") {
                    if (currentStep == 1) "SIMULATING CHIP ID FRONT" else "SIMULATING MAGNETIC STRIPE BACK"
                } else {
                    "SIMULATING COZY SUNSET PANORAMA"
                },
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Emulator Mode Active • Click capture to stamp",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val h = maxHeight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .offset(y = h * scannerProgress)
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF1A73E8), Color.Transparent)
                        )
                    )
            )
        }
    }
}

fun createMockCapturedBitmap(context: Context, cameraMode: String, currentStep: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(1200, 800, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    // Draw background
    val bgPaint = android.graphics.Paint().apply {
        isAntiAlias = true
    }
    
    if (cameraMode == "ID") {
        // Draw elegant mockup ID card background
        // Card frame
        val cardRect = android.graphics.RectF(80f, 60f, 1120f, 740f)
        
        // Solid dark blue rounded rectangle
        bgPaint.color = android.graphics.Color.parseColor("#15202B")
        canvas.drawRoundRect(cardRect, 40f, 40f, bgPaint)
        
        // Draw card header
        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1DA1F2")
            isAntiAlias = true
            textSize = 48f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        }
        
        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#8899A6")
            isAntiAlias = true
            textSize = 28f
            typeface = android.graphics.Typeface.DEFAULT
        }
        
        val valPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            textSize = 34f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        }
        
        if (currentStep == 1) {
            canvas.drawText("CITIZEN IDENTITY CARD", 160f, 160f, headerPaint)
            
            // Draw a mock photo avatar box
            val avatarRect = android.graphics.RectF(160f, 240f, 440f, 560f)
            bgPaint.color = android.graphics.Color.parseColor("#253341")
            canvas.drawRoundRect(avatarRect, 20f, 20f, bgPaint)
            // Head silhouette
            val pHead = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#1DA1F2")
                isAntiAlias = true
            }
            canvas.drawCircle(300f, 360f, 60f, pHead)
            canvas.drawRoundRect(android.graphics.RectF(210f, 440f, 390f, 540f), 30f, 30f, pHead)
            
            // Draw mock ID fields
            canvas.drawText("FULL NAME", 500f, 260f, labelPaint)
            canvas.drawText("DIVICAM VERIFIED CAPTURE", 500f, 300f, valPaint)
            
            canvas.drawText("DOCUMENT ID", 500f, 370f, labelPaint)
            canvas.drawText("ID-994-023-A78", 500f, 410f, valPaint)
            
            canvas.drawText("ISSUE AUTHORITY", 500f, 480f, labelPaint)
            canvas.drawText("GOVERNMENT OF WEB EMULATOR", 500f, 520f, valPaint)
            
            // Gold Chip
            val chipRect = android.graphics.RectF(920f, 220f, 1040f, 320f)
            bgPaint.color = android.graphics.Color.parseColor("#FFD700")
            canvas.drawRoundRect(chipRect, 12f, 12f, bgPaint)
        } else {
            canvas.drawText("CITIZEN ID - REVERSE SIDE", 160f, 160f, headerPaint)
            
            // Magnetic stripe
            val stripePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#0F1419")
            }
            canvas.drawRect(80f, 220f, 1120f, 340f, stripePaint)
            
            // Signature panel
            val sigPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
            }
            canvas.drawRect(160f, 380f, 760f, 480f, sigPaint)
            
            val textPaintSig = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#0F1419")
                textSize = 32f
                typeface = android.graphics.Typeface.create("Courier", android.graphics.Typeface.ITALIC)
            }
            canvas.drawText("DiviCam Authenticated", 190f, 440f, textPaintSig)
            
            // Barcode blocks
            val barcodePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#253341")
            }
            canvas.drawRect(840f, 380f, 1040f, 580f, barcodePaint)
            
            val textPaintBack = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#8899A6")
                textSize = 24f
            }
            canvas.drawText("FOR EMULATOR TESTING PURPOSE ONLY", 160f, 620f, textPaintBack)
            canvas.drawText("IF FOUND, TOSS TO RECYCLE BIN", 160f, 660f, textPaintBack)
        }
        
    } else {
        // SINGLE PHOTO - Gorgeous landscapes vector generated on canvas
        // Sky sunset gradient
        val density = 800
        val skyPaint = android.graphics.Paint()
        for (y in 0 until density) {
            val ratio = y.toFloat() / density
            val r = (0x1F + ratio * (0x12 - 0x1F)).toInt()
            val g = (0x1A + ratio * (0x12 - 0x1A)).toInt()
            val b = (0x5A + ratio * (0x12 - 0x5A)).toInt()
            
            skyPaint.color = android.graphics.Color.rgb(r, g, b)
            canvas.drawRect(0f, y.toFloat(), 1200f, (y + 1).toFloat(), skyPaint)
        }
        
        // Draw golden sun
        val sunPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FF6B6B")
            isAntiAlias = true
        }
        canvas.drawCircle(600f, 420f, 120f, sunPaint)
        sunPaint.color = android.graphics.Color.parseColor("#FFA07A")
        canvas.drawCircle(600f, 420f, 80f, sunPaint)
        
        // Draw elegant scenic mountains with Path
        val mountPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }
        
        // Mountain 1 (Dark purple)
        mountPaint.color = android.graphics.Color.parseColor("#1E112A")
        val path1 = android.graphics.Path().apply {
            moveTo(0f, 800f)
            lineTo(350f, 480f)
            lineTo(800f, 800f)
            close()
        }
        canvas.drawPath(path1, mountPaint)
        
        // Mountain 2 (Slightly lighter dark)
        mountPaint.color = android.graphics.Color.parseColor("#261435")
        val path2 = android.graphics.Path().apply {
            moveTo(400f, 800f)
            lineTo(850f, 380f)
            lineTo(1200f, 800f)
            close()
        }
        canvas.drawPath(path2, mountPaint)
        
        // Mountain 3 (In-front hill overlay)
        mountPaint.color = android.graphics.Color.parseColor("#0D0614")
        val path3 = android.graphics.Path().apply {
            moveTo(-100f, 800f)
            quadTo(300f, 650f, 700f, 800f)
            quadTo(1000f, 700f, 1300f, 800f)
            close()
        }
        canvas.drawPath(path3, mountPaint)
        
        // Star speckles
        val starPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
        }
        canvas.drawCircle(200f, 150f, 4f, starPaint)
        canvas.drawCircle(950f, 220f, 3f, starPaint)
        canvas.drawCircle(450f, 180f, 2f, starPaint)
        canvas.drawCircle(1100f, 100f, 5f, starPaint)
    }
    
    return bitmap
}

