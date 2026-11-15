package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.HoroscopeReading
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoroscopeScreen(
    profile: UserAstrologyProfile,
    selectedPeriod: String,
    horoscopesState: UiState<List<HoroscopeReading>>,
    horoscopeGenState: UiState<HoroscopeReading>,
    currentDateKey: String,
    currentMonthKey: String,
    currentLanguage: AppLanguage,
    strings: AppStrings,
    onSelectPeriod: (String) -> Unit,
    onGenerateHoroscope: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val signInfo = ZodiacCalculator.findByName(profile.zodiacSign)
    val dashaGochar = remember(profile.birthDate, profile.birthTime, profile.zodiacSign, currentDateKey) {
        ZodiacCalculator.calculateDashaAndGochar(profile.birthDate, profile.birthTime, profile.zodiacSign)
    }
    val transitMoon = remember(dashaGochar) {
        val moonTransit = dashaGochar.transits.firstOrNull()
        if (moonTransit != null) moonTransit.transitSign to (if (moonTransit.specialNote.isNotBlank()) moonTransit.specialNote else "${moonTransit.houseFromMoon}H")
        else ZodiacCalculator.currentTransitMoonSign()
    }
    val periodKey = if (selectedPeriod == "monthly") currentMonthKey else currentDateKey
    val expectedId = "${selectedPeriod}_${periodKey}_${profile.zodiacSign}_${currentLanguage.code}"
        .lowercase(java.util.Locale.US)
        .replace(Regex("[^a-z0-9_\\-]"), "_")

    val savedReading = (horoscopesState as? UiState.Success)?.data?.firstOrNull {
        it.id == expectedId || (it.periodType == selectedPeriod && it.periodKey == periodKey && it.zodiacSign == profile.zodiacSign)
    }

    val activeReading = when (horoscopeGenState) {
        is UiState.Success -> {
            if (horoscopeGenState.data.periodType == selectedPeriod && horoscopeGenState.data.zodiacSign == profile.zodiacSign) {
                horoscopeGenState.data
            } else {
                savedReading
            }
        }
        else -> savedReading
    }

    LaunchedEffect(
        profile.userId,
        profile.zodiacSign,
        profile.nakshatra,
        profile.isMoonSignManual,
        selectedPeriod,
        currentLanguage,
        horoscopesState is UiState.Success
    ) {
        if (horoscopesState is UiState.Success && savedReading == null && horoscopeGenState is UiState.Idle) {
            onGenerateHoroscope(selectedPeriod)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(188.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_celestial_banner),
                        contentDescription = "Celestial night sky banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MidnightCosmos.copy(alpha = 0.35f),
                                        MidnightCosmos.copy(alpha = 0.90f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = CosmicSlate.copy(alpha = 0.88f),
                                shape = CircleShape,
                                modifier = Modifier.border(1.dp, ChampagneGold.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Text(
                                    text = "☽ ${signInfo.symbol} ${signInfo.sanskritName} (${signInfo.name}) • ${signInfo.element}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ChampagneGold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = periodKey,
                                style = MaterialTheme.typography.labelMedium,
                                color = StarlightIvory.copy(alpha = 0.85f)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = profile.fullName,
                                style = MaterialTheme.typography.headlineLarge,
                                color = StarlightIvory
                            )
                            val nakText = if (profile.nakshatra.isNotBlank()) {
                                "${strings.nakshatraLabel}: ${profile.nakshatra} (Pada ${profile.nakshatraPada}) • "
                            } else {
                                ""
                            }
                            Text(
                                text = "$nakText${profile.birthPlace} • ${profile.focusArea}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MoonlitLavender
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                        SegmentedButton(
                            selected = selectedPeriod == "daily",
                            onClick = { onSelectPeriod("daily") },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = ChampagneGold,
                                activeContentColor = MidnightCosmos,
                                inactiveContainerColor = DeepIndigoSurface,
                                inactiveContentColor = StarlightIvory
                            ),
                            modifier = Modifier.testTag("period_daily_tab")
                        ) {
                            Text(strings.dailyLabel, style = MaterialTheme.typography.labelLarge)
                        }
                        SegmentedButton(
                            selected = selectedPeriod == "monthly",
                            onClick = { onSelectPeriod("monthly") },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = ChampagneGold,
                                activeContentColor = MidnightCosmos,
                                inactiveContainerColor = DeepIndigoSurface,
                                inactiveContentColor = StarlightIvory
                            ),
                            modifier = Modifier.testTag("period_monthly_tab")
                        ) {
                            Text(strings.monthlyLabel, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Surface(
                        color = ElevatedTwilight,
                        shape = CircleShape,
                        modifier = Modifier.border(1.dp, HairlineBorder, CircleShape)
                    ) {
                        IconButton(
                            onClick = { onGenerateHoroscope(selectedPeriod) },
                            enabled = horoscopeGenState !is UiState.Loading,
                            modifier = Modifier.testTag("refresh_horoscope_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = strings.regenerateReading,
                                tint = ChampagneGold
                            )
                        }
                    }
                }
            }

            if (horoscopeGenState is UiState.Loading && activeReading == null) {
                item {
                    Surface(
                        color = DeepIndigoSurface,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = ChampagneGold)
                            Text(
                                text = "Reading celestial transits for ${profile.fullName}...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MoonlitLavender
                            )
                        }
                    }
                }
            } else if (horoscopeGenState is UiState.Error && activeReading == null) {
                item {
                    Surface(
                        color = SoftErrorCoral.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SoftErrorCoral.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = horoscopeGenState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftErrorCoral
                            )
                            Button(
                                onClick = { onGenerateHoroscope(selectedPeriod) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChampagneGold,
                                    contentColor = MidnightCosmos
                                ),
                                modifier = Modifier.testTag("retry_horoscope_button")
                            ) {
                                Text(strings.retry)
                            }
                        }
                    }
                }
            } else if (activeReading != null) {
                if (horoscopeGenState is UiState.Error) {
                    item {
                        Surface(
                            color = SoftErrorCoral.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SoftErrorCoral.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = horoscopeGenState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftErrorCoral,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onGenerateHoroscope(selectedPeriod) },
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
                }
                item {
                    Surface(
                        color = DeepIndigoSurface.copy(alpha = 0.92f),
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
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = activeReading.headline,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = ChampagneGold
                                    )
                                    if (horoscopeGenState is UiState.Loading) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = MoonlitLavender
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Refreshing...",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MoonlitLavender
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                CosmicScoreGauge(
                                    score = activeReading.cosmicScore.toInt(),
                                    label = strings.cosmicAlignment
                                )
                            }

                            Text(
                                text = activeReading.overview,
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarlightIvory
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CosmicMiniPill(
                                    label = strings.luckyNumberLabel,
                                    value = activeReading.luckyNumber.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                CosmicMiniPill(
                                    label = strings.luckyColorLabel,
                                    value = activeReading.luckyColor,
                                    modifier = Modifier.weight(1f)
                                )
                                CosmicMiniPill(
                                    label = strings.powerPlanetLabel,
                                    value = activeReading.powerPlanet,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    PrecautionsCard(
                        title = strings.precautionsTitle,
                        transitLabel = "${strings.transitMoonLabel}: ${transitMoon.first.sanskritName} (${transitMoon.second})",
                        jobTitle = strings.jobPrecautionsLabel,
                        lifeTitle = strings.lifePrecautionsLabel,
                        jobPrecautions = activeReading.jobPrecautions,
                        lifePrecautions = activeReading.lifePrecautions,
                        emptyHint = strings.emptyPrecautionsHint,
                        revealLabel = strings.revealPrecautions,
                        isLoading = horoscopeGenState is UiState.Loading,
                        onRevealPrecautions = { onGenerateHoroscope(selectedPeriod) }
                    )
                }

                item {
                    DashaGocharBannerCard(
                        title = strings.dashaAndGocharTitle,
                        mahadashaLabel = strings.mahadashaLabel,
                        antardashaLabel = strings.antardashaLabel,
                        dasha = dashaGochar.dasha,
                        isSadeSati = dashaGochar.isSadeSati,
                        isChandrashtama = dashaGochar.isChandrashtama,
                        keyTransits = dashaGochar.transits.take(4),
                        note = activeReading.dashaGocharNote
                    )
                }

                item {
                    InsightSectionCard(
                        icon = Icons.Rounded.Favorite,
                        accent = RoseQuartz,
                        title = strings.loveLabel,
                        body = activeReading.loveInsight
                    )
                }

                item {
                    InsightSectionCard(
                        icon = Icons.Rounded.WorkOutline,
                        accent = ChampagneGold,
                        title = strings.careerLabel,
                        body = activeReading.careerInsight
                    )
                }

                item {
                    InsightSectionCard(
                        icon = Icons.Rounded.Spa,
                        accent = CelestialTeal,
                        title = strings.wellnessLabel,
                        body = activeReading.wellnessInsight
                    )
                }
            } else {
                item {
                    Button(
                        onClick = { onGenerateHoroscope(selectedPeriod) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = MidnightCosmos
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_horoscope_button")
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.generateReading, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun CosmicMiniPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ElevatedTwilight,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, HairlineBorder, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MutedNebula,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = StarlightIvory,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InsightSectionCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    body: String
) {
    Surface(
        color = DeepIndigoSurface.copy(alpha = 0.88f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HairlineBorder, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accent.copy(alpha = 0.16f),
                    shape = CircleShape,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = StarlightIvory
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = StarlightIvory.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun PrecautionsCard(
    title: String,
    transitLabel: String,
    jobTitle: String,
    lifeTitle: String,
    jobPrecautions: List<String>,
    lifePrecautions: List<String>,
    emptyHint: String,
    revealLabel: String,
    isLoading: Boolean,
    onRevealPrecautions: () -> Unit
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            SoftErrorCoral.copy(alpha = 0.55f),
            ChampagneGold.copy(alpha = 0.45f)
        )
    )
    Surface(
        color = DeepIndigoSurface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, borderBrush, RoundedCornerShape(22.dp))
            .testTag("precautions_section_card")
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
                    color = SoftErrorCoral.copy(alpha = 0.16f),
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = SoftErrorCoral,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = StarlightIvory
                    )
                    Text(
                        text = "☽ $transitLabel",
                        style = MaterialTheme.typography.labelMedium,
                        color = ChampagneGold.copy(alpha = 0.9f)
                    )
                }
            }

            if (jobPrecautions.isEmpty() && lifePrecautions.isEmpty()) {
                Surface(
                    color = ElevatedTwilight.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = emptyHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MoonlitLavender
                        )
                        Button(
                            onClick = onRevealPrecautions,
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = MidnightCosmos
                            ),
                            modifier = Modifier.testTag("reveal_precautions_button")
                        ) {
                            Text(revealLabel, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else {
                if (jobPrecautions.isNotEmpty()) {
                    PrecautionDomainGroup(
                        icon = Icons.Rounded.WorkOutline,
                        accent = ChampagneGold,
                        heading = jobTitle,
                        items = jobPrecautions,
                        modifier = Modifier.testTag("job_precautions_list")
                    )
                }
                if (jobPrecautions.isNotEmpty() && lifePrecautions.isNotEmpty()) {
                    HorizontalDivider(color = HairlineBorder)
                }
                if (lifePrecautions.isNotEmpty()) {
                    PrecautionDomainGroup(
                        icon = Icons.Rounded.Shield,
                        accent = SoftErrorCoral,
                        heading = lifeTitle,
                        items = lifePrecautions,
                        modifier = Modifier.testTag("life_precautions_list")
                    )
                }
            }
        }
    }
}

@Composable
private fun PrecautionDomainGroup(
    icon: ImageVector,
    accent: Color,
    heading: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = heading,
                style = MaterialTheme.typography.titleSmall,
                color = accent
            )
        }
        items.forEachIndexed { index, precaution ->
            Surface(
                color = ElevatedTwilight.copy(alpha = 0.72f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = accent.copy(alpha = 0.18f),
                        shape = CircleShape,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent
                            )
                        }
                    }
                    Text(
                        text = precaution,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarlightIvory.copy(alpha = 0.94f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashaGocharBannerCard(
    title: String,
    mahadashaLabel: String,
    antardashaLabel: String,
    dasha: com.example.model.DashaSnapshot,
    isSadeSati: Boolean,
    isChandrashtama: Boolean,
    keyTransits: List<com.example.model.GocharTransit>,
    note: String
) {
    Surface(
        color = DeepIndigoSurface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MoonlitLavender.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .testTag("horoscope_dasha_gochar_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ChampagneGold,
                    modifier = Modifier.weight(1f)
                )
                if (isSadeSati || isChandrashtama) {
                    Surface(
                        color = SoftErrorCoral.copy(alpha = 0.18f),
                        shape = CircleShape,
                        modifier = Modifier.border(1.dp, SoftErrorCoral.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Text(
                            text = if (isChandrashtama) "Chandrashtama" else "Sade Sati",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftErrorCoral,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CosmicMiniPill(
                    label = mahadashaLabel,
                    value = "${dasha.mahadashaLord} (${dasha.mahadashaEnd})",
                    modifier = Modifier.weight(1f)
                )
                CosmicMiniPill(
                    label = antardashaLabel,
                    value = "${dasha.antardashaLord} (${dasha.antardashaEnd})",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                keyTransits.forEach { t ->
                    val tint = when (t.favorability) {
                        "Favorable" -> CelestialTeal
                        "Challenging" -> SoftErrorCoral
                        else -> MoonlitLavender
                    }
                    Surface(
                        color = ElevatedTwilight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = t.planet.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall,
                                color = tint,
                                maxLines = 1
                            )
                            Text(
                                text = t.transitSign.sanskritName,
                                style = MaterialTheme.typography.labelMedium,
                                color = StarlightIvory,
                                maxLines = 1
                            )
                            Text(
                                text = "${t.houseFromMoon}H",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedNebula
                            )
                        }
                    }
                }
            }

            if (note.isNotBlank()) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarlightIvory.copy(alpha = 0.9f)
                )
            }
        }
    }
}
