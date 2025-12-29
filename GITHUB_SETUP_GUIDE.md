# GitHub Repository Setup Guide

## Complete Step-by-Step Instructions for Uploading to GitHub

### Step 1: Create GitHub Account

If you don't have a GitHub account:
1. Go to [github.com](https://github.com)
2. Click **Sign up**
3. Enter email and create password
4. Verify your email
5. Complete your profile

### Step 2: Create New Repository

1. Visit [github.com/new](https://github.com/new)
2. **Repository name:** `TCG-MasterSet-Tracker`
3. **Description:** `Track your Pokemon TCG card collection with ease`
4. **Visibility:** Public (for app store visibility)
5. **Initialize repository:** Select "Add a README file" (optional, we have one)
6. **Add .gitignore:** Select "Android" (we have a custom one, will overwrite)
7. Click **Create repository**

### Step 3: Set Up Local Git

Open PowerShell in your project directory:

```powershell
# Navigate to project
cd "C:\Users\victo\Documents\TCG-MasterSet-Tracker"

# Initialize Git repository
git init

# Configure your Git identity (one time)
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Check Git is configured
git config --global --list
```

### Step 4: Add Remote Repository

```powershell
# Add your GitHub repository as remote
git remote add origin https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker.git

# Verify remote was added
git remote -v
```

### Step 5: Stage and Commit Files

```powershell
# See what files are untracked
git status

# Stage all files
git add .

# Verify files are staged
git status

# Create initial commit
git commit -m "Initial commit: Pokemon Master Set Tracker with MVVM architecture, Jetpack Compose UI, Room database, Retrofit API integration"
```

### Step 6: Create Main Branch and Push

```powershell
# Create and switch to main branch
git branch -M main

# Push to GitHub
git push -u origin main

# Verify push succeeded
git remote -v
```

### Step 7: Configure Repository Settings

In GitHub web interface:

1. Go to your repository: `github.com/YOUR_USERNAME/TCG-MasterSet-Tracker`

2. **Settings** tab:
   - **General**
     - Description: "Track your Pokemon TCG card collection"
     - Website: (leave blank or add your site)
     - Topics: Add `pokemon`, `tcg`, `android`, `kotlin`, `jetpack-compose`
     - Visibility: Public
     - Default branch: `main`

3. **Branches**
   - Set default branch to `main`
   - Add branch protection rules (optional):
     - Require pull request reviews
     - Require status checks
     - Dismiss stale reviews

4. **Security**
   - Enable "Dependabot alerts"
   - Enable "Secret scanning"

5. **Pages** (for documentation)
   - Source: Deploy from a branch
   - Branch: `main`
   - Folder: `/docs` (if you have documentation)

### Step 8: Add GitHub Topics

Topics help people discover your repository:

```
Topics to add:
- pokemon
- tcg
- android
- kotlin
- jetpack-compose
- room-database
- retrofit
- hilt
- material-design
- card-tracking
```

### Step 9: Create GitHub Releases

For each release:

```powershell
# Create and push a tag
git tag -a v1.0.0 -m "Initial release: Pokemon Master Set Tracker v1.0.0"
git push origin v1.0.0
```

Then in GitHub:
1. Go to **Releases**
2. Click **Draft a new release**
3. Tag: `v1.0.0`
4. Release title: `Pokemon Master Set Tracker v1.0.0`
5. Description: Copy from CHANGELOG.md
6. Upload binary: `app-release.aab`
7. Click **Publish release**

### Step 10: Enable GitHub Actions

CI/CD is already configured in `.github/workflows/build.yml`

1. Go to **Actions** tab
2. Workflows should be automatically enabled
3. Push code to trigger build
4. Monitor build status

To set up Play Store publishing automation (optional):

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Add new secret: `PLAY_STORE_SERVICE_ACCOUNT`
3. Value: (contents of service account JSON)
4. Now CI/CD will auto-deploy to Play Store on tag push

### Step 11: Add README Badges

Edit `README.md` and add badges at the top:

```markdown
# Pokemon Master Set Tracker

[![Build Status](https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker/workflows/Android%20Build%20&%20Test/badge.svg)](https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.4-success.svg)](https://developer.android.com/jetpack/compose)

Track your Pokemon TCG card collection with ease.
```

### Step 12: Set Up Branch Protection (Optional)

For production-ready projects:

1. Go to **Settings** → **Branches**
2. Add rule for `main` branch
3. Enable:
   - Require a pull request before merging
   - Require status checks to pass
   - Require branches to be up to date
   - Include administrators in restrictions

### Step 13: Configure Code Owners (Optional)

Create `.github/CODEOWNERS`:

```
# Default owners
* @YOUR_USERNAME

# Specific paths
/app/src/main/kotlin/com/example/pokemonmastersettracker/data/ @YOUR_USERNAME
/app/src/main/kotlin/com/example/pokemonmastersettracker/ui/ @YOUR_USERNAME
```

### Step 14: Add Funding Options (Optional)

Create `.github/FUNDING.yml`:

```yaml
github: [YOUR_USERNAME]
patreon: YOUR_PATREON
ko_fi: YOUR_KOFI
custom: ['https://paypal.me/YOUR_PAYPAL']
```

---

## Troubleshooting Git

### "fatal: not a git repository"
```powershell
# Make sure you're in the correct directory
cd "C:\Users\victo\Documents\TCG-MasterSet-Tracker"

# Re-initialize if needed
git init
```

### "Authentication failed"
```powershell
# Use personal access token instead of password
# Go to github.com/settings/tokens
# Create new token with 'repo' scope
# Use token as password when pushing

# Or set up SSH (recommended)
# See: https://docs.github.com/en/authentication/connecting-to-github-with-ssh
```

### "fatal: refusing to merge unrelated histories"
```powershell
# If created README in GitHub, pull first
git pull origin main --allow-unrelated-histories
git push origin main
```

### "Permission denied (publickey)"
```powershell
# Set up SSH keys
# See: https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent
```

### "Local changes would be overwritten"
```powershell
# Stash changes
git stash

# Or commit them
git add .
git commit -m "In progress changes"
```

---

## Useful Git Commands

```powershell
# View commit history
git log --oneline

# Create a new branch
git checkout -b feature/new-feature

# Switch to existing branch
git checkout main

# Push branch to GitHub
git push origin feature/new-feature

# Create pull request (on GitHub website)

# Merge pull request
git pull origin main

# Delete local branch
git branch -d feature/new-feature

# Delete remote branch
git push origin --delete feature/new-feature

# See all branches
git branch -a

# Revert last commit
git revert HEAD

# See changes not committed
git diff

# See staged changes
git diff --staged
```

---

## GitHub Collaboration

### If others want to contribute:

1. They fork your repository
2. Clone their fork
3. Create feature branch
4. Push to their fork
5. Create pull request to your main repo
6. You review and merge

### Accepting Contributions:

1. Go to **Pull requests** tab
2. Review the code
3. Comment with feedback (if needed)
4. Merge pull request
5. Delete branch

---

## Monetization on GitHub

### Sponsorship Options:
1. **GitHub Sponsors** - Set up in Settings
2. **Patreon** - Add to FUNDING.yml
3. **Ko-fi** - Add to FUNDING.yml

### Visibility:
- Star count increases with good documentation
- Trending tab shows popular repos
- Topics help discoverability

---

## Next Steps After Upload

1. ✅ Repository created and pushed
2. ✅ GitHub Actions configured
3. Next: Build and upload to Play Store
   - Follow [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md)
   - Generate keystore
   - Build .aab file
   - Upload to Play Console

---

## Quick Reference: GitHub URLs

- Repository: `https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker`
- Issues: `https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker/issues`
- Pull Requests: `https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker/pulls`
- Actions: `https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker/actions`
- Releases: `https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker/releases`
- Settings: `https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker/settings`

---

**Ready to push to GitHub?** Follow Step 1 through Step 6 above!
