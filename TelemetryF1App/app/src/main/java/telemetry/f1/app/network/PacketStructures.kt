package telemetry.f1.app.network

/**
 * This file contains the data classes for the F1 2024 UDP Telemetry specification.
 * Each class corresponds to a specific packet type sent by the game.
 */

// Packet Header
data class PacketHeader(
    val m_packetFormat: Int,            // 2024
    val m_gameYear: Int,
    val m_gameMajorVersion: Int,
    val m_gameMinorVersion: Int,
    val m_packetVersion: Int,
    val m_packetId: Int,
    val m_sessionUID: Long,
    val m_sessionTime: Float,
    val m_frameIdentifier: Long,
    val m_overallFrameIdentifier: Long,
    val m_playerCarIndex: Int,
    val m_secondaryPlayerCarIndex: Int
) {
    companion object {
        const val SIZE_BYTES = 32
    }
}

// Motion Packet
data class CarMotionData(
    val m_worldPositionX: Float,
    val m_worldPositionY: Float,
    val m_worldPositionZ: Float,
    val m_worldVelocityX: Float,
    val m_worldVelocityY: Float,
    val m_worldVelocityZ: Float,
    val m_worldForwardDirX: Short,
    val m_worldForwardDirY: Short,
    val m_worldForwardDirZ: Short,
    val m_worldRightDirX: Short,
    val m_worldRightDirY: Short,
    val m_worldRightDirZ: Short,
    val m_gForceLateral: Float,
    val m_gForceLongitudinal: Float,
    val m_gForceVertical: Float,
    val m_yaw: Float,
    val m_pitch: Float,
    val m_roll: Float
)

data class PacketMotionData(
    val m_header: PacketHeader,
    val m_carMotionData: List<CarMotionData>
)

// Session Packet
data class PacketSessionData(
    val m_header: PacketHeader,
    val m_weather: Int,
    val m_trackTemperature: Int,
    val m_airTemperature: Int,
    val m_totalLaps: Int,
    val m_trackLength: Int,
    val m_sessionType: Int,
    val m_trackId: Int,
    val m_formula: Int
)

// Lap Data Packet
data class LapData(
    val m_lastLapTimeInMS: Long,
    val m_currentLapTimeInMS: Long,
    val m_sector1TimeInMS: Int,
    val m_sector2TimeInMS: Int,
    val m_lapDistance: Float,
    val m_totalDistance: Float,
    val m_safetyCarDelta: Float,
    val m_carPosition: Int,
    val m_currentLapNum: Int,
    val m_pitStatus: Int,
    val m_numPitStops: Int,
    val m_sector: Int,
    val m_currentLapInvalid: Int,
    val m_penalties: Int,
    val m_warnings: Int,
    val m_driverStatus: Int,
    val m_resultStatus: Int
)

data class PacketLapData(
    val m_header: PacketHeader,
    val m_lapData: List<LapData>
)

// Car Telemetry Packet
data class CarTelemetryData(
    val m_speed: Int,
    val m_throttle: Float,
    val m_steer: Float,
    val m_brake: Float,
    val m_clutch: Int,
    val m_gear: Int,
    val m_engineRPM: Int,
    val m_drs: Int,
    val m_revLightsPercent: Int,
    val m_revLightsBitValue: Int,
    val m_brakesTemperature: List<Int>,
    val m_tyresSurfaceTemperature: List<Int>,
    val m_tyresInnerTemperature: List<Int>,
    val m_engineTemperature: Int,
    val m_tyresPressure: List<Float>,
    val m_surfaceType: List<Int>
)

data class PacketCarTelemetryData(
    val m_header: PacketHeader,
    val m_carTelemetryData: List<CarTelemetryData>
)

object PacketTypes {
    const val MOTION = 0
    const val SESSION = 1
    const val LAP_DATA = 2
    const val CAR_TELEMETRY = 6
}
