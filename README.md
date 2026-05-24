# Anime Tracker ⛩️

A native Android application designed to track currently airing and trending anime. Built with a sleek, immersive UI inspired by Tachiyomi/Mihon, this app leverages the AniList GraphQL API to provide real-time schedules and trending data.

## ✨ Features

* **Immersive UI:** A full edge-to-edge Jetpack Compose interface featuring swipeable horizontal pagers, gradient image cards, and a custom bottom navigation bar.
* **AniList API Integration:** Fetches real-time trending anime and detailed airing schedules using GraphQL.
* **Custom Watchlist & Categories:** Save anime locally and organize them into custom, user-defined folders using Room Database.
* **Live Countdown Timers:** View live-ticking countdowns for upcoming episodes directly on the anime details page.
* **Exact Push Notifications:** Utilizes Android's `AlarmManager` to send precise push notifications (with live countdowns) the exact moment a saved episode airs.
* **Dynamic Theming:** Built-in Light/Dark mode toggles and app preferences managed securely via Jetpack DataStore.
* **Offline Support:** Browse your saved watchlist and custom categories even without a network connection.

## 🛠️ Tech Stack

This project is built using modern Android development standards and Clean Architecture (Presentation, Domain, Data layers):

* **UI:** Jetpack Compose, Material 3, Coil (Image Loading)
* **Language:** Kotlin
* **Architecture:** MVVM (Model-View-ViewModel)
* **Dependency Injection:** Dagger Hilt
* **Networking:** Retrofit, OkHttp, GraphQL
* **Local Database:** Room
* **Local Storage:** Preferences DataStore
* **Asynchronous Programming:** Coroutines & StateFlow

## 🚀 Getting Started

### Prerequisites
* Android Studio (Latest Version)
* Minimum SDK: 24 (Android 7.0)
* Target SDK: 34 (Android 14)

### Installation
1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/anime-tracker.git](https://github.com/yourusername/anime-tracker.git)
