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
package io.matthewnelson.topl_service_base

import androidx.annotation.StringDef
import io.matthewnelson.topl_core_base.BaseConsts

abstract class BaseServiceConsts: BaseConsts() {


    ///////////////////////////////
    /// BackgroundManagerPolicy ///
    ///////////////////////////////
    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE
    )
    @StringDef(
        BackgroundPolicy.RESPECT_RESOURCES,
        BackgroundPolicy.RUN_IN_FOREGROUND
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class BackgroundPolicy {
        companion object {
            private const val BACKGROUND_POLICY = "BackgroundPolicy_"
            const val RESPECT_RESOURCES = "${BACKGROUND_POLICY}RESPECT_RESOURCES"
            const val RUN_IN_FOREGROUND = "${BACKGROUND_POLICY}RUN_IN_FOREGROUND"
        }
    }


    /////////////////////
    /// ServiceAction ///
    /////////////////////
    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE,
        AnnotationTarget.PROPERTY
    )
    @StringDef(
        ServiceActionName.DISABLE_NETWORK,
        ServiceActionName.ENABLE_NETWORK,
        ServiceActionName.NEW_ID,
        ServiceActionName.RESTART_TOR,
        ServiceActionName.START,
        ServiceActionName.STOP
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ServiceActionName {
        companion object {
            private const val ACTION_NAME = "Action_"
            const val DISABLE_NETWORK = "${ACTION_NAME}DISABLE_NETWORK"
            const val ENABLE_NETWORK = "${ACTION_NAME}ENABLE_NETWORK"
            const val NEW_ID = "${ACTION_NAME}NEW_ID"
            const val RESTART_TOR = "${ACTION_NAME}RESTART_TOR"
            const val START = "${ACTION_NAME}START"
            const val STOP = "${ACTION_NAME}STOP"
        }
    }


    ////////////////////////////////
    /// Service Lifecycle Events ///
    ////////////////////////////////
    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE
    )
    @StringDef(
        ServiceLifecycleEvent.CREATED,
        ServiceLifecycleEvent.DESTROYED,
        ServiceLifecycleEvent.ON_BIND,
        ServiceLifecycleEvent.ON_UNBIND,
        ServiceLifecycleEvent.TASK_REMOVED,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ServiceLifecycleEvent {
        companion object {
            const val CREATED = "onCreate"
            const val DESTROYED = "onDestroy"
            const val ON_BIND = "onBind"
            const val ON_UNBIND = "onUnbind"
            const val TASK_REMOVED = "onTaskRemoved"
        }
    }


    ///////////////////////
    /// TorServicePrefs ///
    ///////////////////////
    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE
    )
    @StringDef(
        PrefKeyBoolean.DISABLE_NETWORK,
        PrefKeyBoolean.HAS_BRIDGES,
        PrefKeyBoolean.HAS_COOKIE_AUTHENTICATION,
        PrefKeyBoolean.HAS_DEBUG_LOGS,
        PrefKeyBoolean.HAS_DORMANT_CANCELED_BY_STARTUP,
        PrefKeyBoolean.HAS_OPEN_PROXY_ON_ALL_INTERFACES,
        PrefKeyBoolean.HAS_REACHABLE_ADDRESS,
        PrefKeyBoolean.HAS_REDUCED_CONNECTION_PADDING,
        PrefKeyBoolean.HAS_SAFE_SOCKS,
        PrefKeyBoolean.HAS_STRICT_NODES,
        PrefKeyBoolean.HAS_TEST_SOCKS,
        PrefKeyBoolean.IS_AUTO_MAP_HOSTS_ON_RESOLVE,
        PrefKeyBoolean.IS_RELAY,
        PrefKeyBoolean.RUN_AS_DAEMON,
        PrefKeyBoolean.USE_SOCKS5
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class PrefKeyBoolean {
        companion object {
            // Keys for returning Booleans
            const val DISABLE_NETWORK = "DISABLE_NETWORK"
            const val HAS_BRIDGES = "HAS_BRIDGES"
            const val HAS_COOKIE_AUTHENTICATION = "HAS_COOKIE_AUTHENTICATION"
            const val HAS_DEBUG_LOGS = "HAS_DEBUG_LOGS"
            const val HAS_DORMANT_CANCELED_BY_STARTUP = "HAS_DORMANT_CANCELED_BY_STARTUP"
            const val HAS_OPEN_PROXY_ON_ALL_INTERFACES = "HAS_OPEN_PROXY_ON_ALL_INTERFACES"
            const val HAS_REACHABLE_ADDRESS = "HAS_REACHABLE_ADDRESS"
            const val HAS_REDUCED_CONNECTION_PADDING = "HAS_REDUCED_CONNECTION_PADDING"
            const val HAS_SAFE_SOCKS = "HAS_SAFE_SOCKS"
            const val HAS_STRICT_NODES = "HAS_STRICT_NODES"
            const val HAS_TEST_SOCKS = "HAS_TEST_SOCKS"
            const val IS_AUTO_MAP_HOSTS_ON_RESOLVE = "IS_AUTO_MAP_HOSTS_ON_RESOLVE"
            const val IS_RELAY = "IS_RELAY"
            const val RUN_AS_DAEMON = "RUN_AS_DAEMON"
            const val USE_SOCKS5 = "USE_SOCKS5"
        }
    }

    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE
    )
    @StringDef(
        PrefKeyInt.DORMANT_CLIENT_TIMEOUT,
        PrefKeyInt.PROXY_PORT,
        PrefKeyInt.PROXY_SOCKS5_SERVER_PORT
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class PrefKeyInt {
        companion object {
            // Keys for returning Ints
            const val DORMANT_CLIENT_TIMEOUT = "DORMANT_CLIENT_TIMEOUT"
            const val PROXY_PORT = "PROXY_PORT"
            const val PROXY_SOCKS5_SERVER_PORT = "PROXY_SOCKS5_SERVER_PORT"
        }
    }

    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE
    )
    @StringDef(
        PrefKeyList.DNS_PORT_ISOLATION_FLAGS,
        PrefKeyList.HTTP_TUNNEL_PORT_ISOLATION_FLAGS,
        PrefKeyList.SOCKS_PORT_ISOLATION_FLAGS,
        PrefKeyList.TRANS_PORT_ISOLATION_FLAGS,
        PrefKeyList.USER_DEFINED_BRIDGES
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class PrefKeyList {
        companion object {
            // Keys for returning Lists
            const val DNS_PORT_ISOLATION_FLAGS = "DNS_PORT_ISOLATION_FLAGS"
            const val HTTP_TUNNEL_PORT_ISOLATION_FLAGS = "HTTP_PORT_ISOLATION_FLAGS"
            const val SOCKS_PORT_ISOLATION_FLAGS = "SOCKS_PORT_ISOLATION_FLAGS"
            const val TRANS_PORT_ISOLATION_FLAGS = "TRANS_PORT_ISOLATION_FLAGS"
            const val USER_DEFINED_BRIDGES = "USER_DEFINED_BRIDGES"
        }
    }

    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE
    )
    @StringDef(
        PrefKeyString.DNS_PORT,
        PrefKeyString.CUSTOM_TORRC,
        PrefKeyString.ENTRY_NODES,
        PrefKeyString.EXCLUDED_NODES,
        PrefKeyString.EXIT_NODES,
        PrefKeyString.HTTP_TUNNEL_PORT,
        PrefKeyString.PROXY_HOST,
        PrefKeyString.PROXY_PASSWORD,
        PrefKeyString.PROXY_SOCKS5_HOST,
        PrefKeyString.PROXY_TYPE,
        PrefKeyString.PROXY_USER,
        PrefKeyString.REACHABLE_ADDRESS_PORTS,
        PrefKeyString.RELAY_NICKNAME,
        PrefKeyString.RELAY_PORT,
        PrefKeyString.SOCKS_PORT,
        PrefKeyString.VIRTUAL_ADDRESS_NETWORK,
        PrefKeyString.HAS_CONNECTION_PADDING,
        PrefKeyString.TRANS_PORT
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class PrefKeyString {
        companion object {
            // Keys for returning Strings
            const val DNS_PORT = "DNS_PORT"
            const val CUSTOM_TORRC = "CUSTOM_TORRC"
            const val ENTRY_NODES = "ENTRY_NODES"
            const val EXCLUDED_NODES = "EXCLUDED_NODES"
            const val EXIT_NODES = "EXIT_NODES"
            const val HTTP_TUNNEL_PORT = "HTTP_TUNNEL_PORT"
            const val PROXY_HOST = "PROXY_HOST"
            const val PROXY_PASSWORD = "PROXY_PASSWORD"
            const val PROXY_SOCKS5_HOST = "PROXY_SOCKS5_HOST"
            const val PROXY_TYPE = "PROXY_TYPE"
            const val PROXY_USER = "PROXY_USER"
            const val REACHABLE_ADDRESS_PORTS = "REACHABLE_ADDRESS_PORTS"
            const val RELAY_NICKNAME = "RELAY_NICKNAME"
            const val RELAY_PORT = "RELAY_PORT"
            const val SOCKS_PORT = "SOCKS_PORT"
            const val VIRTUAL_ADDRESS_NETWORK = "VIRTUAL_ADDRESS_NETWORK"
            const val HAS_CONNECTION_PADDING = "HAS_CONNECTION_PADDING"
            const val TRANS_PORT = "TRANS_PORT"
        }
    }
}