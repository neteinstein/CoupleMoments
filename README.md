# Couple Moments

Spark deeper conversations with the person you love.

Couple Moments is an Android app — a couple-focused fork of
[FamilyMoments](https://github.com/neteinstein/FamilyMoments) — that hands you one thoughtful
question at a time — swipe through a deck of conversation starters across six categories (Ice
Breakers, Memories, Values, Future Dreams, Daily Life, Intimacy), pick a card at random, or browse
the whole deck as a grid. Everything runs entirely on-device: there's no account, no backend, and
no network access beyond an optional version check against this repo's own GitHub Releases.

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
  language picker; see `app/src/main/res/xml/locale_config.xml`).
- **Reset cards** — Settings has a one-tap reset that brings every hidden card back into rotation.
- **Self-updating** — Settings' "Update to latest" button checks this repo's GitHub Releases and
  installs a newer APK directly, no Play Store required.

## Requirements

- Android 12+ (API 32) device or emulator.
- No accounts or API keys to build and run locally — a clean checkout compiles and runs as-is.
  Cutting a signed release build requires the signing/publishing secrets described under
  [Releases](#releases) below.

## Setup

```bash
git clone https://github.com/neteinstein/CoupleMoments.git
cd CoupleMoments
./gradlew assembleDebug
./gradlew installDebug   # with a device/emulator connected
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

Kotlin, Jetpack Compose + Material3, MVVM, Koin DI, one Gradle module per layer/feature:

```
app                 # entry point: MainActivity, Application class, Koin DI wiring, navigation graph
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
./gradlew testDebugUnitTest                   # all unit tests
./gradlew :feature:home:testDebugUnitTest     # a single module
./gradlew ktlintCheck                         # style/formatting gate (ktlintFormat to auto-fix)
./gradlew createDebugUnitTestCoverageReport   # tests + coverage report
```

Every PR into `main`/`develop` runs four independent GitHub Actions jobs
(`.github/workflows/pr.yml`): `ktlint`, `assembleDebug`, `testDebugUnitTest`, and a
coverage report uploaded to Codecov.

## Releases

Every push to `main` triggers `.github/workflows/release.yml`: it re-validates the commit
(`ktlintCheck`, `testDebugUnitTest`), builds a signed release APK, and publishes it as a GitHub
Release tagged `v1.0.<run number>` — which is also what powers the in-app "Update to latest" check
in Settings, since it reads this repo's own Releases. The workflow needs these repository secrets
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
