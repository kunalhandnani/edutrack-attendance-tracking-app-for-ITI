package com.example.firstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.firstapp.data.DemoSchoolRepository
import com.example.firstapp.data.StudentAssetLoader
import com.example.firstapp.ui.AttendanceApp
import com.example.firstapp.ui.theme.FirstappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val importedStudents = StudentAssetLoader.loadFromAssets(this)
        setContent {
            FirstappTheme {
                AttendanceApp(repository = DemoSchoolRepository(importedRecords = importedStudents))
            }
        }
    }
}
