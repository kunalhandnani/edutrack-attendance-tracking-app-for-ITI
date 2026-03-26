package com.iti.edutrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.model.AuthUiState
import com.iti.edutrack.data.model.LoginResult
import com.iti.edutrack.data.model.UserRole

class AuthViewModel(
    private val repository: DemoSchoolRepository
) : ViewModel() {
    var uiState by mutableStateOf(AuthUiState())
        private set

    val sampleStudentLogin = "aayanshaikh@ITI.com / 01082008"
    val sampleTeacherLogin = "anita-sharma@ITI.com / teach@123"

    fun onRoleSelected(role: UserRole) {
        uiState = uiState.copy(
            selectedRole = role,
            email = if (role == UserRole.STUDENT) "aayanshaikh@ITI.com" else "anita-sharma@ITI.com",
            password = if (role == UserRole.STUDENT) "01082008" else "teach@123",
            errorMessage = null
        )
    }

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun login(): LoginResult {
        val result = repository.login(uiState.selectedRole, uiState.email, uiState.password)
        uiState = uiState.copy(errorMessage = result.errorMessage)
        return result
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}
