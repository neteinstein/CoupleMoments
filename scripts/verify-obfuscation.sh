#!/usr/bin/env bash
#
# Asserts that the `release` build type really is obfuscated, and that the handful of names this
# app resolves reflectively survived it. Run after `./gradlew assembleRelease` (or
# `assemble<Flavor>Release`); invoked by the "Minified Release" job in .github/workflows/pr.yml.
#
# Why this exists: `isMinifyEnabled`/`isShrinkResources` (androidApp/build.gradle.kts) and the keep rules
# in androidApp/proguard-rules.pro can regress silently - a stray blanket `-keep`, or minification being
# switched off - and the build still succeeds, so only the mapping file shows what R8 actually
# did. Reading it here turns "obfuscation is on" into a PR gate.
#
# Historical note: this used to also assert that Room's reflectively-resolved
# `CoupleMomentsDatabase`/`CoupleMomentsDatabase_Impl` class names survived obfuscation unchanged
# (Room does a runtime `Class.forName` lookup that a renamed pair would break). core:data's KMP
# migration replaced Room with SQLDelight, whose generated classes are constructed directly by
# normal Kotlin calls - no reflection, no keep rule, no name-survival check needed - so that
# assertion was removed along with the `-keep class * extends androidx.room.RoomDatabase` rule in
# androidApp/proguard-rules.pro. If this codebase ever reintroduces reflective name resolution
# (`Class.forName`, `Resources.getIdentifier`, a `ServiceLoader`, ...), add its keep rule there and
# a matching assertion here, the same way this used to work for Room.
set -euo pipefail

readonly APP_PACKAGE_PREFIX="org.neteinstein.couples."

failures=0

fail() {
    echo "::error::$1"
    failures=$((failures + 1))
}

# Counts app classes that R8 renamed. A class line in mapping.txt is unindented and reads
# "<original> -> <obfuscated>:"; members are indented, so anchoring on the package prefix at the
# start of the line matches classes only.
count_renamed_classes() {
    awk -F' -> ' -v prefix="$1" '
        index($0, prefix) == 1 && NF == 2 {
            obfuscated = $2
            sub(/:$/, "", obfuscated)
            if (obfuscated != $1) renamed++
        }
        END { print renamed + 0 }
    ' "$2"
}

verify_mapping() {
    local mapping="$1"
    local variant
    variant="$(basename "$(dirname "$mapping")")"

    local renamed
    renamed="$(count_renamed_classes "$APP_PACKAGE_PREFIX" "$mapping")"
    if [ "$renamed" -eq 0 ]; then
        fail "$variant: no ${APP_PACKAGE_PREFIX}* class was renamed - the release build is not obfuscated. Check isMinifyEnabled in androidApp/build.gradle.kts and for an over-broad -keep in androidApp/proguard-rules.pro."
    else
        echo "  $variant: $renamed obfuscated app classes"
    fi

    # No name-survival assertions right now: the Room check this used to run was removed with Room
    # itself (see the historical note at the top of this file), and nothing in the app resolves a
    # class by name any more. The loop that read the now-deleted ROOM_KEPT_CLASSES array was left
    # behind by that removal and aborted this script under `set -u` ("unbound variable") before it
    # could check the second variant - so this gate has not actually been running. Reinstate a
    # loop here if reflective name resolution ever comes back.
}

mappings=()
while IFS= read -r line; do
    mappings+=("$line")
done < <(find androidApp/build/outputs/mapping -mindepth 2 -maxdepth 2 -name mapping.txt -path '*Release/*' | sort)

if [ ${#mappings[@]} -eq 0 ]; then
    echo "::error::No release mapping.txt found under androidApp/build/outputs/mapping/. Run './gradlew assembleRelease' first; if it did run, R8 produced no mapping, which means the build is not being minified."
    exit 1
fi

for mapping in "${mappings[@]}"; do
    verify_mapping "$mapping"
done

if [ "$failures" -ne 0 ]; then
    echo "::error::Obfuscation verification failed with $failures problem(s)."
    exit 1
fi

echo "Obfuscation verified across ${#mappings[@]} release variant(s)."
