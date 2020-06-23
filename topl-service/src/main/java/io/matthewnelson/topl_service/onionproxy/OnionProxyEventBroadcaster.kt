package io.matthewnelson.topl_service.onionproxy

import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.matthewnelson.topl_core.broadcaster.DefaultEventBroadcaster
import io.matthewnelson.topl_service.notification.NotificationConsts
import io.matthewnelson.topl_service.notification.NotificationConsts.ImageState
import io.matthewnelson.topl_service.notification.ServiceNotification
import io.matthewnelson.topl_service.service.TorService
import io.matthewnelson.topl_service.service.TorServiceSettings

/**
 * [io.matthewnelson.topl_core.OnionProxyManager] utilizes this customized class for
 * broadcasting things while it is operating (such as Tor's State, operation errors,
 * debugging, etc).
 *
 * [OnionProxyEventListener] utilizes this class by sending it what Tor is spitting out
 * (selectively curated, ofc).
 *
 * @param [torService] [TorService] for context.
 * @param [torSettings] [TorServiceSettings]
 * */
internal class OnionProxyEventBroadcaster(
    private val torService: TorService,
    private val torSettings: TorServiceSettings
): DefaultEventBroadcaster(torSettings) {

    companion object {
        private const val TAG = "EventBroadcaster"
    }

    private val broadcastManager = LocalBroadcastManager.getInstance(torService)

    private var bytesRead = 0L
    private var bytesWritten = 0L
    override fun broadcastBandwidth(bytesRead: String, bytesWritten: String) {
        val read =
            try {
                bytesRead.toLong()
            } catch (e: NumberFormatException) {
                this.bytesRead
            }
        val written =
            try {
                bytesWritten.toLong()
            } catch (e: NumberFormatException) {
                this.bytesWritten
            }

        // Only update the notification if proper State is had.
        if (torState != TorState.ON && torNetworkState != TorNetworkState.ENABLED) return

        if (read != this.bytesRead || written != this.bytesWritten) {
            this.bytesRead = read
            this.bytesWritten = written
            ServiceNotification.get().updateBandwidth(read, written)

            if (read == 0L && written == 0L)
                ServiceNotification.get().updateIcon(torService, ImageState.ON)
            else
                ServiceNotification.get().updateIcon(torService, ImageState.DATA)
        }
    }

    override fun broadcastDebug(msg: String) {
        super.broadcastDebug(msg)
    }

    override fun broadcastException(msg: String?, e: Exception) {
        super.broadcastException(msg, e)
    }

    override fun broadcastLogMessage(logMessage: String?) {
        Log.d(TAG, "LOG_MESSAGE__$logMessage")
    }

    override fun broadcastNotice(msg: String) {
        super.broadcastNotice(msg)
    }

    @TorState var torState: String  = TorState.OFF
        private set
    @TorNetworkState var torNetworkState: String = TorNetworkState.DISABLED
        private set
    override fun broadcastTorState(@TorState state: String, @TorNetworkState networkState: String) {
        if (state != torState)
            ServiceNotification.get().updateContentTitle(state)
            torState = state

        if (networkState != torNetworkState)
            torNetworkState = networkState
            if (networkState == TorNetworkState.DISABLED)
                ServiceNotification.get().updateIcon(torService, ImageState.OFF)
            else
                ServiceNotification.get().updateIcon(torService, ImageState.ON)

        super.broadcastTorState(state, networkState)
    }
}