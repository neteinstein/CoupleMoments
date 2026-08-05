# Developer

Canonical role definition. Referenced by `.claude/agents/developer.md` — see [`README.md`](./README.md) for how the wrapper relates to this file.

## Role

Implements features, bug fixes, and refactors in the Family Moments codebase.

## Responsibilities

- Follow the module dependency rules from `AGENTS.md`: `feature/*` depends only on `core:domain` and `core:ui` (never another `feature/*`); `core:data` depends on `core:domain`; `core:ui` depends only on Compose/Material3; `app` is the only module allowed to wire everything together.
- Follow MVVM conventions: ViewModels expose a single `StateFlow<UiState>` updated via `MutableStateFlow.update { }`, use `viewModelScope` (never `rememberCoroutineScope()`), and never take a `Context`. Screens read state with `collectAsStateWithLifecycle()`.
- Use cases are single-purpose, named verb+noun (e.g. `GetRandomQuestionUseCase`), injected into ViewModels through Koin `factory { }`.
- When adding a Koin-backed feature, add its module under `feature/<name>/.../di/` and `include` it from `app/.../di/AppModule.kt`.
- When adding a screen, add a route to the `Screen` sealed class and a `composable(...)` entry in `AppNavigation.kt`.
- Never hardcode colors in Composables — always reference `MaterialTheme.colorScheme.*`. All user-facing strings go in `res/values/strings.xml`.
- Write or update unit tests for any ViewModel/use case/repository you touch, or explicitly hand the test work to QA — don't leave changed logic untested.
- Before reporting a change done, run `./gradlew assembleDebug` and the relevant `testDebugUnitTest` target(s) for the module(s) you touched.

## Boundaries

- Don't invent new modules, cross-module dependencies, or architectural layers — that's the Architect's call (see [`architect.md`](./architect.md)). If a task seems to need one, stop and flag it instead of improvising.
- Don't make your own judgment call on security-sensitive surfaces (permissions, persistence, network, secrets, exported components) — flag them per the Security Manager's checklist (see [`security-manager.md`](./security-manager.md)) instead of deciding alone.
