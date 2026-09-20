# Couple Moments

Spark deeper conversations with the person you love.

Couple Moments is a Kotlin Multiplatform app — a couple-focused fork of
[FamilyMoments](https://github.com/neteinstein/FamilyMoments) — that hands you one thoughtful
question at a time — swipe through a deck of conversation starters across six categories (Ice
Breakers, Memories, Values, Future Dreams, Daily Life, Intimacy), pick a card at random, or browse
the whole deck as a grid. Everything runs entirely on-device: there's no account, no backend, and
no network access beyond an optional version check against this repo's own GitHub Releases.

It runs on **Android**, **iOS** (simulator) and the **web** — one shared Compose Multiplatform
codebase, three thin platform shells. Try it in a browser at
[couples.loopgain.org](https://couples.loopgain.org).

## Features

- **Swipeable question deck** — swipe left/right for the next/previous question, swipe up to
  focus a card full screen, swipe down to hide a card you've already used or don't want (it stays
  out of rotation until you reset it).
- **Categories** — filter the deck by Ice Breakers, Memories, Values, Future Dreams, Daily Life,
  or Intimacy (romantic and sexual conversation starters for the two of you), or leave it on
  "All".
- **Shuffle** — jump straight to a random question from the current filter.
- **Grid view** — toggle between the one-at-a-time swipe deck and a scrollable grid of every
  question in the current filter, tapping any card to open it full screen.
- **Multi-language content** — question text ships in English, Portuguese, Spanish, French, and
  German (`en`/`pt` are currently exposed as selectable app languages via Android's per-app
  language picker; see `androidApp/src/main/res/xml/locale_config.xml`). iOS and the web, which
  have no such OS setting, get an in-app language picker in Settings that defaults to the
  device/browser language.
- **Keyboard support on the web** — the arrow keys drive the deck (left/right for previous/next,
  up to focus a card, down to hide it), since there's no swipe gesture in a desktop browser.
- **Reset cards** — Settings has a one-tap reset that brings every hidden card back into rotation.
- **Self-updating** — Settings' "Update to latest" button checks this repo's GitHub Releases and
  installs a newer APK directly, no Play Store required.

## Requirements

- **Android:** 12+ (API 32) device or emulator.
- **iOS:** Xcode with an iOS simulator. No Apple Developer signing is configured, so the iOS build
  is simulator-only for now.
- **Web:** any current browser with WebAssembly GC support (Chrome/Edge 119+, Firefox 120+,
  Safari 18.2+).
- No accounts or API keys to build and run locally — a clean checkout compiles and runs as-is.
  Cutting a signed release build requires the signing/publishing secrets described under
  [Releases](#releases) below.

## Setup

```bash
git clone https://github.com/neteinstein/CoupleMoments.git
cd CoupleMoments
# Android
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug   # with a device/emulator connected

# Web — serves on http://localhost:8080
./gradlew :webApp:wasmJsBrowserRun

# iOS — open iosApp/iosApp.xcodeproj in Xcode and run the `iosApp` scheme on a simulator
```

Or open the project directly in Android Studio and run it from there.

## Usage

1. Open the app — you land on the swipe deck with a random question already showing.
2. Swipe **left**/**right** for the next/previous question, **up** to focus it full screen, or
   **down** to hide it (confirmed with a dialog) if you've already used it or don't want it again.
3. Use the category dropdown (bottom left) to filter the deck, or the shuffle button (top right)
   to jump to a random card from the current filter.
4. Tap the grid icon (bottom right) to switch to a scrollable grid of every question in the
   current filter — tap any card to open it full screen.
5. Open Settings (top right) to change the app's language, reset hidden cards, or check for an
   app update.

## Privacy

Couple Moments collects no data and has no backend — everything stays on your device. See
[`PRIVACY.md`](PRIVACY.md) for the full policy.

## Architecture

Kotlin Multiplatform, Compose Multiplatform + Material3, MVVM, Koin DI, one Gradle module per
layer/feature. Every `core:*`/`feature:*`/`app` module targets Android + iOS + wasmJs; the three
platform modules are thin shells around `app`'s shared `App()` composable:

```
build-logic         # convention plugins shared by every module
app                 # shared KMP aggregator: App(), Koin wiring, navigation graph
androidApp          # Android shell: MainActivity, Application, manifest, res/, flavors, R8
iosApp              # Xcode project wrapper (Swift only)
webApp              # wasmJs entry point: main(), index.html
core/domain         # pure Kotlin: models, repository interfaces, use cases
core/data           # repository implementations + local (Room) data source
core/ui             # shared Compose theme, exposes Compose libs via `api`
feature/splash      # splash screen
feature/home        # main question-card / grid screen + HomeViewModel
feature/settings    # settings screen (language, reset cards, update)
```

See [`AGENTS.md`](AGENTS.md) for the full module dependency rules, data flow, DI/navigation
conventions, and testing standards.

## Testing & CI

```bash
./gradlew testAndroidHostTest                 # all unit tests across the KMP modules
./gradlew :feature:home:testAndroidHostTest   # a single module
./gradlew ktlintCheck                         # style/formatting gate (ktlintFormat to auto-fix)
./gradlew koverXmlReport                      # tests + aggregated coverage report
```

Every PR into `main`/`develop` runs seven independent GitHub Actions jobs
(`.github/workflows/pr.yml`): `ktlint`, Android debug compile, a minified release build plus
`scripts/verify-obfuscation.sh`, unit tests, Kover coverage uploaded to Codecov, an iOS simulator
build, and the web (wasmJs) build.

Pushes to `main` also deploy the web build to GitHub Pages
(`.github/workflows/deploy-pages.yml`).
## Releases

Every push to `main` triggers `.github/workflows/release.yml`: it re-validates the commit
(`ktlintCheck`, unit tests), builds a signed release APK, and publishes it as a GitHub
Release tagged `v<major>.<minor>.<run number>`, where `major.minor` comes from
`couples.versionName` in `gradle.properties` (the one place the version is declared — local builds
and Settings' "About" section use it verbatim) — which is also what powers the in-app
"Update to latest" check in Settings, since it reads this repo's own Releases. The workflow needs these repository secrets
configured under **Settings → Secrets and variables → Actions** before it can run (not included
in this checkout):

- `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` — signing config for the
  release APK/AAB.
- `ANDROID_PUBLISHER_CREDENTIALS` — only required if you also want the `playstore` flavor
  published to the Play Console.

See [`AGENTS.md`](AGENTS.md#releases) for how each secret is used.

## Contributing

Issues and pull requests are welcome. See [`AGENTS.md`](AGENTS.md) for coding standards, module
boundaries, and testing conventions, and the PR template for what to include.

### Agent orchestration

This repo defines five specialized agent roles — Developer, QA, Architect, Security Manager, and
Product Manager — usable as Claude Code subagents and GitHub Copilot custom agents alike. See
[`docs/agents/README.md`](docs/agents/README.md) for the full orchestration model.
