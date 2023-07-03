package com.github.mrko900.braintrainer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

class CircularProgressBar : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    private val paint: Paint = Paint()

    var strokeWidth: Float = 0f
        set(value) {
            field = value
            paint.strokeWidth = value
        }

    @ColorInt
    var filledColor: Int = Color.BLUE

    @ColorInt
    var blankColor: Int = Color.parseColor("#D3D3D3")

    var percentage: Float = 0.5f

    init {
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        strokeWidth = 30f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!

        val sw = strokeWidth / 2
        paint.color = filledColor
        canvas.drawArc(
            sw,
            sw,
            width.toFloat() - sw,
            height.toFloat() - sw,
            -90f,
            -percentage * 360f,
            false,
            paint
        )
        paint.color = blankColor
        canvas.drawArc(
            sw,
            sw,
            width.toFloat() - sw,
            height.toFloat() - sw,
            -percentage * 360f - 90f,
            -(1 - percentage) * 360f,
            false,
            paint
        )
    }
}
