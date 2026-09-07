package com.example.ui.camera

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GalleryRepository
import com.example.data.ImageProcessor
import com.example.data.LocationData
import com.example.data.LocationRepository
import com.example.data.SettingsManager
import com.example.utils.DateTimeUtils
import com.example.utils.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    val settings = SettingsManager(application)
    private val galleryRepository = GalleryRepository(application)
    private val locationRepository = LocationRepository(application)

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _cameraMode = MutableStateFlow(settings.cameraMode)
    val cameraMode: StateFlow<String> = _cameraMode.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _frontImage = MutableStateFlow<Bitmap?>(null)
    val frontImage: StateFlow<Bitmap?> = _frontImage.asStateFlow()

    private val _backImage = MutableStateFlow<Bitmap?>(null)
    val backImage: StateFlow<Bitmap?> = _backImage.asStateFlow()

    private val _isRecordingVideo = MutableStateFlow(false)
    val isRecordingVideo: StateFlow<Boolean> = _isRecordingVideo.asStateFlow()

    private val _locationData = MutableStateFlow<LocationData?>(locationRepository.getCachedLocation())
    val locationData: StateFlow<LocationData?> = _locationData.asStateFlow()

    private val _isRefreshingLocation = MutableStateFlow(false)
    val isRefreshingLocation: StateFlow<Boolean> = _isRefreshingLocation.asStateFlow()

    private val _customText = MutableStateFlow(settings.customText)
    val customText: StateFlow<String> = _customText.asStateFlow()

    private val _enableAllStamps = MutableStateFlow(settings.enableAllStamps)
    val enableAllStamps: StateFlow<Boolean> = _enableAllStamps.asStateFlow()

    private val _cameraPermissionGranted = MutableStateFlow(false)
    val cameraPermissionGranted: StateFlow<Boolean> = _cameraPermissionGranted.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    private val _navigationToPreview = MutableStateFlow<Uri?>(null)
    val navigationToPreview: StateFlow<Uri?> = _navigationToPreview.asStateFlow()

    init {
        checkPermissions()
        refreshLocation()
    }

    fun checkPermissions() {
        _cameraPermissionGranted.value = PermissionUtils.hasCameraPermission(getApplication())
        _locationPermissionGranted.value = PermissionUtils.hasLocationPermissions(getApplication())
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _cameraPermissionGranted.value = granted
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        _locationPermissionGranted.value = granted
        if (granted) {
            refreshLocation()
        }
    }

    fun refreshLocation() {
        if (PermissionUtils.hasLocationPermissions(getApplication())) {
            viewModelScope.launch {
                _isRefreshingLocation.value = true
                try {
                    val loc = locationRepository.fetchCurrentLocation(settings)
                    _locationData.value = loc
                    loc?.let {
                        ImageProcessor.prefetchMapTile(it.latitude, it.longitude)
                    }
                } catch (e: Exception) {
                    Log.e("CameraViewModel", "Error refreshing GPS location", e)
                } finally {
                    _isRefreshingLocation.value = false
                }
            }
        }
    }

    fun toggleAllStamps(): Boolean {
        val next = !settings.enableAllStamps
        settings.enableAllStamps = next
        _enableAllStamps.value = next
        return next
    }

    fun setCameraMode(mode: String) {
        settings.cameraMode = mode
        _cameraMode.value = mode
        resetIdFlow()
    }

    fun setCustomText(text: String) {
        settings.customText = text
        _customText.value = text
    }

    fun resetIdFlow() {
        _frontImage.value = null
        _backImage.value = null
        _currentStep.value = 1
        _navigationToPreview.value = null
    }

    fun retakeCurrentStep() {
        if (_currentStep.value == 2) {
            _backImage.value = null
        } else {
            _frontImage.value = null
        }
    }

    fun handlePhotoCaptured(bitmap: Bitmap, screenWidth: Float = 0f, screenHeight: Float = 0f) {
        viewModelScope.launch {
            _isCapturing.value = true
            try {
                withContext(Dispatchers.Default) {
                    val maxDim = when (settings.imageResolution) {
                        "Full Sensor" -> 4096
                        "High" -> 2560
                        else -> 1920 // High-definition 1080p standard default
                    }

                    when (cameraMode.value) {
                        "ID" -> {
                            val cropped = if (screenWidth > 0f && screenHeight > 0f) {
                                ImageProcessor.cropToIdCardBox(bitmap, screenWidth, screenHeight)
                            } else {
                                bitmap
                            }
                            val processed = ImageProcessor.normalizeResolution(cropped, maxDim)
                            if (_currentStep.value == 1) {
                                _frontImage.value = processed
                                _currentStep.value = 2
                                refreshLocation()
                            } else {
                                _backImage.value = processed
                                combineAndStampIdCard()
                            }
                        }
                        "SINGLE" -> {
                            val viewfinderCropped = ImageProcessor.cropToVisibleViewfinder(bitmap, screenWidth, screenHeight, isFitCenter = true)
                            val normalized = ImageProcessor.normalizeResolution(viewfinderCropped, maxDim)
                            stampAndSaveSinglePhoto(normalized)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error processing capture", e)
            } finally {
                _isCapturing.value = false
            }
        }
    }

    fun combineAndSaveTwoBitmaps(photo1: Bitmap, photo2: Bitmap, applyStamps: Boolean = true) {
        viewModelScope.launch {
            _isCapturing.value = true
            try {
                withContext(Dispatchers.Default) {
                    val maxDim = when (settings.imageResolution) {
                        "Full Sensor" -> 4096
                        "High" -> 2560
                        else -> 1920
                    }
                    val norm1 = ImageProcessor.normalizeResolution(photo1, maxDim)
                    val norm2 = ImageProcessor.normalizeResolution(photo2, maxDim)

                    // Combine vertically (Front on top, Back on bottom)
                    val combined = ImageProcessor.combineImages(norm1, norm2)

                    val finalBitmap = if (applyStamps && settings.enableAllStamps) {
                        val timestampText = DateTimeUtils.formatTimestamp(
                            Date(),
                            settings.dateFormat,
                            settings.isTimeFormat24h
                        )
                        val loc = _locationData.value
                        val gpsAddress = if (loc != null) loc.address else "Location unavailable"
                        val gpsCoords = if (loc != null) loc.formattedCoordinates else "Coordinates unavailable"

                        ImageProcessor.stampWatermark(
                            image = combined,
                            customText = _customText.value,
                            timestamp = timestampText,
                            gpsAddress = gpsAddress,
                            gpsCoords = gpsCoords,
                            textColorName = settings.textColor,
                            positionName = settings.timestampPosition,
                            showCoords = settings.showGpsCoords,
                            showAddress = settings.showGpsAddress,
                            showMiniMap = settings.showMiniMap,
                            miniMapOpacity = settings.miniMapOpacity,
                            latitude = loc?.latitude,
                            longitude = loc?.longitude,
                            miniMapPositionName = settings.miniMapPosition,
                            enableAllStamps = settings.enableAllStamps,
                            showBrandingBadge = settings.showBrandingBadge,
                            mapBorderEnabled = settings.mapBorderEnabled,
                            mapTransparentBg = settings.mapTransparentBg,
                            stampBgOpacity = settings.stampBackgroundOpacity,
                            stampBorderEnabled = settings.stampBorderEnabled
                        )
                    } else {
                        combined
                    }

                    val prefix = if (cameraMode.value == "ID") "IDCAM" else "DiviCam"
                    val uri = galleryRepository.saveBitmapToGallery(
                        bitmap = finalBitmap,
                        quality = settings.photoQuality,
                        customName = "${prefix}_${System.currentTimeMillis()}",
                        format = settings.imageFormat
                    )
                    _navigationToPreview.value = uri
                }
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error combining two photos", e)
            } finally {
                _isCapturing.value = false
            }
        }
    }

    private suspend fun combineAndStampIdCard() {
        val front = _frontImage.value ?: return
        val back = _backImage.value ?: return

        val combined = ImageProcessor.combineImages(front, back)

        val timestampText = DateTimeUtils.formatTimestamp(
            Date(),
            settings.dateFormat,
            settings.isTimeFormat24h
        )

        val loc = _locationData.value
        val gpsAddress = if (loc != null) loc.address else "Location unavailable"
        val gpsCoords = if (loc != null) loc.formattedCoordinates else "Coordinates unavailable"

        val stamped = ImageProcessor.stampWatermark(
            image = combined,
            customText = _customText.value,
            timestamp = timestampText,
            gpsAddress = gpsAddress,
            gpsCoords = gpsCoords,
            textColorName = settings.textColor,
            positionName = settings.timestampPosition,
            showCoords = settings.showGpsCoords,
            showAddress = settings.showGpsAddress,
            showMiniMap = settings.showMiniMap,
            miniMapOpacity = settings.miniMapOpacity,
            latitude = loc?.latitude,
            longitude = loc?.longitude,
            miniMapPositionName = settings.miniMapPosition,
            enableAllStamps = settings.enableAllStamps,
            showBrandingBadge = settings.showBrandingBadge,
            mapBorderEnabled = settings.mapBorderEnabled,
            mapTransparentBg = settings.mapTransparentBg,
            stampBgOpacity = settings.stampBackgroundOpacity,
            stampBorderEnabled = settings.stampBorderEnabled
        )

        // Exact naming requested: IDCAM in ID card mode
        val uri = galleryRepository.saveBitmapToGallery(
            bitmap = stamped,
            quality = settings.photoQuality,
            customName = "IDCAM_${System.currentTimeMillis()}",
            format = settings.imageFormat
        )
        _navigationToPreview.value = uri
    }

    private suspend fun stampAndSaveSinglePhoto(bitmap: Bitmap) {
        val timestampText = DateTimeUtils.formatTimestamp(
            Date(),
            settings.dateFormat,
            settings.isTimeFormat24h
        )

        val loc = _locationData.value
        val gpsAddress = if (loc != null) loc.address else "Location unavailable"
        val gpsCoords = if (loc != null) loc.formattedCoordinates else "Coordinates unavailable"

        val stamped = ImageProcessor.stampWatermark(
            image = bitmap,
            customText = _customText.value,
            timestamp = timestampText,
            gpsAddress = gpsAddress,
            gpsCoords = gpsCoords,
            textColorName = settings.textColor,
            positionName = settings.timestampPosition,
            showCoords = settings.showGpsCoords,
            showAddress = settings.showGpsAddress,
            showMiniMap = settings.showMiniMap,
            miniMapOpacity = settings.miniMapOpacity,
            latitude = loc?.latitude,
            longitude = loc?.longitude,
            miniMapPositionName = settings.miniMapPosition,
            enableAllStamps = settings.enableAllStamps,
            showBrandingBadge = settings.showBrandingBadge,
            mapBorderEnabled = settings.mapBorderEnabled,
            mapTransparentBg = settings.mapTransparentBg,
            stampBgOpacity = settings.stampBackgroundOpacity,
            stampBorderEnabled = settings.stampBorderEnabled
        )

        // Exact naming requested: DiviCam in single mode
        val uri = galleryRepository.saveBitmapToGallery(
            bitmap = stamped,
            quality = settings.photoQuality,
            customName = "DiviCam_${System.currentTimeMillis()}",
            format = settings.imageFormat
        )
        _navigationToPreview.value = uri
    }

    fun handleVideoFileRecorded(file: File) {
        viewModelScope.launch {
            _isCapturing.value = true
            try {
                val uri = galleryRepository.saveVideoToGallery(file)
                _navigationToPreview.value = uri
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error saving video file", e)
            } finally {
                _isCapturing.value = false
            }
        }
    }

    fun setRecordingVideoState(recording: Boolean) {
        _isRecordingVideo.value = recording
    }

    fun clearNavigation() {
        _navigationToPreview.value = null
    }
}
