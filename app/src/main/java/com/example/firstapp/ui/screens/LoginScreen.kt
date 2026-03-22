package com.example.firstapp.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.firstapp.data.model.UserRole

@Composable
fun LoginScreen(
    onLogin: (UserRole, String, String) -> String?
) {
    var selectedRole by rememberSaveable { mutableStateOf(UserRole.STUDENT) }
    var email by rememberSaveable { mutableStateOf("aman-kumar@ITI.com") }
    var password by rememberSaveable { mutableStateOf("15-08-2006") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Box(contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.verticalGradient(listOf(Color(0xFF6A3DF0), Color(0xFF4D2CFF))),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ITI", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text("EduTrack", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Education Management System",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select Login Type", modifier = Modifier.fillMaxWidth())
                RoleToggle(
                    selectedRole = selectedRole,
                    onSelected = { role ->
                        selectedRole = role
                        if (role == UserRole.STUDENT) {
                            email = "aman-kumar@ITI.com"
                            password = "15-08-2006"
                        } else {
                            email = "anita-sharma@ITI.com"
                            password = "teach@123"
                        }
                    }
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
                Button(
                    onClick = { errorMessage = onLogin(selectedRole, email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0D5E2), contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Login")
                }
                Text(
                    "Student: use DOB as password. Teacher: use assigned password.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RoleToggle(
    selectedRole: UserRole,
    onSelected: (UserRole) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(UserRole.STUDENT, UserRole.TEACHER).forEach { role ->
            val isSelected = role == selectedRole
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(role) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFF1EDFF) else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) Color(0xFF6A3DF0) else Color(0xFFD9DFEA)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (isSelected) Color(0xFF6A3DF0) else Color(0xFFF4F6FA),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (role == UserRole.STUDENT) "S" else "T",
                            color = if (isSelected) Color.White else Color(0xFF6A3DF0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
