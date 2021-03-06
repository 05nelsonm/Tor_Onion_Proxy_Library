/*
* TorOnionProxyLibrary-Android (a.k.a. topl-android) is a derivation of
* work from the Tor_Onion_Proxy_Library project that started at commit
* hash `74407114cbfa8ea6f2ac51417dda8be98d8aba86`. Contributions made after
* said commit hash are:
*
*     Copyright (C) 2020 Matthew Nelson
*
*     This program is free software: you can redistribute it and/or modify it
*     under the terms of the GNU General Public License as published by the
*     Free Software Foundation, either version 3 of the License, or (at your
*     option) any later version.
*
*     This program is distributed in the hope that it will be useful, but
*     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
*     for more details.
*
*     You should have received a copy of the GNU General Public License
*     along with this program. If not, see <https://www.gnu.org/licenses/>.
*
* `===========================================================================`
* `+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++`
* `===========================================================================`
*
* The following exception is an additional permission under section 7 of the
* GNU General Public License, version 3 (“GPLv3”).
*
*     "The Interfaces" is henceforth defined as Application Programming Interfaces
*     needed to implement TorOnionProxyLibrary-Android, as listed below:
*
*      - From the `topl-core-base` module:
*          - All Classes/methods/variables
*
*      - From the `topl-service-base` module:
*          - All Classes/methods/variables
*
*      - From the `topl-service` module:
*          - The TorServiceController class and it's contained classes/methods/variables
*          - The ServiceNotification.Builder class and it's contained classes/methods/variables
*          - The BackgroundManager.Builder class and it's contained classes/methods/variables
*          - The BackgroundManager.Companion class and it's contained methods/variables
*
*     The following code is excluded from "The Interfaces":
*
*       - All other code
*
*     Linking TorOnionProxyLibrary-Android statically or dynamically with other
*     modules is making a combined work based on TorOnionProxyLibrary-Android.
*     Thus, the terms and conditions of the GNU General Public License cover the
*     whole combination.
*
*     As a special exception, the copyright holder of TorOnionProxyLibrary-Android
*     gives you permission to combine TorOnionProxyLibrary-Android program with free
*     software programs or libraries that are released under the GNU LGPL and with
*     independent modules that communicate with TorOnionProxyLibrary-Android solely
*     through "The Interfaces". You may copy and distribute such a system following
*     the terms of the GNU GPL for TorOnionProxyLibrary-Android and the licenses of
*     the other code concerned, provided that you include the source code of that
*     other code when and as the GNU GPL requires distribution of source code and
*     provided that you do not modify "The Interfaces".
*
*     Note that people who make modified versions of TorOnionProxyLibrary-Android
*     are not obligated to grant this special exception for their modified versions;
*     it is their choice whether to do so. The GNU General Public License gives
*     permission to release a modified version without this exception; this exception
*     also makes it possible to release a modified version which carries forward this
*     exception. If you modify "The Interfaces", this exception does not apply to your
*     modified version of TorOnionProxyLibrary-Android, and you must remove this
*     exception when you distribute your modified version.
* */
package io.matthewnelson.test_helpers.service

import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.WorkerThread
import io.matthewnelson.test_helpers.application_provided_classes.TestEventBroadcaster
import io.matthewnelson.test_helpers.service.components.binding.TestTorServiceBinder
import io.matthewnelson.topl_core.OnionProxyManager
import io.matthewnelson.topl_core.broadcaster.BroadcastLogger
import io.matthewnelson.topl_core_base.BaseConsts.TorNetworkState
import io.matthewnelson.topl_core_base.BaseConsts.TorState
import io.matthewnelson.topl_service.TorServiceController
import io.matthewnelson.topl_service.service.components.onionproxy.ServiceEventBroadcaster
import io.matthewnelson.topl_service.service.components.onionproxy.ServiceEventListener
import io.matthewnelson.topl_service.service.components.onionproxy.ServiceTorInstaller
import io.matthewnelson.topl_service.service.BaseService
import io.matthewnelson.topl_service.service.components.actions.ServiceActionProcessor
import io.matthewnelson.topl_service.service.components.receiver.TorServiceReceiver
import io.matthewnelson.topl_service_base.BaseServiceTorSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineScope
import net.freehaven.tor.control.TorControlCommands
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException

internal class TestTorService(
    private val context: Context,
    dispatcher: CoroutineDispatcher
): BaseService() {

    override fun getContext(): Context {
        return context
    }


    ///////////////
    /// Binding ///
    ///////////////
    val testTorServiceBinder: TestTorServiceBinder by lazy {
            TestTorServiceBinder(this)
    }

    override fun unbindTorService() {
        try {
            unbindService(getContext())
        } catch (e: IllegalArgumentException) {}
    }
    override fun onBind(intent: Intent?): IBinder? {
        return testTorServiceBinder
    }


    /////////////////////////
    /// BroadcastReceiver ///
    /////////////////////////
    val torServiceReceiver by lazy {
        TorServiceReceiver.instantiate(this)
    }
    override fun registerReceiver() {
        torServiceReceiver.register()
    }
    override fun setIsDeviceLocked() {
        torServiceReceiver.setDeviceIsLocked()
    }
    override fun unregisterReceiver() {
        torServiceReceiver.unregister()
    }


    //////////////////
    /// Coroutines ///
    //////////////////
    val supervisorJob = SupervisorJob()
    val testCoroutineScope = TestCoroutineScope(dispatcher + supervisorJob)

    override fun getScopeDefault(): CoroutineScope {
        return testCoroutineScope
    }
    override fun getScopeIO(): CoroutineScope {
        return testCoroutineScope
    }
    override fun getScopeMain(): CoroutineScope {
        return testCoroutineScope
    }


    //////////////////////////////
    /// ServiceActionProcessor ///
    //////////////////////////////
    var stopSelfCalled = false
        private set

    /**
     * In production, this is where `stopSelf()` is called. Need to decouple from
     * the thread it is called on so that functionality of the [ServiceActionProcessor]
     * is simulated properly in that it will stop processing the queue and the Coroutine
     * Job will move to `complete`.
     * */
    override suspend fun stopService() {
        stopSelfCalled = true
        getScopeMain().launch { onDestroy() }
    }


    /////////////////
    /// TOPL-Core ///
    /////////////////
    private val broadcastLogger: BroadcastLogger by lazy {
        getBroadcastLogger(TestTorService::class.java)
    }
    val serviceEventBroadcaster: ServiceEventBroadcaster by lazy {
        ServiceEventBroadcaster.instantiate(this)
    }
    val serviceTorSettings: BaseServiceTorSettings by lazy {
        TorServiceController.getServiceTorSettings()
    }
    val onionProxyManager: OnionProxyManager by lazy {
        OnionProxyManager(
            getContext(),
            TorServiceController.getTorConfigFiles(),
            ServiceTorInstaller.instantiate(this),
            serviceTorSettings,
            ServiceEventListener.instantiate(),
            serviceEventBroadcaster,
            getBuildConfigDebug()
        )
    }

    /**
     * Requires the use of [TorServiceController.Builder.setEventBroadcaster] to set
     * [TestEventBroadcaster] so that [TorServiceController.appEventBroadcaster] is
     * assuredly not null.
     * */
    fun getSimulatedTorStates(): Pair<@TorState String, @TorNetworkState String> {
        val appEventBroadcaster = TorServiceController.appEventBroadcaster!! as TestEventBroadcaster
        return Pair(appEventBroadcaster.torState, appEventBroadcaster.torNetworkState)
    }

    @WorkerThread
    @Throws(IOException::class)
    override fun copyAsset(assetPath: String, file: File) {
        try {
//            FileUtilities.copy(context.assets.open(assetPath), file.outputStream())
        } catch (e: Exception) {
            throw IOException("Failed copying asset from $assetPath", e)
        }
    }
    override fun disableNetwork(disable: Boolean) {
        val networkState = if (disable)
            TorNetworkState.DISABLED
        else
            TorNetworkState.ENABLED
        serviceEventBroadcaster.broadcastTorState(TorState.ON, networkState)
    }
    override fun getBroadcastLogger(clazz: Class<*>): BroadcastLogger {
        return onionProxyManager.getBroadcastLogger(clazz)
    }
    override fun hasNetworkConnectivity(): Boolean {
        return onionProxyManager.hasNetworkConnectivity()
    }
    override fun hasControlConnection(): Boolean {
        return getSimulatedTorStates().first == TorState.ON
    }
    override fun isTorOff(): Boolean {
        return getSimulatedTorStates().first == TorState.OFF
    }
    override fun isTorOn(): Boolean {
        return hasControlConnection()
    }

    var refreshBroadcastLoggerWasCalled = false
    override fun refreshBroadcastLoggersHasDebugLogsVar() {
        onionProxyManager.refreshBroadcastLoggersHasDebugLogsVar()
        refreshBroadcastLoggerWasCalled = true
    }
    override suspend fun signalNewNym() {
        serviceEventBroadcaster.broadcastNotice(
            "${TorControlCommands.SIGNAL_NEWNYM}: ${OnionProxyManager.NEWNYM_SUCCESS_MESSAGE}"
        )
        delay(1000L)
    }
    override fun signalControlConnection(torControlCommand: String): Boolean {
        return true
    }
    @WorkerThread
    override suspend fun startTor() {
        simulateStart()
    }

    val bandwidth1000 = "1000"
    val bandwidth0 = "0"

    // Add 6_000ms delay at start of each test to account for startup time.
    @Suppress("BlockingMethodInNonBlockingContext")
    private fun simulateStart() = getScopeIO().launch {
        try {
            onionProxyManager.setup()
            generateTorrcFile()

            serviceEventBroadcaster.broadcastTorState(TorState.STARTING, TorNetworkState.DISABLED)
            delay(1000)
            serviceEventBroadcaster.broadcastTorState(TorState.ON, TorNetworkState.DISABLED)
            delay(1000)
            serviceEventBroadcaster.broadcastTorState(TorState.ON, TorNetworkState.ENABLED)
            delay(1000)
            serviceEventBroadcaster.broadcastNotice("NOTICE|BaseEventListener|Bootstrapped 95% (")
            delay(1000)
            serviceEventBroadcaster.broadcastNotice("NOTICE|BaseEventListener|Bootstrapped 100% (")
            delay(1000)
            serviceEventBroadcaster.broadcastBandwidth(bandwidth1000, bandwidth1000)
            delay(1000)
            serviceEventBroadcaster.broadcastBandwidth(bandwidth0, bandwidth0)
        } catch (e: Exception) {
            broadcastLogger.exception(e)
        }
    }
    @WorkerThread
    override suspend fun stopTor() {
        simulateStopTor()
    }

    private fun simulateStopTor() = getScopeIO().launch {
        serviceEventBroadcaster.broadcastTorState(TorState.STOPPING, TorNetworkState.ENABLED)
        delay(1000)
        serviceEventBroadcaster.broadcastTorState(TorState.STOPPING, TorNetworkState.DISABLED)
        delay(1000)
        serviceEventBroadcaster.broadcastTorState(TorState.OFF, TorNetworkState.DISABLED)
    }
    @WorkerThread
    @Throws(
        SecurityException::class,
        IllegalAccessException::class,
        IllegalArgumentException::class,
        InvocationTargetException::class,
        NullPointerException::class,
        ExceptionInInitializerError::class,
        IOException::class
    )
    private fun generateTorrcFile() {
        onionProxyManager.getNewSettingsBuilder()
            .updateTorSettings()
//            .setGeoIpFiles()
            .finishAndWriteToTorrcFile()
    }

    override fun onDestroy() {
        super.onDestroy()
//        supervisorJob.cancel()
        removeNotification()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Cancel the BackgroundManager's coroutine if it's active so it doesn't execute
        testTorServiceBinder.cancelExecuteBackgroundPolicyJob()
        super.onTaskRemoved(rootIntent)
    }
}