package com.example.ui.settings

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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var timestampPosition by remember { mutableStateOf(settingsManager.timestampPosition) }
    var textColor by remember { mutableStateOf(settingsManager.textColor) }
    var dateFormat by remember { mutableStateOf(settingsManager.dateFormat) }
    var isTimeFormat24h by remember { mutableStateOf(settingsManager.isTimeFormat24h) }
    var showGpsCoords by remember { mutableStateOf(settingsManager.showGpsCoords) }
    var showGpsAddress by remember { mutableStateOf(settingsManager.showGpsAddress) }
    var customText by remember { mutableStateOf(settingsManager.customText) }
    var saveLocation by remember { mutableStateOf(settingsManager.saveLocation) }
    var photoQuality by remember { mutableFloatStateOf(settingsManager.photoQuality.toFloat()) }
    var showMiniMap by remember { mutableStateOf(settingsManager.showMiniMap) }
    var miniMapOpacity by remember { mutableFloatStateOf(settingsManager.miniMapOpacity) }
    var miniMapPosition by remember { mutableStateOf(settingsManager.miniMapPosition) }
    var flashMode by remember { mutableStateOf(settingsManager.flashMode) }

    // Dropdown toggle states
    var posExpanded by remember { mutableStateOf(false) }
    var colorExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }
    var mapPosExpanded by remember { mutableStateOf(false) }

    val positions = settingsManager.getAllPositions()
    val colors = settingsManager.getAllColors()
    val dateFormats = settingsManager.getAllDateFormats()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D0D0D),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = Color.White) },
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
                    // Reset to Defaults action button
                    IconButton(
                        onClick = {
                            settingsManager.timestampPosition = "Bottom-left"
                            settingsManager.textColor = "White"
                            settingsManager.dateFormat = "DD/MM/YYYY"
                            settingsManager.isTimeFormat24h = true
                            settingsManager.showGpsCoords = true
                            settingsManager.showGpsAddress = true
                            settingsManager.customText = ""
                            settingsManager.saveLocation = "Gallery"
                            settingsManager.photoQuality = 90
                            settingsManager.showMiniMap = true
                            settingsManager.miniMapOpacity = 0.7f
                            settingsManager.flashMode = "OFF"

                            timestampPosition = "Bottom-left"
                            textColor = "White"
                            dateFormat = "DD/MM/YYYY"
                            isTimeFormat24h = true
                            showGpsCoords = true
                            showGpsAddress = true
                            customText = ""
                            saveLocation = "Gallery"
                            photoQuality = 90f
                            showMiniMap = true
                            miniMapOpacity = 0.7f
                            flashMode = "OFF"
                        },
                        modifier = Modifier.testTag("reset_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset preferences to defaults",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0D0D0D))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PRIMARY CAMERA CONFIGURATION
            Text(
                text = "PRIMARY SCREEN SETTINGS",
                color = Color(0xFF1A73E8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Camera Mode Picker
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Active Camera Mode", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Select format mode to capture visual logs.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        
                        var cameraModeSelected by remember { mutableStateOf(settingsManager.cameraMode) }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf("ID", "SINGLE")
                            val labels = listOf("ID Card", "Single")
                            
                            modes.forEachIndexed { idx, m ->
                                val isSelected = cameraModeSelected == m
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF1A73E8) else Color(0xFF262626))
                                        .border(1.dp, if (isSelected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            settingsManager.cameraMode = m
                                            cameraModeSelected = m
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = labels[idx],
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))

                    // Custom Note text field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Custom Watermark Stamp Note", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Appends custom note description text (e.g. Case number or project reference code) on photos.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        
                        OutlinedTextField(
                            value = customText,
                            onValueChange = {
                                settingsManager.customText = it
                                customText = it
                            },
                            placeholder = { Text("E.g. CASE NO: 4492-BX", fontSize = 13.sp, color = Color.White.copy(alpha = 0.3f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }

                    // Divider segment
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))

                    // Camera Flash Option
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Camera Flash Mode", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Select flash behavior: Off, On Click (standard flash), or Always On (torch).", color = Color(0xFF9E9E9E), fontSize = 12.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val flashModes = listOf("OFF", "ON_CLICK", "ALWAYS")
                            val flashLabels = listOf("Off", "On Click", "Always On")

                            flashModes.forEachIndexed { idx, fm ->
                                val isSelected = (flashMode == fm) || (fm == "ON_CLICK" && flashMode == "ON")
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF1A73E8) else Color(0xFF262626))
                                        .border(1.dp, if (isSelected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            settingsManager.flashMode = fm
                                            flashMode = fm
                                        }
                                        .padding(vertical = 10.dp)
                                        .testTag("flash_mode_button_${fm.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = flashLabels[idx],
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // General Watermark Custom Headers
            Text(
                text = "WATERMARK APPEARANCE",
                color = Color(0xFF1A73E8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Timestamp Position Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp Placement Corner", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Choose corner for watermark rendering.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF262626))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { posExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("dropdown_stamp_position"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(timestampPosition, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown indicator", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = posExpanded,
                                onDismissRequest = { posExpanded = false },
                                modifier = Modifier.background(Color(0xFF2E2E2E))
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

                    // Divider segment
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))

                    // Timestamp Colors Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Watermark Tint Color", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Change text tint color on stamps.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF262626))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { colorExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("dropdown_text_color"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dotColor = when (textColor.lowercase()) {
                                    "yellow" -> Color(0xFFFFD600)
                                    "black" -> Color.Black
                                    "red" -> Color(0xFFFF1744)
                                    else -> Color.White
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(dotColor, CircleShape)
                                        .border(1.dp, Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(textColor, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown indicator", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = colorExpanded,
                                onDismissRequest = { colorExpanded = false },
                                modifier = Modifier.background(Color(0xFF2E2E2E))
                            ) {
                                colors.forEach { color ->
                                    DropdownMenuItem(
                                        text = { Text(color, color = Color.White) },
                                        onClick = {
                                            settingsManager.textColor = color
                                            textColor = color
                                            colorExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Divider segment
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))

                    // Date Format Options Dropdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date Format", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Select calendar representation.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF262626))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { dateExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("dropdown_date_format"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dateFormat, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown indicator", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = dateExpanded,
                                onDismissRequest = { dateExpanded = false },
                                modifier = Modifier.background(Color(0xFF2E2E2E))
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

                    // Divider segment
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))

                    // 12h/24h Time Format Slider Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use 24-Hour clock format", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("E.g. 14:30 instead of 02:30 PM.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                        Switch(
                            checked = isTimeFormat24h,
                            onCheckedChange = {
                                settingsManager.isTimeFormat24h = it
                                isTimeFormat24h = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1A73E8)
                            ),
                            modifier = Modifier.testTag("switch_time_format_24h")
                        )
                    }
                }
            }

            // GPS LOCATION SETTINGS SECTION
            Text(
                text = "GPS LOCATION CONTROL",
                color = Color(0xFF1A73E8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Show Coords toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show latitude/longitude", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Stamp raw decimal coordinates on photos.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                        Switch(
                            checked = showGpsCoords,
                            onCheckedChange = {
                                settingsManager.showGpsCoords = it
                                showGpsCoords = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1A73E8)
                            ),
                            modifier = Modifier.testTag("switch_show_gps_coords")
                        )
                    }

                    // Divider segment
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))

                    // Show physical address text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show physical address description", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Geocode locations into street names.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                        Switch(
                            checked = showGpsAddress,
                            onCheckedChange = {
                                settingsManager.showGpsAddress = it
                                showGpsAddress = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1A73E8)
                            ),
                            modifier = Modifier.testTag("switch_show_gps_address")
                        )
                    }
                }
            }

            // STAMPED MINIMAP SECTION
            Text(
                text = "STAMPED MINIMAP SECTION",
                color = Color(0xFF1A73E8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Show map toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp mini-map on photo", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Render a colorful GPS map onto the final captured photo.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                        Switch(
                            checked = showMiniMap,
                            onCheckedChange = {
                                settingsManager.showMiniMap = it
                                showMiniMap = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1A73E8)
                            ),
                            modifier = Modifier.testTag("switch_show_minimap")
                        )
                    }

                    if (showMiniMap) {
                        // Divider segment
                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))

                        // Opacity slider option
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Mini-map Opacity", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Control mini-map opacity on the stamped photo.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                                }
                                Text(
                                    text = "${(miniMapOpacity * 100).toInt()}%",
                                    color = Color(0xFF1A73E8),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = miniMapOpacity,
                                onValueChange = {
                                    miniMapOpacity = it
                                    settingsManager.miniMapOpacity = it
                                },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color(0xFF1A73E8),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("slider_minimap_opacity")
                             )

                            // Divider segment
                            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))

                            // Map Placement Corner Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Map Stamp Corner", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Choose corner to place the stamped mini-map.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                                }
                                Box {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF262626))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .clickable { mapPosExpanded = true }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("dropdown_map_position"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(miniMapPosition, color = Color.White, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown indicator", tint = Color.White)
                                    }
                                    DropdownMenu(
                                        expanded = mapPosExpanded,
                                        onDismissRequest = { mapPosExpanded = false },
                                        modifier = Modifier.background(Color(0xFF2E2E2E))
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
            }

            // FILE PARAMETERS SECTION
            Text(
                text = "FILE PARAMETERS",
                color = Color(0xFF1A73E8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Save Destination Selection (Gallery / App Folder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Save Target Folder", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Save output direct to gallery photos.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                        }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF262626))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val nextVal = if (saveLocation == "Gallery") "App Folder" else "Gallery"
                                        settingsManager.saveLocation = nextVal
                                        saveLocation = nextVal
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("button_save_location"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(saveLocation, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }

                    // Divider segment
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))

                    // Compression Quality Slider Option
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Photo Compression Quality", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Balance file sizes vs detail resolutions.", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                            }
                            Text(
                                text = "${photoQuality.toInt()}%",
                                color = Color(0xFF1A73E8),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = photoQuality,
                            onValueChange = {
                                photoQuality = it
                                settingsManager.photoQuality = it.toInt()
                            },
                            valueRange = 60f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF1A73E8),
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("slider_photo_quality")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
