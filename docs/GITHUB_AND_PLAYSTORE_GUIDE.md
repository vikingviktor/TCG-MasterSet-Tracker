# GitHub & Play Store Publishing Guide

## Part 1: Prepare for GitHub Upload

### Step 1: Create GitHub Repository

1. Go to https://github.com/new
2. Repository name: `TCG-MasterSet-Tracker` (or your preferred name)
3. Description: `A comprehensive Android app for tracking Pokémon Trading Card Game collections`
4. Choose: Public (recommended for open source)
5. Click "Create repository"

### Step 2: Initialize Git Locally

```bash
cd C:\Users\victo\Documents\TCG-MasterSet-Tracker

# Initialize git
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit: Pokemon Master Set Tracker Android App"

# Add remote (replace with your repo URL)
git remote add origin https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker.git

# Rename branch to main (if needed)
git branch -M main

# Push to GitHub
git push -u origin main
```

### Step 3: Update .gitignore (Already Done ✓)

The `.gitignore` file is already configured to exclude:
- Build artifacts
- IDE configurations
- Gradle cache
- Local properties
- APK/AAR files

This is perfect for Git!

---

## Part 2: Configure Signing for Production Build

### Step 1: Create a Keystore File

You need a signing keystore to sign the .aab file. Run this command:

```bash
# On Windows PowerShell:
$JDK = "C:\Program Files\Android\Android Studio\jbr\bin"
& "$JDK\keytool.exe" -genkey -v -keystore C:\Users\victo\key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias pokemon_key

# Answer the prompts:
# - Password: Create a STRONG password (save it!)
# - First and Last Name: Your name or company
# - Organizational Unit: Your company unit
# - Organization: Your company name
# - City: Your city
# - State: Your state
# - Country Code: US (or your country code)
# - Confirm: yes
```

**IMPORTANT:** Save this keystore file and password securely!
- File: `C:\Users\victo\key.jks`
- Password: Write it down in a safe place

### Step 2: Add Local Properties File

Create a `local.properties` file in the project root:

```properties
sdk.dir=C:\\Users\\victo\\AppData\\Local\\Android\\Sdk
storeFile=C:\\Users\\victo\\key.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=pokemon_key
keyPassword=YOUR_KEYSTORE_PASSWORD
```

**WARNING:** This file contains sensitive info - it's in `.gitignore` and won't be uploaded!

### Step 3: Update Build Configuration

Add signing configuration to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("STORE_FILE") ?: "key.jks")
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

## Part 3: Build .aab File for Play Store

### Method 1: Using Android Studio (Recommended)

1. Open the project in Android Studio
2. Go to **Build** → **Generate Signed Bundle/APK**
3. Select **Android App Bundle** (not APK)
4. Click **Next**
5. Select **Create new...** or use existing keystore
6. Fill in keystore details:
   - Keystore path: `C:\Users\victo\key.jks`
   - Keystore password: Your password
   - Key alias: `pokemon_key`
   - Key password: Your password
7. Click **Next**
8. Select **Release** variant
9. Click **Finish**

The .aab file will be created at:
```
app/release/app-release.aab
```

### Method 2: Using Gradle Command

```bash
cd C:\Users\victo\Documents\TCG-MasterSet-Tracker

# Set environment variables
$env:STORE_FILE = "C:\Users\victo\key.jks"
$env:STORE_PASSWORD = "your_keystore_password"
$env:KEY_ALIAS = "pokemon_key"
$env:KEY_PASSWORD = "your_keystore_password"

# Build the bundle
./gradlew bundleRelease

# Output will be at: app/release/app-release.aab
```

---

## Part 4: Update Version & App Details

Before publishing, update your app version:

### Update `app/build.gradle.kts`:
```kotlin
defaultConfig {
    applicationId = "com.example.pokemonmastersettracker"
    minSdk = 24
    targetSdk = 34
    versionCode = 1          // Increment this with each release
    versionName = "1.0.0"    // Semantic versioning
}
```

### Update `AndroidManifest.xml`:
```xml
<manifest>
    <!-- Add if not present -->
    <uses-feature android:name="android.hardware.camera" android:required="false" />
</manifest>
```

### Create App Store Listing Files

Create these files in a new `fastlane/` directory:

#### `fastlane/metadata/android/en-US/title.txt`
```
Pokemon Master Set Tracker
```

#### `fastlane/metadata/android/en-US/short_description.txt`
```
Track your Pokemon TCG collection and view completion percentages
```

#### `fastlane/metadata/android/en-US/full_description.txt`
```
Pokemon Master Set Tracker is your comprehensive tool for managing your Pokemon Trading Card Game collection.

FEATURES:
• Search through thousands of Pokemon cards
• Filter by English and Japanese sets
• Track cards you own vs. cards you're missing
• View collection completion percentage
• Save favorite Pokemon for quick access
• Track card condition and grading
• View card prices and market values
• Manage multiple collections

Whether you're a casual collector or serious enthusiast, Pokemon Master Set Tracker helps you organize and complete your Pokemon TCG master sets with ease.

Built with modern Android technologies including Jetpack Compose, Room Database, and the PokemonTCG.io API.

This is an open-source project. Visit our GitHub for more information and to contribute!
```

---

## Part 5: Prepare for Google Play Store

### Step 1: Create Google Play Developer Account

1. Go to https://play.google.com/console
2. Create a Google Play Developer account ($25 USD one-time fee)
3. Complete all required information:
   - Developer name
   - Email
   - Payment method
   - Address

### Step 2: Create New App

1. In Play Console, click **Create app**
2. App name: `Pokemon Master Set Tracker`
3. Default language: English
4. App category: Games or Utility (depending on positioning)
5. ESRB rating: Low maturity (card game, no violence)
6. Click **Create app**

### Step 3: Fill Out Store Listing

In Play Console, complete these sections:

**App Details:**
- Title: Pokemon Master Set Tracker
- Short description: (from fastlane above)
- Full description: (from fastlane above)

**Category:**
- Games > Card > Trading Card Games

**Contact Details:**
- Add your email and website (if available)

**Privacy Policy:**
- Create a simple privacy policy (required)
- Can use Google's privacy policy generator

### Step 4: Add Screenshots & Graphics

Required assets:
- **App icon:** 512x512 PNG (no rounded corners)
- **Feature graphic:** 1024x500 PNG
- **Screenshots:** 
  - Minimum 2, maximum 8
  - 1440x2560 PNG or JPG
  - Show main features

### Step 5: Content Rating Questionnaire

Complete the IARC questionnaire:
- Violence: No
- Sexual content: No
- Inappropriate language: No
- Substance use: No
- Gambling: No

This will result in an "All Ages" or "Low maturity" rating.

### Step 6: Set Up Pricing & Distribution

**Pricing:**
- Free (recommended for MVP)
- Countries: All countries (or select specific ones)

**Content Rating:**
- Select appropriate rating

**Consent:**
- Agree to Google Play policies

---

## Part 6: Upload to Play Store

### Step 1: Prepare Release

1. In Play Console, go to **Release** → **Production**
2. Click **Create new release**
3. Click **Browse files** and select your `app-release.aab`
4. Wait for upload and validation

### Step 2: Review Details

- Check all information is correct
- Review privacy policy
- Confirm app requirements

### Step 3: Submit for Review

1. Click **Review release**
2. Confirm all details
3. Click **Start rollout to Production**
4. Google will review (usually 1-3 hours)

### Step 4: Monitor Review

- Check Play Console for review status
- Usually approved within 24 hours
- May require changes if policy violations

---

## Part 7: GitHub Repository Setup

### Create README.md Badge

Add this to your GitHub README:

```markdown
<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.example.pokemonmastersettracker">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" width="200">
  </a>
</p>
```

### GitHub Repository Structure

```
TCG-MasterSet-Tracker/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md
│   │   └── feature_request.md
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── workflows/
│       ├── build.yml (CI/CD)
│       └── release.yml
├── app/
│   ├── src/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── .gitignore ✓
├── README.md ✓
├── LICENSE (add MIT license)
└── gradle/
```

### Create LICENSE File

Create `LICENSE` in root directory:

```
MIT License

Copyright (c) 2025 [Your Name/Company]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished in copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Create CONTRIBUTING.md

```markdown
# Contributing to Pokemon Master Set Tracker

We welcome contributions! Here's how to help:

## Getting Started

1. Fork the repository
2. Clone your fork
3. Create a feature branch: `git checkout -b feature/amazing-feature`
4. Make your changes
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open a Pull Request

## Code Style

- Follow Kotlin conventions
- Use MVVM pattern
- Add comments for complex logic
- Include tests when possible

## Pull Request Process

1. Update README if needed
2. Test your changes
3. Ensure no new build errors
4. Add description of changes

## Reporting Issues

- Use GitHub Issues
- Provide reproducible steps
- Include device/Android version
- Attach screenshots if applicable
```

---

## Complete Checklist

- [ ] Create GitHub account (if needed)
- [ ] Create GitHub repository
- [ ] Initialize Git locally and push code
- [ ] Generate keystore file
- [ ] Create local.properties with signing config
- [ ] Update app version in build.gradle
- [ ] Update app metadata (icon, screenshots)
- [ ] Create Play Developer account
- [ ] Create app in Play Console
- [ ] Complete store listing
- [ ] Complete content rating questionnaire
- [ ] Set pricing and distribution
- [ ] Build .aab file
- [ ] Upload .aab to Play Console
- [ ] Submit for review
- [ ] Monitor review status
- [ ] App goes live!

---

## Important Notes

### Security

⚠️ **NEVER commit these files to Git:**
- `key.jks` (keystore)
- `local.properties` (signing credentials)
- `google-play-key.json` (if using)

They're in `.gitignore` for a reason!

### Versioning

Follow semantic versioning:
- Major.Minor.Patch
- Example: 1.0.0 → 1.0.1 (patch fix) → 1.1.0 (feature) → 2.0.0 (breaking change)
- Increment versionCode for each release

### Testing Before Publish

1. Test on multiple devices
2. Test on minimum Android version (API 24)
3. Test on maximum Android version
4. Test all features
5. Check for crashes
6. Verify permissions
7. Test offline mode

---

## Post-Publish

Once live on Play Store:

1. Monitor reviews and ratings
2. Fix reported bugs quickly
3. Respond to user feedback
4. Plan feature updates
5. Release updates regularly
6. Monitor crash reports via Firebase Crashlytics (optional)

---

**Good luck publishing your app!** 🚀
