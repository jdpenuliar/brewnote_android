# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Full build (compile + test + lint)
./gradlew build
```

## Required Setup

Create `local.properties` in the project root with:
```properties
CONVEX_URL=<your-convex-deployment-url>
CLERK_PUBLISHABLE_KEY=<your-clerk-publishable-key>
```

These are injected as `BuildConfig.CONVEX_URL` and `BuildConfig.CLERK_PUBLISHABLE_KEY` via `buildConfigField` in `app/build.gradle.kts`.

## Architecture Overview

BrewNote is a personal coffee journaling app built with **Jetpack Compose + Material3**, backed by **Convex** (real-time backend) and **Clerk** (authentication).

### Startup Flow

1. `BrewNoteApp` (Application class) — initializes Clerk with publishable key and creates `ConvexClientWithAuth`
2. `MainActivity` — collects `Clerk.userFlow` as state; renders `BrewNoteNavGraph`
3. `BrewNoteNavGraph` — if user is null → `AuthScreen`; otherwise → main Scaffold with bottom nav

### Layer Structure

- **`data/model/`** — `@Serializable` data classes mapping to Convex documents: `Bean`, `BeanNote`, `BrewNote`, `BrewMethod`, `Equipment`, `Vendor`, `HomeStats`, `PaginationResult<T>`
- **`navigation/`** — `Screen` (sealed class with all routes) + `BrewNoteNavGraph` (NavHost + Scaffold + BottomBar)
- **`ui/{feature}/`** — one package per feature, each with: `*ListScreen`, `*DetailScreen`, `*FormScreen`, `*ViewModel`
- **`ui/components/`** — shared composables: `DetailRow`, `EmptyState`, `RatingRow`, `SkeletonBox`, `SkeletonListItem`, `SectionHeader`
- **`ui/theme/`** — neutral color palette only (no brand colors), Material3 light/dark schemes

### MVVM Pattern

Each feature's ViewModel:
- Extends `AndroidViewModel`
- Accesses `convexClient` via `(application as BrewNoteApp).convexClient`
- Uses `convexClient.subscribe<T>(path, args)` returning `Flow<Result<T>>` for real-time data
- Uses `convexClient.mutation<T>(path, args)` for writes
- Exposes `StateFlow<UiState>` with a sealed interface having `Loading`, `Success`, and `Error` variants

### Navigation

5 top-level tabs: **Home**, **Brews**, **Beans**, **BeanNotes**, **Vendors**

From Home, users can navigate to the **Equipment** and **BrewMethod** library screens (not tabs). Detail/form screens for all entities are pushed onto the back stack and hide the bottom bar.

Routes use typed arguments (e.g., `BrewDetail/{id}`) defined in the `Screen` sealed class.

## Key Dependencies

| Purpose | Library |
|---|---|
| Backend | `dev.convex:android-convexmobile:0.8.0` |
| Auth | `com.clerk:clerk-android-api/ui:1.0.10`, `com.clerk:clerk-convex-kotlin:0.7.0` |
| Navigation | `androidx.navigation:navigation-compose:2.9.0` |
| Serialization | `org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3` |
| Compose BOM | `androidx.compose.bom:2026.02.01` |
| Icons | `androidx.compose.material:material-icons-extended` |