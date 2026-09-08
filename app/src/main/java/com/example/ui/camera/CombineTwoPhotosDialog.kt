package com.example.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CombineTwoPhotosDialog(
    onDismiss: () -> Unit,
    onCombinePhotos: (Bitmap, Bitmap, Boolean) -> Unit,
    onStampSinglePhoto: (Bitmap, Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: 2-in-1 Dual, 1: 1 Single Photo
    var photo1Uri by remember { mutableStateOf<Uri?>(null) }
    var photo2Uri by remember { mutableStateOf<Uri?>(null) }
    var singlePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var applyWatermark by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    val pickMultipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 2)
    ) { uris ->
        if (uris.isNotEmpty()) {
            photo1Uri = uris.getOrNull(0)
            if (uris.size > 1) {
                photo2Uri = uris.getOrNull(1)
            }
        }
    }

    val pickPhoto1Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) photo1Uri = uri
    }

    val pickPhoto2Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) photo2Uri = uri
    }

    val pickSingleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) singlePhotoUri = uri
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF38BDF8)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (selectedTab == 0) "Combine 2 Photos" else "Stamp Single Photo",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedTab == 0) "2-in-1 Dual Document" else "Gallery Photo Importer",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_close_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Tabs (2-in-1 vs Single Photo)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) Color(0xFF0284C7) else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp)
                            .testTag("tab_combine_2_photos"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "2-in-1 Dual Photos",
                            color = if (selectedTab == 0) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) Color(0xFF0284C7) else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp)
                            .testTag("tab_single_photo_stamp"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1 Single Photo",
                            color = if (selectedTab == 1) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // FAST PICK 2 PHOTOS BUTTON
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .clickable {
                                pickMultipleLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 10.dp, horizontal = 14.dp)
                            .testTag("pick_2_photos_together_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select 2 Photos from Gallery at Once",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Photo 1 Slot (Front)
                    Text(
                        text = "PHOTO 1 (TOP / FRONT)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (photo1Uri != null) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                pickPhoto1Launcher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("slot_photo_1"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photo1Uri != null) {
                            AsyncImage(
                                model = photo1Uri,
                                contentDescription = "Photo 1 Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { photo1Uri = null }
                                    .padding(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Photo, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap to select Top Photo", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }

                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = photo1Uri
                            photo1Uri = photo2Uri
                            photo2Uri = temp
                        },
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), CircleShape)
                            .size(34.dp)
                            .testTag("swap_photos_button")
                    ) {
                        Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Swap Photos", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    }

                    // Photo 2 Slot (Back)
                    Text(
                        text = "PHOTO 2 (BOTTOM / BACK)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (photo2Uri != null) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                pickPhoto2Launcher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("slot_photo_2"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photo2Uri != null) {
                            AsyncImage(
                                model = photo2Uri,
                                contentDescription = "Photo 2 Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { photo2Uri = null }
                                    .padding(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Photo, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap to select Bottom Photo", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // SINGLE PHOTO SLOT
                    Text(
                        text = "SELECT PHOTO TO STAMP OR SAVE",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (singlePhotoUri != null) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                pickSingleLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("slot_single_photo"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (singlePhotoUri != null) {
                            AsyncImage(
                                model = singlePhotoUri,
                                contentDescription = "Selected Photo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { singlePhotoUri = null }
                                    .padding(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap to choose photo from Gallery", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Can save stamped or clean without stamps", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Apply watermark toggle (stamped vs clean without stamp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (applyWatermark) "Apply Stamps & GPS (Stamped)" else "Save Clean (Without Stamps)",
                            color = if (applyWatermark) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (applyWatermark) "Adds GPS, date/time, accurate map" else "Saves original pristine photo",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = applyWatermark,
                        onCheckedChange = { applyWatermark = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier.testTag("switch_apply_watermark_combine")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button
                if (selectedTab == 0) {
                    val canCombine = (photo1Uri != null && photo2Uri != null && !isProcessing)

                    Button(
                        onClick = {
                            val u1 = photo1Uri ?: return@Button
                            val u2 = photo2Uri ?: return@Button
                            isProcessing = true

                            coroutineScope.launch {
                                val b1 = withContext(Dispatchers.IO) { loadBitmapFromUri(context, u1) }
                                val b2 = withContext(Dispatchers.IO) { loadBitmapFromUri(context, u2) }

                                if (b1 != null && b2 != null) {
                                    onCombinePhotos(b1, b2, applyWatermark)
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Could not load selected images", Toast.LENGTH_SHORT).show()
                                    isProcessing = false
                                }
                            }
                        },
                        enabled = canCombine,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("button_execute_combine")
                    ) {
                        Text(
                            text = if (isProcessing) "Combining Photos..." else "Combine & Save as One Photo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canCombine) Color.White else Color(0xFF94A3B8)
                        )
                    }
                } else {
                    val canSaveSingle = (singlePhotoUri != null && !isProcessing)

                    Button(
                        onClick = {
                            val u = singlePhotoUri ?: return@Button
                            isProcessing = true

                            coroutineScope.launch {
                                val b = withContext(Dispatchers.IO) { loadBitmapFromUri(context, u) }
                                if (b != null) {
                                    onStampSinglePhoto(b, applyWatermark)
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Could not load image", Toast.LENGTH_SHORT).show()
                                    isProcessing = false
                                }
                            }
                        },
                        enabled = canSaveSingle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("button_execute_single_save")
                    ) {
                        Text(
                            text = if (isProcessing) "Processing Photo..." else if (applyWatermark) "Stamp & Save Photo" else "Save Clean (No Stamps)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canSaveSingle) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        Log.e("CombineDialog", "Error loading bitmap from uri", e)
        null
    }
}
