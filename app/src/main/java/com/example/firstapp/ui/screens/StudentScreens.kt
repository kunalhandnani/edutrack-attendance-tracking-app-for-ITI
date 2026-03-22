package com.example.firstapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.firstapp.data.model.ExamItem
import com.example.firstapp.data.model.LectureSlot
import com.example.firstapp.data.model.Student
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StudentDashboardScreen(
    student: Student,
    exams: List<ExamItem>,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Class Schedule", "Exam Schedule")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardHeader(
            title = "Student Dashboard",
            subtitle = "${student.name} | ${student.trade}",
            onLogout = onLogout
        )
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }
        AnimatedContent(targetState = selectedTab, label = "student-tabs") { tab ->
            when (tab) {
                0 -> StudentOverview(student)
                1 -> StudentSchedule(student)
                else -> StudentExams(exams)
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
            SummaryChip("Trainee No.", student.traineeNumber, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryChip("Mobile", student.mobileNumber, Modifier.weight(1f))
            SummaryChip("DOB", student.dob, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StudentSchedule(student: Student) {
    val scrollState = rememberScrollState()
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Weekly Class Schedule with A/P", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DayOfWeek.entries.forEach { day ->
                    DayScheduleCard(
                        dayLabel = day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        lectures = student.weeklySchedule[day].orEmpty()
                    )
                }
            }
        }
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
private fun AttendancePieChart(
    present: Int,
    absent: Int,
    modifier: Modifier = Modifier
) {
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
private fun DayScheduleCard(dayLabel: String, lectures: List<LectureSlot>) {
    Card(
        modifier = Modifier.size(width = 210.dp, height = 240.dp),
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
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(lecture.subject, fontWeight = FontWeight.SemiBold)
                            Text(lecture.time, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Attendance: ${lecture.status}")
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
            Column {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Card(
                modifier = Modifier.clickable { onLogout() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFD8DFEA))
            ) {
                Text(
                    "Logout",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
