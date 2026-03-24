package com.example.firstapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firstapp.data.DemoSchoolRepository
import com.example.firstapp.data.ImportSyncNotes
import com.example.firstapp.data.model.Announcement
import com.example.firstapp.data.model.AttendanceStatusRow
import com.example.firstapp.data.model.ExamItem
import com.example.firstapp.data.model.NameSortOrder
import com.example.firstapp.data.model.Student
import com.example.firstapp.data.model.StudentSortOption
import com.example.firstapp.data.model.Teacher
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

@Composable
fun TeacherDashboardScreen(
    teacher: Teacher,
    repository: DemoSchoolRepository,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mark Attendance", "View Attendance", "Student Details", "Exam Schedule", "Announcements")
    val trades = remember(repository.students.size) { repository.availableTrades() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DashboardHeader(
                title = "Teacher Dashboard",
                subtitle = "${teacher.name} | ${trades.size} trades visible",
                onLogout = onLogout
            )
        }
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
        }
        item {
            AnimatedContent(targetState = selectedTab, label = "teacher-tabs") { tab ->
                when (tab) {
                    0 -> MarkAttendanceTab(teacher = teacher, repository = repository)
                    1 -> ViewAttendanceTab(teacher = teacher, repository = repository)
                    2 -> StudentDetailsTab(repository = repository)
                    3 -> ExamScheduleTab(teacher = teacher, repository = repository)
                    else -> AnnouncementsTab(teacher = teacher, repository = repository)
                }
            }
        }
    }
}

@Composable
private fun MarkAttendanceTab(
    teacher: Teacher,
    repository: DemoSchoolRepository
) {
    val allTrades = remember(repository.students.size) { repository.availableTrades() }
    var selectedTrade by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedIds by remember { mutableStateOf(repository.presentStudentIdsForTradeOnDate(selectedTrade, selectedDate)) }
    var nameSortOrder by remember { mutableStateOf(NameSortOrder.ASCENDING) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val students = repository.studentsForTrade(selectedTrade, StudentSortOption.NAME).let {
        when (nameSortOrder) {
            NameSortOrder.ASCENDING -> it.sortedBy { student -> student.name }
            NameSortOrder.DESCENDING -> it.sortedByDescending { student -> student.name }
        }
    }

    LaunchedEffect(selectedTrade, selectedDate, repository.students.size) {
        val savedIds = repository.presentStudentIdsForTradeOnDate(selectedTrade, selectedDate)
        selectedIds = if (savedIds.isNotEmpty()) savedIds else repository.studentsForTrade(selectedTrade, StudentSortOption.NAME).map { it.id }.toSet()
    }

    DashboardSectionCard(
        title = "Mark Attendance",
        subtitle = "Pick a date, choose a trade, and mark the full student list for that trade."
    ) {
        DateSelectorField(
            label = "Attendance Date",
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                TradeSelector(
                    trades = allTrades,
                    selectedTrade = if (selectedTrade.isBlank()) "Select Trade" else selectedTrade,
                    onTradeSelected = { selectedTrade = it }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FilterSelector(
                    selected = nameSortOrder,
                    onSelected = { nameSortOrder = it }
                )
            }
        }
        AttendanceSummaryBanner(
            title = "${students.size} students loaded",
            subtitle = "Present selected: ${selectedIds.size} | Date: $selectedDate"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { selectedIds = students.map { it.id }.toSet() },
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE7F7EA),
                    contentColor = Color(0xFF247A31)
                )
            ) {
                Text("All Present")
            }
            Button(
                onClick = { selectedIds = emptySet() },
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFECE8),
                    contentColor = Color(0xFFB33A2B)
                )
            ) {
                Text("All Absent")
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(students) { student ->
                GlassRowCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${student.registrationNumber} | ${student.traineeNumber.ifBlank { "-" }}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusToggle(
                            isPresent = selectedIds.contains(student.id),
                            onToggle = { makePresent ->
                                selectedIds = if (makePresent) selectedIds + student.id else selectedIds - student.id
                            }
                        )
                    }
                }
            }
            if (students.isEmpty()) {
                item {
                    AttendanceSummaryBanner(
                        title = "No students loaded",
                        subtitle = "Select a trade to load the student list."
                    )
                }
            }
        }
        Button(
            onClick = {
                if (selectedTrade.isNotBlank()) {
                    repository.markAttendance(selectedTrade, selectedDate, selectedIds)
                    saveMessage = "Attendance saved for $selectedTrade on $selectedDate."
                } else {
                    saveMessage = "Please select a trade first."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Attendance")
        }
        saveMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun AnnouncementsTab(
    teacher: Teacher,
    repository: DemoSchoolRepository
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardSectionCard(
            title = "Announcements",
            subtitle = "Broadcast messages to all students."
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Announcement Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Announcement Message") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        repository.addAnnouncement(
                            Announcement(
                                id = "announcement-${Random.nextInt(1000, 9999)}",
                                title = title,
                                message = message,
                                teacherName = teacher.name,
                                createdDate = LocalDate.now().toString()
                            )
                        )
                        title = ""
                        message = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Make Announcement")
            }
        }
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Broadcasted Messages", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                repository.announcements.forEach { announcement ->
                    GlassRowCard {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(announcement.title, fontWeight = FontWeight.Bold)
                            Text(announcement.message)
                            Text(
                                "${announcement.teacherName} | ${announcement.createdDate}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewAttendanceTab(
    teacher: Teacher,
    repository: DemoSchoolRepository
) {
    val allTrades = remember(repository.students.size) { repository.availableTrades() }
    var selectedTrade by remember { mutableStateOf(allTrades.firstOrNull().orEmpty()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val attendanceRows = repository.attendanceForTradeOnDate(selectedTrade, selectedDate)
    val presentCount = attendanceRows.count { it.isPresent == true }
    val absentCount = attendanceRows.count { it.isPresent == false }
    val unmarkedCount = attendanceRows.count { it.isPresent == null }

    DashboardSectionCard(
        title = "View Attendance",
        subtitle = "Review attendance already marked for a selected trade and date."
    ) {
        DateSelectorField(
            label = "View Date",
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )
        TradeChipSelector(
            trades = allTrades,
            selectedTrade = selectedTrade,
            onTradeSelected = { selectedTrade = it }
        )
        AttendanceSummaryBanner(
            title = "Present: $presentCount | Absent: $absentCount",
            subtitle = "Not marked yet: $unmarkedCount"
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(attendanceRows) { row ->
                AttendanceViewRow(row)
            }
        }
    }
}

@Composable
private fun StudentDetailsTab(repository: DemoSchoolRepository) {
    var selectedTrade by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(StudentSortOption.LOW_ATTENDANCE) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    val trades = remember(repository.students.size) { repository.availableTrades() }
    val students = selectedTrade?.let { repository.studentsForTrade(it, sortOption) }.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardSectionCard(
            title = "Student Directory",
            subtitle = "Pick a trade first, then choose a student to view the complete profile."
        ) {
            TradeChipSelector(
                trades = trades,
                selectedTrade = selectedTrade,
                onTradeSelected = { selectedTrade = it }
            )
            SortSelector(sortOption = sortOption, onSelected = { sortOption = it })
            Text(
                text = "MongoDB / Excel sync seam: ${ImportSyncNotes.NOTE}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (selectedTrade == null) {
            AttendanceSummaryBanner(
                title = "Select a trade to view students",
                subtitle = "The student list will appear here after you choose a trade."
            )
        } else {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(students) { student ->
                        GlassRowCard(modifier = Modifier.clickable { selectedStudent = student }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(student.name, fontWeight = FontWeight.SemiBold)
                                    Text(student.registrationNumber, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${student.attendanceSummary.percentage}%")
                            }
                        }
                    }
                }
            }
        }
    }

    selectedStudent?.let { student ->
        AlertDialog(
            onDismissRequest = { selectedStudent = null },
            confirmButton = { TextButton(onClick = { selectedStudent = null }) { Text("Close") } },
            title = { Text(student.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailLine("Trade", student.trade)
                    DetailLine("Registration Number", student.registrationNumber)
                    DetailLine("Trainee Number", student.traineeNumber)
                    DetailLine("Mobile Number", student.mobileNumber)
                    DetailLine("DOB", student.dob)
                    DetailLine("Gender", student.gender)
                    DetailLine("Caste", student.caste)
                    DetailLine("Caste Category", student.casteCategory)
                    DetailLine("Father Name", student.fatherName)
                    DetailLine("Mother Name", student.motherName)
                    DetailLine("Attendance", "${student.attendanceSummary.presentClasses}/${student.attendanceSummary.totalClasses} (${student.attendanceSummary.percentage}%)")
                }
            }
        )
    }
}

@Composable
private fun ExamScheduleTab(
    teacher: Teacher,
    repository: DemoSchoolRepository
) {
    val allTrades = remember(repository.students.size) { repository.availableTrades() }
    var trade by remember { mutableStateOf(allTrades.firstOrNull().orEmpty()) }
    var subject by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-04-15") }
    var time by remember { mutableStateOf("10:00") }
    var room by remember { mutableStateOf("Room 1") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardSectionCard(
            title = "Add Exam Schedule",
            subtitle = "Publish exam dates trade-wise so students can see the latest plan immediately."
        ) {
            TradeSelector(trades = allTrades, selectedTrade = trade, onTradeSelected = { trade = it })
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        repository.addExam(
                            ExamItem(
                                id = "exam-${Random.nextInt(1000, 9999)}",
                                trade = trade,
                                subject = subject,
                                date = date,
                                time = time,
                                room = room
                            )
                        )
                        subject = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Exam Schedule")
            }
        }
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Scheduled Exams", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                repository.examsForTrade("All Trades").forEach { exam ->
                    GlassRowCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("${exam.trade} | ${exam.subject}", fontWeight = FontWeight.SemiBold)
                                Text("${exam.date} | ${exam.time}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(exam.room)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFDFEFF), Color(0xFFF4F8FF))
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun AttendanceSummaryBanner(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF4FF))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AttendanceViewRow(row: AttendanceStatusRow) {
    val statusLabel = when (row.isPresent) {
        true -> "Present"
        false -> "Absent"
        null -> "Not Marked"
    }
    val statusColor = when (row.isPresent) {
        true -> Color(0xFFDCF6E8)
        false -> Color(0xFFFFE1D9)
        null -> Color(0xFFECEFF3)
    }

    GlassRowCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(row.student.name, fontWeight = FontWeight.SemiBold)
                Text(row.student.registrationNumber, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = statusLabel,
                modifier = Modifier
                    .background(statusColor, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GlassRowCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f))
    ) {
        content()
    }
}

@Composable
private fun StatusToggle(
    isPresent: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AttendanceMarkChip(
            label = "P",
            selected = isPresent,
            selectedColor = Color(0xFFE5F5E6),
            selectedTextColor = Color(0xFF3FAE4C),
            onClick = { onToggle(true) }
        )
        AttendanceMarkChip(
            label = "A",
            selected = !isPresent,
            selectedColor = Color(0xFFFFE8E7),
            selectedTextColor = Color(0xFFF34A3D),
            onClick = { onToggle(false) }
        )
    }
}

@Composable
private fun AttendanceMarkChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    selectedTextColor: Color,
    onClick: () -> Unit
) {
    Text(
        text = label,
        modifier = Modifier
            .background(
                color = if (selected) selectedColor else Color(0xFFF1F4F8),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        color = if (selected) selectedTextColor else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectorField(
    label: String,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(selectedDate.toString(), fontWeight = FontWeight.SemiBold)
        }
    }

    if (showPicker) {
        DatePickerDialogField(
            selectedDate = selectedDate,
            onDismiss = { showPicker = false },
            onDateSelected = {
                onDateSelected(it)
                showPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogField(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    onDateSelected(date)
                }
            ) { Text("Select") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun TradeChipSelector(
    trades: List<String>,
    selectedTrade: String?,
    onTradeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        trades.forEach { trade ->
            FilterChip(
                selected = selectedTrade == trade,
                onClick = { onTradeSelected(trade) },
                label = { Text(trade) }
            )
        }
    }
}

@Composable
private fun TradeSelector(
    trades: List<String>,
    selectedTrade: String,
    onTradeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Select Trade", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(selectedTrade, fontWeight = FontWeight.SemiBold)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            trades.forEach { trade ->
                DropdownMenuItem(
                    text = { Text(trade) },
                    onClick = {
                        onTradeSelected(trade)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterSelector(
    selected: NameSortOrder,
    onSelected: (NameSortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Filter", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(selected.label, fontWeight = FontWeight.SemiBold)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            NameSortOrder.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SortSelector(
    sortOption: StudentSortOption,
    onSelected: (StudentSortOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Sort Students", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StudentSortOption.entries.forEach { option ->
                FilterChip(
                    selected = option == sortOption,
                    onClick = { onSelected(option) },
                    label = { Text(option.label) }
                )
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Text("$label: $value")
}
