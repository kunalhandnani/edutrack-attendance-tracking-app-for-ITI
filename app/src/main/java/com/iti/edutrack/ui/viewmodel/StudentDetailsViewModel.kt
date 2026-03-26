package com.iti.edutrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.model.Student
import com.iti.edutrack.data.model.StudentDetailsUiState
import com.iti.edutrack.data.model.StudentSortOption

class StudentDetailsViewModel(
    private val repository: DemoSchoolRepository
) : ViewModel() {
    var uiState by mutableStateOf(StudentDetailsUiState())
        private set

    val availableTrades get() = repository.availableTrades()
    val students get() = uiState.selectedTrade?.let { repository.studentsForTrade(it, uiState.sortOption) }.orEmpty()

    fun onTradeSelected(trade: String) {
        uiState = uiState.copy(selectedTrade = trade, selectedStudent = null)
    }

    fun onSortSelected(option: StudentSortOption) {
        uiState = uiState.copy(sortOption = option)
    }

    fun onStudentSelected(student: Student) {
        uiState = uiState.copy(selectedStudent = student)
    }

    fun closeDialog() {
        uiState = uiState.copy(selectedStudent = null)
    }
}
