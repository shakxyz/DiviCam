package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("id_stamp_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_TIMESTAMP_POSITION = "timestamp_position"
        const val KEY_TEXT_COLOR = "text_color"
        const val KEY_DATE_FORMAT = "date_format"
        const val KEY_TIME_FORMAT_24H = "time_format_24h"
        const val KEY_SHOW_GPS_COORDS = "show_gps_coords"
        const val KEY_SHOW_GPS_ADDRESS = "show_gps_address"
        const val KEY_CUSTOM_TEXT = "custom_text"
        const val KEY_SAVE_LOCATION = "save_location" // "Gallery" or "AppFolder"
        const val KEY_PHOTO_QUALITY = "photo_quality"
        const val KEY_CAMERA_MODE = "camera_mode"       // "ID", "SINGLE", "VIDEO"
        const val KEY_SHOW_MINI_MAP = "show_mini_map"
        const val KEY_MINI_MAP_OPACITY = "mini_map_opacity"
        const val KEY_MINI_MAP_POSITION = "mini_map_position"
        const val KEY_FLASH_MODE = "flash_mode"
    }

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
        get() = prefs.getFloat(KEY_MINI_MAP_OPACITY, 0.7f)
        set(value) = prefs.edit().putFloat(KEY_MINI_MAP_OPACITY, value).apply()

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
        get() = prefs.getInt(KEY_PHOTO_QUALITY, 90)
        set(value) = prefs.edit().putInt(KEY_PHOTO_QUALITY, value).apply()

    var cameraMode: String
        get() = prefs.getString(KEY_CAMERA_MODE, "ID") ?: "ID"
        set(value) = prefs.edit().putString(KEY_CAMERA_MODE, value).apply()

    fun getAllColors(): List<String> = listOf("White", "Yellow", "Black", "Red")
    fun getAllPositions(): List<String> = listOf("Top-left", "Top-right", "Bottom-left", "Bottom-right")
    fun getAllDateFormats(): List<String> = listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD")
}
