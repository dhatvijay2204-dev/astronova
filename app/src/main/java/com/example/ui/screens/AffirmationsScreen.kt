package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.localization.AppStrings
import com.example.model.DailyAffirmation
import com.example.model.UiState
import com.example.model.UserAstrologyProfile
import com.example.notifications.NotificationHelper
import com.example.ui.theme.CelestialTeal
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CosmicSlate
import com.example.ui.theme.DeepIndigoSurface
import com.example.ui.theme.ElevatedTwilight
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MidnightCosmos
import com.example.ui.theme.MoonlitLavender
import com.example.ui.theme.MutedNebula
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SoftErrorCoral
import com.example.ui.theme.StarlightIvory
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AffirmationsScreen(
    profile: UserAstrologyProfile,
    affirmationsState: UiState<List<DailyAffirmation>>,
    affirmationGenState: UiState<DailyAffirmation>,
    currentDateKey: String,
    strings: AppStrings,
    onGenerateAffirmation: (intention: String) -> Unit,
    onToggleFavorite: (DailyAffirmation) -> Unit,
    onDeleteAffirmation: (String) -> Unit,
    onUpdateNotificationPrefs: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val intentions = remember {
        listOf("Confidence", "Calm", "Abundance", "Love", "Clarity", "Courage")
    }
    var selectedIntention by remember(profile.focusArea) {
        mutableStateOf(profile.focusArea.ifBlank { "Confidence" })
    }
    var notifFeedback by remember { mutableStateOf<String?>(null) }

    val savedAffirmations = (affirmationsState as? UiState.Success)?.data.orEmpty()
    val todayAffirmation = when (affirmationGenState) {
        is UiState.Success -> {
            savedAffirmations.firstOrNull { it.id == affirmationGenState.data.id } ?: affirmationGenState.data
        }
        else -> savedAffirmations.firstOrNull { it.dateKey == currentDateKey } ?: savedAffirmations.firstOrNull()
    }

    LaunchedEffect(todayAffirmation?.text) {
        if (todayAffirmation != null && todayAffirmation.text.isNotBlank()) {
            NotificationHelper.cacheAffirmationPayload(
                context = context,
                affirmationText = todayAffirmation.text,
                userName = profile.fullName,
                zodiacSign = profile.zodiacSign
            )
        }
    }

    LaunchedEffect(profile.userId, affirmationsState) {
        if (affirmationsState is UiState.Success && savedAffirmations.isEmpty() && affirmationGenState is UiState.Idle) {
            onGenerateAffirmation(selectedIntention)
        }
    }

    var pendingActionAfterPerm by remember { mutableStateOf<(() -> Unit)?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingActionAfterPerm?.invoke()
            pendingActionAfterPerm = null
        } else {
            notifFeedback = "Notification permission denied. Enable it in Android Settings to receive daily affirmations."
            pendingActionAfterPerm = null
        }
    }

    fun runWithNotificationPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                action()
            } else {
                pendingActionAfterPerm = action
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            action()
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = strings.dailyAffirmationTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ChampagneGold
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    intentions.forEach { item ->
                        FilterChip(
                            selected = selectedIntention == item,
                            onClick = { selectedIntention = item },
                            label = { Text(item, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ChampagneGold,
                                selectedLabelColor = MidnightCosmos,
                                containerColor = ElevatedTwilight,
                                labelColor = StarlightIvory
                            )
                        )
                    }
                }
            }

            item {
                Surface(
                    color = DeepIndigoSurface.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (affirmationGenState is UiState.Loading && todayAffirmation == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ChampagneGold)
                            }
                        } else if (todayAffirmation != null) {
                            Text(
                                text = "“${todayAffirmation.text}”",
                                style = MaterialTheme.typography.headlineMedium,
                                color = StarlightIvory
                            )
                            Surface(
                                color = ElevatedTwilight,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = todayAffirmation.ritualPrompt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MoonlitLavender,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onToggleFavorite(todayAffirmation) },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_affirmation_button")
                                ) {
                                    Icon(
                                        imageVector = if (todayAffirmation.isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                        contentDescription = null,
                                        tint = ChampagneGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (todayAffirmation.isFavorite) strings.savedInVault else strings.saveToVault,
                                        color = ChampagneGold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shareRashi = if (profile.vedicRashiName.isNotBlank()) {
                                            "${profile.vedicRashiName} (${profile.zodiacSign})"
                                        } else {
                                            profile.zodiacSign
                                        }
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "✨ ${todayAffirmation.text}\n— AstraNova (☽ $shareRashi)"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, strings.share))
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = null,
                                        tint = MoonlitLavender,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.share, color = MoonlitLavender)
                                }
                            }
                        }

                        if (affirmationGenState is UiState.Error) {
                            Text(
                                text = affirmationGenState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftErrorCoral
                            )
                        }

                        Button(
                            onClick = { onGenerateAffirmation(selectedIntention) },
                            enabled = affirmationGenState !is UiState.Loading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = MidnightCosmos
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("new_affirmation_button")
                        ) {
                            if (affirmationGenState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MidnightCosmos,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.newAffirmation, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }

            // Push notification schedule card
            item {
                val hour = profile.notificationHour.toInt().coerceIn(0, 23)
                val minute = profile.notificationMinute.toInt().coerceIn(0, 59)
                val formattedTime = String.format(Locale.US, "%02d:%02d", hour, minute)

                Surface(
                    color = DeepIndigoSurface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HairlineBorder, RoundedCornerShape(22.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = CelestialTeal
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.pushNotificationsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = StarlightIvory
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.dailyReminderToggle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarlightIvory
                            )
                            Switch(
                                checked = profile.notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        runWithNotificationPermission {
                                            NotificationHelper.scheduleDailyAffirmation(context, hour, minute)
                                            onUpdateNotificationPrefs(true, hour, minute)
                                            notifFeedback = "Daily push notifications scheduled for $formattedTime."
                                        }
                                    } else {
                                        NotificationHelper.cancelDailyAffirmation(context)
                                        onUpdateNotificationPrefs(false, hour, minute)
                                        notifFeedback = "Daily affirmation notifications paused."
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MidnightCosmos,
                                    checkedTrackColor = ChampagneGold
                                ),
                                modifier = Modifier.testTag("daily_notification_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.reminderTimeLabel}: $formattedTime",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MoonlitLavender
                            )
                            OutlinedButton(
                                onClick = {
                                    TimePickerDialog(context, { _, pickedHour, pickedMinute ->
                                        runWithNotificationPermission {
                                            NotificationHelper.scheduleDailyAffirmation(context, pickedHour, pickedMinute)
                                            onUpdateNotificationPrefs(true, pickedHour, pickedMinute)
                                            notifFeedback = String.format(
                                                Locale.US,
                                                "Daily reminder updated to %02d:%02d.",
                                                pickedHour,
                                                pickedMinute
                                            )
                                        }
                                    }, hour, minute, true).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("pick_notification_time_button")
                            ) {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(formattedTime, color = ChampagneGold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                runWithNotificationPermission {
                                    val sent = NotificationHelper.showAffirmationNotification(
                                        context = context,
                                        customBody = todayAffirmation?.text
                                    )
                                    notifFeedback = if (sent) {
                                        "Push notification preview delivered."
                                    } else {
                                        "Enable notifications permission to display push alerts."
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_test_notification_button")
                        ) {
                            Text(strings.sendTestNotification, color = CelestialTeal)
                        }

                        if (!notifFeedback.isNullOrBlank()) {
                            Text(
                                text = notifFeedback.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = CelestialTeal
                            )
                        }
                    }
                }
            }

            val favorites = savedAffirmations.filter { it.isFavorite }
            if (favorites.isNotEmpty()) {
                item {
                    Text(
                        text = strings.savedVaultTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = ChampagneGold
                    )
                }

                items(favorites, key = { it.id }) { item ->
                    Surface(
                        color = DeepIndigoSurface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HairlineBorder, RoundedCornerShape(18.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "“${item.text}”",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = StarlightIvory
                                )
                                Text(
                                    text = "${item.dateKey} • ${item.focusArea}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedNebula
                                )
                            }
                            IconButton(
                                onClick = { onDeleteAffirmation(item.id) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete affirmation",
                                    tint = RoseQuartz
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
