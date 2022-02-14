package com.ick.kalambury.drawing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ick.kalambury.R
import com.ick.kalambury.drawing.GameViewModel.DrawEvent
import com.ick.kalambury.net.connection.model.DrawableData

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    var mode = MODE_DISPLAY

    var color = 0
    var thickness = 0

    private var cWidth = 0
    private var cHeight = 0
    private var currentDrawable: DrawableData? = null
    private var drawables: MutableList<DrawableData> = mutableListOf()
    private var listener: OnDrawListener? = null

    init {
        color = Color.BLACK
        thickness = context.resources.getIntArray(R.array.line_weights_dp)[0]

        val values = context.obtainStyledAttributes(attrs, R.styleable.CanvasView)
        mode = values.getInt(R.styleable.CanvasView_view_mode, MODE_DISPLAY)
        values.recycle()
    }

    fun setOnDrawListener(listener: OnDrawListener?) {
        this.listener = listener
    }

    fun handleDrawEvent(drawEvent: DrawEvent) {
        when(drawEvent) {
            is DrawEvent.Add -> drawables.addAll(drawEvent.drawables)
            is DrawEvent.Undo -> drawables.removeLastOrNull()
            is DrawEvent.Clear -> drawables.clear()
        }
        invalidate()
    }

    private val newDrawable: DrawableData
        get() = DrawableData(thickness, color, cWidth, cHeight)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        cWidth = w
        cHeight = h
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mode == MODE_DRAW) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    currentDrawable = newDrawable.apply {
                        addPoint(event.x.toInt(), event.y.toInt())
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    currentDrawable?.let {
                        if (it.points.size == MAX_LINE_LENGTH - 1) {
                            it.addPoint(event.x.toInt(), event.y.toInt())
                            listener?.onDraw(it)
                            drawables.add(it)
                            currentDrawable = newDrawable.apply {
                                addPoint(event.x.toInt(), event.y.toInt())
                            }
                        } else {
                            it.addPoint(event.x.toInt(), event.y.toInt())
                        }
                        invalidate()
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    currentDrawable?.let {
                        it.addPoint(event.x.toInt(), event.y.toInt())
                        listener?.onDraw(it)
                        drawables.add(it)
                    }
                    currentDrawable = null
                    invalidate()
                    true
                }
                else -> super.onTouchEvent(event)
            }
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (d in drawables) {
            canvas.drawPath(d.getScaledPath(cWidth, cHeight), d.paint)
        }

        currentDrawable?.let {
            canvas.drawPath(it.path, it.paint)
        }
    }

    fun interface OnDrawListener {
        fun onDraw(data: DrawableData)
    }

    companion object {
        const val MODE_DRAW = 0
        const val MODE_DISPLAY = 1

        private const val MAX_LINE_LENGTH = 30
    }
}