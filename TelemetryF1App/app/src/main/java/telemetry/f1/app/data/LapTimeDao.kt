package telemetry.f1.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LapTimeDao {
    
    @Query("SELECT * FROM lap_times WHERE trackId = :trackId ORDER BY timestamp DESC")
    fun getLapsByTrack(trackId: Int): Flow<List<LapTime>>
    
    @Query("SELECT * FROM lap_times ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLaps(): Flow<List<LapTime>>
    
    @Query("SELECT * FROM lap_times WHERE trackId = :trackId AND isValidLap = 1 ORDER BY lapTimeInMs ASC LIMIT 1")
    fun getBestLapByTrack(trackId: Int): Flow<LapTime?>
    
    @Query("SELECT MIN(lapTimeInMs) FROM lap_times WHERE trackId = :trackId AND isValidLap = 1")
    suspend fun getBestTimeByTrack(trackId: Int): Long?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLapTime(lapTime: LapTime): Long
    
    @Delete
    suspend fun deleteLapTime(lapTime: LapTime)
    
    @Query("DELETE FROM lap_times WHERE trackId = :trackId")
    suspend fun deleteAllLapsForTrack(trackId: Int)
    
    @Query("SELECT COUNT(*) FROM lap_times WHERE trackId = :trackId")
    suspend fun getLapCountByTrack(trackId: Int): Int
}