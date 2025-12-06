package com.example.klarity.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.klarity.db.KlarityDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific dependency module.
 * Provides platform-specific implementations like the database driver.
 */
actual fun platformModule(): Module = module {
    single<SqlDriver> {
        val context: Context = get()
        AndroidSqliteDriver(
            schema = KlarityDatabase.Schema,
            context = context,
            name = "klarity.db"
        )
    }
}
