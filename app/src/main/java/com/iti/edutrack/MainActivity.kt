package com.iti.edutrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.iti.edutrack.data.DemoSchoolRepository
import com.iti.edutrack.data.StudentAssetLoader
import com.iti.edutrack.ui.AttendanceApp
import com.iti.edutrack.ui.theme.EduTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val importedStudents = StudentAssetLoader.loadFromAssets(this)
        setContent {
            val repository = remember(importedStudents) {
                DemoSchoolRepository(importedRecords = importedStudents)
            }
            EduTrackTheme {
                AttendanceApp(repository = repository)
            }
        }
    }
}
