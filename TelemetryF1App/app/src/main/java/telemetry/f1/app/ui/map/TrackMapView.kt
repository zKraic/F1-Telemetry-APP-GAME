package telemetry.f1.app.ui.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import telemetry.f1.app.R

class TrackMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint objects for different elements
    private val backgroundPaint = Paint().apply {
        color = context.getColor(R.color.f1_background)
    }
    
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.f1_dark_gray)
        strokeWidth = dpToPx(4f)
        style = Paint.Style.STROKE
    }
    
    // Player car: Red with white border for maximum visibility
    private val playerCarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.f1_red)
    }
    
    private val playerCarStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.f1_white)
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2f)
    }
    
    // Other cars: White with gray border
    private val otherCarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.f1_white)
    }
    
    private val otherCarStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.f1_gray)
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1.5f)
    }
    
    // Text paint for car numbers (optional)
    private val carNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.f1_black)
        textSize = dpToPx(10f)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    
    // Grid paint for background reference
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.f1_surface)
        strokeWidth = dpToPx(0.5f)
        pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
    }

    private var carPositions = listOf<CarPosition>()
    private val carRadius = dpToPx(12f) // Larger markers as requested
    
    // Default track path for when no circuit is loaded
    private var defaultTrackPath = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createDefaultTrack(w, h)
    }

    private fun createDefaultTrack(width: Int, height: Int) {
        defaultTrackPath.reset()
        
        val margin = dpToPx(50f)
        val cornerRadius = dpToPx(80f)
        
        val rect = RectF(
            margin,
            margin,
            width - margin,
            height - margin
        )
        
        defaultTrackPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Optional grid for reference
        drawGrid(canvas)
        
        // Default track outline (always visible as reference)
        canvas.drawPath(defaultTrackPath, trackPaint)
        
        // Draw cars with improved visibility
        drawCars(canvas)
        
        // Debug info (can be removed in production)
        drawDebugInfo(canvas)
    }
    
    private fun drawGrid(canvas: Canvas) {
        val gridSize = dpToPx(50f)
        
        // Vertical lines
        var x = gridSize
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += gridSize
        }
        
        // Horizontal lines
        var y = gridSize
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += gridSize
        }
    }
    
    private fun drawCars(canvas: Canvas) {
        for (position in carPositions) {
            val x = position.x
            val y = position.y
            
            if (position.isPlayer) {
                // Player car: Red with white border
                canvas.drawCircle(x, y, carRadius, playerCarPaint)
                canvas.drawCircle(x, y, carRadius, playerCarStrokePaint)
                
                // Optional: draw car number
                canvas.drawText("P", x, y + dpToPx(3f), carNumberPaint)
            } else {
                // Other cars: White with gray border
                canvas.drawCircle(x, y, carRadius * 0.8f, otherCarPaint)
                canvas.drawCircle(x, y, carRadius * 0.8f, otherCarStrokePaint)
                
                // Optional: draw car index (small)
                if (carPositions.size <= 10) { // Only show numbers if not too crowded
                    canvas.drawText((position.carIndex + 1).toString(), x, y + dpToPx(3f), carNumberPaint)
                }
            }
        }
    }
    
    private fun drawDebugInfo(canvas: Canvas) {
        val debugText = "Cars: ${carPositions.size}"
        val debugPaint = Paint().apply {
            color = context.getColor(R.color.f1_gray)
            textSize = dpToPx(12f)
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText(debugText, dpToPx(16f), height - dpToPx(16f), debugPaint)
    }
    
    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    fun updateCarPositions(positions: List<CarPosition>) {
        carPositions = positions
        invalidate() // Trigger redraw
    }
    
    fun setTrackInfo(trackInfo: TrackInfo) {
        // This could be used to load track-specific background in the future
        invalidate()
    }
}