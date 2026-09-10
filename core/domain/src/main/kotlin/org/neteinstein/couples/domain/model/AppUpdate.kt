package org.neteinstein.couples.domain.model

/**
 * A GitHub release newer than the currently installed build. [versionName] has the release tag's
 * leading "v" stripped (e.g. "1.0.17" from tag "v1.0.17") so it matches the exact scheme
 * PackageManager reports for the installed build.
 */
data class AppUpdate(
    val versionName: String,
    val apkDownloadUrl: String,
)
