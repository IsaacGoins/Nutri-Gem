# Project Overview
Build a native Android calorie and macro tracking application using Kotlin and Jetpack Compose. The app will use the Gemini API to calculate nutritional information from user text input.

## Tech Stack & Architecture
*   **UI Framework:** Jetpack Compose.
*   **Design System:** Material Design 3 (MD3). Must implement `dynamicLightColorScheme` and `dynamicDarkColorScheme` to inherit the user's OS-level theme and colors.
*   **Local Storage:** Room Database for health data. `EncryptedSharedPreferences` or `DataStore` for secure API key storage.
*   **Architecture Pattern:** MVVM (Model-View-ViewModel) with a Repository pattern.
*   **Future-Proofing:** Design the data repository layer so that a remote data source (REST API) can easily be added later for self-hosted backend syncing. For now, it remains local-only.
*   **Networking:** The official Google GenAI SDK for Kotlin to handle Gemini API calls.

## Core Features & Requirements

### 1. Home Screen (Dashboard Layout)
*   **Layout Inspiration:** Model the layout after Fitbit or the classic Samsung Health app. It should be a vertically scrollable dashboard consisting of a hero section at the top, followed by stacked widget cards/banners.
*   **Top App Bar:** Include a gear icon in the top right to access the Settings screen.
*   **Hero Section (Macros & Calories):** Front and center, implement a large pie chart showing the daily breakdown of Protein, Carbs, and Fats, alongside the total calorie count vs. the daily goal. 
    *   *Action:* Clicking this entire hero section routes the user to the "Calories & Macros Detail" page.
*   **Water Banner/Widget:** A distinct card below the hero section displaying the total daily water intake.
    *   *Action:* Clicking this widget routes the user to the "Water Detail" page.
*   **Floating Action Buttons (FABs):** 
    *   *Primary FAB:* A prominent, standard FAB anchored to the bottom right for "Add Meal".
    *   *Secondary FAB (Water Speed Dial):* Positioned to the left of the primary FAB, implement an expanding "Speed Dial" menu using the modern Compose M3 `FloatingActionButtonMenu` and `ToggleFloatingActionButton` components. 
    *   *Water FAB Behavior:* When tapped, it should animate and expand upwards to reveal `FloatingActionButtonMenuItem`s (bubbles). Provide preset options (e.g., +8oz, +16oz) and one option for "Custom Amount," which triggers a small numeric input dialog.

### 2. Settings & API Key Management
*   **Settings Screen:** A dedicated page accessed from the Home Screen's Top App Bar.
*   **API Key Input:** Provide a text field for the user to paste their Gemini API key. 
*   **Security:** Save this key locally using Android's encrypted storage so the API key persists between sessions but remains secure. The ViewModel handling the API call must read the key from this local storage before executing a request.

### 3. Detail & Analytics Pages (Drill-Downs)
*   **Calories & Macros Detail Page:** Accessed by tapping the hero pie chart.
    *   Contains the Meal History (scrollable list grouped by day, showing logged meals and their items/macros, with swipe-to-delete).
    *   Contains historical analytics (line or bar graphs plotting total calories and macros over time).
*   **Water Detail Page:** Accessed by tapping the water banner.
    *   Contains a chronological log of water intake events.
    *   Contains historical analytics (graphs plotting daily water intake trends over time).

### 4. Add Meal Flow (AI Integration)
*   **Input UI:** A dynamic list of items.
    *   Each item row needs a plain text field (e.g., Chicken Breast), a quantity input (numeric), and a unit input.
    *   The unit input must be a dropdown menu (g, oz, cups, etc.) that also allows the user to type a custom string if their unit is not listed.
    *   Include an Add Item button to dynamically append new rows to the current meal.
*   **Submission & AI State Machine:**
    *   When the user taps Calculate, bundle the items into a structured prompt and send it to the Gemini API.
    *   The UI must show a loading state.
    *   **Resolution handling:**
        *   *Success:* If Gemini has enough info, it returns a JSON object of the calories and macros. Display a confirmation screen for the user to save the data to the Room DB.
        *   *Clarification Needed:* If Gemini needs more info, the API should return a clarifying question. The UI must render a modal/dialog displaying Gemini's question and providing a text box for the user to answer, re-triggering the calculation flow.

## Build & Export Instructions
*   Configure the `build.gradle.kts` files to support standard APK generation.
*   Ensure the Android Manifest includes necessary permissions (e.g., `INTERNET` for the Gemini API).

## API Integration & Structured Outputs
*   **System Instruction:** Configure the Gemini API call with a strict system prompt to handle the routing between successful calculations and clarification loops.
    *   *System Prompt:* You are a precise nutrition API. The user will provide meal items. If the items are detailed enough to estimate accurately, return a status of `success` and the calculated data. If the user provides something too vague, return a status of `needs_clarification` and ask for specifics.
*   **JSON Schema Enforcement:** Use the official SDK capabilities to pass a JSON schema to ensure the output is strictly typed. The API request must include `response_mime_type = "application/json"` and enforce the following exact schema using the `response_schema` parameter:

```json
{
  "type": "object",
  "properties": {
    "status": {
      "type": "string",
      "enum": ["success", "needs_clarification"],
      "description": "Return success if macros can be estimated. Return needs_clarification if crucial details are missing."
    },
    "clarification_question": {
      "type": "string",
      "description": "If status is needs_clarification, ask the user for the missing details."
    },
    "data": {
      "type": "object",
      "description": "If status is success, provide the calculated nutritional data.",
      "properties": {
        "meal_name": { "type": "string" },
        "total_calories": { "type": "integer" },
        "macros": {
          "type": "object",
          "properties": {
            "protein_g": { "type": "integer" },
            "carbs_g": { "type": "integer" },
            "fat_g": { "type": "integer" }
          },
          "required": ["protein_g", "carbs_g", "fat_g"]
        },
        "items": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "name": { "type": "string" },
              "calories": { "type": "integer" }
            },
            "required": ["name", "calories"]
          }
        }
      },
      "required": ["meal_name", "total_calories", "macros", "items"]
    }
  },
  "required": ["status"]
}