package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.MainAppShell
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.Bg
import com.example.ui.theme.HrisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HrisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Bg
                ) {
                    HrisRootNavigation()
                }
            }
        }
    }
}

@Composable
fun HrisRootNavigation() {
    var currentFlow by remember { mutableStateOf("splash") } // "splash", "login", "main"

    when (currentFlow) {
        "splash" -> {
            SplashScreen(
                onNavigateToHome = { currentFlow = "main" },
                onNavigateToLogin = { currentFlow = "login" }
            )
        }
        "login" -> {
            LoginScreen(
                onLoginSuccess = { currentFlow = "main" }
            )
        }
        "main" -> {
            MainAppShell(
                onLogout = { currentFlow = "login" }
            )
        }
    }
}
