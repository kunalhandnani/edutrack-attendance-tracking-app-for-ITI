package com.example.firstapp.data.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class UserRole {
    STUDENT,
    TEACHER
}

enum class StudentSortOption(val label: String) {
    LOW_ATTENDANCE("Low Attendance"),
    HIGH_ATTENDANCE("High Attendance"),
    NAME("Name"),
    CASTE("Caste"),
    TRADE("Trade")
}

data class Teacher(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val trades: List<String>
)

data class AttendanceEntry(
    val date: LocalDate,
    val subject: String,
    val isPresent: Boolean
)

data class LectureSlot(
    val subject: String,
    val time: String,
    val status: String
)

data class ExamItem(
    val id: String,
    val trade: String,
    val subject: String,
    val date: String,
    val time: String,
    val room: String
)

data class Student(
    val id: String,
    val name: String,
    val trade: String,
    val registrationNumber: String,
    val traineeNumber: String,
    val mobileNumber: String,
    val dob: String,
    val gender: String,
    val caste: String,
    val casteCategory: String,
    val fatherName: String,
    val motherName: String,
    val email: String,
    val weeklySchedule: Map<DayOfWeek, List<LectureSlot>>,
    val attendanceEntries: List<AttendanceEntry>
) {
    val attendanceSummary: AttendanceSummary
        get() {
            val present = attendanceEntries.count { it.isPresent }
            val absent = attendanceEntries.size - present
            val percentage = if (attendanceEntries.isEmpty()) 0 else (present * 100) / attendanceEntries.size
            return AttendanceSummary(
                totalClasses = attendanceEntries.size,
                presentClasses = present,
                absentClasses = absent,
                percentage = percentage
            )
        }
}

data class AttendanceSummary(
    val totalClasses: Int,
    val presentClasses: Int,
    val absentClasses: Int,
    val percentage: Int
)

data class AttendanceStatusRow(
    val student: Student,
    val isPresent: Boolean?
)

data class LoginResult(
    val role: UserRole,
    val student: Student? = null,
    val teacher: Teacher? = null,
    val errorMessage: String? = null
)

data class StudentImportRecord(
    val name: String,
    val trade: String,
    val registrationNumber: String,
    val traineeNumber: String,
    val mobileNumber: String,
    val dob: String,
    val gender: String,
    val caste: String,
    val casteCategory: String,
    val fatherName: String,
    val motherName: String
)
