package com.ick.kalambury.net.connection.model

import android.graphics.*
import com.ick.kalambury.entities.GameDataProtos
import com.ick.kalambury.entities.GameDataProtos.Drawable.Tool
import com.ick.kalambury.util.toPx

class DrawableData(
    private val strokeWidth: Int,
    val color: Int,
    val width: Int,
    val height: Int,
    val points: MutableList<Point> = mutableListOf(),
) {

    var paint: Paint = createPaint()
    var path: Path = createPath()

    private fun createPath(): Path {
        return Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x.toFloat(), points[0].y.toFloat())
                for (i in 1 until points.size) {
                    lineTo(points[i].x.toFloat(), points[i].y.toFloat())
                }
            }
        }
    }

    private fun createPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            pathEffect = CornerPathEffect(100f)
            strokeWidth = this@DrawableData.strokeWidth.toPx
            color = this@DrawableData.color
            style = Paint.Style.STROKE
        }
    }

    fun addPoint(x: Int, y: Int) {
        if (points.isEmpty()) {
            val p = Path()
            p.moveTo(x.toFloat(), y.toFloat())
            path = p
        } else {
            path.lineTo(x.toFloat(), y.toFloat())
        }
        points.add(Point(x, y))
    }

    fun getScaledPath(width: Int, height: Int): Path {
        val m = Matrix()
        m.setScale(width.toFloat() / this.width, height.toFloat() / this.height)
        val p = Path(path)
        p.transform(m)
        return p
    }

    fun toProto(): GameDataProtos.Drawable {
        return GameDataProtos.Drawable.newBuilder()
            .setTool(Tool.PENCIL)
            .setStrokeWidth(strokeWidth)
            .setColor(color)
            .setWidth(width)
            .setHeight(height)
            .addAllPoints(points
                .map { point: Point ->
                    GameDataProtos.Drawable.Point.newBuilder()
                        .setX(point.x)
                        .setY(point.y)
                        .build()
                })
            .build()
    }

    companion object {

        fun fromProto(drawable: GameDataProtos.Drawable): DrawableData {
            return DrawableData(
                drawable.strokeWidth,
                drawable.color,
                drawable.width,
                drawable.height,
                drawable.pointsList.map { p -> Point(p.x, p.y) }.toMutableList()
            )
        }

    }
}