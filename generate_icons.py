import os
from PIL import Image, ImageDraw

def create_circular_mask(size):
    mask = Image.new('L', size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0) + size, fill=255)
    return mask

def resize_and_save(img, size, path, round_icon=False):
    resized = img.resize(size, Image.Resampling.LANCZOS).convert("RGBA")
    if round_icon:
        mask = create_circular_mask(size)
        resized.putalpha(mask)
    resized.save(path, format="PNG")

source_path = '/tmp/file_attachments/file_00000000c7687207bc7376499cb3c6c6.png'
base_res_path = 'AnPhone001/app/src/main/res'

# Define standard Android launcher icon sizes
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192,
}

# Adaptive icon foreground (108x108 base, multiplied by density)
# For simplicity, we can provide standard PNGs or high-res foregrounds.
# Adaptive icon sizes: mdpi: 108, hdpi: 162, xhdpi: 216, xxhdpi: 324, xxxhdpi: 432
adaptive_sizes = {
    'mipmap-mdpi': 108,
    'mipmap-hdpi': 162,
    'mipmap-xhdpi': 216,
    'mipmap-xxhdpi': 324,
    'mipmap-xxxhdpi': 432,
}

img = Image.open(source_path)

for density, size in sizes.items():
    dir_path = os.path.join(base_res_path, density)
    if not os.path.exists(dir_path):
        os.makedirs(dir_path)

    # Square/normal icon
    path_normal = os.path.join(dir_path, 'ic_launcher.png')
    resize_and_save(img, (size, size), path_normal, round_icon=False)

    # Round icon
    path_round = os.path.join(dir_path, 'ic_launcher_round.png')
    resize_and_save(img, (size, size), path_round, round_icon=True)

# Generate ic_launcher_foreground.png for adaptive icons
for density, size in adaptive_sizes.items():
    dir_path = os.path.join(base_res_path, density)
    path_fg = os.path.join(dir_path, 'ic_launcher_foreground.png')

    # We want the actual logo to be within the "safe zone" of the adaptive icon.
    # The safe zone is a circle with a diameter of 66dp within the 108dp icon.
    # Let's scale the logo down so it fits nicely in the center.
    fg_img = Image.new("RGBA", (size, size), (255, 255, 255, 0)) # transparent background

    # Scale image to safe zone (approx 66/108 of the total size = 61%)
    safe_size = int(size * 0.61)
    scaled_logo = img.resize((safe_size, safe_size), Image.Resampling.LANCZOS).convert("RGBA")

    # Paste the scaled logo into the center of the foreground image
    offset = (size - safe_size) // 2
    fg_img.paste(scaled_logo, (offset, offset))
    fg_img.save(path_fg, format="PNG")

print("Generated all launcher icons.")
