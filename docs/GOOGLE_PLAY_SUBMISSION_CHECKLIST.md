# Google Play Store Submission Checklist

**App:** Pokemon Master Set Tracker  
**Status:** Ready for submission  
**Checklist Date:** December 29, 2025

---

## Pre-Submission Requirements

### ✅ Technical Setup (COMPLETED)

- [x] Signed APK/AAB created with release keystore
- [x] Keystore file: `C:\Users\victo\key.jks`
- [x] App icon uploaded (MSTicon in all densities)
- [x] App name configured in strings.xml
- [x] AndroidManifest.xml properly configured
- [x] Privacy policy created and published
- [x] All required permissions justified (Internet, Storage, Network State)

### ⚠️ Google Play Developer Account

- [ ] Google Play Developer account created ($25 one-time fee)
- [ ] Payment method added to account
- [ ] Developer profile completed with name and contact info
- [ ] Merchant account set up (if offering in-app purchases)

---

## App Listing Information

### Required Store Text

- [ ] **App name** (50 char max):  
  `Pokemon Master Set Tracker`

- [ ] **Short description** (80 char max):  
  `Manage and track your Pokemon TCG collection`

- [ ] **Full description** (4,000 char max):  
  Should include:
  - What the app does
  - Key features (Search, Collection Management, Favorites, etc.)
  - Supported languages/sets
  - Data source (Pokemon TCG API)
  - No ads/in-app purchases
  - Example:
  ```
  Track and manage your Pokemon Trading Card Game collection with ease!
  
  FEATURES:
  • Search 1000+ Pokemon cards by name, set, and language
  • Track which cards you own with condition ratings
  • View collection completion percentage
  • Save favorite Pokemon for quick access
  • View market prices from TCGPlayer
  • Works offline with cached card data
  • Beautiful Material Design 3 interface
  
  Data is stored locally on your device. Your privacy is important to us.
  See our privacy policy for details.
  ```

- [ ] **Promotional text** (80 char max - optional):  
  `Track your Pokemon card collection in style`

---

## Graphics and Media

### App Icon
- [x] 512x512 PNG already uploaded
- [ ] Icon is clear and recognizable at small sizes
- [ ] No transparency issues

### Screenshots (Required)
Minimum: 2 screenshots, Recommended: 5-8 (for phones)

Create these showing:
- [ ] 1. App home screen with card search
- [ ] 2. Card details/collection view
- [ ] 3. Favorites feature
- [ ] 4. Collection statistics/completion tracking
- [ ] 5. Search and filter functionality

**Technical Requirements:**
- Format: PNG or JPG
- Size: Between 320x426 and 3840x2160 pixels
- Aspect ratio: 9:16 (portrait) recommended
- **Text on screenshots:** Add captions describing features
- Localization: Screenshots for English language minimum

**Tools to create:**
- Android Studio emulator screenshots
- Add text overlays explaining features
- Use design tool (Figma, Canva) for professional look

### Feature Graphic
- [ ] 1024x500 PNG (optional but recommended)
- Show app name + key feature visually

### Video Preview (Optional)
- [ ] 30-second demo video (15-30 MB max)
- Shows app in action

---

## App Content Rating

### Complete Content Rating Questionnaire

- [ ] Access Google Play Console → App content → Target audience and content
- [ ] Answer all questions about:
  - **Target Age Group:** 3+ (Pokemon is family-friendly)
  - **Content in App:**
    - Violence: None
    - Adult content: None
    - Gambling: None (card tracking only, no loot boxes)
    - Other content: None
  - **Permitted audience:** Everyone
  - **Restricted ads:** Can show ads (if you add them)

---

## Pricing & Distribution

### Pricing
- [ ] **Price:** Free (recommended for first app)
- [ ] **In-app purchases:** None currently
- [ ] **Ads:** None currently

### Countries/Regions
- [ ] Select countries where app will be available
- [ ] Recommended: Start with English-speaking countries
  - United States
  - United Kingdom
  - Canada
  - Australia
  - (Later expand to other regions)

---

## Consent & Policies

### Privacy & Permissions

- [ ] **Privacy Policy:** https://github.com/vikingviktor/TCG-MasterSet-Tracker/blob/main/docs/privacy-policy.html
  - ✅ Already created and published
  
- [ ] **Terms of Service:** (Optional, but recommended)
  - Could add simple ToS to your repository

- [ ] **Data Safety Form:**
  - [ ] Confirm what data you collect (none, only local storage)
  - [ ] Explain permission usage:
    - Internet: Fetch Pokemon card data
    - Storage: Save user collections locally
    - Network State: Check connectivity

---

## Legal & Compliance

### Store Listing Compliance

- [ ] App doesn't violate Google Play Policies
- [ ] No unauthorized use of Pokemon intellectual property
  - ✅ Using public Pokemon TCG API (authorized)
  - ✅ Displaying data for reference only
  - ✅ Not selling or distributing cards
  
- [ ] Contact information provided for support
- [ ] App doesn't contain malware or spyware
- [ ] No deceptive or manipulative behavior

### International Considerations

- [ ] Complies with GDPR (EU users)
- [ ] Complies with CCPA (California users)
- [ ] Privacy policy addresses age restrictions (13+)
- [ ] Notifies users about third-party APIs (Pokemon TCG, TCGPlayer)

---

## Technical Requirements Checklist

### App Configuration
- [x] Min SDK: 24 (Android 7.0)
- [x] Target SDK: 34 (Android 14)
- [x] 64-bit support: Yes (required for Play Store)
- [x] Kotlin: Used throughout

### Functionality Testing
- [ ] App launches without crashes
- [ ] All features work correctly:
  - [ ] Search cards
  - [ ] View card details
  - [ ] Add to collection
  - [ ] Mark favorites
  - [ ] View collection stats
- [ ] No permission errors
- [ ] Works on multiple screen sizes
- [ ] Tested on minimum API 24 device

### Performance
- [ ] App size < 100 MB (target < 50 MB)
- [ ] Launch time < 3 seconds
- [ ] No memory leaks
- [ ] Smooth scrolling in lists
- [ ] API calls work reliably

---

## Play Console Submission Steps

### 1. Create App
```
Google Play Console → Create App → Fill in app name and app type
```

### 2. Set Up App on Google Play
- [ ] **App access:** Public or Internal (for testing)
- [ ] **Paid or Free:** Select "Free"
- [ ] **Category:** Games → Card, Role-playing games → Games
- [ ] **Target audience:** Everyone

### 3. Create Store Listing
- [ ] Fill in all required text fields
- [ ] Upload icon (512x512)
- [ ] Upload 2+ screenshots
- [ ] Upload feature graphic (optional)
- [ ] Upload video (optional)

### 4. Content Rating
- [ ] Complete the questionnaire
- [ ] Submit for rating

### 5. Pricing & Distribution
- [ ] Set price: Free
- [ ] Select countries/regions
- [ ] Keep default settings for others

### 6. App Releases
- [ ] Go to "Release management" → "Releases"
- [ ] Select "Internal testing" or "Closed testing" first
- [ ] Upload your signed `app-release.aab`
- [ ] Add release notes:
  ```
  Version 1.0.0
  
  Initial release of Pokemon Master Set Tracker!
  
  Features:
  • Search 1000+ Pokemon trading cards
  • Track your collection with condition ratings
  • View market prices from TCGPlayer
  • Beautiful Material Design 3 interface
  • Works offline with cached data
  ```
- [ ] Review and publish to testing track

### 7. Test on Real Devices
- [ ] Invite testers (friends, family, or internal)
- [ ] Get feedback for 1-2 weeks
- [ ] Fix any reported issues
- [ ] Update app if needed

### 8. Production Release
- [ ] Go to "Releases" → "Create new release"
- [ ] Upload final `app-release.aab`
- [ ] Review store listing one more time
- [ ] Add release notes
- [ ] **Click "Publish"**

---

## After Submission

### Timeline
- **Publishing:** 5 minutes to a few hours
- **Review:** 24-72 hours (usually 24)
- **Go Live:** A few hours after approval

### Post-Launch
- [ ] Monitor crash reports in Play Console
- [ ] Check user reviews and ratings
- [ ] Respond to user feedback
- [ ] Track downloads and active users
- [ ] Plan future updates based on feedback

---

## Troubleshooting Common Issues

### App Rejected?
Check for:
- [ ] Unsigned APK/AAB
- [ ] Missing metadata (descriptions, screenshots)
- [ ] Incomplete privacy policy
- [ ] Intellectual property violations
- [ ] Deceptive/manipulative content

### App Not Appearing After Publish?
- [ ] May take 2-3 hours to appear in store
- [ ] Check it's available in your selected countries
- [ ] Refresh Play Store app search

### Performance Issues?
- [ ] Test on actual devices (emulator can hide issues)
- [ ] Check for API timeouts
- [ ] Verify card data loading works reliably

---

## Resources

- **Google Play Console:** https://play.google.com/console
- **Play Store Policies:** https://support.google.com/googleplay/android-developer
- **Android Publishing Guide:** https://developer.android.com/studio/publish
- **Your App:** https://github.com/vikingviktor/TCG-MasterSet-Tracker
- **Privacy Policy:** https://github.com/vikingviktor/TCG-MasterSet-Tracker/blob/main/docs/privacy-policy.html

---

## Quick Wins Before Submitting

1. ✅ **Create 5 great screenshots** (biggest impact on conversion)
2. ✅ **Write compelling short description** (hooks users)
3. ✅ **Add feature graphics** (looks professional)
4. ✅ **Fill all optional fields** (improves discoverability)
5. ✅ **Test app thoroughly** (prevents bad reviews)

---

## Final Checklist Before Hitting "Publish"

- [ ] AAB file rebuilt with new icon
- [ ] All store text filled in and proofread
- [ ] At least 2 screenshots uploaded
- [ ] Privacy policy linked
- [ ] Content rating submitted
- [ ] Countries selected
- [ ] Release notes written
- [ ] Tested on real device
- [ ] No "coming soon" or placeholder text

---

**You're almost there! After completing this checklist, your app will be ready to submit to Google Play Store.** 🚀

Questions? Refer back to this checklist or check the Play Store publishing documentation.

Good luck! 🎉
