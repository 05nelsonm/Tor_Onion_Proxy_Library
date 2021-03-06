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
package io.matthewnelson.topl_service.service.components.actions

import io.matthewnelson.topl_service.util.ServiceConsts.ServiceActionCommand
import io.matthewnelson.topl_service_base.BaseServiceConsts.ServiceActionName

/**
 * There are multiple avenues to interacting with a Service (BroadcastReceiver, Binder,
 * Context.startService). This class provides a standardized way of processing those requests, no
 * matter what avenue or form (Intents). Each [ServiceAction] has a defined list of commands that
 * are executed by [io.matthewnelson.topl_service.service.components.actions.ServiceActionProcessor]
 * so that it breaks up the steps in a manner which can be interrupted for quickly responding to
 * User interaction.
 * */
internal sealed class ServiceAction {

    @ServiceActionName
    abstract val name: String

    /**
     * Individual [ServiceActionCommand]'s to be executed sequentially by
     * [io.matthewnelson.topl_service.service.components.actions.ServiceActionProcessor].
     * */
    abstract val commands: Array<@ServiceActionCommand String>

    /**
     * For every [ServiceActionCommand.DELAY] within [commands], a value will be consumed
     * when executing it.
     *
     * Override this to define the values for each DELAY call.
     * */
    protected open val delayLengthQueue: MutableList<Long> = mutableListOf()

    /**
     * Removes the 0th element within [delayLengthQueue] then returns it.
     * If [delayLengthQueue] is empty, returns 0L.
     *
     * @return The 0th element within [delayLengthQueue], or 0L if empty
     * */
    @JvmSynthetic
    fun consumeDelayLength(): Long =
        if (delayLengthQueue.isNotEmpty())
            delayLengthQueue.removeAt(0)
        else
            0L

    /**
     * Boolean value for providing [ServiceAction]'s the capability of being issued to
     * the [ServiceActionProcessor] and notifying that the submitter of the [ServiceAction]
     * wants [ServiceActionProcessor.lastServiceAction] to be updated.
     *
     * @see [Start]
     * @see [Stop]
     * */
    open val updateLastAction: Boolean = true

    internal class SetDisableNetwork private constructor(
        override val name: String,
        updateLastServiceAction: Boolean = false
    ): ServiceAction() {

        companion object {
            @JvmSynthetic
            fun instantiate(
                name: String,
                updateLastServiceAction: Boolean = false
            ): SetDisableNetwork =
                SetDisableNetwork(name, updateLastServiceAction)
        }

        override val commands: Array<String>
            get() = when (name) {
                ServiceActionName.DISABLE_NETWORK -> {
                    arrayOf(
                        ServiceActionCommand.DELAY,
                        ServiceActionCommand.SET_DISABLE_NETWORK
                    )
                }
                ServiceActionName.ENABLE_NETWORK -> {
                    arrayOf(
                        ServiceActionCommand.SET_DISABLE_NETWORK
                    )
                }
                else -> {
                    arrayOf(
                        ServiceActionCommand.DELAY
                    )
                }
            }


        override val delayLengthQueue = mutableListOf(ServiceActionProcessor.getDisableNetworkDelay())

        override val updateLastAction: Boolean = updateLastServiceAction
    }

    internal class NewId private constructor(): ServiceAction() {

        companion object {
            @JvmSynthetic
            fun instantiate(): NewId =
                NewId()
        }

        @ServiceActionName
        override val name: String = ServiceActionName.NEW_ID

        override val commands: Array<String>
            get() = arrayOf(
                ServiceActionCommand.NEW_ID
            )
    }

    internal class RestartTor private constructor(): ServiceAction() {

        companion object {
            @JvmSynthetic
            fun instantiate(): RestartTor =
                RestartTor()
        }

        @ServiceActionName
        override val name: String = ServiceActionName.RESTART_TOR

        override val commands: Array<String>
            get() = arrayOf(
                ServiceActionCommand.STOP_TOR,
                ServiceActionCommand.DELAY,
                ServiceActionCommand.START_TOR
            )

        override val delayLengthQueue = mutableListOf(ServiceActionProcessor.getRestartTorDelayTime())
    }

    internal class Start private constructor(
        updateLastServiceAction: Boolean = true
    ): ServiceAction() {

        companion object {
            @JvmSynthetic
            fun instantiate(updateLastServiceAction: Boolean = true): Start =
                Start(updateLastServiceAction)
        }

        @ServiceActionName
        override val name: String = ServiceActionName.START

        override val commands: Array<String>
            get() = arrayOf(
                ServiceActionCommand.START_TOR
            )

        override val updateLastAction: Boolean = updateLastServiceAction
    }

    internal class Stop private constructor(
        updateLastServiceAction: Boolean = true
    ): ServiceAction() {

        companion object {
            @JvmSynthetic
            fun instantiate(updateLastServiceAction: Boolean = true): Stop =
                Stop(updateLastServiceAction)
        }

        @ServiceActionName
        override val name: String = ServiceActionName.STOP

        override val commands: Array<String>
            get() = arrayOf(
                ServiceActionCommand.STOP_TOR,
                ServiceActionCommand.DELAY,
                ServiceActionCommand.STOP_SERVICE
            )

        override val delayLengthQueue = mutableListOf(ServiceActionProcessor.getStopServiceDelayTime())

        override val updateLastAction: Boolean = updateLastServiceAction
    }
}