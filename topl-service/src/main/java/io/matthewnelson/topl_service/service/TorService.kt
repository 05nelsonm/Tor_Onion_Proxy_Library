package io.matthewnelson.topl_service.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.WorkerThread
import io.matthewnelson.topl_core.OnionProxyManager
import io.matthewnelson.topl_core.broadcaster.BroadcastLogger
import io.matthewnelson.topl_service.notification.ServiceNotification
import io.matthewnelson.topl_core_base.TorConfigFiles
import io.matthewnelson.topl_core_base.TorSettings
import io.matthewnelson.topl_service.BuildConfig
import io.matthewnelson.topl_service.onionproxy.ServiceEventBroadcaster
import io.matthewnelson.topl_service.onionproxy.ServiceEventListener
import io.matthewnelson.topl_service.onionproxy.ServiceTorInstaller
import io.matthewnelson.topl_service.onionproxy.ServiceTorSettings
import io.matthewnelson.topl_service.util.ServiceConsts.ServiceAction
import kotlinx.coroutines.*

internal class TorService: Service() {

    companion object {
        private lateinit var torConfigFiles: TorConfigFiles
        private lateinit var torSettings: TorSettings
        private var buildConfigVersionCode: Int = -1
        private var buildConfigDebug: Boolean? = null
        private lateinit var geoipAssetPath: String
        private lateinit var geoip6AssetPath: String

        fun initialize(
            torConfigFiles: TorConfigFiles,
            torSettings: TorSettings,
            buildConfigVersionCode: Int,
            buildConfigDebug: Boolean,
            geoipAssetPath: String,
            geoip6AssetPath: String
        ) {
            this.torConfigFiles = torConfigFiles
            this.torSettings = torSettings
            this.buildConfigVersionCode = buildConfigVersionCode
            this.buildConfigDebug = buildConfigDebug
            this.geoipAssetPath = geoipAssetPath
            this.geoip6AssetPath = geoip6AssetPath
        }

        // Needed to inhibit all TorServiceController methods except for startTor()
        // from sending working such that startService isn't called and Tor doesn't
        // properly start up.
        var isTorStarted = false
            private set
    }

    private lateinit var broadcastLogger: BroadcastLogger

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        ServiceNotification.get().startForegroundNotification(this)
        initTOPLCore()
        broadcastLogger = onionProxyManager.createBroadcastLogger(TorService::class.java)
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

    /////////////////
    /// TOPL-Core ///
    /////////////////
    private lateinit var onionProxyManager: OnionProxyManager

    private fun initTOPLCore() {
        val serviceTorSettings = ServiceTorSettings(torSettings, this)
        val serviceTorInstaller = ServiceTorInstaller(
            this,
            torConfigFiles,
            buildConfigVersionCode,
            buildConfigDebug ?: BuildConfig.DEBUG,
            geoipAssetPath,
            geoip6AssetPath
        )
        val serviceEventBroadcaster = ServiceEventBroadcaster(this, serviceTorSettings)
        val serviceEventListener = ServiceEventListener(this, serviceEventBroadcaster)
        onionProxyManager = OnionProxyManager(
            this,
            torConfigFiles,
            serviceTorInstaller,
            serviceTorSettings,
            serviceEventListener,
            serviceEventBroadcaster,
            buildConfigDebug
        )
    }

    /**
     * Do not call directly, use [executeAction].
     * */
    @WorkerThread
    private fun startTor() {
        if (!onionProxyManager.torStateMachine.isOn) {
            try {
                onionProxyManager.setup()
                onionProxyManager.getNewSettingsBuilder()
                    .updateTorSettings()
                    .setGeoIpFiles()
                    .finishAndWriteToTorrcFile()

                onionProxyManager.start()
            } catch (e: Exception) {
                broadcastLogger.exception(e)
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
            broadcastLogger.exception(e)
        }
    }


    ///////////////
    /// Actions ///
    ///////////////
    private val supervisorJob = SupervisorJob()
    val scopeMain = CoroutineScope(Dispatchers.Main + supervisorJob)
    private lateinit var executeActionJob: Job

    /**
     * Route all [ServiceAction]s here for execution.
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