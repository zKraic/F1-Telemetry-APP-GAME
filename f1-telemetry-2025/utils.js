// Utility functions for F1 Telemetry App

// Time formatting utilities
class TimeUtils {
    static formatLapTime(milliseconds) {
        if (!milliseconds || milliseconds <= 0) return '--:--.---';
        
        const totalSeconds = milliseconds / 1000;
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        
        return `${minutes}:${seconds.toFixed(3).padStart(6, '0')}`;
    }
    
    static formatSectorTime(milliseconds) {
        if (!milliseconds || milliseconds <= 0) return '--.---';
        
        const seconds = milliseconds / 1000;
        return seconds.toFixed(3);
    }
    
    static formatGap(gapSeconds) {
        if (gapSeconds === 0) return 'Leader';
        if (gapSeconds < 0) return `+${Math.abs(gapSeconds).toFixed(3)}`;
        return `+${gapSeconds.toFixed(3)}`;
    }
    
    static formatSessionTime(seconds) {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = Math.floor(seconds % 60);
        
        if (hours > 0) {
            return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
        }
        return `${minutes}:${secs.toString().padStart(2, '0')}`;
    }
}

// Unit conversion utilities
class UnitUtils {
    static kmhToMph(kmh) {
        return kmh * 0.621371;
    }
    
    static celsiusToFahrenheit(celsius) {
        return (celsius * 9/5) + 32;
    }
    
    static psiToBar(psi) {
        return psi * 0.0689476;
    }
    
    static metersToFeet(meters) {
        return meters * 3.28084;
    }
    
    static formatSpeed(kmh, unit = 'kmh') {
        if (unit === 'mph') {
            return `${Math.round(this.kmhToMph(kmh))} mph`;
        }
        return `${Math.round(kmh)} km/h`;
    }
    
    static formatTemperature(celsius, unit = 'celsius') {
        if (unit === 'fahrenheit') {
            return `${Math.round(this.celsiusToFahrenheit(celsius))}°F`;
        }
        return `${Math.round(celsius)}°C`;
    }
    
    static formatPressure(psi, unit = 'psi') {
        if (unit === 'bar') {
            return `${this.psiToBar(psi).toFixed(2)} bar`;
        }
        return `${psi.toFixed(1)} PSI`;
    }
}

// Color utilities for telemetry visualization
class ColorUtils {
    static getTemperatureColor(temp, minTemp = 80, maxTemp = 120, optimalMin = 90, optimalMax = 105) {
        if (temp < optimalMin) {
            // Cold - blue gradient
            const ratio = Math.max(0, (temp - minTemp) / (optimalMin - minTemp));
            return `hsl(240, 100%, ${30 + ratio * 40}%)`;
        } else if (temp > optimalMax) {
            // Hot - red gradient
            const ratio = Math.min(1, (temp - optimalMax) / (maxTemp - optimalMax));
            return `hsl(${15 - ratio * 15}, 100%, ${50 + ratio * 20}%)`;
        } else {
            // Optimal - green gradient
            return `hsl(120, 70%, 50%)`;
        }
    }
    
    static getRPMColor(rpm, maxRpm = 12000) {
        const ratio = rpm / maxRpm;
        
        if (ratio < 0.6) {
            return '#00FF41'; // Green
        } else if (ratio < 0.8) {
            return '#FFD700'; // Yellow
        } else if (ratio < 0.95) {
            return '#FF8C00'; // Orange
        } else {
            return '#FF3333'; // Red
        }
    }
    
    static getSpeedColor(speed, maxSpeed = 350) {
        const ratio = speed / maxSpeed;
        const hue = (1 - ratio) * 120; // Green to red
        return `hsl(${hue}, 70%, 50%)`;
    }
    
    static getPressureColor(pressure, optimal = 23.5, tolerance = 1.5) {
        const diff = Math.abs(pressure - optimal);
        
        if (diff <= tolerance * 0.5) {
            return '#00FF41'; // Green - optimal
        } else if (diff <= tolerance) {
            return '#FFD700'; // Yellow - acceptable
        } else {
            return '#FF3333'; // Red - out of range
        }
    }
}

// Data processing utilities
class DataUtils {
    static smoothValue(newValue, oldValue, smoothingFactor = 0.1) {
        return oldValue + (newValue - oldValue) * smoothingFactor;
    }
    
    static calculateMovingAverage(values, windowSize = 10) {
        if (values.length < windowSize) {
            return values.reduce((sum, val) => sum + val, 0) / values.length;
        }
        
        const window = values.slice(-windowSize);
        return window.reduce((sum, val) => sum + val, 0) / windowSize;
    }
    
    static interpolateValue(value1, value2, factor) {
        return value1 + (value2 - value1) * factor;
    }
    
    static clamp(value, min, max) {
        return Math.min(Math.max(value, min), max);
    }
    
    static normalizeAngle(angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
    
    static calculateDistance(x1, y1, x2, y2) {
        const dx = x2 - x1;
        const dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    static calculateGForce(velocity, previousVelocity, deltaTime) {
        const acceleration = (velocity - previousVelocity) / deltaTime;
        return acceleration / 9.81; // Convert to G-force
    }
}

// Storage utilities (memory-based for web version)
class StorageUtils {
    static storage = {};
    
    static setItem(key, value) {
        try {
            this.storage[key] = JSON.stringify(value);
            return true;
        } catch (error) {
            console.error('Storage error:', error);
            return false;
        }
    }
    
    static getItem(key, defaultValue = null) {
        try {
            const value = this.storage[key];
            return value ? JSON.parse(value) : defaultValue;
        } catch (error) {
            console.error('Storage retrieval error:', error);
            return defaultValue;
        }
    }
    
    static removeItem(key) {
        delete this.storage[key];
    }
    
    static clear() {
        this.storage = {};
    }
    
    static getAllKeys() {
        return Object.keys(this.storage);
    }
}

// Performance monitoring utilities
class PerformanceUtils {
    static measurements = {};
    
    static startMeasurement(name) {
        this.measurements[name] = performance.now();
    }
    
    static endMeasurement(name) {
        if (this.measurements[name]) {
            const duration = performance.now() - this.measurements[name];
            delete this.measurements[name];
            return duration;
        }
        return 0;
    }
    
    static measureFunction(name, fn) {
        this.startMeasurement(name);
        const result = fn();
        const duration = this.endMeasurement(name);
        console.log(`${name}: ${duration.toFixed(2)}ms`);
        return result;
    }
    
    static throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
    
    static debounce(func, wait, immediate) {
        let timeout;
        return function() {
            const context = this;
            const args = arguments;
            const later = function() {
                timeout = null;
                if (!immediate) func.apply(context, args);
            };
            const callNow = immediate && !timeout;
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
            if (callNow) func.apply(context, args);
        };
    }
}

// Validation utilities
class ValidationUtils {
    static isValidIPAddress(ip) {
        const regex = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
        return regex.test(ip);
    }
    
    static isValidPort(port) {
        const portNum = parseInt(port);
        return portNum >= 1 && portNum <= 65535;
    }
    
    static isValidTelemetryData(data) {
        return data && 
               typeof data.speed === 'number' && 
               typeof data.rpm === 'number' && 
               typeof data.gear === 'number';
    }
    
    static sanitizeInput(input, type = 'string') {
        if (type === 'number') {
            const num = parseFloat(input);
            return isNaN(num) ? 0 : num;
        }
        return String(input).trim();
    }
}

// File export utilities
class ExportUtils {
    static exportToCSV(data, filename = 'telemetry_data.csv') {
        if (!data || data.length === 0) {
            console.error('No data to export');
            return;
        }
        
        const headers = Object.keys(data[0]);
        const csvContent = [
            headers.join(','),
            ...data.map(row => 
                headers.map(header => {
                    const value = row[header];
                    return typeof value === 'string' ? `"${value}"` : value;
                }).join(',')
            )
        ].join('\n');
        
        this.downloadFile(csvContent, filename, 'text/csv');
    }
    
    static exportToJSON(data, filename = 'telemetry_data.json') {
        const jsonContent = JSON.stringify(data, null, 2);
        this.downloadFile(jsonContent, filename, 'application/json');
    }
    
    static downloadFile(content, filename, mimeType) {
        const blob = new Blob([content], { type: mimeType });
        const url = window.URL.createObjectURL(blob);
        
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = filename;
        
        document.body.appendChild(a);
        a.click();
        
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    }
}

// Event system for loose coupling
class EventEmitter {
    constructor() {
        this.events = {};
    }
    
    on(event, callback) {
        if (!this.events[event]) {
            this.events[event] = [];
        }
        this.events[event].push(callback);
    }
    
    off(event, callback) {
        if (!this.events[event]) return;
        
        const index = this.events[event].indexOf(callback);
        if (index > -1) {
            this.events[event].splice(index, 1);
        }
    }
    
    emit(event, ...args) {
        if (!this.events[event]) return;
        
        this.events[event].forEach(callback => {
            try {
                callback(...args);
            } catch (error) {
                console.error(`Error in event handler for ${event}:`, error);
            }
        });
    }
    
    once(event, callback) {
        const onceCallback = (...args) => {
            this.off(event, onceCallback);
            callback(...args);
        };
        this.on(event, onceCallback);
    }
}

// Math utilities for calculations
class MathUtils {
    static deg2rad(degrees) {
        return degrees * (Math.PI / 180);
    }
    
    static rad2deg(radians) {
        return radians * (180 / Math.PI);
    }
    
    static calculateLapProgress(currentDistance, trackLength) {
        return Math.max(0, Math.min(1, currentDistance / trackLength));
    }
    
    static calculateSectorTime(sectorDistance, speed) {
        if (speed <= 0) return 0;
        return (sectorDistance / 1000) / (speed / 3600); // Convert to seconds
    }
    
    static calculateFuelConsumption(currentFuel, previousFuel, deltaTime) {
        const consumption = (previousFuel - currentFuel) / (deltaTime / 1000);
        return Math.max(0, consumption); // Fuel consumption per second
    }
    
    static calculateTireWear(temperature, pressure, speed, deltaTime) {
        // Simplified tire wear calculation
        const tempFactor = Math.max(0, (temperature - 80) / 40);
        const pressureFactor = Math.abs(pressure - 23.5) / 5;
        const speedFactor = speed / 300;
        
        return (tempFactor + pressureFactor + speedFactor) * deltaTime * 0.001;
    }
}

// Export utilities for use in other modules
if (typeof window !== 'undefined') {
    window.TelemetryUtils = {
        TimeUtils,
        UnitUtils,
        ColorUtils,
        DataUtils,
        StorageUtils,
        PerformanceUtils,
        ValidationUtils,
        ExportUtils,
        EventEmitter,
        MathUtils
    };
}