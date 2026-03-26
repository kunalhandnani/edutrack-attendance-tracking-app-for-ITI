package com.iti.edutrack.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iti.edutrack.data.model.Announcement
import com.iti.edutrack.data.model.AttendanceEntry
import com.iti.edutrack.data.model.ExamItem
import com.iti.edutrack.data.model.LectureSlot
import com.iti.edutrack.data.model.Student
import com.iti.edutrack.data.model.SubjectAttendanceSummary
import com.iti.edutrack.ui.viewmodel.StudentDashboardViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StudentDashboardScreen(
    viewModel: StudentDashboardViewModel,
    onLogout: () -> Unit
) {
    val uiState = viewModel.uiState
    val student = viewModel.student ?: return
    val tabs = listOf("Overview", "Class Schedule", "Lectures", "Exam Schedule", "Announcements")

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            DashboardHeader(
                title = "Student Dashboard",
                subtitle = "${student.name} | ${student.trade}",
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
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }
        }
        item {
            AnimatedContent(targetState = uiState.selectedTab, label = "student-tabs") { tab ->
                when (tab) {
                    0 -> StudentOverview(student)
                    1 -> StudentSchedule(student)
                    2 -> StudentLectures(student)
                    3 -> StudentExams(viewModel.exams)
                    else -> StudentAnnouncements(viewModel.announcements)
                }
            }
        }
    }
}

@Composable
private fun StudentOverview(student: Student) {
    val summary = student.attendanceSummary
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFDFEFF), Color(0xFFF1F7FF))
                        )
                    )
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Attendance Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Exact attendance count with present and absent split.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Present: ${summary.presentClasses}/${summary.totalClasses}")
                    Text("Absent: ${summary.absentClasses}")
                    Text("Percentage: ${summary.percentage}%")
                }
                AttendancePieChart(
                    present = summary.presentClasses,
                    absent = summary.absentClasses,
                    modifier = Modifier.size(160.dp)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryChip("Registration", student.registrationNumber, Modifier.weight(1f))
            SummaryChip("Trainee No.", student.traineeNumber.ifBlank { "-" }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryChip("Mobile", student.mobileNumber, Modifier.weight(1f))
            SummaryChip("DOB", student.dob, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StudentSchedule(student: Student) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Weekly Class Schedule", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            DayOfWeek.entries.forEach { day ->
                DayScheduleRow(
                    dayLabel = day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    lectures = student.weeklySchedule[day].orEmpty()
                )
            }
        }
    }
}

@Composable
private fun StudentLectures(student: Student) {
    val grouped = remember(student.id, student.attendanceEntries) {
        student.attendanceEntries
            .filter { it.subject != "Daily Attendance" }
            .groupBy { it.subject }
            .map { (subject, entries) ->
                val present = entries.count { it.isPresent }
                val absent = entries.size - present
                SubjectAttendanceSummary(
                    subject = subject,
                    displayName = subject,
                    total = entries.size,
                    present = present,
                    absent = absent,
                    percentage = if (entries.isEmpty()) 0 else (present * 100) / entries.size,
                    entries = entries.sortedBy { it.date }
                )
            }
            .sortedBy { it.subject }
    }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        grouped.forEach { summary ->
            val isExpanded = expanded[summary.subject] == true
            LectureSummaryCard(
                summary = summary,
                expanded = isExpanded,
                onToggle = { expanded[summary.subject] = !isExpanded }
            )
        }
    }
}

@Composable
private fun LectureSummaryCard(
    summary: SubjectAttendanceSummary,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(summary.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(summary.subject, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("See All", color = Color(0xFF0A8A7C), fontWeight = FontWeight.SemiBold)
            }
            Text(
                "${summary.percentage}%",
                color = Color(0xFF3FAE4C),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            AttendanceBar(summary.present, summary.absent)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatsLabel("Total ${summary.total}", Color(0xFF8C8C8C))
                StatsLabel("Present ${summary.present}", Color(0xFF3FAE4C))
                StatsLabel("Absent ${summary.absent}", Color(0xFFF34A3D))
            }
            if (expanded) {
                LectureDetailsTable(summary.entries)
            }
        }
    }
}

@Composable
private fun LectureDetailsTable(entries: List<AttendanceEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F2F5))) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                HeaderCell("Sr. No.", 60.dp)
                HeaderCell("Date", 100.dp)
                HeaderCell("Period", 120.dp)
                HeaderCell("Status", 80.dp)
            }
        }
        entries.forEachIndexed { index, entry ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BodyCell("${index + 1}", 60.dp)
                    BodyCell(entry.date.toString(), 100.dp)
                    BodyCell(entry.period.ifBlank { "-" }, 120.dp)
                    StatusCell(entry.isPresent, 80.dp)
                }
            }
        }
    }
}

@Composable
private fun AttendanceBar(present: Int, absent: Int) {
    val total = (present + absent).coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .background(Color(0xFFE7ECEF), RoundedCornerShape(999.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(present.toFloat() / total.toFloat())
                .background(Color(0xFF49B34F), RoundedCornerShape(topStart = 999.dp, bottomStart = 999.dp))
        )
        Box(
            modifier = Modifier
                .weight(absent.toFloat() / total.toFloat())
                .background(Color(0xFFF34A3D), RoundedCornerShape(topEnd = 999.dp, bottomEnd = 999.dp))
        )
    }
}

@Composable
private fun StatsLabel(text: String, color: Color) {
    Text(text, color = color, fontWeight = FontWeight.Medium)
}

@Composable
private fun HeaderCell(text: String, cellWidth: Dp) {
    Text(text, modifier = Modifier.width(cellWidth), fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
}

@Composable
private fun BodyCell(text: String, cellWidth: Dp) {
    Text(text, modifier = Modifier.width(cellWidth))
}

@Composable
private fun StatusCell(isPresent: Boolean, cellWidth: Dp) {
    val bg = if (isPresent) Color(0xFFE5F5E6) else Color(0xFFFFE8E7)
    val fg = if (isPresent) Color(0xFF3FAE4C) else Color(0xFFF34A3D)
    Box(modifier = Modifier.width(cellWidth), contentAlignment = Alignment.CenterStart) {
        Text(
            if (isPresent) "P" else "A",
            modifier = Modifier
                .background(bg, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            color = fg,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StudentExams(exams: List<ExamItem>) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Exam Schedule", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            exams.forEach { exam ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(exam.subject, fontWeight = FontWeight.SemiBold)
                            Text("${exam.date} | ${exam.time}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(exam.room, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentAnnouncements(announcements: List<Announcement>) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Announcements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            announcements.forEach { announcement ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC))) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(announcement.title, fontWeight = FontWeight.Bold)
                        Text(announcement.message)
                        Text(
                            "By ${announcement.teacherName} | ${announcement.createdDate}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendancePieChart(present: Int, absent: Int, modifier: Modifier = Modifier) {
    val total = (present + absent).coerceAtLeast(1)
    val targetAngle = (present.toFloat() / total.toFloat()) * 360f
    val presentAngle by animateFloatAsState(targetValue = targetAngle, label = "pie-angle")

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.size(150.dp)) {
            drawArc(
                color = Color(0xFF3F8CFF),
                startAngle = -90f,
                sweepAngle = presentAngle,
                useCenter = false,
                style = Stroke(width = 38f, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
            drawArc(
                color = Color(0xFFFF7B54),
                startAngle = -90f + presentAngle,
                sweepAngle = 360f - presentAngle,
                useCenter = false,
                style = Stroke(width = 38f, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$present", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Present", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DayScheduleRow(dayLabel: String, lectures: List<LectureSlot>) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBFF))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(dayLabel, fontWeight = FontWeight.Bold)
            if (lectures.isEmpty()) {
                Text("No classes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                lectures.forEach { lecture ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lecture.subject, fontWeight = FontWeight.SemiBold)
                                Text(lecture.time, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("A/P: ${lecture.status}", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DashboardHeader(title: String, subtitle: String, onLogout: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEEF2FF),
                    contentColor = Color(0xFF213547)
                ),
                border = BorderStroke(1.dp, Color(0xFFD8DFEA))
            ) {
                Text("Logout", fontWeight = FontWeight.Medium)
            }
        }
    }
}
