# 04 — Auth feature (login + register)

Reference files: `fragment/login/LoginFragment{,ViewModel,UseCase}.kt`,
`fragment/register/RegisterFragment{,ViewModel,UseCase}.kt`, `storage/UserPreferences.kt`,
`storage/SessionPreferences.kt`, `storage/extensions.kt`

## The canonical feature pattern (applies to every feature — follow it for new ones)
```
Fragment(@AndroidEntryPoint, Fragment(R.layout.x), ViewBinding, by viewModels())
  observes ViewModel's LiveData<sealed State>
    ViewModel(@HiltViewModel, viewModelScope.launch, try/catch Throwable → Error)
      calls UseCase(@ViewModelScoped, returns Result<T>)
        UseCase wraps the Retrofit call in NetworkCallProcessor.call { }
```
- Follow the state sealed class: `NotSet / Loading / Success / Error` (data objects).
- In the Fragment, react: Loading swaps the action button for a `waitButton` (spinner);
  Success navigates; Error re-enables the button + `requireContext().showErrorAlert()`.

## Login (`LoginFragment`)
- Keep the layout `fragment_login`; inputs `usernameTextInput`, `passwordTextInput`.
- Keep login success → `navigate(R.id.go_to_app_main)`.
- Keep displaying: `BuildConfig.VERSION_NAME` in `versionValue` and the
  **session id** (`SessionPreferences.sessionId`) in `sessionIdValue` — the session id
  is shown so it can be shared/linked (the `session_id` deep-link extra in MainActivity
  consumes it). Keep the bottom inset padding on that view.
- Keep `LoginFragmentUseCase.login`: build the generated `LoginDto(username, password)`
  → `authService.login(dto)` → `Response<Login>`; read the token in the ViewModel from
  `tokenResponse.token` (nullable, `orEmpty()`).
- On success, keep storing `UserPreferences(username, token)` into SharedPreferences
  from the ViewModel.

## Register (`RegisterFragment`)
- Keep the inputs: username, **email**, password.
- Keep `RegisterFragmentUseCase.register` building `UserRegistrationDto` →
  `authService.register(dto): Response<Unit>`.
- Keep the success behavior: `popBackStack()` (back to login) + success dialog
  ("successful_registration" / "now_you_can_log_in"); no auto-login.
- Keep the "Log in" button on this screen just popping the back stack.

## Auth state storage (`storage/`)
- Keep the `SharedPreferences` convention: key = **simple class name** of the payload;
  value = JSON via kotlinx `Json`. `get<T>()` (missing → `"{}"`), `put(value)`
  (`commit = true`).
- Keep `UserPreferences` — a `@Serializable data class` with `var` fields: `token`,
  `username`, `logoutEvent` (enum `LOGOUT`/`FORCE_LOGOUT`, default `LOGOUT`).
  - `isLoggedIn()` = token not blank; `getAuthHeaderValue()` = `"Bearer $token"`.
- Keep `SessionPreferences` — `@Serializable data class(sessionId: String)`.
- Keep `SharedPreferences.logout()` / `forceLogout()` — they write a `UserPreferences`
  with a blank token + the respective `logoutEvent`; because MainActivity listens for
  changes to the `UserPreferences` pref key, this is the app-wide logout trigger.
