# Contributing to Pokemon Master Set Tracker

We welcome contributions! Whether you're fixing bugs, adding features, or improving documentation, your help is appreciated.

## Getting Started

1. **Fork the repository** - Click the fork button on GitHub
2. **Clone your fork** - `git clone https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker.git`
3. **Create a branch** - `git checkout -b feature/your-feature-name`
4. **Make changes** - Implement your feature or fix
5. **Commit** - `git commit -m "Add feature: description"`
6. **Push** - `git push origin feature/your-feature-name`
7. **Create Pull Request** - Open a PR with description

## Code Style Guidelines

### Kotlin Conventions
- Follow [Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Write self-documenting code
- Add comments for complex logic

### Architecture
- **Data Layer:** Keep in `data/` package
- **Presentation:** Keep in `ui/` package
- **State Management:** Use ViewModels + StateFlow
- **DI:** Use Hilt for dependency injection

### File Organization
```kotlin
package com.example.pokemonmastersettracker.feature

// Imports
import ...

// Top-level functions/constants

// Main class/interface

// Extension functions
```

## Testing

- Add unit tests for ViewModels
- Add repository tests
- Test new API endpoints
- Run `./gradlew test` before submitting PR

## Commit Messages

Use clear, descriptive commit messages:
- ✅ `Add feature: search card filtering`
- ✅ `Fix bug: image loading crashes`
- ✅ `Improve: database query performance`
- ❌ `fix` 
- ❌ `update`
- ❌ `stuff`

## Pull Request Process

1. **Update documentation** if you changed functionality
2. **Test thoroughly** on multiple devices
3. **Check for build errors** - Run `./gradlew build`
4. **Write description** explaining your changes
5. **Reference issues** - Use "Fixes #123" format
6. **Keep commits clean** - Rebase before merging

### PR Title Format
```
[Type]: Brief description

Types: feat, fix, docs, refactor, perf, test, ci, chore
Example: feat: Add card filtering by rarity
```

## Reporting Issues

When reporting bugs, include:
- **Device/Android version** - Important for reproduction
- **Steps to reproduce** - Clear, numbered steps
- **Expected behavior** - What should happen
- **Actual behavior** - What actually happened
- **Screenshots/Logs** - If applicable
- **Version** - Which app version

### Issue Title Format
```
[Type] Brief description

Types: bug, feature, improvement, documentation
```

## Feature Requests

1. **Check existing issues** - Avoid duplicates
2. **Describe use case** - Why do you need this?
3. **Provide examples** - Show what you envision
4. **Discuss implementation** - How would you build it?

## Documentation

If you:
- Add a new feature → Update README.md
- Change API usage → Update API_DOCUMENTATION.md
- Add new architecture → Update QUICK_START.md
- Fix a bug → Update changelog

## Development Setup

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/TCG-MasterSet-Tracker.git
cd TCG-MasterSet-Tracker

# Open in Android Studio
# Or sync Gradle manually
./gradlew build

# Run tests
./gradlew test

# Run on device
./gradlew installDebug
```

## Code Review

When reviewing code:
- ✅ Is it following the architecture?
- ✅ Does it follow Kotlin conventions?
- ✅ Are there tests?
- ✅ Is documentation updated?
- ✅ Are there performance concerns?
- ✅ Is it secure?

## Areas for Contribution

**High Priority:**
- [ ] Firebase authentication implementation
- [ ] Advanced filtering UI
- [ ] Price history tracking
- [ ] Collection export (PDF/CSV)

**Medium Priority:**
- [ ] Dark theme support
- [ ] Internationalization (i18n)
- [ ] Unit tests
- [ ] UI tests

**Low Priority:**
- [ ] Additional type colors
- [ ] Animation improvements
- [ ] Performance optimization
- [ ] Documentation improvements

## Project Structure Reminder

```
src/main/kotlin/com/example/pokemonmastersettracker/
├── data/          # API, Database, Models
├── di/            # Dependency Injection
├── ui/            # Screens, Components, Theme
├── viewmodel/     # State Management
└── utils/         # Helpers, Converters
```

## Need Help?

- **Questions?** Open a discussion on GitHub
- **Found a bug?** Open an issue
- **Have an idea?** Start a discussion first
- **Stuck?** Comment on the issue for help

## Thank You!

Your contributions make this project better for everyone. We appreciate your time and effort!

---

**Happy coding!** 🚀
