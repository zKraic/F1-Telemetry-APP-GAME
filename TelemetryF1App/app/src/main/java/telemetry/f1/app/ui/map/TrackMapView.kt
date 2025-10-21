package telemetry.f1.app.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import telemetry.f1.app.R

class TrackMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val playerCarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.f1_red)
        style = Paint.Style.FILL
    }

    private val otherCarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 20f // Thicker track line for visibility
    }

    private var carPositions = listOf<CarPosition>()
    private var trackPath: Path? = null

    fun setTrackId(trackId: Int) {
        trackPath = TrackData.getTrackPath(trackId)
        // Reset the car path when the track changes
        invalidate()
    }

    fun updateCarPositions(positions: List<CarPosition>) {
        carPositions = positions
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        canvas.save()

        // Center the view
        canvas.translate(width / 2f, height / 2f)
        
        // --- Static, Zoomed-Out View ---
        // Adjust scale for a more distant view. The negative inverts the Y-axis.
        canvas.scale(0.12f, -0.12f)

        // Draw the track outline from TrackData.kt
        trackPath?.let { 
            canvas.drawPath(it, trackPaint) 
        }

        // Draw the cars on the track
        carPositions.forEach { position ->
            val paint = if (position.isPlayer) playerCarPaint else otherCarPaint
            // The coordinates are scaled by the canvas transform
            canvas.drawCircle(position.x, position.y, 40f, paint) // Larger circles for cars
        }

        canvas.restore()
    }
}
