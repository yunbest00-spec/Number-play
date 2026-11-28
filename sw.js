/* 유나리 배움 놀이터 — 오프라인 지원
   index.html 을 새로 올릴 때마다 VERSION 숫자를 올리세요. */
const VERSION='v13-1';
const CACHE='yunari-'+VERSION;
const CORE=['./','./index.html','./manifest.webmanifest','./icon-192.png','./icon-512.png','./icon-maskable-192.png','./icon-maskable-512.png','./apple-touch-icon.png','./favicon.png'];
self.addEventListener('install',e=>{e.waitUntil(caches.open(CACHE).then(c=>Promise.all(CORE.map(u=>c.add(u).catch(()=>null)))).then(()=>self.skipWaiting()))});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(ks=>Promise.all(ks.filter(k=>k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim()))});
self.addEventListener('fetch',e=>{const r=e.request;if(r.method!=='GET')return;const u=new URL(r.url);if(u.origin!==location.origin)return;
 // 화면(HTML)은 항상 최신 먼저, 안 되면 저장본
 if(r.mode==='navigate'||r.destination==='document'){e.respondWith(fetch(r,{cache:'no-cache'}).then(res=>{const c=res.clone();caches.open(CACHE).then(x=>x.put(r,c));return res}).catch(()=>caches.match(r).then(m=>m||caches.match('./index.html'))));return}
 // 그림·음성 파일은 저장본 먼저
 e.respondWith(caches.match(r).then(m=>m||fetch(r).then(res=>{if(res.ok&&res.type==='basic'){const c=res.clone();caches.open(CACHE).then(x=>x.put(r,c))}return res})))});
