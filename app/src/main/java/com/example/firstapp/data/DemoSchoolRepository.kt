package com.example.firstapp.data

import androidx.compose.runtime.mutableStateListOf
import com.example.firstapp.data.model.Announcement
import com.example.firstapp.data.model.AttendanceEntry
import com.example.firstapp.data.model.AttendanceStatusRow
import com.example.firstapp.data.model.ExamItem
import com.example.firstapp.data.model.LectureSlot
import com.example.firstapp.data.model.LoginResult
import com.example.firstapp.data.model.Student
import com.example.firstapp.data.model.StudentSortOption
import com.example.firstapp.data.model.Teacher
import com.example.firstapp.data.model.UserRole
import java.time.DayOfWeek
import java.time.LocalDate

class DemoSchoolRepository(importedRecords: List<com.example.firstapp.data.model.StudentImportRecord> = emptyList()) {
    val students = mutableStateListOf<Student>()
    val teachers = mutableStateListOf<Teacher>()
    val exams = mutableStateListOf<ExamItem>()
    val announcements = mutableStateListOf<Announcement>()

    init {
        students += if (importedRecords.isNotEmpty()) {
            importedRecords.mapIndexed { index, record -> studentFromImportRecord(record, index) }
        } else {
            seedStudents()
        }
        teachers += seedTeachers()
        exams += seedExams()
        announcements += seedAnnouncements()
    }

    fun login(role: UserRole, email: String, password: String): LoginResult {
        return when (role) {
            UserRole.STUDENT -> {
                val normalizedEmail = email.trim()
                val student = students.firstOrNull {
                    it.email.equals(normalizedEmail, ignoreCase = true) ||
                        generatedStudentEmail(it.name).equals(normalizedEmail, ignoreCase = true)
                }
                when {
                    student == null -> LoginResult(role = role, errorMessage = "Student account not found.")
                    generatedStudentPassword(student.dob) != password.trim() -> LoginResult(
                        role = role,
                        errorMessage = "Student password should be DOB in DDMMYYYY format, for example 01082008."
                    )
                    else -> LoginResult(role = role, student = student)
                }
            }

            UserRole.TEACHER -> {
                val teacher = teachers.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
                when {
                    teacher == null -> LoginResult(role = role, errorMessage = "Teacher account not found.")
                    teacher.password != password.trim() -> LoginResult(role = role, errorMessage = "Incorrect teacher password.")
                    else -> LoginResult(role = role, teacher = teacher)
                }
            }
        }
    }

    fun refreshStudent(studentId: String): Student? = students.firstOrNull { it.id == studentId }

    fun markAttendance(trade: String, date: LocalDate, presentIds: Set<String>) {
        val indexedStudents = students.withIndex().filter { it.value.trade == trade }
        indexedStudents.forEach { indexedValue ->
            val student = indexedValue.value
            val existingEntries = student.attendanceEntries.filterNot {
                it.date == date && it.subject == "Daily Attendance"
            }
            val updatedEntry = AttendanceEntry(
                date = date,
                subject = "Daily Attendance",
                isPresent = presentIds.contains(student.id)
            )
            students[indexedValue.index] = student.copy(attendanceEntries = existingEntries + updatedEntry)
        }
    }

    fun upsertStudent(student: Student) {
        val index = students.indexOfFirst { it.registrationNumber == student.registrationNumber }
        if (index >= 0) {
            students[index] = student
        } else {
            students += student
        }
    }

    fun addExam(item: ExamItem) {
        exams += item
    }

    fun addAnnouncement(item: Announcement) {
        announcements.add(0, item)
    }

    fun availableTrades(): List<String> = students.map { it.trade }.distinct().sorted()

    fun studentsForTrade(trade: String, sortOption: StudentSortOption): List<Student> {
        val filtered = students.filter { trade == "All Trades" || it.trade == trade }
        return when (sortOption) {
            StudentSortOption.LOW_ATTENDANCE -> filtered.sortedBy { it.attendanceSummary.percentage }
            StudentSortOption.HIGH_ATTENDANCE -> filtered.sortedByDescending { it.attendanceSummary.percentage }
            StudentSortOption.NAME -> filtered.sortedBy { it.name }
            StudentSortOption.CASTE -> filtered.sortedBy { it.caste }
            StudentSortOption.TRADE -> filtered.sortedBy { it.trade }
        }
    }

    fun presentStudentIdsForTradeOnDate(trade: String, date: LocalDate): Set<String> {
        return students
            .filter { it.trade == trade }
            .mapNotNull { student ->
                val record = student.attendanceEntries.lastOrNull {
                    it.date == date && it.subject == "Daily Attendance"
                }
                if (record?.isPresent == true) student.id else null
            }
            .toSet()
    }

    fun attendanceForTradeOnDate(trade: String, date: LocalDate): List<AttendanceStatusRow> {
        return students
            .filter { it.trade == trade }
            .sortedBy { it.name }
            .map { student ->
                val status = student.attendanceEntries.lastOrNull {
                    it.date == date && it.subject == "Daily Attendance"
                }?.isPresent
                AttendanceStatusRow(student = student, isPresent = status)
            }
    }

    fun examsForTrade(trade: String): List<ExamItem> = exams.filter { it.trade == trade || trade == "All Trades" }

    private fun seedTeachers(): List<Teacher> {
        val allTrades = if (students.isEmpty()) {
            listOf("Electrician", "Fitter", "Welder")
        } else {
            students.map { it.trade }.distinct().sorted()
        }
        return listOf(
            Teacher(
                id = "teacher-1",
                name = "Anita Sharma",
                email = "anita-sharma@ITI.com",
                password = "teach@123",
                trades = allTrades
            ),
            Teacher(
                id = "teacher-2",
                name = "Rahul Verma",
                email = "rahul-verma@ITI.com",
                password = "welder@123",
                trades = allTrades
            )
        )
    }

    private fun seedStudents(): List<Student> {
        return listOf(
            sampleStudent(
                id = "student-1",
                name = "Aman Kumar",
                trade = "Electrician",
                registrationNumber = "REG-E-101",
                traineeNumber = "TRN-5001",
                mobileNumber = "9876543210",
                dob = "15-08-2006",
                gender = "Male",
                caste = "OBC",
                casteCategory = "Non Creamy Layer",
                religion = "Hindu",
                fatherName = "Rajesh Kumar",
                motherName = "Sunita Devi",
                presentCount = 18,
                totalCount = 22
            ),
            sampleStudent(
                id = "student-2",
                name = "Neha Singh",
                trade = "Electrician",
                registrationNumber = "REG-E-102",
                traineeNumber = "TRN-5002",
                mobileNumber = "9988776655",
                dob = "04-11-2005",
                gender = "Female",
                caste = "SC",
                casteCategory = "Reserved",
                religion = "Hindu",
                fatherName = "Mohan Singh",
                motherName = "Poonam Singh",
                presentCount = 15,
                totalCount = 22
            ),
            sampleStudent(
                id = "student-5",
                name = "Priya Nair",
                trade = "Electrician",
                registrationNumber = "REG-E-103",
                traineeNumber = "TRN-5003",
                mobileNumber = "9090909090",
                dob = "12-03-2006",
                gender = "Female",
                caste = "General",
                casteCategory = "Open",
                religion = "Hindu",
                fatherName = "Rakesh Nair",
                motherName = "Anjali Nair",
                presentCount = 17,
                totalCount = 22
            ),
            sampleStudent(
                id = "student-6",
                name = "Sahil Das",
                trade = "Electrician",
                registrationNumber = "REG-E-104",
                traineeNumber = "TRN-5004",
                mobileNumber = "8080808080",
                dob = "21-06-2005",
                gender = "Male",
                caste = "ST",
                casteCategory = "Reserved",
                religion = "Hindu",
                fatherName = "Bikash Das",
                motherName = "Mala Das",
                presentCount = 14,
                totalCount = 22
            ),
            sampleStudent(
                id = "student-3",
                name = "Vikram Patel",
                trade = "Fitter",
                registrationNumber = "REG-F-201",
                traineeNumber = "TRN-6001",
                mobileNumber = "9123456780",
                dob = "29-02-2004",
                gender = "Male",
                caste = "General",
                casteCategory = "Open",
                religion = "Hindu",
                fatherName = "Suresh Patel",
                motherName = "Meena Patel",
                presentCount = 20,
                totalCount = 24
            ),
            sampleStudent(
                id = "student-7",
                name = "Ritu Yadav",
                trade = "Fitter",
                registrationNumber = "REG-F-202",
                traineeNumber = "TRN-6002",
                mobileNumber = "9345678123",
                dob = "09-09-2005",
                gender = "Female",
                caste = "OBC",
                casteCategory = "Non Creamy Layer",
                religion = "Hindu",
                fatherName = "Mahesh Yadav",
                motherName = "Kamla Yadav",
                presentCount = 18,
                totalCount = 24
            ),
            sampleStudent(
                id = "student-8",
                name = "Deepak Roy",
                trade = "Fitter",
                registrationNumber = "REG-F-203",
                traineeNumber = "TRN-6003",
                mobileNumber = "9234567812",
                dob = "03-12-2004",
                gender = "Male",
                caste = "SC",
                casteCategory = "Reserved",
                religion = "Hindu",
                fatherName = "Pradeep Roy",
                motherName = "Rekha Roy",
                presentCount = 16,
                totalCount = 24
            ),
            sampleStudent(
                id = "student-4",
                name = "Farhan Ali",
                trade = "Welder",
                registrationNumber = "REG-W-301",
                traineeNumber = "TRN-7001",
                mobileNumber = "9012345678",
                dob = "17-01-2005",
                gender = "Male",
                caste = "Minority",
                casteCategory = "Reserved",
                religion = "Islam",
                fatherName = "Parvez Ali",
                motherName = "Shabana Ali",
                presentCount = 13,
                totalCount = 20
            ),
            sampleStudent(
                id = "student-9",
                name = "Imran Khan",
                trade = "Welder",
                registrationNumber = "REG-W-302",
                traineeNumber = "TRN-7002",
                mobileNumber = "9456123780",
                dob = "24-04-2005",
                gender = "Male",
                caste = "OBC",
                casteCategory = "Non Creamy Layer",
                religion = "Islam",
                fatherName = "Salim Khan",
                motherName = "Nasreen Khan",
                presentCount = 15,
                totalCount = 20
            ),
            sampleStudent(
                id = "student-10",
                name = "Kavya Joshi",
                trade = "Welder",
                registrationNumber = "REG-W-303",
                traineeNumber = "TRN-7003",
                mobileNumber = "9567012345",
                dob = "10-10-2005",
                gender = "Female",
                caste = "General",
                casteCategory = "Open",
                religion = "Hindu",
                fatherName = "Nitin Joshi",
                motherName = "Sonal Joshi",
                presentCount = 18,
                totalCount = 20
            )
        )
    }

    private fun sampleStudent(
        id: String,
        name: String,
        trade: String,
        registrationNumber: String,
        traineeNumber: String,
        mobileNumber: String,
        dob: String,
        gender: String,
        caste: String,
        casteCategory: String,
        religion: String,
        fatherName: String,
        motherName: String,
        presentCount: Int,
        totalCount: Int
    ): Student {
        return Student(
            id = id,
            name = name,
            trade = trade,
            registrationNumber = registrationNumber,
            traineeNumber = traineeNumber,
            mobileNumber = mobileNumber,
            dob = dob,
            gender = gender,
            caste = caste,
            casteCategory = casteCategory,
            religion = religion,
            fatherName = fatherName,
            motherName = motherName,
            email = "${name.lowercase().replace(" ", "-")}@ITI.com",
            weeklySchedule = buildSchedule(trade),
            attendanceEntries = List(totalCount) { index ->
                AttendanceEntry(
                    date = LocalDate.now().minusDays((totalCount - index).toLong()),
                    subject = listOf("Workshop", "Maths", "Engineering Drawing", "Employability Skills")[index % 4],
                    isPresent = index < presentCount,
                    period = "LECT SLOT ${(index % 5) + 1}"
                )
            }
        )
    }

    private fun buildSchedule(trade: String): Map<DayOfWeek, List<LectureSlot>> {
        fun slots(vararg values: LectureSlot) = values.toList()
        return mapOf(
            DayOfWeek.MONDAY to slots(
                LectureSlot("$trade Theory", "09:00 - 10:00", "P"),
                LectureSlot("Workshop Practice", "10:15 - 12:00", "P"),
                LectureSlot("Employability Skills", "01:00 - 02:00", "A")
            ),
            DayOfWeek.TUESDAY to slots(
                LectureSlot("Engineering Drawing", "09:00 - 10:30", "P"),
                LectureSlot("$trade Lab", "10:45 - 12:30", "P")
            ),
            DayOfWeek.WEDNESDAY to slots(
                LectureSlot("Maths", "09:00 - 10:00", "P"),
                LectureSlot("Trade Practical", "10:15 - 12:15", "P")
            ),
            DayOfWeek.THURSDAY to slots(
                LectureSlot("Workshop Calculation", "09:00 - 10:00", "A"),
                LectureSlot("Safety", "10:15 - 11:15", "P")
            ),
            DayOfWeek.FRIDAY to slots(
                LectureSlot("$trade Demo", "09:00 - 11:00", "P"),
                LectureSlot("Revision", "11:15 - 12:00", "P")
            ),
            DayOfWeek.SATURDAY to slots(
                LectureSlot("Assessment", "09:00 - 10:00", "P")
            ),
            DayOfWeek.SUNDAY to emptyList()
        )
    }

    private fun seedExams(): List<ExamItem> {
        return listOf(
            ExamItem("exam-1", "Electrician", "Trade Theory", "2026-04-05", "10:00", "Lab 2"),
            ExamItem("exam-2", "Fitter", "Practical Viva", "2026-04-08", "11:30", "Workshop"),
            ExamItem("exam-3", "Welder", "Safety Assessment", "2026-04-10", "09:30", "Room 5"),
            ExamItem("exam-4", "Architectural Draughtsman (NSQF)", "Trade Drawing", "2026-04-12", "09:00", "Studio 1")
        )
    }

    private fun seedAnnouncements(): List<Announcement> {
        return listOf(
            Announcement(
                id = "announcement-1",
                title = "Welcome Notice",
                message = "All students should check their exam schedule and attendance every week.",
                teacherName = "Anita Sharma",
                createdDate = "2026-03-25"
            )
        )
    }
}

fun generatedStudentEmail(name: String): String {
    val localPart = name.lowercase().filter { it.isLetterOrDigit() }
    return "$localPart@ITI.com"
}

fun generatedStudentPassword(dob: String): String {
    return dob.filter { it.isDigit() }
}
