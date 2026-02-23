package com.cirin0.worktimetracker.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    onNavigateBack: () -> Boolean,
) {

    val useDarkTheme by themeViewModel.userThemePreference.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val themeOptions = mapOf(null to "System", false to "Light", true to "Dark")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Тема")
            Box {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    themeOptions.forEach { (themeValue, themeName) ->
                        DropdownMenuItem(
                            text = { Text(themeName) },
                            onClick = {
                                themeViewModel.setTheme(themeValue)
                                expanded = false
                            }
                        )
                    }
                }
                TextButton(
                    onClick = { expanded = true }
                ) {
                    Text(themeOptions[useDarkTheme] ?: "System")
                    Icons.Default.ArrowDropDown.let { icon ->
                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
