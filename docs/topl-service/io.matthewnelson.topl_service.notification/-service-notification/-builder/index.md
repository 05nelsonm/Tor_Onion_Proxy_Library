[topl-service](../../../index.md) / [io.matthewnelson.topl_service.notification](../../index.md) / [ServiceNotification](../index.md) / [Builder](./index.md)

# Builder

`class Builder` [(source)](https://github.com/05nelsonm/TorOnionProxyLibrary-Android/blob/master/topl-service/src/main/java/io/matthewnelson/topl_service/notification/ServiceNotification.kt#L148)

Where you get to customize how your notification will look and function.

A notification is required to be displayed while [TorService](#) is running in the
Foreground. Even if you set [Builder.showNotification](show-notification.md) to false, [TorService](#)
is brought to the Foreground when the user removes your task from the recent apps tray
in order to properly shut down Tor and clean up w/o being killed by the OS.

``` kotlin
//  private fun generateTorServiceNotificationBuilder(context: Context): ServiceNotification.Builder {
        return ServiceNotification.Builder(
            channelName = "TOPL-Android Demo",
            channelDescription = "TorOnionProxyLibrary-Android Demo",
            channelID = "TOPL-Android Demo",
            notificationID = 615
        )
            .setImageTorNetworkingEnabled(drawableRes = R.drawable.tor_stat_network_enabled)
            .setImageTorNetworkingDisabled(drawableRes = R.drawable.tor_stat_network_disabled)
            .setImageTorDataTransfer(drawableRes = R.drawable.tor_stat_network_dataxfer)
            .setImageTorErrors(drawableRes = R.drawable.tor_stat_notifyerr)
            .setVisibility(visibility = NotificationCompat.VISIBILITY_PRIVATE)
            .setCustomColor(colorRes = R.color.primaryColor)
            .enableTorRestartButton(enable = true)
            .enableTorStopButton(enable = true)
            .showNotification(show = true)

            // Set the notification's contentIntent for when the user clicks the notification
            .also { builder ->
                context.applicationContext.packageManager
                    ?.getLaunchIntentForPackage(context.applicationContext.packageName)
                    ?.let { intent ->

                        // Set in your manifest for the launch activity so the intent won't launch
                        // a new activity over top of your already created activity if the app is
                        // open when the user clicks the notification:
                        //
                        // android:launchMode="singleInstance"
                        //
                        // For more info on launchMode and Activity Intent flags, see:
                        //
                        // https://medium.com/swlh/truly-understand-tasks-and-back-stack-intent-flags-of-activity-2a137c401eca

                        builder.setContentIntent(
                            PendingIntent.getActivity(
                                context.applicationContext,
                                0, // Your desired request code
                                intent,
                                0 // flags
                            // can also include a bundle if desired
                            )
                        )
                }
            }
//  }
```

### Parameters

`channelName` - Your notification channel's name (Cannot be Empty).

`channelID` - Your notification channel's ID (Cannot be Empty).

`channelDescription` - Your notification channel's description (Cannot be Empty).

`notificationID` - Your foreground notification's ID.

### Exceptions

`IllegalArgumentException` - If String fields are empty.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | Where you get to customize how your notification will look and function.`Builder(channelName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, channelID: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, channelDescription: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, notificationID: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`)` |

### Functions

| Name | Summary |
|---|---|
| [enableTorRestartButton](enable-tor-restart-button.md) | Disabled by Default`fun enableTorRestartButton(enable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true): Builder` |
| [enableTorStopButton](enable-tor-stop-button.md) | Disabled by Default`fun enableTorStopButton(enable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true): Builder` |
| [setActivityToBeOpenedOnTap](set-activity-to-be-opened-on-tap.md) | Do not use this method.`fun ~~setActivityToBeOpenedOnTap~~(clazz: `[`Class`](https://docs.oracle.com/javase/6/docs/api/java/lang/Class.html)`<*>, intentExtrasKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?, intentExtras: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?, intentRequestCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`?): Builder` |
| [setContentIntent](set-content-intent.md) | Allows for full control over the [PendingIntent](https://developer.android.com/reference/android/app/PendingIntent.html) used when the user taps the [ServiceNotification](../index.md).`fun setContentIntent(pendingIntent: `[`PendingIntent`](https://developer.android.com/reference/android/app/PendingIntent.html)`?): Builder` |
| [setContentIntentData](set-content-intent-data.md) | Do not use this method.`fun ~~setContentIntentData~~(bundle: `[`Bundle`](https://developer.android.com/reference/android/os/Bundle.html)`?, requestCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`?): Builder` |
| [setCustomColor](set-custom-color.md) | Defaults to [R.color.tor_service_white](#)`fun setCustomColor(colorRes: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): Builder` |
| [setImageTorDataTransfer](set-image-tor-data-transfer.md) | Defaults to Orbot/TorBrowser's icon [R.drawable.tor_stat_network_dataxfer](#).`fun setImageTorDataTransfer(drawableRes: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): Builder` |
| [setImageTorErrors](set-image-tor-errors.md) | Defaults to Orbot/TorBrowser's icon [R.drawable.tor_stat_notifyerr](#).`fun setImageTorErrors(drawableRes: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): Builder` |
| [setImageTorNetworkingDisabled](set-image-tor-networking-disabled.md) | Defaults to Orbot/TorBrowser's icon [R.drawable.tor_stat_network_disabled](#).`fun setImageTorNetworkingDisabled(drawableRes: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): Builder` |
| [setImageTorNetworkingEnabled](set-image-tor-networking-enabled.md) | Defaults to Orbot/TorBrowser's icon [R.drawable.tor_stat_network_enabled](#).`fun setImageTorNetworkingEnabled(drawableRes: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): Builder` |
| [setVisibility](set-visibility.md) | Defaults to NotificationVisibility.VISIBILITY_SECRET`fun setVisibility(visibility: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): Builder` |
| [showNotification](show-notification.md) | Shown by Default.`fun showNotification(show: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): Builder` |
