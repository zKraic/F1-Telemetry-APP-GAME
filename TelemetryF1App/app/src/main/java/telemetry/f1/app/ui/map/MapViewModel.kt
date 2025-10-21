package telemetry.f1.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import telemetry.f1.app.network.TelemetryRepository

data class CarPosition(
    val x: Float,
    val y: Float,
    val isPlayer: Boolean
)

class MapViewModel : ViewModel() {

    private val telemetryRepository = TelemetryRepository.getInstance()

    private val _carPositions = MutableStateFlow<List<CarPosition>>(emptyList())
    val carPositions: StateFlow<List<CarPosition>> = _carPositions.asStateFlow()

    private val _trackId = MutableStateFlow(-1)
    val trackId: StateFlow<Int> = _trackId.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    init {
        viewModelScope.launch {
            telemetryRepository.session.conflate().collect { 
                _trackId.value = it.m_trackId
                _connectionStatus.value = true
            }
        }

        viewModelScope.launch {
            telemetryRepository.motionData.conflate().collect { packet ->
                _connectionStatus.value = true
                val playerIndex = packet.m_header.m_playerCarIndex
                _carPositions.value = packet.m_carMotionData.mapIndexed { i, car ->
                    CarPosition(car.m_worldPositionX, car.m_worldPositionZ, i == playerIndex)
                }
            }
        }
    }
}
