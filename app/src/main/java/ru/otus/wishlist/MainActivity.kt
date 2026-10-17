package ru.otus.wishlist

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import com.benasher44.uuid.uuid4
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import ru.otus.wishlist.databinding.ActivityMainBinding
import ru.otus.wishlist.storage.SessionPreferences
import ru.otus.wishlist.storage.UserPreferences
import ru.otus.wishlist.storage.get
import ru.otus.wishlist.storage.put

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var sharedPreferences: SharedPreferences

    private lateinit var binding: ActivityMainBinding

    private val logoutListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            key?.takeIf { it == UserPreferences::class.simpleName }
                ?.let { sharedPreferences.get<UserPreferences>() }
                ?.takeIf { it.isNotLoggedIn() }
                ?.let {
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.fragment_container)
                            as NavHostFragment
                    navHostFragment.navController.navigate(R.id.logout)
                    if (it.logoutEvent == UserPreferences.LogoutEvent.FORCE_LOGOUT) {
                        MaterialAlertDialogBuilder(this, R.style.Alert)
                            .setTitle(getString(R.string.error))
                            .setMessage(getString(R.string.you_must_log_in_again))
                            .setPositiveButton(getString(R.string.ok)) { _, _ -> }
                            .show()
                    }
                }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        } else {
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBar.left,
                right = systemBar.right,
                top = systemBar.top,
            )
            insets
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)
                as NavHostFragment
        val navController = navHostFragment.navController
        val sessionId = intent.getStringExtra("session_id")
        sharedPreferences.put(
            SessionPreferences(sessionId = sessionId ?: uuid4().toString())
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(logoutListener)
        if (sharedPreferences.get<UserPreferences>().isLoggedIn()) {
            navController.navigate(R.id.go_to_app_main)
        }
        onBackPressedDispatcher.addCallback(this) {
            navController.previousBackStackEntry?.let { navController.popBackStack() }
                ?: let {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
        }
    }
}
