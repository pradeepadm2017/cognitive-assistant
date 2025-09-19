package com.cognitiveassistant.viewmodel

import androidx.lifecycle.ViewModel
import com.cognitiveassistant.model.User
import com.cognitiveassistant.model.UserType
import com.cognitiveassistant.repository.InMemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun updateUserType(userType: UserType) {
        _uiState.value = _uiState.value.copy(userType = userType, errorMessage = null)
    }

    fun login(onSuccess: (User) -> Unit) {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please fill all fields")
            return
        }

        val user = InMemoryRepository.authenticateUser(
            state.username,
            state.password,
            state.userType
        )

        if (user != null) {
            onSuccess(user)
        } else {
            _uiState.value = state.copy(errorMessage = "Invalid credentials")
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val userType: UserType = UserType.PATIENT,
    val errorMessage: String? = null
)