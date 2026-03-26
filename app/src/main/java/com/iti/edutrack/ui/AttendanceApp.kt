package com.iti.edutrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.model.UserRole
import com.iti.edutrack.ui.screens.LoginScreen
import com.iti.edutrack.ui.screens.StudentDashboardScreen
import com.iti.edutrack.ui.screens.TeacherDashboardScreen
import com.iti.edutrack.ui.viewmodel.AnnouncementsViewModel
import com.iti.edutrack.ui.viewmodel.AppViewModel
import com.iti.edutrack.ui.viewmodel.AuthViewModel
import com.iti.edutrack.ui.viewmodel.EduTrackViewModelFactory
import com.iti.edutrack.ui.viewmodel.ExamScheduleViewModel
import com.iti.edutrack.ui.viewmodel.MarkAttendanceViewModel
import com.iti.edutrack.ui.viewmodel.StudentDashboardViewModel
import com.iti.edutrack.ui.viewmodel.StudentDetailsViewModel
import com.iti.edutrack.ui.viewmodel.TeacherDashboardViewModel
import com.iti.edutrack.ui.viewmodel.ViewAttendanceViewModel

@Composable
fun AttendanceApp(repository: DemoSchoolRepository) {
    val factory = remember(repository) { EduTrackViewModelFactory(repository) }
    val appViewModel: AppViewModel = viewModel(factory = factory)
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val studentDashboardViewModel: StudentDashboardViewModel = viewModel(factory = factory)
    val teacherDashboardViewModel: TeacherDashboardViewModel = viewModel(factory = factory)
    val markAttendanceViewModel: MarkAttendanceViewModel = viewModel(factory = factory)
    val viewAttendanceViewModel: ViewAttendanceViewModel = viewModel(factory = factory)
    val studentDetailsViewModel: StudentDetailsViewModel = viewModel(factory = factory)
    val examScheduleViewModel: ExamScheduleViewModel = viewModel(factory = factory)
    val announcementsViewModel: AnnouncementsViewModel = viewModel(factory = factory)
    val sessionState = appViewModel.sessionState

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFF8FBFF),
                            Color(0xFFEAF2F7)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            when (sessionState.currentRole) {
                null -> LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { result ->
                        appViewModel.onLoginSuccess(result)
                    }
                )

                UserRole.STUDENT -> {
                    val studentId = sessionState.currentStudentId
                    if (studentId != null) {
                        LaunchedEffect(studentId) {
                            studentDashboardViewModel.loadStudent(studentId)
                        }
                        StudentDashboardScreen(
                            viewModel = studentDashboardViewModel,
                            onLogout = { appViewModel.logout() }
                        )
                    }
                }

                UserRole.TEACHER -> {
                    val teacherId = sessionState.currentTeacherId
                    if (teacherId != null) {
                        LaunchedEffect(teacherId) {
                            teacherDashboardViewModel.loadTeacher(teacherId)
                            viewAttendanceViewModel.ensureDefaultTrade()
                            examScheduleViewModel.ensureDefaultTrade()
                        }
                        TeacherDashboardScreen(
                            teacherDashboardViewModel = teacherDashboardViewModel,
                            markAttendanceViewModel = markAttendanceViewModel,
                            viewAttendanceViewModel = viewAttendanceViewModel,
                            studentDetailsViewModel = studentDetailsViewModel,
                            examScheduleViewModel = examScheduleViewModel,
                            announcementsViewModel = announcementsViewModel,
                            onLogout = { appViewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}
