package hr.rkgarmory.app

import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.runtime.remember
import hr.rkgarmory.app.feature.inventory.InventoryFileStore
import hr.rkgarmory.app.feature.login.LoginScreen
import hr.rkgarmory.app.remote.StaffSession
import hr.rkgarmory.app.ui.shell.AppShell
import hr.rkgarmory.app.ui.theme.RkgArmoryTheme

class MainActivity : ComponentActivity() {
    private val storagePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) InventoryFileStore(this).ensure()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFormat(PixelFormat.RGBA_8888)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
        }
        if (Build.VERSION.SDK_INT < 29 &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            storagePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            InventoryFileStore(this).ensure()
        }
        enableEdgeToEdge()
        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(false) }
            RkgArmoryTheme(darkTheme = darkTheme) {
                ArmoryApp(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                )
            }
        }
    }
}

@Composable
private fun ArmoryApp(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var loginBusy by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (isLoggedIn && StaffSession.credentials != null) {
            AppShell(
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
                onLogout = {
                    StaffSession.signOut()
                    isLoggedIn = false
                },
            )
        } else {
            LoginScreen(
                error = loginError,
                busy = loginBusy,
                onLogin = { username, password ->
                    loginBusy = true
                    loginError = null
                    StaffSession.signIn(username, password) { error ->
                        loginBusy = false
                        if (error == null) isLoggedIn = true else loginError = error
                    }
                },
            )
        }
    }
}
