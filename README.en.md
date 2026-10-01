<div align="center">

[🇪🇸 Español](README.md) · **🇬🇧 English**

# 📚 Gradify - Academic Management System

### *Your smart companion for academic success*

[![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2025.02.00-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

**Gradify** is a modern native Android app that helps university students manage their courses, grades and GPA efficiently, intuitively and professionally.

[🚀 Features](#-key-features) • [🛠️ Tech Stack](#️-tech-stack) • [⚙️ Setup](#️-installation--setup) • [🏗️ Architecture](#️-project-architecture) • [👥 Credits](#-credits)

</div>

---

## 🎯 What is Gradify?

Gradify is much more than a grade calculator. It is a **complete academic ecosystem** that lets you:

- 📊 **Manage courses** with custom grading systems (0-5, 0-10, 0-100, letters A-D)
- 🧮 **Calculate weighted averages** automatically, in real time
- 🎯 **Plan your strategy** with the "What grade do I need?" calculator
- 📈 **Visualize your performance** with interactive charts
- 📅 **Schedule exams** in a smart calendar with reminders
- 🤖 **Get personalized study recommendations** powered by AI
- 💾 **Back up your grades** to a folder you choose (they survive uninstalling the app) and export reports to Excel and PDF
- 🎨 **Enjoy a modern UI** with Material Design 3 and dynamic theming

**Lovingly dedicated to all university students, and especially to "miripili" 💜**

---

## 📲 Using the app (no build needed)

1. Download the APK from **Releases** and install it (allow "install from unknown sources" if Android asks).
2. Open Gradify and tap **Continue without account**. No Google account needed: everything is stored on your phone.
3. **Recommended backup:** in *Settings → Automatic backup folder* create or pick a folder (e.g. "Gradify" inside Documents). The app saves a copy there daily; if you switch phones, use *Restore backup*.
4. **AI recommendations (optional):** get a free key at [Google AI Studio](https://aistudio.google.com/app/apikey) and paste it in *Settings → AI recommendations*. It is stored only on your phone.
5. When the term ends, pick the semester on *Home* and tap **Close semester**: it leaves the list but still counts in your statistics.

---

## ✨ Key Features

### 📚 Complete Academic Management
- ✅ **Multiple grading systems**: 0-5 (Colombia), 0-10 (Mexico/Spain), 0-100 (percentage), letters A-D, or custom scales
- ✅ **Flexible components**: define midterms, workshops, quizzes, assignments and projects with their weights
- ✅ **Unlimited sub-grades**: add multiple attempts, submissions or activities inside each component
- ✅ **Automatic calculation**: weighted averages computed in real time from the assigned percentages
- ✅ **Drag & Drop**: reorder components and activities with intuitive touch gestures

### 🎯 Smart Tools
- 🧮 **Predictive calculator**: find out what grade you need in upcoming activities to reach your goal
- 📊 **Statistics dashboard**: overall average, passing / at-risk courses, semester progress
- 🔔 **Custom reminders**: notifications before exams, deadlines and important events
- 🤖 **AI recommendations**: study suggestions based on your performance, using Google Gemini

### ☁️ Sync & Backup
- 📥 **Automatic backup**: optional background sync with Google Sheets
- 📄 **Professional export**: detailed reports in Excel (.xlsx) and PDF
- 🔄 **Offline work**: all functionality available without internet
- 🔐 **Secure authentication**: sign in with your Google account

### 🎨 Premium User Experience
- 🌈 **Material You**: dynamic theme that adapts to your device colors (Android 12+)
- 🌙 **Dark mode**: automatic light and dark themes
- 📱 **Responsive design**: optimized for phones, phablets and tablets
- 🎭 **Smooth animations**: soft transitions and haptic feedback
- 🌐 **Multilingual**: Spanish and English
- 🏠 **Widgets**: see your averages right from the home screen

---

## 🛠️ Tech Stack

### Core
| Category | Technology |
|----------|-----------|
| **Language** | [Kotlin 2.1.0](https://kotlinlang.org/) - 100% Kotlin, null-safety |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) - modern declarative UI |
| **Design System** | [Material Design 3](https://m3.material.io/) - Material You with dynamic theming |
| **Min SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 15 (API 35) |

### Architecture
| Layer | Implementation |
|-------|---------------|
| **Pattern** | MVVM (Model-View-ViewModel) |
| **Architecture** | Clean Architecture (data / domain / presentation) |
| **Dependency Injection** | [Hilt](https://dagger.dev/hilt/) |
| **Navigation** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) |
| **Concurrency** | Kotlin Coroutines + Flow |

### Persistence & Backend
| Component | Technology |
|-----------|-----------|
| **Local Database** | [Room](https://developer.android.com/training/data-storage/room) - type-safe SQLite |
| **Preferences** | DataStore (SharedPreferences replacement) |
| **Authentication** | Google Sign-In (Credential Manager API) |
| **Cloud Sync** | Google Sheets API v4 + Google Drive API |
| **Background Tasks** | WorkManager - periodic sync |

### Advanced Features
| Feature | Library |
|---------|---------|
| **Artificial Intelligence** | Gemini / Groq / OpenRouter over REST (+ backend proxy) |
| **Excel Export** | [Apache POI](https://poi.apache.org/) - .xlsx generation |
| **PDF Export** | Native Android PDF API |
| **Drag & Drop** | [sh.calvin.reorderable](https://github.com/Calvin-LL/Reorderable) |
| **Security** | AndroidX Security Crypto (EncryptedSharedPreferences) |
| **Logging** | [Timber](https://github.com/JakeWharton/timber) |

### Testing & CI/CD
- **Unit Testing**: JUnit 5 + MockK
- **UI Testing**: Compose UI Test
- **Code Quality**: Detekt + ktlint

---

## 🏗️ Project Architecture

Gradify implements **Clean Architecture** with strict separation of concerns:

```
app/src/main/java/com/notasapp/
├── data/          # Data layer: Room (AppDatabase, DAOs, entities, relations),
│                  #   mappers, repository implementations, remote services
├── domain/        # Business logic: pure models, repository interfaces, use cases
├── ui/            # Presentation: auth, home, materia (create/detail/edit), stats,
│                  #   calendar, recomendaciones, export, settings, onboarding,
│                  #   components, theme
├── di/            # Dependency injection (Hilt modules)
├── navigation/    # Routes (Screen.kt) and navigation graph (NotasNavGraph.kt)
├── utils/         # GradeCalculator, Excel/PDF exporters, BackupManager, notifications
└── widget/        # Home screen widgets
```

### Data Flow
```
[UI Layer - Compose]
       ↕️ StateFlow
[ViewModel Layer]
       ↕️ Use Cases
[Domain Layer]
       ↕️ Repository Interface
[Data Layer - Room DB]
```

---

## ⚙️ Installation & Setup

### 📋 Prerequisites

- **Android Studio**: Ladybug (2024.3) or newer
- **JDK**: 17 or newer
- **Android SDK**: API 35 (Android 15)
- **Google account**: for authentication and sync (optional)

### 🔧 Project Setup

#### 1️⃣ Clone the Repository
```bash
git clone https://github.com/Pedroj-64/Gradify_App.git
cd Gradify_App
```

#### 2️⃣ Open in Android Studio
- Open Android Studio, choose `File > Open` and select the project folder
- Wait for Gradle to sync dependencies

#### 3️⃣ Configure Google Sign-In

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable these APIs:
   - **Google Sign-In API**
   - **Google Sheets API** (optional, for sync)
   - **Google Drive API** (optional, for backup)
4. Go to `Credentials > Create Credentials > OAuth 2.0 Client ID`
5. Choose **Android** as the application type
6. Enter:
   - **Package name**: `com.notasapp`
   - **SHA-1**: get it with `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
7. Copy the generated **Web Client ID**

#### 4️⃣ Configure Environment Variables

Create a `local.properties` file in the project root (if missing):

```properties
## Android SDK (auto-generated by Android Studio)
sdk.dir=/path/to/Android/Sdk

## Google OAuth 2.0 Web Client ID (REQUIRED for login)
GOOGLE_CLIENT_ID=YOUR_WEB_CLIENT_ID.apps.googleusercontent.com

## Optional APIs (leave empty if unused)
GEMINI_API_KEY=your_optional_gemini_api_key
BACKEND_URL=
GROQ_API_KEY=
OPENROUTER_API_KEY=
```

> ⚠️ **Important**: `local.properties` is in `.gitignore` and must never be pushed to GitHub.

> 💡 **Local build**: use JDK 17 or 21 (not 25). You need `app/google-services.json` (see `google-services.example.json`) and `sdk.dir` in `local.properties`. AI keys in `local.properties` are only included in *debug* builds. In the public APK each user pastes their own free Gemini key in **Settings → AI recommendations**; the backend (`backend/`) is optional.

#### 5️⃣ Build and Run

```bash
# Debug build
./gradlew assembleDebug

# Install on a connected device
./gradlew installDebug

# Run tests
./gradlew test
```

Or just press **▶ Run** in Android Studio.

---

## 🎓 Usage Guide

### First Use

1. **Sign in** with your Google account (or continue without an account)
2. **Interactive tutorial**: follow the onboarding walkthrough
3. **Create your first course**:
   - Tap the floating ➕ button
   - Set name, term and professor
   - Pick the grading scale (0-5, 0-10, etc.)
   - Add components (midterms, workshops, etc.) with their weights

### Managing Grades

1. **Open a course**: tap a course card on Home
2. **Add grades**: tap the ✏️ icon next to each component
3. **See your average**: it updates automatically in real time
4. **Calculator**: use the 🧮 button to find the grade you need

### Statistics & Reports

- **Dashboard**: overall average in the "Statistics" tab
- **Export Excel**: course menu → Export → Excel
- **Export PDF**: generate professional PDF reports
- **Sync**: connect Google Sheets for automatic backup

---

## 📊 Roadmap & Project Status

### ✅ Phase 1 - MVP (DONE)
MVVM + Clean Architecture, Room with optimized DAOs, domain models and repositories, Hilt, full navigation, Google Sign-In, course list, 3-step creation wizard, detail view with grade entry, automatic weighted averages, Material Design 3 UI.

### ✅ Phase 2 - User Experience (DONE)
Smooth animations, drag & drop with haptics, "What grade do I need?" calculator, dark mode and dynamic theme, advanced statistics, reusable components (ShimmerLoading, GradeBadge…), skeleton screens.

### ✅ Phase 3 - Cloud & Export (DONE)
Excel (.xlsx) export with Apache POI, professional PDF export, Google Sheets API integration, WorkManager auto-sync, offline-first with background sync.

### ✅ Phase 4 - Advanced Features (DONE)
Smart notifications, exam calendar with reminders, home screen widget, AI study recommendations (Gemini), full JSON backup/restore, multilingual (ES/EN), tablet and landscape support.

### 🚀 Phase 5 - Future Improvements (PLANNED)
- [ ] Study mode with built-in Pomodoro
- [ ] Share progress on social media
- [ ] Anonymous comparison with classmates
- [ ] Institutional calendar integration
- [ ] Tablet app with multi-course view
- [ ] Web version with real-time sync
- [ ] Gamification (achievements, study streaks)

See also [ROADMAP.md](ROADMAP.md) and [CHANGELOG.md](CHANGELOG.md).

---

## 🔒 Security & Privacy

- ✅ **OAuth 2.0**: secure Google authentication without storing passwords
- ✅ **Encrypted storage**: credentials kept with `EncryptedSharedPreferences`
- ✅ **HTTPS only**: all external API traffic uses TLS
- ✅ **Local first**: your grades are stored locally in Room (SQLite)
- ✅ **Minimal permissions**: only strictly necessary permissions are requested
- ✅ **No tracking**: no third-party analytics or personal data collection
- ✅ **Open source**: audit the full code on GitHub
- ✅ **ProGuard**: code obfuscation in release builds

> 💡 **Note**: Google Sheets sync is entirely optional. You can use the app offline.

---

## 🤝 Contributing

Contributions are welcome!

1. **Fork** the repository
2. Create a **branch** (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add: amazing new feature'`)
4. **Push** the branch (`git push origin feature/amazing-feature`)
5. Open a **Pull Request**

### Code Guidelines
- Idiomatic **Kotlin** and best practices
- Follow **SOLID** and **Clean Architecture**
- Document public functions with **KDoc**
- Add **unit tests** for critical logic
- Use **ktlint** for consistent style

---

## 🐛 Reporting Bugs

Open a [GitHub Issue](https://github.com/Pedroj-64/Gradify_App/issues) including:

- Problem description
- Steps to reproduce
- Expected vs. actual behavior
- Screenshots (if applicable)
- Android version and device model

---

## 📱 Support & Contact

- **Email**: kelequel@gmail.com
- **GitHub Issues**: [Open a ticket](https://github.com/Pedroj-64/Gradify_App/issues)

---

## 👥 Credits

Developed with dedication by **Systems and Computing Engineering** students at **Universidad del Quindío**, Armenia, Colombia.

**Lead developer — Pedro José Soto Rivera**
*Architecture, frontend & backend development, API integration, CI/CD. Junior Cybersecurity Analyst.*

**Special thanks**
- **Mateo Gómez Marulanda** - Testing, UX and design feedback
- **Santiago Padilla Ríos** - Testing, feature ideas and validation

💜 *Dedicated with love to all university students fighting every semester to reach their goals, and especially to **"miripili"**, who inspired this tool.*

<div align="center">

**Systems and Computing Engineering** · *Faculty of Engineering* · Armenia, Quindío, Colombia
**"Science, Education and Culture"**

</div>

---

## 📄 License

Licensed under the **MIT License** — see [LICENSE](LICENSE).

---

<div align="center">

### Made with ❤️ by students, for students

**Gradify** - *Your academic ally on the road to success* 🎓

[⬆ Back to top](#-gradify---academic-management-system)

**© 2026 Pedro José Soto Rivera | Universidad del Quindío**

</div>
