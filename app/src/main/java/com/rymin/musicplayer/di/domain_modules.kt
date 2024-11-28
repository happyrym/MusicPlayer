package com.rymin.musicplayer.di

import com.rymin.musicplayer.repository.MusicDataSource
import com.rymin.musicplayer.repository.MusicRepository
import com.rymin.musicplayer.repository.MusicRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

object DomainModules {
    val musicDataSourceModule: Module = module {
        single { MusicDataSource(get()) }
    }
    val musicRepositoryModule: Module = module {
        single<MusicRepository> { MusicRepositoryImpl(get()) }
    }
}