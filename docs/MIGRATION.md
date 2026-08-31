# Migration plan — a-java-ide (phone-first)

Branch: `attempt_updating_codebase`

Reference apps that already run on your phone:
- [Conductino-Android](https://github.com/ojilon/Conductino-Android)
- [Wayer](https://github.com/ojilon/Wayer)

## Strategy

Delete old weight instead of porting everything.

You only need **edit / compile / run Java on the device**. You do **not** need the 2018 on-device APK builder (AOSP tree), Play billing, or Crashlytics.

## Status

| Area | Status |
|------|--------|
| Root Gradle (plugins DSL, wrapper 8.x, properties) | Done |
| Drop `:aosp:*` from settings | Done |
| Drop Fabric / Firebase / google-services / IAB from `:app` | Done |
| Module `build.gradle` → AGP 8 shape + AndroidX | Done (remaining modules) |
| Java sources still import `android.support.*` | **Next** — fix compile errors as they appear |
| Purchase / Premium / Crashlytics call sites in Java | **Next** — stub or delete |
| Physical delete of `aosp/` folder from git | Optional (already out of the build) |
| Functional-style core + thin Activities | After green build |

## What was removed from the build graph

| Removed | Why |
|---------|-----|
| `:aosp:*` (builder, lint, sdklib, …) | On-device APK packaging; huge & ancient |
| Fabric / Crashlytics / Firebase | 2018 analytics; not needed to run on your phone |
| `google-services` plugin + json | Same |
| In-app billing (`anjlab`, `purchase/*`) | Premium SKU; not needed for local IDE |
| `rate-this-app` | Store fluff |
| Maven `maven-project` / old Support deps in compiler | Pulled by AOSP path |

## What stays

```
:app
:common  :treeview  :androidlogcat
:jdk-1_7  :dx  :lib-android-compiler  :lib-google-java-format
:lib-decompiler  :bouncycastle  :lib-n-ide-release-10
```

`lib-n-ide-release-10` is a prebuilt AAR (editor core). Keep it until you replace that surface.

## Expected next compile errors

After this cut, Gradle should resolve modern plugins, but Java will still reference:

1. `android.support.*` → change to `androidx.*`
2. Crashlytics / Firebase APIs in `JavaApplication` etc. → remove calls
3. `InAppPurchaseHelper` / `Premium` → stub `isPremium() = true` or delete UI gates
4. Any code that called into `com.android.builder` (AOSP) for APK build → gate or remove “Build APK” menu actions

Fix those file-by-file; do not re-add AOSP unless you explicitly want APK-on-phone again.

## Target module shape (already applied)

```gradle
plugins {
    id 'com.android.library' // or application
}

android {
    namespace '…'
    compileSdk 36
    defaultConfig {
        minSdk 26
        targetSdk 34
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
```

## Changelog

- **2026-08-31** — Root Gradle modernized.
- **2026-08-31** — Phone-first cut: AOSP out of graph, app deps cleaned, all remaining modules on AndroidX/AGP 8 shape.
