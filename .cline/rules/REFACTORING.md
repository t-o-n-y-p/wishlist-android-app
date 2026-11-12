# Refactoring Tasks — wishlist-android

Binding requirements for tasks that restructure existing code. A task is a
"refactoring" when its primary goal is improving code structure, duplication, or
maintainability **without changing user-visible behavior** — extracting shared
code, simplifying state handling, renaming, moving classes within the allowed
two-module layout, etc. If the change alters user-visible behavior, it is new
functionality or a bugfix, not a refactoring.

## Example prompt (task description for an agent)

> Each list ViewModel re-implements the same `NotSet → Loading → Content/Empty/
> Error` mapping and the same `try/catch Throwable → Error` handling. Extract
> this into a single shared helper used by all list ViewModels, without changing
> any user-visible behavior.

Any prompt whose main goal is restructuring existing code with behavior
preserved belongs to this category ("refactor", "extract", "deduplicate",
"clean up", "rename", "simplify").

## Response format

The final response for a refactoring task must contain the following sections,
in this order:

### 1. What was wrong with the initial implementation
A comprehensive description of the problems in the initial implementation: the
smells and the concrete code locations involved (duplication, layer violations,
unreadable state handling, misleading names), and their practical consequences
(bug surface, maintenance cost).

### 2. Changes and how they solve the problems
A comprehensive description of the changes: every new/modified/deleted file, what
moved where and why, the resulting structure, and how each change addresses a
specific problem from section 1. State explicitly which behavior was intentionally
kept unchanged and how that was preserved.

### 3. Affected core and child functionalities
A list of the core functionalities and child functionalities affected by the
changes. "Affected" means at least one code file of that functionality was
modified (moved/renamed/deleted files count). Use the taxonomy defined in
section 3 of `NEW_FEATURE.md` and list the modified files next to each affected
functionality.

### 4. Modified architecture rules
The list of architecture rules that had to be modified or added because of the
refactoring, and where each change was recorded in `.cline/rules/`. A
refactoring must never silently contradict a binding rule — if a rule becomes
outdated, update it together with the code and list it here. If no rules
changed, say so explicitly.

### 5. Regression cases
5 to 10 of the most critical cases for regression testing: the user-visible
scenarios of every affected functionality that must behave exactly as before.
Each case must be concrete and testable: precondition → action → expected
(unchanged) result.

## Completion
In addition to the sections above, the task is done only when the gates in
`DEFINITION_OF_DONE.md` pass.
