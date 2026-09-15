/* 윤아리 선생님의 배움 놀이터 - Service Worker
   캐시 버전을 올리면 사용자 기기의 옛 파일이 자동으로 교체됩니다.
   index.html을 수정해 다시 올릴 때마다 아래 CACHE_VERSION 숫자를 +1 하세요. */

const CACHE_VERSION = 'v1';
const CACHE_NAME = `yunari-playground-${CACHE_VERSION}`;

const PRECACHE_URLS = [
  './',
  './index.html',
  './manifest.webmanifest',
  './icon-192.png',
  './icon-512.png',
  './icon-maskable-192.png',
  './icon-maskable-512.png'
];

// 설치: 핵심 파일 미리 캐시
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(PRECACHE_URLS).catch(() => {
        // 일부 파일이 없어도 설치가 실패하지 않도록 개별 시도
        return Promise.all(
          PRECACHE_URLS.map((url) =>
            cache.add(url).catch(() => null)
          )
        );
      }))
      .then(() => self.skipWaiting())
  );
});

// 활성화: 옛 캐시 정리
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(
        keys.filter((key) => key !== CACHE_NAME)
            .map((key) => caches.delete(key))
      ))
      .then(() => self.clients.claim())
  );
});

// 요청 처리
self.addEventListener('fetch', (event) => {
  const request = event.request;

  if (request.method !== 'GET') return;

  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return; // 외부 요청은 그대로 통과

  // HTML 문서: 네트워크 우선 → 실패 시 캐시 (최신 버전 우선 확보)
  if (request.mode === 'navigate' || request.destination === 'document') {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
          return response;
        })
        .catch(() =>
          caches.match(request).then((cached) => cached || caches.match('./index.html'))
        )
    );
    return;
  }

  // 그 외 정적 자원: 캐시 우선 → 없으면 네트워크 후 저장
  event.respondWith(
    caches.match(request).then((cached) => {
      if (cached) return cached;
      return fetch(request)
        .then((response) => {
          if (response && response.status === 200 && response.type === 'basic') {
            const copy = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
          }
          return response;
        })
        .catch(() => cached);
    })
  );
});

// 앱에서 즉시 업데이트를 요청할 때 사용
self.addEventListener('message', (event) => {
  if (event.data === 'SKIP_WAITING') self.skipWaiting();
});
