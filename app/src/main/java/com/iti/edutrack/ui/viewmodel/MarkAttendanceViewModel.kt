package com.iti.edutrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.model.MarkAttendanceUiState
import com.iti.edutrack.data.model.NameSortOrder
import com.iti.edutrack.data.model.StudentSortOption
import java.time.LocalDate

class MarkAttendanceViewModel(
    private val repository: DemoSchoolRepository
) : ViewModel() {
    var uiState by mutableStateOf(MarkAttendanceUiState())
        private set

    val availableTrades get() = repository.availableTrades()

    val students
        get() = repository.studentsForTrade(uiState.selectedTrade, StudentSortOption.NAME).let { loaded ->
            when (uiState.nameSortOrder) {
                NameSortOrder.ASCENDING -> loaded.sortedBy { it.name }
                NameSortOrder.DESCENDING -> loaded.sortedByDescending { it.name }
            }
        }

    fun onTradeSelected(trade: String) {
        uiState = uiState.copy(selectedTrade = trade, saveMessage = null)
        syncSelectionWithRepository()
    }

    fun onDateSelected(date: LocalDate) {
        uiState = uiState.copy(selectedDate = date, saveMessage = null)
        syncSelectionWithRepository()
    }

    fun onFilterSelected(order: NameSortOrder) {
        uiState = uiState.copy(nameSortOrder = order)
    }

    fun toggleStudent(studentId: String, makePresent: Boolean) {
        uiState = uiState.copy(
            selectedIds = if (makePresent) uiState.selectedIds + studentId else uiState.selectedIds - studentId
        )
    }

    fun markAllPresent() {
        uiState = uiState.copy(selectedIds = students.map { it.id }.toSet())
    }

    fun markAllAbsent() {
        uiState = uiState.copy(selectedIds = emptySet())
    }

    fun saveAttendance() {
        val trade = uiState.selectedTrade
        uiState = if (trade.isBlank()) {
            uiState.copy(saveMessage = "Please select a trade first.")
        } else {
            repository.markAttendance(trade, uiState.selectedDate, uiState.selectedIds)
            uiState.copy(saveMessage = "Attendance saved for $trade on ${uiState.selectedDate}.")
        }
    }

    private fun syncSelectionWithRepository() {
        val trade = uiState.selectedTrade
        if (trade.isBlank()) {
            uiState = uiState.copy(selectedIds = emptySet())
            return
        }
        val savedIds = repository.presentStudentIdsForTradeOnDate(trade, uiState.selectedDate)
        val defaultIds = repository.studentsForTrade(trade, StudentSortOption.NAME).map { it.id }.toSet()
        uiState = uiState.copy(selectedIds = if (savedIds.isNotEmpty()) savedIds else defaultIds)
    }
}
