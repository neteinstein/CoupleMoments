# AGENTS.md

This file provides guidance to AI coding agents (Claude Code and others) when working with code in this repository.

## Project Overview

Family Moments is an Android app that helps families spark meaningful conversations by presenting randomized, swipeable conversation-starter questions in categories like Ice Breakers, Memories, Values, Future Dreams, and Daily Life.

- **Package:** `org.neteinstein.family`
- **Language:** Kotlin, UI in Jetpack Compose + Material3
- **minSdk 32 / targetSdk 37 / compileSdk 37** (see `app/build.gradle.kts` and per-module `build.gradle.kts`)

## Commands

```bash
./gradlew assembleDebug                  # compile (mirrors CI "Compile" job)
./gradlew testDebugUnitTest              # run all unit tests (mirrors CI "Unit Tests" job)
./gradlew createDebugUnitTestCoverageReport  # run tests + generate AGP built-in coverage reports (mirrors CI "Code Coverage" job)
```

Run tests for a single module:
```bash
./gradlew :feature:home:testDebugUnitTest
./gradlew :core:domain:testDebugUnitTest
```

Run a single test class or method (`--tests` works with any of the module targets above):
```bash
./gradlew :feature:home:testDebugUnitTest --tests "org.neteinstein.family.feature.home.HomeViewModelTest"
./gradlew :feature:home:testDebugUnitTest --tests "*.HomeViewModelTest.nextQuestion advances to next question"
```

`./gradlew ktlintCheck` runs the [ktlint Gradle plugin](https://github.com/JLLeitschuh/ktlint-gradle) (applied per-module, configured in each module's `build.gradle.kts` + root `.editorconfig`) — the formatting/style gate CI relies on. Run `./gradlew ktlintFormat` to auto-fix violations.

CI (`.github/workflows/pr.yml`) runs four independent jobs on every PR into `main`/`develop`: `ktlint`, `assembleDebug`, `testDebugUnitTest`, and `createDebugUnitTestCoverageReport` (coverage report uploaded to Codecov). Coverage comes from AGP's built-in `enableUnitTestCoverage = true` (set per-module in `buildTypes { debug { ... } }`) — there is no separate Jacoco plugin applied.

## Releases

`app` builds two product flavors under the `distribution` flavor dimension (`app/build.gradle.kts`): **`github`**, the direct-APK build distributed via GitHub Releases, and **`playstore`**, submitted to the Play Store. `assembleRelease`/`assembleDebug`/`testDebugUnitTest` etc. are aggregate tasks that build/test both flavors; use `assembleGithubRelease`/`assemblePlaystoreRelease` (etc.) to target one.

Pushing to `main` (or a manual `workflow_dispatch`) triggers `.github/workflows/release.yml`, which runs `ktlintCheck` + `testDebugUnitTest`, builds signed `assembleRelease` APKs for both flavors plus the `playstore` flavor's `bundlePlaystoreRelease` App Bundle, and publishes all three as assets on a single GitHub Release tagged `v1.0.<run number>`. Signing requires four repo secrets: `KEYSTORE_BASE64` (base64-encoded `.jks`/`.keystore` file), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. The workflow fails fast if any are missing. `app/build.gradle.kts`'s `versionCode`/`versionName` and `signingConfigs["release"]` read `APP_VERSION_CODE`/`APP_VERSION_NAME`/`KEYSTORE_FILE`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` env vars set by the workflow, falling back to debug signing and static defaults for local builds. The `playstore` APK is uploaded to the GitHub Release alongside the `github` one purely for archival; the `playstore` AAB is the one that actually matters for Play Console, whether uploaded there by hand or by the automated step below.

After the GitHub Release step, the workflow also runs `publishPlaystoreReleaseBundle` (from the [Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher) plugin, `com.github.triplet.play` — applied in `app/build.gradle.kts`) to upload the `playstore` flavor's signed release App Bundle straight to the Play Console via the Google Play Developer API. This step only runs if the `ANDROID_PUBLISHER_CREDENTIALS` repo secret is set (the raw JSON contents of a Play Console service account key with "Release to production, exclude devices, and use Play App Signing" — or at least test-track — permission for this app); if it's unset, the workflow logs a notice and skips the step instead of failing, so forks/clones without Play Console access still get a GitHub Release. Publishes go to the `internal` track by default, overridable via the `PLAY_TRACK` repo variable, so a release never reaches production without an explicit promotion in the Play Console. `app/build.gradle.kts`'s `play { }` block is disabled (`enabled.set(false)`) by default and re-enabled only for the `playstore` flavor via `playConfigs` — the `github` flavor shares the same `applicationId` and must never be uploaded. Note: the Play Developer API can only publish updates to an app that already has at least one release uploaded manually through the Play Console; do that first before this automation will work — download the `.aab` asset from the GitHub Release (built regardless of whether `ANDROID_PUBLISHER_CREDENTIALS` is set) and upload it by hand under Play Console → your app → Production/Testing → Create release for that one-time first upload; every release after that is handled automatically by this step.

**Troubleshooting a `403 PERMISSION_DENIED` from `publishPlaystoreReleaseBundle`:** the request that fails is `POST .../applications/<applicationId>/edits` (the very first call Gradle Play Publisher makes, before it touches any track), so the cause is always the service account's standing in the Play Console, not this repo's Gradle config. Check, in order: (1) the service account (the `client_email` inside the `ANDROID_PUBLISHER_CREDENTIALS` JSON) has been invited as a user under Play Console → Users and permissions, with access to *this specific app* (not just "all apps" from a different app list) and the "Release to production, exclude devices, and use Play App Signing" permission (or at least a testing-track release permission); (2) a first release for `applicationId` (`app/build.gradle.kts`) has been uploaded manually through the Play Console — the API can only publish updates, never the initial listing; (3) the Google Play Android Developer API is enabled on the Google Cloud project the service account key belongs to; (4) a newly-granted permission can take a few hours to propagate on Google's side, so a re-run after a short wait can resolve it with no config change at all.

The `github` flavor keeps an in-app self-update flow: the Settings screen's "Update to latest" button (`feature:settings`'s `SettingsViewModel`/`SettingsScreen`) checks `https://api.github.com/repos/neteinstein/FamilyMoments/releases/latest` (`core:data`'s `GitHubUpdateRepositoryImpl`), compares the tag against the installed `versionName` (`core:domain`'s `isNewerVersion`), and downloads/installs the APK asset via `AppUpdateInstallerImpl` (a `FileProvider`-backed install flow gated by the `REQUEST_INSTALL_PACKAGES` permission, the `FileProvider`, and `UpdateApkCleanupReceiver` — all declared only in `app/src/github/AndroidManifest.xml`, merged in for that flavor only). The `playstore` flavor has this feature stripped entirely: `app/build.gradle.kts` sets `BuildConfig.UPDATES_ENABLED = false` for it, wired via Koin as a named `"updatesEnabled"` boolean (`app/.../di/AppModule.kt` → `feature/settings/.../di/SettingsModule.kt`) into `SettingsViewModel`, which skips the update check entirely and drives `SettingsUiState.updatesEnabled = false` so `SettingsScreen` never renders the "Updates" section; the Play Store flavor's manifest carries none of the permission/provider/receiver above since they're only declared in the `github` source set.

`.github/dependabot.yml` runs weekly `gradle` and `github-actions` update checks.

## Architecture

Gradle multi-module project, wired via `settings.gradle.kts`:

```
app                 # entry point: MainActivity, Application class, Koin DI wiring, navigation graph
core/domain         # pure Kotlin: models, repository interfaces, use cases — no Android/Compose deps
core/data           # repository implementations + data sources, depends on core:domain
core/ui             # shared Compose theme (Color/Theme/Typography), exposes Compose libs via `api`
feature/splash      # splash screen (animated logo, auto-navigates after a delay)
feature/home        # main question-card screen + HomeViewModel (the only feature with a ViewModel so far)
feature/settings    # settings screen (language shortcut to system settings, about section)
```

**Dependency rules (enforced by module graph, not lint):**
- `feature/*` depends only on `core:domain` and `core:ui` — never on another `feature/*` module.
- `core:data` depends on `core:domain` and implements its repository interfaces.
- `core:ui` depends only on Compose/Material3 (no domain/data deps).
- `app` depends on every module and is the only place they're wired together (Koin modules, `NavHost`).

### Data flow (question retrieval)

`QuestionSeedData` (object in `core/data`, `core/data/src/main/kotlin/.../data/source/QuestionSeedData.kt`) holds hardcoded question lists per language (`en`, `pt`, `es`, `fr`, `de`) as the single content source, plus a `VERSION` constant. `QuestionRepositoryImpl` persists that content into a local Room database (`FamilyMomentsDatabase`/`CardDao`, `core/data/.../local/`) and implements `QuestionRepository` (the `core:domain` interface); there is no network layer. On first use each process, it compares `QuestionSeedData.VERSION` against what's stored in the single-row `seed_metadata` table (`SeedMetadataDao`) and, on any mismatch, fully deletes and reinserts every card — not just an additive insert — so a question added, edited, *or removed* from `QuestionSeedData` reaches already-installed devices; cards already hidden (see `UsedQuestionsRepositoryImpl`) are re-marked hidden by id after the replace so a version bump doesn't silently un-hide them. **Bump `QuestionSeedData.VERSION` any time you change its content**, and bump `FamilyMomentsDatabase`'s `@Database(version = ...)` (with an added `Migration`, never `fallbackToDestructiveMigration`) any time you change its schema. `GetQuestionsUseCase`/`GetRandomQuestionUseCase` sit on top of the repository interface and are what ViewModels actually call.

Note: `app/src/main/res/xml/locale_config.xml` only declares `en` and `pt` as app locales, even though the data source has content for `es`/`fr`/`de` too — check both places when changing supported languages.

### DI wiring (Koin)

Each module that needs DI defines its own Koin module (`dataModule`, `homeModule`, …); `app/.../di/AppModule.kt` composes them into `appModule`, which `FamilyMomentsApp` (the `Application` subclass) starts via `startKoin`. When adding a feature module with a ViewModel, add its own `*Module.kt` under `feature/<name>/.../di/` and `include` it from `AppModule.kt`.

### Navigation

Single `NavHost` in `app/.../navigation/AppNavigation.kt`, routes defined as a `sealed class Screen` in `Screen.kt` (`Splash`, `Home`, `Settings`). Splash pops itself off the back stack (`popUpTo(inclusive = true)`) once it navigates to Home.

### MVVM conventions

- Each screen that needs state has a `ViewModel` (see `HomeViewModel`) exposing a single `StateFlow<UiState>` (e.g. `HomeUiState`), updated via `MutableStateFlow.update { }`.
- Screens read state with `collectAsStateWithLifecycle()`, not `collectAsState()`.
- ViewModels use `viewModelScope`, never `rememberCoroutineScope()`; no `Context` is passed into ViewModels.
- Use cases are single-purpose, named verb+noun (`GetRandomQuestionUseCase`), and injected into ViewModels through Koin `factory { }`.
- `SettingsScreen` currently has no ViewModel — it's a static/stateless screen that delegates to Android system settings for language changes.

### Theming

`FamilyMomentsTheme` (in `core/ui`) picks between Material3 dynamic color (Android 12+/API 31+, via `dynamicLightColorScheme`/`dynamicDarkColorScheme`) and a static `lightColorScheme`/`darkColorScheme` fallback for older devices, based on `isSystemInDarkTheme()`. Never hardcode colors in Composables — always reference `MaterialTheme.colorScheme.*`. All user-facing strings belong in `res/values/strings.xml`.

## Adding a new feature module

1. Create `feature/<name>/` mirroring `feature/home`'s `build.gradle.kts` (depends on `core:domain` + `core:ui` only).
2. Register it in `settings.gradle.kts` and add it as a dependency in `app/build.gradle.kts`.
3. If it needs a ViewModel, add a Koin module under `feature/<name>/.../di/` and `include` it in `app/.../di/AppModule.kt`.
4. Add a route to `Screen.kt` and a `composable(...)` entry in `AppNavigation.kt`.
5. Add unit tests (ViewModel/use case/repository) before opening a PR — see Testing conventions below.

## Testing conventions

- Unit tests use JUnit 4 + MockK (`mockk()`, `coEvery`, `coVerify`) + `kotlinx-coroutines-test`.
- Coroutine-driven tests use `StandardTestDispatcher`, set via `Dispatchers.setMain()`/`resetMain()` in `@Before`/`@After`, and `runTest { }` with `testDispatcher.scheduler.advanceUntilIdle()` to drive pending coroutines.
- Test method names are backtick-quoted sentences: `` fun `nextQuestion advances to next question`() ``.
- Existing tests to use as templates: `core/domain/.../GetRandomQuestionUseCaseTest.kt` (use case + mocked repository) and `feature/home/.../HomeViewModelTest.kt` (ViewModel + mocked use case + coroutine dispatcher setup).

## Agent orchestration

This repo defines five agent roles — Developer, QA, Architect, Security Manager, Product Manager — as Claude Code subagents (`.claude/agents/`). Architect, Security Manager, and Product Manager delegate to Developer and QA rather than implementing everything themselves. See [`docs/agents/README.md`](docs/agents/README.md) for the full orchestration model, and `docs/agents/<role>.md` for each role's canonical instructions.
