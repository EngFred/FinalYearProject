package com.engineerfred.finalyearproject.ui.screen.username

import androidx.lifecycle.ViewModel
import com.engineerfred.finalyearproject.data.local.PrefsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel @Inject constructor(
    private val prefsStore: PrefsStore
): ViewModel() {

    private val _uiState = MutableStateFlow(UsernameUiState())
    val uiState = _uiState.asStateFlow()

    init {
        prefsStore.getUsername()?.apply {
            _uiState.update {
                it.copy(
                    username = this
                )
        } }
    }

    fun changeUsername(name: String){
        _uiState.update {
            it.copy(
                username = name,
                usernameErr = if (validateUsername(name).not()) "Invalid username" else null
            )
        }
    }

    fun saveUsername() {
        val username = _uiState.value.username
        username?.let {
            if (validateUsername(username)) {
                prefsStore.setUsername(username)
            } else {
                _uiState.update {
                    it.copy(
                        usernameErr = "Invalid username"
                    )
                }
            }
        } ?: run {
            _uiState.update {
                it.copy(
                    usernameErr = "Provide a username"
                )
            }
        }
    }

    private fun validateUsername(input: String): Boolean {
        return input.isNotEmpty() && input.length >= 3 && input.matches(Regex("^[a-zA-Z0-9_]+$"))
    }
}