# KMP + Compose Multiplatform migration

> **Status: shipped.** Couple Moments builds and runs on Android, iOS (simulator) and Web
> (wasmJs, deployed to [couples.loopgain.org](https://couples.loopgain.org)). This file is the
> design record: the original plan is preserved below from §1 onward, and this header says what
> actually shipped, where it diverged, and what is still open.
>
> Authoritative description of the live architecture is [`AGENTS.md`](../AGENTS.md), not this file.

## What shipped

| | Plan said | Shipped |
|---|---|---|
| Module shape | collapse all 8 modules into one `composeApp` | kept the per-layer split; each `core:*`/`feature:*` became a KMP android-library. `app` is the shared aggregator, `androidApp`/`iosApp`/`webApp` are thin shells |
| Database | Room Multiplatform + bundled SQLite driver | **SQLDelight** — Android `AndroidSqliteDriver`, iOS `NativeSqliteDriver`. wasmJs has no driver, so `CardDao`/`SeedMetadataDao` bind to `localStorage`-backed implementations |
| Preferences | `expect class KeyValueStore` | **multiplatform-settings** (`Settings`), one named store per preference |
| HTTP | Ktor for the shared parts | **Ktor** + kotlinx.serialization, Android-only (self-update has no iOS/Web equivalent) |
| Targets | Android + iOS | Android + iOS + **Web** |
| Per-app language (§4.7, §8.2) | keep OS-level *or* switch to in-app picker | **both**: Android keeps its OS deep link, iOS/Web get an in-app picker (`AppLanguage`, `GetContentLanguageUseCase`) |
| Self-update on iOS (§8.4) | out of scope | confirmed out of scope; iOS/Web bind no-ops |
| Rollout (§6, §8.5) | one PR per phase | PR #34 (groundwork + 4 modules), #36 (iOS/Web + remaining modules), #38, #40 |

Delivered beyond the plan: emoji → Material-icon fix (emoji render as "tofu" boxes on the
Wasm/Skia renderer, which has no emoji-font fallback), arrow-key deck navigation for the web, and
an Android-only "install the app" banner on the web build.

## Still open

- **iOS ships nowhere.** Simulator-only; no Apple Developer signing, no TestFlight/App Store
  pipeline. §8.3 called this out as a decision and it has not been made.
- **No CI gate runs the app.** Builds and unit tests both passed while the web build was a blank
  page (a Koin binding registered under its concrete type instead of its interface). `core:data`'s
  `DataModuleGraphTest` now resolves the whole Koin graph on Android, but there is no equivalent
  for the iOS/wasmJs `platformDataModule` actuals, and nothing launches any of the three apps.
- **iOS/Web-only UI paths are unexercised.** `SettingsPlatformActions`' iOS/wasmJs actuals and the
  in-app `LanguagePicker` only run off Android, and no test or CI job touches them.
- **iOS persistence unvalidated.** SQLDelight's native driver compiles and the app runs, but
  nobody has confirmed a hidden card survives an app restart on iOS.
- **Kover excludes are a judgement call.** `QuestionSeedData` and generated classes are excluded so
  the number reflects real logic; revisit if the exclusion list starts hiding things.

---

## 1. Goal

Ship Couple Moments on iOS without a rewrite, by sharing domain, data and UI code between Android and
iOS through one Kotlin Multiplatform module, the same way LoopGain does. Android must keep working
exactly as it does today — same Room schema, same GitHub-releases self-update flow (Android-only),
same two distribution flavors, same R8/ktlint/coverage gates — while iOS gets a real native app built
from shared Compose UI.

## 2. Current state (what we're migrating from)

Gradle multi-module Android project, one module per layer, strict dependency direction enforced by
`settings.gradle.kts` + `AGENTS.md` convention (not lint):

```
app                 → MainActivity, CoupleMomentsApp (Application), Koin wiring, NavHost, 2 flavors
core/domain         → pure Kotlin: models, repository interfaces, use cases (no Android deps)
core/data           → repository impls, Room DB, SharedPreferences, GitHub REST client
core/ui             → CoupleMomentsTheme, Color/Typography, OriginReveal animation
feature/splash      → splash screen
feature/home        → swipe deck + grid, HomeViewModel
feature/game        → GameScreen/GameViewModel
feature/settings    → SettingsScreen/SettingsViewModel
```

Platform touchpoints that don't have an obvious multiplatform equivalent yet:

| Concern | File(s) | Mechanism |
|---|---|---|
| Question/card storage | `core/data/.../local/{CoupleMomentsDatabase,CardDao,SeedMetadataDao,CardEntity,SeedMetadataEntity}.kt` | Room (KSP), versioned reseed via `QuestionSeedData.VERSION` + `seed_metadata` table, real `Migration`s |
| Theme mode flag | `core/data/.../repository/ThemeModeRepositoryImpl.kt` | `SharedPreferences` |
| Intimacy gate flag | `core/data/.../repository/IntimacyGateRepositoryImpl.kt` | `SharedPreferences` |
| "Questions for parents" flag | `core/data/.../repository/QuestionsForParentsRepositoryImpl.kt` | `SharedPreferences` |
| Current language | `core/data/.../locale/LocaleProviderImpl.kt` | `Locale.getDefault()`, driven by Android's per-app language (`android:localeConfig`, `app/src/main/res/xml/locale_config.xml`) |
| Self-update check | `core/data/.../repository/GitHubUpdateRepositoryImpl.kt` | raw `HttpURLConnection` + `org.json`, hits `api.github.com/repos/neteinstein/CoupleMoments/releases/latest` |
| Self-update install | `core/data/.../installer/AppUpdateInstallerImpl.kt`, `receiver/UpdateApkCleanupReceiver.kt`, `app/src/github/AndroidManifest.xml` | `FileProvider` + `REQUEST_INSTALL_PACKAGES`, **`github` flavor only** |
| Dynamic color | `core/ui/.../theme/Theme.kt` | `dynamicLightColorScheme`/`dynamicDarkColorScheme`, Android 12+ only |
| Strings, 5 languages | `app/src/main/res/values{,-pt,-es,-fr,-de}/strings.xml` | Android resources |
| Distribution | `app/build.gradle.kts` `distribution` flavor dim: `github` (self-update APK) vs `playstore` (Play-managed, `BuildConfig.UPDATES_ENABLED=false`) | AGP product flavors |
| Obfuscation | `app/proguard-rules.pro`, `scripts/verify-obfuscation.sh` | R8 full mode, one `-keep` for Room's `_Impl` lookup |
| CI | `.github/workflows/pr.yml`, `.github/workflows/release.yml` | ktlint, assembleDebug, assembleRelease+`verify-obfuscation.sh`, testDebugUnitTest, AGP built-in coverage → Codecov |

## 3. Target architecture (mirrors LoopGain)

AGP 9's `com.android.application` and `org.jetbrains.kotlin.multiplatform` plugins can no longer
coexist in one module (this is why LoopGain splits `composeApp` from `androidApp`). Same split here:

```
composeApp/                # KMP library module — all shared code lives here
├── commonMain/kotlin/org/neteinstein/couples/
│   ├── domain/             # models, repository interfaces, use cases — near-verbatim from core/domain
│   ├── data/
│   │   ├── local/          # Room entities/DAOs/@Database (KMP Room, see §4.2) + expect DatabaseBuilder
│   │   ├── prefs/          # expect/actual KeyValueStore (replaces SharedPreferences, see §4.3)
│   │   ├── source/         # QuestionSeedData — unchanged, pure Kotlin
│   │   └── repository/     # repository impls — mostly unchanged, now built on KeyValueStore/Room commonMain
│   ├── di/                 # Koin module(s), composed from per-area modules (was per-Gradle-module)
│   └── ui/
│       ├── theme/          # CoupleMomentsTheme — dynamic color becomes expect/actual (see §4.5)
│       ├── screens/        # splash/, home/, game/, settings/ screen composables
│       ├── components/     # OriginReveal and other shared composables
│       ├── viewmodel/      # HomeViewModel, GameViewModel, SettingsViewModel, MainViewModel
│       └── navigation/     # Screen.kt, AppNavigation.kt, MainScreen.kt
│   └── composeResources/   # strings.xml per language (CMP resource system, see §4.6)
├── androidMain/kotlin/…    # Android actuals: DatabaseBuilder(Context), KeyValueStore(SharedPreferences),
│                           #   dynamic color, LocaleProvider (per-app language)
├── iosMain/kotlin/…        # iOS actuals: DatabaseBuilder(bundled SQLite driver), KeyValueStore
│                           #   (NSUserDefaults), static color scheme, LocaleProvider (NSLocale)
└── commonTest/kotlin/…     # kotlin.test — use case, repository, ViewModel tests (JVM-executed)

androidApp/                 # plain com.android.application — thin entry point, unchanged behavior
├── src/main/…               MainActivity, CoupleMomentsApp (Application), AndroidManifest, res/
├── src/github/…              self-update-only manifest additions (FileProvider, receiver, permission)
└── build.gradle.kts          distribution flavors, signing, R8, Play Publisher — all as today

iosApp/                     # Xcode project — Swift wrapper only, no business logic
├── iosApp.xcodeproj
└── iosApp/                  iOSApp.swift, ContentView.swift (embeds ComposeUIViewController)
```

**No `core:*`/`feature:*` module split survives inside `composeApp`.** LoopGain uses one shared module
with package-level separation (`domain/`, `data/`, `ui/screens/…`) rather than one Gradle module per
layer. Recommendation: follow that — package boundaries are enough for an app this size, and it halves
the Gradle module count we'd otherwise have to keep multiplatform-converting (8 modules → 1 shared +
`androidApp` + `iosApp`). If module-level compile isolation is valued more than matching LoopGain,
each `core/*`/`feature/*` could instead become its own `com.android.kotlin.multiplatform.library`
module — flagged as an open decision in §8, not the default recommendation.

## 4. Key technical decisions

### 4.1 Domain layer — near-zero change

`core/domain` already has no Android dependency (`AGENTS.md` says so explicitly). Its 6 models, 8
repository interfaces, 12 use cases, and `VersionComparator` move into
`composeApp/src/commonMain/kotlin/org/neteinstein/couples/domain/` file-for-file, package-for-package.
Existing `core/domain` unit tests move into `commonTest` and should compile against `kotlin.test`
largely unchanged (JUnit4-style `@Test`/`assertEquals` mostly map directly; MockK does not exist for
`commonTest` — see §7).

### 4.2 Room → Room Multiplatform

Room 2.7+ (this project is already on Room `2.8.5`, per `gradle/libs.versions.toml`) supports
`commonMain` with a bundled SQLite driver (`androidx.sqlite:sqlite-bundled`) for iOS and JVM. Plan:

- `CardEntity`, `SeedMetadataEntity`, `CardDao`, `SeedMetadataDao`, `CoupleMomentsDatabase`
  (`@Database`), and its `Migration`s move into `commonMain` verbatim — Room's KSP processor runs
  against `commonMain` sources.
- Only database *construction* becomes `expect fun getDatabaseBuilder(): RoomDatabase.Builder<CoupleMomentsDatabase>`:
  the Android `actual` takes a `Context` (as today, via Koin), the iOS `actual` points at
  `NSHomeDirectory()` and installs the bundled SQLite driver.
- The reseed-on-version-mismatch logic in `QuestionRepositoryImpl` (compares `QuestionSeedData.VERSION`
  against `seed_metadata`, full delete+reinsert, re-applies hidden ids) is pure Kotlin over the DAO —
  no change needed once the DAO is common.
- **Risk:** this is the highest-risk single item in the migration — verify Room's iOS driver handles
  the existing `Migration` objects identically to Android's SQLite before trusting it with real user
  data. Write a commonTest that runs the full migration chain against an in-memory driver on both
  targets before cutting over.

### 4.3 SharedPreferences → expect/actual KeyValueStore

`ThemeModeRepositoryImpl`, `IntimacyGateRepositoryImpl`, `QuestionsForParentsRepositoryImpl` each wrap
one `SharedPreferences` value. LoopGain already solved exactly this shape
(`composeApp/src/{common,android,ios}Main/.../data/local/KeyValueStore.kt`): a small
`expect class KeyValueStore` with `getString`/`putString`/`getBoolean`/`putBoolean`, `actual`
implemented via `SharedPreferences` on Android and `NSUserDefaults` on iOS. Port that pattern
directly; the three repository impls change only their storage dependency, their public interface and
call sites are untouched.

### 4.4 GitHub self-update — Android-only feature, Ktor for the shared parts

- `GitHubUpdateRepositoryImpl`'s HTTP call (`HttpURLConnection` + `org.json`) becomes a Ktor
  multiplatform client: `ktor-client-core` + `ktor-client-content-negotiation` +
  `ktor-serialization-kotlinx-json` in `commonMain`, `ktor-client-okhttp` in `androidMain`,
  `ktor-client-darwin` in `iosMain` — identical to LoopGain's `libs.versions.toml` entries. Response
  parsing moves from manual `JSONObject`/`JSONArray` to a `@Serializable` data class.
- APK *installation* (`AppUpdateInstallerImpl`, `UpdateApkCleanupReceiver`, `FileProvider`,
  `REQUEST_INSTALL_PACKAGES`) has no iOS equivalent — Apple doesn't allow sideloaded installs. This
  stays Android-only, living in `androidMain` (or `androidApp` if it needs the Activity), gated by the
  same `AppUpdateInstaller` domain interface.
- Extend the existing `updatesEnabled` Koin boolean (currently `github` vs `playstore` flavor) so iOS
  wires `updatesEnabled = false` too — same mechanism `SettingsViewModel`/`SettingsScreen` already use
  to hide the "Updates" section for the `playstore` flavor, now also hides it on iOS. No new UI state
  needed.

### 4.5 Theme — dynamic color becomes expect/actual

`CoupleMomentsTheme` picks `dynamicLightColorScheme`/`dynamicDarkColorScheme` on API 31+, falling back
to a static scheme. Dynamic/wallpaper-based color has no iOS concept. Make the color-scheme selection
an `expect fun platformColorScheme(dark: Boolean): ColorScheme?` — Android `actual` keeps today's
dynamic-color-if-available logic, iOS `actual` returns `null` (always static scheme). `isSystemInDarkTheme()`
already works cross-platform via Compose Multiplatform, no change needed there.

### 4.6 Strings — Android resources → Compose Multiplatform resources

The 5 language `strings.xml` files (`en`, `pt`, `es`, `fr`, `de`) move from `app/src/main/res/values*/`
to `composeApp/src/commonMain/composeResources/values*/strings.xml` — CMP's resource system accepts
the same Android XML format, generates a common `Res.string.*` accessor usable from any target. This
is mechanical (move + regenerate accessors), the main work is updating every `stringResource(R.string.x)`
call site (already Compose-idiomatic, becomes `stringResource(Res.string.x)`).

### 4.7 Per-app language — recommend switching to an in-app picker

Android's per-app language framework (`localeConfig`, `AppCompatDelegate`/`LocaleManager`) has no iOS
counterpart. LoopGain sidesteps this entirely with an in-app language picker stored in its
`KeyValueStore`/settings, applied at the Compose layer rather than relying on the OS. Recommend the
same here: `LocaleProvider` becomes an interface backed by the KeyValueStore-stored preference
(default = system locale on first run), read by a `CompositionLocalProvider` wrapping the app content,
rather than Android's OS-level per-app language API. This is a small product/UX change on Android
(language switch happens in-app instead of deep-linking to system settings) — flagged as an open
decision in §8 since `AGENTS.md`/README currently describe the OS-level mechanism as intentional.

### 4.8 Navigation, DI, ViewModels — mechanical port

- Koin: `koin-core`, `koin-compose`, `koin-compose-viewmodel` in `commonMain`; `koin-android` stays
  `androidMain`-only (`startKoin` call in `CoupleMomentsApp`, Android-only). iOS needs an explicit
  `initKoin()` entry point called from `iOSApp.swift`'s `init()` (Kotlin/Native renames `init`-prefixed
  top-level functions on export — LoopGain hit and documented this in its `AGENTS.md`; keep
  `initKoin()` in its own file for the same reason).
  `AppModule.kt`'s per-module Koin includes (`dataModule`, `homeModule`, `gameModule`, `settingsModule`,
  …) collapse into a single `commonMain/di/AppModule.kt`, same `include(...)` composition pattern.
- `androidx.navigation:navigation-compose` has a Compose Multiplatform-published artifact
  (`org.jetbrains.androidx.navigation:navigation-compose`, used by LoopGain) — `Screen.kt`'s
  `sealed class Screen` and `AppNavigation.kt`'s single `NavHost` move to `commonMain` unchanged in
  shape.
- `HomeViewModel`, `GameViewModel`, `SettingsViewModel`, `MainViewModel` already follow
  `StateFlow`-based MVVM with no `Context` injected (per `AGENTS.md` convention) — straightforward
  `commonMain` moves. `androidx.lifecycle:lifecycle-viewmodel`/`lifecycle-runtime-compose` both publish
  multiplatform artifacts.

### 4.9 `androidApp` — should look unchanged to users

`androidApp/build.gradle.kts` keeps: the `distribution` flavor dimension (`github`/`playstore`),
signing config reading the same 4 env vars, R8 full-mode minify + resource shrinking, `proguard-rules.pro`
(the Room `_Impl` `-keep` rule still applies, now referencing the same generated class from a
multiplatform-sourced `@Database`), `scripts/verify-obfuscation.sh`, and the `play {}` Gradle Play
Publisher block. `AndroidManifest.xml` (main + `src/github/` override), `MainActivity`,
`CoupleMomentsApp` (Application subclass calling `initKoin(androidContext)`), and all `res/` assets
(icons, `locale_config.xml` if §4.7 keeps it) stay in `androidApp`. The only change from today: this
module now depends on `composeApp` for all UI/domain/data code instead of the current 8
`core:*`/`feature:*` project dependencies.

### 4.10 `iosApp` — new

Minimal Swift wrapper per LoopGain's pattern: `iOSApp.swift` calls `InitKoinKt.doInitKoin()`,
`ContentView.swift` embeds the Compose UI via `ComposeUIViewController` (or `UIKitViewController`
depending on the CMP version's exported API), no CocoaPods — a Run Script build phase calls
`./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`. Ship simulator-only first (no Apple
Distribution signing configured, same as LoopGain's current state per its README) — see §8 for when to
invest in a real device/App Store signing pipeline.

## 5. Migration map (old → new)

| Old path | New path | Notes |
|---|---|---|
| `core/domain/**` | `composeApp/src/commonMain/kotlin/org/neteinstein/couples/domain/**` | verbatim |
| `core/domain/src/test/**` | `composeApp/src/commonTest/kotlin/.../domain/**` | JUnit4 → kotlin.test |
| `core/data/.../local/{CardEntity,SeedMetadataEntity,CardDao,SeedMetadataDao,CoupleMomentsDatabase,CardMapper}.kt` | `composeApp/src/commonMain/.../data/local/**` | Room KMP; DB builder becomes expect/actual |
| `core/data/.../source/QuestionSeedData.kt` | `composeApp/src/commonMain/.../data/source/QuestionSeedData.kt` | verbatim |
| `core/data/.../repository/{QuestionRepositoryImpl,UsedQuestionsRepositoryImpl}.kt` | `composeApp/src/commonMain/.../data/repository/**` | verbatim, DAO now common |
| `core/data/.../repository/{ThemeModeRepositoryImpl,IntimacyGateRepositoryImpl,QuestionsForParentsRepositoryImpl}.kt` | `composeApp/src/commonMain/.../data/repository/**` | swap `SharedPreferences` → `KeyValueStore` |
| `core/data/.../repository/GitHubUpdateRepositoryImpl.kt` | `composeApp/src/commonMain/.../data/repository/GitHubUpdateRepositoryImpl.kt` | `HttpURLConnection`/`org.json` → Ktor + kotlinx.serialization |
| `core/data/.../locale/LocaleProviderImpl.kt` | `composeApp/src/{android,ios}Main/.../data/locale/LocaleProviderImpl.kt` | expect/actual, or KeyValueStore-backed common impl (§4.7) |
| `core/data/.../installer/AppUpdateInstallerImpl.kt`, `receiver/UpdateApkCleanupReceiver.kt` | `composeApp/src/androidMain/.../data/installer/**` | Android-only, actual of a common interface |
| `core/data/.../di/DataModule.kt` | folded into `composeApp/src/commonMain/.../di/AppModule.kt` | |
| `core/ui/.../theme/{Color,Theme,Typography}.kt` | `composeApp/src/commonMain/.../ui/theme/**` | dynamic color → expect/actual |
| `core/ui/.../animation/OriginReveal.kt` | `composeApp/src/commonMain/.../ui/components/OriginReveal.kt` | verbatim if using only common Compose APIs |
| `feature/splash/**` | `composeApp/src/commonMain/.../ui/screens/splash/**` | |
| `feature/home/**` (screen+VM+DI) | `composeApp/src/commonMain/.../ui/screens/home/**` + `ui/viewmodel/` | |
| `feature/game/**` | `composeApp/src/commonMain/.../ui/screens/game/**` | |
| `feature/settings/**` | `composeApp/src/commonMain/.../ui/screens/settings/**` | drop `updatesEnabled` special-casing changes only if §4.4 needs it |
| `app/src/main/kotlin/.../{CoupleMomentsApp,MainActivity,MainViewModel}.kt` | `androidApp/src/main/kotlin/**` | `MainViewModel` may move to commonMain if it holds no Android-only state |
| `app/src/main/kotlin/.../navigation/**` | `composeApp/src/commonMain/.../ui/navigation/**` | |
| `app/src/main/kotlin/.../di/AppModule.kt` | merges into `composeApp/src/commonMain/.../di/AppModule.kt` | |
| `app/src/main/AndroidManifest.xml`, `app/src/github/AndroidManifest.xml`, `app/src/main/res/**` (minus strings) | `androidApp/src/main/**`, `androidApp/src/github/**` | unchanged |
| `app/src/main/res/values*/strings.xml` | `composeApp/src/commonMain/composeResources/values*/strings.xml` | §4.6 |
| `app/proguard-rules.pro`, `scripts/verify-obfuscation.sh` | `androidApp/proguard-rules.pro`, `scripts/verify-obfuscation.sh` | unchanged content |
| n/a | `iosApp/**` | new |

## 6. Phased execution plan

Do not big-bang this. Each phase should land as its own PR, keep `androidApp` shipping green the whole
way, and only add iOS-visible surface once Android parity is proven.

1. **Phase 0 — scaffolding.** Add `composeApp` (empty KMP+CMP module, Android target only for now),
   wire `settings.gradle.kts`, get `./gradlew :composeApp:build` green with nothing in it. Bump/confirm
   `gradle/libs.versions.toml` entries for `kotlin-multiplatform`, `compose-multiplatform`,
   `android-kotlin-multiplatform-library`, matching LoopGain's versions (Kotlin 2.4.x, Compose
   Multiplatform 1.11.x, AGP 9.x — this repo is already on AGP `9.4.0`/Kotlin `2.4.20`, ahead of
   LoopGain's pinned versions, so pin to what's already validated on this codebase where newer).
2. **Phase 1 — domain move.** Move `core/domain` into `composeApp/commonMain/domain` +
   `commonTest`. Delete `core/domain` module. `core/data`/`feature/*`/`app` now depend on
   `composeApp` for domain types instead of `core:domain` — no behavior change, pure refactor,
   should be a no-op diff for CI.
3. **Phase 2 — data move.** Move `core/data` in the order: `QuestionSeedData` (no deps) → Room
   entities/DAOs/`@Database` with the `expect`/`actual` `DatabaseBuilder` split → `KeyValueStore`
   expect/actual + the three `SharedPreferences`-backed repos → Ktor-based
   `GitHubUpdateRepositoryImpl` → `AppUpdateInstallerImpl`/receiver (androidMain-only). Delete
   `core/data`. This is the phase to spend the most test effort on (§4.2's migration-chain test).
4. **Phase 3 — UI move.** Move `core/ui` theme (with dynamic-color expect/actual), then each
   `feature/*` screen+ViewModel+DI module into `composeApp/commonMain/ui/screens/*`, one feature per
   PR (`splash` → `settings` → `game` → `home`, cheapest/lowest-risk first). Strings move to
   `composeResources` in the same PR as the first screen that uses them, then incrementally per
   feature. Delete `core/ui` and each `feature/*` module as it's absorbed.
5. **Phase 4 — androidApp cutover.** Rename/restructure `app/` into `androidApp/` (plain
   `com.android.application`, no more `kotlin.multiplatform` plugin conflict), pointing at
   `composeApp` for everything. Verify: flavors, signing, R8/`verify-obfuscation.sh`, ktlint, all
   still pass identically. This PR should have **zero user-visible behavior change** — it's the
   parity checkpoint before iOS work starts.
6. **Phase 5 — iOS bring-up.** Add `iosMain` targets to `composeApp` (`iosArm64`,
   `iosSimulatorArm64`), implement the iOS `actual`s (`KeyValueStore` via `NSUserDefaults`, Room's
   bundled-SQLite `DatabaseBuilder`, Ktor Darwin engine, static-only theme, `LocaleProvider`). Add
   `iosApp/` Xcode project + Swift wrapper. Target: builds and runs on the iOS Simulator with full
   feature parity except self-update (§4.4).
7. **Phase 6 — CI/CD.** Rewrite `.github/workflows/pr.yml`/`release.yml` per §7 below. Add a
   macOS runner job for iOS build (+ tests once there are iOS-specific tests).
8. **Phase 7 — cleanup.** Remove any now-dead Gradle module scaffolding, update `README.md`/
   `AGENTS.md`/`CLAUDE.md`(if any)/`docs/agents/*.md` to describe the new module layout — these
   currently document the exact per-layer module structure this migration removes, and `AGENTS.md`'s
   "Adding a new feature module" section needs a full rewrite for the collapsed-module shape.

Each phase is independently mergeable and leaves `main` green — this lets the migration proceed over
several PRs/weeks without a long-lived feature branch drifting out of sync.

## 7. CI/CD changes

Mirror LoopGain's 3-workflow split (`pr-checks.yml`, `release.yml`, `deploy-stores.yml`), adapted for
this repo's two-flavor Android distribution:

- **PR checks:** `ktlintCheck` (now spans `commonMain`/`androidMain`/`iosMain` source sets — ktlint's
  Gradle plugin already supports this per source set), `:composeApp:testDebugUnitTest` /
  `:composeApp:allTests` (commonTest run on the JVM, replacing today's per-module
  `testDebugUnitTest`), `:androidApp:assembleDebug`, `:androidApp:assembleRelease` +
  `verify-obfuscation.sh` (unchanged), and a new iOS job (`xcodebuild build … -sdk
  iphonesimulator`, macOS runner) — non-blocking at first per LoopGain's own "not yet required"
  status, promote to required once green consistently.
- **Coverage:** AGP's built-in `enableUnitTestCoverage` only covers Android variants; `commonMain`
  code exercised by `commonTest` needs a KMP-aware coverage tool (Kotlinx **Kover**) merged with the
  Android report before uploading to Codecov, since a meaningful fraction of covered code (domain,
  data) now lives in `commonMain` rather than an Android-variant module.
- **Release:** unchanged secrets/flow for Android (`KEYSTORE_BASE64`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/
  `KEY_PASSWORD`, `ANDROID_PUBLISHER_CREDENTIALS`), still builds both `github`/`playstore` flavor
  APK/AAB from `androidApp`. iOS release/TestFlight publishing is a separate, later decision (§8) —
  not required for this migration to be "done."

## 8. Open decisions (need your call before/during implementation)

1. **Module granularity inside the shared code:** one `composeApp` module (LoopGain's approach,
   recommended) vs. converting each existing `core:*`/`feature:*` module individually to
   `com.android.kotlin.multiplatform.library`. The former is less work and matches the reference
   exactly; the latter preserves today's compile-isolation/dependency-direction enforcement at the
   cost of doing this migration 8 times over.
2. **Per-app language (§4.7):** keep Android's OS-level per-app language mechanism (divergent
   platform behavior, LocaleProvider stays expect/actual with real platform hooks) vs. switch to an
   in-app picker on both platforms (one behavior, matches LoopGain, but changes Android UX — no
   longer deep-links to system language settings).
3. **iOS release target:** how far to take iOS — simulator-only for now (LoopGain's current state) or
   invest in Apple Developer Program enrollment + signing to actually ship on TestFlight/App Store.
   This affects whether Phase 6 needs App Store Connect API credentials as a new set of secrets.
4. **Self-update on iOS:** confirmed out of scope per §4.4 (Apple disallows sideloaded installs) —
   iOS always ships through TestFlight/App Store once that's set up. Flag if there's a different
   expectation here (e.g., an iOS "what's new" screen instead).
5. **Timeline/sequencing:** whether to land phases as they're ready (recommended, keeps `main`
   shippable throughout) or stage the whole migration on a long-lived branch and merge once iOS
   parity is reached.

## 9. Risk register

| Risk | Mitigation |
|---|---|
| Room Multiplatform + bundled SQLite driver behaves differently from Android's SQLite for existing `Migration`s | Dedicated commonTest running the full migration chain against both an Android instrumented DB and an in-memory bundled-SQLite DB before Phase 2 lands |
| R8 keep rule for Room's `_Impl` (`app/proguard-rules.pro`) silently breaks once `@Database` is KSP-processed from a multiplatform source set | Re-run `scripts/verify-obfuscation.sh` in Phase 4 as the go/no-go gate, not just "release build succeeds" |
| ktlint / `.editorconfig` coverage gaps for new `androidMain`/`iosMain`/`commonMain` source sets | Confirm `ktlintCheck` actually visits all three before treating Phase 0 as done |
| MockK has no `commonTest` target | ViewModel/repository tests using MockK either stay JVM-only (`androidUnitTest`) or migrate to a pure-Kotlin fake/stub pattern for `commonTest` — decide per-test, don't block the phase on rewriting every test |
| Coverage tooling swap (AGP built-in → Kover) changes Codecov numbers/format | Land the tooling swap as its own PR before or independent of the migration, so the coverage diff isn't conflated with migration risk |
| CI cost/time: macOS runners are slower/costlier than Linux | Keep the iOS CI job non-blocking until Phase 5 is stable, per LoopGain's own current posture |
| Scope creep: this repo has more features than LoopGain (intimacy gate, questions-for-parents, self-update) that LoopGain's pattern doesn't have a 1:1 answer for | This plan resolves each one explicitly in §4; nothing is deferred silently |

## 10. Effort signal

Roughly in LoopGain-equivalent terms: Phases 0–4 (Android parity on the new module shape) are the bulk
of the work and the only phases required to de-risk everything else — they touch every existing file
but change behavior in none of them. Phase 5 (iOS bring-up) is comparable in size to Phases 0–4
combined, since it's where every expect/actual actually gets a second implementation for the first
time. Phases 6–7 are small, mechanical, and can run in parallel with Phase 5.
