# Pre-Release Checklist

Complete this checklist before each release to Google Play Store.

## Code Quality

- [ ] All unit tests pass
  ```powershell
  ./gradlew test
  ```

- [ ] All lint warnings resolved
  ```powershell
  ./gradlew lint
  ```

- [ ] No compilation errors
  ```powershell
  ./gradlew build
  ```

- [ ] ProGuard/R8 minification enabled
- [ ] Unused resources removed
- [ ] No hardcoded API keys or secrets
- [ ] Error handling implemented
- [ ] Null safety checks in place

## Documentation

- [ ] README.md is current
- [ ] API_DOCUMENTATION.md updated if API changed
- [ ] CHANGELOG.md updated with new features
- [ ] Code comments are clear
- [ ] Architecture documentation updated
- [ ] Known issues documented
- [ ] Breaking changes noted

## Testing

- [ ] Tested on minimum SDK (API 24)
- [ ] Tested on target SDK (API 34)
- [ ] Tested on multiple screen sizes
- [ ] Tested in landscape and portrait
- [ ] Network failures handled gracefully
- [ ] Offline mode tested
- [ ] All features work as expected
- [ ] No crashes found
- [ ] Performance is acceptable

## App Metadata

- [ ] App name correct
- [ ] Version number updated
- [ ] Version code incremented
- [ ] Target SDK set to 34
- [ ] Min SDK is 24
- [ ] All permissions justified
- [ ] Privacy policy ready

## Build Configuration

- [ ] Signing configuration correct
  ```powershell
  # Verify keystore exists
  ls "$env:USERPROFILE\key.jks"
  ```

- [ ] Release build works
  ```powershell
  ./gradlew bundleRelease
  ```

- [ ] Bundle size reasonable (< 50MB)
- [ ] All dependencies are up to date
- [ ] Gradle build succeeds with no warnings

## Store Listing

- [ ] App title is clear and concise
- [ ] Short description (80 characters) ready
- [ ] Full description (4000 characters) ready
- [ ] Screenshots prepared (5-8 images)
  - Resolution: 1440x2560 pixels minimum
  - PNG or JPEG format
  - Show key features
  - Text overlays are clear
  - Landscape screenshots if applicable

- [ ] Feature graphic prepared
  - Resolution: 1024x500 pixels
  - Shows app branding

- [ ] Promo graphic prepared (optional)
  - Resolution: 180x120 pixels

- [ ] Icon updated
  - Resolution: 512x512 pixels
  - PNG format
  - No transparency issues

- [ ] Category selected correctly
- [ ] Content rating completed
- [ ] Privacy policy URL provided
- [ ] Support email configured
- [ ] Website URL provided

## Security & Privacy

- [ ] No personal data collected
- [ ] HTTPS used for all API calls
- [ ] Sensitive data encrypted
- [ ] No permissions over-requested
- [ ] Privacy policy is accurate
- [ ] No tracking code (unless disclosed)
- [ ] keystore.jks NOT committed to Git
- [ ] local.properties NOT committed to Git
- [ ] No API keys in code

## Performance

- [ ] App launches within 2 seconds
- [ ] Network requests timeout handled
- [ ] Database queries optimized
- [ ] Images cached properly
- [ ] Memory leaks checked
- [ ] Battery usage acceptable
- [ ] Data usage acceptable

## Compatibility

- [ ] Android 7.0+ supported
- [ ] 64-bit support enabled
- [ ] Dark mode handled gracefully
- [ ] Various DPI screens tested
- [ ] Phone and tablet support

## Monetization (if applicable)

- [ ] Pricing set
- [ ] Free trial configured (if applicable)
- [ ] In-app purchases tested (if applicable)
- [ ] Ads configured (if applicable)
- [ ] Terms of service ready

## Release Process

1. [ ] Create Git tag
   ```powershell
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

2. [ ] Build signed bundle
   ```powershell
   ./gradlew bundleRelease
   ```

3. [ ] Verify bundle exists
   ```powershell
   ls app/build/outputs/bundle/release/app-release.aab
   ```

4. [ ] Upload to Play Console
   - Go to [Google Play Console](https://play.google.com/console)
   - Select app
   - Go to Release → Production
   - Click "Create new release"
   - Upload app-release.aab
   - Complete store listing
   - Select rollout percentage (recommend 5%, then 25%, then 100%)
   - Review and submit

5. [ ] Confirm submission
   - Monitor Play Console for review status
   - Watch for any rejection reasons
   - Monitor crash reports after release

## Post-Release

- [ ] Monitor app ratings and reviews
- [ ] Check crash reports in Play Console
- [ ] Monitor ANR (Application Not Responding) rate
- [ ] Check user feedback
- [ ] Update documentation if needed
- [ ] Plan next release features
- [ ] Create GitHub release notes
  ```powershell
  # Go to Releases → Create new release
  # Tag: v1.0.0
  # Title: "Pokemon Master Set Tracker v1.0.0"
  # Description: Copy from CHANGELOG.md
  # Upload: app-release.aab as binary
  ```

## Common Issues

| Issue | Solution |
|-------|----------|
| "Invalid keystore format" | Regenerate keystore with keytool |
| "Wrong keystore password" | Check local.properties |
| "Cannot find keystore" | Verify path in local.properties |
| "Version code already used" | Increment versionCode in gradle.properties |
| "Screenshots too small" | Use 1440x2560 minimum |
| "App rejected: crashes" | Check Play Console crash reports |
| "Content rating incomplete" | Complete IARC questionnaire |

## Rollback Plan

If critical issue found after release:

1. Disable app in Play Console if necessary
2. Fix issue immediately
3. Increment versionCode
4. Build new bundle
5. Re-submit with "critical fix" note
6. Monitor crash reports

---

**Ready to release?** Work through the checklist above, then follow the release process steps!

Last updated: 2025-01-XX
Version: 1.0.0
