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
    records.forEachIndexed { index, record ->
        upsertStudent(studentFromImportRecord(record, index))
    }
}
