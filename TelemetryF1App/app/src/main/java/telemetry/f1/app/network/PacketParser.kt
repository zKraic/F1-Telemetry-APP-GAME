package telemetry.f1.app.network

import java.nio.ByteBuffer
import java.nio.ByteOrder

class PacketParser {

    fun parse(buffer: ByteBuffer): Any? {
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        if (buffer.remaining() < PacketHeader.SIZE_BYTES) return null

        val header = parseHeader(buffer)

        // Soporte para F1 2024 y F1 2025
        if (header.m_packetFormat != 2024 && header.m_packetFormat != 2025) return null

        return try {
            when (header.m_packetId) {
                PacketTypes.MOTION -> parseMotionPacket(header, buffer)
                PacketTypes.SESSION -> parseSessionPacket(header, buffer)
                PacketTypes.LAP_DATA -> parseLapDataPacket(header, buffer)
                PacketTypes.CAR_TELEMETRY -> parseCarTelemetryPacket(header, buffer)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseHeader(buffer: ByteBuffer) = PacketHeader(
        m_packetFormat = buffer.short.toInt(),
        m_gameYear = buffer.get().toInt(),
        m_gameMajorVersion = buffer.get().toInt(),
        m_gameMinorVersion = buffer.get().toInt(),
        m_packetVersion = buffer.get().toInt(),
        m_packetId = buffer.get().toInt(),
        m_sessionUID = buffer.long,
        m_sessionTime = buffer.float,
        m_frameIdentifier = buffer.int.toLong(),
        m_overallFrameIdentifier = buffer.int.toLong(),
        m_playerCarIndex = buffer.get().toInt(),
        m_secondaryPlayerCarIndex = buffer.get().toInt()
    )

    private fun parseMotionPacket(header: PacketHeader, buffer: ByteBuffer): PacketMotionData? {
        return try {
            val carMotionData = mutableListOf<CarMotionData>()
            for (i in 0 until 22) {
                if (buffer.remaining() < MIN_CAR_MOTION_SIZE) break
                carMotionData.add(parseCarMotionData(buffer))
            }
            PacketMotionData(header, carMotionData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseCarMotionData(buffer: ByteBuffer) = CarMotionData(
        m_worldPositionX = buffer.float,
        m_worldPositionY = buffer.float,
        m_worldPositionZ = buffer.float,
        m_worldVelocityX = buffer.float,
        m_worldVelocityY = buffer.float,
        m_worldVelocityZ = buffer.float,
        m_worldForwardDirX = buffer.short,
        m_worldForwardDirY = buffer.short,
        m_worldForwardDirZ = buffer.short,
        m_worldRightDirX = buffer.short,
        m_worldRightDirY = buffer.short,
        m_worldRightDirZ = buffer.short,
        m_gForceLateral = buffer.float,
        m_gForceLongitudinal = buffer.float,
        m_gForceVertical = buffer.float,
        m_yaw = buffer.float,
        m_pitch = buffer.float,
        m_roll = buffer.float
    )

    private fun parseSessionPacket(header: PacketHeader, buffer: ByteBuffer): PacketSessionData? {
        return try {
            PacketSessionData(
                header,
                m_weather = buffer.get().toInt(),
                m_trackTemperature = buffer.get().toInt(),
                m_airTemperature = buffer.get().toInt(),
                m_totalLaps = buffer.get().toInt(),
                m_trackLength = buffer.short.toInt(),
                m_sessionType = buffer.get().toInt(),
                m_trackId = buffer.get().toInt(),
                m_formula = buffer.get().toInt()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseLapDataPacket(header: PacketHeader, buffer: ByteBuffer): PacketLapData? {
        return try {
            val lapData = mutableListOf<LapData>()
            for (i in 0 until 22) {
                if (buffer.remaining() < MIN_LAP_DATA_SIZE) break
                lapData.add(parseLapData(buffer))
            }
            PacketLapData(header, lapData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseLapData(buffer: ByteBuffer): LapData {
        // Leer datos básicos de vuelta
        val lastLapTime = buffer.int.toLong()
        val currentLapTime = buffer.int.toLong()
        
        // Para F1 2025, los tiempos de sector pueden tener formato diferente
        // Intentamos leer como enteros de 32 bits (ms directos)
        val sector1Time = try {
            buffer.int
        } catch (e: Exception) {
            buffer.short.toInt()
        }
        
        val sector2Time = try {
            buffer.int
        } catch (e: Exception) {
            buffer.short.toInt()
        }
        
        return LapData(
            m_lastLapTimeInMS = lastLapTime,
            m_currentLapTimeInMS = currentLapTime,
            m_sector1TimeInMS = sector1Time,
            m_sector2TimeInMS = sector2Time,
            m_lapDistance = buffer.float,
            m_totalDistance = buffer.float,
            m_safetyCarDelta = buffer.float,
            m_carPosition = buffer.get().toInt(),
            m_currentLapNum = buffer.get().toInt(),
            m_pitStatus = buffer.get().toInt(),
            m_numPitStops = buffer.get().toInt(),
            m_sector = buffer.get().toInt(),
            m_currentLapInvalid = buffer.get().toInt(),
            m_penalties = buffer.get().toInt(),
            m_warnings = buffer.get().toInt(),
            m_driverStatus = buffer.get().toInt(),
            m_resultStatus = buffer.get().toInt()
        )
    }

    private fun parseCarTelemetryPacket(header: PacketHeader, buffer: ByteBuffer): PacketCarTelemetryData? {
        return try {
            val carTelemetryData = mutableListOf<CarTelemetryData>()
            for (i in 0 until 22) {
                if (buffer.remaining() < MIN_CAR_TELEMETRY_SIZE) break
                carTelemetryData.add(parseCarTelemetryData(buffer))
            }
            PacketCarTelemetryData(header, carTelemetryData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseCarTelemetryData(buffer: ByteBuffer): CarTelemetryData {
        val brakesTemp = mutableListOf<Int>()
        val tyresSurface = mutableListOf<Int>()
        val tyresInner = mutableListOf<Int>()
        val tyresPressure = mutableListOf<Float>()
        val surfaceType = mutableListOf<Int>()
        
        // Leer arrays de forma defensiva
        repeat(4) {
            if (buffer.remaining() >= 2) brakesTemp.add(buffer.short.toInt())
        }
        repeat(4) {
            if (buffer.remaining() >= 1) tyresSurface.add(buffer.get().toInt())
        }
        repeat(4) {
            if (buffer.remaining() >= 1) tyresInner.add(buffer.get().toInt())
        }
        repeat(4) {
            if (buffer.remaining() >= 4) tyresPressure.add(buffer.float)
        }
        repeat(4) {
            if (buffer.remaining() >= 1) surfaceType.add(buffer.get().toInt())
        }
        
        return CarTelemetryData(
            m_speed = if (buffer.remaining() >= 2) buffer.short.toInt() else 0,
            m_throttle = if (buffer.remaining() >= 4) buffer.float else 0f,
            m_steer = if (buffer.remaining() >= 4) buffer.float else 0f,
            m_brake = if (buffer.remaining() >= 4) buffer.float else 0f,
            m_clutch = if (buffer.remaining() >= 1) buffer.get().toInt() else 0,
            m_gear = if (buffer.remaining() >= 1) buffer.get().toInt() else 0,
            m_engineRPM = if (buffer.remaining() >= 2) buffer.short.toInt() else 0,
            m_drs = if (buffer.remaining() >= 1) buffer.get().toInt() else 0,
            m_revLightsPercent = if (buffer.remaining() >= 1) buffer.get().toInt() else 0,
            m_revLightsBitValue = if (buffer.remaining() >= 2) buffer.short.toInt() else 0,
            m_brakesTemperature = brakesTemp,
            m_tyresSurfaceTemperature = tyresSurface,
            m_tyresInnerTemperature = tyresInner,
            m_engineTemperature = if (buffer.remaining() >= 2) buffer.short.toInt() else 0,
            m_tyresPressure = tyresPressure,
            m_surfaceType = surfaceType
        )
    }
    
    companion object {
        private const val MIN_CAR_MOTION_SIZE = 60 // bytes aproximados
        private const val MIN_LAP_DATA_SIZE = 53   // bytes aproximados  
        private const val MIN_CAR_TELEMETRY_SIZE = 60 // bytes aproximados
    }
}