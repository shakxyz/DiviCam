package com.example.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuideOverlay(
    isIdMode: Boolean,
    currentStep: Int, // 1 = Front, 2 = Back
    modifier: Modifier = Modifier
) {
    if (!isIdMode) return

    val labelText = if (currentStep == 1) {
        "Position FRONT of ID"
    } else {
        "Now flip — Position BACK of ID"
    }

    val density = androidx.compose.ui.platform.LocalDensity.current.density

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Card cutout geometry
        val cardWidthPx = widthPx * 0.82f
        val cardHeightPx = cardWidthPx / 1.585f // ID-1 Card aspect ratio

        val left = (widthPx - cardWidthPx) / 2f
        val top = (heightPx - cardHeightPx) / 2.2f // slightly above centered to make space for controls

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cardRect = RoundRect(
                rect = Rect(
                    offset = Offset(left, top),
                    size = Size(cardWidthPx, cardHeightPx)
                ),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )

            val outerPath = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height))
            }

            val innerPath = Path().apply {
                addRoundRect(cardRect)
            }

            // Draw slightly dimmed background outside the cutout
            clipPath(innerPath, clipOp = ClipOp.Difference) {
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    size = size
                )
            }

            // Draw dashed yellow/white border around the cutout
            val strokeColor = if (currentStep == 1) Color(0xFFFFD600) else Color.White
            drawRoundRect(
                color = strokeColor.copy(alpha = 0.6f),
                topLeft = Offset(left, top),
                size = Size(cardWidthPx, cardHeightPx),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                )
            )

            // Draw premium blue corner brackets
            val bracketLen = 24.dp.toPx()
            val bracketThickness = 4.dp.toPx()
            val bracketColor = Color(0xFF1A73E8)

            // Top-Left
            drawPath(
                path = Path().apply {
                    moveTo(left, top + bracketLen)
                    lineTo(left, top)
                    lineTo(left + bracketLen, top)
                },
                color = bracketColor,
                style = Stroke(width = bracketThickness)
            )
            // Top-Right
            drawPath(
                path = Path().apply {
                    moveTo(left + cardWidthPx - bracketLen, top)
                    lineTo(left + cardWidthPx, top)
                    lineTo(left + cardWidthPx, top + bracketLen)
                },
                color = bracketColor,
                style = Stroke(width = bracketThickness)
            )
            // Bottom-Left
            drawPath(
                path = Path().apply {
                    moveTo(left, top + cardHeightPx - bracketLen)
                    lineTo(left, top + cardHeightPx)
                    lineTo(left + bracketLen, top + cardHeightPx)
                },
                color = bracketColor,
                style = Stroke(width = bracketThickness)
            )
            // Bottom-Right
            drawPath(
                path = Path().apply {
                    moveTo(left + cardWidthPx - bracketLen, top + cardHeightPx)
                    lineTo(left + cardWidthPx, top + cardHeightPx)
                    lineTo(left + cardWidthPx, top + cardHeightPx - bracketLen)
                },
                color = bracketColor,
                style = Stroke(width = bracketThickness)
            )
        }

        // Card Overlay Instruction Text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = (top / density - 58).coerceAtLeast(12f).dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = labelText.uppercase(),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = "ALIGN EDGES WITHIN THE BOX",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
