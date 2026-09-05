package com.example.ui.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreviewX
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.utils.PermissionUtils

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToPreview: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State bindings from ViewModel
    val isCapturing by viewModel.isCapturing.collectAsState()
    val cameraMode by viewModel.cameraMode.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val frontImage by viewModel.frontImage.collectAsState()
    val backImage by viewModel.backImage.collectAsState()
    val locationData by viewModel.locationData.collectAsState()
    val customText by viewModel.customText.collectAsState()
    val allStampsEnabled by viewModel.enableAllStamps.collectAsState()
    val cameraPermissionGranted by viewModel.cameraPermissionGranted.collectAsState()
    val locationPermissionGranted by viewModel.locationPermissionGranted.collectAsState()
    val navigationUri by viewModel.navigationToPreview.collectAsState()

    // Local UI states
    var flashState by remember { mutableStateOf(viewModel.settings.flashMode) }
    var isCameraFallback by remember { mutableStateOf(false) }
    var showCombineDialog by remember { mutableStateOf(false) }
    var activeCamera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    // PreviewView for CameraX
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // CameraX ImageCapture UseCase
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Navigation trigger when capture/combine is finished
    LaunchedEffect(navigationUri) {
        navigationUri?.let { uri ->
            viewModel.clearNavigation()
            onNavigateToPreview(uri)
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

    val openPhoneGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onNavigateToPreview(uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
        if (!viewModel.cameraPermissionGranted.value) {
            requestCameraLauncher.launch(PermissionUtils.CAMERA_PERMISSION)
        }
        if (!viewModel.locationPermissionGranted.value) {
            requestLocationLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
        }
        viewModel.setCameraMode(viewModel.settings.cameraMode)
        viewModel.setCustomText(viewModel.settings.customText)
        flashState = viewModel.settings.flashMode
    }

    // Bind and Unbind CameraX when cameraMode changes
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
            Log.e("CameraScreen", "Binding CameraX use cases failed, using emulator simulation", e)
            isCameraFallback = true
            activeCamera = null
        }
    }

    // Toggle torch state
    LaunchedEffect(activeCamera, flashState) {
        val camera = activeCamera ?: return@LaunchedEffect
        try {
            val enableTorch = (flashState == "ALWAYS")
            camera.cameraControl.enableTorch(enableTorch)
        } catch (e: Exception) {
            Log.e("CameraScreen", "Failed to set torch state", e)
        }
    }

    if (!cameraPermissionGranted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070C18))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Camera Access Required",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "DiviCam Access Required",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "DiviCam requires camera access to capture documents and photos for verified stamping.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = { requestCameraLauncher.launch(PermissionUtils.CAMERA_PERMISSION) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("grant_camera_button")
                ) {
                    Text("Grant Camera Access", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.setCameraPermissionGranted(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("simulate_camera_button")
                ) {
                    Text("Bypass / Simulate Camera Feed", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF070C18),
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

            // ID Viewer Grid Overlay
            GuideOverlay(
                isIdMode = (cameraMode == "ID"),
                currentStep = currentStep
            )

            // ON-SCREEN CORNER MINI-MAP (Loaded fast from mobile location, borderless, transparent, OSM tile)
            if (viewModel.settings.showMiniMap && allStampsEnabled && locationData != null) {
                val mapPos = viewModel.settings.miniMapPosition
                val alignment = when (mapPos) {
                    "Top-left" -> Alignment.TopStart
                    "Bottom-left" -> Alignment.BottomStart
                    "Bottom-right" -> Alignment.BottomEnd
                    else -> Alignment.TopEnd
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = if (mapPos.startsWith("Top")) 84.dp else 16.dp,
                            bottom = if (mapPos.startsWith("Bottom")) 190.dp else 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                ) {
                    CornerMiniMap(
                        latitude = locationData!!.latitude,
                        longitude = locationData!!.longitude,
                        opacity = viewModel.settings.miniMapOpacity,
                        modifier = Modifier.align(alignment)
                    )
                }
            }

            // LIVE STAMP HUD ON PREVIEW
            if (allStampsEnabled) {
                val stampPos = viewModel.settings.timestampPosition
                val stampAlignment = when (stampPos) {
                    "Top-left" -> Alignment.TopStart
                    "Top-right" -> Alignment.TopEnd
                    "Bottom-right" -> Alignment.BottomEnd
                    else -> Alignment.BottomStart
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = if (stampPos.startsWith("Top")) 80.dp else 16.dp,
                            bottom = if (stampPos.startsWith("Bottom")) 180.dp else 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                ) {
                    Surface(
                        color = Color(0x730F172A),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(stampAlignment)
                            .testTag("live_stamp_preview_hud")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            if (viewModel.settings.showBrandingBadge) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "DIVICAM • divicam.app",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            if (customText.isNotEmpty()) {
                                Text(text = customText, color = Color.White, fontSize = 11.sp)
                            }
                            if (viewModel.settings.showGpsCoords && locationData != null) {
                                Text(
                                    text = locationData!!.formattedCoordinates,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Top Header Controls
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
                    // DiviCam Brand Header
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DIVI",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.8.sp,
                                color = Color.White
                            )
                            Text(
                                text = "CAM",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(15.dp)
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
                                text = if (hasGps) "GPS LOCKED • FAST" else "GPS SEARCHING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Top Bar Action Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Master Stamp Toggle (Stamps ON / OFF)
                        IconButton(
                            onClick = {
                                val state = viewModel.toggleAllStamps()
                                Toast.makeText(
                                    context,
                                    if (state) "Stamps ENABLED" else "All stamps TURNED OFF",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (allStampsEnabled) Color(0xFF0284C7).copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f)
                                )
                                .testTag("quick_toggle_stamps_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = if (allStampsEnabled) "Turn stamps off" else "Turn stamps on",
                                tint = if (allStampsEnabled) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // GPS Status / Refresh trigger
                        IconButton(
                            onClick = {
                                if (locationPermissionGranted) {
                                    viewModel.refreshLocation()
                                    Toast.makeText(context, "Location refreshed", Toast.LENGTH_SHORT).show()
                                } else {
                                    requestLocationLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("gps_status_button")
                        ) {
                            Icon(
                                imageVector = if (locationPermissionGranted) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                                contentDescription = "GPS Status Indicator",
                                tint = if (locationPermissionGranted && locationData != null) Color(0xFF10B981) else Color(0xFFFFD600)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Quick Flash Toggle
                        val currentFlash = flashState
                        val iconVector = if (currentFlash == "OFF") Icons.Default.FlashOff else Icons.Default.FlashOn
                        val iconTint = when (currentFlash) {
                            "ALWAYS" -> Color(0xFFFFD600)
                            "ON_CLICK", "ON" -> Color.White
                            else -> Color.White.copy(alpha = 0.4f)
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
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("quick_flash_toggle_button")
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = "Flash: $currentFlash",
                                tint = iconTint
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Settings screen button
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                }

                // GPS Information Rationale Banner
                AnimatedVisibility(
                    visible = !locationPermissionGranted,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xD90284C7)),
                        shape = RoundedCornerShape(10.dp)
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
                                    text = "Enable GPS Location Stamps",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Location is added to photo timestamps for document audit trails.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ALLOW",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(vertical = 6.dp, horizontal = 12.dp)
                                    .clickable {
                                        requestLocationLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
                                    }
                            )
                        }
                    }
                }

                // ID Mode Progress tracker
                if (cameraMode == "ID") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (currentStep == 1) "STEP 1 OF 2  •  FRONT SIDE" else "STEP 2 OF 2  •  BACK SIDE",
                                    color = if (currentStep == 1) Color(0xFF38BDF8) else Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation & Capture Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFF070C18).copy(alpha = 0.94f))
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.15f)),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // If in ID Mode Step 2, show a Reset / Retake chip
                if (cameraMode == "ID" && (frontImage != null || backImage != null)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .clickable {
                                viewModel.resetIdFlow()
                                Toast.makeText(context, "ID Capture Reset", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset ID Capture", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Mode Selector (ID Card vs. Single Photo)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
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
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF0284C7) else Color.Transparent)
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
                                    tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = modeName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // Main Action Controls Row (Gallery | Capture | 2-in-1 Combine)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT SLOT: Phone Gallery Button (Robust resolution + Photo Picker fallback)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)), RoundedCornerShape(14.dp))
                            .clickable {
                                var launched = false
                                try {
                                    val galleryCategoryIntent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_APP_GALLERY)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    if (galleryCategoryIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(galleryCategoryIntent)
                                        launched = true
                                    }
                                } catch (e: Exception) {
                                    Log.d("CameraScreen", "Gallery category launch failed", e)
                                }

                                if (!launched) {
                                    try {
                                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        if (viewIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(viewIntent)
                                            launched = true
                                        }
                                    } catch (e: Exception) {
                                        Log.d("CameraScreen", "View images intent failed", e)
                                    }
                                }

                                if (!launched) {
                                    try {
                                        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        if (pickIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(pickIntent)
                                            launched = true
                                        }
                                    } catch (e: Exception) {
                                        Log.d("CameraScreen", "ACTION_PICK intent failed", e)
                                    }
                                }

                                if (!launched) {
                                    openPhoneGalleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            }
                            .testTag("gallery_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cameraMode == "ID" && frontImage != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    bitmap = frontImage!!.asImageBitmap(),
                                    contentDescription = "Front Side Thumbnail Done",
                                    modifier = Modifier.fillMaxSize()
                                )
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Collections,
                                    contentDescription = "Open Phone Gallery",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Gallery",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // MIDDLE SLOT: Pulse Capture Button
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
                            .border(androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)), CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.White, CircleShape)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                if (isCapturing) return@clickable

                                if (isCameraFallback) {
                                    val mockBitmap = createMockCapturedBitmap(context, cameraMode, currentStep)
                                    viewModel.handlePhotoCaptured(mockBitmap, screenWidthPx, screenHeightPx)
                                } else {
                                    imageCapture.flashMode = when (viewModel.settings.flashMode) {
                                        "ON_CLICK", "ON" -> ImageCapture.FLASH_MODE_ON
                                        else -> ImageCapture.FLASH_MODE_OFF
                                    }

                                    val executor = ContextCompat.getMainExecutor(context)
                                    imageCapture.takePicture(
                                        executor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                val rotation = image.imageInfo.rotationDegrees
                                                val originalBitmap = image.toBitmap()
                                                image.close()

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
                            .border(androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF070C18)), CircleShape)
                            .testTag("capture_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Capture",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp).align(Alignment.Center)
                            )
                        }
                    }

                    // RIGHT SLOT: Upload 2 Photos & Combine Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF0284C7))))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)), RoundedCornerShape(14.dp))
                            .clickable {
                                showCombineDialog = true
                            }
                            .testTag("upload_combine_photos_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload and combine 2 photos into one",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "2-in-1",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gesture Bar Home Indicator
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }

            // Spinner Loading Overlay
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
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
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "DiviCam Processing",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Stamping verified GPS, date and metadata to image.",
                                color = Color(0xFF94A3B8),
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

    // UPLOAD & COMBINE 2 PHOTOS DIALOG
    if (showCombineDialog) {
        CombineTwoPhotosDialog(
            onDismiss = { showCombineDialog = false },
            onCombinePhotos = { photo1, photo2, applyStamps ->
                viewModel.combineAndSaveTwoBitmaps(photo1, photo2, applyStamps)
            }
        )
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
                    background-color: transparent;
                }
                .leaflet-control-zoom, .leaflet-control-attribution {
                    display: none !important;
                }
                .leaflet-tile {
                    filter: brightness(0.7) invert(1) contrast(3) hue-rotate(200deg) saturate(0.4) brightness(0.8);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    center: [$latitude, $longitude],
                    zoom: 15,
                    zoomControl: false,
                    attributionControl: false
                });
                L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);
                L.circle([$latitude, $longitude], {
                    color: '#38BDF8',
                    fillColor: '#38BDF8',
                    fillOpacity: 0.4,
                    radius: 90
                }).addTo(map);
                L.circleMarker([$latitude, $longitude], {
                    color: '#FFFFFF',
                    fillColor: '#38BDF8',
                    fillOpacity: 1.0,
                    radius: 5
                }).addTo(map);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .size(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .alpha(opacity)
    ) {
        AndroidView(
            factory = { ctx ->
                android.webkit.WebView(ctx).apply {
                    setBackgroundColor(0)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    setOnTouchListener { _, _ -> true }
                    loadDataWithBaseURL("https://tile.openstreetmap.org/", mapHtml, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://tile.openstreetmap.org/", mapHtml, "text/html", "UTF-8", null)
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
            .background(Color(0xFF070C18)),
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
                tint = Color(0xFF38BDF8).copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (cameraMode == "ID") {
                    if (currentStep == 1) "SIMULATING DOCUMENT FRONT" else "SIMULATING DOCUMENT BACK"
                } else {
                    "SIMULATING SCENIC CAMERA FEED"
                },
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Emulator Mode Active • Click capture to stamp",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val h = maxHeight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .offset(y = h * scannerProgress)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF38BDF8), Color.Transparent)
                        )
                    )
            )
        }
    }
}

fun createMockCapturedBitmap(context: Context, cameraMode: String, currentStep: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(1200, 800, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val bgPaint = android.graphics.Paint().apply { isAntiAlias = true }

    if (cameraMode == "ID") {
        val cardRect = android.graphics.RectF(80f, 60f, 1120f, 740f)
        bgPaint.color = android.graphics.Color.parseColor("#0F172A")
        canvas.drawRoundRect(cardRect, 40f, 40f, bgPaint)

        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#38BDF8")
            isAntiAlias = true
            textSize = 48f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        }

        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
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

            val avatarRect = android.graphics.RectF(160f, 240f, 440f, 560f)
            bgPaint.color = android.graphics.Color.parseColor("#1E293B")
            canvas.drawRoundRect(avatarRect, 20f, 20f, bgPaint)

            val pHead = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#38BDF8")
                isAntiAlias = true
            }
            canvas.drawCircle(300f, 360f, 60f, pHead)
            canvas.drawRoundRect(android.graphics.RectF(210f, 440f, 390f, 540f), 30f, 30f, pHead)

            canvas.drawText("FULL NAME", 500f, 260f, labelPaint)
            canvas.drawText("DIVICAM VERIFIED CAPTURE", 500f, 300f, valPaint)

            canvas.drawText("DOCUMENT ID", 500f, 370f, labelPaint)
            canvas.drawText("ID-994-023-A78", 500f, 410f, valPaint)

            canvas.drawText("ISSUE AUTHORITY", 500f, 480f, labelPaint)
            canvas.drawText("DIVICAM VERIFICATION SYSTEM", 500f, 520f, valPaint)

            val chipRect = android.graphics.RectF(920f, 220f, 1040f, 320f)
            bgPaint.color = android.graphics.Color.parseColor("#F59E0B")
            canvas.drawRoundRect(chipRect, 12f, 12f, bgPaint)
        } else {
            canvas.drawText("CITIZEN ID - REVERSE SIDE", 160f, 160f, headerPaint)

            val stripePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#020617")
            }
            canvas.drawRect(80f, 220f, 1120f, 340f, stripePaint)

            val sigPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
            }
            canvas.drawRect(160f, 380f, 760f, 480f, sigPaint)

            val textPaintSig = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#0F172A")
                textSize = 32f
                typeface = android.graphics.Typeface.create("Courier", android.graphics.Typeface.ITALIC)
            }
            canvas.drawText("DiviCam Authenticated", 190f, 440f, textPaintSig)

            val barcodePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#1E293B")
            }
            canvas.drawRect(840f, 380f, 1040f, 580f, barcodePaint)

            val textPaintBack = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = 24f
            }
            canvas.drawText("AUTHENTICATED BY DIVICAM • divicam.app", 160f, 620f, textPaintBack)
            canvas.drawText("CONTACT: mail@shak.xyz", 160f, 660f, textPaintBack)
        }
    } else {
        val density = 800
        val skyPaint = android.graphics.Paint()
        for (y in 0 until density) {
            val ratio = y.toFloat() / density
            val r = (0x07 + ratio * (0x0F - 0x07)).toInt()
            val g = (0x0C + ratio * (0x17 - 0x0C)).toInt()
            val b = (0x18 + ratio * (0x2A - 0x18)).toInt()
            skyPaint.color = android.graphics.Color.rgb(r, g, b)
            canvas.drawRect(0f, y.toFloat(), 1200f, (y + 1).toFloat(), skyPaint)
        }

        val sunPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#38BDF8")
            isAntiAlias = true
        }
        canvas.drawCircle(600f, 420f, 120f, sunPaint)

        val mountPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.parseColor("#1E293B")
        }

        val path1 = android.graphics.Path().apply {
            moveTo(0f, 800f)
            lineTo(350f, 480f)
            lineTo(800f, 800f)
            close()
        }
        canvas.drawPath(path1, mountPaint)

        val path2 = android.graphics.Path().apply {
            moveTo(400f, 800f)
            lineTo(850f, 380f)
            lineTo(1200f, 800f)
            close()
        }
        canvas.drawPath(path2, mountPaint)
    }

    return bitmap
}
