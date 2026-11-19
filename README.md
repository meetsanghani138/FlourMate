# 🌾 FlourMate – On-Demand Flour Mill & Custom Grinding Platform

**FlourMate** is a modern Android application connecting customers directly with local flour mills (Chakki) for fresh flour ordering, custom grain grinding, and home delivery. Built using a **Single-Activity Architecture** (`MainActivity.kt`), **Kotlin**, **Android Material 3 Design**, **Cloud Firestore**, **Firebase Storage**, and **SQLite Room Caching**.

---

## 🌟 Key Features

### 🛒 1. Customer Experience
* **Flour Mills Directory**: Browse nearby verified flour mills with compact cards, ratings, location, and pricing.
* **Product Catalog**: Explore fresh flour varieties (Wheat, Multigrain, Bajra, Besan, Jowar, etc.) with real-time stock and unit package variants.
* **Shopping Cart & Flexible Checkout**:
  * Choose between **Home Delivery** (₹30.00 fee) or **Mill Pickup** (₹0.00 fee).
  * Flexible payment methods (COD, UPI, Card).
* **Live Order Tracking**: Real-time status updates (*Pending ➔ Preparing ➔ Ready for Pickup / Out for Delivery ➔ Delivered*) with OTP verification.
* **Customer Profile**: Manage addresses, contact info, and profile avatars.

### 🏪 2. Flour Mill Owner Dashboard
* **Real-time Analytics**: Live statistics for total sales revenue, weekly net earnings, active pending orders, and total order volume.
* **Product & Inventory Management**: Add, edit, replace photos, and delete flour products in real-time with instant Cloud Storage image upload.
* **Order Management**: Transition incoming customer orders through preparation and fulfillment steps.
* **Mill Profile**: Update mill details, contact phone, operating hours, cover photos, and shop photos.

### 🚴 3. Delivery Partner Portal
* **Delivery Tasks & Routes**: View assigned delivery tasks with step-by-step pickup and drop location addresses.
* **One-Tap Actions**: Direct call customer, start GPS route navigation, and confirm delivery completion.
* **Partner Profile**: Manage vehicle details and driver profiles.

---

## 🏗️ Architecture & Technology Stack

* **Language**: Kotlin 2.x
* **UI Architecture**: Single Activity Pattern (`MainActivity.kt` hosting dynamic screen inflation and back-stack management without Fragments or Jetpack Compose).
* **UI Components**: Android Material Components, ConstraintLayout, RecyclerView, MaterialCardView, ChipGroup.
* **Cloud Database & Synchronization**:
  * **Cloud Firestore**: Real-time multi-device snapshot synchronization for mills, products, orders, and user profiles.
  * **Firebase Storage**: Cloud hosting and HTTPS URL generation for uploaded mill photos, product images, and profile avatars.
  * **Firebase Auth**: Secure user authentication and role-based access control.
* **Local Persistence**: SQLite Room (`FlourMateDatabase`) for offline caching and fast UI rendering.
* **Image Loading & Handling**: `ImageLoader` utility handling file URIs, content streams, and HTTP/HTTPS Firebase Storage URLs with automatic tint removal and `CENTER_CROP` scaling.

---

## 📁 Directory Structure

```text
E:/ANDROIDPROJECT
├── app
│   ├── src
│   │   └── main
│   │       ├── java/com/marwadiuniversity/flourmate
│   │       │   ├── adapters/          # RecyclerView Adapters (ProductAdapter, MillAdapter, OrderAdapter, etc.)
│   │       │   ├── database/          # SQLite Room Database, DAOs, & Migrations
│   │       │   ├── models/            # Data Models (User, Mill, Product, Order, CartItem, etc.)
│   │       │   ├── network/           # FirebaseSyncManager, ApiClient, ApiService
│   │       │   ├── repositories/      # Data Repositories
│   │       │   ├── utils/             # ImageLoader, ImageStorageHelper, ImagePickerHelper, SessionManager
│   │       │   └── MainActivity.kt    # Single Activity Controller hosting all application screens
│   │       ├── res/
│   │       │   ├── drawable/          # Modern outline vector icons & custom shape backgrounds
│   │       │   ├── layout/            # XML screen layouts & component items
│   │       │   ├── menu/              # Role-specific bottom navigation menus
│   │       │   └── values/            # Brand Color Palette (#0D631B, #FAF8F3, #FBC02D), styles & typography
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── README.md
```

---

## 🛠️ How to Run & Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/meetsanghani138/FlourMate.git
   cd FlourMate
   ```

2. **Open in Android Studio**:
   * Open Android Studio (Ladybug / Jellyfish or newer).
   * Select **Open an existing project** and choose the `FlourMate` directory.

3. **Firebase Configuration** *(Optional for Cloud Sync)*:
   * Add your `google-services.json` file inside the `app/` directory.

4. **Build & Run**:
   * Connect an Android device or launch an Emulator.
   * Click **Run** (`Shift + F10`) or execute `./gradlew assembleDebug`.

---

## 👤 Author
* **Meet Sanghani** - [meetsanghani138](https://github.com/meetsanghani138)
* **University**: Marwadi University

---
*Created for FlourMate Android Application.*
