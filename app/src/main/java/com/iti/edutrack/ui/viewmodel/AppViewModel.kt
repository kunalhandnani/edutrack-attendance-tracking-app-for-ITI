package com.iti.edutrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.iti.edutrack.data.model.AppSessionState
import com.iti.edutrack.data.model.LoginResult

class AppViewModel : ViewModel() {
    var sessionState by mutableStateOf(AppSessionState())
        private set

    fun onLoginSuccess(result: LoginResult) {
        sessionState = AppSessionState(
            currentRole = result.role,
            currentStudentId = result.student?.id,
            currentTeacherId = result.teacher?.id
        )
    }

    fun logout() {
        sessionState = AppSessionState()
    }
}
