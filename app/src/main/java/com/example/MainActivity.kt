package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.ai.AiAstrologyService
import com.example.data.firebase.configureAppCheck
import com.example.data.local.AstrologyDatabase
import com.example.data.repository.AstrologyRepository
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LanguageManager
import com.example.model.BirthChartReading
import com.example.model.CompatibilityReading
import com.example.model.DailyAffirmation
import com.example.model.HoroscopeReading
import com.example.model.UiState
import com.example.model.UserAstrologyProfile
import com.example.notifications.NotificationHelper
import com.example.ui.components.BirthDetailsEditorCard
import com.example.ui.components.CosmicBackdrop
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.screens.AffirmationsScreen
import com.example.ui.screens.BirthChartScreen
import com.example.ui.screens.CompatibilityScreen
import com.example.ui.screens.HoroscopeScreen
import com.example.ui.screens.LunarPhaseScreen
import com.example.ui.theme.AstraNovaTheme
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CosmicSlate
import com.example.ui.theme.DeepIndigoSurface
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MidnightCosmos
import com.example.ui.theme.MoonlitLavender
import com.example.ui.theme.MutedNebula
import com.example.ui.theme.SoftErrorCoral
import com.example.ui.theme.StarlightIvory
import com.example.ui.viewmodel.AstrologyViewModel
import com.example.util.ZodiacCalculator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Must execute before UI/Firebase AI calls so App Check verifies debug/Play tokens early.
        configureAppCheck(this, intent)
        LanguageManager.init(this)
        NotificationHelper.ensureChannel(this)
        enableEdgeToEdge()

        setContent {
            AstraNovaTheme {
                AstraNovaRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureAppCheck(this, intent)
    }
}

private enum class AstraTab(val icon: ImageVector, val testTag: String) {
    HOROSCOPE(Icons.Rounded.AutoAwesome, "nav_tab_horoscope"),
    BIRTH_CHART(Icons.Rounded.Explore, "nav_tab_birth_chart"),
    COMPATIBILITY(Icons.Rounded.Favorite, "nav_tab_compatibility"),
    MOON(Icons.Rounded.NightsStay, "nav_tab_moon"),
    AFFIRMATIONS(Icons.Rounded.SelfImprovement, "nav_tab_affirmations")
}

@Composable
private fun AstraNovaRoot() {
    val context = LocalContext.current
    val currentLanguage by LanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val strings = remember(currentLanguage) { AppStrings.forLanguage(currentLanguage) }

    val viewModel: AstrologyViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val db = AstrologyDatabase.getInstance(context.applicationContext)
                val repository = AstrologyRepository(db.astrologyDao())
                AstrologyViewModel(
                    repository = repository,
                    aiService = AiAstrologyService()
                )
            }
        }
    )

    AstrologyFlow(
        viewModel = viewModel,
        currentLanguage = currentLanguage,
        strings = strings,
        onLanguageSelected = { LanguageManager.setLanguage(context, it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AstrologyFlow(
    viewModel: AstrologyViewModel,
    currentLanguage: AppLanguage,
    strings: AppStrings,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val horoscopesState by viewModel.horoscopesState.collectAsStateWithLifecycle()
    val affirmationsState by viewModel.affirmationsState.collectAsStateWithLifecycle()
    val birthChartsState by viewModel.birthChartsState.collectAsStateWithLifecycle()
    val compatibilitiesState by viewModel.compatibilitiesState.collectAsStateWithLifecycle()

    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val horoscopeGenState by viewModel.horoscopeGenState.collectAsStateWithLifecycle()
    val affirmationGenState by viewModel.affirmationGenState.collectAsStateWithLifecycle()
    val birthChartGenState by viewModel.birthChartGenState.collectAsStateWithLifecycle()
    val compatibilityGenState by viewModel.compatibilityGenState.collectAsStateWithLifecycle()
    val isSavingProfile by viewModel.isSavingProfile.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showProfileDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    if (showLanguageDialog) {
        LanguageSelectorDialog(
            strings = strings,
            currentLanguage = currentLanguage,
            onSelect = onLanguageSelected,
            onDismiss = { showLanguageDialog = false }
        )
    }

    when (val pState = profileState) {
        is UiState.Idle, is UiState.Loading -> {
            CosmicBackdrop {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ChampagneGold)
                }
            }
        }

        is UiState.Error -> {
            CosmicBackdrop {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = DeepIndigoSurface,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .widthIn(max = 460.dp)
                            .border(1.dp, SoftErrorCoral.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                    ) {
                        Text(
                            text = pState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SoftErrorCoral,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }

        is UiState.Success -> {
            val profile = pState.data
            if (profile == null) {
                CosmicBackdrop {
                    Scaffold(
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = strings.appTitle,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = ChampagneGold
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                                actions = {
                                    IconButton(
                                        onClick = { showLanguageDialog = true },
                                        modifier = Modifier.testTag("onboarding_language_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Language,
                                            contentDescription = strings.languageLabel,
                                            tint = ChampagneGold
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 560.dp)
                                    .fillMaxWidth()
                                    .height(172.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_zodiac_astrolabe),
                                    contentDescription = "Celestial zodiac astrolabe illustration",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, MidnightCosmos.copy(alpha = 0.85f))
                                            )
                                        )
                                )
                                Text(
                                    text = strings.celestialTagline,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = StarlightIvory,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                )
                            }

                            BirthDetailsEditorCard(
                                strings = strings,
                                initialName = "",
                                initialBirthDate = "1996-05-14",
                                initialBirthTime = "08:00",
                                initialBirthPlace = "",
                                initialFocusArea = ZodiacCalculator.focusAreas.first(),
                                isSaving = isSavingProfile,
                                submitLabel = strings.saveAndReveal,
                                onSubmit = { name, birthDate, birthTime, birthPlace, focusArea, manualMoonSign ->
                                    val canNotify = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                    viewModel.saveProfile(
                                        fullName = name,
                                        birthDate = birthDate,
                                        birthTime = birthTime,
                                        birthPlace = birthPlace,
                                        focusArea = focusArea,
                                        notificationsEnabled = canNotify,
                                        notificationHour = 8,
                                        notificationMinute = 0,
                                        manualMoonSign = manualMoonSign,
                                        onComplete = {
                                            if (canNotify) {
                                                NotificationHelper.scheduleDailyAffirmation(context, 8, 0)
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier.widthIn(max = 560.dp)
                            )
                        }
                    }
                }
            } else {
                MainAstrologyShell(
                    profile = profile,
                    viewModel = viewModel,
                    horoscopesState = horoscopesState,
                    affirmationsState = affirmationsState,
                    birthChartsState = birthChartsState,
                    compatibilitiesState = compatibilitiesState,
                    selectedPeriod = selectedPeriod,
                    horoscopeGenState = horoscopeGenState,
                    affirmationGenState = affirmationGenState,
                    birthChartGenState = birthChartGenState,
                    compatibilityGenState = compatibilityGenState,
                    isSavingProfile = isSavingProfile,
                    currentLanguage = currentLanguage,
                    strings = strings,
                    snackbarHostState = snackbarHostState,
                    showProfileDialog = showProfileDialog,
                    onOpenLanguageDialog = { showLanguageDialog = true },
                    onToggleProfileDialog = { showProfileDialog = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAstrologyShell(
    profile: UserAstrologyProfile,
    viewModel: AstrologyViewModel,
    horoscopesState: UiState<List<HoroscopeReading>>,
    affirmationsState: UiState<List<DailyAffirmation>>,
    birthChartsState: UiState<List<BirthChartReading>>,
    compatibilitiesState: UiState<List<CompatibilityReading>>,
    selectedPeriod: String,
    horoscopeGenState: UiState<HoroscopeReading>,
    affirmationGenState: UiState<DailyAffirmation>,
    birthChartGenState: UiState<BirthChartReading>,
    compatibilityGenState: UiState<CompatibilityReading>,
    isSavingProfile: Boolean,
    currentLanguage: AppLanguage,
    strings: AppStrings,
    snackbarHostState: SnackbarHostState,
    showProfileDialog: Boolean,
    onOpenLanguageDialog: () -> Unit,
    onToggleProfileDialog: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf(AstraTab.HOROSCOPE) }
    val signInfo = remember(profile.zodiacSign) { ZodiacCalculator.findByName(profile.zodiacSign) }

    BackHandler(enabled = currentTab != AstraTab.HOROSCOPE && !showProfileDialog) {
        currentTab = AstraTab.HOROSCOPE
    }

    if (showProfileDialog) {
        Dialog(
            onDismissRequest = { onToggleProfileDialog(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = MidnightCosmos,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .widthIn(max = 560.dp)
                    .border(1.dp, HairlineBorder, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.editDetails,
                            style = MaterialTheme.typography.titleLarge,
                            color = ChampagneGold
                        )
                        IconButton(onClick = { onToggleProfileDialog(false) }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = StarlightIvory
                            )
                        }
                    }

                    BirthDetailsEditorCard(
                        strings = strings,
                        initialName = profile.fullName,
                        initialBirthDate = profile.birthDate,
                        initialBirthTime = profile.birthTime,
                        initialBirthPlace = profile.birthPlace,
                        initialFocusArea = profile.focusArea,
                        initialManualMoonSign = if (profile.isMoonSignManual) profile.zodiacSign else null,
                        isSaving = isSavingProfile,
                        submitLabel = strings.saveAndReveal,
                        onSubmit = { name, birthDate, birthTime, birthPlace, focusArea, manualMoonSign ->
                            viewModel.saveProfile(
                                fullName = name,
                                birthDate = birthDate,
                                birthTime = birthTime,
                                birthPlace = birthPlace,
                                focusArea = focusArea,
                                notificationsEnabled = profile.notificationsEnabled,
                                notificationHour = profile.notificationHour.toInt(),
                                notificationMinute = profile.notificationMinute.toInt(),
                                manualMoonSign = manualMoonSign,
                                onComplete = { onToggleProfileDialog(false) }
                            )
                        }
                    )
                }
            }
        }
    }

    fun labelForTab(tab: AstraTab): String = when (tab) {
        AstraTab.HOROSCOPE -> strings.tabHoroscope
        AstraTab.BIRTH_CHART -> strings.tabBirthChart
        AstraTab.COMPATIBILITY -> strings.tabCompatibility
        AstraTab.MOON -> strings.tabMoon
        AstraTab.AFFIRMATIONS -> strings.tabAffirmations
    }

    CosmicBackdrop {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (useRail) {
                    NavigationRail(
                        containerColor = DeepIndigoSurface.copy(alpha = 0.96f),
                        contentColor = StarlightIvory,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AstraTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = labelForTab(tab)
                                    )
                                },
                                label = {
                                    Text(
                                        text = labelForTab(tab),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MidnightCosmos,
                                    selectedTextColor = ChampagneGold,
                                    indicatorColor = ChampagneGold,
                                    unselectedIconColor = MutedNebula,
                                    unselectedTextColor = MutedNebula
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MidnightCosmos.copy(alpha = 0.85f),
                                titleContentColor = StarlightIvory
                            ),
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = strings.appTitle,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = ChampagneGold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        color = CosmicSlate,
                                        shape = CircleShape,
                                        modifier = Modifier.border(
                                            1.dp,
                                            ChampagneGold.copy(alpha = 0.4f),
                                            CircleShape
                                        )
                                    ) {
                                        Text(
                                            text = "☽ ${signInfo.symbol} ${signInfo.sanskritName} (${signInfo.name})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = StarlightIvory,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            maxLines = 1
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = onOpenLanguageDialog,
                                    modifier = Modifier.testTag("open_language_dialog_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Language,
                                        contentDescription = strings.languageLabel,
                                        tint = ChampagneGold
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleProfileDialog(true) },
                                    modifier = Modifier.testTag("open_profile_dialog_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = strings.editDetails,
                                        tint = MoonlitLavender
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (!useRail) {
                            NavigationBar(
                                containerColor = DeepIndigoSurface.copy(alpha = 0.96f),
                                contentColor = StarlightIvory
                            ) {
                                AstraTab.entries.forEach { tab ->
                                    val selected = currentTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = labelForTab(tab)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = labelForTab(tab),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MidnightCosmos,
                                            selectedTextColor = ChampagneGold,
                                            indicatorColor = ChampagneGold,
                                            unselectedIconColor = MutedNebula,
                                            unselectedTextColor = MutedNebula
                                        ),
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    val contentModifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    when (currentTab) {
                        AstraTab.HOROSCOPE -> HoroscopeScreen(
                            profile = profile,
                            selectedPeriod = selectedPeriod,
                            horoscopesState = horoscopesState,
                            horoscopeGenState = horoscopeGenState,
                            currentDateKey = viewModel.currentDateKey(),
                            currentMonthKey = viewModel.currentMonthKey(),
                            currentLanguage = currentLanguage,
                            strings = strings,
                            onSelectPeriod = viewModel::selectPeriod,
                            onGenerateHoroscope = { period ->
                                viewModel.generateHoroscope(profile, period, currentLanguage)
                            },
                            modifier = contentModifier
                        )

                        AstraTab.BIRTH_CHART -> BirthChartScreen(
                            profile = profile,
                            birthChartsState = birthChartsState,
                            birthChartGenState = birthChartGenState,
                            strings = strings,
                            onGenerateBirthChart = { name, date, time, place ->
                                val manualMoon = if (profile.isMoonSignManual && date == profile.birthDate) {
                                    profile.zodiacSign
                                } else {
                                    null
                                }
                                viewModel.generateBirthChart(
                                    fullName = name,
                                    birthDate = date,
                                    birthTime = time,
                                    birthPlace = place,
                                    language = currentLanguage,
                                    manualMoonSign = manualMoon
                                )
                            },
                            modifier = contentModifier
                        )

                        AstraTab.COMPATIBILITY -> CompatibilityScreen(
                            profile = profile,
                            compatibilitiesState = compatibilitiesState,
                            compatibilityGenState = compatibilityGenState,
                            strings = strings,
                            onAnalyzeSigns = { s1, s2, rel ->
                                viewModel.analyzeCompatibility(s1, s2, rel, currentLanguage)
                            },
                            onAnalyzePerson = { partnerName, partnerDate, partnerTime, partnerPlace, rel ->
                                viewModel.analyzePersonCompatibility(
                                    profile = profile,
                                    partnerName = partnerName,
                                    partnerBirthDate = partnerDate,
                                    partnerBirthTime = partnerTime,
                                    partnerBirthPlace = partnerPlace,
                                    relationshipType = rel,
                                    language = currentLanguage
                                )
                            },
                            modifier = contentModifier
                        )

                        AstraTab.MOON -> LunarPhaseScreen(
                            profile = profile,
                            currentLanguage = currentLanguage,
                            strings = strings,
                            modifier = contentModifier
                        )

                        AstraTab.AFFIRMATIONS -> AffirmationsScreen(
                            profile = profile,
                            affirmationsState = affirmationsState,
                            affirmationGenState = affirmationGenState,
                            currentDateKey = viewModel.currentDateKey(),
                            strings = strings,
                            onGenerateAffirmation = { intention ->
                                viewModel.generateAffirmation(
                                    profile = profile,
                                    intention = intention,
                                    language = currentLanguage,
                                    onGenerated = { aff ->
                                        NotificationHelper.cacheAffirmationPayload(
                                            context = context,
                                            affirmationText = aff.text,
                                            userName = profile.fullName,
                                            zodiacSign = profile.zodiacSign
                                        )
                                    }
                                )
                            },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onDeleteAffirmation = viewModel::deleteAffirmation,
                            onUpdateNotificationPrefs = { enabled, hour, minute ->
                                viewModel.updateNotificationPrefs(profile, enabled, hour, minute)
                            },
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }
}
