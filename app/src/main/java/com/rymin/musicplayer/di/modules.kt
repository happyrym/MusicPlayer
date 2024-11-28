package com.rymin.musicplayer.di

import com.rymin.musicplayer.MusicListViewModel
import org.koin.core.module.Module

private val domainModules: List<Module> = listOf(
    DomainModules.musicDataSourceModule,
    DomainModules.musicRepositoryModule,
)

private val appModules: List<Module> = listOf(
    MusicListViewModel.module,
)
val modules: List<Module> = domainModules + appModules
