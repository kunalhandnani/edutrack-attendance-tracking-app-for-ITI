package com.iti.edutrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.model.ViewAttendanceUiState
import java.time.LocalDate

class ViewAttendanceViewModel(
    private val repository: DemoSchoolRepository
) : ViewModel() {
    var uiState by mutableStateOf(ViewAttendanceUiState())
        private set

    val availableTrades get() = repository.availableTrades()
    val attendanceRows get() = repository.attendanceForTradeOnDate(uiState.selectedTrade, uiState.selectedDate)
    val presentCount get() = attendanceRows.count { it.isPresent == true }
    val absentCount get() = attendanceRows.count { it.isPresent == false }
    val unmarkedCount get() = attendanceRows.count { it.isPresent == null }

    fun ensureDefaultTrade() {
        if (uiState.selectedTrade.isBlank()) {
            uiState = uiState.copy(selectedTrade = availableTrades.firstOrNull().orEmpty())
        }
    }

    fun onTradeSelected(trade: String) {
        uiState = uiState.copy(selectedTrade = trade)
    }

    fun onDateSelected(date: LocalDate) {
        uiState = uiState.copy(selectedDate = date)
    }
}
