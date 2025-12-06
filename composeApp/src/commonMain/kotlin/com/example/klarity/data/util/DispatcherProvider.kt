package com.example.klarity.data.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides coroutine dispatchers for different use cases.
 * This abstraction allows for easy testing by swapping dispatchers.
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
}
