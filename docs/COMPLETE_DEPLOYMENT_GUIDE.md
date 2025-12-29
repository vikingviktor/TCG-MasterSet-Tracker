# Complete Deployment & Publishing Guide

## Table of Contents
1. [GitHub Setup](#github-setup)
2. [Signing Configuration](#signing-configuration)
3. [Building Release Bundle](#building-release-bundle)
4. [Google Play Console Setup](#google-play-console-setup)
5. [Store Listing Creation](#store-listing-creation)
6. [Upload & Submission](#upload--submission)
7. [Post-Launch Monitoring](#post-launch-monitoring)

---

## GitHub Setup

### 1. Create Repository

```powershell
# Verify you're in project directory
cd "C:\Users\victo\Documents\TCG-MasterSet-Tracker"

# Initialize Git
git init

# Configure Git (one time)
git config --global user.name "Your Name"
git config --global user.email "your@email.com"

# Add GitHub remote
git remote add origin https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker.git

# Stage all files
git add .

# Initial commit
git commit -m "Initial commit: Pokemon Master Set Tracker with MVVM architecture, Jetpack Compose UI, Room database, Retrofit API integration"

# Create main branch and push
git branch -M main
git push -u origin main
```

### 2. Configure Repository (Web)

Visit: `https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker`

1. **Settings** → **General**
   - Add description: "Track your Pokemon TCG card collection"
   - Add topics: pokemon, tcg, android, kotlin

2. **Settings** → **Branches**
   - Set default branch to `main`

3. Verify **Actions** tab shows successful build

---

## Signing Configuration

### 1. Generate Keystore

```powershell
# Set variables
$env:STORE_PASSWORD = "YourStrongPassword123!"
$env:KEY_PASSWORD = "YourStrongPassword123!"

# Generate keystore
keytool -genkey -v `
  -keystore "$env:USERPROFILE\key.jks" `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000 `
  -alias pokemon_key `
  -storepass $env:STORE_PASSWORD `
  -keypass $env:KEY_PASSWORD `
  -dname "CN=Your Name, O=Your Company, L=City, ST=State, C=Country"

# Verify keystore exists
ls "$env:USERPROFILE\key.jks"

# Get keystore details
keytool -list -v -keystore "$env:USERPROFILE\key.jks" `
  -storepass $env:STORE_PASSWORD
```

**IMPORTANT NOTES:**
- ⚠️ Keep this password safe - you'll need it for all future releases
- ⚠️ Don't lose the keystore file - generate a backup
- ✅ Use a strong password (16+ characters, mixed case, numbers, special chars)
- ✅ The same keystore MUST be used for all future updates
- ✅ Valid for 10,000 days (~27 years)

### 2. Create local.properties

Create `local.properties` in project root (DO NOT commit):

```properties
STORE_FILE=C:\Users\your_username\key.jks
STORE_PASSWORD=YourStrongPassword123!
KEY_ALIAS=pokemon_key
KEY_PASSWORD=YourStrongPassword123!
```

Verify in `.gitignore`:
```
local.properties
*.jks
*.keystore
```

### 3. Verify Build Configuration

Check `app/build.gradle.kts` has:

```kotlin
android {
    // ... other configs ...
    
    signingConfigs {
        release {
            storeFile = file(System.getenv("STORE_FILE") ?: "${System.getProperty("user.home")}/key.jks")
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS") ?: "pokemon_key"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.release
        }
    }
    
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}
```

---

## Building Release Bundle

### Option A: Gradle Command Line (Recommended)

```powershell
# Set environment variables
$env:STORE_FILE = "C:\Users\your_username\key.jks"
$env:STORE_PASSWORD = "YourStrongPassword123!"
$env:KEY_ALIAS = "pokemon_key"
$env:KEY_PASSWORD = "YourStrongPassword123!"

# Build release bundle
./gradlew bundleRelease

# Expected output location
ls app/build/outputs/bundle/release/app-release.aab

# Check file size
(Get-Item app/build/outputs/bundle/release/app-release.aab).Length / 1MB
```

**Expected:**
- Build time: 2-5 minutes
- File size: 4-8 MB
- No errors or warnings

### Option B: Android Studio GUI

1. **Build** → **Generate Signed Bundle/APK**
2. Select **Android App Bundle**
3. **Next**
4. Keystore path: Browse to `C:\Users\your_username\key.jks`
5. Keystore password: `YourStrongPassword123!`
6. Key alias: `pokemon_key`
7. Key password: `YourStrongPassword123!`
8. Destination: `app/release/`
9. **Finish**

### Troubleshoot Build Failures

```powershell
# Clean build
./gradlew clean bundleRelease

# With more output
./gradlew bundleRelease --stacktrace

# Check Gradle version
./gradlew --version

# Update Gradle wrapper
./gradlew wrapper --gradle-version=8.1.1
```

---

## Google Play Console Setup

### 1. Create Google Play Developer Account

1. Visit [Google Play Console](https://play.google.com/console)
2. Click **Sign in** with Google account
3. If first time, click **Create a Google Play Developer Account**
4. Accept terms and pay **$25 USD** (one-time)
5. Complete profile information

### 2. Create App in Play Console

1. Login to [Google Play Console](https://play.google.com/console)
2. Click **Create app**
3. **App name:** `Pokemon Master Set Tracker`
4. **Default language:** English
5. **App or game:** App
6. **Free or paid:** Free
7. **Declarations:** Accept/check all as appropriate
8. Click **Create app**

### 3. App Details

Navigate to **Setup** → **App details**

Fill in:
- **App name:** Pokemon Master Set Tracker (50 chars max)
- **Short description:** Track and manage your Pokemon TCG collection (80 chars)
- **Full description:** (below)
- **App category:** Games
- **App type:** Game (or Lifestyle)
- **Contact email:** your@email.com
- **Privacy policy:** https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker/blob/main/PRIVACY_POLICY.md

**Full Description Template:**
```
Pokemon Master Set Tracker is the ultimate app for managing your Pokemon 
Trading Card Game collection!

Features:
✓ Search 1000+ Pokemon cards by name and set
✓ Track cards in English and Japanese sets
✓ Mark cards as owned with condition ratings
✓ View collection completion percentage
✓ Save favorite Pokemon for quick access
✓ Check real-time card prices from TCGPlayer
✓ Works offline with cached card data
✓ Beautiful Material Design 3 interface

Perfect for:
- Collectors tracking their complete sets
- Players managing tournament decks
- Investors monitoring card values
- Anyone passionate about Pokemon TCG

Built with modern Android technologies including Jetpack Compose, 
Room Database, and the official PokemonTCG.io API.

No ads. No tracking. Just pure card management.

Data from: pokemontcg.io
```

---

## Store Listing Creation

### 1. Add Screenshots

Go to **Store presence** → **Screenshots**

Prepare 5-8 screenshots at **1440x2560** pixels (or higher):

**Screenshot 1:** Home Screen
- Show card search interface
- Add text overlay: "Search 1000+ Pokemon Cards"

**Screenshot 2:** Card Detail
- Show individual card view
- Add text overlay: "View Detailed Card Information"

**Screenshot 3:** Collection Screen
- Show collection with stats
- Add text overlay: "Track Your Collection Completion"

**Screenshot 4:** Favorites Screen
- Show favorite Pokemon
- Add text overlay: "Save Your Favorite Pokemon"

**Screenshot 5:** Multi-language Support
- Show EN/JA toggle
- Add text overlay: "English & Japanese Cards"

### 2. Feature Graphic

Upload to **Store presence** → **Featured graphic**
- Size: **1024x500** pixels
- Shows app branding/key feature
- PNG or JPEG

### 3. Icon & Graphics

Prepare icon:
- Size: **512x512** pixels
- PNG format
- No transparency issues
- Clear and recognizable

### 4. Content Rating

Go to **Setup** → **App content**

1. Click **Content rating questionnaire**
2. Complete IARC form:
   - App category: Games or Lifestyle
   - Content descriptions: Select appropriate
   - Targeted age: All Ages (for Pokemon)
   - Marketing: Not targeted to children
3. Submit form
4. Get rating: Should be "Approved for All Ages"

### 5. Privacy Policy

Create `PRIVACY_POLICY.md` in repository:

```markdown
# Privacy Policy

## Data Collection
This app does not collect personal data.

## Local Storage
- Card data is cached locally on your device
- Collection data stored in local database
- No data sent to external servers

## Permissions
- INTERNET: For API calls to pokemontcg.io
- ACCESS_NETWORK_STATE: To check connection

## Third-Party Services
- PokemonTCG.io API: Public API for card data
- No Google Analytics
- No tracking libraries
- No ads

## Contact
For privacy concerns: your@email.com
```

### 6. Pricing & Distribution

Go to **Setup** → **Pricing & distribution**

- **Price:** Free
- **Countries:** All except restricted
- **Content rating:** Approved for All Ages
- **Restricted content:** None
- **Google Play for Work:** Yes
- **Categories:** Games
- **Consent:** Check "Designed for Families" (if appropriate)

---

## Upload & Submission

### 1. Upload App Bundle

1. Go to **Release** → **Production** (or **Internal testing** first)
2. Click **Create new release**
3. Click **Browse files**
4. Select `app/build/outputs/bundle/release/app-release.aab`
5. Wait for upload (1-2 minutes)
6. Review warnings (if any)

### 2. Review Release Info

- **Release name:** v1.0.0 or leave default
- **Release notes:** Copy from CHANGELOG.md
  ```
  Initial Release Features:
  ✓ Card search and browse
  ✓ Collection management with stats
  ✓ Favorites system
  ✓ Price information
  ✓ Offline card browsing
  ✓ Material Design 3 UI
  ✓ English & Japanese support
  ```

### 3. Review App Details

Check all sections are complete:
- ✅ App icon
- ✅ Screenshots (5-8)
- ✅ Feature graphic
- ✅ Short description
- ✅ Full description
- ✅ Content rating
- ✅ Privacy policy
- ✅ Contact email
- ✅ Support email

### 4. Rollout Configuration

For first release, recommend:
- **Rollout percentage:** 5% initially
- This lets you monitor crash reports
- Increase to 25%, then 100% as it proves stable

```
Day 1: 5% rollout → Monitor crash reports
Day 2-3: 25% rollout → Check user reviews
Day 4+: 100% rollout → Full release
```

### 5. Submit for Review

1. Click **Review**
2. Check all information is correct
3. Click **Submit** (or **Save** to review before submit)
4. You'll see review status

**Expected Review Time:** 2-24 hours

---

## Post-Launch Monitoring

### 1. Monitor Submissions

In Play Console:
- Check **Release dashboard** for status
- Watch **Crash reports** in real-time
- Monitor **ANR (Application Not Responding)** rate
- Review **User feedback** and ratings

### 2. Monitor Metrics

Check daily:
- **Installs**: Number of active users
- **Crashes**: Any crash spikes?
- **Ratings**: Average app rating
- **Reviews**: User feedback

### 3. Respond to Issues

If crashes detected:
1. Check crash logs in Play Console
2. Fix the issue
3. Increment versionCode
4. Build new .aab
5. Submit as "Critical fix"

### 4. Update App Regularly

Plan regular updates:
- Bug fixes: As needed
- Features: Monthly or quarterly
- Store listing: Keep descriptions fresh
- Screenshots: Update if UI changes

---

## Rollout Strategy

### Version 1.0.0 (Initial Release)

```
Day 1:  5% rollout
        ↓
        Monitor crashes for 6-8 hours
        ↓
Day 2:  25% rollout
        ↓
        Monitor for 24 hours
        ↓
Day 3+: 100% rollout
        ↓
        Full launch
```

### Version 1.0.1 (Bug Fix)

```
Immediate: 10% rollout
           ↓
           Monitor for 2 hours
           ↓
           50% rollout (if no issues)
           ↓
           100% rollout
```

### Version 1.1.0 (Feature Release)

```
Immediate: 5% rollout
           ↓
           Full monitoring (12+ hours)
           ↓
           25% rollout
           ↓
           100% rollout
```

---

## Monitoring Checklist

Daily for first week:
- [ ] Check crash reports
- [ ] Read user reviews
- [ ] Monitor ratings
- [ ] Check ANR rate
- [ ] Review error logs

Weekly ongoing:
- [ ] Review user feedback
- [ ] Check crash trends
- [ ] Monitor app statistics
- [ ] Plan next update

---

## Troubleshooting

### App Rejected by Google Play

**Reason: Crashes**
- Check Play Console crash reports
- Fix issues locally
- Test thoroughly
- Increment versionCode
- Resubmit

**Reason: Policy Violation**
- Review Play Policies: https://play.google.com/about/developer-content-policy/
- Remove violating content
- Update store listing
- Resubmit

**Reason: Incomplete Store Listing**
- Complete content rating
- Add privacy policy
- Add screenshots
- Add app description

### Build Failures

See `BUILD_INSTRUCTIONS.md` troubleshooting section

### Performance Issues Post-Launch

1. Check crash reports for patterns
2. Monitor memory usage
3. Check database queries
4. Review API calls
5. Fix and resubmit update

---

## Success Checklist

Before launching:
- [ ] Repository on GitHub
- [ ] All tests passing
- [ ] Release .aab built and tested
- [ ] Version numbers updated
- [ ] CHANGELOG.md updated
- [ ] Screenshots prepared
- [ ] Store listing complete
- [ ] Privacy policy written
- [ ] Content rating approved
- [ ] No hardcoded secrets

After launching:
- [ ] App visible in Play Store
- [ ] Download link working
- [ ] Installs increasing
- [ ] Crash reports monitored
- [ ] User reviews monitored
- [ ] Update plan created

---

## After Launch

1. **Monitor closely** for first week
2. **Respond to reviews** professionally
3. **Plan next version** with user feedback
4. **Continue development** with improvements
5. **Build community** through GitHub

---

**Congratulations!** Your app is now on the Google Play Store! 🎉

For updates and improvements, increment versionCode and resubmit.
