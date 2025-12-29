#!/usr/bin/env python3
"""
Android Icon Generator
Generates all required Android app icon sizes from a source image
"""

from PIL import Image
import os
import sys

# Icon sizes required for Android (in density-independent pixels)
# Format: (folder_suffix, size_in_pixels)
ICON_SIZES = {
    'ldpi': 36,
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192,
    'anydpi-v33': 192  # Adaptive icon
}

def generate_icons(source_image_path, output_base_dir):
    """Generate all required icon sizes from source image"""
    
    # Open the source image
    try:
        img = Image.open(source_image_path)
        print(f"Opened image: {source_image_path}")
        print(f"Original size: {img.size}")
    except Exception as e:
        print(f"Error opening image: {e}")
        return False
    
    # Create output directories and generate icons
    for density, size in ICON_SIZES.items():
        # Create mipmap directory
        mipmap_dir = os.path.join(output_base_dir, f'mipmap-{density}')
        os.makedirs(mipmap_dir, exist_ok=True)
        
        # Resize image
        resized_img = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save icon
        output_path = os.path.join(mipmap_dir, 'ic_launcher.png')
        resized_img.save(output_path, 'PNG', quality=95)
        print(f"✓ Generated {density}: {size}x{size} -> {output_path}")
    
    print("\n✓ All icon sizes generated successfully!")
    return True

if __name__ == '__main__':
    source_image = r'C:\Users\victo\Documents\TCG-MasterSet-Tracker\MSTicon.png'
    output_dir = r'C:\Users\victo\Documents\TCG-MasterSet-Tracker\app\src\main\res'
    
    if not os.path.exists(source_image):
        print(f"Error: Source image not found: {source_image}")
        sys.exit(1)
    
    success = generate_icons(source_image, output_dir)
    sys.exit(0 if success else 1)
