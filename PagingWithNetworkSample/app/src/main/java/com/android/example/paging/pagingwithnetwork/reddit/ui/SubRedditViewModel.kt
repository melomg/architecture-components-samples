/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.paging.pagingwithnetwork.reddit.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.cachedIn
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditMetadata
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class SubRedditViewModel(
    private val repository: RedditPostRepository,
    private val savedStateHandle: SavedStateHandle,
    private val metadataState: MutableStateFlow<RedditMetadata?>,
) : ViewModel() {
    companion object {
        const val KEY_SUBREDDIT = "subreddit"
        const val DEFAULT_SUBREDDIT = "androiddev"
    }

    init {
        if (!savedStateHandle.contains(KEY_SUBREDDIT)) {
            savedStateHandle.set(KEY_SUBREDDIT, DEFAULT_SUBREDDIT)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val posts = savedStateHandle.getLiveData<String>(KEY_SUBREDDIT)
        .asFlow()
        .flatMapLatest { repository.postsOfSubreddit(it, 30) }
        // cachedIn() shares the paging state across multiple consumers of posts,
        // e.g. different generations of UI across rotation config change
        .cachedIn(viewModelScope)

    fun showSubreddit(subreddit: String) {
        if (!shouldShowSubreddit(subreddit)) return
        savedStateHandle.set(KEY_SUBREDDIT, subreddit)
    }

    fun onLoadStateChanged(loadState: LoadState) = when (loadState) {
        is LoadState.NotLoading -> {
            // This will crash when adapter is refreshed
            // since loadStateFlow is emitting `NotLoading` at first
            checkNotNull(metadataState.value) {
                "Missing metadata value after load completed"
            }
        }
        LoadState.Loading -> {
//                TODO()
        }
        is LoadState.Error -> {
//                TODO()
        }
    }

    private fun shouldShowSubreddit(subreddit: String): Boolean {
        return savedStateHandle.get<String>(KEY_SUBREDDIT) != subreddit
    }
}
