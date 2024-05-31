package com.example.storyappnadhira.utils

import android.content.Context
import com.example.storyappnadhira.api.ApiConfig
import com.example.storyappnadhira.database.StoryDatabase

object Injection {

    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        val preferencesHelper = PreferencesHelper(context)
        val token = "Bearer ${preferencesHelper.token}"
        return StoryRepository(database, apiService, token)
    }
}