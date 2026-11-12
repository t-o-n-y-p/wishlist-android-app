## Окружение
- Windows 11
- Android Studio Quail 4 | 2026.1.4 Patch 1
- Cline
- llama.cpp
- Qwen3.8-27B-UD-Q4_K_M.gguf
- RTX 3090 (скорость работы около 30 токенов/сек, размер контекстного окна 96к)

За основу взят проект и memory bank домашнего задания 2.

## Промпт 1
```
Transform the existing memory bank into the architecture rules:
- move the README.md file to the .cline/rules folder, other files to the .cline/rules/architecture folder;
- rename README.md to ARCHITECTURE.md;
- reword all known facts in all files into instructions to follow while working on the project.
Also create AGENTS.md file in the project root folder, and instruct agents to follow the new architecture rules.
```
## Результат
Было упущено, что в AGENTS.md также необходимо добавить общее описание проекта.
Также при выполнении задачи агент начал дублировать структуру правил архитектуры в AGENTS.md

## Промпт 1.1
```
Transform the existing memory bank into the architecture rules:
- move the README.md file to the .cline/rules folder, other files to the .cline/rules/architecture folder;
- rename README.md to ARCHITECTURE.md;
- reword all known facts in all files into instructions to follow while working on the project.
Also create AGENTS.md file in the project root folder: instruct agents to follow the new architecture rules, and include a project architecture overview, short and structured, for a person familiar with modern programming in general but not with specifics of Android development:
- purpose;
- key directories;
- entrypoints;
- modules;
- order of reading the code.
Don't repeat .cline/rules/architecture folder structure inside AGENTS.md: instruct agents to read it from .cline/rules/ARCHITECTURE.md.
```
Промпт 1 исправлен, добавлена инструкция сделать краткое описание и не дублировать структуру правил.
## Результат
Созданы архитектурные правила с разбиением на логические блоки, а также общий файл AGENTS.md как точка входа.

## Промпт 2
```
Create the DEFINITION_OF_DONE.md rule file with the following requirements:
- code in the app module is formatted with ktfmt plugin: zero violations
- gradle build task is successful across the entire project
Create a mention of DEFINITION_OF_DONE.md in the AGENTS.md file: all the tasks must comply with it. Don't repeat the requirements there.
Don't change any other files while working on this task.
```
## Результат
Создан критерий готовности задачи: применение линтеров и успешная сборка проекта.

## Промпт 3
```
Create three rule files:
- new functionality;
- bugfixes;
- refactoring.
Each rule file must include an example of prompt (task description for an agent) and response format requirements.
New functionality task response must include at least:
- a comprehensive description on how a new feature relates to existing features;
- a comprehensive technical description of changes;
- a list of core functionalities and child functionalities affected by changes; affected means code files have been modified;
- a list of new architecture rules;
- 5 to 10 most critical cases to verify the changes and for regression testing.
Bugfix task response must include at least:
- a comprehensive technical description of the cause;
- a comprehensive technical description of changes;
- a list of core functionalities and child functionalities affected by changes; affected means code files have been modified;
- 5 to 10 most critical cases to verify the changes and for regression testing.
Refactoring task response must include at least:
- a comprehensive description of what was wrong with initial implementation;
- a comprehensive description of changes and how they help with initial problems;
- a list of core functionalities and child functionalities affected by changes; affected means code files have been modified;
- a list of modified architecture rules, if any;
- 5 to 10 most critical cases for regression testing.
Create a mention of these rules in the AGENTS.md file: all the tasks must comply with corresponding requirements based on task category. Don't repeat the requirements there.
Don't change any other files while working on this task.
```
## Результат
Созданы правила для трех типов задач с примерами постановки задачи и ожидаемым результатом.

## Промпт 4
```
Each list ViewModel re-implements the same NotSet → Loading → Content/Empty/ Error mapping and the same try/catch Throwable → Error handling. Extract this into a single shared helper used by all list ViewModels, without changing any user-visible behavior.
```
Взят напрямую промпт из примера задачи рефакторинга.
## Ответ

All gates pass and the work is complete. Here is the final report for this refactoring task.

### 1. What was wrong with the initial implementation

Each of the three list ViewModels (`UsersFragmentViewModel`, `WishlistsFragmentViewModel`, `GiftsFragmentViewModel`) independently:

- **Duplicated the same 5-state sealed class.** Each file declared its own nested `sealed class DataState` with `NotSet`/`Loading`/`Content`/`Empty`/`Error` data objects — three byte-identical copies. Any change to the state machine (e.g., adding a state) had to be made three times, and Fragments imported three *different* types that were structurally the same, making the code read as if the states could differ.
- **Re-implemented the same `NotSet → Loading → Content/Empty → Error` load choreography.** Each ViewModel's initial load followed the identical sequence: emit `Loading`, run the suspend load in `viewModelScope.launch`, publish the mapped list into `contentState`, emit `Content`/`Empty`, and collapse any `Throwable` into `Error` via a `try/catch` wrapping the whole block. The exception-handling policy ("any `Throwable` ⇒ screen-level `Error`, never crash") was encoded three times — a classic place for one copy to drift (e.g., someone catches only `Exception` and a `CancellationException` on view destruction starts emitting spurious `Error` states).
- **No shared vocabulary for the pattern.** The state machine was an implicit convention; nothing in the code or docs named it, so new list screens (or reviews) had to re-derive the convention by diffing the three ViewModels.

Practical consequence: a 3× bug surface for identical logic, 3× maintenance cost, and zero compile-time guarantee that the three screens agree.

### 2. Changes and how they solve the problems

**New file** — `app/src/main/java/ru/otus/wishlist/fragment/DataState.kt`:
- One shared `sealed class DataState` (NotSet/Loading/Empty/Content/Error), package `ru.otus.wishlist.fragment` — the common parent of the three feature packages.
- `fun <T> MutableLiveData<DataState>.mapToState(items: List<T>)` — the single definition of "non-empty list ⇒ `Content`, empty ⇒ `Empty`".
- `fun <T> MutableLiveData<DataState>.fetchList(scope, contentState, block): Job` — centralizes the full load choreography: emit `Loading` → run the suspending `block` (fetch + map + cache) → publish the returned list into `contentState` → emit `Content`/`Empty` → collapse any `Throwable` to `Error`; returns the launched `Job` so callers can cancel/restart (used for the refresh path in the users flow).

**Modified ViewModels** (the nested `DataState` classes were deleted from each):
- `UsersFragmentViewModel` — initial load and the refresh-with-filter path now call `mDataState.fetchList(viewModelScope, mContentState) { ... }`; the returned `Job` cancels the previous load before starting a new one, exactly as before.
- `WishlistsFragmentViewModel` — the load path uses `fetchList`; the per-row operations (create/edit/delete wishlist) keep their own `viewModelScope.launch` + `try/catch` because they mutate *item-level* state (`WishlistsItem` LiveData) and the cache, not the screen-level `DataState` — deliberately left out of the shared helper.
- `GiftsFragmentViewModel` — uses the shared `DataState` type and `mapToState` in `fillGiftsFromCache()` and the reserve/delete paths, but does **not** use `fetchList`, because per the architecture rules gifts have no fetch of their own (they come from `cache.currentWishlist`). Its idiosyncrasy is preserved verbatim: content is published only when non-empty (`gifts.takeIf { it.isNotEmpty() }`), and `Loading` → `mapToState` ordering is unchanged.

**Modified Fragments** — `UsersFragment`, `WishlistsFragment`, `GiftsFragment` now observe/import the single shared `ru.otus.wishlist.fragment.DataState` instead of their per-ViewModel nested types. The observer bodies (which `when`-branch on the five states) are unchanged.

**Auth layout repair (Definition of Done gate 2)** — `fragment_login.xml` and `fragment_register.xml` carried pre-existing `NotSibling` lint errors that blocked `./gradlew build`: inside the nested `log_in_or_wait_layout` / `register_or_wait_layout` containers, the *second* button constrained its top to `@+id/password_text_input_layout`, which is not a sibling (it is a sibling of the *container*). I replaced those three button constraints (`wait` in login; `register` and `wait` in register) with `app:layout_constraintTop_toTopOf="parent"` — the exact codebase-conventional attribute (all 30+ other top constraints in the project use `..._toTopOf`), and exactly the position ConstraintLayout was already producing at runtime (an unresolvable sibling reference is dropped, leaving the button pinned by its other constraints at the container top, same as the sibling button). The container's own valid `layout_constraintTop_toBottomOf="@+id/password_text_input_layout"` constraint is untouched. (Note: my first attempt used `layout_constraintTop_toTop` — an attribute that does not exist in ConstraintLayout 2.2.2, verified against the AAR's attrs — which aapt2 rejected; the corrected attribute is the one in place now.)

**Behavior intentionally kept unchanged** and how: LiveData's `setValue`/`postValue` equality-deduplication makes any redundant state emission a no-op; the emission *order* at every call site is identical (Loading first, content published before the Content/Empty state, Error last on failure); the Gift suppression of empty-content publishing is preserved (above); the Job-cancel/restart semantics of the users refresh are preserved via the returned `Job`; per-row operations are untouched.

### 3. Affected core and child functionalities

| Core functionality | Child functionality | Modified files |
|---|---|---|
| Users | users list | `UsersFragmentViewModel.kt`, `UsersFragment.kt` |
| Wishlists | own/others wishlists list | `WishlistsFragmentViewModel.kt`, `WishlistsFragment.kt` |
| Gifts | gifts list | `GiftsFragmentViewModel.kt`, `GiftsFragment.kt` |
| Cross-cutting plumbing | shared fragment state helper | `DataState.kt` (new) |
| Auth | login screen, register screen (layout lint repair only) | `fragment_login.xml`, `fragment_register.xml` |
| Build & API contract | — (no changes; rules docs updated) | `.cline/rules/ARCHITECTURE.md`, `.cline/rules/architecture/05-cross-cutting.md`, `.cline/rules/architecture/06-features.md` |

### 4. Modified architecture rules

- `.cline/rules/ARCHITECTURE.md` — the "UI state pattern" global rule now states that the 5-state `DataState` lives **once** in `ru.otus.wishlist.fragment.DataState.kt` (shared by all list ViewModels) instead of being a per-ViewModel nested class.
- `.cline/rules/architecture/05-cross-cutting.md` — documents the shared helper: `DataState` + `mapToState()` + `fetchList()` (Loading → load → publish content → Content/Empty, `Throwable` → `Error`, returns the `Job`), and that per-row operations intentionally stay outside it.
- `.cline/rules/architecture/06-features.md` — the users/wishlists/gifts feature descriptions updated: ViewModels no longer define nested `DataState`; gifts' no-fetch/empty-suppression idiosyncrasy is recorded as deliberate.
- No existing rule was contradicted; the layout repair changes no rule (lint cleanliness is already mandated by `DEFINITION_OF_DONE.md`).

### 5. Regression cases

1. **Users initial load (happy path)** — logged in, open "Others" tab → spinner shows (`Loading`), then the users list renders (`Content`); the current user is excluded from the list.
2. **Users username filter** — type an exact username → list refreshes (`Loading` again) and shows only the exact-match user; clear the filter → full list returns; typing fast (overlapping loads) does not interleave stale results (Job cancellation).
3. **Users load failure (error path)** — cut network, open "Others" tab → error UI (`Error` state), no crash, no stuck spinner.
4. **Own wishlists CRUD** — on "My wishlists", create a wishlist via the bottom sheet → new row appears; edit its title → row updates in place; delete → row removed and `Empty` state shows when the last one is deleted.
5. **Wishlists load failure (error path / 401)** — force a failed/401 response on the wishlists fetch → `Error` state shown; on 401 the force-logout still fires (blank token written, app returns to login).
6. **Gifts from cache (no fetch)** — open someone else's wishlist → gifts render from the parent `Wishlist` DTO without any network gift-list call; an empty gifts list shows the empty UI **and** the content LiveData stays at its previous/initial value (suppression preserved).
7. **Gift reserve switch (other's mode)** — toggle "reserved" on someone else's gift → per-row spinner on that row only, switch flips on success, row reverts on failure; screen-level state is unaffected.
8. **Gift delete (own mode)** — delete a gift from your own wishlist → row removed; deleting the last gift transitions to `Empty`.
9. **Login/Register screens unchanged** — log in and register normally; the two buttons in each screen's bottom section render in the same positions as before the layout repair (pinned to the top of their section), and the section remains directly below the password field.
10. **Logout/login switch (stale-state regression)** — log in as user A, filter users, log out, log in as user B → the users tab shows user B's fresh, unfiltered list (the pre-existing `WizardCache.clear()`/`filteredUsers` pitfall is not regressed by the shared-state refactor).

**Definition of Done gates:**
- `:app:ktfmtCheck` → **BUILD SUCCESSFUL** (exit 0).
- `./gradlew build` (clean run) → **BUILD SUCCESSFUL** (111 tasks: OpenAPI codegen, Kotlin compilation, and all lint tasks including `lintAnalyzeDebug`/`lintDebug`/`lintVitalAnalyzeRelease` — the 4 pre-existing `NotSibling` errors are gone).
- `git status` shows exactly the intended change set: 11 modified files + the new `DataState.kt`; no stray artifacts.

## Результат
Агент следовал описанным правилам.
В процессе выполнения были обнаружены недостатки:
- Сам проект изначально не соответствовал definition of done, в результате чего выполнение задачи затянулось на 60 минут. 
В качестве решения перед выполнением задачи можно было попросить проверить текущий проект на соответствие definition of done и пофиксить его.
- Агент не до конца использует возможности Kotlin, о чем ему можно было дополнительно рассказать с помощью еще одного файла с правилами.
