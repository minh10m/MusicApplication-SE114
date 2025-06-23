package com.example.musicapplicationse114.ui.queue

import androidx.lifecycle.ViewModel
import com.example.musicapplicationse114.auth.TokenManager
import com.example.musicapplicationse114.repositories.Api
import com.example.musicapplicationse114.repositories.MainLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val api: Api?,
    private val mainLog: MainLog?,
    private val tokenManager: TokenManager?
): ViewModel() {
}