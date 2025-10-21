// Reusable UI Components for F1 Telemetry App

// Base Component class
class Component {
    constructor(element) {
        this.element = element;
        this.state = {};
        this.init();
    }
    
    init() {
        // Override in subclasses
    }
    
    setState(newState) {
        this.state = { ...this.state, ...newState };
        this.render();
    }
    
    render() {
        // Override in subclasses
    }
    
    destroy() {
        if (this.element && this.element.parentNode) {
            this.element.parentNode.removeChild(this.element);
        }
    }
}

// Circular Gauge Component
class CircularGauge extends Component {
    constructor(container, options = {}) {
        super(container);
        this.options = {
            size: 120,
            strokeWidth: 8,
            min: 0,
            max: 100,
            value: 0,
            color: '#00FF41',
            backgroundColor: 'rgba(255, 255, 255, 0.2)',
            showValue: true,
            unit: '',
            ...options
        };
    }
    
    init() {
        this.createSVG();
        this.render();
    }
    
    createSVG() {
        const { size, strokeWidth } = this.options;
        const radius = (size - strokeWidth) / 2;
        const circumference = 2 * Math.PI * radius;
        
        this.element.innerHTML = `
            <div class="circular-gauge" style="width: ${size}px; height: ${size}px; position: relative;">
                <svg width="${size}" height="${size}" style="transform: rotate(-90deg);">
                    <circle
                        cx="${size / 2}"
                        cy="${size / 2}"
                        r="${radius}"
                        stroke="${this.options.backgroundColor}"
                        stroke-width="${strokeWidth}"
                        fill="none"
                    />
                    <circle
                        class="gauge-progress"
                        cx="${size / 2}"
                        cy="${size / 2}"
                        r="${radius}"
                        stroke="${this.options.color}"
                        stroke-width="${strokeWidth}"
                        fill="none"
                        stroke-linecap="round"
                        stroke-dasharray="${circumference}"
                        stroke-dashoffset="${circumference}"
                        style="transition: stroke-dashoffset 0.3s ease;"
                    />
                </svg>
                ${this.options.showValue ? `
                    <div class="gauge-value" style="
                        position: absolute;
                        top: 50%;
                        left: 50%;
                        transform: translate(-50%, -50%);
                        font-size: ${size * 0.15}px;
                        font-weight: bold;
                        color: white;
                        text-align: center;
                    ">
                        <div class="value-number">${this.options.value}</div>
                        <div class="value-unit" style="font-size: ${size * 0.08}px; color: #ccc;">${this.options.unit}</div>
                    </div>
                ` : ''}
            </div>
        `;
        
        this.progressCircle = this.element.querySelector('.gauge-progress');
        this.valueElement = this.element.querySelector('.value-number');
        this.circumference = circumference;
    }
    
    setValue(value) {
        this.options.value = Math.max(this.options.min, Math.min(this.options.max, value));
        this.render();
    }
    
    render() {
        const { min, max, value } = this.options;
        const percentage = (value - min) / (max - min);
        const offset = this.circumference * (1 - percentage);
        
        if (this.progressCircle) {
            this.progressCircle.style.strokeDashoffset = offset;
        }
        
        if (this.valueElement) {
            this.valueElement.textContent = Math.round(value);
        }
    }
}

// Linear Bar Gauge Component
class LinearGauge extends Component {
    constructor(container, options = {}) {
        super(container);
        this.options = {
            width: 40,
            height: 120,
            orientation: 'vertical',
            min: 0,
            max: 100,
            value: 0,
            color: '#00FF41',
            backgroundColor: 'rgba(255, 255, 255, 0.1)',
            borderRadius: 8,
            showValue: false,
            ...options
        };
    }
    
    init() {
        this.create();
        this.render();
    }
    
    create() {
        const { width, height, orientation, backgroundColor, borderRadius } = this.options;
        const isVertical = orientation === 'vertical';
        
        this.element.innerHTML = `
            <div class="linear-gauge" style="
                width: ${isVertical ? width : height}px;
                height: ${isVertical ? height : width}px;
                background: ${backgroundColor};
                border-radius: ${borderRadius}px;
                position: relative;
                overflow: hidden;
            ">
                <div class="gauge-fill" style="
                    position: absolute;
                    ${isVertical ? 'bottom: 0; left: 0; width: 100%;' : 'left: 0; top: 0; height: 100%;'}
                    background: ${this.options.color};
                    border-radius: ${isVertical ? `0 0 ${borderRadius}px ${borderRadius}px` : `${borderRadius}px 0 0 ${borderRadius}px`};
                    transition: ${isVertical ? 'height' : 'width'} 0.1s linear;
                "></div>
                ${this.options.showValue ? `
                    <div class="gauge-value" style="
                        position: absolute;
                        top: 50%;
                        left: 50%;
                        transform: translate(-50%, -50%);
                        font-size: 12px;
                        font-weight: bold;
                        color: white;
                        text-shadow: 1px 1px 2px rgba(0,0,0,0.8);
                    ">${this.options.value}</div>
                ` : ''}
            </div>
        `;
        
        this.fillElement = this.element.querySelector('.gauge-fill');
        this.valueElement = this.element.querySelector('.gauge-value');
    }
    
    setValue(value) {
        this.options.value = Math.max(this.options.min, Math.min(this.options.max, value));
        this.render();
    }
    
    render() {
        const { min, max, value, orientation } = this.options;
        const percentage = ((value - min) / (max - min)) * 100;
        
        if (this.fillElement) {
            if (orientation === 'vertical') {
                this.fillElement.style.height = `${percentage}%`;
            } else {
                this.fillElement.style.width = `${percentage}%`;
            }
        }
        
        if (this.valueElement) {
            this.valueElement.textContent = Math.round(value);
        }
    }
}

// Temperature Grid Component
class TemperatureGrid extends Component {
    constructor(container, options = {}) {
        super(container);
        this.options = {
            labels: ['FL', 'FR', 'RL', 'RR'],
            values: [0, 0, 0, 0],
            unit: '°C',
            optimalRange: [90, 105],
            ...options
        };
    }
    
    init() {
        this.create();
        this.render();
    }
    
    create() {
        this.element.innerHTML = `
            <div class="temperature-grid" style="
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 8px;
                width: 100%;
            ">
                ${this.options.labels.map((label, index) => `
                    <div class="temp-item" data-index="${index}" style="
                        background: rgba(255, 255, 255, 0.05);
                        padding: 8px;
                        border-radius: 6px;
                        text-align: center;
                        transition: background-color 0.3s ease;
                    ">
                        <div class="temp-label" style="
                            font-size: 10px;
                            color: #ccc;
                            margin-bottom: 4px;
                        ">${label}</div>
                        <div class="temp-value" style="
                            font-size: 14px;
                            font-weight: bold;
                            transition: color 0.3s ease;
                        ">${this.options.values[index]}${this.options.unit}</div>
                    </div>
                `).join('')}
            </div>
        `;
    }
    
    setValues(values) {
        this.options.values = values;
        this.render();
    }
    
    render() {
        const items = this.element.querySelectorAll('.temp-item');
        const { values, unit, optimalRange } = this.options;
        
        items.forEach((item, index) => {
            const value = values[index];
            const valueElement = item.querySelector('.temp-value');
            const temp = Math.round(value);
            
            valueElement.textContent = `${temp}${unit}`;
            
            // Color coding based on temperature
            if (temp < optimalRange[0]) {
                valueElement.style.color = '#00B8FF'; // Cold - blue
                item.style.backgroundColor = 'rgba(0, 184, 255, 0.1)';
            } else if (temp > optimalRange[1]) {
                valueElement.style.color = '#FF3333'; // Hot - red
                item.style.backgroundColor = 'rgba(255, 51, 51, 0.1)';
            } else {
                valueElement.style.color = '#00FF41'; // Optimal - green
                item.style.backgroundColor = 'rgba(0, 255, 65, 0.1)';
            }
        });
    }
}

// Rev Lights Component
class RevLights extends Component {
    constructor(container, options = {}) {
        super(container);
        this.options = {
            count: 10,
            rpm: 0,
            maxRpm: 12000,
            redlineStart: 0.8,
            flashThreshold: 0.95,
            ...options
        };
        this.isFlashing = false;
    }
    
    init() {
        this.create();
        this.render();
    }
    
    create() {
        this.element.innerHTML = `
            <div class="rev-lights" style="
                display: flex;
                gap: 4px;
                justify-content: center;
            ">
                ${Array(this.options.count).fill(0).map((_, index) => `
                    <div class="rev-light" data-index="${index}" style="
                        width: 16px;
                        height: 16px;
                        border-radius: 2px;
                        background: rgba(255, 255, 255, 0.2);
                        transition: background-color 0.1s ease;
                    "></div>
                `).join('')}
            </div>
        `;
        
        this.lights = this.element.querySelectorAll('.rev-light');
    }
    
    setRPM(rpm) {
        this.options.rpm = rpm;
        this.render();
    }
    
    render() {
        const { rpm, maxRpm, count, redlineStart, flashThreshold } = this.options;
        const rpmRatio = rpm / maxRpm;
        const activeLights = Math.floor(rpmRatio * count);
        const shouldFlash = rpmRatio >= flashThreshold;
        
        // Handle flashing
        if (shouldFlash && !this.isFlashing) {
            this.startFlashing();
        } else if (!shouldFlash && this.isFlashing) {
            this.stopFlashing();
        }
        
        this.lights.forEach((light, index) => {
            light.style.background = 'rgba(255, 255, 255, 0.2)';
            
            if (index < activeLights) {
                const lightRatio = index / count;
                
                if (lightRatio >= redlineStart) {
                    light.style.background = '#FF3333'; // Red
                } else if (lightRatio >= 0.6) {
                    light.style.background = '#FFD700'; // Yellow
                } else {
                    light.style.background = '#00FF41'; // Green
                }
            }
        });
    }
    
    startFlashing() {
        this.isFlashing = true;
        this.flashInterval = setInterval(() => {
            this.lights.forEach(light => {
                if (light.style.background !== 'rgba(255, 255, 255, 0.2)') {
                    light.style.opacity = light.style.opacity === '0.3' ? '1' : '0.3';
                }
            });
        }, 200);
    }
    
    stopFlashing() {
        this.isFlashing = false;
        if (this.flashInterval) {
            clearInterval(this.flashInterval);
        }
        this.lights.forEach(light => {
            light.style.opacity = '1';
        });
    }
    
    destroy() {
        this.stopFlashing();
        super.destroy();
    }
}

// Status Indicator Component
class StatusIndicator extends Component {
    constructor(container, options = {}) {
        super(container);
        this.options = {
            label: 'Status',
            status: 'inactive',
            statuses: {
                inactive: { color: '#666', label: 'OFF' },
                active: { color: '#00FF41', label: 'ON' },
                warning: { color: '#FFD700', label: 'WARNING' },
                error: { color: '#FF3333', label: 'ERROR' }
            },
            ...options
        };
    }
    
    init() {
        this.create();
        this.render();
    }
    
    create() {
        this.element.innerHTML = `
            <div class="status-indicator" style="
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 4px 8px;
                border-radius: 12px;
                background: rgba(0, 0, 0, 0.3);
                font-size: 11px;
                font-weight: bold;
            ">
                <div class="status-dot" style="
                    width: 8px;
                    height: 8px;
                    border-radius: 50%;
                    transition: background-color 0.3s ease;
                "></div>
                <span class="status-label">${this.options.label}</span>
                <span class="status-value"></span>
            </div>
        `;
        
        this.dot = this.element.querySelector('.status-dot');
        this.valueElement = this.element.querySelector('.status-value');
    }
    
    setStatus(status) {
        this.options.status = status;
        this.render();
    }
    
    render() {
        const { status, statuses } = this.options;
        const statusConfig = statuses[status] || statuses.inactive;
        
        this.dot.style.backgroundColor = statusConfig.color;
        this.valueElement.textContent = statusConfig.label;
        this.valueElement.style.color = statusConfig.color;
    }
}

// Mini Chart Component for data visualization
class MiniChart extends Component {
    constructor(container, options = {}) {
        super(container);
        this.options = {
            width: 100,
            height: 40,
            maxDataPoints: 50,
            color: '#00FF41',
            backgroundColor: 'rgba(255, 255, 255, 0.05)',
            showGrid: false,
            ...options
        };
        this.data = [];
    }
    
    init() {
        this.create();
        this.render();
    }
    
    create() {
        const { width, height, backgroundColor } = this.options;
        
        this.element.innerHTML = `
            <div class="mini-chart" style="
                width: ${width}px;
                height: ${height}px;
                background: ${backgroundColor};
                border-radius: 4px;
                position: relative;
                overflow: hidden;
            ">
                <canvas width="${width}" height="${height}" style="
                    position: absolute;
                    top: 0;
                    left: 0;
                "></canvas>
            </div>
        `;
        
        this.canvas = this.element.querySelector('canvas');
        this.ctx = this.canvas.getContext('2d');
    }
    
    addDataPoint(value) {
        this.data.push(value);
        
        if (this.data.length > this.options.maxDataPoints) {
            this.data.shift();
        }
        
        this.render();
    }
    
    setData(data) {
        this.data = data.slice(-this.options.maxDataPoints);
        this.render();
    }
    
    render() {
        const { width, height, color } = this.options;
        const { ctx, data } = this;
        
        // Clear canvas
        ctx.clearRect(0, 0, width, height);
        
        if (data.length < 2) return;
        
        // Calculate min/max for scaling
        const min = Math.min(...data);
        const max = Math.max(...data);
        const range = max - min || 1;
        
        // Draw line
        ctx.strokeStyle = color;
        ctx.lineWidth = 2;
        ctx.beginPath();
        
        data.forEach((value, index) => {
            const x = (index / (data.length - 1)) * width;
            const y = height - ((value - min) / range) * height;
            
            if (index === 0) {
                ctx.moveTo(x, y);
            } else {
                ctx.lineTo(x, y);
            }
        });
        
        ctx.stroke();
    }
}

// Export components for global use
if (typeof window !== 'undefined') {
    window.TelemetryComponents = {
        Component,
        CircularGauge,
        LinearGauge,
        TemperatureGrid,
        RevLights,
        StatusIndicator,
        MiniChart
    };
}