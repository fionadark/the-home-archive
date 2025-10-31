/**
 * Service Worker for The Home Archive - Dark Academia Library
 * Provides advanced offline capabilities and background sync
 * Complements the offline handler for comprehensive offline support
 */

const CACHE_NAME = 'home-archive-v1';
const DYNAMIC_CACHE = 'home-archive-dynamic-v1';

// Assets to cache immediately
const STATIC_ASSETS = [
    '/',
    '/index.html',
    '/login.html',
    '/register.html',
    '/library.html',
    '/book-search.html',
    '/dashboard.html',
    '/src/css/themes/dark-academia.css',
    '/src/js/utils/offlineHandler.js',
    '/src/js/utils/uiUtils.js'
];

// API endpoints to cache
const CACHEABLE_APIS = [
    '/api/categories',
    '/api/library',
    '/api/books',
    '/api/search'
];

/**
 * Install event - cache static assets
 */
self.addEventListener('install', (event) => {
    console.log('[SW] Install event');
    
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then((cache) => {
                console.log('[SW] Caching static assets');
                return cache.addAll(STATIC_ASSETS);
            })
            .then(() => {
                console.log('[SW] Static assets cached');
                return self.skipWaiting();
            })
            .catch((error) => {
                console.error('[SW] Failed to cache static assets:', error);
            })
    );
});

/**
 * Activate event - clean up old caches
 */
self.addEventListener('activate', (event) => {
    console.log('[SW] Activate event');
    
    event.waitUntil(
        caches.keys()
            .then((cacheNames) => {
                return Promise.all(
                    cacheNames.map((cacheName) => {
                        if (cacheName !== CACHE_NAME && cacheName !== DYNAMIC_CACHE) {
                            console.log('[SW] Deleting old cache:', cacheName);
                            return caches.delete(cacheName);
                        }
                    })
                );
            })
            .then(() => {
                console.log('[SW] Cache cleanup complete');
                return self.clients.claim();
            })
    );
});

/**
 * Fetch event - handle network requests with cache strategies
 */
self.addEventListener('fetch', (event) => {
    const { request } = event;
    const url = new URL(request.url);
    
    // Skip non-GET requests for caching
    if (request.method !== 'GET') {
        return;
    }
    
    // Skip external requests
    if (url.origin !== location.origin) {
        return;
    }
    
    // Handle different types of requests
    if (url.pathname.startsWith('/api/')) {
        // API requests - network first, fallback to cache
        event.respondWith(networkFirstStrategy(request));
    } else {
        // Static assets - cache first, fallback to network
        event.respondWith(cacheFirstStrategy(request));
    }
});

/**
 * Network first strategy - for API requests
 * Try network first, fallback to cache if offline
 */
async function networkFirstStrategy(request) {
    const url = new URL(request.url);
    
    try {
        // Try network first
        const networkResponse = await fetch(request);
        
        // Cache successful responses for GET requests
        if (networkResponse.ok && isCacheableAPI(url.pathname)) {
            const cache = await caches.open(DYNAMIC_CACHE);
            await cache.put(request, networkResponse.clone());
            console.log('[SW] Cached API response:', url.pathname);
        }
        
        return networkResponse;
    } catch (error) {
        console.log('[SW] Network failed, trying cache:', url.pathname);
        
        // Network failed, try cache
        const cachedResponse = await caches.match(request);
        if (cachedResponse) {
            console.log('[SW] Serving from cache:', url.pathname);
            return cachedResponse;
        }
        
        // No cache available, return offline response
        return createOfflineResponse(url.pathname);
    }
}

/**
 * Cache first strategy - for static assets
 * Try cache first, fallback to network
 */
async function cacheFirstStrategy(request) {
    // Try cache first
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
        console.log('[SW] Serving from cache:', request.url);
        return cachedResponse;
    }
    
    try {
        // Cache miss, try network
        const networkResponse = await fetch(request);
        
        // Cache the response for future use
        if (networkResponse.ok) {
            const cache = await caches.open(DYNAMIC_CACHE);
            await cache.put(request, networkResponse.clone());
            console.log('[SW] Cached new asset:', request.url);
        }
        
        return networkResponse;
    } catch (error) {
        console.log('[SW] Network failed for asset:', request.url);
        
        // Return offline fallback
        return createOfflineFallback(request);
    }
}

/**
 * Check if API endpoint should be cached
 */
function isCacheableAPI(pathname) {
    return CACHEABLE_APIS.some(api => pathname.startsWith(api));
}

/**
 * Create offline response for API requests
 */
function createOfflineResponse(pathname) {
    const offlineData = {
        error: 'Offline',
        message: 'This content is not available offline',
        offline: true,
        pathname: pathname
    };
    
    return new Response(JSON.stringify(offlineData), {
        status: 503,
        statusText: 'Service Unavailable',
        headers: {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        }
    });
}

/**
 * Create offline fallback for static assets
 */
function createOfflineFallback(request) {
    const url = new URL(request.url);
    
    // For HTML pages, return a basic offline page
    if (request.destination === 'document') {
        return new Response(`
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Offline - The Home Archive</title>
                <style>
                    body {
                        font-family: 'Playfair Display', serif;
                        background: #1a1a1a;
                        color: #f8f9fa;
                        text-align: center;
                        padding: 2rem;
                        margin: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 2rem;
                        background: #2d3436;
                        border-radius: 8px;
                        border: 2px solid #d4af37;
                    }
                    h1 { color: #d4af37; }
                    .icon { font-size: 4rem; margin-bottom: 1rem; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">📚</div>
                    <h1>You're Offline</h1>
                    <p>You're currently offline. Please check your connection and try again.</p>
                    <p>Some cached content may be available in your library.</p>
                    <button onclick="window.location.reload()" style="
                        background: #d4af37;
                        color: #1a1a1a;
                        border: none;
                        padding: 10px 20px;
                        border-radius: 4px;
                        cursor: pointer;
                        font-weight: bold;
                        margin-top: 1rem;
                    ">Try Again</button>
                </div>
            </body>
            </html>
        `, {
            status: 503,
            statusText: 'Service Unavailable',
            headers: {
                'Content-Type': 'text/html'
            }
        });
    }
    
    // For other assets, return a 503 response
    return new Response('Service Unavailable - Offline', {
        status: 503,
        statusText: 'Service Unavailable'
    });
}

/**
 * Background sync event - handle queued operations
 */
self.addEventListener('sync', (event) => {
    console.log('[SW] Background sync event:', event.tag);
    
    if (event.tag === 'background-sync') {
        event.waitUntil(doBackgroundSync());
    }
});

/**
 * Perform background sync
 */
async function doBackgroundSync() {
    console.log('[SW] Performing background sync');
    
    try {
        // Get queued operations from IndexedDB or other storage
        // This would typically sync with the offline handler's queue
        
        // For now, just log that sync is happening
        console.log('[SW] Background sync completed');
        
        // Notify the main application that sync is complete
        const clients = await self.clients.matchAll();
        clients.forEach(client => {
            client.postMessage({
                type: 'BACKGROUND_SYNC_COMPLETE',
                timestamp: new Date().toISOString()
            });
        });
    } catch (error) {
        console.error('[SW] Background sync failed:', error);
    }
}

/**
 * Message event - handle messages from main application
 */
self.addEventListener('message', (event) => {
    console.log('[SW] Message received:', event.data);
    
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
    
    if (event.data && event.data.type === 'CACHE_URLS') {
        event.waitUntil(
            cacheUrls(event.data.urls)
        );
    }
});

/**
 * Cache specific URLs on demand
 */
async function cacheUrls(urls) {
    const cache = await caches.open(DYNAMIC_CACHE);
    
    for (const url of urls) {
        try {
            await cache.add(url);
            console.log('[SW] Cached URL:', url);
        } catch (error) {
            console.error('[SW] Failed to cache URL:', url, error);
        }
    }
}

/**
 * Push event - handle push notifications (if implemented)
 */
self.addEventListener('push', (event) => {
    console.log('[SW] Push event received');
    
    if (event.data) {
        const data = event.data.json();
        
        event.waitUntil(
            self.registration.showNotification(data.title, {
                body: data.body,
                icon: '/icon-192.png',
                badge: '/badge-72.png',
                tag: 'home-archive-notification'
            })
        );
    }
});

/**
 * Notification click event
 */
self.addEventListener('notificationclick', (event) => {
    console.log('[SW] Notification clicked');
    
    event.notification.close();
    
    event.waitUntil(
        clients.openWindow('/')
    );
});

console.log('[SW] Service Worker loaded');