# VLauncher

Launcher Android (Kotlin) untuk menjalankan APK di dalam ruang virtual dengan
penyimpanan terpisah per app. Mesin virtualisasi: NewBlackbox (`Bcore`, Apache-2.0).

## Fitur (v0.2.0)

- Pilih APK lewat file picker lalu pasang ke ruang virtual.
- Buka dan hapus app virtual; tiap app punya folder data sendiri.
- Peringatan jika APK hanya punya library 32-bit dan CPU tidak mendukung 32-bit.
- Build APK otomatis lewat GitHub Actions.

## Batasan

- `targetSdk = 28` (syarat mesin), jadi tidak bisa diunggah ke Google Play; hanya sideload.
- CPU tanpa AArch32 tidak bisa menjalankan library `armeabi-v7a`; mesin ini tidak meng-emulasi ARM32.
- Kompatibilitas per game tidak terjamin (Google Play Services, anti-cheat, dll).
- Versi ini belum diuji di perangkat nyata oleh pembuatnya; laporkan masalah beserta logcat.

## Rilis

```bash
git tag v0.2.0 && git push origin v0.2.0
```
