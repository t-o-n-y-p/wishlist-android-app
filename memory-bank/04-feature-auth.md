# 04 — Auth feature (login + register)

Sources: `fragment/login/LoginFragment{,ViewModel,UseCase}.kt`,
`fragment/register/RegisterFragment{,ViewModel,UseCase}.kt`, `storage/UserPreferences.kt`,
`storage/SessionPreferences.kt`, `storage/extensions.kt`

## The canonical feature pattern (applies to every feature)
```
Fragment(@AndroidEntryPoint, Fragment(R.layout.x), ViewBinding, by viewModels())
  observes ViewModel's LiveData<sealed State>
    ViewModel(@HiltViewModel, viewModelScope.launch, try/catch Throwable → Error)
      calls UseCase(@ViewModelScoped, returns Result<T>)
        UseCase wraps the Retrofit call in NetworkCallProcessor.call { }
```
- State sealed class: `NotSet / Loading / Success / Error` (data objects).
- Fragment reacts: Loading swaps the action button for a `waitButton` (spinner);
  Success navigates; Error re-enables button + `requireContext().showErrorAlert()`.

## Login (`LoginFragment`)
- Layout `fragment_login`; inputs `usernameTextInput`, `passwordTextInput`.
- Login success → `navigate(R.id.go_to_app_main)`.
- Also displays: `BuildConfig.VERSION_NAME` in `versionValue` and the
  **session id** (`SessionPreferences.sessionId`) in `sessionIdValue` — the session id
  is shown to the user so it can be shared/linked (deep-link extra `session_id` in
  MainActivity consumes it). Bottom inset padding on that view.
- `LoginFragmentUseCase.login`: builds generated `LoginDto(username, password)` →
  `authService.login(dto)` → `Response<Login>`; token read by ViewModel from
  `tokenResponse.token` (nullable, `orEmpty()`).
- ViewModel stores `UserPreferences(username, token)` into SharedPreferences on success.

## Register (`RegisterFragment`)
- Inputs: username, **email**, password.
- `RegisterFragmentUseCase.register` builds `UserRegistrationDto` → `authService.register(dto): Response<Unit>`.
- Success → `popBackStack()` (back to login) + success dialog
  ("successful_registration" / "now_you_can_log_in"); no auto-login.
- "Log in" button on this screen just pops back stack.

## Auth state storage (`storage/`)
- `SharedPreferences` key = **simple class name** of the payload; value = JSON via
  kotlinx `Json`. `get<T>()` (missing → `"{}"`), `put(value)` (`commit = true`).
- `UserPreferences` — `@Serializable data class` with `var` fields: `token`, `username`,
  `logoutEvent` (enum `LOGOUT`/`FORCE_LOGOUT`, default `LOGOUT`).
  - `isLoggedIn()` = token not blank; `getAuthHeaderValue()` = `"Bearer $token"`.
- `SessionPreferences` — `@Serializable data class(sessionId: String)`.
- `SharedPreferences.logout()` / `forceLogout()` — write a `UserPreferences` with blank
  token + the respective `logoutEvent`; because MainActivity listens for changes to the
  `UserPreferences` pref key, this is the app-wide logout trigger.
