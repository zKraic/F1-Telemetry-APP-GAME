package telemetry.f1.app.ui.map

import android.graphics.Path

object TrackData {
    fun getTrackPath(trackId: Int): Path? {
        return when (trackId) {
            // Silverstone
            2 -> Path().apply {
                moveTo(-300f, 200f)
                lineTo(200f, 200f)
                lineTo(400f, 0f)
                lineTo(200f, -200f)
                lineTo(-300f, -200f)
                close()
            }
            // Spa-Francorchamps
            3 -> Path().apply {
                moveTo(0f, 400f)
                lineTo(200f, 200f)
                lineTo(0f, 0f)
                lineTo(-200f, -200f)
                lineTo(0f, -400f)
                close()
            }
            // Monza
            8 -> Path().apply {
                moveTo(0f, 300f)
                lineTo(400f, 300f)
                lineTo(400f, -300f)
                lineTo(0f, -300f)
                close()
            }
            // Add other tracks here as simple placeholders
            else -> null // No path for unknown tracks
        }
    }
}