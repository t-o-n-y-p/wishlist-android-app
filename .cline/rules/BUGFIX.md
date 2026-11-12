# Bugfix Tasks — wishlist-android

Binding requirements for tasks that fix defects. A task is a "bugfix" when its
primary goal is correcting behavior that deviates from the behavior documented in
`ARCHITECTURE.md` (including its known pitfalls) or from the OpenAPI contract —
a reported crash, wrong UI state, wrong or stale data, or a regression.

## Example prompt (task description for an agent)

> A user logs in, opens the "Others" tab, filters the users list by username,
> then logs out and logs in as a different user. The users tab still shows the
> previous session's filtered list. Find the root cause and fix it.

Any prompt whose main goal is making incorrect behavior correct belongs to this
category ("fix", "crash", "does not work", "shows stale data", "regression").

## Response format

The final response for a bugfix task must contain the following sections, in this
order:

### 1. Cause
A comprehensive technical description of the root cause: the exact code path(s)
at file + function level and the state/data flow that produce the defect (e.g.,
which cache field is not reset, which observer is missed, which state is
re-emitted), why the defect manifests only in the reported scenario, and which
architecture rule or known pitfall it relates to.

### 2. Technical description of changes
A comprehensive technical description of the fix: every modified file, what
changed and why, how the fix interacts with the surrounding layers (Fragment →
ViewModel → UseCase → service), and why the change is minimal and does not alter
unrelated behavior.

### 3. Affected core and child functionalities
A list of the core functionalities and child functionalities affected by the
changes. "Affected" means at least one code file of that functionality was
modified. Use the taxonomy defined in section 3 of `NEW_FEATURE.md` and list the
modified files next to each affected functionality.

### 4. Verification & regression cases
5 to 10 of the most critical cases to verify the fix and to regression-test the
affected functionality. Each case must be concrete and testable: precondition →
action → expected result. The list must include the originally reported scenario
plus cases for each affected functionality that could break again.

## Completion
In addition to the sections above, the task is done only when the gates in
`DEFINITION_OF_DONE.md` pass.
