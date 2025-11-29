# Repository Guidelines

## Project Structure & Module Organization
- Root Gradle setup lives in `build.gradle`, `settings.gradle`, and `gradle/`. The main Android module is `app/`.
- Application code sits under `app/src/main/java/com/justyn/meow/`, grouped by feature (e.g., `auth/`, `cat/`, `data/`, `util/`). Activities follow the `*Activity` suffix.
- Resources are in `app/src/main/res/` and the manifest in `app/src/main/AndroidManifest.xml`. Do not commit generated assets from `app/build/`.
- Tests: JVM unit tests in `app/src/test/`; instrumented Android tests in `app/src/androidTest/`.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` – compile the app and produce a debug APK.
- `./gradlew installDebug` – build and install the debug build to a connected/emulated device.
- `./gradlew test` – run JVM unit tests under `app/src/test/`.
- `./gradlew connectedAndroidTest` – run instrumented tests on a device/emulator.
- `./gradlew lint` – run Android Lint; fix warnings before opening a PR.

## Coding Style & Naming Conventions
- Java code uses 4-space indentation, braces on new lines for methods/classes, and AndroidX imports where available.
- Package names stay lowercase; classes are PascalCase; methods and fields are camelCase; constants are UPPER_SNAKE_CASE.
- Activity/ViewModel/UI classes end with their role (`LoginActivity`, `CatProfileActivity`); database/helpers match existing patterns (`MeowDbHelper`, `MeowPreferences`).
- Keep resources prefixed by feature (e.g., `cat_profile_*` layouts, `ic_cat_*` drawables). Favor string resources over hardcoded text.

## Testing Guidelines
- Prefer small, deterministic unit tests in `app/src/test/` with names like `CatWikiActivityTest`.
- Instrumented flows (navigation, preferences, DB) belong in `app/src/androidTest/`; name tests `*InstrumentedTest`.
- Use Given/When/Then structure inside tests and clean up shared state (preferences, DB) between cases.
- Run `./gradlew test lint` locally before pushing; add `connectedAndroidTest` when changing UI or storage behavior.

## Commit & Pull Request Guidelines
- Existing history uses short, descriptive messages; continue with imperative, one-line summaries (e.g., `Add cat profile screen`), and include issue IDs when relevant.
- For PRs: describe the change, its motivation, and user-facing impact; attach screenshots for UI changes; list manual/automated tests run (commands above).
- Keep changes focused by feature; avoid committing build outputs or local config (`local.properties`).

## Security & Configuration Tips
- Do not check in secrets or API keys; keep local endpoints in `.env`-style gradle properties if needed and document defaults in code comments.
- `local.properties` should stay local (SDK paths, emulators); verify paths before builds to avoid CI failures.
