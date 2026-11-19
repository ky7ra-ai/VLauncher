# VLauncher

Launcher Android (Kotlin) dengan penyimpanan virtual per app.

## Status (v0.1.0)

Sudah ada:
- Impor APK lewat file picker, disalin ke penyimpanan internal launcher.
- Penyimpanan virtual per paket: `files/virtual/<package>/{data,sdcard,apk}`.
- Deteksi ABI APK dan peringatan jika APK hanya punya library 32-bit.
- Build APK otomatis lewat GitHub Actions (artifact tiap push, Release tiap tag `v*`).

Belum ada:
- Menjalankan APK yang diimpor (butuh DexClassLoader, stub activity, hook framework, redirect I/O).
- Emulasi ARM32 untuk CPU tanpa AArch32.

## Rilis

```bash
git tag v0.1.0 && git push origin v0.1.0
```

APK debug muncul di tab Releases.
