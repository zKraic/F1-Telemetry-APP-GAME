package telemetry.f1.app.ui.timing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import telemetry.f1.app.data.AppDatabase
import telemetry.f1.app.data.LapTime
import telemetry.f1.app.network.LapData
import telemetry.f1.app.network.TelemetryRepository
import java.util.concurrent.TimeUnit

class TimingViewModel(application: Application) : AndroidViewModel(application) {

    private val telemetryRepository = TelemetryRepository.getInstance()
    private val lapTimeDao = AppDatabase.getDatabase(application).lapTimeDao()

    private val _lapData = MutableStateFlow<LapData?>(null)
    val lapData: StateFlow<LapData?> = _lapData.asStateFlow()

    private val _bestLapTime = MutableStateFlow("--:--.---")
    val bestLapTime: StateFlow<String> = _bestLapTime.asStateFlow()

    private val _sector3Time = MutableStateFlow("--:--.---")
    val sector3Time: StateFlow<String> = _sector3Time.asStateFlow()
    
    val recentLaps: StateFlow<List<LapTime>> = lapTimeDao.getRecentLaps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var lastLapTime = 0L
    private var currentTrackId = -1

    init {
        // Get the trackId as soon as the session starts
        viewModelScope.launch {
            telemetryRepository.session.collect { sessionPacket ->
                val newTrackId = sessionPacket.m_trackId
                if (newTrackId != currentTrackId) {
                    currentTrackId = newTrackId
                    // When track changes, update the best lap time
                    launch {
                        lapTimeDao.getBestLapByTrack(currentTrackId).collect { bestLap ->
                            _bestLapTime.value = bestLap?.let { formatTime(it.lapTimeInMs) } ?: "--:--.---"
                        }
                    }
                }
            }
        }

        // Process lap data
        viewModelScope.launch {
            telemetryRepository.lapData.collect { packet ->
                val playerIndex = packet.m_header.m_playerCarIndex
                if (playerIndex in packet.m_lapData.indices) {
                    val data = packet.m_lapData[playerIndex]
                    _lapData.value = data

                    // Check if a new lap has been completed
                    if (data.m_lastLapTimeInMS > 0 && data.m_lastLapTimeInMS != lastLapTime) {
                        lastLapTime = data.m_lastLapTimeInMS
                        
                        val s1 = data.m_sector1TimeInMS.toLong()
                        val s2 = data.m_sector2TimeInMS.toLong()
                        if (s1 > 0 && s2 > 0) {
                             _sector3Time.value = formatTime(lastLapTime - s1 - s2)
                        }

                        // Save valid laps
                        if (data.m_currentLapInvalid == 0) {
                            saveLap(data)
                        }
                    }
                }
            }
        }
    }

    private fun saveLap(lapData: LapData) {
        viewModelScope.launch {
            val s1 = lapData.m_sector1TimeInMS.toLong()
            val s2 = lapData.m_sector2TimeInMS.toLong()
            lapTimeDao.insertLapTime(
                LapTime(
                    trackId = currentTrackId,
                    trackName = "Track $currentTrackId", // Placeholder
                    lapTimeInMs = lapData.m_lastLapTimeInMS,
                    sector1TimeMs = s1,
                    sector2TimeMs = s2,
                    sector3TimeMs = lapData.m_lastLapTimeInMS - s1 - s2,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun formatTime(ms: Long): String {
        if (ms <= 0) return "--:--.---"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        val millis = ms % 1000
        return String.format("%02d:%02d.%03d", minutes, seconds, millis)
    }
}
