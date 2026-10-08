"""
Script to generate Kilo app icons with liquid-glass aesthetic.
Produces:
- 1024x1024 master icon (art/icon_1024.png)
- Google Play Store 512x512 icon (art/playstore-icon.png)
- iOS xcassets (iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/icon_1024.png + Contents.json)
- Android mipmaps (ic_launcher.png and ic_launcher_round.png in hdpi, mdpi, xhdpi, xxhdpi, xxxhdpi)
"""

import math
import os
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

def create_master_icon(size=1024):
    # Render at 2x supersampling for ultra-crisp antialiasing
    scale = 2
    W = size * scale
    H = size * scale
    cx = W / 2.0
    cy = H / 2.0

    y_grid, x_grid = np.mgrid[0:H, 0:W].astype(np.float32)

    # 1. Background Gradient (Deep midnight navy to cosmic twilight indigo)
    # DarkKiloColors: backgroundTop #0B1020 (11, 16, 32), backgroundBottom #1A1030 (26, 16, 48)
    t_v = y_grid / float(H)
    t_diag = (x_grid * 0.7 + y_grid * 1.0) / float(W * 1.7)
    
    bg_r = 10.0 + (26.0 - 10.0) * t_v + 3.0 * np.sin(t_diag * math.pi)
    bg_g = 15.0 + (16.0 - 15.0) * t_v - 2.0 * t_v
    bg_b = 30.0 + (50.0 - 30.0) * t_v + 5.0 * t_diag

    # Ambient radial backlight / glass caustics behind scale
    # Electric cyan/blue glow centered at upper-mid: (cx, cy - 70*scale)
    dx_glow = x_grid - cx
    dy_glow = y_grid - (cy - 50.0 * scale)
    dist_glow = np.sqrt(dx_glow * dx_glow + dy_glow * dy_glow)
    glow_val = np.exp(-0.5 * (dist_glow / (280.0 * scale)) ** 2)

    # Magenta ambient glow near lower right: (cx + 170*scale, cy + 220*scale)
    dx_m = x_grid - (cx + 180.0 * scale)
    dy_m = y_grid - (cy + 220.0 * scale)
    dist_m = np.sqrt(dx_m * dx_m + dy_m * dy_m)
    glow_m = np.exp(-0.5 * (dist_m / (300.0 * scale)) ** 2)

    bg_r += glow_val * 16.0 + glow_m * 42.0
    bg_g += glow_val * 95.0 + glow_m * 8.0
    bg_b += glow_val * 190.0 + glow_m * 48.0

    img_rgb = np.stack([bg_r, bg_g, bg_b], axis=-1)
    img_rgb = np.clip(img_rgb, 0.0, 255.0)

    master = Image.fromarray(img_rgb.astype(np.uint8), mode='RGB')

    # 2. Main Floating Scale Platform (Liquid Glass Squircle)
    # Dimensions: 740pt in 1024 canvas (1480px in 2048)
    plat_w = 730.0 * scale
    plat_h = 730.0 * scale
    plat_r = 190.0 * scale  # smooth Apple squircle corner radius
    plat_x0 = cx - plat_w / 2.0
    plat_y0 = cy - plat_h / 2.0 + 8.0 * scale  # slight optical center offset
    plat_x1 = plat_x0 + plat_w
    plat_y1 = plat_y0 + plat_h

    # Compute Signed Distance Function (SDF) for the squircle
    p_x = np.abs(x_grid - (plat_x0 + plat_w / 2.0)) - (plat_w / 2.0 - plat_r)
    p_y = np.abs(y_grid - (plat_y0 + plat_h / 2.0)) - (plat_h / 2.0 - plat_r)
    
    qx = np.maximum(p_x, 0.0)
    qy = np.maximum(p_y, 0.0)
    dist_outside = np.sqrt(qx * qx + qy * qy)
    dist_inside = np.minimum(np.maximum(p_x, p_y), 0.0)
    sdf_plat = dist_outside + dist_inside - plat_r

    # Platform mask with anti-aliasing
    plat_mask = np.clip(0.5 - sdf_plat, 0.0, 1.0)

    # Multi-stage Elevation Shadow (deep contact shadow + soft ambient floor blur)
    p_sy = np.abs((y_grid - 32.0 * scale) - (plat_y0 + plat_h / 2.0)) - (plat_h / 2.0 - plat_r)
    qsy = np.maximum(p_sy, 0.0)
    dist_shadow = np.sqrt(qx * qx + qsy * qsy) + np.minimum(np.maximum(p_x, p_sy), 0.0) - plat_r
    shadow_raw = np.clip(1.0 - dist_shadow / (75.0 * scale), 0.0, 1.0)
    shadow_smooth = (shadow_raw ** 2.0) * 0.58 * (1.0 - plat_mask)

    # Contact shadow right underneath bottom edge
    p_cy = np.abs((y_grid - 12.0 * scale) - (plat_y0 + plat_h / 2.0)) - (plat_h / 2.0 - plat_r)
    qcy = np.maximum(p_cy, 0.0)
    dist_contact = np.sqrt(qx * qx + qcy * qcy) + np.minimum(np.maximum(p_x, p_cy), 0.0) - plat_r
    contact_smooth = (np.clip(1.0 - dist_contact / (20.0 * scale), 0.0, 1.0) ** 1.5) * 0.40 * (1.0 - plat_mask)

    total_shadow = np.clip(shadow_smooth + contact_smooth, 0.0, 1.0)

    # Composite shadow onto background
    bg_current = np.array(master, dtype=np.float32)
    for c in range(3):
        bg_current[:, :, c] = bg_current[:, :, c] * (1.0 - total_shadow)
    
    # Glass Body Tint & Optical Reflection
    ny_local = (y_grid - plat_y0) / plat_h
    glass_tint_r = 18.0 + 26.0 * (1.0 - ny_local)
    glass_tint_g = 28.0 + 36.0 * (1.0 - ny_local)
    glass_tint_b = 60.0 + 48.0 * (1.0 - ny_local)

    # Specular diagonal gloss sweep (liquid-glass sheen)
    gloss_coord = (x_grid - plat_x0) * 0.45 + (y_grid - plat_y0) * 0.89
    gloss_intensity = np.clip(1.0 - gloss_coord / (plat_w * 0.95), 0.0, 1.0) ** 1.7
    gloss_val = gloss_intensity * 44.0

    # Bevel normal vectors calculation
    eps = 2.0
    p_x_p = np.abs(x_grid + eps - (plat_x0 + plat_w / 2.0)) - (plat_w / 2.0 - plat_r)
    p_x_m = np.abs(x_grid - eps - (plat_x0 + plat_w / 2.0)) - (plat_w / 2.0 - plat_r)
    p_y_p = np.abs(y_grid + eps - (plat_y0 + plat_h / 2.0)) - (plat_h / 2.0 - plat_r)
    p_y_m = np.abs(y_grid - eps - (plat_y0 + plat_h / 2.0)) - (plat_h / 2.0 - plat_r)
    
    sdf_xp = np.sqrt(np.maximum(p_x_p, 0)**2 + qy**2) + np.minimum(np.maximum(p_x_p, p_y), 0) - plat_r
    sdf_xm = np.sqrt(np.maximum(p_x_m, 0)**2 + qy**2) + np.minimum(np.maximum(p_x_m, p_y), 0) - plat_r
    sdf_yp = np.sqrt(qx**2 + np.maximum(p_y_p, 0)**2) + np.minimum(np.maximum(p_x, p_y_p), 0) - plat_r
    sdf_ym = np.sqrt(qx**2 + np.maximum(p_y_m, 0)**2) + np.minimum(np.maximum(p_x, p_y_m), 0) - plat_r

    norm_x = (sdf_xp - sdf_xm) / (2.0 * eps + 1e-5)
    norm_y = (sdf_yp - sdf_ym) / (2.0 * eps + 1e-5)
    norm_len = np.sqrt(norm_x * norm_x + norm_y * norm_y) + 1e-5
    norm_x /= norm_len
    norm_y /= norm_len

    # Light direction: from upper-left (-0.52, -0.85)
    lx = -0.52
    ly = -0.85
    l_dot_n = -(norm_x * lx + norm_y * ly)

    # Rim highlight along top edge bevel
    bevel_width = 5.5 * scale
    bevel_mask = np.clip(1.0 - np.abs(sdf_plat + bevel_width / 2.0) / (bevel_width / 2.0), 0.0, 1.0)
    rim_highlight = np.maximum(l_dot_n, 0.0) ** 1.6 * bevel_mask * 210.0
    rim_ambient = np.maximum(-l_dot_n, 0.0) * bevel_mask * -45.0

    # Glass transparency blend
    glass_alpha = plat_mask * 0.58

    comp_r = bg_current[:, :, 0] * (1.0 - glass_alpha) + (glass_tint_r + gloss_val) * glass_alpha
    comp_g = bg_current[:, :, 1] * (1.0 - glass_alpha) + (glass_tint_g + gloss_val) * glass_alpha
    comp_b = bg_current[:, :, 2] * (1.0 - glass_alpha) + (glass_tint_b + gloss_val * 1.25) * glass_alpha

    # Apply rim highlight and subtle bottom refractive rim
    comp_r += rim_highlight * plat_mask + rim_ambient * plat_mask
    comp_g += (rim_highlight * 1.06) * plat_mask + rim_ambient * plat_mask
    comp_b += (rim_highlight * 1.18) * plat_mask + rim_ambient * plat_mask

    # Inner groove refraction ring
    groove_dist = np.abs(sdf_plat + 13.0 * scale)
    groove_mask = np.clip(1.0 - groove_dist / (1.8 * scale), 0.0, 1.0)
    groove_light = np.maximum(l_dot_n, 0.0) * groove_mask * 48.0
    comp_r += groove_light * plat_mask
    comp_g += (groove_light * 1.1) * plat_mask
    comp_b += (groove_light * 1.25) * plat_mask

    comp_rgb = np.stack([comp_r, comp_g, comp_b], axis=-1)
    comp_rgb = np.clip(comp_rgb, 0.0, 255.0).astype(np.uint8)

    base_img = Image.fromarray(comp_rgb, mode='RGB')

    # 3. Vector Overlay: Scale Elements
    overlay = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)

    # A) ITO Conductive Glass Sensor Pads
    # Ergonomic footpads on lower half
    pad_w = 280.0 * scale
    pad_h = 245.0 * scale
    pad_r = 60.0 * scale
    pad_y0 = cy + 45.0 * scale
    pad_y1 = pad_y0 + pad_h

    left_x0 = cx - pad_w - 38.0 * scale
    left_x1 = cx - 38.0 * scale
    right_x0 = cx + 38.0 * scale
    right_x1 = cx + pad_w + 38.0 * scale

    for (px0, px1) in [(left_x0, left_x1), (right_x0, right_x1)]:
        # Pad subtle translucent fill
        draw.rounded_rectangle(
            [px0, pad_y0, px1, pad_y1],
            radius=pad_r,
            fill=(255, 255, 255, 14),
            outline=(255, 255, 255, 36),
            width=int(1.8 * scale)
        )
        # Pad inner specular bevel curve (concentric inner stroke)
        draw.rounded_rectangle(
            [px0 + 3.0 * scale, pad_y0 + 3.0 * scale, px1 - 3.0 * scale, pad_y1 - 3.0 * scale],
            radius=max(1, int(pad_r - 3.0 * scale)),
            outline=(255, 255, 255, 22),
            width=int(1.0 * scale)
        )

    # Precision scale center crosshair
    ch_y = cy + 165.0 * scale
    ch_len = 16.0 * scale
    draw.line([cx - ch_len, ch_y, cx + ch_len, ch_y], fill=(255, 255, 255, 45), width=int(1.5 * scale))
    draw.line([cx, ch_y - ch_len, cx, ch_y + ch_len], fill=(255, 255, 255, 45), width=int(1.5 * scale))
    draw.ellipse([cx - 4 * scale, ch_y - 4 * scale, cx + 4 * scale, ch_y + 4 * scale], fill=(10, 132, 255, 180))

    # B) Top Dial: The Precision Weight Gauge & Glowing Indicator
    dial_cy = cy - 145.0 * scale
    dial_r = 165.0 * scale

    # Darker glass inset behind dial
    draw.ellipse(
        [cx - dial_r - 14 * scale, dial_cy - dial_r - 14 * scale, cx + dial_r + 14 * scale, dial_cy + dial_r + 14 * scale],
        fill=(7, 13, 26, 160),
        outline=(255, 255, 255, 32),
        width=int(1.8 * scale)
    )

    # Inset subtle cyan ring
    draw.ellipse(
        [cx - dial_r - 8 * scale, dial_cy - dial_r - 8 * scale, cx + dial_r + 8 * scale, dial_cy + dial_r + 8 * scale],
        outline=(10, 132, 255, 45),
        width=int(1.0 * scale)
    )

    # Gauge track
    track_bbox = [cx - dial_r, dial_cy - dial_r, cx + dial_r, dial_cy + dial_r]
    gauge_width = int(10.0 * scale)

    # Background track (dim arc)
    draw.arc(track_bbox, start=140, end=400, fill=(255, 255, 255, 30), width=gauge_width)

    # Active glowing arc: from 140 to 335 deg
    bloom_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    bloom_draw = ImageDraw.Draw(bloom_layer)

    start_ang = 140
    end_ang = 335
    total_steps = 75
    ang_step = (end_ang - start_ang) / float(total_steps)

    for i in range(total_steps):
        a0 = start_ang + i * ang_step
        a1 = a0 + ang_step + 0.6
        t_arc = i / float(total_steps)
        
        # Color: Neon Cyan (0, 238, 255) -> Azure (10, 132, 255) -> Electric Violet (165, 85, 255)
        if t_arc < 0.6:
            st = t_arc / 0.6
            r_col = int(0 + (10 - 0) * st)
            g_col = int(238 + (132 - 238) * st)
            b_col = int(255 + (255 - 255) * st)
        else:
            st = (t_arc - 0.6) / 0.4
            r_col = int(10 + (165 - 10) * st)
            g_col = int(132 + (85 - 132) * st)
            b_col = int(255 + (255 - 255) * st)

        draw.arc(track_bbox, start=a0, end=a1, fill=(r_col, g_col, b_col, 255), width=gauge_width)
        bloom_draw.arc(track_bbox, start=a0, end=a1, fill=(r_col, g_col, b_col, 210), width=int(gauge_width * 2.2))

    # Glowing indicator bead at end of arc (335 deg)
    rad_end = math.radians(end_ang)
    bead_x = cx + dial_r * math.cos(rad_end)
    bead_y = dial_cy + dial_r * math.sin(rad_end)
    bead_r = 10.5 * scale

    bloom_draw.ellipse(
        [bead_x - bead_r * 2.5, bead_y - bead_r * 2.5, bead_x + bead_r * 2.5, bead_y + bead_r * 2.5],
        fill=(0, 225, 255, 180)
    )
    draw.ellipse(
        [bead_x - bead_r, bead_y - bead_r, bead_x + bead_r, bead_y + bead_r],
        fill=(255, 255, 255, 255)
    )

    # Precision calibration tick marks
    for deg in range(140, 405, 15):
        rad = math.radians(deg)
        cos_a = math.cos(rad)
        sin_a = math.sin(rad)
        is_major = (deg % 30 == 0)
        len_tick = (13.0 if is_major else 7.5) * scale
        r_inner = dial_r - (19.0 * scale)
        r_outer = r_inner - len_tick
        
        x_in = cx + r_inner * cos_a
        y_in = dial_cy + r_inner * sin_a
        x_out = cx + r_outer * cos_a
        y_out = dial_cy + r_outer * sin_a

        tick_alpha = 140 if is_major else 65
        draw.line([x_in, y_in, x_out, y_out], fill=(255, 255, 255, tick_alpha), width=int((2.0 if is_major else 1.2) * scale))

    # Center Dial: Minimalist Dynamic Weight Line & Balance Marker
    # An elegant smooth weight curve / trend line
    curve_points = []
    base_w = 78.0 * scale
    for step in range(-40, 41):
        t_c = step / 40.0  # -1.0 to 1.0
        # Smooth weight curve: starts flat on left, smoothly transitions to dynamic reading point
        x_pt = cx + t_c * base_w
        # Gentle sigmoid / smoothstep curve
        y_pt = dial_cy + 18.0 * scale - 28.0 * scale * (0.5 * (1.0 + math.sin(t_c * math.pi * 0.5)))
        curve_points.append((x_pt, y_pt))

    # Baseline rule
    draw.line([cx - base_w, dial_cy + 22 * scale, cx + base_w, dial_cy + 22 * scale], fill=(255, 255, 255, 45), width=int(1.8 * scale))

    # Smooth weight curve
    for k in range(len(curve_points) - 1):
        p1 = curve_points[k]
        p2 = curve_points[k + 1]
        t_prog = k / float(len(curve_points))
        # Color transition along curve
        cr = int(10 + 200 * t_prog)
        cg = int(140 + 100 * t_prog)
        cb = 255
        draw.line([p1[0], p1[1], p2[0], p2[1]], fill=(cr, cg, cb, 230), width=int(2.6 * scale))
        bloom_draw.line([p1[0], p1[1], p2[0], p2[1]], fill=(cr, cg, cb, 140), width=int(6.0 * scale))

    # Center measurement apex bead
    apex_x, apex_y = curve_points[-1]
    draw.ellipse([apex_x - 5.5 * scale, apex_y - 5.5 * scale, apex_x + 5.5 * scale, apex_y + 5.5 * scale], fill=(255, 255, 255, 255))
    bloom_draw.ellipse([apex_x - 14 * scale, apex_y - 14 * scale, apex_x + 14 * scale, apex_y + 14 * scale], fill=(0, 220, 255, 180))

    # Sleek precision balance center needle
    draw.line([cx, dial_cy + 22 * scale, cx, dial_cy + 5 * scale], fill=(0, 220, 255, 220), width=int(2.2 * scale))

    # Diffuse bloom blur
    bloom_blurred = bloom_layer.filter(ImageFilter.GaussianBlur(radius=8.5 * scale))
    
    # Composite bloom and crisp overlay
    base_rgba = base_img.convert('RGBA')
    base_rgba.alpha_composite(bloom_blurred)
    base_rgba.alpha_composite(overlay)

    # 4. Final Polish: Ensure 100% Opaque RGB (Apple App Store & Play Store requirement)
    final_rgb = base_rgba.convert('RGB')

    # Downsample from 2048 to 1024 with high-quality Lanczos resampling
    final_icon = final_rgb.resize((size, size), Image.Resampling.LANCZOS)
    return final_icon

def generate_all_icons(from_master=False):
    root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    art_dir = os.path.join(root_dir, "art")
    os.makedirs(art_dir, exist_ok=True)
    master_path = os.path.join(art_dir, "icon_1024.png")

    if from_master and os.path.exists(master_path):
        print(f"Loading existing Master Icon from {master_path}...")
        master_1024 = Image.open(master_path).convert("RGB")
    else:
        print("Rendering 1024x1024 Master Icon (icon_1024.png)...")
        master_1024 = create_master_icon(1024)
        master_1024.save(master_path, format="PNG", optimize=True)
        print(f"Saved master icon: {master_path}")

    # Google Play Store Icon (512x512, RGB PNG, opaque)
    print("Generating Google Play Store 512x512 Icon...")
    play_512 = master_1024.resize((512, 512), Image.Resampling.LANCZOS)
    play_path = os.path.join(art_dir, "playstore-icon.png")
    play_512.save(play_path, format="PNG", optimize=True)
    print(f"Saved Play Store icon: {play_path}")

    # iOS App Icon (Single-size universal 1024x1024, strictly NO ALPHA / 24-bit RGB)
    ios_appicon_dir = os.path.join(root_dir, "iosApp", "iosApp", "Assets.xcassets", "AppIcon.appiconset")
    os.makedirs(ios_appicon_dir, exist_ok=True)
    ios_icon_path = os.path.join(ios_appicon_dir, "icon_1024.png")
    ios_icon = master_1024.convert("RGB")
    ios_icon.save(ios_icon_path, format="PNG", optimize=True)
    print(f"Saved iOS icon: {ios_icon_path} (mode={ios_icon.mode}, size={ios_icon.size})")

    # iOS Contents.json
    contents_json = """{
  "images" : [
    {
      "filename" : "icon_1024.png",
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    }
  ],
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
"""
    contents_path = os.path.join(ios_appicon_dir, "Contents.json")
    with open(contents_path, "w", encoding="utf-8") as f:
        f.write(contents_json)
    print(f"Saved iOS Contents.json: {contents_path}")

    # Assets.xcassets root Contents.json
    xcassets_dir = os.path.join(root_dir, "iosApp", "iosApp", "Assets.xcassets")
    xcassets_contents = """{
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
"""
    with open(os.path.join(xcassets_dir, "Contents.json"), "w", encoding="utf-8") as f:
        f.write(xcassets_contents)

    # Android mipmaps
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    res_dir = os.path.join(root_dir, "composeApp", "src", "androidMain", "res")

    for folder, dim in densities.items():
        target_dir = os.path.join(res_dir, folder)
        os.makedirs(target_dir, exist_ok=True)

        # 1. Standard ic_launcher
        launcher_img = master_1024.resize((dim, dim), Image.Resampling.LANCZOS)
        launcher_path = os.path.join(target_dir, "ic_launcher.png")
        launcher_img.save(launcher_path, format="PNG", optimize=True)

        # 2. Round ic_launcher_round
        mask = Image.new('L', (dim * 4, dim * 4), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.ellipse([1, 1, dim * 4 - 2, dim * 4 - 2], fill=255)
        mask_smooth = mask.resize((dim, dim), Image.Resampling.LANCZOS)

        round_img = launcher_img.convert("RGBA")
        round_img.putalpha(mask_smooth)
        round_path = os.path.join(target_dir, "ic_launcher_round.png")
        round_img.save(round_path, format="PNG", optimize=True)

        print(f"Generated Android {folder} ({dim}x{dim}): ic_launcher.png & ic_launcher_round.png")

    print("\nAll icon assets successfully created!")

if __name__ == "__main__":
    import sys
    from_master = "--from-master" in sys.argv
    generate_all_icons(from_master=from_master)
