# PowerShell Icon Generator for Android
# Generates all required Android app icon sizes from a source image

param(
    [string]$SourceImage = "C:\Users\victo\Documents\TCG-MasterSet-Tracker\MSTicon.png",
    [string]$OutputDir = "C:\Users\victo\Documents\TCG-MasterSet-Tracker\app\src\main\res"
)

# Load System.Drawing
Add-Type -AssemblyName System.Drawing

# Icon sizes required for Android
$IconSizes = @{
    'ldpi' = 36
    'mdpi' = 48
    'hdpi' = 72
    'xhdpi' = 96
    'xxhdpi' = 144
    'xxxhdpi' = 192
    'anydpi-v33' = 192
}

function Resize-Image {
    param(
        [string]$SourcePath,
        [string]$DestPath,
        [int]$Width,
        [int]$Height
    )
    
    try {
        $image = [System.Drawing.Image]::FromFile($SourcePath)
        $resized = New-Object System.Drawing.Bitmap($Width, $Height)
        $graphics = [System.Drawing.Graphics]::FromImage($resized)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.DrawImage($image, 0, 0, $Width, $Height)
        $graphics.Dispose()
        
        # Create directory if it doesn't exist
        $dir = Split-Path $DestPath
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
        
        $resized.Save($DestPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $resized.Dispose()
        $image.Dispose()
        
        return $true
    }
    catch {
        Write-Error "Error resizing image: $_"
        return $false
    }
}

# Check if source image exists
if (-not (Test-Path $SourceImage)) {
    Write-Error "Source image not found: $SourceImage"
    exit 1
}

Write-Host "Generating Android app icons..." -ForegroundColor Green
Write-Host "Source: $SourceImage`n"

# Generate all icon sizes
$success = $true
foreach ($density in $IconSizes.Keys) {
    $size = $IconSizes[$density]
    $mipmapDir = Join-Path $OutputDir "mipmap-$density"
    $outputPath = Join-Path $mipmapDir "ic_launcher.png"
    
    if (Resize-Image -SourcePath $SourceImage -DestPath $outputPath -Width $size -Height $size) {
        Write-Host "✓ Generated $density`: ${size}x${size}" -ForegroundColor Green
    } else {
        Write-Host "✗ Failed to generate $density" -ForegroundColor Red
        $success = $false
    }
}

if ($success) {
    Write-Host "`n✓ All icon sizes generated successfully!" -ForegroundColor Green
    Write-Host "Icons are now in: $OutputDir`n" -ForegroundColor Green
} else {
    Write-Host "`n✗ Some icons failed to generate" -ForegroundColor Red
    exit 1
}
