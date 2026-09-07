package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("divicam_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_ENABLE_ALL_STAMPS = "enable_all_stamps"
        const val KEY_TIMESTAMP_POSITION = "timestamp_position"
        const val KEY_TEXT_COLOR = "text_color"
        const val KEY_DATE_FORMAT = "date_format"
        const val KEY_TIME_FORMAT_24H = "time_format_24h"
        const val KEY_SHOW_GPS_COORDS = "show_gps_coords"
        const val KEY_SHOW_GPS_ADDRESS = "show_gps_address"
        const val KEY_COORD_FORMAT = "coord_format"
        const val KEY_COORD_PRECISION = "coord_precision"
        const val KEY_COORD_PREFIX = "coord_prefix"
        const val KEY_SHOW_ALTITUDE = "show_altitude"
        const val KEY_ALTITUDE_UNIT = "altitude_unit"
        const val KEY_SHOW_COMPASS_HEADING = "show_compass_heading"
        const val KEY_USE_CARDINAL_DIRECTIONS = "use_cardinal_directions"
        const val KEY_MANUAL_COORDS_OVERRIDE = "manual_coords_override"
        const val KEY_CUSTOM_TEXT = "custom_text"
        const val KEY_SAVE_LOCATION = "save_location" // "Gallery" or "AppFolder"
        const val KEY_PHOTO_QUALITY = "photo_quality"
        const val KEY_CAMERA_MODE = "camera_mode"       // "ID", "SINGLE", "VIDEO"
        const val KEY_SHOW_MINI_MAP = "show_mini_map"
        const val KEY_MINI_MAP_OPACITY = "mini_map_opacity"
        const val KEY_MINI_MAP_POSITION = "mini_map_position"
        const val KEY_MAP_BORDER_ENABLED = "map_border_enabled"
        const val KEY_MAP_TRANSPARENT_BG = "map_transparent_bg"
        const val KEY_STAMP_BG_OPACITY = "stamp_bg_opacity"
        const val KEY_STAMP_BORDER_ENABLED = "stamp_border_enabled"
        const val KEY_SHOW_BRANDING_BADGE = "show_branding_badge"
        const val KEY_FLASH_MODE = "flash_mode"
        const val KEY_IMAGE_FORMAT = "image_format"
        const val KEY_IMAGE_RESOLUTION = "image_resolution"
    }

    var imageFormat: String
        get() = prefs.getString(KEY_IMAGE_FORMAT, "WebP") ?: "WebP"
        set(value) = prefs.edit().putString(KEY_IMAGE_FORMAT, value).apply()

    var imageResolution: String
        get() = prefs.getString(KEY_IMAGE_RESOLUTION, "Standard") ?: "Standard"
        set(value) = prefs.edit().putString(KEY_IMAGE_RESOLUTION, value).apply()

    var enableAllStamps: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_ALL_STAMPS, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_ALL_STAMPS, value).apply()

    var flashMode: String
        get() = prefs.getString(KEY_FLASH_MODE, "OFF") ?: "OFF"
        set(value) = prefs.edit().putString(KEY_FLASH_MODE, value).apply()

    var miniMapPosition: String
        get() = prefs.getString(KEY_MINI_MAP_POSITION, "Top-right") ?: "Top-right"
        set(value) = prefs.edit().putString(KEY_MINI_MAP_POSITION, value).apply()

    var showMiniMap: Boolean
        get() = prefs.getBoolean(KEY_SHOW_MINI_MAP, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_MINI_MAP, value).apply()

    var miniMapOpacity: Float
        get() = prefs.getFloat(KEY_MINI_MAP_OPACITY, 0.65f)
        set(value) = prefs.edit().putFloat(KEY_MINI_MAP_OPACITY, value).apply()

    var mapBorderEnabled: Boolean
        get() = prefs.getBoolean(KEY_MAP_BORDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_MAP_BORDER_ENABLED, value).apply()

    var mapTransparentBg: Boolean
        get() = prefs.getBoolean(KEY_MAP_TRANSPARENT_BG, true)
        set(value) = prefs.edit().putBoolean(KEY_MAP_TRANSPARENT_BG, value).apply()

    var stampBackgroundOpacity: Float
        get() = prefs.getFloat(KEY_STAMP_BG_OPACITY, 0.45f)
        set(value) = prefs.edit().putFloat(KEY_STAMP_BG_OPACITY, value).apply()

    var stampBorderEnabled: Boolean
        get() = prefs.getBoolean(KEY_STAMP_BORDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_STAMP_BORDER_ENABLED, value).apply()

    var showBrandingBadge: Boolean
        get() = prefs.getBoolean(KEY_SHOW_BRANDING_BADGE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_BRANDING_BADGE, value).apply()

    var coordFormat: String
        get() = prefs.getString(KEY_COORD_FORMAT, "Decimal") ?: "Decimal"
        set(value) = prefs.edit().putString(KEY_COORD_FORMAT, value).apply()

    var coordPrecision: Int
        get() = prefs.getInt(KEY_COORD_PRECISION, 5)
        set(value) = prefs.edit().putInt(KEY_COORD_PRECISION, value).apply()

    var coordPrefix: String
        get() = prefs.getString(KEY_COORD_PREFIX, "GPS") ?: "GPS"
        set(value) = prefs.edit().putString(KEY_COORD_PREFIX, value).apply()

    var showAltitude: Boolean
        get() = prefs.getBoolean(KEY_SHOW_ALTITUDE, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_ALTITUDE, value).apply()

    var altitudeUnit: String
        get() = prefs.getString(KEY_ALTITUDE_UNIT, "Meters (m)") ?: "Meters (m)"
        set(value) = prefs.edit().putString(KEY_ALTITUDE_UNIT, value).apply()

    var showCompassHeading: Boolean
        get() = prefs.getBoolean(KEY_SHOW_COMPASS_HEADING, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_COMPASS_HEADING, value).apply()

    var useCardinalDirections: Boolean
        get() = prefs.getBoolean(KEY_USE_CARDINAL_DIRECTIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_CARDINAL_DIRECTIONS, value).apply()

    var manualCoordsOverride: String
        get() = prefs.getString(KEY_MANUAL_COORDS_OVERRIDE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MANUAL_COORDS_OVERRIDE, value).apply()

    var timestampPosition: String
        get() = prefs.getString(KEY_TIMESTAMP_POSITION, "Bottom-left") ?: "Bottom-left"
        set(value) = prefs.edit().putString(KEY_TIMESTAMP_POSITION, value).apply()

    var textColor: String
        get() = prefs.getString(KEY_TEXT_COLOR, "White") ?: "White"
        set(value) = prefs.edit().putString(KEY_TEXT_COLOR, value).apply()

    var dateFormat: String
        get() = prefs.getString(KEY_DATE_FORMAT, "DD/MM/YYYY") ?: "DD/MM/YYYY"
        set(value) = prefs.edit().putString(KEY_DATE_FORMAT, value).apply()

    var isTimeFormat24h: Boolean
        get() = prefs.getBoolean(KEY_TIME_FORMAT_24H, true)
        set(value) = prefs.edit().putBoolean(KEY_TIME_FORMAT_24H, value).apply()

    var showGpsCoords: Boolean
        get() = prefs.getBoolean(KEY_SHOW_GPS_COORDS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_GPS_COORDS, value).apply()

    var showGpsAddress: Boolean
        get() = prefs.getBoolean(KEY_SHOW_GPS_ADDRESS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_GPS_ADDRESS, value).apply()

    var customText: String
        get() = prefs.getString(KEY_CUSTOM_TEXT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_TEXT, value).apply()

    var saveLocation: String
        get() = prefs.getString(KEY_SAVE_LOCATION, "Gallery") ?: "Gallery"
        set(value) = prefs.edit().putString(KEY_SAVE_LOCATION, value).apply()

    var photoQuality: Int
        get() = prefs.getInt(KEY_PHOTO_QUALITY, 92)
        set(value) = prefs.edit().putInt(KEY_PHOTO_QUALITY, value).apply()

    var cameraMode: String
        get() = prefs.getString(KEY_CAMERA_MODE, "ID") ?: "ID"
        set(value) = prefs.edit().putString(KEY_CAMERA_MODE, value).apply()

    fun getAllColors(): List<String> = listOf("White", "Cyan", "Yellow", "Gold", "Black")
    fun getAllPositions(): List<String> = listOf("Bottom-left", "Bottom-right", "Top-left", "Top-right")
    fun getAllDateFormats(): List<String> = listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD")
    fun getAllCoordFormats(): List<String> = listOf("Decimal", "DMS", "Compact", "Grid", "Short")
    fun getAllCoordPrefixes(): List<String> = listOf("GPS", "LAT/LON", "COORD", "None")
    fun getAllAltitudeUnits(): List<String> = listOf("Meters (m)", "Feet (ft)")
    fun getAllPrecisions(): List<Int> = listOf(2, 4, 5, 6)
    fun getAllImageFormats(): List<String> = listOf("WebP", "JPEG")
    fun getAllResolutions(): List<String> = listOf("Standard", "High", "Full Sensor")
}
