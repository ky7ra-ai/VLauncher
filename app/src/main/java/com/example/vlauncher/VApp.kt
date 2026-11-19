package com.example.vlauncher

import android.app.Application
import android.content.Context
import android.util.Log
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.app.configuration.ClientConfiguration

/**
 * Inisialisasi mesin virtualisasi (NewBlackbox / Bcore, Apache-2.0).
 * Urutan pemanggilan mengikuti aplikasi contoh milik mesinnya.
 */
class VApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try {
            val core = BlackBoxCore.get()
            core.closeCodeInit()
            core.onBeforeMainApplicationAttach(this, base)
            core.doAttachBaseContext(base, object : ClientConfiguration() {
                override fun getHostPackageName(): String = base.packageName

                override fun isEnableDaemonService(): Boolean = false

                // Mesin bawaan bisa mengunggah logcat + info perangkat ke server
                // pihak ketiga saat crash. ID kosong mematikan fitur itu sepenuhnya.
                override fun getLogSenderChatId(): String = ""
            })
            core.onAfterMainApplicationAttach(this, base)
        } catch (e: Throwable) {
            Log.e(TAG, "attachBaseContext gagal", e)
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            BlackBoxCore.get().doCreate()
        } catch (e: Throwable) {
            Log.e(TAG, "doCreate gagal", e)
        }
    }

    private companion object {
        const val TAG = "VApp"
    }
}
