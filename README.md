# ThyroCare

ThyroCare is an Android application designed to support people managing thyroid health with a soft, friendly interface and a practical set of daily tools.

The app includes:
- a welcome, login, and signup flow
- a daily medication and habit tracker
- a cycle and symptom tracker with calendar history
- a nutrition screen with local food guidance and optional live gluten-free recipe results from Spoonacular

## Tech Stack

- Android Studio
- Java
- XML layouts
- View Binding
- Material Components
- SharedPreferences for local storage
- Spoonacular API for recipe suggestions

## Project Details

- Package name: `com.example.thyrocare`
- Minimum SDK: 24
- Target SDK: 36
- Java version: 11
- Build system: Gradle Kotlin DSL

## Main Screens

### Welcome Screen

Files:
- [app/src/main/java/com/example/thyrocare/MainActivity.java](app/src/main/java/com/example/thyrocare/MainActivity.java)
- [app/src/main/res/layout/activity_main.xml](app/src/main/res/layout/activity_main.xml)

What it does:
- shows the ThyroCare branding
- displays the main login and signup buttons
- opens the login screen or signup screen when tapped

### Login Screen

Files:
- [app/src/main/java/com/example/thyrocare/LoginScreen_main.java](app/src/main/java/com/example/thyrocare/LoginScreen_main.java)
- [app/src/main/res/layout/activity_login_screen_main.xml](app/src/main/res/layout/activity_login_screen_main.xml)

What it does:
- accepts username or email and password
- checks for empty fields and shows inline error popups
- validates the login against the locally saved account
- opens the dashboard if the credentials are correct

### Signup Screen

Files:
- [app/src/main/java/com/example/thyrocare/SignupActivity.java](app/src/main/java/com/example/thyrocare/SignupActivity.java)
- [app/src/main/res/layout/activity_signup.xml](app/src/main/res/layout/activity_signup.xml)

What it does:
- collects full name, email, password, and confirm password
- checks that the fields are valid
- saves the account locally on the device
- opens the dashboard after successful signup

### Dashboard

Files:
- [app/src/main/java/com/example/thyrocare/home.java](app/src/main/java/com/example/thyrocare/home.java)
- [app/src/main/res/layout/activity_home.xml](app/src/main/res/layout/activity_home.xml)

What it does:
- hosts the main fragments in one dashboard shell
- uses bottom navigation to switch between Home, Cycle, and Nutrition
- keeps the user in the same app flow after login or signup

### Home Fragment

Files:
- [app/src/main/java/com/example/thyrocare/HomeFragment.java](app/src/main/java/com/example/thyrocare/HomeFragment.java)
- [app/src/main/res/layout/fragment_home.xml](app/src/main/res/layout/fragment_home.xml)

What it does:
- shows today's date and a welcome greeting
- tracks daily tasks such as medication, movement, and food habits
- resets tasks daily while keeping total points saved
- updates the theme color as the user earns more points

### Cycle Fragment

Files:
- [app/src/main/java/com/example/thyrocare/CycleFragment.java](app/src/main/java/com/example/thyrocare/CycleFragment.java)
- [app/src/main/res/layout/fragment_cycle.xml](app/src/main/res/layout/fragment_cycle.xml)
- [app/src/main/java/com/example/thyrocare/CycleLog.java](app/src/main/java/com/example/thyrocare/CycleLog.java)

What it does:
- logs period start and end dates
- stores flow level, symptoms, and written notes
- shows monthly history
- lets the user tap a calendar date and view older notes for that date

### Nutrition Fragment

Files:
- [app/src/main/java/com/example/thyrocare/DietFragment.java](app/src/main/java/com/example/thyrocare/DietFragment.java)
- [app/src/main/res/layout/fragment_diet.xml](app/src/main/res/layout/fragment_diet.xml)
- [app/src/main/java/com/example/thyrocare/SpoonacularClient.java](app/src/main/java/com/example/thyrocare/SpoonacularClient.java)
- [app/src/main/java/com/example/thyrocare/FoodDirectoryItem.java](app/src/main/java/com/example/thyrocare/FoodDirectoryItem.java)
- [app/src/main/java/com/example/thyrocare/RecipeSuggestion.java](app/src/main/java/com/example/thyrocare/RecipeSuggestion.java)

What it does:
- searches a built-in thyroid-friendly food directory
- loads gluten-free recipe ideas from Spoonacular
- shows recipe cards with images and details
- opens the full recipe link when the user taps a card

## Shared App Logic

### Local Storage

File:
- [app/src/main/java/com/example/thyrocare/AppStorage.java](app/src/main/java/com/example/thyrocare/AppStorage.java)

What it stores:
- account name
- email
- password
- display name
- total points
- daily task state
- yearly counters
- cycle logs

### Theme Logic

File:
- [app/src/main/java/com/example/thyrocare/ThemePalette.java](app/src/main/java/com/example/thyrocare/ThemePalette.java)

What it does:
- changes the app colors based on the user's points
- keeps the visual style soft and consistent

## Resources

### Colors

File:
- [app/src/main/res/values/colors.xml](app/src/main/res/values/colors.xml)

The app uses a lavender and sage palette, including:
- plum
- pink
- lilac
- sage
- shell background

### Strings

File:
- [app/src/main/res/values/strings.xml](app/src/main/res/values/strings.xml)

This file stores:
- button labels
- screen titles
- hints
- validation messages
- helper text

### Bottom Navigation

File:
- [app/src/main/res/menu/bottom_nav_menu.xml](app/src/main/res/menu/bottom_nav_menu.xml)

It defines:
- Home
- Track Cycle
- Nutrition

## How To Run

1. Open the project in Android Studio.
2. Let Gradle sync finish.
3. Run the app on an emulator or Android device.
4. The first screen should be the welcome page.

## How To Test The App

### 1. Test the welcome screen

- Open the app.
- Confirm the ThyroCare branding appears.
- Tap Login to go to the login screen.
- Tap Signup to go to the signup screen.

### 2. Test signup

- Enter a full name, email, password, and confirm password.
- Use at least 6 characters for the password.
- Make sure both password fields match.
- Tap Create account.
- Confirm the dashboard opens.

### 3. Test login

- Return to the login screen.
- Enter the saved email or full name.
- Enter the correct password.
- Tap Continue to dashboard.
- Confirm the app opens the dashboard.
- Try leaving a field empty to see the error popup.

### 4. Test the home dashboard

- Confirm today's date appears.
- Tap the daily habit checkboxes.
- Check that points increase.
- Open the app again and confirm saved points remain.
- Refresh or reopen the screen on a new day to see task reset behavior.

### 5. Test the cycle tracker

- Open Track Cycle from bottom navigation.
- Select a start date and end date.
- Choose a flow level.
- Pick one or more symptoms.
- Write notes.
- Tap Save cycle entry.
- Use the calendar to tap a date and view old notes.

### 6. Test the nutrition screen

- Open Nutrition from bottom navigation.
- Search for a food or nutrient.
- Tap Load gluten-free recipes.
- If no API key is set, the screen should show the setup message.
- If the Spoonacular key is added in `local.properties`, recipe cards should appear.

## Spoonacular Setup

To enable live recipes, add this line to `local.properties`:

```properties
spoonacular.apiKey=YOUR_KEY_HERE
```

Then rebuild the project.

## Notes

- Login is stored locally on the device only.
- Cycle logs and points are also stored locally.
- There is no cloud sync yet.
- If you uninstall the app, local data will be cleared from that device.

## Suggested Screenshots For Submission

- Welcome screen
- Login screen with validation popup
- Signup screen
- Home dashboard with points and daily tasks
- Cycle tracker with calendar and notes
- Nutrition screen with recipe cards

## License

This project is for educational use.
