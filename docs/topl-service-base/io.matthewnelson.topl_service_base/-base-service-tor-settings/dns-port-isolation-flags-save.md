[topl-service-base](../../index.md) / [io.matthewnelson.topl_service_base](../index.md) / [BaseServiceTorSettings](index.md) / [dnsPortIsolationFlagsSave](./dns-port-isolation-flags-save.md)

# dnsPortIsolationFlagsSave

`@WorkerThread abstract fun dnsPortIsolationFlagsSave(isolationFlags: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) [(source)](https://github.com/05nelsonm/TorOnionProxyLibrary-Android/blob/master/topl-service-base/src/main/java/io/matthewnelson/topl_service_base/BaseServiceTorSettings.kt#L163)

Saves the value for [isolationFlags](dns-port-isolation-flags-save.md#io.matthewnelson.topl_service_base.BaseServiceTorSettings$dnsPortIsolationFlagsSave(kotlin.collections.List((kotlin.String)))/isolationFlags) to [TorServicePrefs](../-tor-service-prefs/index.md). If the value is the same as what is
declared in [defaultTorSettings](default-tor-settings.md), [TorServicePrefs](../-tor-service-prefs/index.md) is queried to remove the setting if
it exists.

### Parameters

`isolationFlags` - A List of [io.matthewnelson.topl_core_base.BaseConsts.IsolationFlag](../../..//topl-core-base/io.matthewnelson.topl_core_base/-base-consts/-isolation-flag/index.md)'s
for the [dnsPort](dns-port.md)

**See Also**

[io.matthewnelson.topl_core_base.BaseConsts.IsolationFlag](../../..//topl-core-base/io.matthewnelson.topl_core_base/-base-consts/-isolation-flag/index.md)

[TorSettings.dnsPortIsolationFlags](../../..//topl-core-base/io.matthewnelson.topl_core_base/-tor-settings/dns-port-isolation-flags.md)

