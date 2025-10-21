package telemetry.f1.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import telemetry.f1.app.network.TelemetryRepository
import kotlin.math.max
import kotlin.math.min

data class CarPosition(
    val carIndex: Int,
    val x: Float,
    val y: Float,
    val isPlayer: Boolean = false,
    val speed: Int = 0,
    val gear: Int = 0
)

class MapViewModel : ViewModel() {

    private val telemetryRepository = TelemetryRepository.getInstance()

    private val _trackInfo = MutableStateFlow(TrackAssets.getTrackInfo(-1))
    val trackInfo: StateFlow<TrackInfo> = _trackInfo.asStateFlow()

    private val _carPositions = MutableStateFlow<List<CarPosition>>(emptyList())
    val carPositions: StateFlow<List<CarPosition>> = _carPositions.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()
    
    private val _debugInfo = MutableStateFlow("Waiting for telemetry...")
    val debugInfo: StateFlow<String> = _debugInfo.asStateFlow()

    private var currentTrackId = -1
    private var lastPositionUpdate = 0L
    private var canvasWidth = 0
    private var canvasHeight = 0
    
    // Fallback simulation data
    private var simulationAngle = 0f
    private val simulationRadius = 200f

    init {
        observeSessionData()
        observeMotionData()
        observeTelemetryDataForFallback()
    }

    fun setCanvasSize(width: Int, height: Int) {
        canvasWidth = width
        canvasHeight = height
    }

    private fun observeSessionData() {
        viewModelScope.launch {
            telemetryRepository.session
                .distinctUntilChanged { old, new -> old.m_trackId == new.m_trackId }
                .collect { sessionData ->
                    val trackId = sessionData.m_trackId
                    if (trackId != currentTrackId) {
                        currentTrackId = trackId
                        val newTrackInfo = TrackAssets.getTrackInfo(trackId)
                        _trackInfo.value = newTrackInfo
                        _debugInfo.value = "Track loaded: ${newTrackInfo.name} (ID: $trackId)"
                        println("F1 Map: Switched to track $trackId - ${newTrackInfo.name}")
                    }
                    _connectionStatus.value = true
                }
        }
    }

    private fun observeMotionData() {
        viewModelScope.launch {
            telemetryRepository.motionData
                // Throttle to max 60 Hz (16.67ms)
                .sample(17)
                .collect { motionPacket ->
                    processMotionData(motionPacket)
                    _connectionStatus.value = true
                    
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastPositionUpdate > 1000) {
                        val fps = 1000.0 / max(currentTime - lastPositionUpdate, 1)
                        _debugInfo.value = "Motion data: ${String.format("%.1f", fps)} FPS, ${motionPacket.m_carMotionData.size} cars"
                        lastPositionUpdate = currentTime
                    }
                }
        }
    }

    private fun observeTelemetryDataForFallback() {
        viewModelScope.launch {
            telemetryRepository.carTelemetry
                .sample(50) // Slower fallback simulation
                .collect { telemetryPacket ->
                    // Only use as fallback if no motion data is coming
                    if (System.currentTimeMillis() - lastPositionUpdate > 2000) {
                        processSimulatedPositions(telemetryPacket)
                        _debugInfo.value = "Fallback mode: using simulated positions"
                    }
                }
        }
    }

    private fun processMotionData(motionPacket: telemetry.f1.app.network.PacketMotionData) {
        if (canvasWidth <= 0 || canvasHeight <= 0) return
        
        val currentTrackInfo = _trackInfo.value
        val playerCarIndex = motionPacket.m_header.m_playerCarIndex
        val positions = mutableListOf<CarPosition>()

        motionPacket.m_carMotionData.forEachIndexed { index, carMotion ->
            try {
                // Use X and Z coordinates (Y is elevation, not used for 2D map)
                val worldX = carMotion.m_worldPositionX
                val worldZ = carMotion.m_worldPositionZ
                
                // Transform world coordinates to view coordinates
                val (viewX, viewY) = TrackAssets.worldToView(
                    worldX, worldZ, 
                    currentTrackInfo, 
                    canvasWidth, canvasHeight
                )
                
                // Only add cars that are within reasonable bounds (active cars)
                if (viewX > -50 && viewX < canvasWidth + 50 && 
                    viewY > -50 && viewY < canvasHeight + 50) {
                    
                    positions.add(
                        CarPosition(
                            carIndex = index,
                            x = viewX,
                            y = viewY,
                            isPlayer = index == playerCarIndex,
                            speed = 0, // We can get this from telemetry if needed
                            gear = 0   // We can get this from telemetry if needed
                        )
                    )
                }
            } catch (e: Exception) {
                // Skip this car if transformation fails
                println("F1 Map: Failed to transform position for car $index: ${e.message}")
            }
        }
        
        _carPositions.value = positions
    }

    private fun processSimulatedPositions(telemetryPacket: telemetry.f1.app.network.PacketCarTelemetryData) {
        if (canvasWidth <= 0 || canvasHeight <= 0) return
        
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f
        val playerCarIndex = telemetryPacket.m_header.m_playerCarIndex
        val positions = mutableListOf<CarPosition>()
        
        simulationAngle += 2f // Slow rotation for simulation
        if (simulationAngle >= 360f) simulationAngle = 0f

        telemetryPacket.m_carTelemetryData.forEachIndexed { index, carData ->
            if (carData.m_speed > 0) { // Only show moving cars
                val angle = simulationAngle + (index * 16f) // Spread cars around track
                val radius = simulationRadius + (carData.m_speed * 0.5f).coerceIn(0f, 100f)
                
                val x = centerX + (radius * Math.cos(Math.toRadians(angle.toDouble()))).toFloat()
                val y = centerY + (radius * Math.sin(Math.toRadians(angle.toDouble()))).toFloat()
                
                positions.add(
                    CarPosition(
                        carIndex = index,
                        x = x,
                        y = y,
                        isPlayer = index == playerCarIndex,
                        speed = carData.m_speed,
                        gear = carData.m_gear
                    )
                )
            }
        }
        
        _carPositions.value = positions
    }
    
    fun refreshConnection() {
        _connectionStatus.value = telemetryRepository.isConnected()
        _debugInfo.value = telemetryRepository.getConnectionInfo()
    }
}