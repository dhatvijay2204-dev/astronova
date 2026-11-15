package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Brightness3
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.UserAstrologyProfile
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CosmicSlate
import com.example.ui.theme.DeepIndigoSurface
import com.example.ui.theme.ElevatedTwilight
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MidnightCosmos
import com.example.ui.theme.MoonlitLavender
import com.example.ui.theme.MutedNebula
import com.example.ui.theme.StarlightIvory
import com.example.util.LunarPhaseCalculator
import com.example.util.MoonPhaseType
import com.example.util.UpcomingMoonEvent
import com.example.util.ZodiacCalculator
import java.util.Locale
import kotlin.math.cos

@Composable
fun LunarPhaseScreen(
    profile: UserAstrologyProfile,
    currentLanguage: AppLanguage,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val userSignInfo = remember(profile.zodiacSign) { ZodiacCalculator.findByName(profile.zodiacSign) }
    val userRashiLabel = remember(userSignInfo) { "${userSignInfo.sanskritName} (${userSignInfo.name})" }
    val transitMoon = remember { ZodiacCalculator.currentTransitMoonSign() }
    val lunarStatus = remember(userRashiLabel, currentLanguage) {
        LunarPhaseCalculator.getCurrentLunarStatus(userRashiLabel, currentLanguage)
    }
    val upcomingEvents = remember(userRashiLabel, currentLanguage) {
        LunarPhaseCalculator.getUpcomingEvents(userRashiLabel, currentLanguage)
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
                    text = strings.lunarPhaseTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ChampagneGold
                )
            }

            item {
                Surface(
                    color = DeepIndigoSurface.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MoonDiscCanvas(
                            cycleFraction = lunarStatus.cycleFraction,
                            modifier = Modifier.size(148.dp)
                        )

                        Text(
                            text = lunarStatus.phaseName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = StarlightIvory
                        )

                        Surface(
                            color = CosmicSlate,
                            shape = CircleShape,
                            modifier = Modifier.border(1.dp, ChampagneGold.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Text(
                                text = "${strings.transitMoonLabel}: ☽ ${transitMoon.first.symbol} ${transitMoon.first.sanskritName} (${transitMoon.first.name}) • ${transitMoon.second}",
                                style = MaterialTheme.typography.labelLarge,
                                color = ChampagneGold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LunarStatPill(
                                label = strings.illuminationLabel,
                                value = "${lunarStatus.illuminationPercent}%",
                                modifier = Modifier.weight(1f)
                            )
                            LunarStatPill(
                                label = strings.lunarAgeLabel,
                                value = String.format(Locale.US, "%.1f d", lunarStatus.lunarAgeDays),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = lunarStatus.astrologicalSignificance,
                            style = MaterialTheme.typography.bodyLarge,
                            color = StarlightIvory
                        )

                        Surface(
                            color = ElevatedTwilight,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = lunarStatus.personalImpact,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MoonlitLavender
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = strings.upcomingMoonEventsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = ChampagneGold
                )
            }

            items(upcomingEvents) { event ->
                UpcomingMoonEventCard(event = event)
            }
        }
    }
}

@Composable
fun MoonDiscCanvas(
    cycleFraction: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val r = size.minDimension * 0.42f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Atmospheric moonlight halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ChampagneGold.copy(alpha = 0.25f), Color.Transparent),
                center = center,
                radius = size.minDimension * 0.5f
            ),
            center = center,
            radius = size.minDimension * 0.5f
        )

        // Dark lunar base
        drawCircle(
            color = CosmicSlate,
            radius = r,
            center = center
        )

        val circlePath = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    center.x - r,
                    center.y - r,
                    center.x + r,
                    center.y + r
                )
            )
        }

        clipPath(circlePath) {
            val phaseAngle = cycleFraction * 2f * Math.PI.toFloat()
            val waxing = cycleFraction < 0.5f
            val rx = (r * kotlin.math.abs(cos(phaseAngle.toDouble())).toFloat()).coerceAtLeast(1f)

            // Illuminated half (right side when waxing, left side when waning)
            if (waxing) {
                drawArc(
                    color = StarlightIvory,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(center.x - r, center.y - r),
                    size = Size(2 * r, 2 * r)
                )
                val ellipseColor = if (cycleFraction < 0.25f) CosmicSlate else StarlightIvory
                drawOval(
                    color = ellipseColor,
                    topLeft = Offset(center.x - rx, center.y - r),
                    size = Size(2 * rx, 2 * r)
                )
            } else {
                drawArc(
                    color = StarlightIvory,
                    startAngle = 90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(center.x - r, center.y - r),
                    size = Size(2 * r, 2 * r)
                )
                val ellipseColor = if (cycleFraction < 0.75f) StarlightIvory else CosmicSlate
                drawOval(
                    color = ellipseColor,
                    topLeft = Offset(center.x - rx, center.y - r),
                    size = Size(2 * rx, 2 * r)
                )
            }
        }

        // Subtle rim stroke
        drawCircle(
            color = ChampagneGold.copy(alpha = 0.55f),
            radius = r,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
private fun LunarStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ElevatedTwilight,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MutedNebula)
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = ChampagneGold)
        }
    }
}

@Composable
private fun UpcomingMoonEventCard(event: UpcomingMoonEvent) {
    val iconFraction = when (event.phaseType) {
        MoonPhaseType.NEW_MOON -> 0.0f
        MoonPhaseType.FIRST_QUARTER -> 0.25f
        MoonPhaseType.FULL_MOON -> 0.5f
        MoonPhaseType.LAST_QUARTER -> 0.75f
        else -> 0.25f
    }
    Surface(
        color = DeepIndigoSurface.copy(alpha = 0.88f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HairlineBorder, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            MoonDiscCanvas(
                cycleFraction = iconFraction,
                modifier = Modifier.size(48.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = StarlightIvory
                    )
                    Surface(
                        color = CosmicSlate,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${event.dateFormatted} • ${event.daysAway}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChampagneGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = event.significance,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarlightIvory.copy(alpha = 0.88f)
                )
                Text(
                    text = event.impactHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MoonlitLavender
                )
            }
        }
    }
}
