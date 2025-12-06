package com.example.sentio

import androidx.compose.runtime.Composable
import com.example.sentio.presentation.navigation.KlarityNavigation
import com.example.sentio.presentation.theme.KlarityTheme
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        KlarityTheme {
            KlarityNavigation()
        }
    }
}
