package com.example.sentio.di

import com.example.sentio.data.repositories.SqlDelightFolderRepository
import com.example.sentio.data.repositories.SqlDelightNoteRepository
import com.example.sentio.data.repositories.SqlDelightTagRepository
import com.example.sentio.data.util.DefaultDispatcherProvider
import com.example.sentio.data.util.DispatcherProvider
import com.example.sentio.db.KlarityDatabase
import com.example.sentio.domain.repositories.FolderRepository
import com.example.sentio.domain.repositories.NoteRepository
import com.example.sentio.domain.repositories.TagRepository
import com.example.sentio.domain.usecase.CreateNoteUseCase
import com.example.sentio.domain.usecase.DeleteNoteUseCase
import com.example.sentio.domain.usecase.NoteUseCases
import com.example.sentio.domain.usecase.SearchNotesUseCase
import com.example.sentio.domain.usecase.UpdateNoteUseCase
import com.example.sentio.presentation.viewmodel.EditorViewModel
import com.example.sentio.presentation.viewmodel.HomeViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Platform-specific module for dependencies that vary by platform (e.g., database driver).
 */
expect fun platformModule(): org.koin.core.module.Module

/**
 * Core utilities module - dispatchers, etc.
 */
val coreModule = module {
    singleOf(::DefaultDispatcherProvider) { bind<DispatcherProvider>() }
}

/**
 * Database module - database instance.
 */
val databaseModule = module {
    single { KlarityDatabase(driver = get()) }
}

/**
 * Repository module - repository implementations.
 * Repositories use SQLDelight directly - no DataSource layer needed.
 */
val repositoryModule = module {
    single<NoteRepository> { SqlDelightNoteRepository(get(), get()) }
    single<FolderRepository> { SqlDelightFolderRepository(get(), get()) }
    single<TagRepository> { SqlDelightTagRepository(get(), get()) }
}

/**
 * Domain module - use cases.
 */
val domainModule = module {
    // Individual use cases
    factoryOf(::CreateNoteUseCase)
    factoryOf(::UpdateNoteUseCase)
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::SearchNotesUseCase)

    // Use case containers for ViewModels
    factory { NoteUseCases(get(), get(), get(), get()) }
}

/**
 * ViewModel module - all ViewModels.
 */
val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::EditorViewModel)
}

/**
 * Main application module that includes all other modules.
 */
val appModule = module {
    includes(
        platformModule(),
        coreModule,
        databaseModule,
        repositoryModule,
        domainModule,
        viewModelModule
    )
}
