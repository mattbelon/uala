package com.test.uala.ui

import com.test.uala.data.models.Location

sealed interface HomeUiState {
    data class Success(val apiResponse: Location): HomeUiState
    data class Error(val exception: Exception): HomeUiState
    object Loading: HomeUiState
}