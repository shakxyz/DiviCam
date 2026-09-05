package com.example.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log

object ImageProcessor {

    fun combineImages(front: Bitmap, back: Bitmap): Bitmap {
        // Target width will be normalized to front image width
        val targetWidth = front.width
        
        // Scale back aspect-ratio-proportionally to match the front width
        val scaleFactorBack = targetWidth.toFloat() / back.width.toFloat()
        val scaledBackHeight = (back.height * scaleFactorBack).toInt()
        val scaledBack = Bitmap.createScaledBitmap(back, targetWidth, scaledBackHeight, true)

        // Clean modern separator line
        val dividerHeight = 6
        val totalHeight = front.height + dividerHeight + scaledBack.height

        val combined = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)

        // 1. Draw Front on top
        canvas.drawBitmap(front, 0f, 0f, null)

        // 2. Draw modern separator line
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            0f, 
            front.height.toFloat(), 
            targetWidth.toFloat(), 
            (front.height + dividerHeight).toFloat(), 
            dividerPaint
        )

        // 3. Draw Back below divider
        canvas.drawBitmap(scaledBack, 0f, (front.height + dividerHeight).toFloat(), null)

        return combined
    }

    fun stampWatermark(
        image: Bitmap,
        customText: String,
        timestamp: String,
        gpsAddress: String,
        gpsCoords: String,
        textColorName: String,
        positionName: String,
        showCoords: Boolean,
        showAddress: Boolean,
        showMiniMap: Boolean = false,
        miniMapOpacity: Float = 0.65f,
        latitude: Double? = null,
        longitude: Double? = null,
        miniMapPositionName: String = "Top-right",
        enableAllStamps: Boolean = true,
        showBrandingBadge: Boolean = true,
        mapBorderEnabled: Boolean = false,
        mapTransparentBg: Boolean = true,
        stampBgOpacity: Float = 0.45f,
        stampBorderEnabled: Boolean = false
    ): Bitmap {
        // If master stamp switch is disabled, return clean unedited photo
        if (!enableAllStamps) {
            return image
        }

        val workingBitmap = image.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)
        val width = workingBitmap.width
        val height = workingBitmap.height

        // Calculate dynamic sizes based on width (~1.6% width)
        val baseSize = (width * 0.016f).coerceAtLeast(24f)
        val largeTextSize = baseSize * 1.25f // For date/time
        val regularTextSize = baseSize * 0.95f // For GPS & custom text
        val badgeTextSize = baseSize * 0.78f // For DiviCam branding badge

        val colorValue = when (textColorName.lowercase()) {
            "cyan" -> Color.parseColor("#38BDF8")
            "yellow" -> Color.parseColor("#FDE047")
            "gold" -> Color.parseColor("#F59E0B")
            "black" -> Color.BLACK
            "red" -> Color.parseColor("#EF4444")
            else -> Color.WHITE
        }

        // Prepare paint objects
        val badgePaint = Paint().apply {
            color = Color.parseColor("#38BDF8") // Vibrant Cyan for brand tag
            textSize = badgeTextSize
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.06f
        }

        val titlePaint = Paint().apply {
            color = colorValue
            textSize = regularTextSize
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val datePaint = Paint().apply {
            color = colorValue
            textSize = largeTextSize
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val gpsPaint = Paint().apply {
            color = colorValue
            textSize = regularTextSize
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        // Gather lines of text to draw
        val lines = mutableListOf<Pair<String, Paint>>()

        if (showBrandingBadge) {
            lines.add(Pair("DIVICAM • divicam.app", badgePaint))
        }
        
        if (customText.isNotEmpty()) {
            lines.add(Pair(customText, titlePaint))
        }
        
        if (timestamp.isNotEmpty()) {
            lines.add(Pair(timestamp, datePaint))
        }

        if (showAddress && gpsAddress.isNotEmpty()) {
            lines.add(Pair(gpsAddress, gpsPaint))
        }
        if (showCoords && gpsCoords.isNotEmpty()) {
            lines.add(Pair(gpsCoords, gpsPaint))
        }

        if (lines.isEmpty() && !showMiniMap) {
            return workingBitmap
        }

        // Calculate geometry
        val paddingX = baseSize * 1.0f
        val paddingY = baseSize * 0.8f
        val lineSpacing = baseSize * 0.38f

        var maxLineWidth = 0f
        var totalTextHeight = 0f

        val lineHeights = FloatArray(lines.size)
        val lineWidths = FloatArray(lines.size)

        for ((index, pair) in lines.withIndex()) {
            val (text, paint) = pair
            val rect = android.graphics.Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            lineWidths[index] = paint.measureText(text)
            if (lineWidths[index] > maxLineWidth) {
                maxLineWidth = lineWidths[index]
            }
            lineHeights[index] = paint.fontMetrics.descent - paint.fontMetrics.ascent
            totalTextHeight += lineHeights[index]
            if (index > 0) {
                totalTextHeight += lineSpacing
            }
        }

        val mapSize = (width * 0.18f).coerceAtLeast(180f) // map is 18% of photo width

        // 1. Draw Text Watermark PILL
        if (lines.isNotEmpty()) {
            val adjustedRectWidth = maxLineWidth + (paddingX * 2)
            val adjustedRectHeight = totalTextHeight + (paddingY * 2)
            val margin = width * 0.03f

            val rectLeft: Float
            val rectTop: Float

            when (positionName) {
                "Top-left" -> {
                    rectLeft = margin
                    rectTop = margin
                }
                "Top-right" -> {
                    rectLeft = width - adjustedRectWidth - margin
                    rectTop = margin
                }
                "Bottom-right" -> {
                    rectLeft = width - adjustedRectWidth - margin
                    rectTop = height - adjustedRectHeight - margin
                }
                else -> { // Default "Bottom-left"
                    rectLeft = margin
                    rectTop = height - adjustedRectHeight - margin
                }
            }

            val alphaInt = (stampBgOpacity * 255).toInt().coerceIn(0, 255)
            if (alphaInt > 0) {
                val bgPaint = Paint().apply {
                    color = Color.argb(alphaInt, 15, 23, 42) // Dark midnight glass
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val bgRect = RectF(rectLeft, rectTop, rectLeft + adjustedRectWidth, rectTop + adjustedRectHeight)
                canvas.drawRoundRect(bgRect, baseSize * 0.5f, baseSize * 0.5f, bgPaint)

                if (stampBorderEnabled) {
                    val borderPaint = Paint().apply {
                        color = Color.argb(40, 255, 255, 255)
                        style = Paint.Style.STROKE
                        strokeWidth = 2f
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(bgRect, baseSize * 0.5f, baseSize * 0.5f, borderPaint)
                }
            }

            // Draw each line inside the container
            var currentY = rectTop + paddingY
            for (i in lines.indices) {
                val (text, paint) = lines[i]
                val textX = rectLeft + paddingX
                val paintBaselineOffset = -paint.fontMetrics.ascent
                canvas.drawText(text, textX, currentY + paintBaselineOffset, paint)
                currentY += lineHeights[i] + lineSpacing
            }
        }

        // 2. Draw Separate MiniMap
        if (showMiniMap && latitude != null && longitude != null) {
            val margin = width * 0.03f
            val mapContainerWidth = mapSize + (paddingX * 2)
            val mapContainerHeight = mapSize + (paddingY * 2)

            val mapContainerLeft: Float
            val mapContainerTop: Float

            when (miniMapPositionName) {
                "Top-left" -> {
                    mapContainerLeft = margin
                    mapContainerTop = margin
                }
                "Top-right" -> {
                    mapContainerLeft = width - mapContainerWidth - margin
                    mapContainerTop = margin
                }
                "Bottom-right" -> {
                    mapContainerLeft = width - mapContainerWidth - margin
                    mapContainerTop = height - mapContainerHeight - margin
                }
                else -> {
                    mapContainerLeft = margin
                    mapContainerTop = height - mapContainerHeight - margin
                }
            }

            val mapAlphaInt = if (mapTransparentBg) {
                (stampBgOpacity * 255).toInt().coerceIn(0, 255)
            } else {
                200
            }

            if (mapAlphaInt > 0) {
                val bgPaint = Paint().apply {
                    color = Color.argb(mapAlphaInt, 15, 23, 42)
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val mapContainerRect = RectF(mapContainerLeft, mapContainerTop, mapContainerLeft + mapContainerWidth, mapContainerTop + mapContainerHeight)
                canvas.drawRoundRect(mapContainerRect, baseSize * 0.5f, baseSize * 0.5f, bgPaint)
            }

            val mapLeft = mapContainerLeft + paddingX
            val mapTop = mapContainerTop + paddingY
            val mapRight = mapLeft + mapSize
            val mapBottom = mapTop + mapSize

            val mapRect = RectF(mapLeft, mapTop, mapRight, mapBottom)

            val mapTile = fetchMapTile(latitude, longitude)
            val mapAlphaPaint = Paint().apply {
                alpha = (miniMapOpacity * 255).toInt().coerceIn(0, 255)
                isAntiAlias = true
            }

            canvas.save()
            val clipPath = android.graphics.Path().apply {
                addRoundRect(mapRect, baseSize * 0.3f, baseSize * 0.3f, android.graphics.Path.Direction.CW)
            }
            canvas.clipPath(clipPath)

            if (mapTile != null) {
                val scaledTile = Bitmap.createScaledBitmap(mapTile, mapSize.toInt(), mapSize.toInt(), true)
                canvas.drawBitmap(scaledTile, mapLeft, mapTop, mapAlphaPaint)
            } else {
                // High-fidelity fallback blue digital/radar GPS grid
                val circlePaint = Paint().apply {
                    color = Color.parseColor("#4D38BDF8")
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    isAntiAlias = true
                }
                val centerValX = mapLeft + mapSize / 2f
                val centerValY = mapTop + mapSize / 2f

                canvas.drawCircle(centerValX, centerValY, mapSize * 0.2f, circlePaint)
                canvas.drawCircle(centerValX, centerValY, mapSize * 0.4f, circlePaint)

                canvas.drawLine(mapLeft, centerValY, mapRight, centerValY, circlePaint)
                canvas.drawLine(centerValX, mapTop, centerValX, mapBottom, circlePaint)

                val textPaint = Paint().apply {
                    color = Color.parseColor("#9938BDF8")
                    textSize = mapSize * 0.08f
                    isAntiAlias = true
                }
                canvas.drawText("N", centerValX - (mapSize * 0.025f), mapTop + (mapSize * 0.12f), textPaint)
                canvas.drawText("E", mapRight - (mapSize * 0.12f), centerValY + (mapSize * 0.025f), textPaint)
            }

            // Target center ring & beacon dot in DiviCam Cyan
            val targetPaint = Paint().apply {
                color = Color.parseColor("#FF38BDF8")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val targetRingPaint = Paint().apply {
                color = Color.parseColor("#8038BDF8")
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }

            val centerValX = mapLeft + mapSize / 2f
            val centerValY = mapTop + mapSize / 2f
            canvas.drawCircle(centerValX, centerValY, baseSize * 0.4f, targetRingPaint)
            canvas.drawCircle(centerValX, centerValY, baseSize * 0.16f, targetPaint)

            canvas.restore()

            // Draw border only if user explicitly enabled it
            if (mapBorderEnabled) {
                val mapBorderPaint = Paint().apply {
                    color = Color.parseColor("#40FFFFFF")
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    isAntiAlias = true
                }
                canvas.drawRoundRect(mapRect, baseSize * 0.3f, baseSize * 0.3f, mapBorderPaint)
            }
        }

        return workingBitmap
    }

    private fun fetchMapTile(lat: Double, lng: Double): Bitmap? {
        try {
            val zoom = 15
            val x = ((lng + 180.0) / 360.0 * (1 shl zoom)).toInt()
            val latRad = lat * Math.PI / 180.0
            val y = ((1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * (1 shl zoom)).toInt()
            
            // Standard OpenStreetMap tiles - Free, public, no API key required
            val urlStr = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
            val url = java.net.URL(urlStr)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.setRequestProperty("User-Agent", "DiviCam/1.0 (Android; contact: mail@shak.xyz)")
            connection.inputStream.use { stream ->
                return android.graphics.BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            Log.d("ImageProcessor", "Map tile download skipped or unavailable: ${e.message}")
        }
        return null
    }

    fun cropToIdCardBox(
        bitmap: Bitmap,
        screenWidth: Float,
        screenHeight: Float
    ): Bitmap {
        if (screenWidth <= 0f || screenHeight <= 0f) {
            return bitmap
        }
        val camW = bitmap.width.toFloat()
        val camH = bitmap.height.toFloat()

        // Card cutout geometry ratio on screen
        val boxWidth = screenWidth * 0.82f
        val boxHeight = boxWidth / 1.585f
        val boxLeft = (screenWidth - boxWidth) / 2f
        val boxTop = (screenHeight - boxHeight) / 2.2f

        // Camera scale (center crop FILL_CENTER)
        val scale = java.lang.Math.max(screenWidth / camW, screenHeight / camH)
        val scaledCamW = camW * scale
        val scaledCamH = camH * scale
        val offsetX = (screenWidth - scaledCamW) / 2f
        val offsetY = (screenHeight - scaledCamH) / 2f

        // Map box bounds from screen coords to camera image coords
        val imgLeft = (boxLeft - offsetX) / scale
        val imgTop = (boxTop - offsetY) / scale
        val imgWidth = boxWidth / scale
        val imgHeight = boxHeight / scale

        val x = imgLeft.toInt().coerceIn(0, bitmap.width - 1)
        val y = imgTop.toInt().coerceIn(0, bitmap.height - 1)
        val w = imgWidth.toInt().coerceIn(1, bitmap.width - x)
        val h = imgHeight.toInt().coerceIn(1, bitmap.height - y)

        return try {
            Bitmap.createBitmap(bitmap, x, y, w, h)
        } catch (e: Exception) {
            Log.e("ImageProcessor", "Failed to crop image", e)
            bitmap
        }
    }
}
