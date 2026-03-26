package com.iti.edutrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.model.ExamItem
import com.iti.edutrack.data.model.ExamScheduleUiState
import kotlin.random.Random

class ExamScheduleViewModel(
    private val repository: DemoSchoolRepository
) : ViewModel() {
    var uiState by mutableStateOf(ExamScheduleUiState())
        private set

    val availableTrades get() = repository.availableTrades()
    val exams get() = repository.examsForTrade("All Trades")

    fun ensureDefaultTrade() {
        if (uiState.trade.isBlank()) {
            uiState = uiState.copy(trade = availableTrades.firstOrNull().orEmpty())
        }
    }

    fun onTradeSelected(trade: String) {
        uiState = uiState.copy(trade = trade)
    }

    fun onSubjectChanged(value: String) {
        uiState = uiState.copy(subject = value)
    }

    fun onDateChanged(value: String) {
        uiState = uiState.copy(date = value)
    }

    fun onTimeChanged(value: String) {
        uiState = uiState.copy(time = value)
    }

    fun onRoomChanged(value: String) {
        uiState = uiState.copy(room = value)
    }

    fun saveExam() {
        if (uiState.subject.isBlank()) return
        repository.addExam(
            ExamItem(
                id = "exam-${Random.nextInt(1000, 9999)}",
                trade = uiState.trade,
                subject = uiState.subject,
                date = uiState.date,
                time = uiState.time,
                room = uiState.room
            )
        )
        uiState = uiState.copy(subject = "")
    }
}
