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
package io.matthewnelson.topl_core.settings

import io.matthewnelson.topl_core.OnionProxyContext
import io.matthewnelson.topl_core_base.SettingsConsts.ConnectionPadding
import io.matthewnelson.topl_core_base.TorConfigFiles
import io.matthewnelson.topl_core_base.TorSettings
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

/**
 * This class is basically a torrc file builder. Every method you call adds a
 * specific value to the [buffer] which Tor understands.
 *
 * Calling [finishAndReturnString] will return to you the String that has been
 * built for you to write to the
 * [io.matthewnelson.topl_core_base.TorConfigFiles.torrcFile].
 *
 * Calling [finishAndWriteToTorrcFile] will do just that.
 *
 * See [io.matthewnelson.topl_core.OnionProxyManager.getNewSettingsBuilder]
 * */
class TorSettingsBuilder(private val onionProxyContext: OnionProxyContext) {

    private var buffer = StringBuffer()

    /**
     * This returns what's in the [buffer] as a String and then clears it.
     * You still need to write the String to the
     * [io.matthewnelson.topl_core_base.TorConfigFiles.torrcFile].
     * */
    fun finishAndReturnString(): String {
        val string = buffer.toString()
        buffer = StringBuffer()
        return string
    }

    /**
     * A convenience method for after populating the [buffer] by calling
     * [updateTorSettings]. It will overwrite your current torrc file (or
     * create a new one if it doesn't exist) with the new settings.
     *
     * TODO: Devise a more elegant solution using a diff to simply update it if
     *       need be.
     * */
    fun finishAndWriteToTorrcFile() =
        onionProxyContext.torConfigFiles.torrcFile.writeText(finishAndReturnString())

    /**
     * Updates the buffer for all methods annotated with [SettingsConfig]. You still need
     * to call [finishAndReturnString] and then write the returned String to your
     * [io.matthewnelson.topl_core_base.TorConfigFiles.torrcFile].
     *
     * Alternatively, call [finishAndWriteToTorrcFile], it's up to you.
     *
     * @throws [SecurityException] If denied access to the class
     * @throws [IllegalAccessException] see [java.lang.reflect.Method.invoke]
     * @throws [IllegalArgumentException] see [java.lang.reflect.Method.invoke]
     * @throws [InvocationTargetException] see [java.lang.reflect.Method.invoke]
     * @throws [NullPointerException] see [java.lang.reflect.Method.invoke]
     * @throws [ExceptionInInitializerError] see [java.lang.reflect.Method.invoke]
     *
     * TODO: Replace reflection.......... gross.
     * */
    @Throws(
        SecurityException::class,
        IllegalAccessException::class,
        IllegalArgumentException::class,
        InvocationTargetException::class,
        NullPointerException::class,
        ExceptionInInitializerError::class
    )
    fun updateTorSettings(): TorSettingsBuilder {
        for (method in this.javaClass.methods)
            for (annotation in method.annotations)
                if (annotation is SettingsConfig) {
                    method.invoke(this)
                    break
                }

        return this
    }

    fun automapHostsOnResolve(enable: Boolean): TorSettingsBuilder {
        val autoMapHostsOnResolve = if (enable) "1" else "0"
        buffer.append("AutomapHostsOnResolve $autoMapHostsOnResolve\n")
        return this
    }

    @SettingsConfig
    fun automapHostsOnResolveFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.isAutoMapHostsOnResolve)
            automapHostsOnResolve(true)
        else
            this

    fun addBridge(type: String?, config: String?): TorSettingsBuilder {
        if (!type.isNullOrEmpty() && !config.isNullOrEmpty())
            buffer.append("Bridge $type $config\n")
        return this
    }

    fun addCustomBridge(config: String?): TorSettingsBuilder {
        if (!config.isNullOrEmpty())
            buffer.append("Bridge $config\n")
        return this
    }

    @SettingsConfig
    fun bridgesFromSettings(): TorSettingsBuilder {
        try {
            addBridgesFromResources()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return this
    }

    // TODO: Re-work this to support different transport types in a more declarative manner
    fun transportPlugin(clientPath: String): TorSettingsBuilder {
        buffer.append("ClientTransportPlugin meek_lite,obfs3,obfs4 exec $clientPath\n")
        return this
    }

    // TODO: Re-work this to utilize torConfigFiles
    @Throws(IOException::class, SecurityException::class)
    fun configurePluggableTransportsFromSettings(pluggableTransportClient: File?): TorSettingsBuilder {
        if (pluggableTransportClient == null) return this

        if (!pluggableTransportClient.exists())
            throw IOException(
                "Bridge binary does not exist: ${pluggableTransportClient.canonicalPath}"
            )

        if (!pluggableTransportClient.canExecute())
            throw IOException(
                "Bridge binary is not executable: ${pluggableTransportClient.canonicalPath}"
            )

        transportPlugin(pluggableTransportClient.canonicalPath)
        return this
    }

    fun cookieAuthentication(): TorSettingsBuilder {
        buffer.append("CookieAuthentication 1\n")
        buffer.append("CookieAuthFile ${onionProxyContext.torConfigFiles.cookieAuthFile.absolutePath}\n")
        return this
    }

    @SettingsConfig
    fun cookieAuthenticationFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasCookieAuthentication)
            cookieAuthentication()
        else
            this

    fun connectionPadding(@ConnectionPadding setting: String): TorSettingsBuilder {
        buffer.append("ConnectionPadding $setting\n")
        return this
    }

    @SettingsConfig
    fun connectionPaddingFromSettings(): TorSettingsBuilder {
        when (onionProxyContext.torSettings.connectionPadding) {
            ConnectionPadding.AUTO, ConnectionPadding.OFF, ConnectionPadding.ON -> {
                return connectionPadding(onionProxyContext.torSettings.connectionPadding)
            }
        }
        return this
    }

    fun controlPortWriteToFile(torConfigFiles: TorConfigFiles): TorSettingsBuilder {
        buffer.append("ControlPortWriteToFile ${torConfigFiles.controlPortFile.absolutePath}\n")
        buffer.append("ControlPort auto\n")
        return this
    }

    @SettingsConfig
    fun controlPortWriteToFileFromConfig(): TorSettingsBuilder =
        controlPortWriteToFile(onionProxyContext.torConfigFiles)

    fun debugLogs(): TorSettingsBuilder {
        buffer.append("Log debug syslog\n")
        buffer.append("Log info syslog\n")
        buffer.append("SafeLogging 0\n")
        return this
    }

    @SettingsConfig
    fun debugLogsFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasDebugLogs)
            debugLogs()
        else
            this

    fun disableNetwork(disable: Boolean): TorSettingsBuilder {
        val disableNetwork = if (disable) "1" else "0"
        buffer.append("DisableNetwork $disableNetwork\n")
        return this
    }

    @SettingsConfig
    fun disableNetworkFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.disableNetwork)
            disableNetwork(true)
        else
            this

    fun dnsPort(dnsPort: String): TorSettingsBuilder {
        buffer.append("DNSPort $dnsPort\n")
        return this
    }

    @SettingsConfig
    fun dnsPortFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.dnsPort != TorSettings.DEFAULT__DNS_PORT)
            dnsPort(onionProxyContext.torSettings.dnsPort)
        else
            this

    fun dormantCanceledByStartup(enable: Boolean): TorSettingsBuilder {
        val dormantCanceledStartup = if (enable) "1" else "0"
        buffer.append("DormantCanceledByStartup $dormantCanceledStartup\n")
        return this
    }

    @SettingsConfig
    fun dormantCanceledByStartupFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasDormantCanceledByStartup)
            dormantCanceledByStartup(true)
        else
            this

    fun entryNodes(entryNodes: String?): TorSettingsBuilder {
        if (!entryNodes.isNullOrEmpty())
            buffer.append("EntryNodes $entryNodes\n")
        return this
    }

    fun excludeNodes(excludeNodes: String?): TorSettingsBuilder {
        if (!excludeNodes.isNullOrEmpty())
            buffer.append("ExcludeNodes $excludeNodes\n")
        return this
    }

    fun exitNodes(exitNodes: String?): TorSettingsBuilder {
        if (!exitNodes.isNullOrEmpty())
            buffer.append("ExitNodes $exitNodes\n")
        return this
    }

    fun geoIpFile(path: String?): TorSettingsBuilder {
        if (!path.isNullOrEmpty())
            buffer.append("GeoIPFile $path\n")
        return this
    }

    fun geoIpV6File(path: String?): TorSettingsBuilder {
        if (!path.isNullOrEmpty())
            buffer.append("GeoIPv6File $path\n")
        return this
    }

    fun httpTunnelPort(port: String, isolationFlags: String?): TorSettingsBuilder {
        buffer.append("HTTPTunnelPort $port")
        if (!isolationFlags.isNullOrEmpty())
            buffer.append(" $isolationFlags")
        buffer.append("\n")
        return this
    }

    @SettingsConfig
    fun httpTunnelPortFromSettings(): TorSettingsBuilder {
        if (onionProxyContext.torSettings.httpTunnelPort == TorSettings.DEFAULT__HTTP_TUNNEL_PORT)
            return this

        return httpTunnelPort(
            onionProxyContext.torSettings.httpTunnelPort,
            if (onionProxyContext.torSettings.hasIsolationAddressFlagForTunnel)
                "IsolateDestAddr"
            else
                null
        )
    }

    private fun addLine(value: String?): TorSettingsBuilder {
        if (!value.isNullOrEmpty())
            buffer.append("$value\n")
        return this
    }

    fun makeNonExitRelay(dnsFile: String, orPort: Int, nickname: String): TorSettingsBuilder {
        buffer.append("ServerDNSResolvConfFile $dnsFile\n")
        buffer.append("ORPort $orPort\n")
        buffer.append("Nickname $nickname\n")
        buffer.append("ExitPolicy reject *:*\n")
        return this
    }

    /**
     * Sets the entry/exit/exclude nodes
     */
    @SettingsConfig
    fun nodesFromSettings(): TorSettingsBuilder {
        entryNodes(onionProxyContext.torSettings.entryNodes)
            .exitNodes(onionProxyContext.torSettings.exitNodes)
            .excludeNodes(onionProxyContext.torSettings.excludeNodes)
        return this
    }

    /**
     * Adds non exit relay to builder. This method uses a default Quad9 nameserver.
     */
    @SettingsConfig
    fun nonExitRelayFromSettings(): TorSettingsBuilder {
        if (!onionProxyContext.torSettings.hasReachableAddress &&
            !onionProxyContext.torSettings.hasBridges &&
            onionProxyContext.torSettings.isRelay
        ) {
            val relayPort = onionProxyContext.torSettings.relayPort
            val relayNickname = onionProxyContext.torSettings.relayNickname
            if (relayPort != null && !relayNickname.isNullOrEmpty()) {
                try {
                    val resolv = onionProxyContext.createQuad9NameserverFile()
                    makeNonExitRelay(resolv.canonicalPath, relayPort, relayNickname)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return this
    }

    fun proxyOnAllInterfaces(): TorSettingsBuilder {
        buffer.append("SocksListenAddress 0.0.0.0\n")
        return this
    }

    @SettingsConfig
    fun proxyOnAllInterfacesFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasOpenProxyOnAllInterfaces)
            proxyOnAllInterfaces()
        else
            this

    /**
     * Set socks5 proxy with no authentication.
     */
    fun proxySocks5(host: String?, port: Int?): TorSettingsBuilder {
        if (!host.isNullOrEmpty() && port != null)
            buffer.append("socks5Proxy $host:$port\n")
        return this
    }

    @SettingsConfig
    fun proxySocks5FromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.useSocks5 && !onionProxyContext.torSettings.hasBridges)
            proxySocks5(
                onionProxyContext.torSettings.proxySocks5Host,
                onionProxyContext.torSettings.proxySocks5ServerPort
            )
        else
            this

    /**
     * Sets proxyWithAuthentication information. If proxyType, proxyHost or proxyPort is
     * empty/null, then this method does nothing.
     *
     * HTTPProxyAuthenticator is deprecated as of 0.3.1.0-alpha, *use HTTPS/Socks5* authentication.
     *
     * TODO: Remove support for HTTPProxyAuthenticator
     * TODO: Re-work this mess with annotation types and when statements...
     * */
    fun proxyWithAuthentication(
        proxyType: String?,
        proxyHost: String?,
        proxyPort: Int?,
        proxyUser: String?,
        proxyPass: String?
    ): TorSettingsBuilder {
        if (!proxyType.isNullOrEmpty() && !proxyHost.isNullOrEmpty() && proxyPort != null) {
            buffer.append("${proxyType}Proxy $proxyHost:$proxyPort\n")
            if (proxyUser != null && proxyPass != null) {
                if (proxyType.equals("socks5", ignoreCase = true)) {
                    buffer.append("Socks5ProxyUsername $proxyUser\n")
                    buffer.append("Socks5ProxyPassword $proxyPass\n")
                } else {
                    buffer.append("${proxyType}ProxyAuthenticator $proxyUser:$proxyPort\n")
                }
            } else if (proxyPass != null) {
                buffer.append("${proxyType}ProxyAuthenticator $proxyUser:$proxyPort\n").append(proxyUser)
                    .append(":").append(proxyPort.toString()).append("\n")
            }
        }
        return this
    }

    @SettingsConfig
    fun proxyWithAuthenticationFromSettings(): TorSettingsBuilder =
        if (!onionProxyContext.torSettings.useSocks5 && !onionProxyContext.torSettings.hasBridges)
            proxyWithAuthentication(
                onionProxyContext.torSettings.proxyType,
                onionProxyContext.torSettings.proxyHost,
                onionProxyContext.torSettings.proxyPort,
                onionProxyContext.torSettings.proxyUser,
                onionProxyContext.torSettings.proxyPassword
            )
        else
            this

    fun reachableAddressPorts(reachableAddressesPorts: String?): TorSettingsBuilder {
        if (!reachableAddressesPorts.isNullOrEmpty())
            buffer.append("ReachableAddresses ").append(reachableAddressesPorts).append("\n")
        return this
    }

    @SettingsConfig
    fun reachableAddressesFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasReachableAddress)
            reachableAddressPorts(onionProxyContext.torSettings.reachableAddressPorts)
        else
            this

    fun reducedConnectionPadding(enable: Boolean): TorSettingsBuilder {
        val reducedPadding = if (enable) "1" else "0"
        buffer.append("ReducedConnectionPadding $reducedPadding").append("\n")
        return this
    }

    @SettingsConfig
    fun reducedConnectionPaddingFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasReducedConnectionPadding)
            reducedConnectionPadding(true)
        else
            this

    fun reset() {
        buffer = StringBuffer()
    }

    fun runAsDaemon(enable: Boolean): TorSettingsBuilder {
        val daemon = if (enable) "1" else "0"
        buffer.append("RunAsDaemon $daemon").append("\n")
        return this
    }

    @SettingsConfig
    fun runAsDaemonFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.runAsDaemon)
            runAsDaemon(true)
        else
            this

    fun safeSocks(enable: Boolean): TorSettingsBuilder {
        val safeSocksSetting = if (enable) "1" else "0"
        buffer.append("SafeSocks $safeSocksSetting").append("\n")
        return this
    }

    @SettingsConfig
    fun safeSocksFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasSafeSocks)
            safeSocks(true)
        else
            this

    /**
     * Ensure that you have setup [io.matthewnelson.topl_core.util.TorInstaller]
     * such that you've copied the geoip/geoip6 files over prior to calling this.
     * */
    @Throws(IOException::class, SecurityException::class)
    fun setGeoIpFiles(): TorSettingsBuilder {
        val torConfigFiles = onionProxyContext.torConfigFiles
        if (torConfigFiles.geoIpFile.exists())
            geoIpFile(torConfigFiles.geoIpFile.canonicalPath)
        if (torConfigFiles.geoIpv6File.exists())
            geoIpV6File(torConfigFiles.geoIpv6File.canonicalPath)
        return this
    }

    fun socksPort(socksPort: String, isolationFlag: String?): TorSettingsBuilder {
        if (socksPort.isEmpty()) return this

        buffer.append("SOCKSPort ").append(socksPort)

        if (!isolationFlag.isNullOrEmpty())
            buffer.append(" ").append(isolationFlag)

        buffer.append(" KeepAliveIsolateSOCKSAuth")
        buffer.append(" IPv6Traffic")
        buffer.append(" PreferIPv6")
        buffer.append("\n")
        return this
    }

    @SettingsConfig
    fun socksPortFromSettings(): TorSettingsBuilder {
        var socksPort = onionProxyContext.torSettings.socksPort
        if (socksPort.indexOf(':') != -1)
            socksPort = socksPort.split(":".toRegex()).toTypedArray()[1]

        if (!socksPort.equals("auto", ignoreCase = true) && isLocalPortOpen(socksPort.toInt()))
            socksPort = "auto"

        return socksPort(
            socksPort,
            if (onionProxyContext.torSettings.hasIsolationAddressFlagForTunnel)
                "IsolateDestAddr"
            else
                null
        )
    }

    fun strictNodes(enable: Boolean): TorSettingsBuilder {
        val strictNodeSetting = if (enable) "1" else "0"
        buffer.append("StrictNodes $strictNodeSetting").append("\n")
        return this
    }

    @SettingsConfig
    fun strictNodesFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasStrictNodes)
            strictNodes(true)
        else
            this

    fun testSocks(enable: Boolean): TorSettingsBuilder {
        val testSocksSetting = if (enable) "1" else "0"
        buffer.append("TestSocks $testSocksSetting").append("\n")
        return this
    }

    @SettingsConfig
    fun testSocksFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasTestSocks)
            testSocks(true)
        else
            this

    @SettingsConfig
    @Throws(UnsupportedEncodingException::class)
    fun torrcCustomFromSettings(): TorSettingsBuilder {
        val customTorrc = onionProxyContext.torSettings.customTorrc
        return if (customTorrc != null)
            addLine(String(customTorrc.toByteArray(Charsets.US_ASCII)))
        else
            this
    }

    fun transPort(transPort: String): TorSettingsBuilder {
        buffer.append("TransPort ").append(transPort).append("\n")
        return this
    }

    @SettingsConfig
    fun transPortFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.transPort != TorSettings.DEFAULT__TRANS_PORT)
            transPort(onionProxyContext.torSettings.transPort)
        else
            this

    fun useBridges(useThem: Boolean): TorSettingsBuilder {
        val useBridges = if (useThem) "1" else "0"
        buffer.append("UseBridges $useBridges").append("\n")
        return this
    }

    @SettingsConfig
    fun useBridgesFromSettings(): TorSettingsBuilder =
        if (onionProxyContext.torSettings.hasBridges)
            useBridges(true)
        else
            this

    fun virtualAddressNetwork(address: String?): TorSettingsBuilder {
        if (!address.isNullOrEmpty())
            buffer.append("VirtualAddrNetwork ").append(address).append("\n")
        return this
    }

    @SettingsConfig
    fun virtualAddressNetworkFromSettings(): TorSettingsBuilder =
        virtualAddressNetwork(onionProxyContext.torSettings.virtualAddressNetwork)

    /**
     * Adds bridges from a resource stream. This relies on the
     * [io.matthewnelson.topl_core.util.TorInstaller] to know how to obtain this stream.
     * These entries may be type-specified like:
     *
     *
     * `obfs3 169.229.59.74:31493 AF9F66B7B04F8FF6F32D455F05135250A16543C9`
     *
     *
     * Or it may just be a custom entry like
     *
     *
     * `69.163.45.129:443 9F090DE98CA6F67DEEB1F87EFE7C1BFD884E6E2F`
     *
     * See [io.matthewnelson.topl_core.util.TorInstaller] comment for further details
     * on how to implement that.
     * */
    @Throws(IOException::class)
    fun addBridgesFromResources(): TorSettingsBuilder {
        if (onionProxyContext.torSettings.hasBridges) {
            val bridgesStream = onionProxyContext.torInstaller.openBridgesStream()
            if (bridgesStream != null) {
                val formatType = bridgesStream.read()

                if (formatType == 0)
                    addBridges(bridgesStream)
                else
                    addCustomBridges(bridgesStream)
            }
        }
        return this
    }

    /**
     * Add bridges from bridges.txt file.
     * */
    private fun addBridges(input: InputStream?) {
        if (input == null) return

        val bridges = readBridgesFromStream(input)
        for (b in bridges)
            addBridge(b.type, b.config)
    }

    /**
     * Add custom bridges defined by the user. These will have a bridgeType of 'custom' as
     * the first field.
     * */
    private fun addCustomBridges(input: InputStream?) {
        if (input == null) return

        val bridges = readCustomBridgesFromStream(input)
        for (b in bridges)
            if (b.type == "custom")
                addCustomBridge(b.config)
    }

    private class Bridge(val type: String, val config: String)

    private fun readBridgesFromStream(input: InputStream): List<Bridge> {
        val bridges: MutableList<Bridge> = ArrayList()
        try {
            val br = BufferedReader(InputStreamReader(input, Charsets.UTF_8))
            var line = br.readLine()

            while (line != null) {
                val tokens = line.split("\\s+".toRegex(), 2).toTypedArray()
                if (tokens.size != 2) {
                    line = br.readLine()
                    continue  //bad entry
                }
                bridges.add(Bridge(tokens[0], tokens[1]))
                line = br.readLine()
            }

            br.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bridges
    }

    private fun readCustomBridgesFromStream(input: InputStream): List<Bridge> {
        val bridges: MutableList<Bridge> = ArrayList()
        try {
            val br = BufferedReader(InputStreamReader(input, Charsets.UTF_8))
            var line = br.readLine()

            while (line != null) {
                if (line.isEmpty()) {
                    line = br.readLine()
                    continue
                }
                bridges.add(Bridge("custom", line))
                line = br.readLine()
            }

            br.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bridges
    }

    /**
     * Checks if the ascribed port is open.
     * */
    private fun isLocalPortOpen(port: Int): Boolean {
        val socket = Socket()

        return try {
            socket.connect(InetSocketAddress("127.0.0.1", port), 500)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                socket.close()
            } catch (ee: Exception) {
                ee.printStackTrace()
            }
        }
    }
}