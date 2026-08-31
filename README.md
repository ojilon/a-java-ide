# a-java-ide

Java N-IDE — a Java (and Android) IDE that runs on Android.

You can edit, compile (JDK 1.7 toolchain), run, format, and package Java projects on-device. The project also embeds tooling for Android APK builds (aapt, dx, signer, etc.).

This branch (`attempt_updating_codebase`) modernizes the **Gradle configuration** and documentation toward the style used in [Conductino-Android](https://github.com/ojilon/Conductino-Android) and [Wayer](https://github.com/ojilon/Wayer). Full module-by-module AndroidX / AGP 8 migration and a more functional Java core are still in progress.

## Quick links

- Migration status & plan: [docs/MIGRATION.md](docs/MIGRATION.md)
- Contributing: [CONTRIBUTING.md](CONTRIBUTING.md)
- Original wiki (tutorials): [wiki/](wiki/)

## What this project includes

| Tool | Role |
|------|------|
| Javac (JDK 1.7) | Compile Java sources |
| Google Java Format | Code formatter |
| Dx | Dex conversion for Dalvik/ART |
| Aapt / APK builder | Android packaging (legacy path) |
| Decompiler | Class / jar inspection |
| Android Logcat UI | Device log viewing |

## Prerequisites (target after migration)

- Android Studio / SDK command-line tools
- Gradle **8.x** (wrapper is configured for 8.14.5)
- JDK 17 for the Gradle toolchain (app still targets older language level until modules are updated)
- NDK only if you re-enable native pieces later

> **Important:** Root Gradle files have been updated to a modern layout. Individual modules still use Support Library, old `apply plugin`, and AGP 3-era patterns. A clean `./gradlew assembleDebug` will **not** succeed until module `build.gradle` files are migrated. See [docs/MIGRATION.md](docs/MIGRATION.md).

## Build (once modules are migrated)

```bash
./gradlew assembleDebug
./gradlew :app:testDebugUnitTest
```

## Project layout (high level)

```
app/                    Main Android application
common/                 Shared utilities
treeview/               File / project tree UI
androidlogcat/          Logcat viewer
bouncycastle/           Crypto helpers
jdk-1_7/                Bundled JDK 1.7 toolchain pieces
dx/                     Dex tooling
aosp/                   AOSP-derived build / lint / SDK libraries (legacy)
lib-*/                  Decompiler, compiler, formatter, release helpers
wiki/                   Tutorials and images
docs/                   Migration and developer notes
```

## Code direction (Java)

Goal on this branch:

- Prefer **functional** style for pure logic (parsing, transforms, pure data stores).
- Keep **OOP / Android framework types** only at the boundary that talks to XML layouts, Activities, Views, and the Android lifecycle.
- Match the clarity and small surface area of Conductino / Wayer Java (managers, stores, thin Activities).

See `docs/MIGRATION.md` for concrete steps.

## License

GNU GPL 3.0 — see [LICENSE](LICENSE).

Original work © 2017–2018 Duy Tran Le; this fork continues under the same license.
