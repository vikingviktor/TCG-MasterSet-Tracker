# Pokemon Master Set Tracker

[![Build Status](https://github.com/vikingviktor/TCG-MasterSet-Tracker/workflows/Android%20Build%20&%20Test/badge.svg)](https://github.com/vikingviktor/TCG-MasterSet-Tracker/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Android API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.4-success.svg)](https://developer.android.com/jetpack/compose)

Track and manage your Pokemon Trading Card Game collection with ease.

---

## 🎯 Quick Links

📚 **[📖 Start Here](docs/00_START_HERE.md)** - New to this project? Start here!

🚀 **[Ready to Launch?](docs/READY_FOR_LAUNCH.md)** - Want to publish to Google Play Store?

📋 **[Complete Checklist](docs/MASTER_LAUNCH_CHECKLIST.md)** - Step-by-step workflow for publishing
✅ **[Play Store Submission](docs/GOOGLE_PLAY_SUBMISSION_CHECKLIST.md)** - Complete checklist with all required information
� **[Privacy Policy](docs/privacy-policy.html)** - Our privacy policy for the Play Store

�📚 **[All Documentation](docs/)** - Browse all documentation files

---

## ✨ Features

- **Card Search & Browse** - Search 1000+ Pokemon cards by name, set, and language
- **Collection Management** - Track which cards you own with condition ratings
- **Completion Tracking** - See your collection completion percentage
- **Favorites System** - Save favorite Pokemon for quick access
- **Price Information** - View card market prices from TCGPlayer
- **Offline Support** - Browse cached cards without internet connection
- **Modern UI** - Beautiful Material Design 3 interface with Pokemon theme colors
- **Multi-language** - Support for English and Japanese card sets

---

## 🏗️ Architecture

This project uses modern Android technologies:

- **MVVM Architecture** with Clean Architecture principles
- **Jetpack Compose** for declarative UI
- **Room Database** for local data caching
- **Retrofit** for REST API integration
- **Hilt** for dependency injection
- **Coil** for image loading and caching
- **Kotlin Coroutines** for asynchronous operations

**Data Source:** [PokemonTCG.io API](https://docs.pokemontcg.io/)

---

## 📂 Project Structure

```
.
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/example/pokemonmastersettracker/
│   │   │   ├── data/              # Data layer (API, DB, Models, Repository)
│   │   │   ├── di/                # Dependency injection (Hilt)
│   │   │   ├── ui/                # Presentation (Screens, Components, Theme)
│   │   │   ├── viewmodel/         # State management
│   │   │   └── utils/             # Utilities and helpers
│   │   └── res/                   # Resources
│   └── build.gradle.kts           # App configuration
├── docs/                          # 📚 Comprehensive documentation
├── build.gradle.kts               # Project configuration
├── settings.gradle.kts            # Module setup
├── gradle.properties              # Gradle properties
└── LICENSE                        # MIT License
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio 2022.1 or later
- JDK 17 or higher
- Android SDK API 34
- Gradle 8.x

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/vikingviktor/TCG-MasterSet-Tracker.git
   cd TCG-MasterSet-Tracker
   ```

2. **Open in Android Studio**
   - File → Open → Select the project
   - Let Gradle sync automatically

3. **Run the app**
   - Select a device/emulator
   - Click Run (or press Shift + F10)

### Configuration

The app uses the public **PokemonTCG.io API** - no API keys required!

---

## 📚 Documentation

All documentation has been moved to the [`docs/`](docs/) folder:

| Document | Purpose |
|----------|---------|
| **[00_START_HERE.md](docs/00_START_HERE.md)** | 👈 **Start here!** Project overview and quick start |
| **[READY_FOR_LAUNCH.md](docs/READY_FOR_LAUNCH.md)** | Project status and 6-step publishing guide |
| **[MASTER_LAUNCH_CHECKLIST.md](docs/MASTER_LAUNCH_CHECKLIST.md)** | Complete 8-phase workflow to publish |
| **[BUILD_INSTRUCTIONS.md](docs/BUILD_INSTRUCTIONS.md)** | Build, signing, and release configuration |
| **[COMPLETE_DEPLOYMENT_GUIDE.md](docs/COMPLETE_DEPLOYMENT_GUIDE.md)** | End-to-end GitHub to Play Store guide |
| **[API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)** | REST API endpoints and data models |
| **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** | MVVM pattern and design decisions |
| **[QUICK_START.md](docs/QUICK_START.md)** | Development setup and first run |

➡️ **[See all documentation →](docs/DOCUMENTATION_INDEX.md)**

---

## 🎯 Key Features

### Card Search
```
1. Enter Pokemon name
2. Select language (EN/JA)
3. View detailed card info
4. Check market prices
```

### Collection Management
```
1. Add cards to collection
2. Mark as owned/missing
3. Rate condition
4. Track completion %
```

### Favorites
```
1. Save favorite Pokemon
2. Quick access
3. View all cards for favorite
4. Manage list
```

---

## 🔒 Security & Privacy

- ✅ No personal data collection
- ✅ HTTPS-only API communication
- ✅ Local-only data storage
- ✅ Secure credential handling
- ✅ MIT Open Source License

See [SECURITY.md](docs/SECURITY.md) for details.

---

## 🤝 Contributing

We welcome contributions! Please:

1. Fork the repository
2. Create a feature branch
3. Follow [CONTRIBUTING.md](docs/CONTRIBUTING.md)
4. Submit a pull request

See [CONTRIBUTING.md](docs/CONTRIBUTING.md) for detailed guidelines.

---

## 📊 Project Statistics

- **23** Kotlin source files
- **2000+** lines of code
- **27** comprehensive documentation files
- **60+** code examples
- **15+** architecture diagrams
- **MVVM** architecture with Jetpack Compose
- **4** database entities
- **6** API endpoints
- **5** full-featured screens
- **4** ViewModels

---

## 📱 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 1.9.0 |
| UI | Jetpack Compose | 1.5.4 |
| Architecture | MVVM + Clean | Latest |
| Database | Room | 2.6.1 |
| API | Retrofit | 2.9.0 |
| DI | Hilt | 2.48 |
| Images | Coil | 2.5.0 |
| Async | Coroutines | 1.7.3 |
| Design | Material Design 3 | Latest |
| Target SDK | Android 14 | API 34 |
| Min SDK | Android 7.0 | API 24 |

---

## 🎓 Learning Resources

This project demonstrates:
- MVVM pattern with Jetpack Compose
- Clean Architecture principles
- Database design with Room
- REST API integration
- Dependency injection with Hilt
- State management with StateFlow
- Building production-ready Android apps

Perfect for learning modern Android development!

---

## 📄 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) for details.

The app uses data from [PokemonTCG.io](https://pokemontcg.io/) (public API, no license required).

---

## 📞 Support

- 📖 **Documentation**: Check [docs/](docs/) folder
- 🐛 **Issues**: Create an issue on GitHub
- 💬 **Discussions**: Use GitHub Discussions
- 🔒 **Security**: See [SECURITY.md](docs/SECURITY.md)

---

## 🚀 Next Steps

### First Time Here?
1. Read **[00_START_HERE.md](docs/00_START_HERE.md)**
2. Run the app locally
3. Explore the code

### Want to Publish?
1. Follow **[MASTER_LAUNCH_CHECKLIST.md](docs/MASTER_LAUNCH_CHECKLIST.md)**
2. Takes ~90 minutes to publish to Google Play Store
3. See **[BUILD_INSTRUCTIONS.md](docs/BUILD_INSTRUCTIONS.md)** for details

### Want to Develop?
1. Check **[QUICK_START.md](docs/QUICK_START.md)**
2. Read **[ARCHITECTURE.md](docs/ARCHITECTURE.md)**
3. See **[DEVELOPER_QUICK_REFERENCE.md](docs/DEVELOPER_QUICK_REFERENCE.md)**

---

## 🌟 Features Coming Soon

- 🔐 User authentication with Firebase
- 💾 Cloud backup
- 📊 Price history charts
- 📤 Collection export (PDF/CSV)
- 🌙 Dark mode
- 📸 Barcode scanning
- 🔄 Trading wishlist

---

## 🎉 Status

| Status | Details |
|--------|---------|
| **Version** | 1.0.0 |
| **Development** | ✅ Complete |
| **Documentation** | ✅ Complete |
| **Testing** | ✅ Ready |
| **Publishing** | ✅ Ready |

---

**Pokemon Master Set Tracker** - Track your collection, master your sets! 🎮✨

Made with ❤️ for Pokemon collectors everywhere.
