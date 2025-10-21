package telemetry.f1.app.ui.map

import telemetry.f1.app.R

/**
 * Track assets and positioning parameters for F1 circuits.
 * Maps trackId from session data to drawable resources and transformation parameters.
 */
data class TrackInfo(
    val name: String,
    val drawableRes: Int,
    val bounds: TrackBounds,
    val rotationDegrees: Float = 0f
)

data class TrackBounds(
    val minX: Float,
    val maxX: Float,
    val minZ: Float,
    val maxZ: Float
)

object TrackAssets {
    
    // Track positioning parameters based on F1 coordinate system
    // X and Z are horizontal coordinates, Y is vertical (not used for map)
    private val trackDatabase = mapOf(
        0 to TrackInfo(
            name = "Albert Park Circuit (Melbourne)",
            drawableRes = R.drawable.track_melbourne,
            bounds = TrackBounds(-600f, 600f, -800f, 400f),
            rotationDegrees = 0f
        ),
        1 to TrackInfo(
            name = "Circuit Paul Ricard (France)",
            drawableRes = R.drawable.track_paul_ricard,
            bounds = TrackBounds(-800f, 800f, -600f, 600f),
            rotationDegrees = 90f
        ),
        2 to TrackInfo(
            name = "Silverstone Circuit (Britain)",
            drawableRes = R.drawable.track_silverstone,
            bounds = TrackBounds(-1000f, 1000f, -800f, 800f),
            rotationDegrees = 45f
        ),
        3 to TrackInfo(
            name = "Spa-Francorchamps (Belgium)",
            drawableRes = R.drawable.track_spa,
            bounds = TrackBounds(-1200f, 800f, -600f, 1000f),
            rotationDegrees = 0f
        ),
        4 to TrackInfo(
            name = "Monaco Circuit",
            drawableRes = R.drawable.track_monaco,
            bounds = TrackBounds(-400f, 400f, -800f, 400f),
            rotationDegrees = 180f
        ),
        5 to TrackInfo(
            name = "Baku City Circuit (Azerbaijan)",
            drawableRes = R.drawable.track_baku,
            bounds = TrackBounds(-800f, 1200f, -600f, 600f),
            rotationDegrees = 270f
        ),
        6 to TrackInfo(
            name = "Hungaroring (Hungary)",
            drawableRes = R.drawable.track_hungaroring,
            bounds = TrackBounds(-600f, 600f, -600f, 600f),
            rotationDegrees = 0f
        ),
        7 to TrackInfo(
            name = "Circuit de Catalunya (Spain)",
            drawableRes = R.drawable.track_catalunya,
            bounds = TrackBounds(-800f, 800f, -600f, 800f),
            rotationDegrees = 0f
        ),
        8 to TrackInfo(
            name = "Autodromo Nazionale Monza (Italy)",
            drawableRes = R.drawable.track_monza,
            bounds = TrackBounds(-600f, 600f, -1000f, 400f),
            rotationDegrees = 0f
        ),
        9 to TrackInfo(
            name = "Marina Bay Street Circuit (Singapore)",
            drawableRes = R.drawable.track_singapore,
            bounds = TrackBounds(-800f, 800f, -800f, 800f),
            rotationDegrees = 90f
        ),
        10 to TrackInfo(
            name = "Suzuka International Racing Course (Japan)",
            drawableRes = R.drawable.track_suzuka,
            bounds = TrackBounds(-800f, 800f, -600f, 1000f),
            rotationDegrees = 45f
        ),
        11 to TrackInfo(
            name = "Circuit of the Americas (USA)",
            drawableRes = R.drawable.track_cota,
            bounds = TrackBounds(-800f, 800f, -1000f, 600f),
            rotationDegrees = 0f
        ),
        12 to TrackInfo(
            name = "Autódromo José Carlos Pace (Interlagos - Brazil)",
            drawableRes = R.drawable.track_interlagos,
            bounds = TrackBounds(-600f, 600f, -800f, 600f),
            rotationDegrees = 180f
        ),
        13 to TrackInfo(
            name = "Red Bull Ring (Austria)",
            drawableRes = R.drawable.track_red_bull_ring,
            bounds = TrackBounds(-600f, 600f, -1000f, 400f),
            rotationDegrees = 0f
        ),
        14 to TrackInfo(
            name = "Sochi Autodrom (Russia)",
            drawableRes = R.drawable.track_sochi,
            bounds = TrackBounds(-800f, 800f, -600f, 1200f),
            rotationDegrees = 270f
        ),
        15 to TrackInfo(
            name = "Circuit Gilles Villeneuve (Canada)",
            drawableRes = R.drawable.track_montreal,
            bounds = TrackBounds(-600f, 1000f, -400f, 800f),
            rotationDegrees = 0f
        ),
        16 to TrackInfo(
            name = "Bahrain International Circuit",
            drawableRes = R.drawable.track_bahrain,
            bounds = TrackBounds(-800f, 800f, -1000f, 600f),
            rotationDegrees = 0f
        ),
        17 to TrackInfo(
            name = "Nürburgring (Germany)",
            drawableRes = R.drawable.track_nurburgring,
            bounds = TrackBounds(-800f, 800f, -800f, 800f),
            rotationDegrees = 0f
        ),
        18 to TrackInfo(
            name = "Autódromo Hermanos Rodríguez (Mexico)",
            drawableRes = R.drawable.track_mexico,
            bounds = TrackBounds(-800f, 800f, -600f, 1000f),
            rotationDegrees = 90f
        ),
        19 to TrackInfo(
            name = "Yas Marina Circuit (Abu Dhabi)",
            drawableRes = R.drawable.track_yas_marina,
            bounds = TrackBounds(-800f, 800f, -800f, 800f),
            rotationDegrees = 180f
        ),
        20 to TrackInfo(
            name = "Circuit Zandvoort (Netherlands)",
            drawableRes = R.drawable.track_zandvoort,
            bounds = TrackBounds(-600f, 600f, -800f, 600f),
            rotationDegrees = 0f
        ),
        21 to TrackInfo(
            name = "Autodromo Internazionale Enzo e Dino Ferrari (Imola - Italy)",
            drawableRes = R.drawable.track_imola,
            bounds = TrackBounds(-800f, 800f, -600f, 800f),
            rotationDegrees = 45f
        ),
        22 to TrackInfo(
            name = "Autódromo Internacional do Algarve (Portimão - Portugal)",
            drawableRes = R.drawable.track_portimao,
            bounds = TrackBounds(-600f, 800f, -600f, 800f),
            rotationDegrees = 0f
        ),
        23 to TrackInfo(
            name = "Jeddah Corniche Circuit (Saudi Arabia)",
            drawableRes = R.drawable.track_jeddah,
            bounds = TrackBounds(-800f, 1200f, -400f, 1000f),
            rotationDegrees = 0f
        ),
        24 to TrackInfo(
            name = "Miami International Autodrome (USA)",
            drawableRes = R.drawable.track_miami,
            bounds = TrackBounds(-800f, 800f, -800f, 800f),
            rotationDegrees = 90f
        ),
        25 to TrackInfo(
            name = "Las Vegas Street Circuit (USA)",
            drawableRes = R.drawable.track_las_vegas,
            bounds = TrackBounds(-1000f, 1000f, -600f, 1200f),
            rotationDegrees = 0f
        ),
        26 to TrackInfo(
            name = "Losail International Circuit (Qatar)",
            drawableRes = R.drawable.track_qatar,
            bounds = TrackBounds(-800f, 800f, -1000f, 600f),
            rotationDegrees = 0f
        )
    )
    
    // Default/placeholder track info
    private val defaultTrack = TrackInfo(
        name = "Unknown Circuit",
        drawableRes = R.drawable.track_placeholder,
        bounds = TrackBounds(-800f, 800f, -800f, 800f),
        rotationDegrees = 0f
    )
    
    /**
     * Get track information by trackId from session data
     */
    fun getTrackInfo(trackId: Int): TrackInfo {
        return trackDatabase[trackId] ?: defaultTrack.copy(
            name = "Circuit #$trackId"
        )
    }
    
    /**
     * Get all available tracks (for debugging/testing)
     */
    fun getAllTracks(): Map<Int, TrackInfo> = trackDatabase
    
    /**
     * Transform world coordinates (X, Z) to view coordinates (x, y) on canvas
     */
    fun worldToView(
        worldX: Float,
        worldZ: Float,
        trackInfo: TrackInfo,
        canvasWidth: Int,
        canvasHeight: Int,
        padding: Float = 50f
    ): Pair<Float, Float> {
        val bounds = trackInfo.bounds
        
        // Normalize to 0-1 range
        val normalizedX = (worldX - bounds.minX) / (bounds.maxX - bounds.minX)
        val normalizedZ = (worldZ - bounds.minZ) / (bounds.maxZ - bounds.minZ)
        
        // Apply padding and scale to canvas size
        val availableWidth = canvasWidth - (2 * padding)
        val availableHeight = canvasHeight - (2 * padding)
        
        var viewX = padding + (normalizedX * availableWidth)
        var viewY = padding + (normalizedZ * availableHeight)
        
        // Apply rotation if needed (around center)
        if (trackInfo.rotationDegrees != 0f) {
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            
            val radians = Math.toRadians(trackInfo.rotationDegrees.toDouble())
            val cos = Math.cos(radians).toFloat()
            val sin = Math.sin(radians).toFloat()
            
            val translatedX = viewX - centerX
            val translatedY = viewY - centerY
            
            viewX = centerX + (translatedX * cos - translatedY * sin)
            viewY = centerY + (translatedX * sin + translatedY * cos)
        }
        
        return Pair(viewX, viewY)
    }
}