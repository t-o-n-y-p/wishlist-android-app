# New Functionality Tasks — wishlist-android

Binding requirements for tasks that add new functionality. A task is
"new functionality" when its primary goal is a new user-facing capability (a new
screen, tab, dialog, list action, setting, or new use of an API endpoint) rather
than fixing a defect or restructuring existing code.

Before writing code, read `ARCHITECTURE.md` and the numbered files in
`architecture/`; new features must follow the canonical feature pattern
(`architecture/04-feature-auth.md`) and the Definition of Done.

## Example prompt (task description for an agent)

> Add a title filter to the "My wishlists" tab: a search field in the tab's
> toolbar that hides wishlist rows whose title does not contain the entered
> substring, case-insensitively, while the user types. Reuse the same UX pattern
> as the users username filter. Do not change the backend API.

Any prompt whose main goal is a new capability belongs to this category, no matter
how it is worded ("implement", "add support for", "extend the app so that...").

## Response format

The final response for a new-functionality task must contain the following
sections, in this order:

### 1. Relationship to existing features
A comprehensive description of how the new feature relates to existing features:
which features it extends, reuses, or interacts with (e.g., `WizardCache` flow
state, `DtoMapper`, the own/other mode from `cache.currentUser == null`, existing
bottom-sheet dialogs, interceptors, storage), which screens and nav graphs it
touches, and which existing behavior must remain unchanged.

### 2. Technical description of changes
A comprehensive technical description of the implementation: every new or modified
file, the layer each change lives in (Fragment → ViewModel → UseCase → service →
storage), new/extended sealed state objects and their states, new FragmentResult
keys, DI scopes of new classes, layout/navigation XML changes, new resources
(strings, drawables), and any new API endpoints consumed.

### 3. Affected core and child functionalities
A list of the core functionalities and child functionalities affected by the
changes. "Affected" means at least one code file of that functionality was
modified (new files count as modifications of their functionality). Use the
taxonomy below and list the modified files next to each affected functionality.

| Core functionality | Child functionalities |
|---|---|
| App bootstrap | AndroidManifest, `WishlistApp` + DI module, MainActivity, session/login/logout wiring |
| Navigation | nav graphs, screen map, bottom tabs, routing actions |
| Auth | login screen, register screen, auth state storage |
| Main UI | main screen, bottom navigation, tab fragments |
| Users | users list, username filter |
| Wishlists | own wishlists list, others' wishlists list, wishlist create/edit dialogs |
| Gifts | gifts list, gift create/edit dialogs, gift reserve switch |
| Cross-cutting plumbing | NetworkCallProcessor, Retrofit services, interceptors, DtoMapper, WizardCache, storage, fragment helpers/constants |
| Build & API contract | Gradle modules, version catalog, OpenAPI codegen (`wishlist.yaml`), generated models |

### 4. New architecture rules
The list of new architecture rules the change introduces or implies, and where
each was recorded (a new file in `architecture/` or an update to an existing one).
If the change conflicts with an existing rule, the rule must be updated together
with the code — list those updates in this section. If the change introduces no
new rules, say so explicitly.

### 5. Verification & regression cases
5 to 10 of the most critical cases to verify the new feature and to
regression-test the affected functionality. Each case must be concrete and
testable: precondition → action → expected result. The list must cover at least
the happy path, the error path (network failure / 401 force logout), and one
regression case for each affected functionality.

## Completion
In addition to the sections above, the task is done only when the gates in
`DEFINITION_OF_DONE.md` pass.
