package com.example.storyappnadhira.story

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyappnadhira.model.Story
import com.example.storyappnadhira.utils.Injection
import com.example.storyappnadhira.utils.StoryRepository

class StoryViewModel(quoteRepository: StoryRepository) : ViewModel() {

    val story: LiveData<PagingData<Story>> =
        quoteRepository.getStory().cachedIn(viewModelScope)


    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoryViewModel(Injection.provideRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}