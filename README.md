# Nutri-Gem (Calorie Tracker)

Nutri-Gem is an intelligent, modern Android application built to simplify health and nutrition tracking. Utilizing Google's Gemini AI, the app automatically estimates the nutritional content of your meals from natural language descriptions or images, and cross-references data with the FDA FoodData Central API for high accuracy. 

It provides an all-in-one dashboard to track your daily macronutrients, water intake, and weight goals with beautiful, interactive visualizations.

---

## 🚀 Features

* **AI-Powered Multi-Tiered Meal Logging:** Uses the `gemini-3.1-flash-lite` model to intelligently analyze natural language descriptions. The app automatically groups ingredients logically into meals and dynamically calculates precise portion sizes (e.g., "half a cup", "3 pounds").
* **FDA Data Cross-Referencing & Scaling:** The system uses a multi-tiered architecture for supreme accuracy:
  * **Tier 1 (Composite Meals):** Looks up full meals in the FDA FoodData Central API.
  * **Tier 2 (Ingredient Breakdown):** If the meal is complex, the app forces an ingredient-level breakdown and queries the FDA for each distinct component.
  * **Intelligent Portion Scaling:** FDA data is algorithmically scaled to match your exact requested portion size by cross-referencing Gemini's dynamic calorie estimations with the FDA's highly-accurate macro ratios. Items successfully verified by the FDA receive a visual "FDA Verified" badge in the UI.
* **Daily Macro Dashboard:** A beautiful, quick-glance UI that shows a circular progress ring of your daily caloric intake alongside your protein, carb, and fat goals.
* **Water Tracking:** Quickly log water intake throughout the day (in ounces). Features a dedicated screen with a daily intake summary.
* **Weight Analytics & Goals:** Log your weight daily, set a target weight goal, and visualize your progress over time on an interactive, customized line graph.
* **Local First & Secure:** User data (meals, water, weight) is securely stored locally on your device using Room Database. API keys are safely managed using Android's EncryptedSharedPreferences.
* **Dynamic Theming:** Built entirely with Jetpack Compose using Material Design 3 guidelines, supporting dynamic system colors and fully adapting to both Light and Dark modes.

---

## 🛠 Tech Stack

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Local Storage:** [Room Database](https://developer.android.com/training/data-storage/room) (SQLite)
* **Asynchronous Programming:** Kotlin Coroutines & StateFlow
* **Navigation:** Jetpack Navigation Compose
* **Security:** `androidx.security.crypto.EncryptedSharedPreferences`
* **External APIs:**
  * [Google Gemini API](https://aistudio.google.com/)
  * [FDA FoodData Central API](https://fdc.nal.usda.gov/api-guide.html)

---

## 📱 How to Use

### 1. Initial Setup
To get the most out of the app's AI features, you will need to provide API keys:
1. Tap the **Settings** icon (gear) on the top right of the Home screen.
2. Enter your **Gemini API Key**. (You can get one from Google AI Studio).
3. Enter your **FDA API Key**. (You can get one from the FDA Data API website).
4. Tap **Save**. Your keys are securely stored locally on your device.

### 2. Tracking Meals
1. On the Home screen, tap the **"+"** Floating Action Button.
2. Enter a description of your meal (e.g., "Two scrambled eggs and a slice of wheat toast") or provide a photo.
3. The AI will analyze the meal and fetch exact nutritional metrics.
4. Review the estimated macros (Calories, Protein, Carbs, Fat). You can manually edit these if needed before tapping **Save**.

### 3. Tracking Water
1. Tap the **Water** card on the Home screen.
2. Tap the **Add Water** button.
3. Input the amount of water you drank in ounces.
4. The screen will update to show your total intake for the day.

### 4. Tracking Weight & Goals
1. Tap the **Weight** card on the Home screen to open the Weight Analytics dashboard.
2. **Set a Goal:** Tap the banner at the top of the screen to input your target weight.
3. **Log Weight:** Tap the **Add Weight** button to record a new entry.
4. Watch your progress over time via the interactive line graph that maps your historical entries against your goal line.

---

## 🏗 Project Structure
* `data/local`: Contains Room Database configurations, Entities (Meals, Water, Weight), and DAOs.
* `data/network`: Contains clients and network configurations for interacting with Gemini and FDA APIs.
* `data/repository`: The single source of truth for data access, abstracting the local database and API calls.
* `data/security`: Contains `SecureStorage` for encrypting and persisting sensitive data like API keys.
* `ui/screens`: Contains all Jetpack Compose screens (Home, Add Meal, Water Detail, Weight Detail, Settings).
* `ui/viewmodels`: Contains the `MainViewModel` handling state management and business logic.
* `ui/theme`: Contains typography, color definitions, and dynamic theme handling for Material 3.
