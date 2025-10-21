package telemetry.f1.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import telemetry.f1.app.network.CarTelemetryData
import telemetry.f1.app.network.TelemetryRepository

class DashboardViewModel : ViewModel() {

    private val telemetryRepository = TelemetryRepository.getInstance()

    private val _telemetryData = MutableStateFlow<CarTelemetryData?>(null)
    val telemetryData: StateFlow<CarTelemetryData?> = _telemetryData.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    init {
        viewModelScope.launch {
            // Use .conflate() to only process the latest packet, preventing UI lag
            telemetryRepository.carTelemetry.conflate().collect { packet ->
                _connectionStatus.value = true
                val playerIndex = packet.m_header.m_playerCarIndex
                if (playerIndex in packet.m_carTelemetryData.indices) {
                    _telemetryData.value = packet.m_carTelemetryData[playerIndex]
                }
            }
        }
    }
}