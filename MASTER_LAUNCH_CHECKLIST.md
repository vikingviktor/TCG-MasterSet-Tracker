# Pokemon Master Set Tracker - Master Launch Checklist

## 🎯 Complete Preparation Checklist for GitHub & Google Play Store

Use this master checklist to ensure you're ready to publish. Work through sections in order.

---

## ✅ PHASE 1: PREPARATION (30 minutes)

### Read Documentation
- [ ] Read `READY_FOR_LAUNCH.md` (15 min)
- [ ] Understand the 6-step quick start process
- [ ] Review your keystore password requirements
- [ ] Ensure you have a Google account for Play Store

### Verify Code is Complete
- [ ] All 23 source files exist in app/src/main/kotlin/
- [ ] No syntax errors in project
- [ ] Tests pass (if applicable)
- [ ] Build configuration is correct

### Check Credentials Ready
- [ ] Have a GitHub username
- [ ] Have a Google account
- [ ] Have an email for signing keystore
- [ ] Have a strong password (16+ chars, mixed case, numbers, special)

---

## ✅ PHASE 2: GITHUB SETUP (15 minutes)

### Create Repository
- [ ] Go to https://github.com/new
- [ ] Create repo named `TCG-MasterSet-Tracker`
- [ ] Public visibility
- [ ] Note the HTTPS URL from step 4 below

### Initialize Local Git
```powershell
cd "C:\Users\victo\Documents\TCG-MasterSet-Tracker"
git init
git config --global user.name "Your Name"
git config --global user.email "your@email.com"
git remote add origin https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker.git
```

**Checklist:**
- [ ] Git initialized in project directory
- [ ] Remote URL set correctly (replace YOUR_USERNAME)
- [ ] Test: `git remote -v` shows origin

### Commit & Push Code
```powershell
git add .
git commit -m "Initial commit: Pokemon Master Set Tracker"
git branch -M main
git push -u origin main
```

**Checklist:**
- [ ] All files staged with `git add .`
- [ ] Initial commit created
- [ ] Code pushed to GitHub
- [ ] Can see repository at https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker

### Configure GitHub Settings (on github.com)
- [ ] Go to Settings → General
- [ ] Add repository description
- [ ] Add topics: pokemon, tcg, android, kotlin
- [ ] Set default branch to main
- [ ] Verify GitHub Actions workflow is configured

---

## ✅ PHASE 3: SIGNING SETUP (10 minutes)

### Generate Keystore Certificate

```powershell
# Open PowerShell in ANY directory and run:
$env:STORE_PASSWORD = "YourStrongPassword123!"
$env:KEY_PASSWORD = "YourStrongPassword123!"

keytool -genkey -v `
  -keystore "$env:USERPROFILE\key.jks" `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000 `
  -alias pokemon_key `
  -storepass $env:STORE_PASSWORD `
  -keypass $env:KEY_PASSWORD `
  -dname "CN=Your Name, O=Company, L=City, ST=State, C=Country"

# Verify it worked:
ls "$env:USERPROFILE\key.jks"
```

**Checklist:**
- [ ] Keystore file exists at `C:\Users\your_username\key.jks`
- [ ] Passwords are written down in secure location
- [ ] File size is reasonable (2-3 KB)

### Create local.properties

In project root directory, create file named `local.properties`:

```properties
STORE_FILE=C:\Users\your_username\key.jks
STORE_PASSWORD=YourStrongPassword123!
KEY_ALIAS=pokemon_key
KEY_PASSWORD=YourStrongPassword123!
```

**Checklist:**
- [ ] File created in project root
- [ ] Paths match your system (replace `your_username`)
- [ ] File is NOT committed to Git (check .gitignore)
- [ ] Verified in .gitignore: `local.properties` is excluded

### Backup Keystore
- [ ] Copy `key.jks` to external backup drive
- [ ] Store password securely (password manager recommended)
- [ ] Document the keystore info for future reference

**CRITICAL:** Without this keystore, you cannot update your app on Play Store. Keep secure backups!

---

## ✅ PHASE 4: BUILD RELEASE BUNDLE (10 minutes)

### Set Environment Variables
```powershell
# Open new PowerShell window and run:
$env:STORE_FILE = "C:\Users\your_username\key.jks"
$env:STORE_PASSWORD = "YourStrongPassword123!"
$env:KEY_ALIAS = "pokemon_key"
$env:KEY_PASSWORD = "YourStrongPassword123!"
```

**Checklist:**
- [ ] Environment variables set in PowerShell
- [ ] All 4 variables have correct values

### Build Bundle
```powershell
# In same PowerShell window:
cd "C:\Users\victo\Documents\TCG-MasterSet-Tracker"
./gradlew bundleRelease
```

**Checklist:**
- [ ] Build starts without errors
- [ ] Build completes (takes 2-5 minutes)
- [ ] No failures in output
- [ ] Sees "BUILD SUCCESSFUL"

### Verify Bundle Created
```powershell
# Verify file exists and check size
ls app/build/outputs/bundle/release/app-release.aab
(Get-Item app/build/outputs/bundle/release/app-release.aab).Length / 1MB
```

**Checklist:**
- [ ] File exists: `app/build/outputs/bundle/release/app-release.aab`
- [ ] File size is 4-8 MB (reasonable size)
- [ ] File is signed (no signature errors)

---

## ✅ PHASE 5: GOOGLE PLAY SETUP (20 minutes)

### Create Google Play Developer Account

1. Go to https://play.google.com/console
2. Sign in with Google account
3. Create Developer Account
4. Pay $25 USD (one-time)
5. Complete profile information

**Checklist:**
- [ ] Google Play Developer account created
- [ ] $25 payment processed
- [ ] Account fully set up and verified

### Create App in Play Console

1. Click **Create app**
2. App name: `Pokemon Master Set Tracker`
3. Default language: English
4. App type: App
5. Free app: Yes
6. Accept declarations
7. Click **Create app**

**Checklist:**
- [ ] App created in Play Console
- [ ] App name matches project
- [ ] App is set as Free
- [ ] Can see app in dashboard

### Complete App Details

Go to **Setup** → **App details**

**Checklist:**
- [ ] App name entered (50 chars max)
- [ ] Short description entered (80 chars max)
- [ ] Full description entered (use template from guide)
- [ ] Category selected
- [ ] Contact email provided
- [ ] Privacy policy link ready (from GitHub README)

---

## ✅ PHASE 6: STORE LISTING (25 minutes)

### Prepare Screenshots

Create 5-8 screenshots at **1440x2560** resolution:
- [ ] Screenshot 1: Home/Search screen
- [ ] Screenshot 2: Card detail view
- [ ] Screenshot 3: Collection screen
- [ ] Screenshot 4: Favorites screen
- [ ] Screenshot 5: Multi-language support

**Checklist:**
- [ ] Screenshots are 1440x2560 pixels minimum
- [ ] Screenshots show key features
- [ ] Text overlays are clear and readable
- [ ] Screenshots are in PNG or JPEG format

### Upload Screenshots

Go to **Store presence** → **Screenshots**

**Checklist:**
- [ ] All screenshots uploaded
- [ ] Appear in correct order
- [ ] Display properly in preview

### Add Icon & Graphics

**Checklist:**
- [ ] App icon uploaded (512x512 PNG)
- [ ] Feature graphic ready (1024x500 PNG)
- [ ] Promo graphic ready (180x120 PNG, if desired)

### Complete Content Rating

Go to **Setup** → **App content**

**Checklist:**
- [ ] Content rating questionnaire completed
- [ ] Got approval (should be "All Ages")
- [ ] Age target appropriate
- [ ] Content descriptions accurate

---

## ✅ PHASE 7: UPLOAD & SUBMIT (10 minutes)

### Prepare for Upload

**Checklist:**
- [ ] Release bundle file ready: `app-release.aab`
- [ ] Version number verified in gradle.properties
- [ ] Version code: 1 (for first release)
- [ ] Version name: 1.0.0
- [ ] CHANGELOG.md updated with release notes

### Upload Bundle

1. Go to **Release** → **Production** (or **Internal testing** first)
2. Click **Create new release**
3. Upload `app-release.aab` file
4. Wait for processing (1-2 minutes)

**Checklist:**
- [ ] Bundle uploaded successfully
- [ ] No validation errors
- [ ] File shows in release section

### Complete Release Information

**Checklist:**
- [ ] Release name entered (v1.0.0)
- [ ] Release notes added (from CHANGELOG.md)
- [ ] Version code verified (1)
- [ ] Version name verified (1.0.0)
- [ ] All app details complete

### Submit for Review

**Checklist:**
- [ ] Reviewed all information once more
- [ ] Rollout set to 5% for initial launch
- [ ] Ready to submit
- [ ] Clicked "Submit" or saved as draft

---

## ✅ PHASE 8: MONITORING (Ongoing)

### First 6 Hours
- [ ] Monitor Play Console dashboard
- [ ] Check for crash reports
- [ ] Note any errors in real-time logs

### First 24 Hours
- [ ] Check if app appears in Play Store search
- [ ] Verify download link works
- [ ] Test install on device (if possible)
- [ ] Check ratings and reviews

### First Week
- [ ] Monitor crash rate (target: < 0.5%)
- [ ] Monitor ANR rate (target: < 0.1%)
- [ ] Read user reviews and feedback
- [ ] Plan fixes if issues found
- [ ] Track download/install numbers

### Ongoing (Monthly)
- [ ] Check analytics
- [ ] Respond to reviews
- [ ] Plan next feature release
- [ ] Monitor for deprecations
- [ ] Update as needed

---

## 📝 Important Passwords & Keys

**⚠️ SAVE THESE SECURELY:**

```
Keystore File: C:\Users\your_username\key.jks
Store Password: [Your Strong Password]
Key Alias: pokemon_key
Key Password: [Your Strong Password]
```

**Storage Locations:**
- [ ] Primary: Saved locally
- [ ] Backup 1: External USB drive
- [ ] Backup 2: Cloud storage (encrypted)
- [ ] Backup 3: Password manager

---

## 🚨 Critical Reminders

### NEVER:
- ❌ Commit `local.properties` to GitHub
- ❌ Commit `key.jks` files to GitHub
- ❌ Share your keystore password
- ❌ Lose your keystore file
- ❌ Upload signing credentials anywhere
- ❌ Use same keystore for different apps

### ALWAYS:
- ✅ Keep keystore backups secure
- ✅ Use strong passwords
- ✅ Keep credentials encrypted
- ✅ Use environment variables in builds
- ✅ Backup keystore before each release
- ✅ Test before submitting to Play Store

---

## 📊 Tracking Completion

Mark off each section as you complete it:

| Phase | Task | Status |
|-------|------|--------|
| 1 | Preparation | ☐ Complete |
| 2 | GitHub Setup | ☐ Complete |
| 3 | Signing Setup | ☐ Complete |
| 4 | Build Release | ☐ Complete |
| 5 | Play Store Setup | ☐ Complete |
| 6 | Store Listing | ☐ Complete |
| 7 | Upload & Submit | ☐ Complete |
| 8 | Monitoring | ☐ Complete |

---

## 🎯 Time Estimates

| Phase | Time | Total |
|-------|------|-------|
| Phase 1: Preparation | 30 min | 30 min |
| Phase 2: GitHub | 15 min | 45 min |
| Phase 3: Signing | 10 min | 55 min |
| Phase 4: Build | 10 min | 65 min |
| Phase 5: Play Store | 20 min | 85 min |
| Phase 6: Store Listing | 25 min | 110 min |
| Phase 7: Upload | 10 min | 120 min |
| **Total Active Time** | - | **2 hours** |
| **Review Wait Time** | 2-24 hours | - |

---

## ✨ Success Criteria

**Your launch is successful when:**
- ✅ Repository visible on GitHub
- ✅ Code builds without errors
- ✅ App appears in Google Play Store
- ✅ Can download and install from Play Store
- ✅ App runs without crashes
- ✅ User reviews are positive
- ✅ Download numbers increasing

---

## 🎉 Post-Launch

After your app is live:

1. **Monitor daily for first week:**
   - Crash reports
   - User reviews
   - Download metrics

2. **Plan updates:**
   - Bug fixes for any issues
   - Feature improvements from feedback
   - Quarterly major updates

3. **Grow community:**
   - Respond to reviews
   - Accept contributions on GitHub
   - Build features users want

---

## 📞 If You Get Stuck

**Build issues?** → Read `BUILD_INSTRUCTIONS.md` troubleshooting section  
**GitHub issues?** → Read `GITHUB_SETUP_GUIDE.md`  
**Publishing issues?** → Read `COMPLETE_DEPLOYMENT_GUIDE.md`  
**Security questions?** → Read `SECURITY.md`  
**Before each release?** → Check `RELEASE_CHECKLIST.md`  

---

**Good luck! Your app is ready to launch! 🚀**

Remember: Starting from right now, you can be live on Google Play Store in just 2 hours!

Questions? Check the comprehensive documentation in the project root directory.

---

**Last Updated:** January 2025  
**Version:** 1.0.0  
**Status:** 🟢 Ready for Launch
