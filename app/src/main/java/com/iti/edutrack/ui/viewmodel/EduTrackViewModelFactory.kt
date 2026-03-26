package com.iti.edutrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.edutrack.data.DemoSchoolRepository

class EduTrackViewModelFactory(
    private val repository: DemoSchoolRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AppViewModel::class.java) -> AppViewModel() as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository) as T
            modelClass.isAssignableFrom(StudentDashboardViewModel::class.java) -> StudentDashboardViewModel(repository) as T
            modelClass.isAssignableFrom(TeacherDashboardViewModel::class.java) -> TeacherDashboardViewModel(repository) as T
            modelClass.isAssignableFrom(MarkAttendanceViewModel::class.java) -> MarkAttendanceViewModel(repository) as T
            modelClass.isAssignableFrom(ViewAttendanceViewModel::class.java) -> ViewAttendanceViewModel(repository) as T
            modelClass.isAssignableFrom(StudentDetailsViewModel::class.java) -> StudentDetailsViewModel(repository) as T
            modelClass.isAssignableFrom(ExamScheduleViewModel::class.java) -> ExamScheduleViewModel(repository) as T
            modelClass.isAssignableFrom(AnnouncementsViewModel::class.java) -> AnnouncementsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
