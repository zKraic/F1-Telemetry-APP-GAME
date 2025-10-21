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

    private val _session = MutableSharedFlow<PacketSessionData>(replay = 1)
    val session: SharedFlow<PacketSessionData> = _session.asSharedFlow()

    private val _motionData = MutableSharedFlow<PacketMotionData>(replay = 1)
    val motionData: SharedFlow<PacketMotionData> = _motionData.asSharedFlow()

    private val _lapData = MutableSharedFlow<PacketLapData>(replay = 1)
    val lapData: SharedFlow<PacketLapData> = _lapData.asSharedFlow()

    private val _carTelemetry = MutableSharedFlow<PacketCarTelemetryData>(replay = 1)
    val carTelemetry: SharedFlow<PacketCarTelemetryData> = _carTelemetry.asSharedFlow()

    private var listeningJob: Job? = null

    fun startListening(port: Int = 20777) {
        if (isListening) return

        listeningJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                udpSocket = DatagramSocket(port)
                isListening = true

                val buffer = ByteArray(1460) // Standard UDP packet size for F1

                while (isListening) {
                    try {
                        val datagramPacket = DatagramPacket(buffer, buffer.size)
                        udpSocket?.receive(datagramPacket)

                        val byteBuffer = ByteBuffer.wrap(datagramPacket.data, 0, datagramPacket.length)
                        when (val parsedPacket = parser.parse(byteBuffer)) {
                            is PacketSessionData -> _session.tryEmit(parsedPacket)
                            is PacketMotionData -> _motionData.tryEmit(parsedPacket)
                            is PacketLapData -> _lapData.tryEmit(parsedPacket)
                            is PacketCarTelemetryData -> _carTelemetry.tryEmit(parsedPacket)
                        }
                    } catch (e: Exception) {
                        if (isListening) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopListening() {
        isListening = false
        listeningJob?.cancel()
        udpSocket?.close()
        udpSocket = null
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