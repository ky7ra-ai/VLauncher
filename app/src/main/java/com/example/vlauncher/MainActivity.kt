package com.example.vlauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import kotlin.concurrent.thread
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.core.env.BEnvironment

class MainActivity : Activity() {

    private val ui = Handler(Looper.getMainLooper())
    private lateinit var status: TextView
    private lateinit var list: LinearLayout

    private val userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        root.addView(TextView(this).apply {
            text = "VLauncher"
            textSize = 24f
        })

        status = TextView(this).apply { setPadding(0, 8, 0, 16) }
        root.addView(status)

        root.addView(Button(this).apply {
            text = "Tambah APK"
            setOnClickListener { pickApk() }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            root.addView(Button(this).apply {
                text = "Izinkan akses semua file (untuk sdcard virtual)"
                setOnClickListener { requestAllFilesAccess() }
            })
        }

        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(list) })

        setContentView(root)
        showDeviceInfo()
        refresh()
    }

    private fun showDeviceInfo() {
        val has32 = Build.SUPPORTED_32_BIT_ABIS.isNotEmpty()
        status.text = if (has32) {
            "CPU ini mendukung 32-bit dan 64-bit."
        } else {
            "CPU ini hanya 64-bit. APK yang hanya punya library 32-bit " +
                "(armeabi-v7a) tidak akan bisa jalan."
        }
    }

    private fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        if (Environment.isExternalStorageManager()) {
            toast("Akses semua file sudah diizinkan")
            return
        }
        try {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName"),
                ),
            )
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
    }

    private fun pickApk() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, REQ_PICK_APK)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_PICK_APK || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        installFrom(uri)
    }

    private fun installFrom(uri: Uri) {
        toast("Memasang APK...")
        thread {
            val message = try {
                val temp = File(cacheDir, "import.apk")
                contentResolver.openInputStream(uri)?.use { input ->
                    temp.outputStream().use { input.copyTo(it) }
                } ?: throw IllegalStateException("Tidak bisa membuka file")

                val abis = AbiScanner.abisOf(temp)
                val warn = if (AbiScanner.only32Bit(abis) && Build.SUPPORTED_32_BIT_ABIS.isEmpty()) {
                    "\nPeringatan: APK hanya 32-bit dan CPU ini tidak mendukung 32-bit."
                } else {
                    ""
                }

                val core = BlackBoxCore.get()
                if (core.users.none { it.id == userId }) core.createUser(userId)
                val result = core.installPackageAsUser(temp, userId)
                temp.delete()

                if (result.success) {
                    "Terpasang: ${result.packageName}$warn"
                } else {
                    "Gagal memasang: ${result.msg}"
                }
            } catch (e: Throwable) {
                "Gagal memasang: ${e.message}"
            }
            ui.post {
                toast(message)
                refresh()
            }
        }
    }

    private fun refresh() {
        list.removeAllViews()
        thread {
            val apps: List<ApplicationInfo> = try {
                BlackBoxCore.get().getInstalledApplications(0, userId) ?: emptyList()
            } catch (e: Throwable) {
                emptyList()
            }
            ui.post { render(apps) }
        }
    }

    private fun render(apps: List<ApplicationInfo>) {
        list.removeAllViews()
        if (apps.isEmpty()) {
            list.addView(TextView(this).apply { text = "Belum ada app. Tekan Tambah APK." })
            return
        }
        for (app in apps) {
            val pkg = app.packageName
            val label = try {
                BlackBoxCore.getPackageManager().getApplicationLabel(app).toString()
            } catch (e: Throwable) {
                pkg
            }
            val storage = try {
                BEnvironment.getDataDir(pkg, userId).absolutePath
            } catch (e: Throwable) {
                "-"
            }

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 24, 0, 24)
            }
            row.addView(TextView(this).apply {
                text = "$label\n$pkg\n$storage"
            })
            val buttons = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.START
            }
            buttons.addView(Button(this).apply {
                text = "Buka"
                setOnClickListener { launch(pkg) }
            })
            buttons.addView(Button(this).apply {
                text = "Hapus"
                setOnClickListener { remove(pkg) }
            })
            row.addView(buttons)
            list.addView(row)
        }
    }

    private fun launch(pkg: String) {
        thread {
            val ok = try {
                BlackBoxCore.get().launchApk(pkg, userId)
            } catch (e: Throwable) {
                false
            }
            ui.post { if (!ok) toast("Gagal membuka $pkg") }
        }
    }

    private fun remove(pkg: String) {
        thread {
            try {
                BlackBoxCore.get().uninstallPackageAsUser(pkg, userId)
            } catch (e: Throwable) {
                ui.post { toast("Gagal menghapus: ${e.message}") }
            }
            ui.post { refresh() }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    private companion object {
        const val REQ_PICK_APK = 1001
    }
}
