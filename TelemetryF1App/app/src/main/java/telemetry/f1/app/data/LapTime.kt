package telemetry.f1.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lap_times")
data class LapTime(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackName: String,
    val trackId: Int,
    val lapTimeInMs: Long,
    val sector1TimeMs: Long,
    val sector2TimeMs: Long,
    val sector3TimeMs: Long,
    val timestamp: Long,
    val isValidLap: Boolean = true,
    val weather: Int = 0,
    val sessionType: Int = 0
)