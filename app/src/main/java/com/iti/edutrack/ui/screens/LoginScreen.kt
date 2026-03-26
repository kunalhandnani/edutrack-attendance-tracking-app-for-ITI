package com.iti.edutrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iti.edutrack.data.model.UserRole
import com.iti.edutrack.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (com.iti.edutrack.data.model.LoginResult) -> Unit
) {
    val uiState = viewModel.uiState

    Box(contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 30.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InstitutionLogoBadge()
                Text(
                    "EduTrack",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Education Management System",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select Login Type", modifier = Modifier.fillMaxWidth())
                RoleToggle(
                    selectedRole = uiState.selectedRole,
                    onSelected = viewModel::onRoleSelected
                )
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick = {
                        val result = viewModel.login()
                        if (result.errorMessage == null) {
                            onLoginSuccess(result)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCDD4E3),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Login")
                }
                Text(
                    if (uiState.selectedRole == UserRole.STUDENT) {
                        "Sample student login: ${viewModel.sampleStudentLogin}"
                    } else {
                        "Sample teacher login: ${viewModel.sampleTeacherLogin}"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Student username format: name@ITI.com and password format: DDMMYYYY",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun InstitutionLogoBadge() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .background(Color(0xFF353168), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(66.dp)) {
                drawCircle(Color(0xFF556A9C), radius = size.minDimension * 0.18f, center = center.copy(x = center.x - 16f, y = center.y))
                drawCircle(Color(0xFFF1B24A), radius = size.minDimension * 0.20f, center = center.copy(x = center.x, y = center.y - 10f))
                drawCircle(Color(0xFF8F5D3D), radius = size.minDimension * 0.24f, center = center.copy(x = center.x + 8f, y = center.y + 2f))
                drawCircle(Color(0xFF45B659), radius = size.minDimension * 0.17f, center = center.copy(x = center.x - 10f, y = center.y + 18f))
                drawRoundRect(
                    color = Color(0xFF67C66D),
                    topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.76f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.12f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                    style = Fill
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .background(Color(0xFFF7F0D8), RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                "Kaushal Setu",
                color = Color(0xFF353168),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
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
                border = BorderStroke(
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
