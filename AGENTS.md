# AGENTS.md

This file provides guidance to AI coding agents (Claude Code and others) when working with code in this repository.

## Project Overview

Couple Moments helps couples spark meaningful conversations by presenting randomized, swipeable conversation-starter questions in categories like Ice Breakers, Memories, Values, Future Dreams, Daily Life, and Intimacy.

- **Package:** `org.neteinstein.couples`
- **Language:** Kotlin Multiplatform, UI in Compose Multiplatform + Material3
- **Targets:** Android (Play Store + direct APK), iOS (simulator), Web (wasmJs, deployed to [couples.loopgain.org](https://couples.loopgain.org))
- **minSdk 32 / targetSdk 37 / compileSdk 37** (see `androidApp/build.gradle.kts` and `build-logic/convention/src/main/kotlin/kmp-library.gradle.kts`)

## Commands

```bash
./gradlew :androidApp:assembleDebug           # compile Android (mirrors CI "Compile" job)
./gradlew :webApp:wasmJsBrowserDistribution   # build the web app (mirrors CI "Web" job)
./gradlew testAndroidHostTest :androidApp:testGithubDebugUnitTest :androidApp:testPlaystoreDebugUnitTest  # all unit tests
./gradlew koverXmlReport                      # tests + aggregated coverage (mirrors CI "Code Coverage" job)
./gradlew :androidApp:assembleRelease && ./scripts/verify-obfuscation.sh  # minified release (mirrors CI "Minified Release")
```

> **Task names:** every `core:*`/`feature:*`/`app` module is a KMP android-library, whose JVM/Android unit tests live in the `androidHostTest` source set — so they run under **`testAndroidHostTest`**, which also runs everything in `commonTest`. `androidApp` is the one classic `com.android.application` module left and it builds two product flavors (see Releases below), so its test task is flavor-scoped: `testGithubDebugUnitTest`/`testPlaystoreDebugUnitTest`. The plain `testDebugUnitTest`/`createDebugUnitTestCoverageReport` names no longer resolve anywhere in this build.

Run tests for a single module:
```bash
./gradlew :feature:home:testAndroidHostTest
./gradlew :core:domain:testAndroidHostTest
```

Run a single test class or method (`--tests` works with any of the module targets above):
```bash
./gradlew :feature:home:testAndroidHostTest --tests "org.neteinstein.couples.feature.home.HomeViewModelTest"
./gradlew :feature:home:testAndroidHostTest --tests "*.HomeViewModelTest.nextQuestion advances to next question"
```

Build the other two platforms:
```bash
./gradlew :app:compileKotlinIosSimulatorArm64   # shared code for iOS
xcodebuild build -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO
./gradlew :webApp:wasmJsBrowserRun              # run the web app locally
```

`./gradlew ktlintCheck` runs the [ktlint Gradle plugin](https://github.com/JLLeitschuh/ktlint-gradle) (applied to every module through the `kmp-library`/`kmp-web-application` convention plugins in `build-logic`, plus the root `.editorconfig`), across all of `commonMain`/`androidMain`/`iosMain`/`wasmJsMain` — the formatting/style gate CI relies on. Run `./gradlew ktlintFormat` to auto-fix violations.

CI (`.github/workflows/pr.yml`) runs seven independent jobs on every PR into `main`/`develop`: `ktlint`, `:androidApp:assembleDebug`, `:androidApp:assembleRelease` + `scripts/verify-obfuscation.sh` (see [Obfuscation and shrinking](#obfuscation-and-shrinking) — `assembleDebug` never runs R8, so this is what catches a broken keep rule), the unit tests, `koverXmlReport` (uploaded to Codecov), an **iOS** job (macOS runner: compiles both iOS targets and builds `iosApp` against the simulator SDK with `CODE_SIGNING_ALLOWED=NO`), and a **Web** job (the same wasmJs distribution the Pages deploy publishes).

Coverage comes from [kotlinx-kover](https://github.com/Kotlin/kotlinx-kover), applied by the `kmp-library` convention plugin and aggregated at the root project. AGP's built-in `enableUnitTestCoverage` was removed because it only instruments Android variants, and essentially all of this project's code now lives in `commonMain` source sets it cannot see. The root `kover {}` block excludes `QuestionSeedData` (~6,500 lines of card content) and generated classes so the headline number reflects real logic.

`.github/workflows/deploy-pages.yml` publishes the wasmJs build to GitHub Pages on every push to `main`. It needs repo Settings → Pages → Source set to "GitHub Actions"; the custom domain is configured in that same screen (an Actions-sourced deployment ignores the repo-root `CNAME` file entirely).

## Releases

`androidApp` builds two product flavors under the `distribution` flavor dimension (`androidApp/build.gradle.kts`): **`github`**, the direct-APK build distributed via GitHub Releases, and **`playstore`**, submitted to the Play Store. `assembleRelease`/`assembleDebug` are aggregate tasks that build both flavors; use `assembleGithubRelease`/`assemblePlaystoreRelease` to target one. Its unit-test tasks are flavor-scoped — see the task-names note under Commands.

Pushing to `main` (or a manual `workflow_dispatch`) triggers `.github/workflows/release.yml`, which runs `ktlintCheck` + the unit tests, builds signed `:androidApp:assembleRelease` APKs for both flavors plus the `playstore` flavor's `bundlePlaystoreRelease` App Bundle, and publishes all three as assets on a single GitHub Release tagged `v<major>.<minor>.<run number>` (the `Resolve version name` step reads `couples.versionName` out of `gradle.properties`, keeps its `major.minor`, and substitutes the run number for the patch segment — so bumping the release line is a one-line edit there). Signing requires four repo secrets: `KEYSTORE_BASE64` (base64-encoded `.jks`/`.keystore` file), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. The workflow fails fast if any are missing. `androidApp/build.gradle.kts`'s `versionCode`/`versionName` and `signingConfigs["release"]` read `APP_VERSION_CODE`/`APP_VERSION_NAME`/`KEYSTORE_FILE`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` env vars set by the workflow, falling back to debug signing, `versionCode` 1, and `gradle.properties`' `couples.versionName` for local builds. The `playstore` APK is uploaded to the GitHub Release alongside the `github` one purely for archival; the `playstore` AAB is the one that actually matters for Play Console, whether uploaded there by hand or by the automated step below.

After the GitHub Release step, the workflow also runs `publishPlaystoreReleaseBundle` (from the [Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher) plugin, `com.github.triplet.play` — applied in `androidApp/build.gradle.kts`) to upload the `playstore` flavor's signed release App Bundle straight to the Play Console via the Google Play Developer API. This step only runs if the `ANDROID_PUBLISHER_CREDENTIALS` repo secret is set (the raw JSON contents of a Play Console service account key with "Release to production, exclude devices, and use Play App Signing" — or at least test-track — permission for this app); if it's unset, the workflow logs a notice and skips the step instead of failing, so forks/clones without Play Console access still get a GitHub Release. Publishes go to the `internal` track by default, overridable via the `PLAY_TRACK` repo variable, so a release never reaches production without an explicit promotion in the Play Console. `androidApp/build.gradle.kts`'s `play { }` block is disabled (`enabled.set(false)`) by default and re-enabled only for the `playstore` flavor via `playConfigs` — the `github` flavor shares the same `applicationId` and must never be uploaded. Note: the Play Developer API can only publish updates to an app that already has at least one release uploaded manually through the Play Console; do that first before this automation will work — download the `.aab` asset from the GitHub Release (built regardless of whether `ANDROID_PUBLISHER_CREDENTIALS` is set) and upload it by hand under Play Console → your app → Production/Testing → Create release for that one-time first upload; every release after that is handled automatically by this step.

**Troubleshooting a `403 PERMISSION_DENIED` from `publishPlaystoreReleaseBundle`:** the request that fails is `POST .../applications/<applicationId>/edits` (the very first call Gradle Play Publisher makes, before it touches any track), so the cause is always the service account's standing in the Play Console, not this repo's Gradle config. Check, in order: (1) the service account (the `client_email` inside the `ANDROID_PUBLISHER_CREDENTIALS` JSON) has been invited as a user under Play Console → Users and permissions, with access to *this specific app* (not just "all apps" from a different app list) and the "Release to production, exclude devices, and use Play App Signing" permission (or at least a testing-track release permission); (2) a first release for `applicationId` (`androidApp/build.gradle.kts`) has been uploaded manually through the Play Console — the API can only publish updates, never the initial listing; (3) the Google Play Android Developer API is enabled on the Google Cloud project the service account key belongs to; (4) a newly-granted permission can take a few hours to propagate on Google's side, so a re-run after a short wait can resolve it with no config change at all.

The `github` flavor keeps an in-app self-update flow: the Settings screen's "Update to latest" button (`feature:settings`'s `SettingsViewModel`/`SettingsScreen`) checks `https://api.github.com/repos/neteinstein/CoupleMoments/releases/latest` (`core:data`'s `GitHubUpdateRepositoryImpl`), compares the tag against the installed `versionName` (`core:domain`'s `isNewerVersion`), and downloads/installs the APK asset via `AppUpdateInstallerImpl` (a `FileProvider`-backed install flow gated by the `REQUEST_INSTALL_PACKAGES` permission, the `FileProvider`, and `UpdateApkCleanupReceiver` — all declared only in `androidApp/src/github/AndroidManifest.xml`, merged in for that flavor only). The `playstore` flavor has this feature stripped entirely: `androidApp/build.gradle.kts` sets `BuildConfig.UPDATES_ENABLED = false` for it, wired via Koin as a named `"updatesEnabled"` boolean (`app/.../di/AppModule.kt` → `feature/settings/.../di/SettingsModule.kt`) into `SettingsViewModel`, which skips the update check entirely and drives `SettingsUiState.updatesEnabled = false` so `SettingsScreen` never renders the "Updates" section; the Play Store flavor's manifest carries none of the permission/provider/receiver above since they're only declared in the `github` source set.

### Obfuscation and shrinking

The `release` build type (`androidApp/build.gradle.kts`) is minified, obfuscated **and** resource-shrunk for both flavors — `isMinifyEnabled = true` + `isShrinkResources = true`, with R8 in full mode (`android.enableR8.fullMode=true` in `gradle.properties`). `debug` is untouched, so `assembleDebug` and every unit test run against unshrunk code; nothing about R8 is exercised by the debug pipeline.

`androidApp/proguard-rules.pro` deliberately keeps almost nothing. Everything this app resolves through ordinary Kotlin calls — Koin's `single { }`/`get()`/`by inject()` DSL (which captures `KClass` literals at compile time), use cases, ViewModels, domain models, Compose UI, SQLDelight's generated database/query classes (constructed directly, not resolved reflectively) — is renamed consistently by R8 at both the definition and the call site, so keeping it only inflates the APK and weakens the obfuscation. **Only add a `-keep` for something reached by name at runtime**, and say in a comment what resolves it:

- **Manifest-declared components** (`MainActivity`, `CoupleMomentsApp`, `UpdateApkCleanupReceiver`, the `FileProvider`) need no rule here — AGP generates keep rules from the merged manifest.
- **Resource shrinking** is safe only because nothing looks a resource up dynamically; every reference is a static `R.*` one. Adding a `Resources.getIdentifier` call anywhere means either a `tools:keep` entry or turning `isShrinkResources` back off.

`scripts/verify-obfuscation.sh` reads R8's `mapping.txt` for each release variant and fails if no `org.neteinstein.couples.*` class was renamed (i.e. obfuscation silently stopped happening). The "Minified Release" PR job and the release workflow both run it — an over-broad `-keep` or a flipped `isMinifyEnabled` therefore fails CI rather than shipping. (On the `kmp` branch: this section described Room's reflective `Class.forName` lookup, which needed a matching `-keep`/verification pair; core:data's KMP migration replaced Room with SQLDelight, which needs neither.)

Obfuscated stack traces need the matching mapping file, and R8 emits a different one per build. `-keepattributes SourceFile,LineNumberTable` + `-renamesourcefileattribute SourceFile` keep traces line-accurate without shipping the original `.kt` filenames, and `.github/workflows/release.yml` attaches `*-mapping.txt` for both flavors to every GitHub Release (the `.aab` needs no asset — AGP embeds its mapping in the bundle, so the Play Console de-obfuscates that flavor itself). Run a trace through R8's `retrace` with the mapping from the exact release the crash came from.

`.github/dependabot.yml` runs weekly `gradle` and `github-actions` update checks.

## Architecture

Kotlin Multiplatform, Gradle multi-module, wired via `settings.gradle.kts`. Every `core:*`/`feature:*`/`app` module is a `com.android.kotlin.multiplatform.library` targeting Android + iOS + wasmJs; the three platform entry points are thin wrappers around `app`'s shared `App()` composable.

```
build-logic         # convention plugins (kmp-library / kmp-compose-library / kmp-web-application)
app                 # shared KMP aggregator: App(), appModule (Koin), navigation graph, MainViewModel
androidApp          # Android application shell: MainActivity, Application, manifest, res/, flavors, R8
iosApp              # Xcode project wrapper (Swift only, no logic) — no build.gradle.kts
webApp              # wasmJs entry point: main(), index.html, favicon
core/domain         # pure Kotlin: models, repository interfaces, use cases — no Android/Compose deps
core/data           # repository implementations + data sources, depends on core:domain
core/ui             # shared Compose theme, OriginReveal, PagerIndicator, PlatformBackHandler, arrowKeyNavigation
feature/splash      # splash screen (animated logo, auto-navigates after a delay)
feature/home        # question-card deck + grid + HomeViewModel
feature/game        # game-mode card deck + GameViewModel
feature/settings    # settings screen (theme, language, card reset, updates, about)
```

**Dependency rules (enforced by module graph, not lint):**
- `feature/*` depends only on `core:domain` and `core:ui` — never on another `feature/*` module.
- `core:data` depends on `core:domain` and implements its repository interfaces.
- `core:ui` depends only on Compose/Material3 (no domain/data deps).
- `app` depends on every module and is the only place they're wired together (Koin modules, `NavHost`).
- `androidApp`/`iosApp`/`webApp` depend **only** on `app` — no platform entry point reaches into a `core:*`/`feature:*` module directly.

The migration that produced this shape, what diverged from its original plan, and what is still open are recorded in [`docs/kmp-cmp-migration-plan.md`](docs/kmp-cmp-migration-plan.md).

### Source sets and expect/actual

Put code in `commonMain` by default. A platform source set is only for something that genuinely has no common form; the current list is short and worth knowing:

| Concern | Common declaration | Android | iOS | Web |
|---|---|---|---|---|
| Storage bindings | `core:data` `platformDataModule` | SQLDelight `AndroidSqliteDriver`, `SharedPreferencesSettings` | SQLDelight `NativeSqliteDriver`, `NSUserDefaultsSettings` | `WebCardDao` + `StorageSettings` (`localStorage`) |
| Blocking-IO dispatcher | `core:data` `ioDispatcher` | `Dispatchers.IO` | `Dispatchers.Default` (`IO` is internal on Native) | `Dispatchers.Default` (single event loop) |
| OS locale | `core:data` `LocaleProviderImpl` | `Locale.getDefault()` | `NSLocale.currentLocale` | `navigator.language` |
| Self-update | `core:data` `platformDataModule` | Ktor + GitHub Releases + `FileProvider` install | no-op | no-op |
| Dynamic color / status bar | `core:ui` `platformColorScheme` / `PlatformStatusBarEffect` | Material You (API 31+), `WindowCompat` | static scheme, no-op | static scheme, no-op |
| System back gesture | `core:ui` `PlatformBackHandler` | `androidx.activity` `BackHandler` | no-op | no-op |
| Language settings deep link / version name | `feature:settings` `SettingsPlatformActions` | `ACTION_LOCALE_SETTINGS`, `PackageManager` | none, `CFBundleShortVersionString` | none, placeholder |
| "Install the Android app" banner | `app` `PlatformInstallAppBanner` | renders nothing | renders nothing | dismissible Play Store banner |

**SQLDelight has no wasmJs driver.** `CardDao`/`SeedMetadataDao` were always persistence-agnostic interfaces, so the web build binds them to `WebCardDao`/`WebSeedMetadataDao` instead: the card rows live in memory (re-seeded from `QuestionSeedData` every page load, which costs nothing since it's a compiled-in constant) and only the hidden-id set is persisted, through `localStorage`. Android and iOS both get the real SQLDelight database. If a wasm driver ever lands, swapping it in needs no change to either interface or any caller.

### Icons and text glyphs

**Never use an emoji or a Unicode arrow/symbol as UI content.** Compose Multiplatform's Wasm/Skia text renderer has no emoji-font fallback the way Android and iOS do, so every such glyph renders as a "tofu" box on the web build. Use a Material vector icon instead — they render identically on all three targets. Category icons are mapped in `feature:home`'s `HomeScreen.kt` (`QuestionCategory.icon()`) rather than on the `core:domain` model, which has no Compose dependency.

### Data flow (question retrieval)

`QuestionSeedData` (object in `core/data`, `core/data/src/commonMain/kotlin/.../data/source/QuestionSeedData.kt`) holds hardcoded question lists per language (`en`, `pt`, `es`, `fr`, `de`) as the single content source, plus a `VERSION` constant. `QuestionRepositoryImpl` persists that content into a local database (`CoupleMomentsDatabase`/`CardDao`, `core/data/.../local/`) and implements `QuestionRepository` (the `core:domain` interface); there is no network layer. On first use each process, it compares `QuestionSeedData.VERSION` against what's stored in the single-row `seed_metadata` table (`SeedMetadataDao`) and, on any mismatch, fully deletes and reinserts every card — not just an additive insert — so a question added, edited, *or removed* from `QuestionSeedData` reaches already-installed devices; cards already hidden (see `UsedQuestionsRepositoryImpl`) are re-marked hidden by id after the replace so a version bump doesn't silently un-hide them. **Bump `QuestionSeedData.VERSION` any time you change its content.** `GetQuestionsUseCase`/`GetRandomQuestionUseCase` sit on top of the repository interface and are what ViewModels actually call.

(On the `kmp` branch: `CoupleMomentsDatabase`/`CardDao`/`SeedMetadataDao` are now SQLDelight-backed — see `core/data/src/main/sqldelight/.../local/*.sq` for schema and `CardDaoImpl`/`SeedMetadataDaoImpl`/`DriverFactory` for the implementation — not the Room setup this paragraph originally described. Bump the version by editing the `.sq` files' `CREATE TABLE` and adding a numbered `.sqm` migration, never by hand-editing a generated class.)

Note: the set of supported languages is declared in three places that must be kept in step — `androidApp/src/main/res/xml/locale_config.xml` (Android's OS-level per-app language picker), `AppLanguage` in `core:domain` (the in-app picker iOS/Web use), and `QuestionSeedData`'s per-language content. All three currently cover `en`/`pt`/`es`/`fr`/`de`.

### DI wiring (Koin)

Each module that needs DI defines its own Koin module (`dataModule`, `homeModule`, …); `app/.../di/AppModule.kt` composes them into `appModule(updatesEnabled)`. `updatesEnabled` is a parameter rather than a `BuildConfig` read because `AppModule.kt` is commonMain code with no Android build variant behind it: `CoupleMomentsApp` passes its own `BuildConfig.UPDATES_ENABLED`, iOS/Web always pass `false`.

`core:data` splits the same way: everything platform-agnostic lives in its `dataModule`, and each target's `platformDataModule` actual binds the storage drivers, `Settings` stores, `LocaleProvider` and self-update implementations (see the expect/actual table above).

Koin is started per platform: `CoupleMomentsApp` (the Android `Application`) calls `startKoin` directly so it can register `androidContext()`/`androidLogger()`; iOS and Web call the zero-argument `doInitKoin()` in `app/.../di/InitKoin.kt`. It is named `doInitKoin`, not `initKoin`, because Kotlin/Native's Objective-C exporter treats an `init`-prefixed top-level function as an initializer and renames it unpredictably.

Use Koin's multiplatform APIs — `org.koin.core.module.dsl.viewModel` and `org.koin.compose.viewmodel.koinViewModel`, never the `koin-androidx-*` equivalents. When adding a feature module with a ViewModel, add its own `*Module.kt` under `feature/<name>/.../di/` and `include` it from `AppModule.kt`.

### Navigation

Single `NavHost` in `app/.../navigation/AppNavigation.kt` (commonMain), routes defined as a `sealed class Screen` in `Screen.kt` (`Splash`, `Home`, `Settings`). Splash pops itself off the back stack (`popUpTo(inclusive = true)`) once it navigates to Home.

Navigation comes from `org.jetbrains.androidx.navigation:navigation-compose` — the JetBrains-published KMP fork. Same `androidx.navigation.*` package, but the mainline `androidx.navigation:navigation-compose` artifact publishes no wasmJs variant at all.

### MVVM conventions

- Each screen that needs state has a `ViewModel` (see `HomeViewModel`) exposing a single `StateFlow<UiState>` (e.g. `HomeUiState`), updated via `MutableStateFlow.update { }`.
- Screens read state with `collectAsStateWithLifecycle()`, not `collectAsState()`.
- ViewModels use `viewModelScope`, never `rememberCoroutineScope()`; no `Context` is passed into ViewModels.
- Use cases are single-purpose, named verb+noun (`GetRandomQuestionUseCase`), and injected into ViewModels through Koin `factory { }`.
- `SettingsScreen` currently has no ViewModel — it's a static/stateless screen that delegates to Android system settings for language changes.

### Theming

`CoupleMomentsTheme` (in `core/ui`) asks `platformColorScheme(darkTheme)` for a Material3 dynamic-color scheme and falls back to the static `lightColorScheme`/`darkColorScheme` when it returns null — which is always the case off Android, and on Android below API 31. `PlatformStatusBarEffect` adjusts the Android status bar and no-ops elsewhere. Never hardcode colors in Composables — always reference `MaterialTheme.colorScheme.*`. All user-facing strings belong in a module's `src/commonMain/composeResources/values*/strings.xml` and are read with `org.jetbrains.compose.resources.stringResource(Res.string.foo)` — **not** Android's `R.string`. Each module generates its own `Res` class (`compose.resources { packageOfResClass = ... }`); `core:ui` sets `publicResClass = true` so `feature:splash` can reach its logo drawable. `androidApp/src/main/res/` keeps only what the Android platform itself needs: launcher icons, themes, `locale_config.xml`, `app_name`.

## Adding a new feature module

1. Create `feature/<name>/` mirroring `feature/home`'s `build.gradle.kts` (depends on `core:domain` + `core:ui` only).
2. Register it in `settings.gradle.kts` and add it as a dependency in `androidApp/build.gradle.kts`.
4. If it needs a ViewModel, add a Koin module under `feature/<name>/.../di/` and `include` it in `app/.../di/AppModule.kt`.
5. Add a route to `Screen.kt` and a `composable(...)` entry in `AppNavigation.kt`.
6. Add unit tests (ViewModel/use case/repository) before opening a PR — see Testing conventions below.

## Testing conventions

- **Where a test goes:** `src/commonTest/` (runs on every target, `kotlin.test` + hand-written fakes) is the default. `src/androidHostTest/` (JUnit 4 + MockK + Robolectric) is for tests that genuinely need the JVM/Android — MockK has no Kotlin/Native or wasmJs support, so any MockK-based test *must* live there.
- Unit tests in `androidHostTest` use JUnit 4 + MockK (`mockk()`, `coEvery`, `coVerify`) + `kotlinx-coroutines-test`.
- Coroutine-driven tests use `StandardTestDispatcher`, set via `Dispatchers.setMain()`/`resetMain()` in `@Before`/`@After`, and `runTest { }` with `testDispatcher.scheduler.advanceUntilIdle()` to drive pending coroutines.
- Test method names are backtick-quoted sentences: `` fun `nextQuestion advances to next question`() ``.
- Existing tests to use as templates: `core/domain/.../GetRandomQuestionUseCaseTest.kt` (use case + mocked repository) and `feature/home/.../HomeViewModelTest.kt` (ViewModel + mocked use case + coroutine dispatcher setup).

## Agent orchestration

This repo defines five agent roles — Developer, QA, Architect, Security Manager, Product Manager — as Claude Code subagents (`.claude/agents/`). Architect, Security Manager, and Product Manager delegate to Developer and QA rather than implementing everything themselves. See [`docs/agents/README.md`](docs/agents/README.md) for the full orchestration model, and `docs/agents/<role>.md` for each role's canonical instructions.
