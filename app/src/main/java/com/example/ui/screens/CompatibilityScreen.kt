package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.localization.AppStrings
import com.example.model.CompatibilityReading
import com.example.model.UiState
import com.example.model.UserAstrologyProfile
import com.example.ui.components.CosmicScoreGauge
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
import com.example.util.ZodiacCalculator
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompatibilityScreen(
    profile: UserAstrologyProfile,
    compatibilitiesState: UiState<List<CompatibilityReading>>,
    compatibilityGenState: UiState<CompatibilityReading>,
    strings: AppStrings,
    onAnalyzeSigns: (signOne: String, signTwo: String, relationshipType: String) -> Unit,
    onAnalyzePerson: (partnerName: String, partnerBirthDate: String, partnerBirthTime: String, partnerBirthPlace: String, relationshipType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf("signs") } // "signs" or "person"
    var signOne by remember(profile.zodiacSign) { mutableStateOf(profile.zodiacSign.ifBlank { "Aries" }) }
    var signTwo by remember { mutableStateOf("Scorpio") }
    var relationshipType by remember { mutableStateOf("Romantic") }

    var partnerName by remember { mutableStateOf("") }
    var partnerBirthDate by remember { mutableStateOf("1997-10-28") }
    var partnerBirthTime by remember { mutableStateOf("14:30") }
    var partnerBirthPlace by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val savedList = (compatibilitiesState as? UiState.Success)?.data.orEmpty()
    val activeReading = when (compatibilityGenState) {
        is UiState.Success -> compatibilityGenState.data
        else -> savedList.firstOrNull()
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ChampagneGold,
        unfocusedBorderColor = HairlineBorder,
        focusedLabelColor = ChampagneGold,
        unfocusedLabelColor = MutedNebula,
        focusedTextColor = StarlightIvory,
        unfocusedTextColor = StarlightIvory,
        cursorColor = ChampagneGold
    )

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
                    text = strings.compatibilityTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ChampagneGold
                )
            }

            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = mode == "signs",
                        onClick = { mode = "signs" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = ChampagneGold,
                            activeContentColor = MidnightCosmos,
                            inactiveContainerColor = DeepIndigoSurface,
                            inactiveContentColor = StarlightIvory
                        ),
                        modifier = Modifier.testTag("compat_mode_signs")
                    ) {
                        Text(strings.bySignsLabel, style = MaterialTheme.typography.labelLarge)
                    }
                    SegmentedButton(
                        selected = mode == "person",
                        onClick = { mode = "person" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = ChampagneGold,
                            activeContentColor = MidnightCosmos,
                            inactiveContainerColor = DeepIndigoSurface,
                            inactiveContentColor = StarlightIvory
                        ),
                        modifier = Modifier.testTag("compat_mode_person")
                    ) {
                        Text(strings.byBirthDetailsLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            item {
                Surface(
                    color = DeepIndigoSurface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HairlineBorder, RoundedCornerShape(22.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (mode == "signs") {
                            Text(
                                text = strings.selectFirstSign,
                                style = MaterialTheme.typography.labelLarge,
                                color = MoonlitLavender
                            )
                            SignSelectorRow(
                                selectedSign = signOne,
                                onSelect = { signOne = it },
                                tagPrefix = "sign_one"
                            )

                            Text(
                                text = strings.selectSecondSign,
                                style = MaterialTheme.typography.labelLarge,
                                color = MoonlitLavender
                            )
                            SignSelectorRow(
                                selectedSign = signTwo,
                                onSelect = { signTwo = it },
                                tagPrefix = "sign_two"
                            )
                        } else {
                            Text(
                                text = strings.partnerDetailsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = StarlightIvory
                            )
                            OutlinedTextField(
                                value = partnerName,
                                onValueChange = { partnerName = it },
                                label = { Text(strings.partnerNameLabel) },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = ChampagneGold) },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = partnerBirthDate,
                                    onValueChange = { partnerBirthDate = it },
                                    label = { Text(strings.birthDateLabel) },
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = {
                                        val parts = partnerBirthDate.split("-")
                                        val cal = Calendar.getInstance()
                                        val y = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR) - 25
                                        val m = (parts.getOrNull(1)?.toIntOrNull() ?: 10) - 1
                                        val d = parts.getOrNull(2)?.toIntOrNull() ?: 28
                                        DatePickerDialog(context, { _, year, month, day ->
                                            partnerBirthDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                        }, y, m, d).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Pick date", tint = ChampagneGold)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = partnerBirthTime,
                                    onValueChange = { partnerBirthTime = it },
                                    label = { Text(strings.birthTimeLabel) },
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = {
                                        val parts = partnerBirthTime.split(":")
                                        val h = parts.getOrNull(0)?.toIntOrNull() ?: 12
                                        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                        TimePickerDialog(context, { _, hour, minute ->
                                            partnerBirthTime = String.format(Locale.US, "%02d:%02d", hour, minute)
                                        }, h, min, true).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Icon(Icons.Rounded.Schedule, contentDescription = "Pick time", tint = ChampagneGold)
                                }
                            }
                            OutlinedTextField(
                                value = partnerBirthPlace,
                                onValueChange = { partnerBirthPlace = it },
                                label = { Text(strings.birthPlaceLabel) },
                                leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = ChampagneGold) },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            val partnerVedic = remember(partnerBirthDate, partnerBirthTime) {
                                if (ZodiacCalculator.isValidBirthDate(partnerBirthDate.trim())) {
                                    ZodiacCalculator.calculateVedicMoonDetails(partnerBirthDate.trim(), partnerBirthTime.trim())
                                } else {
                                    null
                                }
                            }
                            if (partnerVedic != null) {
                                Surface(
                                    color = CosmicSlate.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                ) {
                                    Text(
                                        text = "${strings.moonRashiLabel}: ☽ ${partnerVedic.moonSign.symbol} ${partnerVedic.moonSign.sanskritName} (${partnerVedic.moonSign.name}) • ${partnerVedic.nakshatra}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ChampagneGold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = strings.relationshipTypeLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MoonlitLavender
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Romantic" to strings.romanticLabel,
                                "Platonic" to strings.platonicLabel,
                                "Professional" to strings.professionalLabel
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = relationshipType == key,
                                    onClick = { relationshipType = key },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ChampagneGold,
                                        selectedLabelColor = MidnightCosmos,
                                        containerColor = ElevatedTwilight,
                                        labelColor = StarlightIvory
                                    ),
                                    modifier = Modifier.testTag("rel_type_$key")
                                )
                            }
                        }

                        if (!inputError.isNullOrBlank()) {
                            Text(
                                text = inputError.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftErrorCoral
                            )
                        }

                        Button(
                            onClick = {
                                if (mode == "signs") {
                                    inputError = null
                                    onAnalyzeSigns(signOne, signTwo, relationshipType)
                                } else {
                                    if (!ZodiacCalculator.isValidBirthDate(partnerBirthDate.trim())) {
                                        inputError = "Please enter partner birth date as YYYY-MM-DD."
                                    } else {
                                        inputError = null
                                        onAnalyzePerson(
                                            partnerName,
                                            partnerBirthDate.trim(),
                                            partnerBirthTime.trim(),
                                            partnerBirthPlace.trim(),
                                            relationshipType
                                        )
                                    }
                                }
                            },
                            enabled = compatibilityGenState !is UiState.Loading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = MidnightCosmos
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("analyze_compatibility_button")
                        ) {
                            if (compatibilityGenState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MidnightCosmos,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.checkCompatibility, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }

            if (compatibilityGenState is UiState.Error) {
                item {
                    Surface(
                        color = SoftErrorCoral.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SoftErrorCoral.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = compatibilityGenState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftErrorCoral,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            if (activeReading != null) {
                item {
                    val info1 = ZodiacCalculator.findByName(activeReading.signOne)
                    val info2 = ZodiacCalculator.findByName(activeReading.signTwo)
                    Surface(
                        color = DeepIndigoSurface.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${info1.symbol} ${info1.sanskritName} (${info1.name}) + ${info2.symbol} ${info2.sanskritName} (${info2.name})",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = ChampagneGold
                                    )
                                    Surface(
                                        color = CosmicSlate,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = activeReading.relationshipType,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MoonlitLavender,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                CosmicScoreGauge(
                                    score = activeReading.overallScore.toInt(),
                                    label = "Match",
                                    accentColor = RoseQuartz
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ScoreBarRow(strings.romanticLabel, activeReading.romanticScore.toInt(), RoseQuartz)
                                ScoreBarRow(strings.platonicLabel, activeReading.platonicScore.toInt(), CelestialTeal)
                                ScoreBarRow(strings.professionalLabel, activeReading.professionalScore.toInt(), ChampagneGold)
                            }

                            Text(
                                text = activeReading.summary,
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarlightIvory
                            )

                            CompatDetailBlock(
                                icon = Icons.Rounded.Favorite,
                                title = strings.strengthsLabel,
                                body = activeReading.strengths,
                                accent = CelestialTeal
                            )
                            CompatDetailBlock(
                                icon = Icons.Rounded.Shield,
                                title = strings.growthAreasLabel,
                                body = activeReading.challenges,
                                accent = RoseQuartz
                            )
                        }
                    }
                }
            } else if (compatibilityGenState !is UiState.Loading) {
                item {
                    Surface(
                        color = DeepIndigoSurface,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HairlineBorder, RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = strings.emptyCompatHint,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MoonlitLavender,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignSelectorRow(
    selectedSign: String,
    onSelect: (String) -> Unit,
    tagPrefix: String
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ZodiacCalculator.allSigns) { sign ->
            val selected = sign.name.equals(selectedSign, ignoreCase = true)
            Surface(
                color = if (selected) ChampagneGold else ElevatedTwilight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .border(
                        1.dp,
                        if (selected) ChampagneGold else HairlineBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(sign.name) }
                    .testTag("${tagPrefix}_${sign.name}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = sign.symbol,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (selected) MidnightCosmos else ChampagneGold
                    )
                    Text(
                        text = sign.sanskritName,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MidnightCosmos else StarlightIvory
                    )
                    Text(
                        text = sign.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MidnightCosmos.copy(alpha = 0.8f) else MutedNebula
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreBarRow(
    label: String,
    score: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MutedNebula)
            Text(text = "$score%", style = MaterialTheme.typography.labelLarge, color = color)
        }
        LinearProgressIndicator(
            progress = { (score.coerceIn(0, 100)) / 100f },
            color = color,
            trackColor = ElevatedTwilight,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun CompatDetailBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    accent: Color
) {
    Surface(
        color = ElevatedTwilight.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = accent)
            }
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = StarlightIvory)
        }
    }
}
