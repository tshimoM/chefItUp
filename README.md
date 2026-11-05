ChefItUp is a Android recipe finder app which allows for users to discover, save, plan and cook based on their dietary preferences, available ingredients and skill level. 
Purpose
ChefItUp is to help users discover, save and plan meals. It helps reduce food waste as the cook with what you have feature gives users recipes based off of the food they have entered as the food they have. It simplifies meal planning as it has a weekly meal planner with an automated shopping list. It accommodates a lot of people as it supports multiple languages, has light and dark mode for different eye sensitivities and it also work offline. Its main features are: Register and Log in, Single Sign-On(SSO), Search and filtering, favourites, Weekly meal planner, shopping lists, Settings, Real time notifications, Multi-language support and offline mode. Its innovative features are: Cook with what I have, Gamification, Automated shopping list, South African language support, Offline mode with sync
Design considerations
The design follows a warm flow using a bold orange colour with a white/cream background which goes along with the flame in the logo. We used clear image focused recipe cards which show the meal, its cooking time and rating. It includes a five tab navigation panel at the bottom of the screen which buttons labelled :”Home”, “Search”, “Meal Plan”, “Saved” and “Profile” which flows throughout the whole app and every page. 
 

Github and Github actions
https://github.com/tshimoM/chefItUp
We used one shared repository where every team member can view and add meaningful commits. 
The pipeline runs automatically with every push and pull request to the main. The pipeline checks the code and sets us JDK 17, then gives permission to gradlew to execute and run unit tests suite then assembles a debug APK
 <img width="723" height="1600" alt="WhatsApp Image 2026-11-05 at 17 05 51 (1)" src="https://github.com/user-attachments/assets/c865e887-c3d3-48e5-9596-69af24a386d4" />
<img width="723" height="1600" alt="SSO" src="https://github.com/user-attachments/assets/ae8c7bf3-f0bd-4092-9555-98d7763a61e0" />
<img width="1080" height="2388" alt="shoppinglist" src="https://github.com/user-attachments/assets/0a8e3f39-f920-4f6c-b13a-fd656ec4fdf3" />
<img width="723" height="1600" alt="settings" src="https://github.com/user-attachments/assets/cbbbf174-2144-47a8-b2fd-703cfc6a7a4c" />
<img width="1080" height="2388" alt="search2" src="https://github.com/user-attachments/assets/d45a9550-c1a1-4cb4-90b0-73e7ce8d73df" />
<img width="723" height="1600" alt="search" src="https://github.com/user-attachments/assets/06283e9a-c67f-4903-8846-419948dd7884" />
<img width="1080" height="2388" alt="savedpage" src="https://github.com/user-attachments/assets/d9dd9178-b237-4d40-8f8e-7824df6265c2" />
<img width="723" height="1600" alt="registration" src="https://github.com/user-attachments/assets/7fe3d81e-15b0-4d71-924b-7d811983fefc" />
<img width="723" height="1600" alt="profile" src="https://github.com/user-attachments/assets/5ea7a2f1-42de-4c2b-be35-054b4c36b1e8" />
<img width="1080" height="2388" alt="mealplanner" src="https://github.com/user-attachments/assets/bed4a4be-bb62-40ea-a633-57037c5016f8" />
<img width="723" height="1600" alt="homepage" src="https://github.com/user-attachments/assets/fd10b65c-13a7-4ff3-b5d1-cd868d0dd110" />
<img width="1080" height="2388" alt="cookwithwhatyouhave" src="https://github.com/user-attachments/assets/fa0294f3-ec4d-48d3-af1b-f8af1f98962a" />

YouTube video Link
https://youtu.be/KD_zKPh6Tbo?si=fIYGMzN87GvOgK7i



# Chef it Up (Android)

Kotlin + Jetpack Compose recipe app for South African kitchens. Talks to the local **ChefItUp.Api** middle layer (Spoonacular keys stay off-device).

## Requirements

- Android Studio Ladybug+ / AGP 8.7
- JDK 17
- Emulator API 24+ (or physical device)
- Optional: [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0) to run `../ChefItUp.Api`
- Optional: Firebase project (`app/google-services.json` is required for Auth / Firestore / FCM)

## Run the API (recommended)

```powershell
cd C:\Users\Simiso01\Downloads\ChefItUp\ChefItUp.Api
dotnet run --urls http://0.0.0.0:5080
```

Confirm it is up: open http://localhost:5080/health in a browser.

Emulator reaches the host at `http://10.0.2.2:5080/` (already set as `BuildConfig.API_BASE_URL`).

Physical device: set `API_BASE_URL` in `app/build.gradle.kts` to your PC LAN IP, e.g. `http://192.168.1.20:5080/`.

Without a Spoonacular key the API (and app) serve rich mock recipes.

## Configure API key (optional)

If you set `ChefItUp:ApiKey` on the server, mirror it in the app:

```kotlin
buildConfigField("String", "API_KEY", "\"your-shared-secret\"")
```

The app sends `X-ChefItUp-Key` automatically when `API_KEY` is non-empty.

## Network / HTTPS

- Debug: cleartext allowed only for `10.0.2.2`, `localhost`, `127.0.0.1` via `res/xml/network_security_config.xml`.
- Release: minify + shrink enabled; point `API_BASE_URL` at your HTTPS host.

## Features

- Auth (email/password + Google SSO), onboarding, search, cook-with-ingredients
- Meal planner, shopping list, cooking mode (timer + TTS)
- XP / chef levels / badges
- Offline-first Room cache: favourites, meal plan, shopping, settings, recipes, and profile photo queue; auto-sync on reconnect (WorkManager + connectivity observer)
- Firebase Firestore (NoSQL) read + write for profile, gamification, favourites, FCM tokens
- Firebase Storage (blob) for profile photo upload/download
- Push notifications via Firebase Cloud Messaging
- Meal reminder & recipe recommendation notifications (Settings toggles)
- Languages: English, Afrikaans, isiZulu, Sesotho

## Project layout

```
app/src/main/java/com/chefitup/app/
  presentation/   # Compose UI
  domain/         # models + repository contracts
  data/           # Room, Retrofit, Firebase, repos
  notifications/  # FCM + reminder workers
```

## Tests

```powershell
.\gradlew.bat test
```

GitHub Actions runs `./gradlew test` and `assembleDebug` on every push to `main` / `master`.

## PoE demo tips

1. Register / log in — passwords are hashed by Firebase Auth (never stored in the app).
2. Google SSO from the login screen.
3. Change settings (theme, language, diet, notification toggles).
4. Profile → change photo — uploads to Firebase Storage; URL saved in Firestore.
5. Airplane mode → favourite a recipe / edit shopping list → go online → sync.
6. In Firebase Console show Auth users, Firestore `users/{uid}`, Storage `users/{uid}/profile.jpg`, and FCM token under `users/{uid}/meta/fcm`.
