package com.iti.edutrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.model.Announcement
import com.iti.edutrack.data.model.AnnouncementsUiState
import java.time.LocalDate
import kotlin.random.Random

class AnnouncementsViewModel(
    private val repository: DemoSchoolRepository
) : ViewModel() {
    var uiState by mutableStateOf(AnnouncementsUiState())
        private set

    val announcements get() = repository.announcements

    fun onTitleChanged(value: String) {
        uiState = uiState.copy(title = value)
    }

    fun onMessageChanged(value: String) {
        uiState = uiState.copy(message = value)
    }

    fun makeAnnouncement(teacherName: String) {
        if (uiState.title.isBlank() || uiState.message.isBlank()) return
        repository.addAnnouncement(
            Announcement(
                id = "announcement-${Random.nextInt(1000, 9999)}",
                title = uiState.title,
                message = uiState.message,
                teacherName = teacherName,
                createdDate = LocalDate.now().toString()
            )
        )
        uiState = AnnouncementsUiState()
    }
}
