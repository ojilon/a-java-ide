# Migration plan — a-java-ide

Branch: `attempt_updating_codebase`

Reference projects:
- [Conductino-Android](https://github.com/ojilon/Conductino-Android) — modern AGP 8, AndroidX, thin Java layer
- [Wayer](https://github.com/ojilon/Wayer) — same Gradle style, multi-module, viewBinding

## Status

| Area | Status |
|------|--------|
| Root `settings.gradle` (pluginManagement, dependencyResolutionManagement) | Done |
| Root `build.gradle` (plugins DSL) | Done |
| `gradle.properties` (AndroidX flags, version props) | Done |
| Gradle wrapper → 8.14.5 | Done |
| Per-module `build.gradle` (AGP 8 + namespace + AndroidX) | **Not started** |
| Remove Support Library / enable Jetifier cleanup | **Not started** |
| Remove Fabric / old Firebase / jcenter | **Not started** |
| Java: functional core, OOP only at XML/Android boundary | **Not started** |
| XML / themes modernization | **Not started** |

## Why the build still fails

Root config now expects AGP 8.x and AndroidX, but modules still contain:

- `apply plugin: 'com.android.application'` + AGP 3-era blocks
- `com.android.support:*` dependencies
- `dataBinding { enabled = true }` old form
- Fabric / Google Services 3.x classpath
- `jcenter()` and other removed repositories inside some modules
- AOSP subprojects written for older Gradle Java plugins

A full green build requires migrating modules one by one (or a larger coordinated PR).

## Recommended order

1. **Leaf libraries first** (no Android UI): `:common`, `:bouncycastle`, pure Java AOSP pieces that can become `java-library`.
2. **Android library modules**: `:treeview`, `:androidlogcat` → AndroidX, `namespace`, `compileSdk 36`.
3. **Tooling modules**: `:dx`, `:jdk-1_7`, `:lib-*` — keep as Java or Android libraries as appropriate.
4. **`:app` last** — largest surface; switch Support → AndroidX, remove Fabric, adopt viewBinding or keep dataBinding in new form, Java 17 `compileOptions`.
5. **Java style pass** after modules compile: extract pure logic from Activities into functions / small stores; leave Activities/Fragments as thin adapters to XML.

## Gradle target shape (per Android module)

```gradle
plugins {
    id 'com.android.library' // or application
}

android {
    namespace 'com.example.module'
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

dependencies {
    implementation 'androidx.appcompat:appcompat:1.7.0'
    // ...
}
```

Match Conductino/Wayer for version numbers where practical.

## Java direction

- **Functional:** pure transforms, validation, path/file helpers, model mapping — static methods or small utility types, prefer immutability.
- **OOP retained:** Activity, Fragment, RecyclerView.Adapter, custom View, Application, anything that must implement Android framework contracts or inflate XML.
- Avoid new deep inheritance trees; prefer composition (`XxxStore`, `XxxManager`) as in Conductino.

## Docs hygiene

When a module is migrated, note it in this file’s status table and add a one-line entry under “Changelog” below.

## Changelog

- **2026-08-31** — Root Gradle + wrapper + properties modernized; README / CONTRIBUTING / this plan added.
