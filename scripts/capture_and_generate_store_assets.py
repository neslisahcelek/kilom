#!/usr/bin/env python3
"""
Automates capturing native iPhone 17 Pro Max (1320x2868) screenshots in TR & EN,
and composites them into world-class Apple (2026) Liquid Glass App Store marketing visuals
with realistic Titanium Pro Max bezel, Dynamic Island, clean 9:41 status bar, and sRGB profile.

Outputs:
- art/app-store/raw/{lang}/{slide_id}.png
- art/app-store/{lang}/iphone-6.9/     (1320x2868 - 6.9" Display: iPhone 16/17 Pro Max)
- art/app-store/{lang}/iphone-6.7/     (1290x2796 - 6.7" Display: iPhone 14/15 Pro Max)
- art/app-store/{lang}/iphone-6.5/     (1284x2778 - 6.5" Display: iPhone 12/13/14 Pro Max & Plus)
- art/app-store/{lang}/iphone-6.5-alt/ (1242x2688 - 6.5" Display: iPhone 11 Pro Max / XS Max)
- art/app-store/{lang}/iphone-6.3/     (1206x2622 - 6.3" Display: iPhone 16/17 Pro)
- art/app-store/{lang}/iphone-6.1/     (1179x2556 - 6.1" Display: iPhone 14/15/16)
- art/app-store/{lang}/iphone-5.5/     (1242x2208 - 5.5" Display: iPhone 8 Plus)
"""

import os
import sys
import time
import subprocess
from PIL import Image, ImageDraw, ImageFont, ImageFilter

SIMULATOR_UUID = "9B1B5F93-7D08-474B-8BEA-0975F5FA751B"
BUNDLE_ID = "com.nesli.kilo"

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
ART_DIR = os.path.join(ROOT_DIR, "art", "app-store")

SLIDES_CONFIG = [
    {
        "id": "1_dashboard",
        "mode": "standard",
        "preview": None,
        "accent": (56, 189, 248),      # Sky Cyan
        "glow_sec": (99, 102, 241),    # Indigo
        "tr": {
            "badge": "SIVI CAM TASARIM • %100 CİHAZ İÇİ",
            "title": "Tartını Anında Kaydet",
            "subtitle": "Hareketli ortalama trend eğrisi ve akıllı hedef takibi"
        },
        "en": {
            "badge": "LIQUID GLASS • 100% ON-DEVICE",
            "title": "Track Weight Effortlessly",
            "subtitle": "Moving average trend line & smart goal arrival date"
        }
    },
    {
        "id": "2_ocr_scanner",
        "mode": "standard",
        "preview": "entry_ocr",
        "accent": (52, 211, 153),      # Emerald Mint
        "glow_sec": (14, 165, 233),    # Ocean Blue
        "tr": {
            "badge": "APPLE VISION • YEREL TARAMA",
            "title": "Tartı Ekranını Kamerayla Tara",
            "subtitle": "7 segmentli dijital ekranları telefonunda anında tanı"
        },
        "en": {
            "badge": "APPLE VISION • LOCAL AI OCR",
            "title": "Scan Your Scale Instantly",
            "subtitle": "On-device Vision reads digital LED & LCD displays"
        }
    },
    {
        "id": "3_doms_shield",
        "mode": "doms",
        "preview": None,
        "accent": (251, 191, 36),      # Warm Amber
        "glow_sec": (244, 63, 94),     # Coral Rose
        "tr": {
            "badge": "EGZERSİZ FİZYOLOJİSİ & ÖDEM MODELİ",
            "title": "Antrenman Ödemini Anla",
            "subtitle": "Ağır antrenman sonrası su tutulumunu yağ sanma"
        },
        "en": {
            "badge": "EXERCISE PHYSIOLOGY & FLUID SHIELD",
            "title": "Decode Workout Fluid Shifts",
            "subtitle": "Post-workout muscle repair & glycogen water, not fat"
        }
    },
    {
        "id": "4_strength_1rm",
        "mode": "standard",
        "preview": "workout_sheet",
        "accent": (167, 139, 250),     # Electric Violet
        "glow_sec": (59, 130, 246),    # Royal Blue
        "tr": {
            "badge": "NSCA NORMATİF KUVVET STANDARTLARI",
            "title": "1RM Göreceli Kuvvetini Gör",
            "subtitle": "Epley formülü, vücut ağırlığına oran ve 4 seviye"
        },
        "en": {
            "badge": "NSCA NORMATIVE STRENGTH STANDARDS",
            "title": "Universal 1RM Relative Strength",
            "subtitle": "Epley formula, strength-to-weight tiers & compound PRs"
        }
    },
    {
        "id": "5_cycle_nutrition",
        "mode": "standard",
        "preview": "cycle_sheet",
        "accent": (244, 114, 182),     # Rose Pink
        "glow_sec": (192, 132, 252),   # Soft Lavender
        "tr": {
            "badge": "KANITA DAYALI KADIN SAĞLIĞI",
            "title": "Hormonal Döngüne Göre Beslen",
            "subtitle": "Luteal evre ödem uyarısı ve döneme özgü mikrobesinler"
        },
        "en": {
            "badge": "EVIDENCE-BASED INFRADIAN RHYTHMS",
            "title": "Cycle-Synced Body & Nutrition",
            "subtitle": "Luteal water retention alerts & phase-specific nutrition"
        }
    },
    {
        "id": "6_streak_share",
        "mode": "standard",
        "preview": "share_sheet",
        "accent": (45, 212, 191),      # Teal Mint
        "glow_sec": (59, 130, 246),    # Sapphire
        "tr": {
            "badge": "%100 GİZLİLİK • SIFIR SUNUCU",
            "title": "İstikrarını Gör ve Paylaş",
            "subtitle": "14 günlük alışkanlık matrisi ve kilonu gizleme seçeneği"
        },
        "en": {
            "badge": "100% PRIVATE • ZERO SERVERS",
            "title": "Build Habits & Share Privately",
            "subtitle": "14-day consistency heatmap with hide-weight share card"
        }
    }
]


def run_cmd(cmd):
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if res.returncode != 0 and "found nothing to terminate" not in res.stderr:
        print(f"Command warning: {res.stderr.strip()}")
    return res.stdout


def capture_screens():
    raw_dir = os.path.join(ART_DIR, "raw")
    os.makedirs(raw_dir, exist_ok=True)

    # Set clean Apple marketing status bar (9:41, full signal, 100% battery)
    run_cmd([
        "xcrun", "simctl", "status_bar", SIMULATOR_UUID, "override",
        "--time", "9:41",
        "--dataNetwork", "wifi",
        "--wifiMode", "active",
        "--wifiBars", "3",
        "--cellularMode", "active",
        "--cellularBars", "4",
        "--batteryState", "charged",
        "--batteryLevel", "100"
    ])

    languages = [
        ("tr", "-AppleLanguages '(tr)' -AppleLocale 'tr_TR'"),
        ("en", "-AppleLanguages '(en)' -AppleLocale 'en_US'")
    ]

    for lang_code, lang_args in languages:
        lang_raw_dir = os.path.join(raw_dir, lang_code)
        os.makedirs(lang_raw_dir, exist_ok=True)
        print(f"\n==========================================")
        print(f"Capturing screenshots for language: {lang_code.upper()}")
        print(f"==========================================")

        for slide in SLIDES_CONFIG:
            slide_id = slide["id"]
            mode = slide["mode"]
            preview = slide["preview"]
            target_raw = os.path.join(lang_raw_dir, f"{slide_id}.png")

            print(f"\n[+] Preparing slide: {slide_id} (mode={mode}, preview={preview})")

            subprocess.run(
                [sys.executable, os.path.join(ROOT_DIR, "scripts", "seed_demo_data.py"), f"--mode={mode}"],
                check=True
            )

            if preview:
                run_cmd(["xcrun", "simctl", "spawn", SIMULATOR_UUID, "defaults", "write", BUNDLE_ID, "app_store_screen_preview", preview])
            else:
                run_cmd(["xcrun", "simctl", "spawn", SIMULATOR_UUID, "defaults", "delete", BUNDLE_ID, "app_store_screen_preview"])

            run_cmd(["xcrun", "simctl", "terminate", SIMULATOR_UUID, BUNDLE_ID])
            time.sleep(0.5)

            launch_cmd = f"xcrun simctl launch {SIMULATOR_UUID} {BUNDLE_ID} {lang_args}"
            subprocess.run(launch_cmd, shell=True, check=True)
            time.sleep(2.5)

            run_cmd(["xcrun", "simctl", "io", SIMULATOR_UUID, "screenshot", target_raw])
            # Ensure raw screenshot is saved as 24-bit RGB without alpha
            raw_im = Image.open(target_raw).convert("RGB")
            raw_im.save(target_raw, format="PNG")
            print(f"  -> Saved raw screenshot: {target_raw}")


def get_sf_font(size, weight=400, width=100, optical_size=None):
    """
    Loads macOS native San Francisco Variable Font (/System/Library/Fonts/SFNS.ttf)
    with explicit Weight and Optical Size axes so headlines render in true Apple Display Bold/Heavy.
    Axes of SFNS.ttf: [Width (30..150), Optical Size (17..96), GRAD (400..1000), Weight (1..1000)]
    """
    sf_path = "/System/Library/Fonts/SFNS.ttf"
    if os.path.exists(sf_path):
        try:
            font = ImageFont.truetype(sf_path, size)
            opt = optical_size if optical_size is not None else min(96, max(17, int(size * 0.75)))
            font.set_variation_by_axes([width, opt, 400, weight])
            return font
        except Exception:
            pass

    fallback_candidates = [
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf" if weight >= 600 else "/System/Library/Fonts/Supplemental/Arial.ttf",
        "/System/Library/Fonts/HelveticaNeue.ttc",
        "/System/Library/Fonts/Helvetica.ttc",
    ]
    for c in fallback_candidates:
        if os.path.exists(c):
            try:
                return ImageFont.truetype(c, size)
            except Exception:
                continue
    return ImageFont.load_default()


def clean_and_add_dynamic_island(raw_screen):
    """
    Takes a 1320x2868 raw simulator screenshot:
    1. Cleans the simulator's status bar clock (e.g. 20:08 -> 9:41) and cellular dots (.... -> 4 full signal bars)
    2. Renders a pixel-accurate Apple Dynamic Island hardware cutout in the top center with optical camera lens details.
    """
    screen = raw_screen.copy().convert("RGBA")
    w, h = screen.size
    draw = ImageDraw.Draw(screen)

    # 1. Seamlessly erase old clock (x=165..345, y=68..130) by sampling each column at y=64
    for x in range(165, 346):
        top_px = screen.getpixel((x, 64))
        for y in range(68, 131):
            screen.putpixel((x, y), top_px)

    clock_font = get_sf_font(51, weight=640, optical_size=28)
    clock_text = "9:41"
    cb = clock_font.getbbox(clock_text)
    cw, ch = cb[2] - cb[0], cb[3] - cb[1]
    cx = 248 - cw // 2
    cy = 97 - ch // 2 - cb[1]
    draw.text((cx, cy), clock_text, fill=(18, 22, 30, 255), font=clock_font)

    # 2. Seamlessly erase cellular dots (x=936..1018, y=95..119) via vertical linear interpolation
    for x in range(936, 1019):
        p_top = screen.getpixel((x, 94))
        p_bot = screen.getpixel((x, 120))
        for y in range(95, 120):
            t_y = (y - 94) / 26.0
            interp = (
                int(p_top[0] * (1 - t_y) + p_bot[0] * t_y),
                int(p_top[1] * (1 - t_y) + p_bot[1] * t_y),
                int(p_top[2] * (1 - t_y) + p_bot[2] * t_y),
                255
            )
            screen.putpixel((x, y), interp)

    bar_color = (22, 26, 35, 255)
    bar_heights = [14, 21, 29, 38]
    bar_w = 9
    bar_gap = 6
    base_x = 955
    base_bottom_y = 115
    for idx, bh in enumerate(bar_heights):
        bx = base_x + idx * (bar_w + bar_gap)
        by = base_bottom_y - bh
        draw.rounded_rectangle([bx, by, bx + bar_w, base_bottom_y], radius=3, fill=bar_color)

    # 3. Render Authentic Apple Dynamic Island (Centered at top of screen)
    # On 1320x2868 (iPhone 16/17 Pro Max): width=372px, height=108px, center_y=96px
    di_w = 372
    di_h = 108
    di_x1 = (w - di_w) // 2
    di_y1 = 42
    di_x2 = di_x1 + di_w
    di_y2 = di_y1 + di_h
    di_radius = di_h // 2

    # Subtle outer anti-aliased dark rim
    draw.rounded_rectangle(
        [di_x1 - 2, di_y1 - 2, di_x2 + 2, di_y2 + 2],
        radius=di_radius + 2,
        fill=(28, 32, 42, 95)
    )
    # Core pitch-black Dynamic Island pill
    draw.rounded_rectangle(
        [di_x1, di_y1, di_x2, di_y2],
        radius=di_radius,
        fill=(6, 7, 10, 255),
        outline=(38, 42, 54, 180),
        width=2
    )

    # TrueDepth infrared sensor module (left interior pill/circle)
    sensor_cx = di_x1 + 92
    sensor_cy = (di_y1 + di_y2) // 2
    draw.ellipse(
        [sensor_cx - 18, sensor_cy - 18, sensor_cx + 18, sensor_cy + 18],
        fill=(15, 17, 24, 255),
        outline=(26, 30, 40, 255),
        width=2
    )

    # FaceTime HD Camera Lens (right side of Dynamic Island)
    cam_cx = di_x2 - 58
    cam_cy = (di_y1 + di_y2) // 2
    cam_r = 22
    # Outer dark housing ring
    draw.ellipse(
        [cam_cx - cam_r, cam_cy - cam_r, cam_cx + cam_r, cam_cy + cam_r],
        fill=(14, 18, 30, 255),
        outline=(32, 38, 56, 255),
        width=2
    )
    # Deep sapphire optical core
    inner_r = 13
    draw.ellipse(
        [cam_cx - inner_r, cam_cy - inner_r, cam_cx + inner_r, cam_cy + inner_r],
        fill=(6, 9, 18, 255)
    )
    # Realistic blue-violet anti-reflective lens coating glint
    draw.ellipse(
        [cam_cx - 8, cam_cy - 9, cam_cx - 1, cam_cy - 2],
        fill=(70, 130, 235, 190)
    )
    draw.ellipse(
        [cam_cx + 2, cam_cy + 3, cam_cx + 6, cam_cy + 7],
        fill=(110, 175, 255, 110)
    )

    return screen


def draw_gradient_headline(canvas, text, font, y_pos, accent_rgb):
    """
    Renders the main headline with a smooth vertical Apple metallic-pearl gradient
    (#FFFFFF top -> soft ice tint bottom) and subtle drop shadow for maximum legibility.
    """
    out_w, out_h = canvas.size
    bbox = font.getbbox(text)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]

    # If text is wider than 90% of canvas, shrink font slightly to fit with generous margin
    current_size = font.size
    while tw > out_w * 0.90 and current_size > 44:
        current_size -= 2
        font = get_sf_font(current_size, weight=760, optical_size=72)
        bbox = font.getbbox(text)
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]

    tx = (out_w - tw) // 2

    # 1. Soft ambient text shadow
    shadow_layer = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_layer)
    s_draw.text((tx, y_pos + 6), text, font=font, fill=(0, 0, 0, 135))
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(radius=10))
    canvas.alpha_composite(shadow_layer)

    # 2. Create alpha mask of the headline text
    mask = Image.new("L", (tw + 24, th + 32), 0)
    m_draw = ImageDraw.Draw(mask)
    m_draw.text((12 - bbox[0], 16 - bbox[1]), text, font=font, fill=255)

    # 3. Vertical Pearl-to-Ice Gradient fill for the text
    grad = Image.new("RGBA", (tw + 24, th + 32), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(grad)
    gh = th + 32
    for row in range(gh):
        t = min(1.0, max(0.0, (row - 10) / float(max(1, th))))
        # Pure white (#FFFFFF) at top -> soft tinted pearl at bottom
        r = int(255 * (1.0 - t * 0.14) + accent_rgb[0] * (t * 0.14))
        g = int(255 * (1.0 - t * 0.10) + accent_rgb[1] * (t * 0.10))
        b = int(255 * (1.0 - t * 0.04) + accent_rgb[2] * (t * 0.04))
        g_draw.line([(0, row), (tw + 24, row)], fill=(r, g, b, 255))

    text_layer = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    text_layer.paste(grad, (tx - 12, y_pos - 16), mask)
    canvas.alpha_composite(text_layer)

    return y_pos + th


def render_marketing_banner(raw_img_path, slide_data, lang_code, out_w=1320, out_h=2868):
    lang_info = slide_data[lang_code]
    badge_text = lang_info["badge"]
    title_text = lang_info["title"]
    subtitle_text = lang_info["subtitle"]
    accent_r, accent_g, accent_b = slide_data["accent"]
    sec_r, sec_g, sec_b = slide_data["glow_sec"]

    # 1. Base Canvas (1320 x 2868) with Deep Obsidian-Midnight Gradient
    canvas = Image.new("RGBA", (out_w, out_h), (7, 10, 20, 255))
    draw = ImageDraw.Draw(canvas)

    for y in range(out_h):
        t = y / float(out_h)
        # Smooth midnight gradient: #070B16 -> #0F172A -> #090D1A
        if t < 0.5:
            lt = t * 2.0
            r = int(7 + (15 - 7) * lt)
            g = int(11 + (23 - 11) * lt)
            b = int(22 + (42 - 22) * lt)
        else:
            lt = (t - 0.5) * 2.0
            r = int(15 + (9 - 15) * lt)
            g = int(23 + (13 - 23) * lt)
            b = int(42 + (26 - 42) * lt)
        draw.line([(0, y), (out_w, y)], fill=(r, g, b, 255))

    # 2. Ambient Liquid Glass Caustics & Soft Backlight Aura
    glow = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)

    # Top-center ambient spotlight behind the headline & top of phone
    glow_draw.ellipse(
        [out_w // 2 - 580, -140, out_w // 2 + 580, 760],
        fill=(accent_r, accent_g, accent_b, 44)
    )
    # Secondary aura behind phone body
    glow_draw.ellipse(
        [-220, 320, 620, 1280],
        fill=(sec_r, sec_g, sec_b, 38)
    )
    glow_draw.ellipse(
        [out_w - 620, 420, out_w + 220, 1360],
        fill=(accent_r, accent_g, accent_b, 34)
    )
    glow_blurred = glow.filter(ImageFilter.GaussianBlur(radius=135))
    canvas.alpha_composite(glow_blurred)

    # 3. Redesigned Apple (2026) Liquid Glass Header (Properly Alpha-Composited!)
    # A) Dark Frosted Glass Kicker Pill (NO solid white rectangle bug!)
    badge_font = get_sf_font(26, weight=660, width=104, optical_size=24)
    badge_bbox = badge_font.getbbox(badge_text)
    badge_w = badge_bbox[2] - badge_bbox[0]
    badge_h = badge_bbox[3] - badge_bbox[1]

    dot_size = 12
    dot_gap = 14
    badge_pad_x = 30
    badge_pad_y = 15
    badge_total_w = badge_w + dot_size + dot_gap + badge_pad_x * 2
    badge_total_h = badge_h + badge_pad_y * 2
    badge_x = (out_w - badge_total_w) // 2
    badge_y = 108

    pill_layer = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    p_draw = ImageDraw.Draw(pill_layer)

    # Dark translucent tinted glass fill + delicate specular border
    p_draw.rounded_rectangle(
        [badge_x, badge_y, badge_x + badge_total_w, badge_y + badge_total_h],
        radius=badge_total_h // 2,
        fill=(
            int(14 + accent_r * 0.12),
            int(20 + accent_g * 0.12),
            int(36 + accent_b * 0.12),
            185
        ),
        outline=(
            min(255, int(accent_r * 0.65 + 90)),
            min(255, int(accent_g * 0.65 + 90)),
            min(255, int(accent_b * 0.65 + 90)),
            95
        ),
        width=2
    )
    # Top edge specular gleam on pill
    p_draw.line(
        [badge_x + badge_total_h // 2, badge_y + 1, badge_x + badge_total_w - badge_total_h // 2, badge_y + 1],
        fill=(255, 255, 255, 95),
        width=1
    )

    # Glowing accent indicator dot inside pill
    dot_x = badge_x + badge_pad_x
    dot_cy = badge_y + badge_total_h // 2
    p_draw.ellipse(
        [dot_x - 3, dot_cy - dot_size // 2 - 3, dot_x + dot_size + 3, dot_cy + dot_size // 2 + 3],
        fill=(accent_r, accent_g, accent_b, 85)
    )
    p_draw.ellipse(
        [dot_x, dot_cy - dot_size // 2, dot_x + dot_size, dot_cy + dot_size // 2],
        fill=(accent_r, accent_g, accent_b, 255)
    )

    # Crisp badge label in bright ice-accent tint
    text_x = dot_x + dot_size + dot_gap
    text_y = badge_y + (badge_total_h - badge_h) // 2 - badge_bbox[1]
    p_draw.text(
        (text_x, text_y),
        badge_text,
        fill=(
            min(255, int(accent_r * 0.45 + 145)),
            min(255, int(accent_g * 0.45 + 145)),
            min(255, int(accent_b * 0.45 + 145)),
            255
        ),
        font=badge_font
    )
    canvas.alpha_composite(pill_layer)

    # B) Main Headline with Apple Display Bold + Metallic Pearl Gradient
    title_font = get_sf_font(70, weight=770, optical_size=72)
    title_y = badge_y + badge_total_h + 30
    title_bottom = draw_gradient_headline(canvas, title_text, title_font, title_y, slide_data["accent"])

    # C) Subheadline in Soft Silver-Slate
    sub_font = get_sf_font(36, weight=510, optical_size=32)
    sub_bbox = sub_font.getbbox(subtitle_text)
    sub_w = sub_bbox[2] - sub_bbox[0]
    sub_x = (out_w - sub_w) // 2
    sub_y = title_bottom + 24

    sub_layer = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    sub_draw = ImageDraw.Draw(sub_layer)
    sub_draw.text((sub_x, sub_y), subtitle_text, fill=(186, 204, 228, 245), font=sub_font)
    canvas.alpha_composite(sub_layer)

    # 4. Realistic iPhone 16 / 17 Pro Max Hardware Frame + Dynamic Island Screen
    raw_screen = Image.open(raw_img_path).convert("RGBA")
    enhanced_screen = clean_and_add_dynamic_island(raw_screen)

    device_scale = 0.825
    dev_w = int(enhanced_screen.width * device_scale)
    dev_h = int(enhanced_screen.height * device_scale)
    screen_resized = enhanced_screen.resize((dev_w, dev_h), Image.Resampling.LANCZOS)

    dev_x = (out_w - dev_w) // 2
    dev_y = 468  # Balanced spacing below subheadline

    screen_radius = int(118 * device_scale)  # True iPhone Pro Max continuous corner radius
    bezel_px = 18                            # OLED black display border
    titanium_px = 8                          # Outer Titanium chassis band
    outer_radius = screen_radius + bezel_px + titanium_px

    # A) Multi-stage Elevation Drop Shadow behind Titanium Chassis
    shadow_layer = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_layer)
    s_draw.rounded_rectangle(
        [
            dev_x - bezel_px - titanium_px - 16,
            dev_y - bezel_px - titanium_px + 22,
            dev_x + dev_w + bezel_px + titanium_px + 16,
            dev_y + dev_h + bezel_px + titanium_px + 45
        ],
        radius=outer_radius + 10,
        fill=(0, 0, 0, 175)
    )
    shadow_blurred = shadow_layer.filter(ImageFilter.GaussianBlur(radius=45))
    canvas.alpha_composite(shadow_blurred)

    # B) Hardware Chassis Layer (Buttons + Titanium Band + OLED Bezel)
    chassis_layer = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    c_draw = ImageDraw.Draw(chassis_layer)

    ox1 = dev_x - bezel_px - titanium_px
    oy1 = dev_y - bezel_px - titanium_px
    ox2 = dev_x + dev_w + bezel_px + titanium_px
    oy2 = dev_y + dev_h + bezel_px + titanium_px

    # Physical Side Buttons (Left: Action + Vol Up/Down, Right: Power)
    btn_color = (74, 82, 98, 255)
    # Action button
    c_draw.rounded_rectangle([ox1 - 5, oy1 + 320, ox1 + 4, oy1 + 385], radius=4, fill=btn_color)
    # Volume Up
    c_draw.rounded_rectangle([ox1 - 5, oy1 + 435, ox1 + 4, oy1 + 560], radius=4, fill=btn_color)
    # Volume Down
    c_draw.rounded_rectangle([ox1 - 5, oy1 + 590, ox1 + 4, oy1 + 715], radius=4, fill=btn_color)
    # Side Power Button
    c_draw.rounded_rectangle([ox2 - 4, oy1 + 490, ox2 + 5, oy1 + 685], radius=4, fill=btn_color)

    # Outer Titanium Chassis Body
    c_draw.rounded_rectangle(
        [ox1, oy1, ox2, oy2],
        radius=outer_radius,
        fill=(56, 62, 76, 255),
        outline=(135, 148, 172, 210),
        width=3
    )
    # Brushed Titanium inner chamfer ring
    c_draw.rounded_rectangle(
        [ox1 + 4, oy1 + 4, ox2 - 4, oy2 - 4],
        radius=outer_radius - 4,
        fill=(28, 31, 38, 255),
        outline=(82, 90, 108, 160),
        width=2
    )
    # Deep Pitch-Black OLED Display Bezel
    bx1 = dev_x - bezel_px
    by1 = dev_y - bezel_px
    bx2 = dev_x + dev_w + bezel_px
    by2 = dev_y + dev_h + bezel_px
    c_draw.rounded_rectangle(
        [bx1, by1, bx2, by2],
        radius=screen_radius + bezel_px,
        fill=(6, 7, 10, 255)
    )
    # Top speaker micro-slit in the top bezel
    slit_w = 136
    slit_x1 = (out_w - slit_w) // 2
    slit_y1 = by1 + 6
    c_draw.rounded_rectangle(
        [slit_x1, slit_y1, slit_x1 + slit_w, slit_y1 + 5],
        radius=2,
        fill=(22, 25, 32, 255)
    )
    # Top-edge specular catchlight on Titanium frame
    c_draw.line(
        [ox1 + outer_radius, oy1 + 1, ox2 - outer_radius, oy1 + 1],
        fill=(215, 228, 250, 175),
        width=2
    )

    canvas.alpha_composite(chassis_layer)

    # C) Paste Rounded Screen Image (with Dynamic Island & 9:41 Status Bar)
    screen_mask = Image.new("L", (dev_w, dev_h), 0)
    mask_draw = ImageDraw.Draw(screen_mask)
    mask_draw.rounded_rectangle([0, 0, dev_w, dev_h], radius=screen_radius, fill=255)

    canvas.paste(screen_resized, (dev_x, dev_y), screen_mask)

    # D) Subtle Inner Screen Edge Anti-Aliasing Ring
    edge_layer = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    e_draw = ImageDraw.Draw(edge_layer)
    e_draw.rounded_rectangle(
        [dev_x, dev_y, dev_x + dev_w, dev_y + dev_h],
        radius=screen_radius,
        outline=(255, 255, 255, 32),
        width=2
    )
    canvas.alpha_composite(edge_layer)

    # 5. Flatten to 100% Opaque 24-bit RGB (Strict App Store Connect requirement: no alpha channel)
    final_rgb = canvas.convert("RGB")
    return final_rgb


def apply_srgb_profile(filepath):
    """
    Ensures the saved PNG has an explicit sRGB IEC61966-2.1 color profile and no alpha channel
    using macOS native sips, preventing App Store Connect validator rejections.
    """
    srgb_icc = "/System/Library/ColorSync/Profiles/sRGB Profile.icc"
    if os.path.exists(srgb_icc):
        subprocess.run(
            ["sips", "--matchTo", srgb_icc, filepath],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )


def generate_all_banners():
    raw_dir = os.path.join(ART_DIR, "raw")

    # Complete set of official Apple App Store Connect portrait resolutions
    resolutions = [
        ("iphone-6.9",     1320, 2868),  # 6.9" Display (iPhone 16 Pro Max, 17 Pro Max)
        ("iphone-6.7",     1290, 2796),  # 6.7" Display (iPhone 15 Pro Max, 14 Pro Max)
        ("iphone-6.5",     1284, 2778),  # 6.5" Display (iPhone 14 Plus, 13 Pro Max, 12 Pro Max)
        ("iphone-6.5-alt", 1242, 2688),  # 6.5" Display (iPhone 11 Pro Max, XS Max)
        ("iphone-6.3",     1206, 2622),  # 6.3" Display (iPhone 16 Pro, 17 Pro)
        ("iphone-6.1",     1179, 2556),  # 6.1" Display (iPhone 15 Pro, 15, 14 Pro)
        ("iphone-5.5",     1242, 2208),  # 5.5" Display (iPhone 8 Plus)
    ]

    for lang_code in ["tr", "en"]:
        print(f"\n==========================================")
        print(f"Generating Marketing Banners for: {lang_code.upper()}")
        print(f"==========================================")

        lang_raw_dir = os.path.join(raw_dir, lang_code)

        for slide in SLIDES_CONFIG:
            slide_id = slide["id"]
            raw_path = os.path.join(lang_raw_dir, f"{slide_id}.png")
            if not os.path.exists(raw_path):
                print(f"[!] Warning: Raw screenshot missing: {raw_path}")
                continue

            # Also ensure raw file itself has no alpha channel
            raw_clean = Image.open(raw_path).convert("RGB")
            raw_clean.save(raw_path, format="PNG", dpi=(72, 72))

            print(f"[+] Rendering banner: {slide_id} ({lang_code})")
            master_69 = render_marketing_banner(raw_path, slide, lang_code, 1320, 2868)

            for res_name, target_w, target_h in resolutions:
                target_dir = os.path.join(ART_DIR, lang_code, res_name)
                os.makedirs(target_dir, exist_ok=True)
                target_file = os.path.join(target_dir, f"{slide_id}.png")

                if target_w == 1320 and target_h == 2868:
                    banner_img = master_69
                else:
                    banner_img = master_69.resize((target_w, target_h), Image.Resampling.LANCZOS).convert("RGB")

                banner_img.save(target_file, format="PNG", dpi=(72, 72))
                apply_srgb_profile(target_file)
                print(f"  -> Generated {res_name} ({target_w}x{target_h}): {target_file}")


def main():
    print("Starting Kilom App Store Screenshot & Banner Generation Pipeline...")
    if "--render-only" not in sys.argv:
        capture_screens()
    generate_all_banners()
    print("\n[SUCCESS] All TR and EN App Store visual assets have been successfully created!")


if __name__ == "__main__":
    main()
