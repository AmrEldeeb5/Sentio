package com.example.klarity

import androidx.compose.runtime.Composable
import com.example.klarity.presentation.navigation.KlarityNavigation
import com.example.klarity.presentation.theme.KlarityTheme
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        KlarityTheme {
            KlarityNavigation()
        }
    }
}
