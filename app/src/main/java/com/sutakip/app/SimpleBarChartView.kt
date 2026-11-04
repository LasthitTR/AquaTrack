package com.sutakip.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class SimpleBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.chart_bar)
        style = Paint.Style.FILL
    }

    private val goalLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.success)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_secondary)
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_primary)
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    private var entries: List<Pair<String, Double>> = emptyList()
    private var maxGoal: Double = 2.5

    fun setData(data: List<Pair<String, Double>>, targetGoal: Double) {
        this.entries = data
        this.maxGoal = if (targetGoal > 0) targetGoal else 2.5
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (entries.isEmpty()) return

        val paddingBottom = 60f
        val paddingTop = 40f
        val paddingLeft = 40f
        val paddingRight = 40f

        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom

        var highestVal = maxGoal
        for (item in entries) {
            if (item.second > highestVal) {
                highestVal = item.second
            }
        }
        highestVal *= 1.15 // Üstten biraz boşluk kalsın

        val barCount = entries.size
        val slotWidth = availableWidth / barCount
        val barWidth = (slotWidth * 0.55f).coerceAtMost(60f)

        // Hedef referans çizgisi
        val goalY = (paddingTop + availableHeight * (1.0 - (maxGoal / highestVal))).toFloat()
        canvas.drawLine(paddingLeft, goalY, width - paddingRight, goalY, goalLinePaint)

        val rect = RectF()
        for (i in entries.indices) {
            val (label, value) = entries[i]
            val centerX = paddingLeft + slotWidth * i + slotWidth / 2f
            val barHeight = (availableHeight * (value / highestVal)).toFloat()
            val top = paddingTop + availableHeight - barHeight
            val bottom = paddingTop + availableHeight

            rect.set(
                centerX - barWidth / 2f,
                top,
                centerX + barWidth / 2f,
                bottom
            )
            // Köşeleri yuvarlatılmış sütun
            canvas.drawRoundRect(rect, 8f, 8f, barPaint)

            // Değer yazısı
            val valText = if (value > 0) String.format("%.1f", value) else "0"
            canvas.drawText(valText, centerX, top - 8f, valPaint)

            // Alt etiket (örn. G1, G2 veya tarih kısaltması)
            canvas.drawText(label, centerX, height - 16f, labelPaint)
        }
    }
}

