# Play Store visual assets

Graphics for the Play Console's Store presence page (Main store listing → Graphics). These are
generated, not hand-drawn — `generate.py` renders each one as SVG (matching the real app's
colors from `core/ui/.../theme/Color.kt`, the launcher mark from
`app/src/main/res/drawable/ic_launcher_foreground.xml`, and the `feature/home` /
`feature/settings` screen layouts/copy) and rasterizes it with macOS's `sips`.

| File | Use | Spec |
| --- | --- | --- |
| `icon-512.png` | Hi-res icon | 512×512, 32-bit PNG |
| `feature-graphic-1024x500.png` | Feature graphic | 1024×500, PNG/JPEG, no alpha |
| `screenshot-1-home.png` | Phone screenshot — swipe deck | 1080×1920 (9:16) |
| `screenshot-2-fullscreen.png` | Phone screenshot — full-screen question | 1080×1920 |
| `screenshot-3-grid.png` | Phone screenshot — grid view | 1080×1920 |
| `screenshot-4-settings.png` | Phone screenshot — Settings | 1080×1920 |

All are en-US only; Play falls back to this set for locales without their own screenshots. Add a
`screenshots/<locale>/` subfolder if per-locale screenshots are ever wanted for the other listed
languages (`docs/playstore/play/listings/`).

## Regenerating

The screenshots are faithful mockups (real strings, colors, and layout) rather than device
captures — there's no Android emulator in this environment. Regenerate after a UI or copy change:

```bash
python3 docs/playstore/assets/generate.py
```

Requires Python 3 with Pillow (`pip install pillow`) and macOS's `sips` (used to rasterize the
intermediate SVGs; the .svg files are deleted after each run). For a pixel-exact capture, swap
these for real device/emulator screenshots before submission if desired - the generated ones are
accurate enough to submit as-is but aren't a substitute for the genuine article.
