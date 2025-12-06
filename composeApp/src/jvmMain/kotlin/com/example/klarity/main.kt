package com.example.klarity

import androidx.compose.ui.window.Window
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
    ) {
        App()
    }
}
