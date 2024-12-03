package com.rymin.data.usecase

import com.rymin.common.data.Album
import com.rymin.common.data.Music
import com.rymin.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.module.Module
import org.koin.dsl.module

class MusicListUseCase(
    private val musicRepository: MusicRepository,
) {

    suspend fun getMusicList(): Flow<List<Music>> {
        return musicRepository.getMusicList()
    }

    suspend fun getMusicListByAlbum(albumId: Long): Flow<List<Music>> {
        return musicRepository.getMusicListByAlbum(albumId)
    }

    suspend fun getAlbumList(): Flow<List<Album>> {
        return musicRepository.getAlbumList()
    }


    companion object {
        val modules: Module = module {
            factory {
                MusicListUseCase(get())
            }
        }
    }
}