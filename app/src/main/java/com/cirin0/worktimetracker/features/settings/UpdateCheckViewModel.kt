package com.cirin0.worktimetracker.features.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.BuildConfig
import com.cirin0.worktimetracker.core.utils.AppUpdateManager
import com.cirin0.worktimetracker.core.utils.UpdateProgress
import com.cirin0.worktimetracker.features.auth.data.api.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateCheckState(
    val isCheckingForUpdates: Boolean = false,
    val updateAvailable: Boolean = false,
    val versionName: String = "",
    val downloadUrl: String = "",
    val changelog: String? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val error: String? = null,
    val isUpdateReady: Boolean = false
)

@HiltViewModel
class UpdateCheckViewModel @Inject constructor(
    private val authApi: AuthApi,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(UpdateCheckState())
    val state: StateFlow<UpdateCheckState> = _state.asStateFlow()

    private val updateManager = AppUpdateManager(context)

    fun checkForUpdates() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCheckingForUpdates = true, error = null)
            try {
                val response = authApi.checkForUpdates(versionCode = BuildConfig.VERSION_CODE)
                _state.value = _state.value.copy(
                    updateAvailable = response.updateAvailable,
                    versionName = response.versionName,
                    downloadUrl = response.downloadUrl,
                    changelog = response.changelog,
                    isCheckingForUpdates = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isCheckingForUpdates = false,
                    error = e.message ?: "Failed to check for updates"
                )
            }
        }
    }

    fun downloadUpdate(downloadUrl: String) {
        viewModelScope.launch {
            updateManager.downloadApk(downloadUrl).collect { progress ->
                when (progress) {
                    is UpdateProgress.Loading -> {
                        _state.value = _state.value.copy(
                            isDownloading = true,
                            downloadProgress = progress.progress,
                            error = null
                        )
                    }

                    is UpdateProgress.Success -> {
                        _state.value = _state.value.copy(
                            isDownloading = false,
                            isUpdateReady = true
                        )
                    }

                    is UpdateProgress.Error -> {
                        _state.value = _state.value.copy(
                            isDownloading = false,
                            error = progress.message
                        )
                    }
                }
            }
        }
    }

    fun installUpdate() {
        viewModelScope.launch {
            try {
                val apkFile = java.io.File(
                    context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS),
                    "app-update.apk"
                )
                if (apkFile.exists()) {
                    updateManager.installApk(apkFile)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}


