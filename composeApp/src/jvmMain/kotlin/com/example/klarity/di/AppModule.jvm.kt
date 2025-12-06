package com.example.klarity.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.klarity.db.KlarityDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

/**
 * JVM/Desktop-specific dependency module.
 * Provides platform-specific implementations like the database driver.
 */
actual fun platformModule(): Module = module {
    single<SqlDriver> {
        val databasePath = getDatabasePath()
        val databaseFile = File(databasePath)
        val databaseExists = databaseFile.exists()
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")
        
        // Only create schema if database is new
        if (!databaseExists) {
            KlarityDatabase.Schema.create(driver)
        }
        
        driver
    }
}

private fun getDatabasePath(): String {
    val userHome = System.getProperty("user.home")
    val klarityDir = File(userHome, ".klarity")
    if (!klarityDir.exists()) {
        klarityDir.mkdirs()
    }
    return File(klarityDir, "klarity.db").absolutePath
}
