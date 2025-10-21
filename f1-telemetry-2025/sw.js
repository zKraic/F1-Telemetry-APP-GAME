// Service Worker for F1 Telemetry PWA
const CACHE_NAME = 'f1-telemetry-v1.0.0';

self.addEventListener('install', (event) => {
    console.log('Service Worker installed');
});

self.addEventListener('fetch', (event) => {
    console.log('Fetch intercepted');
});)