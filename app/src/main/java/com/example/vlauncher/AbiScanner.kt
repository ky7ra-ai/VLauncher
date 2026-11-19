package com.example.vlauncher

import java.io.File
import java.util.zip.ZipFile

/** Membaca ABI library native yang ada di dalam APK. */
object AbiScanner {

    fun abisOf(apk: File): List<String> =
        ZipFile(apk).use { zip ->
            zip.entries().asSequence()
                .map { it.name }
                .filter { it.startsWith("lib/") && it.endsWith(".so") }
                .map { it.split("/")[1] }
                .distinct()
                .toList()
        }

    /** True jika APK punya library native tetapi tidak ada versi 64-bit. */
    fun only32Bit(abis: List<String>): Boolean =
        abis.isNotEmpty() && abis.none { it == "arm64-v8a" || it == "x86_64" }
}
