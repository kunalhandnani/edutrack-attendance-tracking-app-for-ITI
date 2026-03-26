package com.iti.edutrack.data

import android.content.Context
import com.iti.edutrack.data.model.AttendanceEntry
import com.iti.edutrack.data.model.LectureSlot
import com.iti.edutrack.data.model.Student
import com.iti.edutrack.data.model.StudentImportRecord
import org.json.JSONArray
import java.time.DayOfWeek
import java.time.LocalDate

object StudentAssetLoader {
    fun loadFromAssets(context: Context, fileName: String = "students_2025.json"): List<StudentImportRecord> {
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    StudentImportRecord(
                        name = item.optString("name"),
                        trade = item.optString("trade"),
                        registrationNumber = item.optString("registrationNumber"),
                        traineeNumber = item.optString("traineeNumber"),
                        mobileNumber = item.optString("mobileNumber"),
                        dob = item.optString("dob"),
                        gender = item.optString("gender"),
                        caste = item.optString("caste"),
                        casteCategory = item.optString("casteCategory"),
                        religion = item.optString("religion"),
                        fatherName = item.optString("fatherName"),
                        motherName = item.optString("motherName"),
                        email = item.optString("email"),
                        address = item.optString("address"),
                        admissionDate = item.optString("admissionDate"),
                        tradeCode = item.optString("tradeCode"),
                        isImc = item.optString("isImc")
                    )
                )
            }
        }
    }
}

fun studentFromImportRecord(record: StudentImportRecord, index: Int): Student {
    val subjects = subjectTemplatesForTrade(record.trade)
    val attendanceEntries = buildList {
        subjects.forEachIndexed { subjectIndex, subject ->
            repeat(6) { lectureIndex ->
                add(
                    AttendanceEntry(
                        date = LocalDate.of(2026, 1, 10).plusDays((subjectIndex * 7L) + lectureIndex),
                        subject = subject,
                        isPresent = ((index + subjectIndex + lectureIndex) % 3) != 0,
                        period = "LECT SLOT ${((lectureIndex + subjectIndex) % 5) + 1}"
                    )
                )
            }
        }
    }

    return Student(
        id = "student-${record.registrationNumber.ifBlank { index.toString() }}",
        name = record.name,
        trade = record.trade,
        registrationNumber = record.registrationNumber,
        traineeNumber = record.traineeNumber,
        mobileNumber = record.mobileNumber,
        dob = record.dob,
        gender = record.gender,
        caste = record.caste,
        casteCategory = record.casteCategory,
        religion = record.religion,
        fatherName = record.fatherName,
        motherName = record.motherName,
        email = generatedStudentEmail(record.name),
        address = record.address,
        admissionDate = record.admissionDate,
        tradeCode = record.tradeCode,
        isImc = record.isImc,
        weeklySchedule = buildScheduleFromSubjects(subjects),
        attendanceEntries = attendanceEntries
    )
}

private fun subjectTemplatesForTrade(trade: String): List<String> {
    val slug = trade.lowercase()
    return when {
        "architect" in slug -> listOf("Architectural Drawing", "Building Materials", "Survey Basics", "Workshop")
        "electric" in slug -> listOf("Electrical Theory", "Wiring Practice", "Workshop Calculation", "Engineering Drawing")
        "fitter" in slug -> listOf("Fitting Theory", "Machine Shop", "Engineering Drawing", "Workshop Science")
        "welder" in slug -> listOf("Welding Theory", "Gas Welding Lab", "Arc Welding Lab", "Safety")
        "computer" in slug || "it" in slug -> listOf("Artificial Intelligence", "Computer Networks", "Programming Lab", "Data Structures")
        else -> listOf("Trade Theory", "Practical Lab", "Workshop Calculation", "Employability Skills")
    }
}

private fun buildScheduleFromSubjects(subjects: List<String>): Map<DayOfWeek, List<LectureSlot>> {
    val rotated = if (subjects.isEmpty()) listOf("Theory") else subjects
    return mapOf(
        DayOfWeek.MONDAY to listOf(
            LectureSlot(rotated[0], "09:00 - 10:00", "P"),
            LectureSlot(rotated[1 % rotated.size], "10:15 - 11:15", "P")
        ),
        DayOfWeek.TUESDAY to listOf(
            LectureSlot(rotated[2 % rotated.size], "09:00 - 10:00", "P"),
            LectureSlot(rotated[3 % rotated.size], "10:15 - 11:15", "A")
        ),
        DayOfWeek.WEDNESDAY to listOf(
            LectureSlot(rotated[0], "09:00 - 10:00", "P"),
            LectureSlot(rotated[2 % rotated.size], "10:15 - 11:15", "P")
        ),
        DayOfWeek.THURSDAY to listOf(
            LectureSlot(rotated[1 % rotated.size], "09:00 - 10:00", "P"),
            LectureSlot(rotated[3 % rotated.size], "10:15 - 11:15", "P")
        ),
        DayOfWeek.FRIDAY to listOf(
            LectureSlot(rotated[0], "09:00 - 10:00", "A"),
            LectureSlot(rotated[1 % rotated.size], "10:15 - 11:15", "P")
        ),
        DayOfWeek.SATURDAY to listOf(
            LectureSlot(rotated[2 % rotated.size], "09:00 - 10:00", "P")
        ),
        DayOfWeek.SUNDAY to emptyList()
    )
}
