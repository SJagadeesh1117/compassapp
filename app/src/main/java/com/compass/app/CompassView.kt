package com.compass.app

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var azimuth: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    // --- Paints ---

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#16213E")
        style = Paint.Style.FILL
    }

    private val outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E94560")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val innerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E3A5F")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val tickPaintHeavy = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CDD6F4")
        strokeCap = Paint.Cap.ROUND
    }

    private val tickPaintLight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#45475A")
        strokeCap = Paint.Cap.ROUND
    }

    private val northLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F38BA8")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
    }

    private val cardinalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CDD6F4")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
    }

    private val intercardinalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C7086")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
    }

    private val degreePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CDD6F4")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F38BA8")
        style = Paint.Style.FILL
    }

    private val centerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F0F1A")
        style = Paint.Style.FILL
    }

    private val centerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E94560")
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val r = minOf(cx, cy) * 0.90f

        // Scale-dependent sizes
        val cardinalSize = r * 0.14f
        val intercardinalSize = r * 0.095f
        val degreeSize = r * 0.20f
        northLabelPaint.textSize = cardinalSize * 1.15f
        cardinalLabelPaint.textSize = cardinalSize
        intercardinalLabelPaint.textSize = intercardinalSize
        degreePaint.textSize = degreeSize

        // Background circle
        canvas.drawCircle(cx, cy, r, bgPaint)

        // === ROTATING COMPASS ROSE ===
        canvas.save()
        canvas.rotate(-azimuth, cx, cy)

        // Tick marks around the edge
        val tickOuterR = r * 0.90f
        for (i in 0 until 360 step 5) {
            val isCardinal = i % 90 == 0
            val isIntercardinal = i % 45 == 0 && !isCardinal
            val isMajorTen = i % 10 == 0

            val tickLen = when {
                isCardinal -> r * 0.12f
                isIntercardinal -> r * 0.09f
                isMajorTen -> r * 0.05f
                else -> r * 0.03f
            }
            val tickInnerR = tickOuterR - tickLen

            val paint = if (isMajorTen || isCardinal || isIntercardinal) tickPaintHeavy else tickPaintLight
            paint.strokeWidth = when {
                isCardinal -> r * 0.014f
                isIntercardinal -> r * 0.009f
                isMajorTen -> r * 0.006f
                else -> r * 0.003f
            }

            val rad = Math.toRadians(i.toDouble())
            val sinA = sin(rad).toFloat()
            val cosA = cos(rad).toFloat()

            canvas.drawLine(
                cx + tickOuterR * sinA, cy - tickOuterR * cosA,
                cx + tickInnerR * sinA, cy - tickInnerR * cosA,
                paint
            )
        }

        // Inner decorative ring
        canvas.drawCircle(cx, cy, r * 0.56f, innerRingPaint)

        // Cardinal direction labels (N, E, S, W)
        val cardinalR = r * 0.70f
        val cardinals = listOf(0 to "N", 90 to "E", 180 to "S", 270 to "W")
        for ((deg, label) in cardinals) {
            val rad = Math.toRadians(deg.toDouble())
            val x = cx + (cardinalR * sin(rad)).toFloat()
            val y = cy - (cardinalR * cos(rad)).toFloat()
            val paint = if (deg == 0) northLabelPaint else cardinalLabelPaint
            canvas.drawText(label, x, y + paint.textSize * 0.37f, paint)
        }

        // Intercardinal labels (NE, SE, SW, NW)
        val intercardinalR = r * 0.71f
        val intercardinals = listOf(45 to "NE", 135 to "SE", 225 to "SW", 315 to "NW")
        for ((deg, label) in intercardinals) {
            val rad = Math.toRadians(deg.toDouble())
            val x = cx + (intercardinalR * sin(rad)).toFloat()
            val y = cy - (intercardinalR * cos(rad)).toFloat()
            canvas.drawText(label, x, y + intercardinalLabelPaint.textSize * 0.37f, intercardinalLabelPaint)
        }

        canvas.restore()
        // === END ROTATING PART ===

        // Outer border ring (fixed)
        canvas.drawCircle(cx, cy, r, outerRingPaint)

        // Fixed indicator triangle at 12 o'clock (points INTO the compass)
        val indTipY = cy - r * 0.76f
        val indTopY = cy - r * 0.97f
        val indHalfW = r * 0.055f
        val indicatorPath = Path().apply {
            moveTo(cx, indTipY)
            lineTo(cx - indHalfW, indTopY)
            lineTo(cx + indHalfW, indTopY)
            close()
        }
        canvas.drawPath(indicatorPath, indicatorPaint)

        // Center background disk
        val centerR = r * 0.22f
        canvas.drawCircle(cx, cy, centerR, centerBgPaint)
        canvas.drawCircle(cx, cy, centerR, centerRingPaint)

        // Degree text in center
        val degText = "${azimuth.toInt()}°"
        canvas.drawText(degText, cx, cy + degreePaint.textSize * 0.36f, degreePaint)
    }
}
