#!/usr/bin/env bash
#
# Asserts that the `release` build type really is obfuscated, and that the handful of names this
# app resolves reflectively survived it. Run after `./gradlew assembleRelease` (or
# `assemble<Flavor>Release`); invoked by the "Minified Release" job in .github/workflows/pr.yml.
#
# Why this exists: `isMinifyEnabled`/`isShrinkResources` (app/build.gradle.kts) and the keep rules
# in app/proguard-rules.pro can regress silently - a stray blanket `-keep`, or minification being
# switched off - and the build still succeeds, so only the mapping file shows what R8 actually
# did. Reading it here turns "obfuscation is on and Room still resolves" into a PR gate.
set -euo pipefail

readonly APP_PACKAGE_PREFIX="org.neteinstein.couples."
# Room appends "_Impl" to the runtime name of the class passed to Room.databaseBuilder(...) and
# looks the result up with Class.forName (core/data/.../data/di/DataModule.kt), so renaming either
# of these independently breaks the database at runtime. Pinned by the RoomDatabase keep rule in
# app/proguard-rules.pro; asserted here because the failure is a runtime crash, not a build error.
readonly ROOM_KEPT_CLASSES=(
    "org.neteinstein.couples.data.local.CoupleMomentsDatabase"
    "org.neteinstein.couples.data.local.CoupleMomentsDatabase_Impl"
)

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
        fail "$variant: no ${APP_PACKAGE_PREFIX}* class was renamed - the release build is not obfuscated. Check isMinifyEnabled in app/build.gradle.kts and for an over-broad -keep in app/proguard-rules.pro."
    else
        echo "  $variant: $renamed obfuscated app classes"
    fi

    local kept
    for kept in "${ROOM_KEPT_CLASSES[@]}"; do
        if grep -qxF "$kept -> $kept:" "$mapping"; then
            echo "  $variant: kept $kept"
        else
            fail "$variant: $kept was renamed or removed - Room's reflective Class.forName lookup will fail at runtime. Check the RoomDatabase keep rule in app/proguard-rules.pro."
        fi
    done
}

mappings=()
while IFS= read -r line; do
    mappings+=("$line")
done < <(find app/build/outputs/mapping -mindepth 2 -maxdepth 2 -name mapping.txt -path '*Release/*' | sort)

if [ ${#mappings[@]} -eq 0 ]; then
    echo "::error::No release mapping.txt found under app/build/outputs/mapping/. Run './gradlew assembleRelease' first; if it did run, R8 produced no mapping, which means the build is not being minified."
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
