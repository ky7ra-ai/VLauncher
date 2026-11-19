package com.example.vlauncher

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var storage: VirtualStorage
    private lateinit var list: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = VirtualStorage(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        root.addView(TextView(this).apply {
            text = "VLauncher"
            textSize = 24f
            setTypeface(typeface, Typeface.BOLD)
        })

        root.addView(TextView(this).apply {
            text = if (ApkImporter.deviceSupports32Bit()) {
                "CPU mendukung 32-bit."
            } else {
                "CPU ini tidak mendukung 32-bit. APK yang hanya punya library armeabi-v7a tidak bisa dijalankan."
            }
        })

        root.addView(Button(this).apply {
            text = "Tambah APK"
            setOnClickListener { pickApk() }
        })

        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(list) })

        setContentView(root)
        refresh()
    }

    private fun pickApk() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/vnd.android.package-archive", "application/octet-stream"))
        }
        startActivityForResult(intent, REQ_PICK_APK)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_PICK_APK || resultCode != RESULT_OK) return
        val uri = data?.data ?: return

        val info = try {
            ApkImporter.import(this, uri, storage)
        } catch (e: Exception) {
            null
        }

        if (info == null) {
            Toast.makeText(this, "Gagal membaca APK", Toast.LENGTH_LONG).show()
            return
        }
        if (info.only32Bit) {
            Toast.makeText(this, "APK hanya 32-bit: butuh emulasi ARM32", Toast.LENGTH_LONG).show()
        }
        refresh()
    }

    private fun refresh() {
        list.removeAllViews()
        val packages = storage.listPackages()
        if (packages.isEmpty()) {
            list.addView(TextView(this).apply { text = "Belum ada app. Tekan Tambah APK." })
            return
        }
        for (pkg in packages) {
            val mb = storage.usedBytes(pkg) / (1024 * 1024)
            list.addView(TextView(this).apply {
                text = "$pkg  ($mb MB)\n${storage.baseDir(pkg).absolutePath}"
                setPadding(0, 24, 0, 24)
            })
        }
    }

    companion object {
        private const val REQ_PICK_APK = 1001
    }
}
