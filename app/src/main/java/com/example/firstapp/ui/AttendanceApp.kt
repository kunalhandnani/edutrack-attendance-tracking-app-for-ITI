package com.example.firstapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.firstapp.data.DemoSchoolRepository
import com.example.firstapp.data.model.Student
import com.example.firstapp.data.model.Teacher
import com.example.firstapp.data.model.UserRole
import com.example.firstapp.ui.screens.LoginScreen
import com.example.firstapp.ui.screens.StudentDashboardScreen
import com.example.firstapp.ui.screens.TeacherDashboardScreen

@Composable
fun AttendanceApp(repository: DemoSchoolRepository) {
    var currentRole by remember { mutableStateOf<UserRole?>(null) }
    var currentStudent by remember { mutableStateOf<Student?>(null) }
    var currentTeacher by remember { mutableStateOf<Teacher?>(null) }

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
            when (currentRole) {
                null -> LoginScreen(
                    onLogin = { role, email, password ->
                        val result = repository.login(role, email, password)
                        if (result.errorMessage == null) {
                            currentRole = role
                            currentStudent = result.student
                            currentTeacher = result.teacher
                        }
                        result.errorMessage
                    }
                )

                UserRole.STUDENT -> currentStudent?.let { student ->
                    StudentDashboardScreen(
                        student = repository.refreshStudent(student.id) ?: student,
                        exams = repository.examsForTrade(student.trade),
                        onLogout = {
                            currentRole = null
                            currentStudent = null
                        }
                    )
                }

                UserRole.TEACHER -> currentTeacher?.let { teacher ->
                    TeacherDashboardScreen(
                        teacher = teacher,
                        repository = repository,
                        onLogout = {
                            currentRole = null
                            currentTeacher = null
                        }
                    )
                }
            }
        }
    }
}
