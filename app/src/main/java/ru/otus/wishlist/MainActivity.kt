package ru.otus.wishlist

import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowInsets
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import com.benasher44.uuid.uuid4
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.databinding.ActivityMainBinding
import ru.otus.wishlist.storage.SessionPreferences
import ru.otus.wishlist.storage.UserPreferences
import ru.otus.wishlist.storage.get
import ru.otus.wishlist.storage.put
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var sharedPreferences: SharedPreferences

    private lateinit var binding: ActivityMainBinding
    private val logoutListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        key
            ?.takeIf { it == UserPreferences::class.simpleName }
            ?.let { sharedPreferences.get<UserPreferences>() }
            ?.takeIf { it.isNotLoggedIn() }
            ?.let {
                binding.fragmentContainer
                    .getFragment<NavHostFragment>()
                    .navController
                    .navigate(R.id.logout)
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
        super.onCreate(savedInstanceState)
        val sessionId = intent.getStringExtra("session_id")
        sharedPreferences.put(
            SessionPreferences(sessionId = sessionId ?: uuid4().toString())
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        sharedPreferences.registerOnSharedPreferenceChangeListener(logoutListener)
        if (sharedPreferences.get<UserPreferences>().isLoggedIn()) {
            binding.fragmentContainer
                .getFragment<NavHostFragment>()
                .navController
                .navigate(R.id.go_to_app_main)
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBar = insets.getInsets(
                WindowInsets.Type.statusBars()
                        or WindowInsets.Type.navigationBars())
            v.updatePadding(
                left = systemBar.left,
                top = systemBar.top,
                right = systemBar.right,
                bottom = systemBar.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
        setContentView(binding.root)
    }
}