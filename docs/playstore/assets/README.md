# Play Store visual assets

Graphics for the Play Console's Store presence page (Main store listing → Graphics).

| File | Use | Spec |
| --- | --- | --- |
| `icon-512.png` | Hi-res icon | 512×512, 32-bit PNG |
| `feature-graphic-1024x500.png` | Feature graphic | 1024×500, PNG/JPEG, no alpha |
| `screenshot-1-home.png` | Phone screenshot — swipe deck | 1080×2400 (20:9, e.g. Pixel 10) |
| `screenshot-2-fullscreen.png` | Phone screenshot — full-screen question | 1080×2400 |
| `screenshot-3-grid.png` | Phone screenshot — grid view | 1080×2400 |
| `screenshot-4-settings.png` | Phone screenshot — Settings | 1080×2400 |

All are en-US only; Play falls back to this set for locales without their own screenshots. Add a
`screenshots/<locale>/` subfolder if per-locale screenshots are ever wanted for the other listed
languages (`docs/playstore/play/listings/`).

`icon-512.png` is a static, designer-sourced asset (the app's mark, exported at 512×512) — it is
not generated. The same artwork (minus its opaque background) also backs the Android launcher icon
at `app/src/main/res/drawable-*/ic_launcher_foreground.png` (plus the monochrome/themed variant and
`ic_splash_logo.png`) and the shared in-app mark at
`core/ui/src/commonMain/composeResources/drawable-*/ic_couple_moments_mark.png` (moved from
`androidMain/res` to Compose Multiplatform's composeResources on the `kmp` branch, since
`feature:splash` now references it from commonMain); update all of those together when the
mark changes.

`feature-graphic-1024x500.png` is generated — `generate.py` renders it as SVG (matching the real
app's colors from `core/ui/.../theme/Color.kt`, and embedding
`app/src/main/res/drawable-xxxhdpi/ic_launcher_foreground.png` for the mark) and rasterizes it with
macOS's `sips`, falling back to a headless Chromium `headless_shell` binary when `sips` isn't on
PATH (e.g. on Linux). The four `screenshot-*.png` files are genuine device captures taken from an
Android emulator (`sdk_gphone16k_arm64`, playstore debug build) — not generated — so they show the
real running app.

## Regenerating

Regenerate `feature-graphic-1024x500.png` after a color change (or after the mark itself changes -
see above):

```bash
python3 docs/playstore/assets/generate.py
```

Requires Python 3 with Pillow (`pip install pillow`) and either macOS's `sips` or a Chromium/Chrome
build providing `headless_shell` (used to rasterize the intermediate SVGs; the .svg files are
deleted after each run) - set `CHROME_BIN` to point at a specific binary if none is auto-detected.

Recapture the `screenshot-*.png` files after a UI or copy change, from a running emulator:

```bash
./gradlew :app:assemblePlaystoreDebug
adb install -r app/build/outputs/apk/playstore/debug/app-playstore-debug.apk
```

Then launch the app, disable animations (`adb shell settings put global window_animation_scale 0`
etc.) for crisp captures, navigate to each of the four views (home swipe deck; swipe up on a card
for the full-screen question; the grid icon, filtered to a single wholesome category such as
Memories, to avoid surfacing Intimacy-category questions in a store screenshot; the settings gear),
capture with `adb shell screencap -p /sdcard/shot.png` + `adb pull`, and downscale to 1080×2400
with `sips -z 2400 1080 <capture>.png --out screenshot-N-name.png` (the emulator's native
resolution is already a 20:9 aspect ratio, so this is a straight resize with no cropping).
