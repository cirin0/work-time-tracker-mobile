package com.cirin0.worktimetracker.features.profile.presentation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogoutSuccess: () -> Unit = {},
    onNavigateToCompany: (Int) -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToRequests: () -> Unit = {},
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var isFullScreenVisible by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            file?.let { imageFile ->
                viewModel.updateAvatar(imageFile)
            }
        }
    }

    LaunchedEffect(logoutState.isSuccess) {
        if (logoutState.isSuccess) {
            onLogoutSuccess()
        }
    }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            val message = if (state.pinCode.isNotEmpty() || state.newPinCode.isNotEmpty()) {
                "PIN-код успішно оновлено"
            } else {
                "Профіль успішно оновлено"
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearUpdateSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Профіль",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = onNavigateToChat) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Чат"
                        )
                    }
                    if (state.user != null) {
                        IconButton(onClick = { viewModel.openEditDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редагувати"
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.loadUserProfile() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Оновити"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.showServerUnavailableWarning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Попередження",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "Сервер недоступний - показано збережені дані",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Помилка: ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.loadUserProfile() }) {
                            Text("Спробувати знову")
                        }
                    }
                }

                state.user != null -> {
                    val user = state.user!!

                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable(
                                    onClick = { isFullScreenVisible = true }
                                    // TODO: Додати перегляд аватара у повному розмірі
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.avatar != null) {
                                AsyncImage(
                                    model = user.avatar,
                                    contentDescription = "Аватар користувача",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Text(
                                    text = user.name.firstOrNull()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.BottomEnd)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Змінити аватар",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (state.isUpdating) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = user.role.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    ProfileInfoCard(
                        icon = Icons.Default.Security,
                        title = "PIN-код",
                        value = if (user.hasPinCode) "Встановлено" else "Не встановлено",
                        onClick = {
                            if (user.hasPinCode) {
                                viewModel.openUpdatePinCodeDialog()
                            } else {
                                viewModel.openPinCodeDialog()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoCard(
                        icon = Icons.Default.Email,
                        title = "Email",
                        value = user.email
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoCard(
                        icon = Icons.Default.Person,
                        title = "ID",
                        value = user.id.toString()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoCard(
                        icon = Icons.Default.Schedule,
                        title = "Режим роботи",
                        value = when (user.workMode) {
                            "office" -> "Офіс"
                            "remote" -> "Віддалено"
                            "hybrid" -> "Гібрид"
                            else -> user.workMode
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    user.company?.let { company ->
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoCard(
                            icon = Icons.Default.Business,
                            title = "Компанія",
                            value = company.name,
                            onClick = { onNavigateToCompany(company.id) }
                        )
                    }

                    user.manager?.let { manager ->
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoCard(
                            icon = Icons.Default.Person,
                            title = "Менеджер",
                            value = manager.name
                        )
                    }

                    user.workSchedule?.let { schedule ->
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoCard(
                            icon = Icons.Default.Schedule,
                            title = "Графік роботи",
                            value = schedule.name
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRequests() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Мої заявки",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Відпустка, лікарняний, відгул",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Налаштування")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !logoutState.isLoading
                    ) {
                        if (logoutState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Вийти")
                        }
                    }

                    if (logoutState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = logoutState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (state.isPinCodeDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closePinCodeDialog() },
            title = { Text("Встановити PIN-код") },
            text = {
                Column {
                    Text("Введіть 4 цифри для вашого PIN-коду. Обов'язково запам'ятайте його.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.pinCode,
                        onValueChange = { viewModel.onPinCodeChange(it) },
                        label = { Text("PIN-код") },
                        isError = state.pinCodeError != null,
                        supportingText = state.pinCodeError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.updateError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.updateError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.setupPinCode() },
                    enabled = !state.isUpdating && state.pinCode.length == 4
                ) {
                    Text("Встановити")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closePinCodeDialog() }) {
                    Text("Скасувати")
                }
            }
        )
    }

    if (state.isUpdatePinCodeDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeUpdatePinCodeDialog() },
            title = { Text("Оновити PIN-код") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.currentPinCode,
                        onValueChange = { viewModel.onCurrentPinCodeChange(it) },
                        label = { Text("Поточний PIN-код") },
                        isError = state.currentPinCodeError != null,
                        supportingText = state.currentPinCodeError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.newPinCode,
                        onValueChange = { viewModel.onNewPinCodeChange(it) },
                        label = { Text("Новий PIN-код") },
                        isError = state.newPinCodeError != null,
                        supportingText = state.newPinCodeError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.updateError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.updateError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.updatePinCode() },
                    enabled = !state.isUpdating && state.currentPinCode.length == 4 && state.newPinCode.length == 4
                ) {
                    Text("Оновити")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeUpdatePinCodeDialog() }) {
                    Text("Скасувати")
                }
            }
        )
    }

    if (state.isEditDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeEditDialog() },
            title = { Text("Редагувати профіль") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.editName,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Ім'я") },
                        isError = state.nameError != null,
                        supportingText = {
                            state.nameError?.let { Text(it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.editEmail,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email") },
                        isError = state.emailError != null,
                        supportingText = {
                            state.emailError?.let { Text(it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.updateError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.updateError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.closeEditDialog() },
                        enabled = !state.isUpdating
                    ) {
                        Text("Скасувати")
                    }
                    Button(
                        onClick = { viewModel.updateProfile() },
                        enabled = !state.isUpdating,
                        modifier = Modifier.size(width = 120.dp, height = 40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (state.isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Зберегти")
                            }
                        }
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
fun ProfileInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("avatar", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

