package com.iti.edutrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.model.TeacherDashboardUiState

class TeacherDashboardViewModel(
    private val repository: DemoSchoolRepository
) : ViewModel() {
    var uiState by mutableStateOf(TeacherDashboardUiState())
        private set

    val teacher get() = uiState.teacherId?.let(repository::teacherById)
    val visibleTrades get() = repository.availableTrades()

    fun loadTeacher(teacherId: String) {
        if (uiState.teacherId != teacherId) {
            uiState = uiState.copy(teacherId = teacherId)
        }
    }

    fun onTabSelected(index: Int) {
        uiState = uiState.copy(selectedTab = index)
    }
}
