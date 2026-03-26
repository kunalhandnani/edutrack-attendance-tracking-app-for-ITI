package com.iti.edutrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.model.StudentDashboardUiState

class StudentDashboardViewModel(
    private val repository: DemoSchoolRepository
) : ViewModel() {
    var uiState by mutableStateOf(StudentDashboardUiState())
        private set

    val student get() = uiState.studentId?.let(repository::refreshStudent)
    val exams get() = student?.let { repository.examsForTrade(it.trade) }.orEmpty()
    val announcements get() = repository.announcements

    fun loadStudent(studentId: String) {
        if (uiState.studentId != studentId) {
            uiState = uiState.copy(studentId = studentId)
        }
    }

    fun onTabSelected(index: Int) {
        uiState = uiState.copy(selectedTab = index)
    }
}
