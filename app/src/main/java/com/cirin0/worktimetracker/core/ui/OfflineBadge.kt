package com.cirin0.worktimetracker.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cirin0.worktimetracker.R

/**
 * Badge to show offline mode indicator
 */
@Composable
fun OfflineBadge(
    modifier: Modifier = Modifier,
    text: String? = null
) {
    val displayText = text ?: stringResource(R.string.offline_mode_badge)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp, 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = stringResource(R.string.general_offline),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

