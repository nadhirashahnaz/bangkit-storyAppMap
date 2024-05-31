package com.example.storyappnadhira.utils

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.storyappnadhira.api.StoryAPI
import com.example.storyappnadhira.database.StoryDatabase
import com.example.storyappnadhira.model.Story

class StoryRepository(private val storyDatabase: StoryDatabase, private val apiService: StoryAPI, private val token: String) {
    fun getStory(): LiveData<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, token)
            }
        ).liveData
    }
}