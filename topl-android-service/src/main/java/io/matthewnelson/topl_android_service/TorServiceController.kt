package io.matthewnelson.topl_android_service

import android.content.Context
import android.content.IntentFilter
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import io.matthewnelson.topl_android_service.model.NotificationSettings
import io.matthewnelson.topl_android_settings.TorConfigFiles
import io.matthewnelson.topl_android_settings.TorSettings

class TorServiceController private constructor() {

    class Builder(
        private val context: Context,
        private val buildConfigVersion: Int,
        private val torSettings: TorSettings,
        private val geoipAssetPath: String,
        private val geoip6AssetPath: String
    ) {

        private lateinit var torConfig: TorConfigFiles
        private lateinit var startServiceAsap: String
        private lateinit var stopServiceOnTermination: String

        fun useCustomTorConfigFiles(torConfigFiles: TorConfigFiles): Builder {
            torConfig = torConfigFiles
            return this
        }

        fun startServiceAsSoonAsPossible(): Builder {
            startServiceAsap = ""
            return this
        }

        fun stopServiceWhenAppIsTerminated(): Builder {
            stopServiceOnTermination = ""
            return this
        }

        fun showNotification(channelDescription: String, notificationID: Short): NotificationBuilder {
            return NotificationBuilder(this, channelDescription, notificationID.toInt())
        }

        class NotificationBuilder(
            private val builder: Builder,
            channelDescription: String,
            notificationID: Int
        ) {

            private val notificationSettings = NotificationSettings(channelDescription, notificationID)

            fun setNotificationImageTorOn(@DrawableRes drawableRes: Int): NotificationBuilder {
                notificationSettings.imageOn = drawableRes
                return this
            }

            fun setNotificationImageTorOff(@DrawableRes drawableRes: Int): NotificationBuilder {
                notificationSettings.imageOff = drawableRes
                return this
            }

            fun setNotificationImageDataTransfer(@DrawableRes drawableRes: Int): NotificationBuilder {
                notificationSettings.imageData = drawableRes
                return this
            }

            fun setNotificationImageErrors(@DrawableRes drawableRes: Int): NotificationBuilder {
                notificationSettings.imageError = drawableRes
                return this
            }

            fun setNotificationColor(@ColorRes colorRes: Int): NotificationBuilder {
                notificationSettings.colorRes = colorRes
                return this
            }

            fun enableRestartButton(): NotificationBuilder {
                notificationSettings.enableRestartButton = true
                return this
            }

            fun enableStopButton(): NotificationBuilder {
                notificationSettings.enableStopButton = true
                return this
            }

            fun applyNotificationSettings(): Builder {
                NotificationSettings.initialize(notificationSettings)
                return builder
            }

        }

        fun build() {
            TorServiceManager.getInstance(context.applicationContext).initialize(
                torSettings,
                buildConfigVersion,
                geoipAssetPath,
                geoip6AssetPath,
                if (::torConfig.isInitialized) torConfig else null,
                ::startServiceAsap.isInitialized,
                ::stopServiceOnTermination.isInitialized
            )
        }
    }

    companion object {

        /**
         * Starts the TorService. Does nothing if called before the builder has gone off.
         * */
        fun startTor() =
            TorServiceManager.getInstance()?.startTor()

        /**
         * Stops the TorService. Does nothing if called before the builder has gone off.
         * */
        fun stopTor() =
            TorServiceManager.getInstance()?.stopTor()

        /**
         * Restarts the TorService. Does nothing if called before the builder has gone off.
         * */
        fun restartTor() =
            TorServiceManager.getInstance()?.restartTor()

        /**
         * Renews the identity. Does nothing if called before the builder has gone off.
         * */
        fun newIdentity() =
            TorServiceManager.getInstance()?.newIdentity()
    }
}