package com.example.cfvaname.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfvaname.data.AuthRepository
import com.example.cfvaname.data.LoginResponse
import com.example.cfvaname.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val userSession: UserSession? = null
)

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email tidak boleh kosong")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.login(email, password)

            result.fold(
                onSuccess = { response ->
                    val session = UserSession(
                        userId = response.userId ?: "",
                        email = response.email ?: email,
                        fullName = response.fullName ?: "",
                        role = response.role ?: "user"
                    )
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        loginSuccess = true,
                        userSession = session
                    )
                },
                onFailure = { error ->
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Login gagal"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }
}