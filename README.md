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
