 # Profile Discovery App

Android application for discovering and managing professional profiles. Built with Kotlin and Jetpack Compose.

## 📱 Download APK

[Download APK](https://drive.google.com/file/d/1g9pJDx8ZlyWV5HnR94yNoP8poNOF47l0/view?usp=sharing)

## 📱 Screenshots

| Login Screen | Register Screen | Profile Discovery |
|--------------|----------------|-------------------|
| ![Login](screenshots/login.png) | ![Register](screenshots/register.png) | ![Discovery](screenshots/discovery.png) |

| Profile Details | Edit Profile | Search & Filters |
|-----------------|--------------|------------------|
| ![Profile](screenshots/profile_details.png) | ![Edit](screenshots/edit_profile.png) | ![Search](screenshots/search.png) |

| Favorites | Settings Light | Settings Dark |
|-----------|---------------|---------------|
| ![Favorites](screenshots/favorites.png) | ![Light](screenshots/settings_light.png) | ![Dark](screenshots/settings_dark.png) |

---

## Features

### Authentication
- User registration with email/password
- User login
- Forgot password (email reset)
- Session management (auto-login)

### Profile Management
- Create profile with all required fields
- Edit profile information
- Upload profile picture
- View profile details

**Profile Fields:** Full Name, Age, Email, Phone, Occupation, Location, About Me

### Profile Discovery
- Browse all profiles in scrollable list
- Search profiles by name, occupation, or location
- Filter by age range, occupation, location
- View detailed profile information
- Save/favorite profiles for offline access

### Additional Features
- Dark/Light mode toggle
- Logout functionality
- Offline access to saved profiles

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI Toolkit | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| State Management | StateFlow |
| Dependency Injection | Dagger Hilt |
| Networking | Retrofit + OkHttp |
| Local Storage | SQLite Database + SharedPreferences |
| Image Loading | Coil |
| Async Programming | Coroutines |

---

## Project Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK API 33+

### Steps

1. **Clone repository**
```bash
git clone https://github.com/yourusername/profile-discovery-app.git
cd profile-discovery-app
