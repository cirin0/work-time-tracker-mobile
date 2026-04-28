package com.cirin0.worktimetracker.features.profile.presentation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cirin0.worktimetracker.R
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToCompany: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToRequests: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToManager: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val pinUpdatedMessage = stringResource(R.string.profile_pin_updated_success)
    val profileUpdatedMessage = stringResource(R.string.profile_updated_success)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            file?.let { imageFile -> viewModel.updateAvatar(imageFile) }
        }
    }


    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            val message = if (state.pinCode.isNotEmpty() || state.newPinCode.isNotEmpty()) {
                pinUpdatedMessage
            } else {
                profileUpdatedMessage
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearUpdateSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileTopBar(
                onRefresh = { viewModel.loadUserProfile() },
                onChat = onNavigateToChat,
                onSettings = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = state.showServerUnavailableWarning) {
                ServerWarningBanner()
            }

            when {
                state.isLoading -> LoadingContent()

                state.error != null -> ErrorContent(
                    error = state.error!!,
                    onRetry = { viewModel.loadUserProfile() }
                )

                state.user != null -> {
                    val user = state.user!!

                    Spacer(modifier = Modifier.height(16.dp))

                    AvatarSection(
                        name = user.name,
                        avatarUrl = user.avatar,
                        isUpdating = state.isUpdating,
                        onAvatarClick = { imagePickerLauncher.launch("image/*") }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp
                        ) {
                            IconButton(
                                onClick = { viewModel.openEditDialog() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.profile_edit),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = user.role.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "#${user.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    ProfileSection(title = stringResource(R.string.profile_personal_data)) {
                        ProfileRow(
                            icon = Icons.Default.Security,
                            label = stringResource(R.string.profile_pin_code),
                            value = if (user.hasPinCode) {
                                stringResource(R.string.profile_pin_set)
                            } else {
                                stringResource(R.string.profile_pin_not_set)
                            },
                            valueColor = if (user.hasPinCode)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            showArrow = true,
                            onClick = {
                                if (user.hasPinCode) viewModel.openUpdatePinCodeDialog()
                                else viewModel.openPinCodeDialog()
                            }
                        )
                        RowDivider()
                        ProfileRow(
                            icon = Icons.Default.Email,
                            label = stringResource(R.string.general_email),
                            value = user.email
                        )
                        RowDivider()
                        ProfileRow(
                            icon = Icons.Default.Schedule,
                            label = stringResource(R.string.profile_work_mode),
                            value = when (user.workMode) {
                                "office" -> stringResource(R.string.profile_work_mode_office)
                                "remote" -> stringResource(R.string.profile_work_mode_remote)
                                "hybrid" -> stringResource(R.string.profile_work_mode_hybrid)
                                else -> user.workMode
                            }
                        )
                    }

                    val hasWorkInfo =
                        user.company != null || user.manager != null || user.workSchedule != null
                    if (hasWorkInfo) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileSection(title = stringResource(R.string.profile_work)) {
                            var addDivider = false
                            user.company?.let { company ->
                                ProfileRow(
                                    icon = Icons.Default.Business,
                                    label = stringResource(R.string.profile_company),
                                    value = company.name,
                                    showArrow = true,
                                    onClick = { onNavigateToCompany() }
                                )
                                addDivider = true
                            }
                            user.manager?.let { manager ->
                                if (addDivider) RowDivider()
                                ProfileRow(
                                    icon = Icons.Default.Person,
                                    label = stringResource(R.string.profile_manager),
                                    value = manager.name
                                )
                                addDivider = true
                            }
                            user.workSchedule?.let { schedule ->
                                if (addDivider) RowDivider()
                                ProfileRow(
                                    icon = Icons.Default.Schedule,
                                    label = stringResource(R.string.profile_work_schedule),
                                    value = schedule.name,
                                    showArrow = true,
                                    onClick = onNavigateToSchedule
                                )
                            }
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.user != null) {
                ProfileSection(title = stringResource(R.string.profile_actions)) {
                    ProfileRow(
                        icon = Icons.Default.Edit,
                        label = stringResource(R.string.profile_my_requests),
                        value = stringResource(R.string.profile_requests_description),
                        showArrow = true,
                        onClick = onNavigateToRequests
                    )

                    if (state.user!!.role.equals("manager", ignoreCase = true)) {
                        RowDivider()
                        ProfileRow(
                            icon = Icons.Default.Business,
                            label = stringResource(R.string.manager_button),
                            value = stringResource(R.string.manager_subtitle),
                            showArrow = true,
                            onClick = onNavigateToManager
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )
    }

    if (state.isPinCodeDialogOpen) {
        SetPinCodeDialog(
            pinCode = state.pinCode,
            pinCodeError = state.pinCodeError,
            updateError = state.updateError,
            isUpdating = state.isUpdating,
            onPinChange = { viewModel.onPinCodeChange(it) },
            onConfirm = { viewModel.setupPinCode() },
            onDismiss = { viewModel.closePinCodeDialog() }
        )
    }

    if (state.isUpdatePinCodeDialogOpen) {
        UpdatePinCodeDialog(
            currentPin = state.currentPinCode,
            newPin = state.newPinCode,
            currentPinError = state.currentPinCodeError,
            newPinError = state.newPinCodeError,
            updateError = state.updateError,
            isUpdating = state.isUpdating,
            onCurrentPinChange = { viewModel.onCurrentPinCodeChange(it) },
            onNewPinChange = { viewModel.onNewPinCodeChange(it) },
            onConfirm = { viewModel.updatePinCode() },
            onDismiss = { viewModel.closeUpdatePinCodeDialog() }
        )
    }

    if (state.isEditDialogOpen) {
        EditProfileDialog(
            name = state.editName,
            nameError = state.nameError,
            updateError = state.updateError,
            isUpdating = state.isUpdating,
            onNameChange = { viewModel.onNameChange(it) },
            onConfirm = { viewModel.updateProfile() },
            onDismiss = { viewModel.closeEditDialog() }
        )
    }
}

@Composable
private fun ProfileTopBar(
    onRefresh: () -> Unit,
    onChat: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.profile_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TopBarIconButton(
                icon = Icons.AutoMirrored.Filled.Chat,
                contentDescription = stringResource(R.string.profile_chat),
                onClick = onChat
            )
            TopBarIconButton(
                icon = Icons.Default.Settings,
                contentDescription = stringResource(R.string.profile_settings),
                onClick = onSettings
            )
            TopBarIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.general_refresh),
                onClick = onRefresh
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AvatarSection(
    name: String,
    avatarUrl: String?,
    isUpdating: Boolean,
    onAvatarClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = stringResource(R.string.profile_user_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.BottomEnd)
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(R.string.profile_change_avatar),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 62.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    showArrow: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = valueColor
                )
            }
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ServerWarningBanner() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.general_server_unavailable_cached),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.general_retry))
            }
        }
    }
}

@Composable
private fun SetPinCodeDialog(
    pinCode: String,
    pinCodeError: String?,
    updateError: String?,
    isUpdating: Boolean,
    onPinChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_set_pin_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.profile_set_pin_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = pinCode,
                    onValueChange = onPinChange,
                    label = { Text(stringResource(R.string.profile_pin_code)) },
                    isError = pinCodeError != null,
                    supportingText = pinCodeError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                updateError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isUpdating && pinCode.length == 4
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.profile_set_pin_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}

@Composable
private fun UpdatePinCodeDialog(
    currentPin: String,
    newPin: String,
    currentPinError: String?,
    newPinError: String?,
    updateError: String?,
    isUpdating: Boolean,
    onCurrentPinChange: (String) -> Unit,
    onNewPinChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_update_pin_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = onCurrentPinChange,
                    label = { Text(stringResource(R.string.profile_current_pin)) },
                    isError = currentPinError != null,
                    supportingText = currentPinError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = onNewPinChange,
                    label = { Text(stringResource(R.string.profile_new_pin)) },
                    isError = newPinError != null,
                    supportingText = newPinError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                updateError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isUpdating && currentPin.length == 4 && newPin.length == 4
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.general_update))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        }
    )
}

@Composable
private fun EditProfileDialog(
    name: String,
    nameError: String?,
    updateError: String?,
    isUpdating: Boolean,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_edit_profile_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.profile_name)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                updateError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.general_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUpdating) {
                Text(stringResource(R.string.general_cancel))
            }
        }
    )
}

private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("avatar", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { output ->
            inputStream?.use { input -> input.copyTo(output) }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}