package com.rymin.musicplayer.di

import com.rymin.musicplayer.repository.MusicDataSource
import com.rymin.musicplayer.repository.MusicRepository
import com.rymin.musicplayer.repository.MusicRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

object DomainModules {
    val musicRepositoryModule: Module = module {
        single { MusicDataSource(androidContext()) }
        single<MusicRepository> { MusicRepositoryImpl(get()) }
    }
}