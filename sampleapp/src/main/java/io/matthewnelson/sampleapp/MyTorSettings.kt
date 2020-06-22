package io.matthewnelson.sampleapp

import io.matthewnelson.topl_core_base.TorSettings

/**
 * See [TorSettings] for comments on what is what.
 * */
class MyTorSettings: TorSettings() {

    override val disableNetwork: Boolean
        get() = DEFAULT__DISABLE_NETWORK

    override val dnsPort: String
        get() = DEFAULT__DNS_PORT

    override val customTorrc: String?
        get() = null

    override val entryNodes: String?
        get() = DEFAULT__ENTRY_NODES

    override val excludeNodes: String?
        get() = DEFAULT__EXCLUDED_NODES

    override val exitNodes: String?
        get() = DEFAULT__EXIT_NODES

    override val httpTunnelPort: String
        get() = DEFAULT__HTTP_TUNNEL_PORT

    override val listOfSupportedBridges: List<@SupportedBridges String>
        get() = arrayListOf(SupportedBridges.MEEK, SupportedBridges.OBFS4)

    override val proxyHost: String?
        get() = DEFAULT__PROXY_HOST

    override val proxyPassword: String?
        get() = DEFAULT__PROXY_PASSWORD

    override val proxyPort: Int?
        get() = null

    override val proxySocks5Host: String?
        get() = DEFAULT__PROXY_SOCKS5_HOST

    override val proxySocks5ServerPort: Int?
        get() = null

    override val proxyType: String?
        get() = DEFAULT__PROXY_TYPE

    override val proxyUser: String?
        get() = DEFAULT__PROXY_USER

    override val reachableAddressPorts: String
        get() = DEFAULT__REACHABLE_ADDRESS_PORTS

    override val relayNickname: String?
        get() = DEFAULT__RELAY_NICKNAME

    override val relayPort: Int?
        get() = null

    override val socksPort: String
        get() = "9051"

    override val virtualAddressNetwork: String?
        get() = "10.192.0.2/10"

    override val hasBridges: Boolean
        get() = DEFAULT__HAS_BRIDGES

    override val connectionPadding: @ConnectionPadding String
        get() = DEFAULT__HAS_CONNECTION_PADDING

    override val hasCookieAuthentication: Boolean
        get() = DEFAULT__HAS_COOKIE_AUTHENTICATION

    override val hasDebugLogs: Boolean
        get() = false

    override val hasDormantCanceledByStartup: Boolean
        get() = DEFAULT__HAS_DORMANT_CANCELED_BY_STARTUP

    override val hasIsolationAddressFlagForTunnel: Boolean
        get() = DEFAULT__HAS_ISOLATION_ADDRESS_FLAG_FOR_TUNNEL

    override val hasOpenProxyOnAllInterfaces: Boolean
        get() = DEFAULT__HAS_OPEN_PROXY_ON_ALL_INTERFACES

    override val hasReachableAddress: Boolean
        get() = DEFAULT__HAS_REACHABLE_ADDRESS

    override val hasReducedConnectionPadding: Boolean
        get() = DEFAULT__HAS_REDUCED_CONNECTION_PADDING

    override val hasSafeSocks: Boolean
        get() = DEFAULT__HAS_SAFE_SOCKS

    override val hasStrictNodes: Boolean
        get() = DEFAULT__HAS_STRICT_NODES

    override val hasTestSocks: Boolean
        get() = DEFAULT__HAS_TEST_SOCKS

    override val isAutoMapHostsOnResolve: Boolean
        get() = DEFAULT__IS_AUTO_MAP_HOSTS_ON_RESOLVE

    override val isRelay: Boolean
        get() = DEFAULT__IS_RELAY

    override val runAsDaemon: Boolean
        get() = DEFAULT__RUN_AS_DAEMON

    override val transPort: String
        get() = DEFAULT__TRANS_PORT

    override val useSocks5: Boolean
        get() = DEFAULT__USE_SOCKS5
}