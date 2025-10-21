// F1 2025 Telemetry Application
// Complete telemetry simulation and app functionality

class F1TelemetryApp {
    constructor() {
        this.isConnected = false;
        this.isRecording = false;
        this.recordingStartTime = null;
        this.currentScreen = 'dashboard';
        this.telemetryData = this.initializeTelemetryData();
        this.recordedSessions = this.loadRecordedSessions();
        this.simulationInterval = null;
        this.recordingInterval = null;
        this.currentLap = 1;
        this.trackZoom = 1;
        this.trackPanX = 0;
        this.trackPanY = 0;
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupRevLights();
        this.setupTimingTable();
        this.setupCarMarkers();
        this.loadSessionsList();
        this.startTelemetrySimulation();
        
        // Auto-connect simulation after 2 seconds
        setTimeout(() => {
            this.simulateConnection();
        }, 2000);
    }

    initializeTelemetryData() {
        return {
            // Car motion data
            speed: 0,
            rpm: 800,
            gear: 0,
            throttle: 0,
            brake: 0,
            steering: 0,
            drs: 0,
            
            // Temperatures
            engineTemp: 85,
            brakeTemps: [320, 325, 310, 315],
            tireSurfaceTemps: [95, 97, 92, 94],
            tireInnerTemps: [88, 90, 85, 87],
            
            // Other data
            fuel: 45.2,
            position: 1,
            lapTime: '1:23.456',
            sector1: '28.123',
            sector2: '32.456',
            sector3: '22.877',
            
            // Position data
            worldPosX: 200,
            worldPosY: 150,
            
            // Physics
            gForceX: 0,
            gForceY: 0,
            gForceZ: 1
        };
    }

    setupEventListeners() {
        // Navigation
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => {
            item.addEventListener('click', () => {
                const screen = item.dataset.screen;
                this.switchScreen(screen);
            });
        });

        // Settings inputs
        const inputs = document.querySelectorAll('.setting-input');
        inputs.forEach(input => {
            input.addEventListener('change', () => {
                this.saveSettings();
            });
        });

        // Touch/gesture handling for mobile
        this.setupTouchHandlers();
    }

    setupTouchHandlers() {
        let startX, startY;
        const app = document.getElementById('app');
        
        app.addEventListener('touchstart', (e) => {
            startX = e.touches[0].clientX;
            startY = e.touches[0].clientY;
        }, { passive: true });
        
        app.addEventListener('touchmove', (e) => {
            e.preventDefault();
        }, { passive: false });
        
        // Double tap for fullscreen
        let lastTap = 0;
        app.addEventListener('touchend', (e) => {
            const currentTime = new Date().getTime();
            const tapLength = currentTime - lastTap;
            if (tapLength < 500 && tapLength > 0) {
                if (this.currentScreen === 'dashboard') {
                    this.toggleFullscreen();
                }
            }
            lastTap = currentTime;
        });
    }

    switchScreen(screenName) {
        // Hide all screens
        const screens = document.querySelectorAll('.screen');
        screens.forEach(screen => screen.classList.remove('active'));
        
        // Show selected screen
        const targetScreen = document.getElementById(screenName + 'Screen');
        if (targetScreen) {
            targetScreen.classList.add('active');
        }
        
        // Update navigation
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => item.classList.remove('active'));
        
        const activeNav = document.querySelector(`[data-screen="${screenName}"]`);
        if (activeNav) {
            activeNav.classList.add('active');
        }
        
        this.currentScreen = screenName;
        
        // Screen-specific actions
        if (screenName === 'map') {
            this.updateMap();
        } else if (screenName === 'timing') {
            this.updateTimingTable();
        }
    }

    setupRevLights() {
        const revLightsContainer = document.getElementById('revLights');
        revLightsContainer.innerHTML = '';
        
        for (let i = 0; i < 10; i++) {
            const light = document.createElement('div');
            light.className = 'rev-light';
            revLightsContainer.appendChild(light);
        }
    }

    updateRevLights() {
        const lights = document.querySelectorAll('.rev-light');
        const rpmPercent = this.telemetryData.rpm / 12000; // Max RPM 12000
        const activeLights = Math.floor(rpmPercent * 10);
        
        lights.forEach((light, index) => {
            light.classList.remove('active', 'warning', 'danger');
            
            if (index < activeLights) {
                if (index >= 8) {
                    light.classList.add('danger');
                } else if (index >= 6) {
                    light.classList.add('warning');
                } else {
                    light.classList.add('active');
                }
            }
        });
    }

    startTelemetrySimulation() {
        this.simulationInterval = setInterval(() => {
            if (this.isConnected) {
                this.simulateTelemetryData();
                this.updateDashboard();
                
                if (this.currentScreen === 'map') {
                    this.updateMap();
                } else if (this.currentScreen === 'timing') {
                    this.updateTimingTable();
                }
                
                if (this.isRecording) {
                    this.recordTelemetryFrame();
                }
            }
        }, 33); // ~30 FPS
    }

    simulateTelemetryData() {
        const time = Date.now() / 1000;
        
        // Simulate realistic F1 car behavior
        // Speed varies between 50-320 km/h
        this.telemetryData.speed = Math.max(50, 180 + Math.sin(time * 0.1) * 100 + Math.random() * 40);
        
        // RPM correlates with speed but varies
        const baseRPM = (this.telemetryData.speed / 320) * 11000 + 1000;
        this.telemetryData.rpm = Math.max(800, baseRPM + Math.sin(time * 0.5) * 2000 + Math.random() * 500);
        
        // Gear based on RPM
        if (this.telemetryData.rpm < 2000) this.telemetryData.gear = 1;
        else if (this.telemetryData.rpm < 4000) this.telemetryData.gear = 2;
        else if (this.telemetryData.rpm < 6000) this.telemetryData.gear = 3;
        else if (this.telemetryData.rpm < 8000) this.telemetryData.gear = 4;
        else if (this.telemetryData.rpm < 9500) this.telemetryData.gear = 5;
        else if (this.telemetryData.rpm < 10500) this.telemetryData.gear = 6;
        else if (this.telemetryData.rpm < 11500) this.telemetryData.gear = 7;
        else this.telemetryData.gear = 8;
        
        // Throttle and brake simulation
        this.telemetryData.throttle = Math.max(0, Math.min(1, 0.7 + Math.sin(time * 0.3) * 0.3));
        this.telemetryData.brake = Math.max(0, Math.sin(time * 0.7) * 0.4 + 0.1);
        if (this.telemetryData.brake < 0.1) this.telemetryData.brake = 0;
        
        // Steering
        this.telemetryData.steering = Math.sin(time * 0.2) * 0.6;
        
        // DRS (randomly activate)
        this.telemetryData.drs = Math.random() > 0.85 ? 1 : 0;
        
        // Temperatures (realistic F1 ranges)
        this.telemetryData.engineTemp = 85 + Math.sin(time * 0.05) * 15 + Math.random() * 5;
        
        // Tire temperatures
        for (let i = 0; i < 4; i++) {
            this.telemetryData.tireSurfaceTemps[i] = 85 + Math.sin(time * 0.1 + i) * 20 + Math.random() * 10;
            this.telemetryData.tireInnerTemps[i] = this.telemetryData.tireSurfaceTemps[i] - 10 + Math.random() * 5;
        }
        
        // Brake temperatures
        for (let i = 0; i < 4; i++) {
            this.telemetryData.brakeTemps[i] = 300 + this.telemetryData.brake * 200 + Math.random() * 50;
        }
        
        // Fuel consumption
        this.telemetryData.fuel = Math.max(0, 45.2 - (time % 3600) * 0.008);
        
        // Position on track (simple circular motion)
        const trackTime = time * 0.05;
        this.telemetryData.worldPosX = 200 + Math.cos(trackTime) * 120;
        this.telemetryData.worldPosY = 150 + Math.sin(trackTime) * 80;
        
        // G-Forces
        this.telemetryData.gForceX = this.telemetryData.steering * 2;
        this.telemetryData.gForceY = (this.telemetryData.throttle - this.telemetryData.brake) * 3;
        this.telemetryData.gForceZ = 1 + Math.random() * 0.5;
    }

    updateDashboard() {
        // Speed
        document.getElementById('speedValue').textContent = Math.round(this.telemetryData.speed);
        
        // RPM
        document.getElementById('rpmValue').textContent = Math.round(this.telemetryData.rpm);
        const rpmFill = document.getElementById('rpmFill');
        const rpmPercent = (this.telemetryData.rpm / 12000) * 360;
        rpmFill.style.transform = `rotate(${rpmPercent}deg)`;
        
        // Gear
        const gearDisplay = document.getElementById('gearDisplay');
        gearDisplay.textContent = this.telemetryData.gear === 0 ? 'N' : this.telemetryData.gear;
        
        // Throttle and Brake bars
        const throttleFill = document.getElementById('throttleFill');
        const brakeFill = document.getElementById('brakeFill');
        throttleFill.style.height = `${this.telemetryData.throttle * 100}%`;
        brakeFill.style.height = `${this.telemetryData.brake * 100}%`;
        
        // Engine temperature
        document.getElementById('engineTemp').textContent = Math.round(this.telemetryData.engineTemp);
        
        // Fuel
        document.getElementById('fuelLevel').textContent = this.telemetryData.fuel.toFixed(1);
        
        // Tire temperatures
        document.getElementById('tireTempFL').textContent = Math.round(this.telemetryData.tireSurfaceTemps[0]);
        document.getElementById('tireTempFR').textContent = Math.round(this.telemetryData.tireSurfaceTemps[1]);
        document.getElementById('tireTempRL').textContent = Math.round(this.telemetryData.tireSurfaceTemps[2]);
        document.getElementById('tireTempRR').textContent = Math.round(this.telemetryData.tireSurfaceTemps[3]);
        
        // Update rev lights
        this.updateRevLights();
        
        // Position
        document.getElementById('positionValue').textContent = this.telemetryData.position;
    }

    setupTimingTable() {
        const drivers = [
            'L. Hamilton', 'M. Verstappen', 'C. Leclerc', 'G. Russell', 'F. Alonso',
            'L. Norris', 'C. Sainz', 'S. Perez', 'O. Piastri', 'P. Gasly',
            'E. Ocon', 'A. Albon', 'Y. Tsunoda', 'L. Stroll', 'K. Magnussen',
            'N. Hulkenberg', 'D. Ricciardo', 'V. Bottas', 'Zhou Guanyu', 'L. Sargeant'
        ];
        
        this.drivers = drivers.map((name, index) => ({
            position: index + 1,
            name,
            lapTime: this.generateRandomLapTime(),
            sector1: this.generateRandomSectorTime(25, 35),
            sector2: this.generateRandomSectorTime(30, 40),
            sector3: this.generateRandomSectorTime(20, 30),
            bestLap: this.generateRandomLapTime(),
            gap: index === 0 ? '0.000' : `+${(Math.random() * (index * 0.5 + 0.1)).toFixed(3)}`
        }));
    }

    generateRandomLapTime() {
        const baseTime = 83; // Base lap time in seconds
        const variation = (Math.random() - 0.5) * 4; // ±2 seconds variation
        const totalSeconds = baseTime + variation;
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = (totalSeconds % 60).toFixed(3);
        return `${minutes}:${seconds.padStart(6, '0')}`;
    }

    generateRandomSectorTime(min, max) {
        const time = min + Math.random() * (max - min);
        return time.toFixed(3);
    }

    updateTimingTable() {
        const timingList = document.getElementById('timingList');
        timingList.innerHTML = '';
        
        // Occasionally shuffle positions slightly for realism
        if (Math.random() > 0.95) {
            this.shufflePositions();
        }
        
        this.drivers.forEach((driver, index) => {
            const row = document.createElement('div');
            row.className = 'timing-row';
            
            // Randomly update sector times
            if (Math.random() > 0.8) {
                driver.sector1 = this.generateRandomSectorTime(25, 35);
                driver.sector2 = this.generateRandomSectorTime(30, 40);
                driver.sector3 = this.generateRandomSectorTime(20, 30);
            }
            
            row.innerHTML = `
                <div class="position">${driver.position}</div>
                <div class="driver-name">${driver.name}</div>
                <div class="time">${driver.lapTime}</div>
                <div class="time ${Math.random() > 0.9 ? 'fastest' : ''}">${driver.sector1}</div>
                <div class="time ${Math.random() > 0.9 ? 'personal-best' : ''}">${driver.sector2}</div>
                <div class="time">${driver.sector3}</div>
            `;
            
            timingList.appendChild(row);
        });
    }

    shufflePositions() {
        // Small position changes for realism
        for (let i = 0; i < this.drivers.length - 1; i++) {
            if (Math.random() > 0.9) {
                // Swap adjacent positions
                [this.drivers[i], this.drivers[i + 1]] = [this.drivers[i + 1], this.drivers[i]];
                this.drivers[i].position = i + 1;
                this.drivers[i + 1].position = i + 2;
            }
        }
    }

    setupCarMarkers() {
        const svg = document.getElementById('trackSvg');
        
        // Create car markers for other cars
        for (let i = 0; i < 20; i++) {
            const marker = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            marker.setAttribute('class', i === 0 ? 'car-marker player' : 'car-marker');
            marker.setAttribute('cx', 200);
            marker.setAttribute('cy', 150);
            marker.id = `car-${i}`;
            svg.appendChild(marker);
        }
    }

    updateMap() {
        const playerCar = document.getElementById('car-0');
        if (playerCar) {
            playerCar.setAttribute('cx', this.telemetryData.worldPosX);
            playerCar.setAttribute('cy', this.telemetryData.worldPosY);
        }
        
        // Update other cars with simulated positions
        for (let i = 1; i < 20; i++) {
            const car = document.getElementById(`car-${i}`);
            if (car) {
                const offset = i * 10; // Cars spread around track
                const time = (Date.now() / 1000 + offset) * 0.04;
                const x = 200 + Math.cos(time) * 120;
                const y = 150 + Math.sin(time) * 80;
                car.setAttribute('cx', x);
                car.setAttribute('cy', y);
            }
        }
    }

    simulateConnection() {
        this.isConnected = true;
        document.getElementById('statusDot').classList.add('connected');
        document.getElementById('statusText').textContent = 'Connected - F1 2025';
    }

    toggleRecording() {
        if (!this.isRecording) {
            this.startRecording();
        } else {
            this.stopRecording();
        }
    }

    startRecording() {
        this.isRecording = true;
        this.recordingStartTime = Date.now();
        this.currentRecordingData = [];
        
        const recordBtn = document.getElementById('recordBtn');
        recordBtn.classList.add('recording');
        document.getElementById('recordText').textContent = 'Stop Recording';
        
        // Update recording duration
        this.recordingInterval = setInterval(() => {
            const duration = Math.floor((Date.now() - this.recordingStartTime) / 1000);
            const minutes = Math.floor(duration / 60);
            const seconds = duration % 60;
            document.getElementById('recordDuration').textContent = 
                `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        }, 1000);
    }

    stopRecording() {
        this.isRecording = false;
        
        const recordBtn = document.getElementById('recordBtn');
        recordBtn.classList.remove('recording');
        document.getElementById('recordText').textContent = 'Start Recording';
        
        clearInterval(this.recordingInterval);
        
        // Save the session
        const duration = Math.floor((Date.now() - this.recordingStartTime) / 1000);
        const sessionName = `Session ${new Date().toLocaleDateString()} ${new Date().toLocaleTimeString()}`;
        
        const session = {
            id: Date.now(),
            name: sessionName,
            duration: duration,
            date: new Date().toISOString(),
            data: this.currentRecordingData || []
        };
        
        this.recordedSessions.push(session);
        this.saveRecordedSessions();
        this.loadSessionsList();
        
        document.getElementById('recordDuration').textContent = '00:00';
    }

    recordTelemetryFrame() {
        if (!this.currentRecordingData) {
            this.currentRecordingData = [];
        }
        
        this.currentRecordingData.push({
            timestamp: Date.now() - this.recordingStartTime,
            ...this.telemetryData
        });
    }

    loadSessionsList() {
        const sessionsList = document.getElementById('sessionsList');
        sessionsList.innerHTML = '';
        
        if (this.recordedSessions.length === 0) {
            sessionsList.innerHTML = '<p style="color: #888; text-align: center; padding: 20px;">No recorded sessions</p>';
            return;
        }
        
        this.recordedSessions.forEach(session => {
            const sessionItem = document.createElement('div');
            sessionItem.className = 'session-item';
            
            const duration = Math.floor(session.duration / 60) + ':' + 
                           (session.duration % 60).toString().padStart(2, '0');
            
            sessionItem.innerHTML = `
                <div class="session-info">
                    <h4>${session.name}</h4>
                    <p>Duration: ${duration} | ${new Date(session.date).toLocaleDateString()}</p>
                </div>
                <div class="session-actions">
                    <button class="action-btn" onclick="app.exportSession(${session.id})">Export</button>
                    <button class="action-btn" onclick="app.deleteSession(${session.id})">Delete</button>
                </div>
            `;
            
            sessionsList.appendChild(sessionItem);
        });
    }

    exportSession(sessionId) {
        const session = this.recordedSessions.find(s => s.id === sessionId);
        if (!session) return;
        
        // Create CSV data
        let csvContent = "timestamp,speed,rpm,gear,throttle,brake,steering,engineTemp,fuel\n";
        
        session.data.forEach(frame => {
            csvContent += `${frame.timestamp},${frame.speed},${frame.rpm},${frame.gear},` +
                         `${frame.throttle},${frame.brake},${frame.steering},` +
                         `${frame.engineTemp},${frame.fuel}\n`;
        });
        
        // Download file
        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = `f1_telemetry_${session.name.replace(/[^a-z0-9]/gi, '_')}.csv`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    }

    deleteSession(sessionId) {
        this.recordedSessions = this.recordedSessions.filter(s => s.id !== sessionId);
        this.saveRecordedSessions();
        this.loadSessionsList();
    }

    saveRecordedSessions() {
        // In a real app, this would save to device storage
        // For web demo, we'll just keep in memory
    }

    loadRecordedSessions() {
        // In a real app, this would load from device storage
        return [];
    }

    saveSettings() {
        // In a real app, this would save settings to device storage
        const settings = {
            udpPort: document.getElementById('udpPort').value,
            gameIP: document.getElementById('gameIP').value,
            timeout: document.getElementById('timeout').value,
            refreshRate: document.getElementById('refreshRate').value,
            tempUnit: document.getElementById('tempUnit').value,
            speedUnit: document.getElementById('speedUnit').value
        };
        
        console.log('Settings saved:', settings);
    }

    toggleFullscreen() {
        const app = document.getElementById('app');
        
        if (!app.classList.contains('fullscreen')) {
            app.classList.add('fullscreen');
            
            // Add exit button
            const exitBtn = document.createElement('button');
            exitBtn.className = 'exit-fullscreen';
            exitBtn.textContent = 'Exit Fullscreen';
            exitBtn.onclick = () => this.toggleFullscreen();
            app.appendChild(exitBtn);
            
            // Hide system UI on mobile
            if (document.documentElement.requestFullscreen) {
                document.documentElement.requestFullscreen();
            }
        } else {
            app.classList.remove('fullscreen');
            
            // Remove exit button
            const exitBtn = app.querySelector('.exit-fullscreen');
            if (exitBtn) {
                exitBtn.remove();
            }
            
            // Exit fullscreen
            if (document.exitFullscreen) {
                document.exitFullscreen();
            }
        }
    }
}

// Map control functions
function zoomIn() {
    app.trackZoom = Math.min(app.trackZoom * 1.2, 3);
    updateMapTransform();
}

function zoomOut() {
    app.trackZoom = Math.max(app.trackZoom / 1.2, 0.5);
    updateMapTransform();
}

function resetZoom() {
    app.trackZoom = 1;
    app.trackPanX = 0;
    app.trackPanY = 0;
    updateMapTransform();
}

function updateMapTransform() {
    const svg = document.getElementById('trackSvg');
    svg.style.transform = `scale(${app.trackZoom}) translate(${app.trackPanX}px, ${app.trackPanY}px)`;
}

// Recording control functions
function toggleRecording() {
    app.toggleRecording();
}

// Initialize app when page loads
let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new F1TelemetryApp();
    
    // Add some sample recorded sessions for demo
    setTimeout(() => {
        app.recordedSessions = [
            {
                id: 1,
                name: 'Monaco Practice Session',
                duration: 1847,
                date: new Date(Date.now() - 86400000).toISOString(),
                data: []
            },
            {
                id: 2,
                name: 'Silverstone Qualifying',
                duration: 923,
                date: new Date(Date.now() - 172800000).toISOString(),
                data: []
            }
        ];
        app.loadSessionsList();
    }, 1000);
});

// Service Worker registration for PWA functionality
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js')
            .then((registration) => {
                console.log('SW registered: ', registration);
            })
            .catch((registrationError) => {
                console.log('SW registration failed: ', registrationError);
            });
    });
}

// Prevent zoom on double tap (iOS Safari)
document.addEventListener('touchend', function (event) {
    var now = (new Date()).getTime();
    if (now - lastTouchEnd <= 300) {
        event.preventDefault();
    }
    lastTouchEnd = now;
}, false);

var lastTouchEnd = 0;

// Handle orientation changes
window.addEventListener('orientationchange', () => {
    setTimeout(() => {
        // Recalculate layouts after orientation change
        if (app && app.currentScreen === 'map') {
            app.updateMap();
        }
    }, 100);
});

// Handle visibility changes (app going to background)
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        // App went to background - could pause non-critical updates
        console.log('App backgrounded');
    } else {
        // App returned to foreground
        console.log('App foregrounded');
    }
});