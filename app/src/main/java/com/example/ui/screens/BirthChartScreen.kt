package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.localization.AppStrings
import com.example.model.BirthChartReading
import com.example.model.DashaGocharContext
import com.example.model.GocharTransit
import com.example.model.PlanetaryPlacement
import com.example.model.UiState
import com.example.model.UserAstrologyProfile
import com.example.ui.components.NatalBirthChartWheel
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

@Composable
fun BirthChartScreen(
    profile: UserAstrologyProfile,
    birthChartsState: UiState<List<BirthChartReading>>,
    birthChartGenState: UiState<BirthChartReading>,
    strings: AppStrings,
    onGenerateBirthChart: (fullName: String, birthDate: String, birthTime: String, birthPlace: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showInputs by remember { mutableStateOf(false) }
    var chartName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var chartDate by remember(profile.birthDate) { mutableStateOf(profile.birthDate) }
    var chartTime by remember(profile.birthTime) {
        mutableStateOf(if (profile.birthTime == "Unknown") "08:00" else profile.birthTime)
    }
    var chartPlace by remember(profile.birthPlace) { mutableStateOf(profile.birthPlace) }
    var inputError by remember { mutableStateOf<String?>(null) }

    val savedChart = (birthChartsState as? UiState.Success)?.data?.firstOrNull()
    val activeChart = when (birthChartGenState) {
        is UiState.Success -> birthChartGenState.data
        else -> savedChart
    }

    LaunchedEffect(profile.userId, profile.zodiacSign, profile.birthDate, profile.birthTime, birthChartsState) {
        if (birthChartsState is UiState.Success && savedChart == null && birthChartGenState is UiState.Idle) {
            onGenerateBirthChart(profile.fullName, profile.birthDate, profile.birthTime, profile.birthPlace)
        }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.tabBirthChart,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = ChampagneGold
                                )
                                Text(
                                    text = "$chartName • $chartDate • $chartTime • $chartPlace",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MoonlitLavender
                                )
                            }
                            OutlinedButton(
                                onClick = { showInputs = !showInputs },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("toggle_birth_chart_inputs")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = strings.editDetails,
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.editDetails, color = ChampagneGold)
                            }
                        }

                        AnimatedVisibility(visible = showInputs || activeChart == null) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = chartName,
                                    onValueChange = { chartName = it },
                                    label = { Text(strings.fullNameLabel) },
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
                                        value = chartDate,
                                        onValueChange = { chartDate = it },
                                        label = { Text(strings.birthDateLabel) },
                                        singleLine = true,
                                        colors = fieldColors,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            val parts = chartDate.split("-")
                                            val cal = Calendar.getInstance()
                                            val y = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR) - 25
                                            val m = (parts.getOrNull(1)?.toIntOrNull() ?: 5) - 1
                                            val d = parts.getOrNull(2)?.toIntOrNull() ?: 14
                                            DatePickerDialog(context, { _, year, month, day ->
                                                chartDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                            }, y, m, d).show()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(54.dp)
                                    ) {
                                        Icon(Icons.Rounded.CalendarMonth, contentDescription = "Date", tint = ChampagneGold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = chartTime,
                                        onValueChange = { chartTime = it },
                                        label = { Text(strings.birthTimeLabel) },
                                        singleLine = true,
                                        colors = fieldColors,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            val parts = chartTime.split(":")
                                            val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                                            val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                            TimePickerDialog(context, { _, hour, minute ->
                                                chartTime = String.format(Locale.US, "%02d:%02d", hour, minute)
                                            }, h, min, true).show()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(54.dp)
                                    ) {
                                        Icon(Icons.Rounded.Schedule, contentDescription = "Time", tint = ChampagneGold)
                                    }
                                }

                                OutlinedTextField(
                                    value = chartPlace,
                                    onValueChange = { chartPlace = it },
                                    label = { Text(strings.birthPlaceLabel) },
                                    leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = ChampagneGold) },
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (!inputError.isNullOrBlank()) {
                                    Text(
                                        text = inputError.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SoftErrorCoral
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (!ZodiacCalculator.isValidBirthDate(chartDate.trim())) {
                                            inputError = "Use YYYY-MM-DD format for birth date."
                                        } else if (chartPlace.isBlank()) {
                                            inputError = "Please enter your birth location."
                                        } else {
                                            inputError = null
                                            showInputs = false
                                            onGenerateBirthChart(chartName, chartDate.trim(), chartTime.trim(), chartPlace.trim())
                                        }
                                    },
                                    enabled = birthChartGenState !is UiState.Loading,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ChampagneGold,
                                        contentColor = MidnightCosmos
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("generate_birth_chart_button")
                                ) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.generateBirthChart, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }

            if (birthChartGenState is UiState.Loading) {
                item {
                    Surface(
                        color = DeepIndigoSurface,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = ChampagneGold)
                            Text(
                                text = "Calculating natal planetary positions & houses...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MoonlitLavender
                            )
                        }
                    }
                }
            } else if (birthChartGenState is UiState.Error && activeChart == null) {
                item {
                    Surface(
                        color = SoftErrorCoral.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SoftErrorCoral.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(birthChartGenState.message, color = SoftErrorCoral)
                            Button(
                                onClick = { onGenerateBirthChart(chartName, chartDate, chartTime, chartPlace) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChampagneGold,
                                    contentColor = MidnightCosmos
                                )
                            ) {
                                Text(strings.retry)
                            }
                        }
                    }
                }
            } else if (activeChart != null) {
                item {
                    Surface(
                        color = DeepIndigoSurface.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            NatalBirthChartWheel(
                                placements = activeChart.placements,
                                sunSign = activeChart.sunSign,
                                moonSign = activeChart.moonSign,
                                risingSign = activeChart.risingSign
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BigThreeBadge(
                                    label = strings.moonSignLabel,
                                    sign = activeChart.moonSign,
                                    accent = ChampagneGold,
                                    modifier = Modifier.weight(1f)
                                )
                                BigThreeBadge(
                                    label = strings.sunSignLabel,
                                    sign = activeChart.sunSign,
                                    accent = MoonlitLavender,
                                    modifier = Modifier.weight(1f)
                                )
                                BigThreeBadge(
                                    label = strings.risingSignLabel,
                                    sign = activeChart.risingSign,
                                    accent = CelestialTeal,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (activeChart.nakshatra.isNotBlank()) {
                                Surface(
                                    color = CosmicSlate,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                ) {
                                    Text(
                                        text = "${strings.nakshatraLabel}: ${activeChart.nakshatra}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ChampagneGold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            Text(
                                text = activeChart.chartSummary,
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarlightIvory
                            )
                        }
                    }
                }

                item {
                    val dg = remember(activeChart.birthDate, activeChart.birthTime, activeChart.moonSign) {
                        ZodiacCalculator.calculateDashaAndGochar(
                            birthDate = activeChart.birthDate,
                            birthTime = activeChart.birthTime,
                            natalMoonSign = activeChart.moonSign
                        )
                    }
                    DashaGocharKundaliSection(
                        chart = activeChart,
                        context = dg,
                        strings = strings,
                        isLoading = birthChartGenState is UiState.Loading,
                        onAnalyzeImpact = {
                            onGenerateBirthChart(
                                activeChart.fullName,
                                activeChart.birthDate,
                                activeChart.birthTime,
                                activeChart.birthPlace
                            )
                        }
                    )
                }

                item {
                    Text(
                        text = strings.planetaryPlacementsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = ChampagneGold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(activeChart.placements) { placement ->
                    PlacementCard(placement = placement)
                }
            } else {
                item {
                    Surface(
                        color = DeepIndigoSurface,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HairlineBorder, RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = strings.emptyChartHint,
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
private fun BigThreeBadge(
    label: String,
    sign: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val info = ZodiacCalculator.findByName(sign)
    Surface(
        color = ElevatedTwilight,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            Text(
                text = "${info.symbol} ${info.sanskritName}",
                style = MaterialTheme.typography.labelLarge,
                color = StarlightIvory,
                maxLines = 1
            )
            Text(
                text = info.name,
                style = MaterialTheme.typography.labelSmall,
                color = MutedNebula,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PlacementCard(placement: PlanetaryPlacement) {
    val signInfo = ZodiacCalculator.findByName(placement.sign)
    val accent = when (placement.planet.lowercase(Locale.US)) {
        "sun" -> ChampagneGold
        "moon" -> StarlightIvory
        "venus" -> RoseQuartz
        "rahu" -> Color(0xFF7FA3D6)
        "ketu" -> Color(0xFFD69F7E)
        "ascendant", "rising" -> CelestialTeal
        else -> MoonlitLavender
    }
    Surface(
        color = DeepIndigoSurface.copy(alpha = 0.88f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HairlineBorder, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${placement.planet} in ${signInfo.symbol} ${signInfo.sanskritName} (${signInfo.name})",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = CosmicSlate,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${placement.degree}° • Bhava ${placement.house}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MoonlitLavender,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = placement.meaning,
                style = MaterialTheme.typography.bodyMedium,
                color = StarlightIvory.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DashaGocharKundaliSection(
    chart: BirthChartReading,
    context: DashaGocharContext,
    strings: AppStrings,
    isLoading: Boolean,
    onAnalyzeImpact: () -> Unit
) {
    Surface(
        color = DeepIndigoSurface.copy(alpha = 0.94f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .testTag("kundali_dasha_gochar_section")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ChampagneGold.copy(alpha = 0.16f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Timeline,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = strings.dashaAndGocharTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = ChampagneGold,
                    modifier = Modifier.weight(1f)
                )
                if (context.isSadeSati || context.isChandrashtama) {
                    Surface(
                        color = SoftErrorCoral.copy(alpha = 0.16f),
                        shape = CircleShape,
                        modifier = Modifier.border(1.dp, SoftErrorCoral.copy(alpha = 0.45f), CircleShape)
                    ) {
                        Text(
                            text = if (context.isChandrashtama) "Chandrashtama" else "Sade Sati",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftErrorCoral,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashaPeriodTile(
                    label = strings.mahadashaLabel,
                    lord = context.dasha.mahadashaLord,
                    span = "${context.dasha.mahadashaStart} – ${context.dasha.mahadashaEnd}",
                    accent = ChampagneGold,
                    modifier = Modifier.weight(1f)
                )
                DashaPeriodTile(
                    label = strings.antardashaLabel,
                    lord = context.dasha.antardashaLord,
                    span = "${context.dasha.antardashaStart} – ${context.dasha.antardashaEnd}",
                    accent = CelestialTeal,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = strings.gocharTransitsTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MoonlitLavender
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("gochar_transits_row")
            ) {
                items(context.transits) { transit ->
                    GocharTransitChip(transit = transit, strings = strings)
                }
            }

            if (chart.dashaEffect.isNotBlank() || chart.gocharEffect.isNotBlank()) {
                if (chart.dashaEffect.isNotBlank()) {
                    Surface(
                        color = ElevatedTwilight.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ChampagneGold.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = strings.dashaEffectTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = ChampagneGold
                            )
                            Text(
                                text = chart.dashaEffect,
                                style = MaterialTheme.typography.bodyMedium,
                                color = StarlightIvory.copy(alpha = 0.92f)
                            )
                        }
                    }
                }
                if (chart.gocharEffect.isNotBlank()) {
                    Surface(
                        color = ElevatedTwilight.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CelestialTeal.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = strings.gocharEffectTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = CelestialTeal
                            )
                            Text(
                                text = chart.gocharEffect,
                                style = MaterialTheme.typography.bodyMedium,
                                color = StarlightIvory.copy(alpha = 0.92f)
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = onAnalyzeImpact,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = MidnightCosmos
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analyze_dasha_gochar_button")
                ) {
                    Text(strings.analyzeDashaGochar, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun DashaPeriodTile(
    label: String,
    lord: String,
    span: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ElevatedTwilight,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = accent)
            Text(text = lord, style = MaterialTheme.typography.titleMedium, color = StarlightIvory)
            Text(text = span, style = MaterialTheme.typography.labelSmall, color = MutedNebula)
        }
    }
}

@Composable
private fun GocharTransitChip(
    transit: GocharTransit,
    strings: AppStrings
) {
    val statusColor = when (transit.favorability) {
        "Favorable" -> CelestialTeal
        "Challenging" -> SoftErrorCoral
        else -> MoonlitLavender
    }
    val statusLabel = when (transit.favorability) {
        "Favorable" -> strings.favorableLabel
        "Challenging" -> strings.challengingLabel
        else -> strings.neutralLabel
    }
    Surface(
        color = ElevatedTwilight,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = transit.planet.substringBefore(" "),
                style = MaterialTheme.typography.labelMedium,
                color = StarlightIvory
            )
            Text(
                text = "${transit.transitSign.symbol} ${transit.transitSign.sanskritName}",
                style = MaterialTheme.typography.labelLarge,
                color = ChampagneGold
            )
            Text(
                text = if (transit.specialNote.isNotBlank()) {
                    "${transit.houseFromMoon}H • ${transit.specialNote}"
                } else {
                    "${transit.houseFromMoon}H • $statusLabel"
                },
                style = MaterialTheme.typography.labelSmall,
                color = statusColor
            )
        }
    }
}
