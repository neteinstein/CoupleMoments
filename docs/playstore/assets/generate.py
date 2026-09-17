#!/usr/bin/env python3
"""Generates Play Store visual assets for Couple Moments as SVG, then rasterizes them.

Colors and layout are taken from the real app: core/ui/.../theme/Color.kt (Material3 palette)
and feature/home + feature/settings Compose screens (structure/copy). The mark itself
(icon-512.png) is a static designer-sourced asset, not generated here - this script only embeds
it into the feature graphic's card. Regenerate icon-512.png (and the matching Android
drawable-*/ic_launcher_foreground.png etc.) by hand when the mark changes.
"""
import base64
import subprocess
import os
import shutil

OUT_DIR = os.path.dirname(os.path.abspath(__file__))

# ---- Palette (core/ui Color.kt, light scheme) -----------------------------------------------
PRIMARY = "#E5342F"
ON_PRIMARY = "#FFFFFF"
PRIMARY_CONTAINER = "#FFDAD6"
ON_PRIMARY_CONTAINER = "#410002"
SECONDARY_CONTAINER = "#F6C9C2"
ON_SECONDARY_CONTAINER = "#341210"
BACKGROUND = "#FFF4F1"
ON_BACKGROUND = "#241A17"
SURFACE = "#FFF4F1"
ON_SURFACE = "#241A17"
SURFACE_VARIANT = "#F2DEDA"
ON_SURFACE_VARIANT = "#59413D"
OUTLINE = "#8C7370"
WHITE = "#FFFFFF"

FONT = "Helvetica Neue, Helvetica, Arial, sans-serif"

# Emoji as plain literals (not \U escapes: Py3.9 forbids backslashes in f-string expressions).
EMOJI_ICE = "\U0001F389"
EMOJI_MEMORIES = "\U0001F4F8"
EMOJI_VALUES = "❤️"
EMOJI_FUTURE = "\U0001F52E"
EMOJI_DAILY = "\U0001F33B"
EMOJI_LOCK = "\U0001F512"
EMOJI_GLOBE = "\U0001F30D"
EMOJI_SPEECH = "\U0001F4AC"
EMOJI_BALLOON = "\U0001F4AC"
EMOJI_HEART_SMALL = "❤️"


def run_render(svg_path, png_path, width, height):
    """Rasterizes svg_path to png_path at width x height.

    Prefers macOS's `sips`; falls back to headless Chromium (no macOS available in this
    environment) when `sips` isn't on PATH.
    """
    if shutil.which("sips"):
        subprocess.run(["sips", "-s", "format", "png", svg_path, "--out", png_path], check=True, capture_output=True)
        return
    chrome = (
        os.environ.get("CHROME_BIN")
        or shutil.which("headless_shell")
        or shutil.which("chromium")
        or shutil.which("google-chrome")
        or "/opt/pw-browsers/chromium_headless_shell-1194/chrome-linux/headless_shell"
    )
    # headless_shell (not the full `chromium` binary) is used here: the full binary's headless
    # screenshot leaves a blank strip at the bottom of the image - window-size isn't the actual
    # captured viewport size - while headless_shell renders exactly window-size with no gap.
    subprocess.run(
        [
            chrome, "--disable-gpu", "--no-sandbox", "--hide-scrollbars",
            "--force-device-scale-factor=1", f"--screenshot={png_path}",
            f"--window-size={width},{height}", f"file://{svg_path}",
        ],
        check=True, capture_output=True,
    )


def write_svg(name, content):
    path = os.path.join(OUT_DIR, f"{name}.svg")
    with open(path, "w") as f:
        f.write(content)
    return path


def esc(s):
    return (
        s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "&#39;")
        .replace("’", "'")
    )


def wrap_text(text, max_chars):
    words = text.split()
    lines, cur = [], ""
    for w in words:
        trial = (cur + " " + w).strip()
        if len(trial) > max_chars and cur:
            lines.append(cur)
            cur = w
        else:
            cur = trial
    if cur:
        lines.append(cur)
    return lines


def text_lines(lines, x, start_y, line_height, font_size, fill, weight="500", style="normal", text_anchor="start"):
    """Separate <text> elements per line (sips's SVG renderer mispositions stacked <tspan>s
    inside one <text> - each one collapses to the first line's y), one per wrapped line."""
    out = []
    for i, line in enumerate(lines):
        out.append(
            f'<text x="{x}" y="{start_y + i * line_height}" font-family="{FONT}" font-size="{font_size}" '
            f'font-weight="{weight}" font-style="{style}" fill="{fill}" text-anchor="{text_anchor}">{esc(line)}</text>'
        )
    return "\n".join(out)


def mark_image_data_uri():
    """Base64 data URI for the transparent-background mark (the same static designer-sourced
    artwork as icon-512.png, minus its opaque background), for embedding in the feature graphic's
    white card - icon-512.png itself is opaque (Play Store requires no alpha), which would show as
    a visible pink square inset inside the card."""
    mark_path = os.path.join(OUT_DIR, "..", "..", "..", "app", "src", "main", "res",
                              "drawable-xxxhdpi", "ic_launcher_foreground.png")
    with open(mark_path, "rb") as f:
        b64 = base64.b64encode(f.read()).decode("ascii")
    return f"data:image/png;base64,{b64}"


def mark(cx, cy, size):
    """<image> of icon-512.png, centered at (cx, cy), size x size."""
    return (
        f'<image href="{mark_image_data_uri()}" x="{cx - size / 2}" y="{cy - size / 2}" '
        f'width="{size}" height="{size}" />'
    )


def pill(x, y, w, h, fill, label, text_fill, font_size=20, weight="600", text_anchor="middle"):
    tx = x + w / 2 if text_anchor == "middle" else x + 16
    ty = y + h / 2 + font_size * 0.35
    return (
        f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{h/2}" fill="{fill}" />'
        f'<text x="{tx}" y="{ty}" font-family="{FONT}" font-size="{font_size}" '
        f'font-weight="{weight}" fill="{text_fill}" text-anchor="{text_anchor}">{esc(label)}</text>'
    )


def circle_button(cx, cy, r, glyph):
    return f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="{SURFACE_VARIANT}"/>{glyph}'


def glyph_dice(cx, cy, size=20):
    x0, y0 = cx - size / 2, cy - size / 2
    dots = [(x0 + size * 0.25, y0 + size * 0.25), (x0 + size * 0.75, y0 + size * 0.25),
            (x0 + size * 0.5, y0 + size * 0.5), (x0 + size * 0.25, y0 + size * 0.75),
            (x0 + size * 0.75, y0 + size * 0.75)]
    dotstr = "".join(f'<circle cx="{dx}" cy="{dy}" r="{size*0.09}" fill="{ON_SURFACE_VARIANT}"/>' for dx, dy in dots)
    return (
        f'<rect x="{x0}" y="{y0}" width="{size}" height="{size}" rx="{size*0.22}" '
        f'fill="none" stroke="{ON_SURFACE_VARIANT}" stroke-width="2"/>{dotstr}'
    )


def glyph_gear(cx, cy, r=11):
    teeth = "".join(
        f'<rect x="{cx-1.6}" y="{cy-r-4}" width="3.2" height="6" fill="{ON_SURFACE_VARIANT}" '
        f'transform="rotate({i*45},{cx},{cy})"/>'
        for i in range(8)
    )
    return (
        f'{teeth}<circle cx="{cx}" cy="{cy}" r="{r-2}" fill="none" stroke="{ON_SURFACE_VARIANT}" stroke-width="3"/>'
        f'<circle cx="{cx}" cy="{cy}" r="3" fill="{ON_SURFACE_VARIANT}"/>'
    )


def glyph_close(cx, cy, size=13, color=None):
    color = color or ON_SURFACE_VARIANT
    return (
        f'<line x1="{cx-size/2}" y1="{cy-size/2}" x2="{cx+size/2}" y2="{cy+size/2}" '
        f'stroke="{color}" stroke-width="2.6" stroke-linecap="round"/>'
        f'<line x1="{cx+size/2}" y1="{cy-size/2}" x2="{cx-size/2}" y2="{cy+size/2}" '
        f'stroke="{color}" stroke-width="2.6" stroke-linecap="round"/>'
    )


def glyph_back_arrow(cx, cy, size=16, color=None):
    color = color or ON_SURFACE
    return (
        f'<polyline points="{cx+size/2},{cy-size/2} {cx-size/2},{cy} {cx+size/2},{cy+size/2}" '
        f'fill="none" stroke="{color}" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"/>'
    )


def glyph_grid(cx, cy, size=18):
    x0, y0 = cx - size / 2, cy - size / 2
    cell = size * 0.42
    gap = size * 0.16
    rects = "".join(
        f'<rect x="{x0 + c*(cell+gap)}" y="{y0 + r*(cell+gap)}" width="{cell}" height="{cell}" rx="2" '
        f'fill="{ON_SURFACE_VARIANT}"/>'
        for r in range(2) for c in range(2)
    )
    return rects


def glyph_carousel(cx, cy, size=18):
    return (
        f'<rect x="{cx-size*0.5}" y="{cy-size*0.35}" width="{size*0.28}" height="{size*0.7}" rx="3" '
        f'fill="{ON_SURFACE_VARIANT}" opacity="0.5"/>'
        f'<rect x="{cx-size*0.14}" y="{cy-size*0.45}" width="{size*0.28}" height="{size*0.9}" rx="3" '
        f'fill="{ON_SURFACE_VARIANT}"/>'
        f'<rect x="{cx+size*0.22}" y="{cy-size*0.35}" width="{size*0.28}" height="{size*0.7}" rx="3" '
        f'fill="{ON_SURFACE_VARIANT}" opacity="0.5"/>'
    )


def glyph_globe(cx, cy, r=11):
    return (
        f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="none" stroke="{PRIMARY}" stroke-width="2"/>'
        f'<ellipse cx="{cx}" cy="{cy}" rx="{r*0.45}" ry="{r}" fill="none" stroke="{PRIMARY}" stroke-width="1.6"/>'
        f'<line x1="{cx-r}" y1="{cy}" x2="{cx+r}" y2="{cy}" stroke="{PRIMARY}" stroke-width="1.6"/>'
    )


def status_bar(width):
    right_x = width - 44
    return f'''
<text x="44" y="46" font-family="{FONT}" font-size="26" font-weight="600" fill="{ON_SURFACE}">9:41</text>
<g transform="translate({right_x-92},18)">
  <rect x="0" y="10" width="5" height="8" rx="1" fill="{ON_SURFACE}"/>
  <rect x="8" y="7" width="5" height="11" rx="1" fill="{ON_SURFACE}"/>
  <rect x="16" y="4" width="5" height="14" rx="1" fill="{ON_SURFACE}"/>
  <rect x="24" y="0" width="5" height="18" rx="1" fill="{ON_SURFACE}"/>
  <path d="M42,18 A13,13 0 0 1 68,18" fill="none" stroke="{ON_SURFACE}" stroke-width="2.4"/>
  <path d="M48,18 A8,8 0 0 1 62,18" fill="none" stroke="{ON_SURFACE}" stroke-width="2.4"/>
  <circle cx="55" cy="17" r="1.8" fill="{ON_SURFACE}"/>
  <rect x="76" y="3" width="26" height="14" rx="3" fill="none" stroke="{ON_SURFACE}" stroke-width="2"/>
  <rect x="103" y="7" width="2.5" height="6" rx="1" fill="{ON_SURFACE}"/>
  <rect x="79" y="6" width="20" height="8" rx="1.5" fill="{ON_SURFACE}"/>
</g>'''


def gesture_bar(width, y):
    return f'<rect x="{width/2-70}" y="{y}" width="140" height="6" rx="3" fill="{ON_SURFACE}" opacity="0.3"/>'


def top_bar(width, subtitle=True, settings_glyph="grid"):
    hint = ""
    if subtitle:
        hint = f'''
<text x="{width/2}" y="248" font-family="{FONT}" font-size="21" font-weight="500"
      fill="{ON_SURFACE_VARIANT}" text-anchor="middle">Swipe for a new question &#8592; &#8594;</text>
<text x="{width/2}" y="284" font-family="{FONT}" font-size="18" fill="{ON_SURFACE_VARIANT}"
      text-anchor="middle">Swipe up to focus, down to hide &#8593; &#8595;</text>'''
    return f'''
<text x="64" y="146" font-family="{FONT}" font-size="42" font-weight="800" fill="{PRIMARY}">Couple Moments</text>
<text x="64" y="178" font-family="{FONT}" font-size="22" fill="{ON_SURFACE_VARIANT}">Start a deep conversation</text>
{circle_button(width-142, 128, 34, glyph_dice(width-142, 128))}
{circle_button(width-64, 128, 34, glyph_gear(width-64, 128))}
{hint}'''


def gen_feature_graphic():
    q1 = "What's the best inside joke we have together?"
    q2 = "What's your favorite memory of the two of us together?"
    card1 = f'''
    <g transform="translate(700,55) rotate(7)">
      <rect x="0" y="6" width="220" height="300" rx="26" fill="#000000" fill-opacity="0.08"/>
      <rect x="0" y="0" width="220" height="300" rx="26" fill="{WHITE}" stroke="{SURFACE_VARIANT}" stroke-width="2"/>
      <rect x="0" y="0" width="220" height="300" rx="26" fill="url(#cardTint)"/>
      {pill(16, 20, 122, 32, SECONDARY_CONTAINER, EMOJI_ICE + " Ice Breakers", ON_SECONDARY_CONTAINER, 14, "600", "start")}
      {text_lines(wrap_text(q1, 17), 16, 120, 25, 18, ON_SURFACE)}
    </g>'''
    card2 = f'''
    <g transform="translate(792,175) rotate(-6)">
      <rect x="0" y="6" width="220" height="300" rx="26" fill="#000000" fill-opacity="0.08"/>
      <rect x="0" y="0" width="220" height="300" rx="26" fill="{WHITE}" stroke="{SURFACE_VARIANT}" stroke-width="2"/>
      <rect x="0" y="0" width="220" height="300" rx="26" fill="url(#cardTint)"/>
      {pill(16, 20, 112, 32, SECONDARY_CONTAINER, EMOJI_MEMORIES + " Memories", ON_SECONDARY_CONTAINER, 14, "600", "start")}
      {text_lines(wrap_text(q2, 17), 16, 120, 25, 18, ON_SURFACE)}
    </g>'''

    badges_y = 336
    badges = "".join([
        pill(64, badges_y, 172, 44, WHITE, EMOJI_LOCK + " 100% private", ON_SURFACE_VARIANT, 17, "600", "middle"),
        pill(64 + 182, badges_y, 172, 44, WHITE, EMOJI_GLOBE + " 5 languages", ON_SURFACE_VARIANT, 17, "600", "middle"),
        pill(64 + 182 * 2, badges_y, 182, 44, WHITE, EMOJI_SPEECH + " 600+ prompts", ON_SURFACE_VARIANT, 17, "600", "middle"),
    ])

    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="500" viewBox="0 0 1024 500">
<defs>
  <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="{BACKGROUND}"/>
    <stop offset="1" stop-color="{PRIMARY_CONTAINER}"/>
  </linearGradient>
  <linearGradient id="cardTint" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="{PRIMARY_CONTAINER}" stop-opacity="0.35"/>
    <stop offset="1" stop-color="{SECONDARY_CONTAINER}" stop-opacity="0.15"/>
  </linearGradient>
</defs>
<rect width="1024" height="500" fill="url(#bg)"/>
{card1}
{card2}
<rect x="64" y="70" width="150" height="150" rx="34" fill="{WHITE}"/>
{mark(139, 145, 124)}
<text x="64" y="268" font-family="{FONT}" font-size="62" font-weight="800" fill="{PRIMARY}">Couple Moments</text>
<text x="66" y="304" font-family="{FONT}" font-size="25" font-weight="400" fill="{ON_SURFACE_VARIANT}">Swipe your way to deeper conversations</text>
{badges}
</svg>'''
    p = write_svg("feature-graphic-1024x500", svg)
    run_render(p, os.path.join(OUT_DIR, "feature-graphic-1024x500.png"), 1024, 500)


# 1080x2400 (20:9) matches a modern flagship phone (e.g. Pixel 10) rather than the squarer 16:9
# (1080x1920) used previously, which read as tablet-proportioned next to a real phone screenshot.
W, H = 1080, 2400


def gen_screenshot_home():
    # The real QuestionCard (feature/home HomeScreen.kt) only fillMaxWidth()s - it does NOT
    # fillMaxHeight() - so the white card wraps its content and floats centered (Alignment.Center)
    # inside the weight(1f) Box below the header, rather than stretching to fill it. Stretching the
    # card itself (as an earlier version of this generator did) leaves a mostly-empty white card on
    # a tall canvas; matching the real layout instead keeps the card content-sized and lets the
    # (non-white, gradient) background show above/below it, which reads as intentional whitespace
    # rather than a big blank card.
    # Anchor positions below (region_top, dots_cy, and the bottom-nav row further down) are
    # likewise measured from that same real screenshot's proportions, not just eyeballed.
    region_top = 440  # just below the swipe-hint text
    dots_cy = H - 228
    region_bottom = dots_cy - 40  # just above the page-dots row

    # Sizes below are measured (in px, at this 1080-wide canvas) from an actual Pixel screenshot of
    # this exact screen: ~101px line-to-line spacing, card spanning ~38-71% of screen height.
    question = "What's the funniest thing that's happened to either of us this week?"
    lines = wrap_text(question, 19)
    font_size = 76
    line_height = 101
    pill_h = 84
    gap_pill_question = 64
    gap_question_caption = 88
    pad_top, pad_bottom = 108, 136
    content_extent = pill_h + gap_pill_question + (len(lines) - 1) * line_height + gap_question_caption
    card_h = pad_top + content_extent + pad_bottom
    card_top = (region_top + region_bottom) / 2 - card_h / 2

    pill_y = card_top + pad_top
    question_y = pill_y + pill_h + gap_pill_question
    caption_y = question_y + (len(lines) - 1) * line_height + gap_question_caption

    dots = ""
    total, current = 7, 2
    for i in range(total):
        cx = W / 2 - (total - 1) * 15 + i * 30
        r = 8 if i == current else 6
        fill = PRIMARY if i == current else OUTLINE
        op = 1.0 if i == current else 0.4
        dots += f'<circle cx="{cx}" cy="{dots_cy}" r="{r}" fill="{fill}" opacity="{op}"/>'

    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<defs>
  <radialGradient id="bgGrad" cx="50%" cy="0%" r="75%">
    <stop offset="0" stop-color="{PRIMARY_CONTAINER}" stop-opacity="0.3"/>
    <stop offset="1" stop-color="{BACKGROUND}" stop-opacity="1"/>
  </radialGradient>
  <linearGradient id="cardTint" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="{PRIMARY_CONTAINER}" stop-opacity="0.4"/>
    <stop offset="1" stop-color="{SECONDARY_CONTAINER}" stop-opacity="0.2"/>
  </linearGradient>
</defs>
<rect width="{W}" height="{H}" fill="url(#bgGrad)"/>
{status_bar(W)}
{top_bar(W, subtitle=True)}

<rect x="60" y="{card_top+10}" width="960" height="{card_h}" rx="40" fill="#000000" fill-opacity="0.06"/>
<rect x="60" y="{card_top}" width="960" height="{card_h}" rx="40" fill="{WHITE}" stroke="{SURFACE_VARIANT}" stroke-width="2"/>
<rect x="60" y="{card_top}" width="960" height="{card_h}" rx="40" fill="url(#cardTint)"/>

{pill(370, pill_y, 340, pill_h, SECONDARY_CONTAINER, EMOJI_ICE + " Ice Breakers", ON_SECONDARY_CONTAINER, 34, "600", "middle")}

{text_lines(lines, W/2, question_y, line_height, font_size, ON_SURFACE, text_anchor="middle")}

<text x="{W/2}" y="{caption_y}" font-family="{FONT}" font-size="38" font-style="italic" fill="{ON_SURFACE_VARIANT}"
      text-anchor="middle">Take turns sharing your answers</text>

{dots}

{pill(60, H-178, 150, 56, SURFACE_VARIANT, "All ▾", ON_SURFACE_VARIANT, 24, "600", "middle")}

<circle cx="1000" cy="{H-150}" r="44" fill="{SURFACE_VARIANT}"/>
{glyph_grid(1000, H-150)}

{gesture_bar(W, H-31)}
</svg>'''
    p = write_svg("screenshot-1-home", svg)
    run_render(p, os.path.join(OUT_DIR, "screenshot-1-home.png"), W, H)


def gen_screenshot_fullscreen():
    # Content block (pill/emoji/question) is vertically centered between the top icon row and the
    # gesture bar; shift it down from its H=1920 position to keep that same centered ratio on the
    # taller canvas, rather than leaving it stranded near the top with a big empty bottom half.
    content_shift = int(round(0.5372 * (H - 1920)))
    question = "What's your favorite memory of the two of us together?"
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<rect width="{W}" height="{H}" fill="{SURFACE}"/>
{status_bar(W)}

<circle cx="128" cy="146" r="34" fill="{SURFACE_VARIANT}"/>
{glyph_close(128, 146)}
<circle cx="{W-128}" cy="146" r="34" fill="{SURFACE_VARIANT}"/>
{glyph_dice(W-128, 146)}

{pill(W/2-130, 760+content_shift, 260, 58, SECONDARY_CONTAINER, EMOJI_MEMORIES + " Memories", ON_SECONDARY_CONTAINER, 25, "600", "middle")}

<text x="{W/2}" y="{1010+content_shift}" font-family="{FONT}" font-size="120" text-anchor="middle">&#128172;</text>

{text_lines(wrap_text(question, 17), W/2, 1180+content_shift, 74, 54, ON_SURFACE, text_anchor="middle")}

{gesture_bar(W, H-44)}
</svg>'''
    p = write_svg("screenshot-2-fullscreen", svg)
    run_render(p, os.path.join(OUT_DIR, "screenshot-2-fullscreen.png"), W, H)


GRID_ITEMS = [
    (EMOJI_ICE, "What's the best inside joke we have together?"),
    (EMOJI_MEMORIES, "What's the best trip we've taken together?"),
    (EMOJI_VALUES, "What's one quality of mine you admire most?"),
    (EMOJI_FUTURE, "What's one adventure you want us to have in the next five years?"),
    (EMOJI_DAILY, "What was the best part of your day today?"),
    (EMOJI_ICE, "What's the silliest nickname you've ever wanted to give me?"),
    (EMOJI_MEMORIES, "What do you remember about the moment you first realized you liked me?"),
    (EMOJI_VALUES, "How can I best support you when you're having a hard day?"),
    (EMOJI_FUTURE, "If we designed our dream home, what's one unusual feature you'd want?"),
    (EMOJI_DAILY, "What's one small thing I did recently that made you smile?"),
    (EMOJI_ICE, "If we could teleport anywhere right now, where would we go?"),
    (EMOJI_MEMORIES, "What's a memory that always makes you laugh when you think about it?"),
]


def gen_screenshot_grid():
    margin = 60
    gap = 22
    cols = 3
    colw = (W - 2 * margin - (cols - 1) * gap) / cols
    # The real GridQuestionCard (feature/home HomeScreen.kt) uses a fixed aspectRatio(0.75f) -
    # cards don't stretch taller on a bigger screen, more of them just become visible at once - so
    # keep that ratio here too and show an extra row (GRID_ITEMS below) to fill the taller canvas.
    cardh = colw / 0.75
    rows = -(-len(GRID_ITEMS) // cols)  # ceil division
    cards = ""
    for i, (emoji, text) in enumerate(GRID_ITEMS):
        r, c = divmod(i, cols)
        x = margin + c * (colw + gap)
        y = 300 + r * (cardh + gap)
        lines = wrap_text(text, 14)
        start_y = y + cardh / 2 - (len(lines) - 1) * 12.5
        cards += f'''
<rect x="{x}" y="{y}" width="{colw}" height="{cardh}" rx="20" fill="{WHITE}" stroke="{SURFACE_VARIANT}" stroke-width="1.5"/>
<rect x="{x}" y="{y}" width="{colw}" height="{cardh}" rx="20" fill="url(#cardTint)"/>
<text x="{x+colw-16}" y="{y+34}" font-family="{FONT}" font-size="22" text-anchor="end">{emoji}</text>
{text_lines(lines, x+colw/2, start_y, 25, 19, ON_SURFACE, text_anchor="middle")}'''

    clip_bottom = 300 + rows * cardh + (rows - 1) * gap + 20
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<defs>
  <linearGradient id="cardTint" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="{PRIMARY_CONTAINER}" stop-opacity="0.4"/>
    <stop offset="1" stop-color="{SECONDARY_CONTAINER}" stop-opacity="0.2"/>
  </linearGradient>
  <clipPath id="gridClip"><rect x="0" y="290" width="{W}" height="{clip_bottom-290}"/></clipPath>
</defs>
<rect width="{W}" height="{H}" fill="{BACKGROUND}"/>
{status_bar(W)}
{top_bar(W, subtitle=False)}
<g clip-path="url(#gridClip)">
{cards}
</g>

{pill(60, H-220, 150, 56, SURFACE_VARIANT, "All ▾", ON_SURFACE_VARIANT, 24, "600", "middle")}

<circle cx="1000" cy="{H-120}" r="44" fill="{SURFACE_VARIANT}"/>
{glyph_carousel(1000, H-120)}

{gesture_bar(W, H-44)}
</svg>'''
    p = write_svg("screenshot-3-grid", svg)
    run_render(p, os.path.join(OUT_DIR, "screenshot-3-grid.png"), W, H)


def gen_screenshot_settings():
    def section_label(y, text):
        return (
            f'<text x="60" y="{y}" font-family="{FONT}" font-size="20" font-weight="700" '
            f'fill="{ON_SURFACE_VARIANT}" letter-spacing="1">{esc(text.upper())}</text>'
        )

    def card(y, h):
        return f'<rect x="60" y="{y}" width="960" height="{h}" rx="24" fill="{SURFACE}" stroke="{SURFACE_VARIANT}" stroke-width="2"/>'

    reset_desc = wrap_text(
        "Hidden cards (swiped down as already used or not wanted) stay out of rotation. "
        "Reset to bring them all back.", 46)
    about_desc = "Strengthening couples, one question at a time. " + EMOJI_HEART_SMALL

    # Sections are a top-anchored scrollable list in the real screen, so at H=1920 the leftover
    # space below the footer was one large blank strip. Spread the extra height from the taller
    # canvas across the gaps *between* sections instead, so the page reads as a well-composed
    # screenshot rather than mostly-empty content pinned to the top third of the screen.
    label1_y = 344
    card1_y = 411
    label2_y = 689
    card2_y = 756
    label3_y = 1194
    card3_y = 1261
    footer1_y = 1676
    footer2_y = 1708

    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<rect width="{W}" height="{H}" fill="{BACKGROUND}"/>
{status_bar(W)}

<circle cx="90" cy="146" r="0" fill="none"/>
{glyph_back_arrow(70, 146)}
<text x="112" y="158" font-family="{FONT}" font-size="38" font-weight="700" fill="{ON_SURFACE}">Settings</text>
<line x1="0" y1="196" x2="{W}" y2="196" stroke="{SURFACE_VARIANT}" stroke-width="2"/>

{section_label(label1_y, "App Preferences")}
{card(card1_y, 130)}
{glyph_globe(120, card1_y+65)}
<text x="168" y="{card1_y+57}" font-family="{FONT}" font-size="26" font-weight="600" fill="{ON_SURFACE}">App Language</text>
<text x="168" y="{card1_y+92}" font-family="{FONT}" font-size="21" fill="{ON_SURFACE_VARIANT}">Change language in Android settings</text>

{section_label(label2_y, "Reset Cards")}
{card(card2_y, 300)}
{text_lines(reset_desc, 108, card2_y+58, 30, 22, ON_SURFACE)}
{pill(108, card2_y+198, 260, 66, PRIMARY, "Reset Cards", ON_PRIMARY, 24, "700", "middle")}

{section_label(label3_y, "About")}
{card(card3_y, 220)}
<text x="108" y="{card3_y+56}" font-family="{FONT}" font-size="27" font-weight="700" fill="{ON_SURFACE}">Couple Moments: LoopGain</text>
<text x="108" y="{card3_y+94}" font-family="{FONT}" font-size="21" fill="{ON_SURFACE_VARIANT}">Version 1.0.0</text>
{text_lines(wrap_text(about_desc, 40), 108, card3_y+138, 30, 22, ON_SURFACE)}

<text x="{W/2}" y="{footer1_y}" font-family="{FONT}" font-size="21" fill="{ON_SURFACE_VARIANT}" text-anchor="middle">Couple Moments is part of the LoopGain family tools</text>
<text x="{W/2}" y="{footer2_y}" font-family="{FONT}" font-size="21" fill="{ON_SURFACE_VARIANT}" text-anchor="middle">by Pedro Vicente</text>

{gesture_bar(W, H-44)}
</svg>'''
    p = write_svg("screenshot-4-settings", svg)
    run_render(p, os.path.join(OUT_DIR, "screenshot-4-settings.png"), W, H)


gen_feature_graphic()
gen_screenshot_home()
gen_screenshot_fullscreen()
gen_screenshot_grid()
gen_screenshot_settings()

# Flatten to opaque RGB (Play Store screenshots/feature graphic reject an alpha channel) and
# recompress, then drop the intermediate .svg sources - only the PNGs are committed.
from PIL import Image  # noqa: E402

for _name in [
    "feature-graphic-1024x500", "screenshot-1-home",
    "screenshot-2-fullscreen", "screenshot-3-grid", "screenshot-4-settings",
]:
    _png = os.path.join(OUT_DIR, f"{_name}.png")
    Image.open(_png).convert("RGB").save(_png, optimize=True)
    os.remove(os.path.join(OUT_DIR, f"{_name}.svg"))

print("all assets generated")
