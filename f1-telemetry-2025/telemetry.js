// F1 2025 Telemetry Data Structures and UDP Simulation
// This file contains the telemetry packet definitions and networking simulation

class F1TelemetryPacket {
    constructor() {
        this.header = {
            packetFormat: 2025,
            gameMajorVersion: 1,
            gameMinorVersion: 0,
            packetVersion: 1,
            packetId: 0,
            sessionUID: BigInt(0),
            sessionTime: 0,
            frameIdentifier: 0,
            playerCarIndex: 0,
            secondaryPlayerCarIndex: 255
        };
    }
}

class CarMotionData {
    constructor() {
        this.worldPositionX = 0;     // World space X position
        this.worldPositionY = 0;     // World space Y position 
        this.worldPositionZ = 0;     // World space Z position
        this.worldVelocityX = 0;     // Velocity in world space X
        this.worldVelocityY = 0;     // Velocity in world space Y
        this.worldVelocityZ = 0;     // Velocity in world space Z
        this.worldForwardDirX = 0;   // World space forward X direction (normalised)
        this.worldForwardDirY = 0;   // World space forward Y direction (normalised)
        this.worldForwardDirZ = 0;   // World space forward Z direction (normalised)
        this.worldRightDirX = 0;     // World space right X direction (normalised)
        this.worldRightDirY = 0;     // World space right Y direction (normalised)
        this.worldRightDirZ = 0;     // World space right Z direction (normalised)
        this.gForceLateral = 0;      // Lateral G-Force component
        this.gForceLongitudinal = 0; // Longitudinal G-Force component
        this.gForceVertical = 0;     // Vertical G-Force component
        this.yaw = 0;                // Yaw angle in radians
        this.pitch = 0;              // Pitch angle in radians
        this.roll = 0;               // Roll angle in radians
    }
}

class CarTelemetryData {
    constructor() {
        this.speed = 0;                    // Speed of car in km/h
        this.throttle = 0;                 // Amount of throttle applied (0.0 to 1.0)
        this.steer = 0;                    // Steering (-1.0 (full lock left) to 1.0 (full lock right))
        this.brake = 0;                    // Amount of brake applied (0.0 to 1.0)
        this.clutch = 0;                   // Amount of clutch applied (0 to 1)
        this.gear = 0;                     // Gear selected (1-8, N=0, R=-1)
        this.engineRPM = 0;                // Engine RPM
        this.drs = 0;                      // 0 = off, 1 = on
        this.revLightsPercent = 0;         // Rev lights indicator (percentage)
        this.revLightsBitValue = 0;        // Rev lights (bit 0 = leftmost LED, bit 14 = rightmost LED)
        this.brakesTemperature = [0,0,0,0]; // Brakes temperature (celsius) [RL, RR, FL, FR]
        this.tyresSurfaceTemperature = [0,0,0,0]; // Tyres surface temperature (celsius) [RL, RR, FL, FR]
        this.tyresInnerTemperature = [0,0,0,0];   // Tyres inner temperature (celsius) [RL, RR, FL, FR]
        this.engineTemperature = 0;        // Engine temperature (celsius)
        this.tyresPressure = [0,0,0,0];    // Tyres pressure (PSI) [RL, RR, FL, FR]
        this.surfaceType = [0,0,0,0];      // Driving surface [RL, RR, FL, FR]
    }
}

class CarStatusData {
    constructor() {
        this.tractionControl = 0;          // Traction control - 0 (off) to 2 (high)
        this.antiLockBrakes = 0;           // 0 (off) - 1 (on)
        this.fuelMix = 0;                  // Fuel mix - 0 = lean, 1 = standard, 2 = rich, 3 = max
        this.frontBrakeBias = 0;           // Front brake bias (percentage)
        this.pitLimiterStatus = 0;         // Pit limiter status - 0 = off, 1 = on
        this.fuelInTank = 0;               // Current fuel mass
        this.fuelCapacity = 0;             // Fuel capacity
        this.fuelRemainingLaps = 0;        // Fuel remaining in terms of laps (value on MFD)
        this.maxRPM = 0;                   // Cars max RPM, point of rev limiter
        this.idleRPM = 0;                  // Cars idle RPM
        this.maxGears = 0;                 // Maximum number of gears
        this.drsAllowed = 0;               // 0 = not allowed, 1 = allowed
        this.drsActivationDistance = 0;    // 0 = DRS not available, non-zero - DRS will be available in [X] metres
        this.actualTyreCompound = 0;       // F1 Modern - 16 = C5, 17 = C4, 18 = C3, 19 = C2, 20 = C1
        this.visualTyreCompound = 0;       // F1 visual (can be different from actual compound)
        this.tyresAgeLaps = 0;             // Age in laps of the current set of tyres
        this.vehicleFiaFlags = 0;          // -1 = invalid/unknown, 0 = none, 1 = green, 2 = blue, 3 = yellow, 4 = red
        this.ersStoreEnergy = 0;           // ERS energy store in Joules
        this.ersDeployMode = 0;            // ERS deployment mode, 0 = none, 1 = medium, 2 = hotlap, 3 = overtake
        this.ersHarvestedThisLapMGUK = 0;  // ERS energy harvested this lap by MGU-K
        this.ersHarvestedThisLapMGUH = 0;  // ERS energy harvested this lap by MGU-H
        this.ersDeployedThisLap = 0;       // ERS energy deployed this lap
        this.networkPaused = 0;            // Whether the car is paused in a network game
    }
}

class LapData {
    constructor() {
        this.lastLapTimeInMS = 0;          // Last lap time in milliseconds
        this.currentLapTimeInMS = 0;       // Current time around the lap in milliseconds
        this.sector1TimeInMS = 0;          // Sector 1 time in milliseconds
        this.sector2TimeInMS = 0;          // Sector 2 time in milliseconds
        this.currentLapDistance = 0;       // Distance vehicle is around current lap in metres – could be negative if line hasn't been crossed yet
        this.totalDistance = 0;            // Total distance travelled in session in metres – could be negative if line hasn't been crossed yet
        this.safetyCarDelta = 0;           // Delta in seconds for safety car
        this.carPosition = 0;              // Car race position
        this.currentLapNum = 0;            // Current lap number
        this.pitStatus = 0;                // 0 = none, 1 = pitting, 2 = in pit area
        this.numPitStops = 0;              // Number of pit stops taken in this race
        this.sector = 0;                   // 0 = sector1, 1 = sector2, 2 = sector3
        this.currentLapInvalid = 0;        // Current lap invalid - 0 = valid, 1 = invalid
        this.penalties = 0;                // Accumulated time penalties in seconds to be added
        this.warnings = 0;                 // Accumulated number of warnings issued
        this.numUnservedDriveThroughPens = 0; // Num drive through pens left to serve
        this.numUnservedStopGoPens = 0;    // Num stop go pens left to serve
        this.gridPosition = 0;             // Grid position the vehicle started the race in
        this.driverStatus = 0;             // Status of driver - 0 = in garage, 1 = flying lap, 2 = in lap, 3 = out lap, 4 = on track
        this.resultStatus = 0;             // Result status - 0 = invalid, 1 = inactive, 2 = active, 3 = finished, 4 = didnotfinish, 5 = disqualified, 6 = not classified, 7 = retired
        this.pitLaneTimerActive = 0;       // Pit lane timing, 0 = inactive, 1 = active
        this.pitLaneTimeInLaneInMS = 0;    // If active, the current time spent in the pit lane in ms
        this.pitStopTimerInMS = 0;         // Time of the actual pit stop in ms
        this.pitStopShouldServePen = 0;    // Whether the car should serve a penalty at this stop
    }
}

class SessionData {
    constructor() {
        this.weather = 0;                  // Weather - 0 = clear, 1 = light cloud, 2 = overcast, 3 = light rain, 4 = heavy rain, 5 = storm
        this.trackTemperature = 0;         // Track temp. in degrees Celsius
        this.airTemperature = 0;           // Air temp. in degrees Celsius
        this.totalLaps = 0;                // Total number of laps in this race
        this.trackLength = 0;              // Track length in metres
        this.sessionType = 0;              // 0 = unknown, 1 = P1, 2 = P2, 3 = P3, 4 = Short P, 5 = Q1, 6 = Q2, 7 = Q3, 8 = Short Q, 9 = OSQ, 10 = R, 11 = R2, 12 = R3, 13 = Time Trial
        this.trackId = 0;                  // -1 for unknown, see appendix
        this.formula = 0;                  // Formula, 0 = F1 Modern, 1 = F1 Classic, 2 = F2, 3 = F1 Generic, 4 = Beta, 5 = Supercars, 6 = Esports, 7 = F2 2021
        this.sessionTimeLeft = 0;          // Time left in session in seconds
        this.sessionDuration = 0;          // Session duration in seconds
        this.pitSpeedLimit = 0;            // Pit speed limit in kilometres per hour
        this.gamePaused = 0;               // Whether the game is paused – network game only
        this.isSpectating = 0;             // Whether the player is spectating
        this.spectatorCarIndex = 0;        // Index of the car being spectated
        this.sliProNativeSupport = 0;      // SLI Pro support, 0 = inactive, 1 = active
        this.numMarshalZones = 0;          // Number of marshal zones to follow
        this.marshalZones = [];            // List of marshal zones – max 21
        this.safetyCarStatus = 0;          // 0 = no safety car, 1 = full, 2 = virtual, 3 = formation lap
        this.networkGame = 0;              // 0 = offline, 1 = online
        this.numWeatherForecastSamples = 0; // Number of weather samples to follow
        this.weatherForecastSamples = [];  // Array of weather forecast samples
        this.forecastAccuracy = 0;         // 0 = Perfect, 1 = Approximate
        this.aiDifficulty = 0;             // AI Difficulty rating – 0-110
        this.seasonLinkIdentifier = 0;     // Identifier for season - persists across saves
        this.weekendLinkIdentifier = 0;    // Identifier for weekend - persists across saves
        this.sessionLinkIdentifier = 0;    // Identifier for session - persists across saves
        this.pitStopWindowIdealLap = 0;    // Ideal lap to pit on for current strategy (player)
        this.pitStopWindowLatestLap = 0;   // Latest lap to pit on for current strategy (player)
        this.pitStopRejoinPosition = 0;    // Predicted position to rejoin at (player)
        this.steeringAssist = 0;           // 0 = off, 1 = on
        this.brakingAssist = 0;            // 0 = off, 1 = low, 2 = medium, 3 = high
        this.gearboxAssist = 0;            // 1 = manual, 2 = manual & suggested gear, 3 = auto
        this.pitAssist = 0;                // 0 = off, 1 = on
        this.pitReleaseAssist = 0;         // 0 = off, 1 = on
        this.ERSAssist = 0;                // 0 = off, 1 = on
        this.DRSAssist = 0;                // 0 = off, 1 = on
        this.dynamicRacingLine = 0;        // 0 = off, 1 = corners only, 2 = full
        this.dynamicRacingLineType = 0;    // 0 = 2D, 1 = 3D
        this.gameMode = 0;                 // Game mode id - see appendix
        this.ruleSet = 0;                  // Ruleset - see appendix
        this.timeOfDay = 0;                // Local time of day - minutes since midnight
        this.sessionLength = 0;            // 0 = None, 2 = Very Short, 3 = Short, 4 = Medium, 5 = Medium Long, 6 = Long, 7 = Full
    }
}

// Track definitions
const F1_TRACKS = {
    0: { name: 'Melbourne', country: 'Australia', length: 5303 },
    1: { name: 'Paul Ricard', country: 'France', length: 5842 },
    2: { name: 'Shanghai', country: 'China', length: 5451 },
    3: { name: 'Sakhir (Bahrain)', country: 'Bahrain', length: 5412 },
    4: { name: 'Catalunya', country: 'Spain', length: 4675 },
    5: { name: 'Monaco', country: 'Monaco', length: 3337 },
    6: { name: 'Montreal', country: 'Canada', length: 4361 },
    7: { name: 'Silverstone', country: 'Great Britain', length: 5891 },
    8: { name: 'Hockenheim', country: 'Germany', length: 4574 },
    9: { name: 'Hungaroring', country: 'Hungary', length: 4381 },
    10: { name: 'Spa', country: 'Belgium', length: 7004 },
    11: { name: 'Monza', country: 'Italy', length: 5793 },
    12: { name: 'Singapore', country: 'Singapore', length: 5063 },
    13: { name: 'Suzuka', country: 'Japan', length: 5807 },
    14: { name: 'Abu Dhabi', country: 'UAE', length: 5554 },
    15: { name: 'Texas', country: 'USA', length: 5513 },
    16: { name: 'Brazil', country: 'Brazil', length: 4309 },
    17: { name: 'Austria', country: 'Austria', length: 4318 },
    18: { name: 'Sochi', country: 'Russia', length: 5848 },
    19: { name: 'Mexico', country: 'Mexico', length: 4304 },
    20: { name: 'Baku', country: 'Azerbaijan', length: 6003 },
    21: { name: 'Sakhir Short', country: 'Bahrain', length: 3543 },
    22: { name: 'Silverstone Short', country: 'Great Britain', length: 3658 },
    23: { name: 'Texas Short', country: 'USA', length: 2830 },
    24: { name: 'Suzuka Short', country: 'Japan', length: 2130 },
    25: { name: 'Hanoi', country: 'Vietnam', length: 5607 },
    26: { name: 'Zandvoort', country: 'Netherlands', length: 4259 },
    27: { name: 'Imola', country: 'Italy', length: 4909 },
    28: { name: 'Portimão', country: 'Portugal', length: 4692 },
    29: { name: 'Jeddah', country: 'Saudi Arabia', length: 6174 },
    30: { name: 'Miami', country: 'USA', length: 5412 },
    31: { name: 'Las Vegas', country: 'USA', length: 6201 },
    32: { name: 'Losail', country: 'Qatar', length: 5380 }
};

// UDP Network simulation class
class UDPTelemetrySimulator {
    constructor() {
        this.isConnected = false;
        this.port = 20777;
        this.updateRate = 30; // Hz
        this.packetId = 0;
        this.frameId = 0;
        this.sessionTime = 0;
        this.callbacks = {};
    }
    
    connect(ip, port) {
        this.port = port || 20777;
        
        // Simulate connection process
        setTimeout(() => {
            this.isConnected = true;
            this.startPacketStream();
            if (this.callbacks.onConnect) {
                this.callbacks.onConnect();
            }
        }, 1000);
    }
    
    disconnect() {
        this.isConnected = false;
        if (this.streamInterval) {
            clearInterval(this.streamInterval);
        }
        if (this.callbacks.onDisconnect) {
            this.callbacks.onDisconnect();
        }
    }
    
    on(event, callback) {
        this.callbacks[event] = callback;
    }
    
    startPacketStream() {
        this.streamInterval = setInterval(() => {
            this.frameId++;
            this.sessionTime += (1000 / this.updateRate);
            
            // Simulate different packet types
            if (this.frameId % 2 === 0) {
                this.sendMotionPacket();
            }
            if (this.frameId % 3 === 0) {
                this.sendTelemetryPacket();
            }
            if (this.frameId % 5 === 0) {
                this.sendLapPacket();
            }
            if (this.frameId % 10 === 0) {
                this.sendSessionPacket();
            }
            
        }, 1000 / this.updateRate);
    }
    
    sendMotionPacket() {
        const packet = new F1TelemetryPacket();
        packet.header.packetId = 0; // Motion packet
        packet.header.frameIdentifier = this.frameId;
        packet.header.sessionTime = this.sessionTime;
        
        const motionData = new CarMotionData();
        // Fill with simulated data...
        
        if (this.callbacks.onMotionData) {
            this.callbacks.onMotionData(motionData);
        }
    }
    
    sendTelemetryPacket() {
        const packet = new F1TelemetryPacket();
        packet.header.packetId = 6; // Telemetry packet
        packet.header.frameIdentifier = this.frameId;
        packet.header.sessionTime = this.sessionTime;
        
        const telemetryData = new CarTelemetryData();
        // Fill with simulated data...
        
        if (this.callbacks.onTelemetryData) {
            this.callbacks.onTelemetryData(telemetryData);
        }
    }
    
    sendLapPacket() {
        const packet = new F1TelemetryPacket();
        packet.header.packetId = 2; // Lap data packet
        
        const lapData = new LapData();
        // Fill with simulated data...
        
        if (this.callbacks.onLapData) {
            this.callbacks.onLapData(lapData);
        }
    }
    
    sendSessionPacket() {
        const packet = new F1TelemetryPacket();
        packet.header.packetId = 1; // Session packet
        
        const sessionData = new SessionData();
        sessionData.trackId = 5; // Monaco
        sessionData.sessionType = 10; // Race
        sessionData.trackTemperature = 35;
        sessionData.airTemperature = 28;
        
        if (this.callbacks.onSessionData) {
            this.callbacks.onSessionData(sessionData);
        }
    }
}

// Export for use in main app
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        F1TelemetryPacket,
        CarMotionData,
        CarTelemetryData,
        CarStatusData,
        LapData,
        SessionData,
        F1_TRACKS,
        UDPTelemetrySimulator
    };
}