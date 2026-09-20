package org.neteinstein.couples.ui

/**
 * Whether [userAgent] is an Android browser - the only visitors [PlatformInstallAppBanner] shows
 * the "get the Android app" banner to, since they're the only ones who can act on it.
 *
 * Every Android browser puts "Android" in its user agent, phones and tablets alike; the Play Store
 * listing installs on both, so both get the banner. (Phones additionally report "Mobile" and
 * tablets omit it, which is how Android documents telling the two apart - that distinction just
 * isn't one this banner cares about.)
 *
 * Lives in commonMain purely so it can be unit-tested on every target - the wasmJs actual only
 * supplies the `navigator.userAgent` string and does no parsing of its own.
 */
internal fun isAndroidUserAgent(userAgent: String): Boolean = userAgent.contains(ANDROID, ignoreCase = true)

private const val ANDROID = "Android"
