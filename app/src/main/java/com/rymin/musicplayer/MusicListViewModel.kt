package com.rymin.musicplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rymin.musicplayer.data.Music
import com.rymin.musicplayer.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class MusicListViewModel(private val musicRepository: MusicRepository) : ViewModel() {

    private val _musicList = MutableStateFlow<List<Music>>(emptyList())
    val musicList: StateFlow<List<Music>> = _musicList

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadMusicList()
    }

     fun loadMusicList() {
        viewModelScope.launch {
            _isLoading.value = true
            _musicList.value = musicRepository.getMusicList()
            _isLoading.value = false
        }
    }

    companion object {
        val module = module {
            viewModel<MusicListViewModel> { get() }
        }
    }
}
