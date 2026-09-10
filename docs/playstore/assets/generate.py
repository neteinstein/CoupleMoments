#!/usr/bin/env python3
"""Generates Play Store visual assets for Couple Moments as SVG, rasterized via macOS `sips`.

Colors and layout are taken from the real app: core/ui/.../theme/Color.kt (Material3 palette),
app/src/main/res/drawable/ic_launcher_foreground.xml (mark), and feature/home + feature/settings
Compose screens (structure/copy).
"""
import subprocess
import os

OUT_DIR = os.path.dirname(os.path.abspath(__file__))

# ---- Palette (core/ui Color.kt, light scheme) -----------------------------------------------
PRIMARY = "#BF6900"
ON_PRIMARY = "#FFFFFF"
PRIMARY_CONTAINER = "#FFDDB5"
ON_PRIMARY_CONTAINER = "#3D1E00"
SECONDARY_CONTAINER = "#F9DEBA"
ON_SECONDARY_CONTAINER = "#271904"
BACKGROUND = "#FFF8F4"
ON_BACKGROUND = "#201A14"
SURFACE = "#FFF8F4"
ON_SURFACE = "#201A14"
SURFACE_VARIANT = "#F0E0CF"
ON_SURFACE_VARIANT = "#4F4539"
OUTLINE = "#817567"
SPLASH_BG = "#FFE8C9"
HEART = "#FF6F91"
FIG_LEFT = "#BF6900"
FIG_RIGHT = "#D8471F"
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


def run_sips(svg_path, png_path):
    subprocess.run(["sips", "-s", "format", "png", svg_path, "--out", png_path], check=True, capture_output=True)


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


def mark(cx, cy, scale=1.0):
    """Two leaning figures + clasped hands + heart, matching ic_launcher_foreground.xml,
    centered at (cx, cy) at the given scale (1.0 == the original 108x108 viewport)."""
    s = scale
    return (
        f'<g transform="translate({cx - 54*s},{cy - 54*s}) scale({s})">'
        f'<path fill="{PRIMARY_CONTAINER}" fill-opacity="0.45" '
        f'd="M54,58m-28,0a28,28 0,1 1,56 0a28,28 0,1 1,-56 0" />'
        f'<g transform="rotate(-13, 54, 82)">'
        f'<path fill="{FIG_LEFT}" d="M32,82 L32,68 A11,11 0 0 1 54,62 A11,11 0 0 1 54,82 Z" />'
        f'<path fill="{FIG_LEFT}" d="M40,38m-8,0a8,8 0,1 1,16 0a8,8 0,1 1,-16 0" />'
        f'</g>'
        f'<g transform="rotate(13, 54, 82)">'
        f'<path fill="{FIG_RIGHT}" d="M76,82 L76,68 A11,11 0 0 0 54,62 A11,11 0 0 0 54,82 Z" />'
        f'<path fill="{FIG_RIGHT}" d="M68,38m-8,0a8,8 0,1 1,16 0a8,8 0,1 1,-16 0" />'
        f'</g>'
        f'<g transform="rotate(-18, 50.5, 78)">'
        f'<path fill="{FIG_LEFT}" d="M45,78 A5.5,3.8 0 1 1 56,78 A5.5,3.8 0 1 1 45,78 Z" />'
        f'</g>'
        f'<g transform="rotate(18, 57.5, 78)">'
        f'<path fill="{FIG_RIGHT}" d="M52,78 A5.5,3.8 0 1 1 63,78 A5.5,3.8 0 1 1 52,78 Z" />'
        f'</g>'
        f'<path fill="{HEART}" fill-opacity="0.95" '
        f'd="M54,42 C52,39 47,39 47,43.4 C47,47.8 51,51 54,54.2 C57,51 61,47.8 61,43.4 C61,39 56,39 54,42 Z" />'
        f'</g>'
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


def gen_icon():
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 108 108">
<rect width="108" height="108" fill="{SPLASH_BG}"/>
{mark(54, 54, 1.0)}
</svg>'''
    p = write_svg("icon-512", svg)
    run_sips(p, os.path.join(OUT_DIR, "icon-512.png"))


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
{mark(139, 145, 1.05)}
<text x="64" y="268" font-family="{FONT}" font-size="62" font-weight="800" fill="{PRIMARY}">Couple Moments</text>
<text x="66" y="304" font-family="{FONT}" font-size="25" font-weight="400" fill="{ON_SURFACE_VARIANT}">Swipe your way to deeper conversations</text>
{badges}
</svg>'''
    p = write_svg("feature-graphic-1024x500", svg)
    run_sips(p, os.path.join(OUT_DIR, "feature-graphic-1024x500.png"))


W, H = 1080, 1920


def gen_screenshot_home():
    dots = ""
    total, current = 7, 2
    for i in range(total):
        cx = W / 2 - (total - 1) * 15 + i * 30
        r = 8 if i == current else 6
        fill = PRIMARY if i == current else OUTLINE
        op = 1.0 if i == current else 0.4
        dots += f'<circle cx="{cx}" cy="1624" r="{r}" fill="{fill}" opacity="{op}"/>'

    question = "What's the funniest thing that's happened to either of us this week?"
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

<rect x="60" y="336" width="960" height="1240" rx="40" fill="#000000" fill-opacity="0.06"/>
<rect x="60" y="326" width="960" height="1240" rx="40" fill="{WHITE}" stroke="{SURFACE_VARIANT}" stroke-width="2"/>
<rect x="60" y="326" width="960" height="1240" rx="40" fill="url(#cardTint)"/>

{pill(430, 400, 220, 56, SECONDARY_CONTAINER, EMOJI_ICE + " Ice Breakers", ON_SECONDARY_CONTAINER, 24, "600", "middle")}

{text_lines(wrap_text(question, 21), W/2, 540, 62, 46, ON_SURFACE, text_anchor="middle")}

<text x="{W/2}" y="1480" font-family="{FONT}" font-size="26" font-style="italic" fill="{ON_SURFACE_VARIANT}"
      text-anchor="middle">Take turns sharing your answers</text>

{dots}

{pill(60, 1690, 150, 56, SURFACE_VARIANT, "All ▾", ON_SURFACE_VARIANT, 24, "600", "middle")}

<circle cx="1000" cy="1800" r="44" fill="{SURFACE_VARIANT}"/>
{glyph_grid(1000, 1800)}

{gesture_bar(W, 1876)}
</svg>'''
    p = write_svg("screenshot-1-home", svg)
    run_sips(p, os.path.join(OUT_DIR, "screenshot-1-home.png"))


def gen_screenshot_fullscreen():
    question = "What's your favorite memory of the two of us together?"
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<rect width="{W}" height="{H}" fill="{SURFACE}"/>
{status_bar(W)}

<circle cx="128" cy="146" r="34" fill="{SURFACE_VARIANT}"/>
{glyph_close(128, 146)}
<circle cx="{W-128}" cy="146" r="34" fill="{SURFACE_VARIANT}"/>
{glyph_dice(W-128, 146)}

{pill(W/2-130, 760, 260, 58, SECONDARY_CONTAINER, EMOJI_MEMORIES + " Memories", ON_SECONDARY_CONTAINER, 25, "600", "middle")}

<text x="{W/2}" y="1010" font-family="{FONT}" font-size="120" text-anchor="middle">&#128172;</text>

{text_lines(wrap_text(question, 17), W/2, 1180, 74, 54, ON_SURFACE, text_anchor="middle")}

{gesture_bar(W, 1876)}
</svg>'''
    p = write_svg("screenshot-2-fullscreen", svg)
    run_sips(p, os.path.join(OUT_DIR, "screenshot-2-fullscreen.png"))


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
]


def gen_screenshot_grid():
    margin = 60
    gap = 22
    cols = 3
    colw = (W - 2 * margin - (cols - 1) * gap) / cols
    cardh = colw / 0.75
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

    clip_bottom = 300 + 3 * cardh + 2 * gap + 20
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

{pill(60, 1700, 150, 56, SURFACE_VARIANT, "All ▾", ON_SURFACE_VARIANT, 24, "600", "middle")}

<circle cx="1000" cy="1800" r="44" fill="{SURFACE_VARIANT}"/>
{glyph_carousel(1000, 1800)}

{gesture_bar(W, 1876)}
</svg>'''
    p = write_svg("screenshot-3-grid", svg)
    run_sips(p, os.path.join(OUT_DIR, "screenshot-3-grid.png"))


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

    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<rect width="{W}" height="{H}" fill="{BACKGROUND}"/>
{status_bar(W)}

<circle cx="90" cy="146" r="0" fill="none"/>
{glyph_back_arrow(70, 146)}
<text x="112" y="158" font-family="{FONT}" font-size="38" font-weight="700" fill="{ON_SURFACE}">Settings</text>
<line x1="0" y1="196" x2="{W}" y2="196" stroke="{SURFACE_VARIANT}" stroke-width="2"/>

{section_label(258, "App Preferences")}
{card(286, 130)}
{glyph_globe(120, 351)}
<text x="168" y="343" font-family="{FONT}" font-size="26" font-weight="600" fill="{ON_SURFACE}">App Language</text>
<text x="168" y="378" font-family="{FONT}" font-size="21" fill="{ON_SURFACE_VARIANT}">Change language in Android settings</text>

{section_label(478, "Reset Cards")}
{card(506, 300)}
{text_lines(reset_desc, 108, 564, 30, 22, ON_SURFACE)}
{pill(108, 704, 260, 66, PRIMARY, "Reset Cards", ON_PRIMARY, 24, "700", "middle")}

{section_label(864, "About")}
{card(892, 220)}
<text x="108" y="948" font-family="{FONT}" font-size="27" font-weight="700" fill="{ON_SURFACE}">Couple Moments: LoopGain</text>
<text x="108" y="986" font-family="{FONT}" font-size="21" fill="{ON_SURFACE_VARIANT}">Version 1.0.0</text>
{text_lines(wrap_text(about_desc, 40), 108, 1030, 30, 22, ON_SURFACE)}

<text x="{W/2}" y="1194" font-family="{FONT}" font-size="21" fill="{ON_SURFACE_VARIANT}" text-anchor="middle">Couple Moments is part of the LoopGain family tools</text>
<text x="{W/2}" y="1226" font-family="{FONT}" font-size="21" fill="{ON_SURFACE_VARIANT}" text-anchor="middle">by Pedro Vicente</text>

{gesture_bar(W, 1876)}
</svg>'''
    p = write_svg("screenshot-4-settings", svg)
    run_sips(p, os.path.join(OUT_DIR, "screenshot-4-settings.png"))


gen_icon()
gen_feature_graphic()
gen_screenshot_home()
gen_screenshot_fullscreen()
gen_screenshot_grid()
gen_screenshot_settings()

# Flatten to opaque RGB (Play Store screenshots/feature graphic reject an alpha channel) and
# recompress, then drop the intermediate .svg sources - only the PNGs are committed.
from PIL import Image  # noqa: E402

for _name in [
    "icon-512", "feature-graphic-1024x500", "screenshot-1-home",
    "screenshot-2-fullscreen", "screenshot-3-grid", "screenshot-4-settings",
]:
    _png = os.path.join(OUT_DIR, f"{_name}.png")
    Image.open(_png).convert("RGB").save(_png, optimize=True)
    os.remove(os.path.join(OUT_DIR, f"{_name}.svg"))

print("all assets generated")
