package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocationRepository
import com.example.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val scrollState = rememberScrollState()

    // Local configuration states
    var enableAllStamps by remember { mutableStateOf(settingsManager.enableAllStamps) }
    var timestampPosition by remember { mutableStateOf(settingsManager.timestampPosition) }
    var textColor by remember { mutableStateOf(settingsManager.textColor) }
    var dateFormat by remember { mutableStateOf(settingsManager.dateFormat) }
    var isTimeFormat24h by remember { mutableStateOf(settingsManager.isTimeFormat24h) }
    var showGpsCoords by remember { mutableStateOf(settingsManager.showGpsCoords) }
    var showGpsAddress by remember { mutableStateOf(settingsManager.showGpsAddress) }
    var coordFormat by remember { mutableStateOf(settingsManager.coordFormat) }
    var coordPrecision by remember { mutableStateOf(settingsManager.coordPrecision) }
    var coordPrefix by remember { mutableStateOf(settingsManager.coordPrefix) }
    var showAltitude by remember { mutableStateOf(settingsManager.showAltitude) }
    var altitudeUnit by remember { mutableStateOf(settingsManager.altitudeUnit) }
    var showCompassHeading by remember { mutableStateOf(settingsManager.showCompassHeading) }
    var useCardinalDirections by remember { mutableStateOf(settingsManager.useCardinalDirections) }
    var manualCoordsOverride by remember { mutableStateOf(settingsManager.manualCoordsOverride) }
    var customText by remember { mutableStateOf(settingsManager.customText) }
    var saveLocation by remember { mutableStateOf(settingsManager.saveLocation) }
    var photoQuality by remember { mutableFloatStateOf(settingsManager.photoQuality.toFloat()) }
    var showMiniMap by remember { mutableStateOf(settingsManager.showMiniMap) }
    var miniMapOpacity by remember { mutableFloatStateOf(settingsManager.miniMapOpacity) }
    var miniMapPosition by remember { mutableStateOf(settingsManager.miniMapPosition) }
    var mapBorderEnabled by remember { mutableStateOf(settingsManager.mapBorderEnabled) }
    var mapTransparentBg by remember { mutableStateOf(settingsManager.mapTransparentBg) }
    var stampBackgroundOpacity by remember { mutableFloatStateOf(settingsManager.stampBackgroundOpacity) }
    var stampBorderEnabled by remember { mutableStateOf(settingsManager.stampBorderEnabled) }
    var stampSizeScale by remember { mutableFloatStateOf(settingsManager.stampSizeScale) }
    var showBrandingBadge by remember { mutableStateOf(settingsManager.showBrandingBadge) }
    var imageFormat by remember { mutableStateOf(settingsManager.imageFormat) }
    var imageResolution by remember { mutableStateOf(settingsManager.imageResolution) }
    var shutterMode by remember { mutableStateOf(settingsManager.shutterMode) }

    // Dropdown toggle states
    var posExpanded by remember { mutableStateOf(false) }
    var colorExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }
    var mapPosExpanded by remember { mutableStateOf(false) }
    var coordFormatExpanded by remember { mutableStateOf(false) }
    var precisionExpanded by remember { mutableStateOf(false) }
    var prefixExpanded by remember { mutableStateOf(false) }
    var altitudeUnitExpanded by remember { mutableStateOf(false) }

    val positions = settingsManager.getAllPositions()
    val colors = settingsManager.getAllColors()
    val dateFormats = settingsManager.getAllDateFormats()
    val coordFormats = settingsManager.getAllCoordFormats()
    val coordPrecisions = settingsManager.getAllPrecisions()
    val coordPrefixes = settingsManager.getAllCoordPrefixes()
    val altitudeUnits = settingsManager.getAllAltitudeUnits()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF070C18),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DiviCam Preferences", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_to_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back to camera view",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            settingsManager.enableAllStamps = true
                            settingsManager.timestampPosition = "Bottom-left"
                            settingsManager.textColor = "White"
                            settingsManager.dateFormat = "DD/MM/YYYY"
                            settingsManager.isTimeFormat24h = true
                            settingsManager.showGpsCoords = true
                            settingsManager.showGpsAddress = true
                            settingsManager.coordFormat = "Decimal"
                            settingsManager.coordPrecision = 5
                            settingsManager.coordPrefix = "GPS"
                            settingsManager.showAltitude = false
                            settingsManager.altitudeUnit = "Meters (m)"
                            settingsManager.showCompassHeading = false
                            settingsManager.useCardinalDirections = true
                            settingsManager.manualCoordsOverride = ""
                            settingsManager.customText = ""
                            settingsManager.saveLocation = "Gallery"
                            settingsManager.photoQuality = 92
                            settingsManager.showMiniMap = true
                            settingsManager.miniMapOpacity = 0.65f
                            settingsManager.miniMapPosition = "Top-right"
                            settingsManager.mapBorderEnabled = false
                            settingsManager.mapTransparentBg = true
                            settingsManager.stampBackgroundOpacity = 0.45f
                            settingsManager.stampBorderEnabled = false
                            settingsManager.showBrandingBadge = true

                            enableAllStamps = true
                            timestampPosition = "Bottom-left"
                            textColor = "White"
                            dateFormat = "DD/MM/YYYY"
                            isTimeFormat24h = true
                            showGpsCoords = true
                            showGpsAddress = true
                            coordFormat = "Decimal"
                            coordPrecision = 5
                            coordPrefix = "GPS"
                            showAltitude = false
                            altitudeUnit = "Meters (m)"
                            showCompassHeading = false
                            useCardinalDirections = true
                            manualCoordsOverride = ""
                            customText = ""
                            saveLocation = "Gallery"
                            photoQuality = 92f
                            showMiniMap = true
                            miniMapOpacity = 0.65f
                            miniMapPosition = "Top-right"
                            mapBorderEnabled = false
                            mapTransparentBg = true
                            stampBackgroundOpacity = 0.45f
                            stampBorderEnabled = false
                            settingsManager.stampSizeScale = 1.0f
                            stampSizeScale = 1.0f
                            showBrandingBadge = true
                        },
                        modifier = Modifier.testTag("reset_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset settings to defaults",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070C18))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // BRANDING HEADER CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF38BDF8)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "DiviCam Logo",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DiviCam",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified App",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Official Domain: divicam.app",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Contact: mail@shak.xyz",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Visit website button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://divicam.app"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore if no browser
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("divicam.app", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Email support button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:mail@shak.xyz")
                                            putExtra(Intent.EXTRA_SUBJECT, "DiviCam Support & Feedback")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("mail@shak.xyz", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // MASTER STAMP TOGGLE CARD
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (enableAllStamps) Color(0xFF1E293B) else Color(0xFF181B22)
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (enableAllStamps) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "All Stamps On / Off",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (enableAllStamps) Color(0xFF0284C7) else Color(0xFF475569))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (enableAllStamps) "ACTIVE" else "OFF",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (enableAllStamps)
                                "All metadata stamps, mini-map, GPS & time will be applied."
                            else
                                "All stamps completely removed for clean original captures.",
                            color = if (enableAllStamps) Color(0xFFBAE6FD) else Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = enableAllStamps,
                        onCheckedChange = {
                            enableAllStamps = it
                            settingsManager.enableAllStamps = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("switch_master_all_stamps")
                    )
                }
            }

            // MINI-MAP OPTIONS & STYLING
            Text(
                text = "MINI-MAP CONTROLS & TRANSPARENCY",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Show map toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp Mini-Map on Photo", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Render fast mobile location map onto captured photos.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(
                            checked = showMiniMap,
                            onCheckedChange = {
                                showMiniMap = it
                                settingsManager.showMiniMap = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier.testTag("switch_show_minimap")
                        )
                    }

                    if (showMiniMap) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Remove Border toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Map Border (Outline)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Clean borderless look with no outline (keep OFF for no outline look).", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Switch(
                                checked = mapBorderEnabled,
                                onCheckedChange = {
                                    mapBorderEnabled = it
                                    settingsManager.mapBorderEnabled = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF38BDF8)
                                ),
                                modifier = Modifier.testTag("switch_map_border")
                            )
                        }

                        // Transparent background toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Transparent Map Container", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Blend seamlessly over photo without solid block background.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Switch(
                                checked = mapTransparentBg,
                                onCheckedChange = {
                                    mapTransparentBg = it
                                    settingsManager.mapTransparentBg = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF38BDF8)
                                ),
                                modifier = Modifier.testTag("switch_map_transparent_bg")
                            )
                        }

                        // Mini-map Opacity Slider
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Mini-Map Opacity", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Fine-tune transparency level on the photo.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                                Text(
                                    text = "${(miniMapOpacity * 100).toInt()}%",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = miniMapOpacity,
                                onValueChange = {
                                    miniMapOpacity = it
                                    settingsManager.miniMapOpacity = it
                                },
                                valueRange = 0.2f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color(0xFF38BDF8),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("slider_minimap_opacity")
                            )
                        }

                        // Mini-Map Corner Position
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Map Position", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Place mini-map anywhere: 9 positions across corners, edges, or center.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable { mapPosExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("dropdown_map_position"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(miniMapPosition, color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
                                }

                                DropdownMenu(
                                    expanded = mapPosExpanded,
                                    onDismissRequest = { mapPosExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E293B))
                                ) {
                                    positions.forEach { position ->
                                        DropdownMenuItem(
                                            text = { Text(position, color = Color.White) },
                                            onClick = {
                                                settingsManager.miniMapPosition = position
                                                miniMapPosition = position
                                                mapPosExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // GPS & COORDINATES FORMAT
            Text(
                text = "GPS & COORDINATES FORMAT",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Show GPS Coords toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp GPS Coordinates", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Include Latitude and Longitude.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(
                            checked = showGpsCoords,
                            onCheckedChange = {
                                showGpsCoords = it
                                settingsManager.showGpsCoords = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier.testTag("switch_show_coords")
                        )
                    }

                    if (showGpsCoords) {
                        // LIVE COORDINATES PREVIEW BOX
                        val sampleLat = 37.774929
                        val sampleLng = -122.419416
                        val sampleAlt = 42.0
                        val sampleHdg = 184f
                        val previewCoordString = LocationRepository.formatCoordinates(
                            lat = if (manualCoordsOverride.isNotBlank()) manualCoordsOverride.split(",", ";", " ").filter { it.isNotBlank() }.firstOrNull()?.toDoubleOrNull() ?: sampleLat else sampleLat,
                            lng = if (manualCoordsOverride.isNotBlank()) manualCoordsOverride.split(",", ";", " ").filter { it.isNotBlank() }.getOrNull(1)?.toDoubleOrNull() ?: sampleLng else sampleLng,
                            altitude = sampleAlt,
                            bearing = sampleHdg,
                            format = coordFormat,
                            precision = coordPrecision,
                            prefix = coordPrefix,
                            showAltitude = showAltitude,
                            altitudeUnit = altitudeUnit,
                            showHeading = showCompassHeading,
                            useCardinal = useCardinalDirections
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF070C18))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "LIVE COORDINATE STAMP PREVIEW",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("WYSIWYG", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = previewCoordString,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 1. Coordinates Format Picker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Lat/Long Display Style", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    when (coordFormat) {
                                        "DMS" -> "Degrees Minutes Seconds (e.g. 37°46'29\"N 122°25'10\"W)"
                                        "Compact" -> "Raw Numbers (e.g. 37.77493, -122.41942)"
                                        "Grid" -> "Military Grid (e.g. LAT 37.7749 | LON -122.4194)"
                                        "Short" -> "Compact Degrees (e.g. 37.77°N / 122.42°W)"
                                        else -> "Standard Decimal (e.g. 37.77493° N, 122.41942° W)"
                                    },
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
                            }
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable { coordFormatExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("dropdown_coord_format"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(coordFormat, color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
                                }

                                DropdownMenu(
                                    expanded = coordFormatExpanded,
                                    onDismissRequest = { coordFormatExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E293B))
                                ) {
                                    coordFormats.forEach { fmt ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(fmt, color = Color.White, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        when (fmt) {
                                                            "DMS" -> "37°46'29\"N 122°25'10\"W"
                                                            "Compact" -> "37.7749, -122.4194"
                                                            "Grid" -> "LAT 37.7749 | LON -122.4194"
                                                            "Short" -> "37.77°N / 122.42°W"
                                                            else -> "37.7749° N, 122.4194° W"
                                                        },
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            },
                                            onClick = {
                                                settingsManager.coordFormat = fmt
                                                coordFormat = fmt
                                                coordFormatExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Decimal Precision Picker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Decimal Precision", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Number of digits after decimal point (2 to 6).", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable { precisionExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("dropdown_coord_precision"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("$coordPrecision Decimals", color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
                                }

                                DropdownMenu(
                                    expanded = precisionExpanded,
                                    onDismissRequest = { precisionExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E293B))
                                ) {
                                    coordPrecisions.forEach { prec ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    when (prec) {
                                                        2 -> "2 Decimals (~1.1 km accuracy)"
                                                        4 -> "4 Decimals (~11 meters accuracy)"
                                                        5 -> "5 Decimals (~1.1 meter - Default)"
                                                        else -> "6 Decimals (~0.1 meter survey)"
                                                    },
                                                    color = Color.White
                                                )
                                            },
                                            onClick = {
                                                settingsManager.coordPrecision = prec
                                                coordPrecision = prec
                                                precisionExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Coordinate Prefix Picker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Label Prefix", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Header word displayed before coordinates.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable { prefixExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("dropdown_coord_prefix"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(coordPrefix, color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
                                }

                                DropdownMenu(
                                    expanded = prefixExpanded,
                                    onDismissRequest = { prefixExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E293B))
                                ) {
                                    coordPrefixes.forEach { pref ->
                                        DropdownMenuItem(
                                            text = { Text(if (pref == "None") "None (Coordinates only)" else "$pref:", color = Color.White) },
                                            onClick = {
                                                settingsManager.coordPrefix = pref
                                                coordPrefix = pref
                                                prefixExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Cardinal Directions Switch (N/S/E/W vs +/-)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Use Cardinal Indicators (N/S/E/W)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (useCardinalDirections) "Enabled: N, S, E, W hemishperes" else "Disabled: Mathematical signs (+, -)",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = useCardinalDirections,
                                onCheckedChange = {
                                    useCardinalDirections = it
                                    settingsManager.useCardinalDirections = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF38BDF8)
                                ),
                                modifier = Modifier.testTag("switch_use_cardinal")
                            )
                        }

                        // 5. Altitude / Elevation Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Stamp Altitude / Elevation", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Include elevation above sea level in coordinates.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Switch(
                                checked = showAltitude,
                                onCheckedChange = {
                                    showAltitude = it
                                    settingsManager.showAltitude = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF38BDF8)
                                ),
                                modifier = Modifier.testTag("switch_show_altitude")
                            )
                        }

                        if (showAltitude) {
                            // Altitude Units Picker
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Altitude Unit", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Format altitude in meters or feet.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                                Box {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E293B))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .clickable { altitudeUnitExpanded = true }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("dropdown_altitude_unit"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(altitudeUnit, color = Color.White, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
                                    }

                                    DropdownMenu(
                                        expanded = altitudeUnitExpanded,
                                        onDismissRequest = { altitudeUnitExpanded = false },
                                        modifier = Modifier.background(Color(0xFF1E293B))
                                    ) {
                                        altitudeUnits.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text(unit, color = Color.White) },
                                                onClick = {
                                                    settingsManager.altitudeUnit = unit
                                                    altitudeUnit = unit
                                                    altitudeUnitExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 6. Compass Heading Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Stamp Compass Heading", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Include travel or device orientation (e.g. Hdg: 184° S).", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Switch(
                                checked = showCompassHeading,
                                onCheckedChange = {
                                    showCompassHeading = it
                                    settingsManager.showCompassHeading = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF38BDF8)
                                ),
                                modifier = Modifier.testTag("switch_show_heading")
                            )
                        }

                        // 7. Manual Coordinates Override TextField
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text("Manual Coordinates Override (Optional)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Enter custom lat, lng to override device GPS (or leave blank).", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = manualCoordsOverride,
                                onValueChange = {
                                    manualCoordsOverride = it
                                    settingsManager.manualCoordsOverride = it
                                },
                                placeholder = { Text("e.g. 37.7749, -122.4194", color = Color(0xFF64748B)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_manual_coords"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E293B),
                                    unfocusedContainerColor = Color(0xFF1E293B)
                                )
                            )
                        }
                    }

                    // Show Address toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp Street Address", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Reverse-geocoded location place.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(
                            checked = showGpsAddress,
                            onCheckedChange = {
                                showGpsAddress = it
                                settingsManager.showGpsAddress = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier.testTag("switch_show_address")
                        )
                    }
                }
            }

            // STAMP APPEARANCE & BRANDING
            Text(
                text = "STAMP APPEARANCE & STYLING",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // DiviCam Branding Badge toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DiviCam Brand Badge", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Include 'DIVICAM • divicam.app' badge on stamped photos.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(
                            checked = showBrandingBadge,
                            onCheckedChange = {
                                showBrandingBadge = it
                                settingsManager.showBrandingBadge = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier.testTag("switch_show_branding_badge")
                        )
                    }

                    // Stamp Container Opacity
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Stamp Background Transparency", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Make stamp pill background subtle or solid.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Text(
                                text = "${(stampBackgroundOpacity * 100).toInt()}%",
                                color = Color(0xFF38BDF8),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = stampBackgroundOpacity,
                            onValueChange = {
                                stampBackgroundOpacity = it
                                settingsManager.stampBackgroundOpacity = it
                            },
                            valueRange = 0.0f..0.9f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF38BDF8),
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("slider_stamp_bg_opacity")
                        )
                    }

                    // Stamp Border toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp Box Border", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Draw subtle outline around timestamp pill.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(
                            checked = stampBorderEnabled,
                            onCheckedChange = {
                                stampBorderEnabled = it
                                settingsManager.stampBorderEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier.testTag("switch_stamp_border")
                        )
                    }

                    // Text Color Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp Text Color", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Select high-contrast typography color.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { colorExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("dropdown_text_color"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(textColor, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
                            }

                            DropdownMenu(
                                expanded = colorExpanded,
                                onDismissRequest = { colorExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                colors.forEach { colorName ->
                                    DropdownMenuItem(
                                        text = { Text(colorName, color = Color.White) },
                                        onClick = {
                                            settingsManager.textColor = colorName
                                            textColor = colorName
                                            colorExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Stamp Size & Scale Control: Full control over big/small size
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Stamp Size & Scale", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Full control: make stamps small or large to fit anywhere.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Text(
                                text = "${(stampSizeScale * 100).toInt()}%",
                                color = Color(0xFF38BDF8),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quick size presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Tiny" to 0.5f,
                                "Small" to 0.75f,
                                "Normal" to 1.0f,
                                "Large" to 1.25f,
                                "Max" to 1.5f
                            ).forEach { (label, scale) ->
                                val isSelected = kotlin.math.abs(stampSizeScale - scale) < 0.05f
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B))
                                        .border(
                                            1.dp, 
                                            if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.08f), 
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            stampSizeScale = scale
                                            settingsManager.stampSizeScale = scale
                                        }
                                        .padding(vertical = 7.dp)
                                        .testTag("preset_stamp_scale_$label"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Slider(
                            value = stampSizeScale,
                            onValueChange = {
                                stampSizeScale = it
                                settingsManager.stampSizeScale = it
                            },
                            valueRange = 0.4f..1.8f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF38BDF8),
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("slider_stamp_size_scale")
                        )
                    }

                    // Stamp Placement Position: 9 positions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp Position", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Place stamps anywhere: 9 positions across corners, edges, or center.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { posExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("dropdown_timestamp_position"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(timestampPosition, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
                            }

                            DropdownMenu(
                                expanded = posExpanded,
                                onDismissRequest = { posExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                positions.forEach { position ->
                                    DropdownMenuItem(
                                        text = { Text(position, color = Color.White) },
                                        onClick = {
                                            settingsManager.timestampPosition = position
                                            timestampPosition = position
                                            posExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Date Format Picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date Formatting", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Standard notation style.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { dateExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("dropdown_date_format"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dateFormat, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8))
                            }

                            DropdownMenu(
                                expanded = dateExpanded,
                                onDismissRequest = { dateExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                dateFormats.forEach { format ->
                                    DropdownMenuItem(
                                        text = { Text(format, color = Color.White) },
                                        onClick = {
                                            settingsManager.dateFormat = format
                                            dateFormat = format
                                            dateExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 24h Clock toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("24-Hour Military Time", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("e.g., 14:35:00 vs 02:35:00 PM.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(
                            checked = isTimeFormat24h,
                            onCheckedChange = {
                                isTimeFormat24h = it
                                settingsManager.isTimeFormat24h = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier.testTag("switch_time_format_24h")
                        )
                    }

                    // Custom Note or Watermark Title
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("Custom Watermark Label", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Personal notes, batch code, or inspector title.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customText,
                            onValueChange = {
                                customText = it
                                settingsManager.customText = it
                            },
                            placeholder = { Text("e.g., Project Site Alpha, Inspection #4", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("textfield_custom_text")
                        )
                    }
                }
            }

            // FILE QUALITY & DESTINATION
            Text(
                text = "FILE & EXPORT PARAMETERS",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Image Output Format (WebP vs JPEG)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Output Image Format", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("WebP produces small file size with crisp clarity.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val nextVal = if (imageFormat == "WEBP") "JPEG" else "WEBP"
                                    settingsManager.imageFormat = nextVal
                                    imageFormat = nextVal
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("button_image_format")
                        ) {
                            Text(imageFormat, color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Camera Resolution Limit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Max Camera Resolution", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Prevent bloated megapixels and keep output snappy.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val nextVal = when (imageResolution) {
                                        "1080p" -> "1440p"
                                        "1440p" -> "Original"
                                        else -> "1080p"
                                    }
                                    settingsManager.imageResolution = nextVal
                                    imageResolution = nextVal
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("button_image_resolution")
                        ) {
                            Text(imageResolution, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Shutter Speed", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Instant eliminates lag so you don't need to hold the phone.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val nextVal = if (shutterMode == SettingsManager.SHUTTER_MODE_INSTANT) {
                                        SettingsManager.SHUTTER_MODE_SENSOR
                                    } else {
                                        SettingsManager.SHUTTER_MODE_INSTANT
                                    }
                                    settingsManager.shutterMode = nextVal
                                    shutterMode = nextVal
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("button_shutter_mode")
                        ) {
                            Text(shutterMode, color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Save Location", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Direct to device Pictures/DiviCam.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val nextVal = if (saveLocation == "Gallery") "App Folder" else "Gallery"
                                    settingsManager.saveLocation = nextVal
                                    saveLocation = nextVal
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("button_save_location")
                        ) {
                            Text(saveLocation, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Photo Compression Quality", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("High resolution vs file compactness.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Text(
                                text = "${photoQuality.toInt()}%",
                                color = Color(0xFF38BDF8),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = photoQuality,
                            onValueChange = {
                                photoQuality = it
                                settingsManager.photoQuality = it.toInt()
                            },
                            valueRange = 60f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF38BDF8),
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("slider_photo_quality")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
