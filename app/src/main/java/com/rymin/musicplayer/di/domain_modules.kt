package com.rymin.musicplayer.di

import com.rymin.data.repository.MusicDataSource
import com.rymin.data.repository.MusicRepository
import com.rymin.data.repository.MusicRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

object DomainModules {
    val musicRepositoryModule: Module = module {
        single { MusicDataSource(androidContext()) }
        single<MusicRepository> { MusicRepositoryImpl(get()) }
    }
}