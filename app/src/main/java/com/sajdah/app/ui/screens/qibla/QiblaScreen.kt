package com.sajdah.app.ui.screens.qibla

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sajdah.app.ui.theme.GoldAccent
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(viewModel: QiblaViewModel = hiltViewModel()) {
    val azimuth by viewModel.azimuth.collectAsState()
    val qiblaDirection by viewModel.qiblaDirection.collectAsState()

    // ── Compass math ──────────────────────────────────────────────────────────
    // The rotating dial already counter-rotates by -azimuth so that N stays up.
    // The Qibla needle is drawn OUTSIDE the dial rotation, so its angle in screen
    // space = qiblaDirection - azimuth (i.e. "where Makkah is relative to current
    // phone heading").
    val needleAngle = qiblaDirection - azimuth

    // Aligned when the needle is pointing straight up (≈ 0° or 360°)
    val alignedDiff = ((needleAngle % 360) + 360) % 360
    val isAligned = alignedDiff < 3f || alignedDiff > 357f

    // Hoist theme colours so they are available inside Canvas
    val primaryColor   = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor   = MaterialTheme.colorScheme.surface
    val bgColor        = MaterialTheme.colorScheme.background

    // ── Cardinal direction label ──────────────────────────────────────────────
    val headingText = when {
        azimuth < 22.5f || azimuth >= 337.5f -> "N"
        azimuth < 67.5f  -> "NE"
        azimuth < 112.5f -> "E"
        azimuth < 157.5f -> "SE"
        azimuth < 202.5f -> "S"
        azimuth < 247.5f -> "SW"
        azimuth < 292.5f -> "W"
        else             -> "NW"
    }

    val locationData by viewModel.locationRepository.currentLocation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Title ─────────────────────────────────────────────────────────────
        Text(
            text = "Qibla Compass",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        // Location row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${locationData.cityName}  →  Makkah  (${qiblaDirection.toInt()}°)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Heading chip
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 4.dp,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(
                text = "${azimuth.toInt()}°  $headingText",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        // ── Compass Canvas ────────────────────────────────────────────────────
        Box(
            modifier = Modifier.size(320.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center     = Offset(size.width / 2f, size.height / 2f)
                val outerR     = size.width / 2f
                val bezelW     = 12.dp.toPx()
                val dialR      = outerR - bezelW - 2.dp.toPx()   // inner edge of the dial
                val tickOuterR = dialR                             // ticks start here
                val labelR     = dialR - 34.dp.toPx()             // N/S/E/W sit here

                // ── Static: gold bezel ────────────────────────────────────────
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF8B6914), Color(0xFFFFDF00),
                            Color(0xFFDAA520), Color(0xFFFFDF00),
                            Color(0xFF8B6914)
                        ),
                        center = center
                    ),
                    radius = outerR - bezelW / 2f,
                    style = Stroke(width = bezelW)
                )
                // Dial face background
                drawCircle(color = Color(0xFF0D1520), radius = dialR)
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = dialR,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // ── Rotating dial (ticks + N/S/E/W + rose) ───────────────────
                // Everything inside rotates by -azimuth so that North stays up on screen
                rotate(-azimuth, center) {

                    // Tick marks — use trig so they always radiate from the rim
                    for (deg in 0 until 360 step 2) {
                        val isMain = deg % 90 == 0
                        val isMid  = deg % 45 == 0
                        val isSub  = deg % 10 == 0

                        if (!isMain && !isMid && !isSub && deg % 2 != 0) continue

                        val tickLen = when {
                            isMain -> 20.dp.toPx()
                            isMid  -> 14.dp.toPx()
                            isSub  -> 9.dp.toPx()
                            else   -> 5.dp.toPx()
                        }
                        val tickW = when {
                            isMain -> 3.5.dp.toPx()
                            isMid  -> 2.dp.toPx()
                            else   -> 1.dp.toPx()
                        }
                        val color = when {
                            deg == 0 -> Color.Red
                            isMain   -> primaryColor
                            isMid    -> primaryColor.copy(alpha = 0.75f)
                            else     -> primaryColor.copy(alpha = 0.35f)
                        }

                        val rad = Math.toRadians(deg.toDouble() - 90.0)
                        val startX = center.x + tickOuterR * cos(rad).toFloat()
                        val startY = center.y + tickOuterR * sin(rad).toFloat()
                        val endX   = center.x + (tickOuterR - tickLen) * cos(rad).toFloat()
                        val endY   = center.y + (tickOuterR - tickLen) * sin(rad).toFloat()

                        drawLine(
                            color = color,
                            start = Offset(startX, startY),
                            end   = Offset(endX, endY),
                            strokeWidth = tickW,
                            cap = StrokeCap.Round
                        )
                    }

                    // N / S / E / W labels
                    val textPaint = android.graphics.Paint().apply {
                        textSize  = 48f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface  = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.apply {
                        textPaint.color = android.graphics.Color.RED
                        drawText("N", center.x, center.y - labelR + 16f, textPaint)

                        textPaint.color = primaryColor.toArgb()
                        drawText("S", center.x, center.y + labelR + 16f, textPaint)
                        drawText("E", center.x + labelR, center.y + 16f, textPaint)
                        drawText("W", center.x - labelR, center.y + 16f, textPaint)
                    }

                    // Small decorative rose at center
                    val roseR = dialR * 0.18f
                    val rose = Path().apply {
                        moveTo(center.x, center.y - roseR)
                        lineTo(center.x + roseR * 0.28f, center.y - roseR * 0.28f)
                        lineTo(center.x + roseR, center.y)
                        lineTo(center.x + roseR * 0.28f, center.y + roseR * 0.28f)
                        lineTo(center.x, center.y + roseR)
                        lineTo(center.x - roseR * 0.28f, center.y + roseR * 0.28f)
                        lineTo(center.x - roseR, center.y)
                        lineTo(center.x - roseR * 0.28f, center.y - roseR * 0.28f)
                        close()
                    }
                    drawPath(rose, color = primaryColor.copy(alpha = 0.07f))
                    drawPath(rose, color = primaryColor.copy(alpha = 0.35f),
                        style = Stroke(width = 1.dp.toPx()))
                }

                // ── Qibla needle (fixed to screen, points to Makkah) ─────────
                rotate(needleAngle, center) {
                    val needleLen  = dialR - 18.dp.toPx()
                    val halfBase   = 16.dp.toPx()
                    val tailLength = 36.dp.toPx()

                    // Needle tip (pointing up = toward Makkah)
                    val tip = Offset(center.x, center.y - needleLen)

                    // Arrow body (gold, toward Makkah)
                    val arrowPath = Path().apply {
                        moveTo(tip.x, tip.y)
                        lineTo(center.x + halfBase, center.y + tailLength)
                        lineTo(center.x, center.y + tailLength * 0.5f)
                        lineTo(center.x - halfBase, center.y + tailLength)
                        close()
                    }
                    // Drop shadow
                    drawPath(arrowPath, color = Color.Black.copy(alpha = 0.3f),
                        style = Stroke(width = 3.dp.toPx()))
                    // Gold fill
                    drawPath(
                        arrowPath,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = if (isAligned)
                                listOf(Color(0xFF00E676), Color(0xFF69F0AE))
                            else
                                listOf(Color(0xFFFFD700), Color(0xFFDAA520)),
                            start = tip,
                            end   = Offset(center.x, center.y + tailLength)
                        )
                    )
                    drawPath(arrowPath,
                        color = if (isAligned) Color(0xFF00E676) else GoldAccent,
                        style = Stroke(width = 1.5.dp.toPx()))

                    // Opposite tail (darker, pointing away from Makkah)
                    val tailPath = Path().apply {
                        moveTo(center.x, center.y - tailLength * 0.4f)
                        lineTo(center.x + halfBase * 0.7f, center.y + tailLength * 0.2f)
                        lineTo(center.x, center.y + tailLength * 0.55f)
                        lineTo(center.x - halfBase * 0.7f, center.y + tailLength * 0.2f)
                        close()
                    }
                    drawPath(tailPath, color = Color(0xFF555555))
                }

                // ── Static center pivot ───────────────────────────────────────
                drawCircle(color = surfaceColor,   radius = 14.dp.toPx(), center = center)
                drawCircle(color = primaryColor,   radius = 9.dp.toPx(),  center = center)
                drawCircle(color = bgColor,        radius = 4.dp.toPx(),  center = center)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Status pill ───────────────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isAligned) primaryColor.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isAligned) primaryColor
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (isAligned) "✓ Perfectly Aligned with Qibla!"
                           else "Rotate phone until needle points up",
                    color = if (isAligned) primaryColor
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isAligned) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}
