package org.neteinstein.family.domain.util

/**
 * Compares two dot-separated numeric version strings (e.g. "1.0.16") by their numeric segments,
 * NOT lexicographically - a plain string comparison would wrongly rank "1.0.9" above "1.0.16"
 * (comparing the third segment's leading character, '9' > '1') even though 16 > 9. A missing
 * trailing segment is treated as 0 (so "1.0" == "1.0.0"), and a non-numeric segment is treated as
 * 0 rather than throwing, so an unexpected tag format degrades to "not newer" instead of crashing
 * the update check. Callers are expected to strip any leading "v" from a GitHub release tag first.
 */
fun isNewerVersion(current: String, candidate: String): Boolean {
    val currentSegments = current.trim().toVersionSegments()
    val candidateSegments = candidate.trim().toVersionSegments()

    for (index in 0 until maxOf(currentSegments.size, candidateSegments.size)) {
        val currentSegment = currentSegments.getOrElse(index) { 0 }
        val candidateSegment = candidateSegments.getOrElse(index) { 0 }
        if (currentSegment != candidateSegment) return candidateSegment > currentSegment
    }
    return false
}

private fun String.toVersionSegments(): List<Int> = split(".").map { it.toIntOrNull() ?: 0 }
