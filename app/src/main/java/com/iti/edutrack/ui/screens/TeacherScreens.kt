package com.iti.edutrack.ui.screens

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.edutrack.data.ImportSyncNotes
import com.iti.edutrack.data.model.AttendanceStatusRow
import com.iti.edutrack.data.model.NameSortOrder
import com.iti.edutrack.data.model.Student
import com.iti.edutrack.data.model.StudentSortOption
import com.iti.edutrack.ui.viewmodel.AnnouncementsViewModel
import com.iti.edutrack.ui.viewmodel.ExamScheduleViewModel
import com.iti.edutrack.ui.viewmodel.MarkAttendanceViewModel
import com.iti.edutrack.ui.viewmodel.StudentDetailsViewModel
import com.iti.edutrack.ui.viewmodel.TeacherDashboardViewModel
import com.iti.edutrack.ui.viewmodel.ViewAttendanceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TeacherDashboardScreen(
    teacherDashboardViewModel: TeacherDashboardViewModel,
    markAttendanceViewModel: MarkAttendanceViewModel,
    viewAttendanceViewModel: ViewAttendanceViewModel,
    studentDetailsViewModel: StudentDetailsViewModel,
    examScheduleViewModel: ExamScheduleViewModel,
    announcementsViewModel: AnnouncementsViewModel,
    onLogout: () -> Unit
) {
    val uiState = teacherDashboardViewModel.uiState
    val teacher = teacherDashboardViewModel.teacher ?: return
    val tabs = listOf("Mark Attendance", "View Attendance", "Student Details", "Exam Schedule", "Announcements")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DashboardHeader(
                title = "Teacher Dashboard",
                subtitle = "${teacher.name} | ${teacherDashboardViewModel.visibleTrades.size} trades visible",
                onLogout = onLogout
            )
        }
        item {
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color.Transparent,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { teacherDashboardViewModel.onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }
        }
        item {
            AnimatedContent(targetState = uiState.selectedTab, label = "teacher-tabs") { tab ->
                when (tab) {
                    0 -> MarkAttendanceTab(markAttendanceViewModel)
                    1 -> ViewAttendanceTab(viewAttendanceViewModel)
                    2 -> StudentDetailsTab(studentDetailsViewModel)
                    3 -> ExamScheduleTab(examScheduleViewModel)
                    else -> AnnouncementsTab(teacher.name, announcementsViewModel)
                }
            }
        }
    }
}

@Composable
private fun MarkAttendanceTab(viewModel: MarkAttendanceViewModel) {
    val uiState = viewModel.uiState
    val students = viewModel.students

    DashboardSectionCard(
        title = "Mark Attendance",
        subtitle = "Pick a date, choose a trade, and mark the full student list for that trade."
    ) {
        DateSelectorField(
            label = "Attendance Date",
            selectedDate = uiState.selectedDate,
            onDateSelected = viewModel::onDateSelected
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                TradeSelector(
                    trades = viewModel.availableTrades,
                    selectedTrade = if (uiState.selectedTrade.isBlank()) "Select Trade" else uiState.selectedTrade,
                    onTradeSelected = viewModel::onTradeSelected
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FilterSelector(
                    selected = uiState.nameSortOrder,
                    onSelected = viewModel::onFilterSelected
                )
            }
        }
        AttendanceSummaryBanner(
            title = "${students.size} students loaded",
            subtitle = "Present selected: ${uiState.selectedIds.size} | Date: ${uiState.selectedDate}"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = viewModel::markAllPresent,
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
                onClick = viewModel::markAllAbsent,
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
                            isPresent = uiState.selectedIds.contains(student.id),
                            onToggle = { makePresent -> viewModel.toggleStudent(student.id, makePresent) }
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
        Button(onClick = viewModel::saveAttendance, modifier = Modifier.fillMaxWidth()) {
            Text("Save Attendance")
        }
        uiState.saveMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun ViewAttendanceTab(viewModel: ViewAttendanceViewModel) {
    val uiState = viewModel.uiState
    LaunchedEffect(Unit) {
        viewModel.ensureDefaultTrade()
    }

    DashboardSectionCard(
        title = "View Attendance",
        subtitle = "Review attendance already marked for a selected trade and date."
    ) {
        DateSelectorField(
            label = "View Date",
            selectedDate = uiState.selectedDate,
            onDateSelected = viewModel::onDateSelected
        )
        TradeChipSelector(
            trades = viewModel.availableTrades,
            selectedTrade = uiState.selectedTrade,
            onTradeSelected = viewModel::onTradeSelected
        )
        AttendanceSummaryBanner(
            title = "Present: ${viewModel.presentCount} | Absent: ${viewModel.absentCount}",
            subtitle = "Not marked yet: ${viewModel.unmarkedCount}"
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.attendanceRows) { row ->
                AttendanceViewRow(row)
            }
        }
    }
}

@Composable
private fun StudentDetailsTab(viewModel: StudentDetailsViewModel) {
    val uiState = viewModel.uiState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardSectionCard(
            title = "Student Directory",
            subtitle = "Pick a trade first, then choose a student to view the complete profile."
        ) {
            TradeChipSelector(
                trades = viewModel.availableTrades,
                selectedTrade = uiState.selectedTrade,
                onTradeSelected = viewModel::onTradeSelected
            )
            SortSelector(sortOption = uiState.sortOption, onSelected = viewModel::onSortSelected)
            Text(
                text = "MongoDB / Excel sync seam: ${ImportSyncNotes.NOTE}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (uiState.selectedTrade == null) {
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
                    items(viewModel.students) { student ->
                        GlassRowCard(modifier = Modifier.clickable { viewModel.onStudentSelected(student) }) {
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

    uiState.selectedStudent?.let { student ->
        AlertDialog(
            onDismissRequest = viewModel::closeDialog,
            confirmButton = { TextButton(onClick = viewModel::closeDialog) { Text("Close") } },
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
private fun ExamScheduleTab(viewModel: ExamScheduleViewModel) {
    val uiState = viewModel.uiState
    LaunchedEffect(Unit) {
        viewModel.ensureDefaultTrade()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardSectionCard(
            title = "Add Exam Schedule",
            subtitle = "Publish exam dates trade-wise so students can see the latest plan immediately."
        ) {
            TradeSelector(trades = viewModel.availableTrades, selectedTrade = uiState.trade, onTradeSelected = viewModel::onTradeSelected)
            OutlinedTextField(value = uiState.subject, onValueChange = viewModel::onSubjectChanged, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = uiState.date, onValueChange = viewModel::onDateChanged, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = uiState.time, onValueChange = viewModel::onTimeChanged, label = { Text("Time") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = uiState.room, onValueChange = viewModel::onRoomChanged, label = { Text("Room") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = viewModel::saveExam, modifier = Modifier.fillMaxWidth()) {
                Text("Save Exam Schedule")
            }
        }
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Scheduled Exams", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                viewModel.exams.forEach { exam ->
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
private fun AnnouncementsTab(teacherName: String, viewModel: AnnouncementsViewModel) {
    val uiState = viewModel.uiState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardSectionCard(
            title = "Announcements",
            subtitle = "Broadcast messages to all students."
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = { Text("Announcement Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.message,
                onValueChange = viewModel::onMessageChanged,
                label = { Text("Announcement Message") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.makeAnnouncement(teacherName) },
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
                viewModel.announcements.forEach { announcement ->
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
private fun GlassRowCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f))
    ) {
        content()
    }
}

@Composable
private fun StatusToggle(isPresent: Boolean, onToggle: (Boolean) -> Unit) {
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
