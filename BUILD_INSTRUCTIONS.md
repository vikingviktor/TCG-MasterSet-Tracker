# Build Instructions for Release

## Prerequisites

Before building a release, ensure you have:
1. JDK 17 or higher installed
2. Android SDK API 34 installed
3. Gradle 8.x or higher
4. Git installed

## Step 1: Prepare Signing Credentials

### Generate Keystore (First Time Only)

```powershell
# Open PowerShell and run:
$env:STORE_PASSWORD = "your_strong_password"
$env:KEY_PASSWORD = "your_strong_password"

keytool -genkey -v `
  -keystore "$env:USERPROFILE\key.jks" `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000 `
  -alias pokemon_key `
  -storepass $env:STORE_PASSWORD `
  -keypass $env:KEY_PASSWORD `
  -dname "CN=Your Name, O=Your Org, L=City, ST=State, C=Country"

# Verify keystore was created
ls "$env:USERPROFILE\key.jks"
```

### Create local.properties

Create `local.properties` in the root directory (DO NOT commit):

```properties
STORE_FILE=C:\Users\your_username\key.jks
STORE_PASSWORD=your_strong_password
KEY_ALIAS=pokemon_key
KEY_PASSWORD=your_strong_password
```

## Step 2: Update Version Information

Edit `gradle.properties`:

```properties
app_version=1.0.0
app_versionCode=1
```

Increment `versionCode` for each release:
- versionCode is an integer (1, 2, 3...)
- versionName is user-visible (1.0.0, 1.0.1...)
- Each release MUST have a higher versionCode
- Google Play requires this for update detection

## Step 3: Build Release Bundle

### Method A: Using Gradle (Recommended)

```powershell
# Set environment variables (PowerShell)
$env:STORE_FILE = "C:\Users\your_username\key.jks"
$env:STORE_PASSWORD = "your_strong_password"
$env:KEY_ALIAS = "pokemon_key"
$env:KEY_PASSWORD = "your_strong_password"

# Build the bundle
./gradlew bundleRelease

# Output location: app/build/outputs/bundle/release/app-release.aab
```

### Method B: Using Android Studio

1. Open project in Android Studio
2. Go to **Build** → **Generate Signed Bundle/APK**
3. Select **Android App Bundle (AAB)**
4. Click **Next**
5. Under "Keystore path", click **...** and select your key.jks
6. Enter passwords:
   - Keystore password: your_strong_password
   - Key password: your_strong_password
7. Select **release** variant
8. Click **Finish**
9. Wait for build to complete (2-5 minutes)
10. Output: `app/release/app-release.aab`

## Step 4: Verify Release Build

```powershell
# Check the bundle was created
ls app/build/outputs/bundle/release/app-release.aab

# Check file size (should be 3-8 MB)
(Get-Item app/build/outputs/bundle/release/app-release.aab).Length / 1MB
```

## Step 5: Test Release Build (Optional)

### Install on connected device:
```powershell
./gradlew installRelease
```

### Or create APK for testing:
```powershell
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

## Step 6: Upload to Google Play

1. Sign in to [Google Play Console](https://play.google.com/console)
2. Select your app
3. Go to **Release** → **Production** (or **Testing** first)
4. Click **Create new release**
5. Upload the `.aab` file
6. Review app details and confirm
7. Submit for review

## Troubleshooting

### Build fails with "Cannot find keystore"
- Verify `local.properties` path is correct
- Ensure file exists: `dir $env:USERPROFILE\key.jks`

### "Invalid keystore format"
- Keystore file may be corrupted
- Regenerate using the keytool command above

### Build is slow
- First build takes longer (downloads dependencies)
- Subsequent builds are faster
- Use `./gradlew bundleRelease --parallel` for parallel builds

### Memory errors
- Increase JVM memory: Add to gradle.properties
  ```
  org.gradle.jvmargs=-Xmx4096m
  ```

### Signing errors
- Verify passwords are correct
- Ensure KEY_ALIAS matches what was generated
- Check gradle.properties has correct properties

## Build Variants

### Debug Build
```powershell
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```powershell
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Release Bundle
```powershell
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

## Release Checklist

Before each release:
- [ ] Version numbers updated (versionCode and versionName)
- [ ] Changelog updated in CHANGELOG.md
- [ ] All tests passing (`./gradlew test`)
- [ ] Lint warnings resolved (`./gradlew lint`)
- [ ] Screenshots updated for store listing
- [ ] Feature descriptions updated
- [ ] Release notes written
- [ ] Tested on multiple Android versions
- [ ] No hardcoded secrets in code
- [ ] All comments updated
- [ ] Code minification enabled (ProGuard)
- [ ] Unused resources removed

## Security Best Practices

⚠️ **IMPORTANT:**
- Never commit keystore files (.jks)
- Never commit local.properties
- Never commit secrets to GitHub
- Use environment variables for sensitive data
- Always use strong passwords (16+ characters)
- Keep backup of keystore file in secure location

## Continuous Integration

See `.github/workflows/build.yml` for automated:
- Building on every push
- Running tests
- Generating reports
- Optional auto-deployment to Play Store

## Performance Tips

### Faster builds:
```powershell
# Enable Gradle daemon and parallel builds
./gradlew --daemon --parallel bundleRelease
```

### Skip tests:
```powershell
./gradlew bundleRelease -x test
```

### Clean build:
```powershell
./gradlew clean bundleRelease
```

## Support

For issues:
1. Check [Troubleshooting](#troubleshooting) above
2. Review [Android Gradle docs](https://developer.android.com/build)
3. Open an issue on GitHub
4. Check CI logs in GitHub Actions

---

**Ready to build?** Run the commands above step by step!
