package io.matthewnelson.topl_service.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.WorkerThread
import io.matthewnelson.topl_core.OnionProxyContext
import io.matthewnelson.topl_core.OnionProxyManager
import io.matthewnelson.topl_service.notification.ServiceNotification
import io.matthewnelson.topl_core_base.TorConfigFiles
import io.matthewnelson.topl_core_base.TorSettings
import io.matthewnelson.topl_service.onionproxy.OnionProxyEventBroadcaster
import io.matthewnelson.topl_service.onionproxy.OnionProxyEventListener
import io.matthewnelson.topl_service.onionproxy.OnionProxyInstaller
import io.matthewnelson.topl_service.service.ActionConsts.ServiceAction
import kotlinx.coroutines.*
import net.freehaven.tor.control.EventListener

internal class TorService: Service() {

    companion object {
        private lateinit var torConfigFiles: TorConfigFiles
        private lateinit var torSettings: TorSettings
        private var additionalEventListener: EventListener? = null
        private var buildConfigVersionCode: Int = -1
        private lateinit var geoipAssetPath: String
        private lateinit var geoip6AssetPath: String

        fun initialize(
            torConfigFiles: TorConfigFiles,
            torSettings: TorSettings,
            additionalEventListener: EventListener?,
            buildConfigVersionCode: Int,
            geoipAssetPath: String,
            geoip6AssetPath: String
        ) {
            this.torConfigFiles = torConfigFiles
            this.torSettings = torSettings
            this.additionalEventListener = additionalEventListener
            this.buildConfigVersionCode = buildConfigVersionCode
            this.geoipAssetPath = geoipAssetPath
            this.geoip6AssetPath = geoip6AssetPath
        }

        // Needed to inhibit all TorServiceController methods except for startTor()
        // from sending working such that startService isn't called and Tor doesn't
        // properly start up.
        var isTorStarted = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        ServiceNotification.get().startForegroundNotification(this)

        // Instantiate TOPL
        val torServiceSettings = TorServiceSettings(torSettings, this)
        val onionProxyInstaller = OnionProxyInstaller(
            this,
            torConfigFiles,
            buildConfigVersionCode,
            geoipAssetPath,
            geoip6AssetPath
        )
        val onionProxyContext = OnionProxyContext(
            torConfigFiles,
            onionProxyInstaller,
            torServiceSettings
        )
        val onionProxyEventBroadcaster = OnionProxyEventBroadcaster(this, torServiceSettings)
        val onionProxyEventListener = OnionProxyEventListener(this, onionProxyEventBroadcaster)
        onionProxyManager = OnionProxyManager(
            this,
            onionProxyContext,
            onionProxyEventBroadcaster,
            onionProxyEventListener,
            arrayOf(additionalEventListener)
        )
    }

    override fun onDestroy() {
        supervisorJob.cancel()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { executeAction(it) }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        executeAction(ServiceAction.ACTION_STOP)
    }

    ////////////
    /// TOPL ///
    ////////////
    private lateinit var onionProxyManager: OnionProxyManager

    /**
     * Do not call directly, use [executeAction].
     * */
    @WorkerThread
    private fun startTor() {
        if (!onionProxyManager.eventBroadcaster.torStateMachine.isOn) {
            try {
                onionProxyManager.setup()
                onionProxyManager.getNewSettingsBuilder()
                    .updateTorSettings()
                    .setGeoIpFiles()
                    .finishAndWriteToTorrcFile()

                onionProxyManager.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Do not call directly, use [executeAction].
     * */
    @WorkerThread
    private fun stopTor() {
        try {
            onionProxyManager.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    ///////////////
    /// Actions ///
    ///////////////
    private val supervisorJob = SupervisorJob()
    val scopeMain = CoroutineScope(Dispatchers.Main + supervisorJob)
    private lateinit var executeActionJob: Job

    /**
     * Route all [ActionConsts.ServiceAction]s here for execution.
     *
     * @param [action] A [ServiceAction]
     * */
    private fun executeAction(@ServiceAction action: String) {
        scopeMain.launch {
            if (::executeActionJob.isInitialized && executeActionJob.isActive) {
                executeActionJob.join()
                delay(100L)
            }

            executeActionJob = launch(Dispatchers.IO) {
                when (action) {
                    ServiceAction.ACTION_START -> {
                        startTor()
                        isTorStarted = true
                    }
                    ServiceAction.ACTION_STOP -> {
                        isTorStarted = false
                        stopTor()

                        // Need a delay before calling stopSelf so that the coroutine which
                        // removes notification actions isn't cancelled via the supervisorJob
                        // being cancelled in onDestroy.
                        delay(200L)
                        stopSelf()
                    }
                    ServiceAction.ACTION_RESTART -> {
                        stopTor()
                        delay(1000L)
                        startTor()
                    }
                    ServiceAction.ACTION_NEW_ID -> {
                        onionProxyManager.signalNewNym()
                    }
                }
            }
        }
    }
}