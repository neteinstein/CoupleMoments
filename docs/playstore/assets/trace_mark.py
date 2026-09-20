#!/usr/bin/env python3
"""Traces the designer's Couple Moments mark PNG into the two vector drawables that ship it.

Run from anywhere; it reads the 432px artwork (the launcher icon's foreground, which is the same
file the mark was always drawn from) and overwrites:

    core/ui/src/commonMain/composeResources/drawable/ic_couple_moments_mark.xml  (in-app splash)
    app/src/main/res/drawable/ic_splash_logo.xml                                 (system splash)

Needs numpy, scipy, scikit-image and Pillow. Re-run it whenever the designer's mark changes -
those two files are generated, not hand-edited.

The mark is four flat colours, so it traces cleanly. The source PNG is antialiased, so its alpha
ramp already encodes sub-pixel edge positions; the contours come from that continuous field rather
than from a binary mask, which is what keeps the traced outline faithful to a fraction of a source
pixel. It also carries a white edge matte - fine over the light launcher background it was cut
for, a pale halo around every shape on the splash screen's dark gradient - so the classifier below
deliberately ignores pixel colour near an edge.
"""
import math
import os
import textwrap

import numpy as np
from PIL import Image
from scipy import ndimage
from skimage import measure

SRC = "app/src/main/res/drawable-xxxhdpi/ic_launcher_foreground.png"
N = 432
VIEWPORT = 108.0

CIRCLE_A = 79.0
FIGURES = {"brown": (139, 74, 64), "red": (229, 52, 47), "heart": (255, 110, 145)}
# Painted back-to-front; each shape's mask is the union of itself and everything above it, so
# adjacent shapes overlap instead of leaving a hairline seam between them.
ORDER = ["brown", "red", "heart"]

FIT_ERR = 0.50 ** 2   # max squared deviation from the traced outline, source pixels
RDP_EPS = 0.30
CORNER_DEG = 52.0
CORNER_SPAN = 3.0     # arc length, source pixels, used to measure the turn at a vertex
TANGENT_SPAN = 2.0    # arc length, source pixels, over which a run's end tangent is measured
SMOOTH_SIGMA = 2.2    # arc length, source pixels, of the anti-zig-zag Gaussian

REPO = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "..")

img = np.array(Image.open(os.path.join(REPO, SRC)).convert("RGBA")).astype(np.float64)
alpha, rgb = img[..., 3], img[..., :3]

# ---- 1. Recover the exact circle ------------------------------------------------------------
band = (alpha > 40) & (alpha < 170)
edge = band & ~ndimage.binary_erosion(alpha > 8, np.ones((3, 3)))
ys, xs = np.nonzero(edge)
pts = np.column_stack([xs + 0.5, ys + 0.5])
for _ in range(8):   # refit, dropping points that are not on the circle (figure boundaries)
    A = np.column_stack([pts[:, 0], pts[:, 1], np.ones(len(pts))])
    sol, *_ = np.linalg.lstsq(A, pts[:, 0] ** 2 + pts[:, 1] ** 2, rcond=None)
    cx, cy = sol[0] / 2, sol[1] / 2
    r = math.sqrt(sol[2] + cx * cx + cy * cy)
    resid = np.abs(np.hypot(pts[:, 0] - cx, pts[:, 1] - cy) - r)
    keep = resid < max(0.6, np.percentile(resid, 92))
    if keep.all():
        break
    pts = pts[keep]
print(f"circle cx={cx:.3f} cy={cy:.3f} r={r:.3f}  residual max={resid[keep].max():.3f}px "
      f"over {keep.sum()} edge points")

# ---- 2. Continuous coverage fields ----------------------------------------------------------
yy, xx = np.mgrid[0:N, 0:N]
disc = np.clip(r + 0.5 - np.hypot(xx + 0.5 - cx, yy + 0.5 - cy), 0.0, 1.0)
bg = CIRCLE_A * disc
fig_cov = np.clip((alpha - bg) / (255.0 - bg), 0.0, 1.0)

cols = np.array([FIGURES[k] for k in ORDER], dtype=np.float64)
d2 = ((rgb[..., None, :] - cols) ** 2).sum(-1)
order2 = np.argsort(d2, axis=-1)
first, second = order2[..., 0], order2[..., 1]

# Deep inside the figure union a pixel is a clean blend of the two nearest figure colours, so it
# unmixes exactly. Near the outer edge the source's white matte corrupts the hue, so there the
# label is taken from the nearest reliable interior pixel instead.
ca, cb = cols[first], cols[second]
ab = cb - ca
t = np.clip(((rgb - ca) * ab).sum(-1) / np.maximum((ab * ab).sum(-1), 1e-9), 0.0, 1.0)

# "Reliable" means far enough inside the figure union that the source's white edge matte cannot
# have tinted the pixel - at the very edge a nearly-opaque pixel still reads as pale pink, which
# would otherwise be mistaken for the heart colour.
reliable = ndimage.binary_erosion(fig_cov > 0.97, np.ones((5, 5)))
_, idx = ndimage.distance_transform_edt(~reliable, return_indices=True)
propagated = first[tuple(idx)]

fields = {}
for i, name in enumerate(ORDER):
    w_unmix = np.where(first == i, 1.0 - t, 0.0) + np.where(second == i, t, 0.0)
    w = np.where(reliable, w_unmix, (propagated == i).astype(np.float64))
    fields[name] = fig_cov * w
for i in range(len(ORDER) - 2, -1, -1):
    fields[ORDER[i]] = np.minimum(1.0, fields[ORDER[i]] + fields[ORDER[i + 1]])


# ---- 3. Contours ----------------------------------------------------------------------------
def polygon_area(p):
    x, y = p[:, 0], p[:, 1]
    return 0.5 * float(np.sum(x * np.roll(y, -1) - np.roll(x, -1) * y))


def contours(field, min_area=40.0):
    padded = np.pad(field, 1)
    out = []
    for c in measure.find_contours(padded, 0.5):
        p = (c[:, ::-1] - 1.0)          # (row, col) -> (x, y)
        if abs(polygon_area(p)) >= min_area:
            out.append(p)
    return out


# ---- 4. Fit cubic Beziers (Schneider) --------------------------------------------------------
def unit(v):
    n = float(np.linalg.norm(v))
    return v / n if n > 1e-12 else np.array([0.0, 0.0])


def arc_lengths(pts):
    return np.concatenate([[0.0], np.cumsum(np.hypot(*np.diff(pts, axis=0).T))])


def find_corners(pts):
    """Indices where the outline turns sharply, measured over a fixed arc-length window."""
    n = len(pts)
    s = arc_lengths(np.vstack([pts, pts[:1]]))
    out = []
    for i in range(n):
        j = k = i
        while (s[i] - s[j % n] if j >= 0 else 0) < CORNER_SPAN and i - j < n // 3:
            j -= 1
        while (s[min(k, n)] - s[i]) < CORNER_SPAN and k - i < n // 3:
            k += 1
        a, b = unit(pts[i] - pts[j % n]), unit(pts[k % n] - pts[i])
        if np.any(a) and np.any(b):
            ang = math.degrees(math.acos(max(-1.0, min(1.0, float(a @ b)))))
            if ang > CORNER_DEG:
                out.append((i, ang))
    # Collapse each run of adjacent candidates down to its sharpest vertex.
    corners, run = [], []
    for i, ang in out:
        if run and i - run[-1][0] > max(2, int(CORNER_SPAN)):
            corners.append(max(run, key=lambda v: v[1])[0])
            run = []
        run.append((i, ang))
    if run:
        corners.append(max(run, key=lambda v: v[1])[0])
    if len(corners) >= 2 and (corners[0] + n - corners[-1]) <= max(2, int(CORNER_SPAN)):
        corners.pop()
    return sorted(set(corners))


def _rdp(pts, eps):
    if len(pts) < 3:
        return [p for p in pts]
    a, b = pts[0], pts[-1]
    ab = b - a
    L = float(np.linalg.norm(ab))
    if L < 1e-9:
        dev = np.hypot(*(pts - a).T)
    else:
        dev = np.abs((pts - a)[:, 0] * ab[1] - (pts - a)[:, 1] * ab[0]) / L
    i = int(np.argmax(dev))
    if dev[i] <= eps:
        return [a, b]
    return _rdp(pts[: i + 1], eps)[:-1] + _rdp(pts[i:], eps)


def bezier(b, t):
    mt = 1 - t
    return mt ** 3 * b[0] + 3 * mt * mt * t * b[1] + 3 * mt * t * t * b[2] + t ** 3 * b[3]


def chord_params(pts):
    d = arc_lengths(pts)
    return d / d[-1] if d[-1] > 0 else np.linspace(0, 1, len(pts))


def generate_bezier(pts, u, t1, t2):
    p0, p3 = pts[0], pts[-1]
    a1 = (3 * (1 - u) ** 2 * u)[:, None] * t1
    a2 = (3 * (1 - u) * u ** 2)[:, None] * t2
    tmp = pts - (((1 - u) ** 3 + 3 * (1 - u) ** 2 * u)[:, None] * p0
                 + (3 * (1 - u) * u ** 2 + u ** 3)[:, None] * p3)
    c00, c01, c11 = np.sum(a1 * a1), np.sum(a1 * a2), np.sum(a2 * a2)
    x0, x1 = np.sum(a1 * tmp), np.sum(a2 * tmp)
    det = c00 * c11 - c01 * c01
    seg = float(np.linalg.norm(p3 - p0))
    if abs(det) < 1e-12:
        a = b = seg / 3.0
    else:
        a = (x0 * c11 - c01 * x1) / det
        b = (c00 * x1 - x0 * c01) / det
        # A least-squares solve on a nearly-degenerate run can throw the handles far outside the
        # shape; fall back to the plain chord heuristic rather than emit a spike.
        lim = max(seg, 1e-6) * 1.6
        if not (1e-6 < a < lim and 1e-6 < b < lim):
            a = b = seg / 3.0
    return (p0, p0 + t1 * a, p3 + t2 * b, p3)


def max_error(pts, bez, u):
    diff = bezier(bez, u[:, None]) - pts
    d = (diff ** 2).sum(-1)
    i = int(np.argmax(d))
    return float(d[i]), i


def reparam(pts, bez, u):
    q1 = [3 * (bez[i + 1] - bez[i]) for i in range(3)]
    q2 = [2 * (q1[i + 1] - q1[i]) for i in range(2)]
    mt = (1 - u)[:, None]
    tt = u[:, None]
    d = bezier(bez, u[:, None]) - pts
    d1 = mt * mt * q1[0] + 2 * mt * tt * q1[1] + tt * tt * q1[2]
    d2 = mt * q2[0] + tt * q2[1]
    den = (d1 * d1).sum(-1) + (d * d2).sum(-1)
    step = np.where(np.abs(den) < 1e-12, 0.0, (d * d1).sum(-1) / np.where(den == 0, 1, den))
    return np.clip(u - step, 0.0, 1.0)


def fit_cubic(pts, t1, t2, depth=0):
    if len(pts) == 2:
        d = float(np.linalg.norm(pts[1] - pts[0])) / 3.0
        return [(pts[0], pts[0] + t1 * d, pts[1] + t2 * d, pts[1])]
    u = chord_params(pts)
    bez = generate_bezier(pts, u, t1, t2)
    err, split = max_error(pts, bez, u)
    if err < FIT_ERR:
        return [bez]
    # Newton reparameterisation almost always rescues a run that chord-length parameterisation
    # alone fits poorly, so try it before paying for a split.
    if err < FIT_ERR * 2500:
        for _ in range(12):
            u = reparam(pts, bez, u)
            bez = generate_bezier(pts, u, t1, t2)
            err, split = max_error(pts, bez, u)
            if err < FIT_ERR:
                return [bez]
    if depth > 22 or split <= 0 or split >= len(pts) - 1:
        d = float(np.linalg.norm(pts[-1] - pts[0])) / 3.0
        return [(pts[0], pts[0] + t1 * d, pts[-1] + t2 * d, pts[-1])]
    centre = unit(pts[split - 1] - pts[split + 1])
    return (fit_cubic(pts[: split + 1], t1, centre, depth + 1)
            + fit_cubic(pts[split:], -centre, t2, depth + 1))


def tangent(pts, span=TANGENT_SPAN):
    """Direction leaving pts[0], averaged over `span` of arc length so one noisy step can't skew it."""
    s = arc_lengths(pts)
    j = int(np.searchsorted(s, min(span, s[-1])))
    return unit(pts[min(max(j, 1), len(pts) - 1)] - pts[0])


def fit_run(run):
    if len(run) < 3:
        return fit_cubic(run, unit(run[-1] - run[0]), unit(run[0] - run[-1]))
    # Decimate only enough to keep the least-squares solve cheap; Schneider fits best on dense data.
    simple = np.array(_rdp(run, RDP_EPS))
    if len(simple) < 3:
        simple = run
    return fit_cubic(simple, tangent(run), tangent(run[::-1]))


def denoise(pts, corners):
    """Removes the marching-squares zig-zag while leaving detected corners exactly where they are.

    Thresholding an area-coverage field at 0.5 puts each crossing up to ~0.3px off a diagonal edge,
    which is invisible on its own but forces the curve fitter to spend dozens of segments chasing
    noise. A sub-pixel Gaussian along the outline removes it; corners are faded back to their raw
    positions so they stay sharp.
    """
    n = len(pts)
    smooth = ndimage.gaussian_filter1d(pts, SMOOTH_SIGMA / 0.8, axis=0, mode="wrap")
    if not corners:
        return smooth
    s = arc_lengths(np.vstack([pts, pts[:1]]))
    keep = np.zeros(n)
    for c in corners:
        d = np.abs(s[:n] - s[c])
        d = np.minimum(d, s[-1] - d)
        keep = np.maximum(keep, np.clip(1.0 - d / CORNER_SPAN, 0.0, 1.0))
    return smooth + (pts - smooth) * keep[:, None]


def fit_path(pts):
    if np.allclose(pts[0], pts[-1]):
        pts = pts[:-1]
    n = len(pts)
    if n < 4:
        return []
    corners = find_corners(pts)
    pts = denoise(pts, corners)
    if not corners:
        closed = np.vstack([pts, pts[:1]])
        simple = np.array(_rdp(closed, RDP_EPS))
        tan = tangent(closed)
        return fit_cubic(simple, tan, -tan)
    segs = []
    for a, b in zip(corners, corners[1:] + [corners[0] + n]):
        segs += fit_run(np.array([pts[i % n] for i in range(a, b + 1)]))
    return segs


# ---- 5. Emit ---------------------------------------------------------------------------------
S = VIEWPORT / N


def fmt(v):
    s = f"{v * S:.3f}".rstrip("0").rstrip(".")
    return "0" if s in ("-0", "") else s


def path_data(runs):
    out = []
    for segs in runs:
        if not segs:
            continue
        out.append(f"M{fmt(segs[0][0][0])},{fmt(segs[0][0][1])}")
        for _, p1, p2, p3 in segs:
            out.append(f"C{fmt(p1[0])},{fmt(p1[1])} {fmt(p2[0])},{fmt(p2[1])} "
                       f"{fmt(p3[0])},{fmt(p3[1])}")
        out.append("Z")
    return "".join(out)


DESTS = {
    # The in-app splash screen's mark, via Compose Multiplatform's composeResources.
    "core/ui/src/commonMain/composeResources/drawable/ic_couple_moments_mark.xml": """\
<!-- The Couple Moments mark, traced from the designer's 432px artwork by
     docs/playstore/assets/trace_mark.py.

     It is a vector rather than the density-bucketed PNGs it replaces for two reasons. The splash
     screen draws it at 160dp and animates a continuous heartbeat scale on top, so no fixed raster
     size is ever the one actually needed, and the source PNGs carried a white edge matte that
     showed up as a pale outline traced around every shape once the mark was drawn on the splash's
     dark red gradient.""",
    # The system (Android 12+) splash screen's icon - see themes.xml.
    "app/src/main/res/drawable/ic_splash_logo.xml": """\
<!-- The Couple Moments mark, traced from the designer's 432px artwork by
     docs/playstore/assets/trace_mark.py, shown as the system splash screen's icon
     (windowSplashScreenAnimatedIcon in themes.xml).

     It is a vector rather than the density-bucketed PNGs it replaces because the platform scales
     this icon to fit the splash window rather than picking a density bucket, so no fixed raster
     size was ever the one actually needed. Identical to core:ui's composeResources copy below the
     comment - the two resource systems can't share one file.""",
}

LAYERS = [
    ("circle", "#F8CECB", '\n        android:fillAlpha="0.31"', "The soft disc behind the couple."),
    ("brown", "#8B4A40", "", "Left figure, behind."),
    ("red", "#E5342F", "", "Right figure."),
    ("heart", "#FF6E91", "", "The heart where the two arms cross."),
]

paths = {}
for name in ORDER:
    runs = [fit_path(c) for c in contours(fields[name])]
    paths[name] = path_data(runs)
    print(f"{name:6s}: {len([r for r in runs if r])} subpath(s), "
          f"{sum(len(r) for r in runs)} curves, {len(paths[name])} chars")

k = r * 0.5522847498307936
paths["circle"] = (
    f"M{fmt(cx - r)},{fmt(cy)}"
    f"C{fmt(cx - r)},{fmt(cy - k)} {fmt(cx - k)},{fmt(cy - r)} {fmt(cx)},{fmt(cy - r)}"
    f"C{fmt(cx + k)},{fmt(cy - r)} {fmt(cx + r)},{fmt(cy - k)} {fmt(cx + r)},{fmt(cy)}"
    f"C{fmt(cx + r)},{fmt(cy + k)} {fmt(cx + k)},{fmt(cy + r)} {fmt(cx)},{fmt(cy + r)}"
    f"C{fmt(cx - k)},{fmt(cy + r)} {fmt(cx - r)},{fmt(cy + k)} {fmt(cx - r)},{fmt(cy)}Z")

body = []
for key, colour, extra, note in LAYERS:
    wrapped = "\n".join("        " + line for line in textwrap.wrap(paths[key], 96))
    body.append(f"    <!-- {note} -->\n"
                f'    <path\n        android:fillColor="{colour}"{extra}\n'
                f'        android:fillType="evenOdd"\n'
                f'        android:pathData="\n{wrapped}" />')
shared = ('<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
          '    android:width="108dp"\n    android:height="108dp"\n'
          '    android:viewportWidth="108"\n    android:viewportHeight="108">\n'
          + "\n".join(body) + "\n</vector>\n")

for dest, header in DESTS.items():
    path = os.path.join(REPO, dest)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n' + header + """

     Each shape is painted over the one before it and its outline includes everything stacked on
     top, so overlapping figures share an edge instead of leaving a hairline of background between
     them. -->
""" + shared)
    print("wrote", dest)
