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
    
    private val _currentLapTime = MutableStateFlow("00:00.000")
    val currentLapTime: StateFlow<String> = _currentLapTime.asStateFlow()
    
    private val _sector1Time = MutableStateFlow("--:--.---")
    val sector1Time: StateFlow<String> = _sector1Time.asStateFlow()
    
    private val _sector2Time = MutableStateFlow("--:--.---") 
    val sector2Time: StateFlow<String> = _sector2Time.asStateFlow()
    
    private val _position = MutableStateFlow(0)
    val position: StateFlow<Int> = _position.asStateFlow()
    
    private val _lapNumber = MutableStateFlow(0)
    val lapNumber: StateFlow<Int> = _lapNumber.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()
    
    val recentLaps: StateFlow<List<LapTime>> = lapTimeDao.getRecentLaps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var lastLapTime = 0L
    private var currentTrackId = -1

    init {
        // Track changes and best lap - usando flatMapLatest para evitar colectas múltiples
        viewModelScope.launch {
            telemetryRepository.session
                .map { it.m_trackId }
                .distinctUntilChanged()
                .onEach { 
                    currentTrackId = it
                    _connectionStatus.value = true
                }
                .flatMapLatest { trackId ->
                    if (trackId >= 0) {
                        lapTimeDao.getBestLapByTrack(trackId)
                    } else {
                        flowOf(null)
                    }
                }
                .collect { bestLap ->
                    _bestLapTime.value = bestLap?.let { formatTime(it.lapTimeInMs) } ?: "--:--.---"
                }
        }

        // Lap data processing - con validaciones defensivas
        viewModelScope.launch {
            telemetryRepository.lapData.collect { packet ->
                val playerIndex = packet.m_header.m_playerCarIndex
                
                // Validación defensiva del índice
                if (playerIndex !in packet.m_lapData.indices || playerIndex < 0) {
                    return@collect
                }

                val data = packet.m_lapData[playerIndex]
                _lapData.value = data
                _connectionStatus.value = true

                // Actualizar tiempos actuales
                updateCurrentTimingData(data)

                // Verificar vuelta completada
                val currentLapTimeMs = data.m_lastLapTimeInMS
                if (currentLapTimeMs > 0 && currentLapTimeMs != lastLapTime) {
                    lastLapTime = currentLapTimeMs
                    procesCompletedLap(data)
                }
            }
        }
    }

    private fun updateCurrentTimingData(data: LapData) {
        try {
            // Tiempo de vuelta actual
            val currentLap = data.m_currentLapTimeInMS
            if (currentLap > 0) {
                _currentLapTime.value = formatTime(currentLap.toLong())
            }

            // Tiempos de sectores (usando propiedades que realmente existen)
            val s1 = data.m_sector1TimeInMS.toLong()
            val s2 = data.m_sector2TimeInMS.toLong()

            if (s1 > 0) {
                _sector1Time.value = formatTime(s1)
            }
            
            if (s2 > 0) {
                _sector2Time.value = formatTime(s2)
            }

            // Posición y número de vuelta
            _position.value = data.m_carPosition
            _lapNumber.value = data.m_currentLapNum
            
        } catch (e: Exception) {
            // Log del error pero no crash
            e.printStackTrace()
        }
    }

    private fun procesCompletedLap(data: LapData) {
        try {
            val s1 = data.m_sector1TimeInMS.toLong()
            val s2 = data.m_sector2TimeInMS.toLong()
            val totalLap = data.m_lastLapTimeInMS

            // Calcular sector 3 solo si tenemos datos válidos
            if (s1 > 0 && s2 > 0 && totalLap > s1 + s2) {
                val s3 = totalLap - s1 - s2
                _sector3Time.value = formatTime(s3)
            }

            // Guardar vuelta válida
            if (data.m_currentLapInvalid == 0 && currentTrackId >= 0) {
                saveLap(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveLap(lapData: LapData) {
        viewModelScope.launch {
            try {
                val s1 = lapData.m_sector1TimeInMS.toLong()
                val s2 = lapData.m_sector2TimeInMS.toLong()
                val totalTime = lapData.m_lastLapTimeInMS
                val s3 = if (s1 > 0 && s2 > 0 && totalTime > s1 + s2) {
                    totalTime - s1 - s2
                } else {
                    0L
                }

                val lapTime = LapTime(
                    trackId = currentTrackId,
                    trackName = getTrackName(currentTrackId),
                    lapTimeInMs = totalTime,
                    sector1TimeMs = s1,
                    sector2TimeMs = s2,
                    sector3TimeMs = s3,
                    timestamp = System.currentTimeMillis(),
                    isValidLap = lapData.m_currentLapInvalid == 0
                )

                lapTimeDao.insertLapTime(lapTime)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getTrackName(trackId: Int): String {
        return when (trackId) {
            0 -> "Albert Park (Melbourne)"
            1 -> "Circuit Paul Ricard (France)"
            2 -> "Silverstone (Britain)"
            3 -> "Spa-Francorchamps (Belgium)"
            4 -> "Monaco Circuit"
            5 -> "Baku City Circuit (Azerbaijan)"
            6 -> "Hungaroring (Hungary)"
            7 -> "Circuit de Catalunya (Spain)"
            8 -> "Autodromo Nazionale Monza (Italy)"
            9 -> "Marina Bay (Singapore)"
            10 -> "Suzuka (Japan)"
            11 -> "Circuit of the Americas (USA)"
            12 -> "Interlagos (Brazil)"
            13 -> "Red Bull Ring (Austria)"
            14 -> "Sochi Autodrom (Russia)"
            15 -> "Circuit Gilles Villeneuve (Canada)"
            16 -> "Bahrain International Circuit"
            17 -> "Nürburgring (Germany)"
            18 -> "Hermanos Rodríguez (Mexico)"
            19 -> "Yas Marina (Abu Dhabi)"
            20 -> "Circuit Zandvoort (Netherlands)"
            21 -> "Imola (Italy)"
            22 -> "Portimão (Portugal)"
            23 -> "Jeddah Corniche Circuit (Saudi Arabia)"
            24 -> "Miami International Autodrome (USA)"
            25 -> "Las Vegas Street Circuit (USA)"
            26 -> "Losail International Circuit (Qatar)"
            else -> "Circuit #$trackId"
        }
    }

    fun formatTime(ms: Long): String {
        if (ms <= 0) return "--:--.---"
        
        return try {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
            val millis = ms % 1000
            String.format("%02d:%02d.%03d", minutes, seconds, millis)
        } catch (e: Exception) {
            "--:--.---"
        }
    }
}