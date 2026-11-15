package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.PlanetaryPlacement
import com.example.ui.theme.CelestialTeal
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CosmicSlate
import com.example.ui.theme.DeepGold
import com.example.ui.theme.DeepIndigoSurface
import com.example.ui.theme.ElevatedTwilight
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MidnightCosmos
import com.example.ui.theme.MoonlitLavender
import com.example.ui.theme.MutedNebula
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.StarlightIvory
import com.example.util.ZodiacCalculator
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CosmicBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val stars = remember {
        listOf(
            0.12f to 0.08f, 0.28f to 0.15f, 0.48f to 0.06f, 0.72f to 0.11f,
            0.88f to 0.18f, 0.15f to 0.34f, 0.83f to 0.38f, 0.22f to 0.62f,
            0.66f to 0.56f, 0.91f to 0.74f, 0.08f to 0.82f, 0.44f to 0.88f
        )
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightCosmos, DeepIndigoSurface, MidnightCosmos)
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ChampagneGold.copy(alpha = 0.09f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.12f),
                        radius = size.width * 0.65f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(MoonlitLavender.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.72f),
                        radius = size.width * 0.7f
                    )
                )
                stars.forEachIndexed { idx, (fx, fy) ->
                    val radius = if (idx % 3 == 0) 2.5f else 1.5f
                    drawCircle(
                        color = StarlightIvory.copy(alpha = if (idx % 2 == 0) 0.45f else 0.22f),
                        radius = radius,
                        center = Offset(size.width * fx, size.height * fy)
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
fun CosmicScoreGauge(
    score: Int,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = ChampagneGold
) {
    val clamped = score.coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "cosmic_gauge"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(96.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                drawArc(
                    color = HairlineBorder,
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(MoonlitLavender, accentColor, DeepGold)
                    ),
                    startAngle = 140f,
                    sweepAngle = 260f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$clamped%",
                    style = MaterialTheme.typography.titleLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MutedNebula
        )
    }
}

@Composable
fun NatalBirthChartWheel(
    placements: List<PlanetaryPlacement>,
    sunSign: String,
    moonSign: String,
    risingSign: String,
    modifier: Modifier = Modifier
) {
    val signs = ZodiacCalculator.allSigns
    val risingIdx = ZodiacCalculator.signIndex(risingSign)

    val planetAngles = remember(placements, risingIdx) {
        placements.map { p ->
            val signIdx = ZodiacCalculator.signIndex(p.sign)
            // Position relative to Ascendant on the left (180 deg) going counter-clockwise
            val relativeSignOffset = ((signIdx - risingIdx + 12) % 12) * 30f + (p.degree % 30).toFloat()
            val angleDeg = 180f - relativeSignOffset
            p to angleDeg
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val center = Offset(cx, cy)
            val outerR = size.minDimension * 0.47f
            val zodiacInnerR = size.minDimension * 0.36f
            val planetRingR = size.minDimension * 0.28f
            val innerHubR = size.minDimension * 0.14f

            // Subtle glow behind wheel
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ChampagneGold.copy(alpha = 0.12f), Color.Transparent),
                    center = center,
                    radius = outerR * 1.08f
                ),
                center = center,
                radius = outerR * 1.08f
            )

            // Outer & inner concentric rings
            drawCircle(
                color = ElevatedTwilight.copy(alpha = 0.85f),
                radius = outerR,
                center = center
            )
            drawCircle(
                color = ChampagneGold.copy(alpha = 0.65f),
                radius = outerR,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = MidnightCosmos.copy(alpha = 0.9f),
                radius = zodiacInnerR,
                center = center
            )
            drawCircle(
                color = HairlineBorder,
                radius = zodiacInnerR,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = HairlineBorder.copy(alpha = 0.6f),
                radius = innerHubR,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            val labelPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = size.minDimension * 0.05f
                color = android.graphics.Color.parseColor("#F6F2E9")
            }

            val housePaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = size.minDimension * 0.028f
                color = android.graphics.Color.parseColor("#9AA3C2")
            }

            // 12 zodiac sectors & house cusps
            for (i in 0 until 12) {
                val cuspDeg = 180f - i * 30f
                val cuspRad = cuspDeg * PI / 180.0
                val pStart = Offset(
                    cx + (innerHubR * cos(cuspRad)).toFloat(),
                    cy + (innerHubR * sin(cuspRad)).toFloat()
                )
                val pEnd = Offset(
                    cx + (outerR * cos(cuspRad)).toFloat(),
                    cy + (outerR * sin(cuspRad)).toFloat()
                )
                drawLine(
                    color = if (i % 3 == 0) ChampagneGold.copy(alpha = 0.55f) else HairlineBorder,
                    start = pStart,
                    end = pEnd,
                    strokeWidth = if (i % 3 == 0) 1.5.dp.toPx() else 1.dp.toPx()
                )

                val midDeg = cuspDeg - 15f
                val midRad = midDeg * PI / 180.0
                val signObj = signs[(risingIdx + i) % 12]
                val symbolR = (outerR + zodiacInnerR) / 2f
                val sx = cx + (symbolR * cos(midRad)).toFloat()
                val sy = cy + (symbolR * sin(midRad)).toFloat() + labelPaint.textSize * 0.35f
                drawContext.canvas.nativeCanvas.drawText(signObj.symbol, sx, sy, labelPaint)

                val houseNumR = innerHubR + (zodiacInnerR - innerHubR) * 0.24f
                val hx = cx + (houseNumR * cos(midRad)).toFloat()
                val hy = cy + (houseNumR * sin(midRad)).toFloat() + housePaint.textSize * 0.35f
                drawContext.canvas.nativeCanvas.drawText("${i + 1}", hx, hy, housePaint)
            }

            // Aspect lines between planetary placements inside inner ring
            val planetPoints = planetAngles.map { (placement, angleDeg) ->
                val rad = angleDeg * PI / 180.0
                Triple(
                    placement,
                    angleDeg,
                    Offset(
                        cx + (planetRingR * cos(rad)).toFloat(),
                        cy + (planetRingR * sin(rad)).toFloat()
                    )
                )
            }

            for (i in planetPoints.indices) {
                for (j in i + 1 until planetPoints.size) {
                    val diff = abs((planetPoints[i].second - planetPoints[j].second + 360f) % 360f)
                    val shortest = if (diff > 180f) 360f - diff else diff
                    val aspectColor = when {
                        abs(shortest - 120f) <= 9f -> ChampagneGold.copy(alpha = 0.45f) // Trine
                        abs(shortest - 60f) <= 7f -> CelestialTeal.copy(alpha = 0.45f) // Sextile
                        abs(shortest - 90f) <= 8f || abs(shortest - 180f) <= 9f -> RoseQuartz.copy(alpha = 0.42f) // Square/Opposition
                        else -> null
                    }
                    if (aspectColor != null) {
                        drawLine(
                            color = aspectColor,
                            start = planetPoints[i].third,
                            end = planetPoints[j].third,
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }
                }
            }

            // Planet markers & glyphs
            val glyphPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = size.minDimension * 0.036f
                color = android.graphics.Color.parseColor("#0B0E1A")
                isFakeBoldText = true
            }
            planetPoints.forEach { (placement, _, pos) ->
                val badgeColor = when (placement.planet.lowercase(Locale.US)) {
                    "sun" -> ChampagneGold
                    "moon" -> StarlightIvory
                    "venus" -> RoseQuartz
                    "mars" -> Color(0xFFFF8A65)
                    "jupiter", "ascendant" -> CelestialTeal
                    "rahu" -> Color(0xFF7FA3D6)
                    "ketu" -> Color(0xFFD69F7E)
                    else -> MoonlitLavender
                }
                drawCircle(
                    color = badgeColor,
                    radius = 12.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = MidnightCosmos,
                    radius = 12.dp.toPx(),
                    center = pos,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                val glyph = planetGlyph(placement.planet)
                drawContext.canvas.nativeCanvas.drawText(
                    glyph,
                    pos.x,
                    pos.y + glyphPaint.textSize * 0.35f,
                    glyphPaint
                )
            }

            // Center Moon / Sun badge (Moon Sign primary for Vedic Kundali)
            val hubPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = size.minDimension * 0.034f
                color = android.graphics.Color.parseColor("#F2C97D")
                isFakeBoldText = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                ZodiacCalculator.findByName(moonSign).symbol + " " + ZodiacCalculator.findByName(sunSign).symbol,
                cx,
                cy + hubPaint.textSize * 0.35f,
                hubPaint
            )
        }
    }
}

private fun planetGlyph(planet: String): String {
    return when (planet.trim().lowercase(Locale.US)) {
        "sun" -> "☉"
        "moon" -> "☽"
        "mercury" -> "☿"
        "venus" -> "♀"
        "mars" -> "♂"
        "jupiter" -> "♃"
        "saturn" -> "♄"
        "rahu" -> "☊"
        "ketu" -> "☋"
        "uranus" -> "♅"
        "neptune" -> "♆"
        "pluto" -> "♇"
        "ascendant", "rising" -> "LG"
        else -> planet.take(2).uppercase(Locale.US)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BirthDetailsEditorCard(
    strings: AppStrings,
    initialName: String,
    initialBirthDate: String,
    initialBirthTime: String,
    initialBirthPlace: String,
    initialFocusArea: String,
    isSaving: Boolean,
    submitLabel: String,
    initialManualMoonSign: String? = null,
    onSubmit: (
        name: String,
        birthDate: String,
        birthTime: String,
        birthPlace: String,
        focusArea: String,
        manualMoonSign: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fullName by remember(initialName) { mutableStateOf(initialName) }
    var birthDate by remember(initialBirthDate) {
        mutableStateOf(initialBirthDate.ifBlank { "1996-05-14" })
    }
    var birthTime by remember(initialBirthTime) {
        mutableStateOf(initialBirthTime.ifBlank { "08:00" })
    }
    var birthPlace by remember(initialBirthPlace) { mutableStateOf(initialBirthPlace) }
    var focusArea by remember(initialFocusArea) {
        mutableStateOf(initialFocusArea.ifBlank { ZodiacCalculator.focusAreas.first() })
    }
    var manualMoonSign by remember(initialManualMoonSign) { mutableStateOf(initialManualMoonSign) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val vedicPreview = remember(birthDate, birthTime) {
        if (ZodiacCalculator.isValidBirthDate(birthDate)) {
            ZodiacCalculator.calculateVedicMoonDetails(birthDate, birthTime)
        } else {
            null
        }
    }
    val activeMoonInfo = remember(vedicPreview, manualMoonSign) {
        val manual = manualMoonSign
        if (!manual.isNullOrBlank()) ZodiacCalculator.findByName(manual) else vedicPreview?.moonSign
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

    Surface(
        color = DeepIndigoSurface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = strings.birthDetailsTitle,
                style = MaterialTheme.typography.titleLarge,
                color = ChampagneGold
            )

            if (activeMoonInfo != null) {
                Surface(
                    color = CosmicSlate.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ChampagneGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${strings.moonRashiLabel}: ☽ ${activeMoonInfo.symbol} ${activeMoonInfo.sanskritName} (${activeMoonInfo.name})",
                            style = MaterialTheme.typography.titleMedium,
                            color = ChampagneGold
                        )
                        if (vedicPreview != null) {
                            Text(
                                text = "${strings.nakshatraLabel}: ${vedicPreview.nakshatra} • Pada ${vedicPreview.pada} (${vedicPreview.nakshatraLord})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MoonlitLavender
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(strings.fullNameLabel) },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = ChampagneGold) },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_full_name")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text(strings.birthDateLabel) },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_birth_date")
                )
                OutlinedButton(
                    onClick = {
                        val parts = birthDate.split("-")
                        val cal = Calendar.getInstance()
                        val y = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR) - 25
                        val m = (parts.getOrNull(1)?.toIntOrNull() ?: 5) - 1
                        val d = parts.getOrNull(2)?.toIntOrNull() ?: 14
                        DatePickerDialog(context, { _, year, month, day ->
                            birthDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                        }, y, m, d).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("pick_birth_date_button")
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
                    value = birthTime,
                    onValueChange = { birthTime = it },
                    label = { Text(strings.birthTimeLabel) },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_birth_time")
                )
                OutlinedButton(
                    onClick = {
                        val parts = birthTime.split(":")
                        val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        TimePickerDialog(context, { _, hour, minute ->
                            birthTime = String.format(Locale.US, "%02d:%02d", hour, minute)
                        }, h, min, true).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("pick_birth_time_button")
                ) {
                    Icon(Icons.Rounded.Schedule, contentDescription = "Pick time", tint = ChampagneGold)
                }
            }

            OutlinedTextField(
                value = birthPlace,
                onValueChange = { birthPlace = it },
                label = { Text(strings.birthPlaceLabel) },
                leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = ChampagneGold) },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_birth_place")
            )

            Text(
                text = strings.overrideMoonSignLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MoonlitLavender
            )
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = manualMoonSign == null,
                        onClick = { manualMoonSign = null },
                        label = { Text(strings.autoFromBirthLabel, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampagneGold,
                            selectedLabelColor = MidnightCosmos,
                            containerColor = ElevatedTwilight,
                            labelColor = StarlightIvory
                        ),
                        modifier = Modifier.testTag("moon_rashi_auto")
                    )
                }
                items(ZodiacCalculator.allSigns.size) { index ->
                    val sign = ZodiacCalculator.allSigns[index]
                    val isSelected = manualMoonSign.equals(sign.name, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { manualMoonSign = sign.name },
                        label = {
                            Text(
                                "${sign.symbol} ${sign.sanskritName} (${sign.name})",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampagneGold,
                            selectedLabelColor = MidnightCosmos,
                            containerColor = ElevatedTwilight,
                            labelColor = StarlightIvory
                        ),
                        modifier = Modifier.testTag("moon_rashi_${sign.name}")
                    )
                }
            }

            Text(
                text = strings.focusAreaLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MoonlitLavender
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ZodiacCalculator.focusAreas.forEach { area ->
                    FilterChip(
                        selected = focusArea == area,
                        onClick = { focusArea = area },
                        label = { Text(area, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampagneGold,
                            selectedLabelColor = MidnightCosmos,
                            containerColor = ElevatedTwilight,
                            labelColor = StarlightIvory
                        )
                    )
                }
            }

            if (!validationError.isNullOrBlank()) {
                Text(
                    text = validationError.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    when {
                        fullName.isBlank() -> validationError = "Please enter your name."
                        !ZodiacCalculator.isValidBirthDate(birthDate.trim()) ->
                            validationError = "Birth date must use YYYY-MM-DD format."
                        birthPlace.isBlank() -> validationError = "Please enter your birth city."
                        else -> {
                            validationError = null
                            onSubmit(
                                fullName.trim(),
                                birthDate.trim(),
                                birthTime.trim().ifBlank { "Unknown" },
                                birthPlace.trim(),
                                focusArea,
                                manualMoonSign
                            )
                        }
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = MidnightCosmos
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_birth_details_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MidnightCosmos,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = submitLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageSelectorDialog(
    strings: AppStrings,
    currentLanguage: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepIndigoSurface,
        titleContentColor = ChampagneGold,
        textContentColor = StarlightIvory,
        title = {
            Text(text = strings.chooseLanguageTitle, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { lang ->
                    val selected = lang == currentLanguage
                    Surface(
                        color = if (selected) ChampagneGold.copy(alpha = 0.2f) else ElevatedTwilight,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (selected) ChampagneGold else HairlineBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onSelect(lang)
                                onDismiss()
                            }
                            .testTag("dialog_lang_${lang.code}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.nativeLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selected) ChampagneGold else StarlightIvory
                            )
                            Text(
                                text = lang.englishName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedNebula
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ChampagneGold)
            }
        }
    )
}
