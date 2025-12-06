package com.example.sentio.presentation.state

/**
 * Base sealed class for common UI states across all screens.
 * Can be extended for screen-specific states.
 */
sealed interface UiState<out T> {
    /**
     * Initial state before any data is loaded.
     */
    data object Idle : UiState<Nothing>

    /**
     * Loading state while fetching data.
     */
    data object Loading : UiState<Nothing>


    data class Success<T>(val data: T) : UiState<T>


    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : UiState<Nothing>

    /**
     * Empty state when data is loaded but empty.
     */
    data object Empty : UiState<Nothing>
}

/**
 * Extension to check if state is loading.
 */
val UiState<*>.isLoading: Boolean
    get() = this is UiState.Loading

/**
 * Extension to check if state is success.
 */
val UiState<*>.isSuccess: Boolean
    get() = this is UiState.Success

/**
 * Extension to check if state is error.
 */
val UiState<*>.isError: Boolean
    get() = this is UiState.Error

/**
 * Extension to get data or null.
 */
fun <T> UiState<T>.getOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}

/**
 * Extension to get data or default.
 */
fun <T> UiState<T>.getOrDefault(default: T): T = when (this) {
    is UiState.Success -> data
    else -> default
}
