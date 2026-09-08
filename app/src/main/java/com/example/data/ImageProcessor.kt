package com.example.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log

object ImageProcessor {

    private val mapTileCache = android.util.LruCache<String, Bitmap>(20)

    fun prefetchMapTile(lat: Double, lng: Double) {
        val zoom = 15
        val x = ((lng + 180.0) / 360.0 * (1 shl zoom)).toInt()
        val latRad = lat * Math.PI / 180.0
        val y = ((1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * (1 shl zoom)).toInt()
        val key = "$zoom/$x/$y"
        if (mapTileCache.get(key) != null) return

        Thread {
            try {
                val urlStr = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
                val url = java.net.URL(urlStr)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 2000
                connection.readTimeout = 2000
                connection.setRequestProperty("User-Agent", "DiviCam/1.0 (Android; contact: app@divicam.xyz)")
                connection.inputStream.use { stream ->
                    val bmp = android.graphics.BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        mapTileCache.put(key, bmp)
                    }
                }
            } catch (e: Exception) {
                // Ignore prefetch network failure
            }
        }.start()
    }

    fun combineImages(
        front: Bitmap, 
        back: Bitmap,
        addStampMargin: Boolean = true
    ): Bitmap {
        // Target width will be normalized to front image width
        val targetWidth = front.width
        
        // Scale back aspect-ratio-proportionally to match the front width
        val scaleFactorBack = targetWidth.toFloat() / back.width.toFloat()
        val scaledBackHeight = (back.height * scaleFactorBack).toInt()
        val scaledBack = Bitmap.createScaledBitmap(back, targetWidth, scaledBackHeight, true)

        // Clean modern separator line
        val dividerHeight = 8
        // Dedicated footer area so stamps and mini-maps NEVER overlap or obscure the 2nd card!
        val stampMarginHeight = if (addStampMargin) (targetWidth * 0.16f).toInt().coerceAtLeast(160) else 0
        val totalHeight = front.height + dividerHeight + scaledBack.height + stampMarginHeight

        val combined = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)

        // Sleek midnight dark canvas background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#0B1120")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, targetWidth.toFloat(), totalHeight.toFloat(), bgPaint)

        // 1. Draw Front on top
        canvas.drawBitmap(front, 0f, 0f, null)

        // 2. Draw modern separator line
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
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
        val backTop = (front.height + dividerHeight).toFloat()
        canvas.drawBitmap(scaledBack, 0f, backTop, null)

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
        stampBorderEnabled: Boolean = false,
        stampSizeScale: Float = 1.0f,
        mapSizeScale: Float = 1.0f,
        isIdMode: Boolean = false
    ): Bitmap {
        // If master stamp switch is disabled, return clean unedited photo
        if (!enableAllStamps) {
            return image
        }

        val workingBitmap = image.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)
        val width = workingBitmap.width
        val height = workingBitmap.height

        // Calculate dynamic sizes based on width (~1.35% width, adjusted by user size scale)
        // In ID card mode, automatically apply a compact 0.65x multiplier so stamps never obscure card data
        val modeScale = if (isIdMode) 0.65f else 1.0f
        val effectiveScale = (stampSizeScale * modeScale).coerceIn(0.25f, 2.5f)

        val baseSize = (width * 0.0135f * effectiveScale).coerceAtLeast(14f)
        val largeTextSize = baseSize * 1.15f // For date/time
        val regularTextSize = baseSize * 0.90f // For GPS & custom text
        val badgeTextSize = baseSize * 0.72f // For DiviCam branding badge

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

        // Calculate geometry: snug, refined padding avoiding oversized boxes
        val paddingX = baseSize * 0.70f
        val paddingY = baseSize * 0.50f
        val lineSpacing = baseSize * 0.22f

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

        val effectiveMapScale = (if (isIdMode) 0.65f else 1.0f) * mapSizeScale.coerceIn(0.25f, 2.5f)
        val mapSize = (width * 0.12f * effectiveMapScale).coerceAtLeast(60f) // Sleek, non-intrusive map size

        // 1. Draw Text Watermark PILL with support for all 9 screen positions
        if (lines.isNotEmpty()) {
            val adjustedRectWidth = maxLineWidth + (paddingX * 2)
            val adjustedRectHeight = totalTextHeight + (paddingY * 2)
            val margin = width * 0.025f

            val rectLeft: Float = when (positionName) {
                "Top-left", "Center-left", "Bottom-left" -> margin
                "Top-center", "Center", "Bottom-center" -> (width - adjustedRectWidth) / 2f
                "Top-right", "Center-right", "Bottom-right" -> width - adjustedRectWidth - margin
                else -> margin
            }

            val rectTop: Float = when (positionName) {
                "Top-left", "Top-center", "Top-right" -> margin
                "Center-left", "Center", "Center-right" -> (height - adjustedRectHeight) / 2f
                "Bottom-left", "Bottom-center", "Bottom-right" -> height - adjustedRectHeight - margin
                else -> height - adjustedRectHeight - margin
            }

            val alphaInt = (stampBgOpacity * 255).toInt().coerceIn(0, 255)
            if (alphaInt > 0) {
                val bgPaint = Paint().apply {
                    color = Color.argb(alphaInt, 15, 23, 42) // Dark midnight glass
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val bgRect = RectF(rectLeft, rectTop, rectLeft + adjustedRectWidth, rectTop + adjustedRectHeight)
                canvas.drawRoundRect(bgRect, baseSize * 0.45f, baseSize * 0.45f, bgPaint)

                if (stampBorderEnabled) {
                    val borderPaint = Paint().apply {
                        color = Color.argb(40, 255, 255, 255)
                        style = Paint.Style.STROKE
                        strokeWidth = 2f
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(bgRect, baseSize * 0.45f, baseSize * 0.45f, borderPaint)
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

        // 2. Draw Separate MiniMap with support for all 9 screen positions and NO outline
        if (showMiniMap && latitude != null && longitude != null) {
            val margin = width * 0.025f
            val mapContainerWidth = mapSize + (paddingX * 2)
            val mapContainerHeight = mapSize + (paddingY * 2)

            val mapContainerLeft: Float = when (miniMapPositionName) {
                "Top-left", "Center-left", "Bottom-left" -> margin
                "Top-center", "Center", "Bottom-center" -> (width - mapContainerWidth) / 2f
                "Top-right", "Center-right", "Bottom-right" -> width - mapContainerWidth - margin
                else -> width - mapContainerWidth - margin
            }

            val mapContainerTop: Float = when (miniMapPositionName) {
                "Top-left", "Top-center", "Top-right" -> margin
                "Center-left", "Center", "Center-right" -> (height - mapContainerHeight) / 2f
                "Bottom-left", "Bottom-center", "Bottom-right" -> height - mapContainerHeight - margin
                else -> margin
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
                canvas.drawRoundRect(mapContainerRect, baseSize * 0.45f, baseSize * 0.45f, bgPaint)
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
            // Clean square map with NO border/outline for a seamless, borderless look
            val clipPath = android.graphics.Path().apply {
                addRoundRect(mapRect, baseSize * 0.35f, baseSize * 0.35f, android.graphics.Path.Direction.CW)
            }
            canvas.clipPath(clipPath)

            if (mapTile != null) {
                val scaledTile = Bitmap.createScaledBitmap(mapTile, mapSize.toInt(), mapSize.toInt(), true)
                canvas.drawBitmap(scaledTile, mapLeft, mapTop, mapAlphaPaint)
            } else {
                // Digital fallback GPS grid
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

            // Accurate GPS beacon marker positioning
            val zoom = 15
            val exactX = (longitude + 180.0) / 360.0 * (1 shl zoom)
            val xTile = exactX.toInt()
            val fracX = (exactX - xTile).toFloat().coerceIn(0.08f, 0.92f)

            val latRad = latitude * Math.PI / 180.0
            val exactY = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * (1 shl zoom)
            val yTile = exactY.toInt()
            val fracY = (exactY - yTile).toFloat().coerceIn(0.08f, 0.92f)

            val pinX = mapLeft + (fracX * mapSize)
            val pinY = mapTop + (fracY * mapSize)

            val targetPaint = Paint().apply {
                color = Color.parseColor("#FF38BDF8")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val targetRingPaint = Paint().apply {
                color = Color.parseColor("#8038BDF8")
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                isAntiAlias = true
            }

            canvas.drawCircle(pinX, pinY, baseSize * 0.35f, targetRingPaint)
            canvas.drawCircle(pinX, pinY, baseSize * 0.14f, targetPaint)

            canvas.restore()

            // Map border outline: only if explicitly enabled (default is false / NO outline)
            if (mapBorderEnabled) {
                val borderPaint = Paint().apply {
                    color = Color.argb(60, 255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    isAntiAlias = true
                }
                canvas.drawRoundRect(mapRect, baseSize * 0.35f, baseSize * 0.35f, borderPaint)
            }
        }

        return workingBitmap
    }

    private fun fetchMapTile(lat: Double, lng: Double): Bitmap? {
        val zoom = 15
        val x = ((lng + 180.0) / 360.0 * (1 shl zoom)).toInt()
        val latRad = lat * Math.PI / 180.0
        val y = ((1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * (1 shl zoom)).toInt()
        val key = "$zoom/$x/$y"

        val cached = mapTileCache.get(key)
        if (cached != null) return cached

        // Trigger async prefetch for future shots so current photo renders instantly without latency
        prefetchMapTile(lat, lng)
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

        // Card cutout geometry ratio on screen (matches GuideOverlay 1:1)
        val boxWidth = screenWidth * 0.82f
        val boxHeight = boxWidth / 1.585f
        val boxLeft = (screenWidth - boxWidth) / 2f
        val boxTop = (screenHeight - boxHeight) / 2.2f

        // When bitmap is already screen-sized (e.g. from PreviewView instant capture)
        val (imgLeft, imgTop, imgWidth, imgHeight) = if (kotlin.math.abs(camW - screenWidth) < 2f && kotlin.math.abs(camH - screenHeight) < 2f) {
            listOf(boxLeft, boxTop, boxWidth, boxHeight)
        } else {
            // Camera scale under PreviewView FILL_CENTER
            val scale = java.lang.Math.max(screenWidth / camW, screenHeight / camH)
            val scaledCamW = camW * scale
            val scaledCamH = camH * scale
            val offsetX = (screenWidth - scaledCamW) / 2f
            val offsetY = (screenHeight - scaledCamH) / 2f

            listOf(
                (boxLeft - offsetX) / scale,
                (boxTop - offsetY) / scale,
                boxWidth / scale,
                boxHeight / scale
            )
        }

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

    /**
     * Normalizes image resolution to standard mobile camera dimension (e.g. 1920 or 2048px).
     * Eliminates huge 48MP/12MP memory bloat and keeps WebP output crisp and compact.
     */
    fun normalizeResolution(bitmap: Bitmap, maxDimension: Int = 1920): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDimension && h <= maxDimension) {
            return bitmap
        }
        val scale = maxDimension.toFloat() / maxOf(w, h)
        val newW = (w * scale).toInt().coerceAtLeast(1)
        val newH = (h * scale).toInt().coerceAtLeast(1)
        return try {
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } catch (e: Exception) {
            Log.e("ImageProcessor", "Failed to scale bitmap resolution", e)
            bitmap
        }
    }

    /**
     * Ensures what the user sees in the viewfinder is 1:1 identical to what is captured in the output.
     */
    fun cropToVisibleViewfinder(
        bitmap: Bitmap,
        screenWidth: Float,
        screenHeight: Float,
        isFitCenter: Boolean = false
    ): Bitmap {
        if (screenWidth <= 0f || screenHeight <= 0f) {
            return bitmap
        }
        if (isFitCenter) {
            return bitmap
        }

        val camW = bitmap.width.toFloat()
        val camH = bitmap.height.toFloat()

        // If bitmap is already screen sized (instant viewfinder grab), return as is
        if (kotlin.math.abs(camW - screenWidth) < 2f && kotlin.math.abs(camH - screenHeight) < 2f) {
            return bitmap
        }

        val scale = java.lang.Math.max(screenWidth / camW, screenHeight / camH)
        val scaledCamW = camW * scale
        val scaledCamH = camH * scale
        val offsetX = (screenWidth - scaledCamW) / 2f
        val offsetY = (screenHeight - scaledCamH) / 2f

        val imgLeft = (0f - offsetX) / scale
        val imgTop = (0f - offsetY) / scale
        val imgWidth = screenWidth / scale
        val imgHeight = screenHeight / scale

        val x = imgLeft.toInt().coerceIn(0, bitmap.width - 1)
        val y = imgTop.toInt().coerceIn(0, bitmap.height - 1)
        val w = imgWidth.toInt().coerceIn(1, bitmap.width - x)
        val h = imgHeight.toInt().coerceIn(1, bitmap.height - y)

        return try {
            Bitmap.createBitmap(bitmap, x, y, w, h)
        } catch (e: Exception) {
            Log.e("ImageProcessor", "Failed to crop viewfinder", e)
            bitmap
        }
    }

    /**
     * Trims black letterbox borders when capturing directly from PreviewView FIT_CENTER
     * so the instant photo is pristine and matches the camera sensor output with no black bars.
     */
    fun cropLetterboxBars(bitmap: Bitmap, targetAspectRatio: Float? = null): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return bitmap

        val currentAspect = w.toFloat() / h.toFloat()
        val targetAspect = targetAspectRatio ?: if (w < h) {
            // Portrait: typical camera aspect ratio is 3:4 (0.75f) or 9:16 (0.5625f)
            if (currentAspect < 0.65f) {
                if (currentAspect < 0.50f) 0.5625f else 0.75f
            } else {
                currentAspect
            }
        } else {
            // Landscape: typical camera aspect ratio is 4:3 or 16:9
            if (currentAspect > 1.55f) {
                16f / 9f
            } else {
                4f / 3f
            }
        }

        if (kotlin.math.abs(currentAspect - targetAspect) < 0.03f) {
            return bitmap
        }

        return if (currentAspect < targetAspect) {
            // Screen is taller than sensor aspect -> trim top and bottom black bars
            val contentHeight = (w / targetAspect).toInt().coerceIn(1, h)
            val top = ((h - contentHeight) / 2).coerceAtLeast(0)
            val safeHeight = contentHeight.coerceAtMost(h - top)
            try {
                Bitmap.createBitmap(bitmap, 0, top, w, safeHeight)
            } catch (e: Exception) {
                bitmap
            }
        } else {
            // Screen is wider than sensor aspect -> trim left and right black bars
            val contentWidth = (h * targetAspect).toInt().coerceIn(1, w)
            val left = ((w - contentWidth) / 2).coerceAtLeast(0)
            val safeWidth = contentWidth.coerceAtMost(w - left)
            try {
                Bitmap.createBitmap(bitmap, left, 0, safeWidth, h)
            } catch (e: Exception) {
                bitmap
            }
        }
    }
}
