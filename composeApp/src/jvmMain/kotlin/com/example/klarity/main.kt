package com.example.klarity

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.example.klarity.di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    // Initialize Koin
    startKoin {
        modules(appModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Klarity - Your Second Brain",
        state = WindowState(
            placement = WindowPlacement.Maximized,
            width = 1400.dp,
            height = 900.dp
        )
    ) {
        App()
    }
}
