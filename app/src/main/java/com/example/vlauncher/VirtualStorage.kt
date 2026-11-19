package com.example.vlauncher

import android.content.Context
import java.io.File

/**
 * Penyimpanan virtual per app. Setiap paket mendapat folder sendiri
 * di dalam penyimpanan internal launcher:
 *
 *   files/virtual/<package>/data    -> pengganti /data/data/<package>
 *   files/virtual/<package>/sdcard  -> pengganti /sdcard
 *   files/virtual/<package>/apk     -> salinan APK
 */
class VirtualStorage(context: Context) {

    private val root = File(context.filesDir, "virtual").apply { mkdirs() }

    fun baseDir(pkg: String): File = File(root, pkg).apply { mkdirs() }

    fun dataDir(pkg: String): File = File(baseDir(pkg), "data").apply { mkdirs() }

    fun sdcardDir(pkg: String): File = File(baseDir(pkg), "sdcard").apply { mkdirs() }

    fun apkFile(pkg: String): File {
        val dir = File(baseDir(pkg), "apk").apply { mkdirs() }
        return File(dir, "base.apk")
    }

    fun listPackages(): List<String> =
        root.listFiles()?.filter { it.isDirectory }?.map { it.name }?.sorted() ?: emptyList()

    fun usedBytes(pkg: String): Long = baseDir(pkg).walkTopDown().filter { it.isFile }.sumOf { it.length() }

    fun remove(pkg: String): Boolean = baseDir(pkg).deleteRecursively()
}
