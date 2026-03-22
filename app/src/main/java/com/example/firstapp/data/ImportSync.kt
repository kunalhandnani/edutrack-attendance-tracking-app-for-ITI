package com.example.firstapp.data

import com.example.firstapp.data.model.AttendanceEntry
import com.example.firstapp.data.model.LectureSlot
import com.example.firstapp.data.model.Student
import com.example.firstapp.data.model.StudentImportRecord
import java.time.DayOfWeek
import java.time.LocalDate

interface StudentExcelImporter {
    suspend fun importStudents(filePath: String): List<StudentImportRecord>
}

class StubStudentExcelImporter : StudentExcelImporter {
    override suspend fun importStudents(filePath: String): List<StudentImportRecord> = emptyList()
}

data class MongoSyncConfig(
    val baseUrl: String = "https://your-backend.example.com",
    val databaseName: String = "iti_attendance",
    val studentsCollection: String = "students",
    val attendanceCollection: String = "attendance",
    val examsCollection: String = "exams"
)

object ImportSyncNotes {
    const val NOTE = "Replace StubStudentExcelImporter with an Apache POI based importer or a backend upload API. The repository already supports upsert by registration number so new Excel rows can be merged safely."
}

fun DemoSchoolRepository.mergeImportedStudents(records: List<StudentImportRecord>) {
    records.forEach { record ->
        upsertStudent(
            Student(
                id = "student-${record.registrationNumber}",
                name = record.name,
                trade = record.trade,
                registrationNumber = record.registrationNumber,
                traineeNumber = record.traineeNumber,
                mobileNumber = record.mobileNumber,
                dob = record.dob,
                gender = record.gender,
                caste = record.caste,
                casteCategory = record.casteCategory,
                fatherName = record.fatherName,
                motherName = record.motherName,
                email = "${record.name.lowercase().replace(" ", "-")}@ITI.com",
                weeklySchedule = DayOfWeek.entries.associateWith { emptyList<LectureSlot>() },
                attendanceEntries = listOf(
                    AttendanceEntry(
                        date = LocalDate.now(),
                        subject = "Imported Profile",
                        isPresent = true
                    )
                )
            )
        )
    }
}
