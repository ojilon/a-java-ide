# Contributing to a-java-ide

Thanks for helping modernize this project. The long-term goal is a maintainable Android IDE that follows the same Gradle and documentation conventions as [Conductino-Android](https://github.com/ojilon/Conductino-Android) and [Wayer](https://github.com/ojilon/Wayer).

## Getting started

1. Work on branch `attempt_updating_codebase` (or open a PR against it).
2. Read [docs/MIGRATION.md](docs/MIGRATION.md) before large Gradle or dependency changes.
3. Prefer small, focused commits and PRs.

## Local setup (target)

- Android SDK + build-tools
- JDK 17 for Gradle
- Use the project wrapper: `./gradlew ...`

Until module migration is complete, expect build failures; that is expected on this branch.

## Code style

### Java

- Prefer pure functions and immutable data for non-UI logic.
- Restrict heavy OOP / inheritance to Android UI boundaries (Activity, Fragment, Adapter, custom View, data-binding adapters).
- Prefer composition (managers, stores, controllers) over deep class hierarchies — same spirit as Conductino’s `*Store`, `*Manager`, thin Activities.
- Prefer AndroidX packages over `android.support.*` once a module is migrated.

### Gradle

- Root uses plugins DSL + `pluginManagement` / `dependencyResolutionManagement`.
- Module scripts should eventually use `plugins { id 'com.android.application' }` (or library) and `namespace`, not `apply plugin` + Support Library.
- Do not reintroduce `jcenter()` or Fabric / Crashlytics 2.x without a clear need.

### XML / resources

- Prefer themes, colors, and dimension tokens similar to modern Conductino layouts.
- Avoid deprecated attribute patterns when touching layouts.

## Pull requests

- Title and description should say what migrated (e.g. “migrate :treeview to AndroidX + AGP 8”).
- Include a short test plan (even if “compile only” while the rest is broken).
- Update `docs/MIGRATION.md` when you finish a module or change the plan.

## Documentation

- Add a short README inside any new top-level feature directory.
- Keep user-facing behavior changes reflected in the root README.

## License

Contributions are under the project’s GNU GPL 3.0 license (see LICENSE).
