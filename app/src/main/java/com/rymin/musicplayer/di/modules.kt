package com.rymin.musicplayer.di

import com.rymin.data.usecase.MusicListUseCase
import com.rymin.musicplayer.viewmodel.MusicListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

private val domainModules: List<Module> = listOf(
    DomainModules.musicRepositoryModule,
    MusicListUseCase.modules,
)

val appModules = module {
    viewModel { MusicListViewModel(androidContext(),get()) }
}

val modules: List<Module> = domainModules + appModules
