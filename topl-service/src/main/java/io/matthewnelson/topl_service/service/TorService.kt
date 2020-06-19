package io.matthewnelson.topl_service.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.matthewnelson.topl_service.model.ServiceNotification
import io.matthewnelson.topl_android_settings.TorConfigFiles
import io.matthewnelson.topl_android_settings.TorSettings
import io.matthewnelson.topl_service.service.ServiceActions.ServiceAction

internal class TorService: Service() {

    companion object {
        private lateinit var torConfigFiles: TorConfigFiles
        private lateinit var torSettings: TorSettings
        private var buildConfigVersion: Int = -1
        private lateinit var geoipAssetPath: String
        private lateinit var geoip6AssetPath: String

        fun initialize(
            config: TorConfigFiles,
            settings: TorSettings,
            buildVersion: Int,
            geoipPath: String,
            geoip6Path: String
        ) {
            torConfigFiles = config
            torSettings = settings
            buildConfigVersion = buildVersion
            geoipAssetPath = geoipPath
            geoip6AssetPath = geoip6Path
        }

        // Intents/LocalBroadcastManager
        const val FILTER = "io.matthewnelson.topl_service.service.TorService"
        const val ACTION_EXTRAS_KEY = "SERVICE_ACTION_EXTRA"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        registerBroadcastReceiver(this)
        ServiceNotification.get().startForegroundNotification(this)
        // TODO: Implement
    }

    override fun onDestroy() {
        unregisterBroadcastReceiver()
        super.onDestroy()
        // TODO: Implement
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // TODO: Implement
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: Implement
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // TODO: Implement
        stopSelf()
    }

    private val actionBR = ActionBroadcastReceiver()
    private lateinit var localBM: LocalBroadcastManager

    private fun registerBroadcastReceiver(torService: TorService) {
        if (!::localBM.isInitialized)
            localBM = LocalBroadcastManager.getInstance(torService)
        localBM.registerReceiver(actionBR, IntentFilter(FILTER))
    }

    private fun unregisterBroadcastReceiver() =
        localBM.unregisterReceiver(actionBR)

    private inner class ActionBroadcastReceiver: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {

                when (intent.getStringExtra(ACTION_EXTRAS_KEY)) {
                    ServiceAction.ACTION_STOP -> {
                        stopSelf()
                    }
                    ServiceAction.ACTION_RESTART -> {
                        // TODO: Implement
                    }
                    ServiceAction.ACTION_NEW_ID -> {
                        // TODO: Implement
                    }
                    else -> {}
                }
            }
        }
    }
}