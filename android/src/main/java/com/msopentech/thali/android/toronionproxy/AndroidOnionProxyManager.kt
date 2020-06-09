/*
Copyright (C) 2011-2014 Sublime Software Ltd

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

THIS CODE IS PROVIDED ON AN *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED,
INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE,
MERCHANTABLITY OR NON-INFRINGEMENT.

See the Apache 2 License for the specific language governing permissions and limitations under the License.
*/
package com.msopentech.thali.android.toronionproxy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.msopentech.thali.universal.toronionproxy.*
import net.freehaven.tor.control.EventHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

class AndroidOnionProxyManager(
    private val context: Context,
    torConfig: TorConfig,
    torInstaller: TorInstaller,
    settings: TorSettings?,
    eventBroadcaster: EventBroadcaster?,
    eventHandler: EventHandler?
) : OnionProxyManager(
    OnionProxyContext(torConfig, torInstaller, settings),
    eventBroadcaster,
    eventHandler
) {

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(AndroidOnionProxyManager::class.java)
    }

    @Volatile
    private var networkStateReceiver: BroadcastReceiver? = null

    @Synchronized
    @Throws(IOException::class)
    override fun start() {
        super.start()
        // Register to receive network status events
        networkStateReceiver = NetworkStateReceiver()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkStateReceiver, filter)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun stop() {
        try {
            super.stop()
        } finally {
            if (networkStateReceiver == null) return

            try {
                context.unregisterReceiver(networkStateReceiver)
            } catch (e: IllegalArgumentException) {
                // There is a race condition where if someone calls stop before
                // installAndStartTorOp is done then we could get an exception because
                // the network state receiver might not be properly registered.
                LOG.info("Someone tried to call stop before registering the receiver finished", e)
            }
        }
    }

    private inner class NetworkStateReceiver : BroadcastReceiver() {

        override fun onReceive(ctx: Context, i: Intent) {

            Thread(Runnable {
                if (!isRunning) return@Runnable

                var online = !i.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
                if (online) {
                    // Some devices fail to set EXTRA_NO_CONNECTIVITY, double check
                    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val net = cm.activeNetworkInfo
                    if (net == null || !net.isConnected)
                        online = false
                }
                LOG.info("Online: $online")

                try {
                    enableNetwork(online)
                } catch (e: IOException) {
                    LOG.warn(e.toString(), e)
                }

            }).start()
        }
    }
}