package com.example.vlauncher

import android.content.Context
import android.net.Uri
import android.os.Build
import java.io.File
import java.util.zip.ZipFile

data class ApkInfo(
    val packageName: String,
    val label: String,
    val abis: List<String>,
    val hasNativeLibs: Boolean,
) {
    /** True jika APK hanya punya library 32-bit (butuh emulasi di CPU tanpa AArch32). */
    val only32Bit: Boolean
        get() = hasNativeLibs && abis.none { it == "arm64-v8a" }
}

object ApkImporter {

    /** Salin APK dari Uri (hasil file picker) ke penyimpanan virtual, lalu baca infonya. */
    fun import(context: Context, uri: Uri, storage: VirtualStorage): ApkInfo? {
        val temp = File(context.cacheDir, "import.apk")
        context.contentResolver.openInputStream(uri)?.use { input ->
            temp.outputStream().use { input.copyTo(it) }
        } ?: return null

        val pm = context.packageManager
        val pkgInfo = pm.getPackageArchiveInfo(temp.absolutePath, 0) ?: return null
        val appInfo = pkgInfo.applicationInfo ?: return null
        appInfo.sourceDir = temp.absolutePath
        appInfo.publicSourceDir = temp.absolutePath

        val pkg = pkgInfo.packageName
        val label = appInfo.loadLabel(pm).toString()

        val dest = storage.apkFile(pkg)
        temp.copyTo(dest, overwrite = true)
        temp.delete()
        storage.dataDir(pkg)
        storage.sdcardDir(pkg)

        val abis = readAbis(dest)
        return ApkInfo(pkg, label, abis, abis.isNotEmpty())
    }

    private fun readAbis(apk: File): List<String> =
        ZipFile(apk).use { zip ->
            zip.entries().asSequence()
                .map { it.name }
                .filter { it.startsWith("lib/") && it.endsWith(".so") }
                .map { it.split("/")[1] }
                .distinct()
                .toList()
        }

    fun deviceSupports32Bit(): Boolean = Build.SUPPORTED_32_BIT_ABIS.isNotEmpty()
}
