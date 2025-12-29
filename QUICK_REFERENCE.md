# Pokemon Master Set Tracker - Quick Reference Card

Print this or keep it handy during your launch!

---

## 🚀 Quick Start: From Code to Play Store (70 minutes)

### Phase 1️⃣: GitHub (15 min)
```powershell
cd "C:\Users\victo\Documents\TCG-MasterSet-Tracker"
git init
git config --global user.name "Your Name"
git config --global user.email "your@email.com"
git remote add origin https://github.com/USERNAME/TCG-MasterSet-Tracker.git
git add .
git commit -m "Initial commit: Pokemon Master Set Tracker"
git branch -M main
git push -u origin main
```
✅ Then configure on github.com (description, topics, settings)

### Phase 2️⃣: Keystore (10 min)
```powershell
$env:STORE_PASSWORD = "YourPassword123!"
$env:KEY_PASSWORD = "YourPassword123!"

keytool -genkey -v `
  -keystore "$env:USERPROFILE\key.jks" `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000 `
  -alias pokemon_key `
  -storepass $env:STORE_PASSWORD `
  -keypass $env:KEY_PASSWORD `
  -dname "CN=Name, O=Company, L=City, ST=State, C=Country"
```
✅ Create `local.properties` in project root:
```properties
STORE_FILE=C:\Users\username\key.jks
STORE_PASSWORD=YourPassword123!
KEY_ALIAS=pokemon_key
KEY_PASSWORD=YourPassword123!
```

### Phase 3️⃣: Build (10 min)
```powershell
$env:STORE_FILE = "C:\Users\username\key.jks"
$env:STORE_PASSWORD = "YourPassword123!"
$env:KEY_ALIAS = "pokemon_key"
$env:KEY_PASSWORD = "YourPassword123!"
cd "C:\Users\victo\Documents\TCG-MasterSet-Tracker"
./gradlew bundleRelease
```
✅ Output: `app/build/outputs/bundle/release/app-release.aab`

### Phase 4️⃣: Play Store (10 min)
1. Go to https://play.google.com/console
2. Create developer account ($25)
3. Create new app: "Pokemon Master Set Tracker"

### Phase 5️⃣: Listing (20 min)
1. Add 5-8 screenshots (1440x2560)
2. Complete store descriptions
3. Complete content rating
4. Set as Free app

### Phase 6️⃣: Upload (5 min)
1. Go to Release → Production
2. Create new release
3. Upload `app-release.aab`
4. Add release notes from CHANGELOG.md
5. Submit for review

✅ **Done!** App will be live in 2-24 hours.

---

## 📚 Essential Documentation

| Need | File | Time |
|------|------|------|
| Overview | `READY_FOR_LAUNCH.md` | 15 min |
| Complete checklist | `MASTER_LAUNCH_CHECKLIST.md` | 20 min |
| GitHub setup | `GITHUB_SETUP_GUIDE.md` | 20 min |
| Build & signing | `BUILD_INSTRUCTIONS.md` | 25 min |
| Full guide | `COMPLETE_DEPLOYMENT_GUIDE.md` | 30 min |
| Pre-release QA | `RELEASE_CHECKLIST.md` | 20 min |

---

## 🔐 Critical Information

**⚠️ KEEP THESE SAFE:**
- Store Password: ___________________
- Key Password: ___________________
- Keystore File: `C:\Users\your_username\key.jks`

**Backup Locations:**
- [ ] External USB drive
- [ ] Cloud storage (encrypted)
- [ ] Password manager

---

## 🎯 File Locations

```
Project Root: C:\Users\victo\Documents\TCG-MasterSet-Tracker\

Key Files:
├── Source Code
│   └── app/src/main/kotlin/.../
├── Configuration
│   ├── build.gradle.kts
│   ├── gradle.properties
│   └── AndroidManifest.xml
├── Signing (after generation)
│   └── C:\Users\username\key.jks
├── Release Output
│   └── app/build/outputs/bundle/release/app-release.aab
└── Documentation
    ├── READY_FOR_LAUNCH.md
    ├── MASTER_LAUNCH_CHECKLIST.md
    ├── BUILD_INSTRUCTIONS.md
    └── ... (13 more guides)
```

---

## ✅ Pre-Launch Checklist

**Before Git Push:**
- [ ] No compilation errors: `./gradlew build`
- [ ] Tests pass: `./gradlew test`
- [ ] No hardcoded secrets
- [ ] Version numbers correct

**Before Keystore Generation:**
- [ ] Strong password ready (16+ chars)
- [ ] Personal info prepared (name, company, city)
- [ ] Understand this is permanent for your app

**Before Build Release:**
- [ ] Keystore generated: `ls "$env:USERPROFILE\key.jks"`
- [ ] local.properties created with correct paths
- [ ] Environment variables set correctly

**Before Play Store Upload:**
- [ ] .aab file exists: `app-release.aab`
- [ ] File size 4-8 MB
- [ ] Version code incremented (starts at 1)
- [ ] CHANGELOG.md updated
- [ ] Screenshots 1440x2560 pixels

**Before Submit:**
- [ ] Store listing complete (title, description, icon)
- [ ] Content rating approved
- [ ] Screenshots uploaded
- [ ] Privacy policy link ready
- [ ] Contact email valid

---

## 🔗 Important URLs

| Service | URL |
|---------|-----|
| GitHub | https://github.com |
| Google Play Console | https://play.google.com/console |
| Play Store | https://play.google.com/store |
| PokemonTCG API | https://api.pokemontcg.io/v2 |
| Android Docs | https://developer.android.com |

---

## 📊 Version Tracking

| Release | Code | Name | Status |
|---------|------|------|--------|
| v1.0.0 | 1 | Initial Release | 🟢 Ready |
| v1.0.1 | 2 | (Bug fixes) | ⭕ Next |
| v1.1.0 | 3 | (Features) | ⭕ Planned |

**Rule:** Always increment versionCode for each release!

---

## 🐛 Troubleshooting Quick Links

| Problem | Solution |
|---------|----------|
| Keystore not found | Check path in local.properties |
| Wrong password | Verify STORE_PASSWORD matches |
| Build fails | See `BUILD_INSTRUCTIONS.md` |
| GitHub auth fails | Use personal access token |
| Upload rejected | Check `RELEASE_CHECKLIST.md` |
| App crashes | Monitor Play Console crash reports |

---

## 📞 Documentation Quick Links

```
Need help? Pick one:

1. First time launch
   → READY_FOR_LAUNCH.md

2. Step-by-step checklist
   → MASTER_LAUNCH_CHECKLIST.md

3. GitHub setup
   → GITHUB_SETUP_GUIDE.md

4. Building & signing
   → BUILD_INSTRUCTIONS.md

5. Complete end-to-end
   → COMPLETE_DEPLOYMENT_GUIDE.md

6. Before each release
   → RELEASE_CHECKLIST.md

7. Architecture & code
   → ARCHITECTURE.md, API_DOCUMENTATION.md

8. Contribution guidelines
   → CONTRIBUTING.md

9. Security & privacy
   → SECURITY.md

10. Version history
    → CHANGELOG.md
```

---

## ⏱️ Time Breakdown

| Task | Time | Notes |
|------|------|-------|
| Read documentation | 20 min | Start with READY_FOR_LAUNCH.md |
| GitHub setup | 15 min | Code push |
| Keystore generation | 5 min | One-time setup |
| create local.properties | 5 min | DO NOT COMMIT |
| Build release | 5 min | Run gradlew bundleRelease |
| Play Store account | 10 min | Pay $25 |
| Store listing | 20 min | Screenshots, descriptions |
| Upload & submit | 5 min | Submit for review |
| **TOTAL** | **85 min** | To live app! |
| Review time | 2-24 hrs | Google's review |

---

## 🚦 Status Signals

- 🟢 Ready to launch
- 🟡 Minor issues to fix
- 🔴 Blocking issue

**Current Project:** 🟢 **READY!**

---

## 💰 Costs

| Item | Cost | When |
|------|------|------|
| Keystore generation | $0 | Once |
| GitHub repository | $0 | Once |
| Google Play account | $25 | Once |
| Uploading to Play Store | $0 | Every release |
| **Total** | **$25** | One-time |

---

## 🎯 Success Metrics (First Week)

| Metric | Target |
|--------|--------|
| Downloads | 100+ |
| Rating | 4.0+ stars |
| Crash rate | < 0.5% |
| Install retention | 30%+ |
| User reviews | Positive feedback |

---

## 🔄 After Launch

- [ ] Day 1: Monitor crash reports
- [ ] Day 2-7: Monitor ratings/reviews
- [ ] Week 2: Plan v1.0.1 bug fixes
- [ ] Month 1: Plan v1.1.0 features
- [ ] Ongoing: Respond to reviews, accept contributions

---

## 📱 App Store Links (after launch)

**Google Play Store:**
`https://play.google.com/store/apps/details?id=com.example.pokemonmastersettracker`

**GitHub Repository:**
`https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker`

---

## 🎉 You're Ready!

**Current state:** 100% complete  
**Time to launch:** 70 minutes  
**Status:** 🟢 READY TO GO  

**Next action:** Read `READY_FOR_LAUNCH.md` and follow `MASTER_LAUNCH_CHECKLIST.md`

---

**Pokemon Master Set Tracker v1.0.0**  
Complete. Documented. Ready for the world. ✨

Keep this card handy during launch!
