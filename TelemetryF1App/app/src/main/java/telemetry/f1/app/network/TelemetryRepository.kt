package telemetry.f1.app.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer

class TelemetryRepository {

    private var udpSocket: DatagramSocket? = null
    private var isListening = false
    private val parser = PacketParser()

    // SharedFlows with replay=1 for latest state, optimized for 60 Hz
    private val _session = MutableSharedFlow<PacketSessionData>(replay = 1, extraBufferCapacity = 10)
    val session: SharedFlow<PacketSessionData> = _session.asSharedFlow()

    private val _motionData = MutableSharedFlow<PacketMotionData>(replay = 1, extraBufferCapacity = 10)
    val motionData: SharedFlow<PacketMotionData> = _motionData.asSharedFlow()

    private val _lapData = MutableSharedFlow<PacketLapData>(replay = 1, extraBufferCapacity = 10)
    val lapData: SharedFlow<PacketLapData> = _lapData.asSharedFlow()

    private val _carTelemetry = MutableSharedFlow<PacketCarTelemetryData>(replay = 1, extraBufferCapacity = 10)
    val carTelemetry: SharedFlow<PacketCarTelemetryData> = _carTelemetry.asSharedFlow()

    private var listeningJob: Job? = null
    private var packetsReceived = 0L
    private var lastStatsTime = System.currentTimeMillis()

    fun startListening(port: Int = 20777) {
        if (isListening) return

        listeningJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                udpSocket = DatagramSocket(port)
                udpSocket?.receiveBufferSize = 65536 // Larger receive buffer for 60 Hz
                isListening = true

                // Increased buffer size for F1 2025 and 60 Hz rate
                val buffer = ByteArray(2048)
                println("F1 Telemetry: Started listening on port $port")

                while (isListening) {
                    try {
                        val datagramPacket = DatagramPacket(buffer, buffer.size)
                        udpSocket?.receive(datagramPacket)
                        
                        packetsReceived++
                        
                        // Log stats every 5 seconds for debugging
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastStatsTime > 5000) {
                            val packetsPerSecond = packetsReceived / ((currentTime - lastStatsTime) / 1000.0)
                            println("F1 Telemetry: Receiving ${String.format("%.1f", packetsPerSecond)} packets/sec")
                            packetsReceived = 0
                            lastStatsTime = currentTime
                        }

                        val byteBuffer = ByteBuffer.wrap(datagramPacket.data, 0, datagramPacket.length)
                        when (val parsedPacket = parser.parse(byteBuffer)) {
                            is PacketSessionData -> _session.tryEmit(parsedPacket)
                            is PacketMotionData -> _motionData.tryEmit(parsedPacket)
                            is PacketLapData -> _lapData.tryEmit(parsedPacket)
                            is PacketCarTelemetryData -> _carTelemetry.tryEmit(parsedPacket)
                            null -> {
                                // Packet parsing failed, but don't spam logs
                                if (packetsReceived % 100 == 0L) {
                                    println("F1 Telemetry: Failed to parse packet of size ${datagramPacket.length}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        if (isListening) {
                            println("F1 Telemetry: UDP receive error: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("F1 Telemetry: Failed to start UDP listener: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun stopListening() {
        println("F1 Telemetry: Stopping UDP listener")
        isListening = false
        listeningJob?.cancel()
        udpSocket?.close()
        udpSocket = null
    }
    
    fun isConnected(): Boolean = isListening && udpSocket?.isClosed == false
    
    fun getConnectionInfo(): String {
        return if (isConnected()) {
            "Connected on port ${udpSocket?.localPort ?: "unknown"}"
        } else {
            "Disconnected"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TelemetryRepository? = null

        fun getInstance(): TelemetryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TelemetryRepository().also { INSTANCE = it }
            }
        }
    }
}