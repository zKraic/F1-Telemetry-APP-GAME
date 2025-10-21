package telemetry.f1.app.network

import java.nio.ByteBuffer
import java.nio.ByteOrder

class PacketParser {

    fun parse(buffer: ByteBuffer): Any? {
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        if (buffer.remaining() < PacketHeader.SIZE_BYTES) return null

        val header = parseHeader(buffer)

        if (header.m_packetFormat != 2024) return null

        return when (header.m_packetId) {
            PacketTypes.MOTION -> parseMotionPacket(header, buffer)
            PacketTypes.SESSION -> parseSessionPacket(header, buffer)
            PacketTypes.LAP_DATA -> parseLapDataPacket(header, buffer)
            PacketTypes.CAR_TELEMETRY -> parseCarTelemetryPacket(header, buffer)
            else -> null
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

    private fun parseMotionPacket(header: PacketHeader, buffer: ByteBuffer): PacketMotionData {
        val carMotionData = List(22) { parseCarMotionData(buffer) }
        return PacketMotionData(header, carMotionData)
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

    private fun parseSessionPacket(header: PacketHeader, buffer: ByteBuffer): PacketSessionData {
        return PacketSessionData(
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
    }

    private fun parseLapDataPacket(header: PacketHeader, buffer: ByteBuffer): PacketLapData {
        val lapData = List(22) { parseLapData(buffer) }
        return PacketLapData(header, lapData)
    }

    private fun parseLapData(buffer: ByteBuffer) = LapData(
        m_lastLapTimeInMS = buffer.int.toLong(),
        m_currentLapTimeInMS = buffer.int.toLong(),
        m_sector1TimeInMS = buffer.short.toInt(),
        m_sector2TimeInMS = buffer.short.toInt(),
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

    private fun parseCarTelemetryPacket(header: PacketHeader, buffer: ByteBuffer): PacketCarTelemetryData {
        val carTelemetryData = List(22) { parseCarTelemetryData(buffer) }
        return PacketCarTelemetryData(header, carTelemetryData)
    }

    private fun parseCarTelemetryData(buffer: ByteBuffer) = CarTelemetryData(
        m_speed = buffer.short.toInt(),
        m_throttle = buffer.float,
        m_steer = buffer.float,
        m_brake = buffer.float,
        m_clutch = buffer.get().toInt(),
        m_gear = buffer.get().toInt(),
        m_engineRPM = buffer.short.toInt(),
        m_drs = buffer.get().toInt(),
        m_revLightsPercent = buffer.get().toInt(),
        m_revLightsBitValue = buffer.short.toInt(),
        m_brakesTemperature = List(4) { buffer.short.toInt() },
        m_tyresSurfaceTemperature = List(4) { buffer.get().toInt() },
        m_tyresInnerTemperature = List(4) { buffer.get().toInt() },
        m_engineTemperature = buffer.short.toInt(),
        m_tyresPressure = List(4) { buffer.float },
        m_surfaceType = List(4) { buffer.get().toInt() }
    )
}
