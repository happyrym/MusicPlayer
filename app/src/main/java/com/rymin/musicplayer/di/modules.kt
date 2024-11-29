package com.rymin.musicplayer.di

import com.rymin.musicplayer.viewmodel.MusicListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

private val domainModules: List<Module> = listOf(
    DomainModules.musicRepositoryModule,
)

val appModules = module {
    viewModel { MusicListViewModel(get()) }
}

val modules: List<Module> = domainModules + appModules
