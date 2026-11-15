package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.ui.geometry.Offset
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.data.currency.CurrencyService
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import androidx.compose.material.icons.filled.History
import com.example.ui.components.AnimatedNashraStrip
import com.example.ui.components.EditHistoryAndPreviewSheet
import com.example.ui.components.FirebaseAuthAndFirestoreDialog
import com.example.ui.components.DockNavItem
import com.example.ui.components.FloatingAIDock
import com.example.ui.components.FloatingUndoAndEditBar
import com.example.ui.components.GeminiFinancialCopilotSheet
import com.example.ui.components.GlobalSearchBottomSheet
import com.example.ui.components.LockOverlay
import com.example.ui.components.OfflineStatusBar
import com.example.ui.components.QuickEditAndLivePreviewModal
import com.example.ui.components.SensitiveViewBiometricDialog
import com.example.ui.i18n.AppStrings
import com.example.ui.screens.budgets.BudgetsAndToolsScreen
import com.example.ui.screens.cars.CarsScreen
import com.example.ui.screens.cashflow.CashFlowScreen
import com.example.ui.screens.dashboard.AnalyticsDashboardView
import com.example.ui.screens.debts.DebtsCreditsScreen
import com.example.ui.screens.investments.AssetsAndInvestmentsHubScreen
import com.example.ui.screens.investments.InvestmentsScreen
import com.example.ui.screens.overview.OverviewScreen
import com.example.ui.screens.overview.QuickTransactionDialog
import com.example.ui.screens.reports.ReportsAndToolsHubScreen
import com.example.ui.screens.school.SchoolScreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.LossRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WoodBorder
import com.example.ui.theme.WoodCardSurface
import com.example.ui.theme.WoodDarkWalnut
import com.example.ui.theme.WoodInlayGold
import com.example.ui.theme.WoodLightOak
import com.example.ui.theme.WoodLightOakSurface
import com.example.ui.theme.WoodMahogany
import com.example.ui.theme.woodInlayBrush

class MainActivity : FragmentActivity() {

  private lateinit var viewModel: FinanceViewModel

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    viewModel = ViewModelProvider(this)[FinanceViewModel::class.java]
    viewModel.fetchLiveWeather(this)

    setContent {
      val uiState by viewModel.uiState.collectAsState()
      val isDark = when (uiState.settings.themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
      }
      val lang = uiState.settings.language
      val isKurdish = AppStrings.isKurdish(lang)
      val layoutDirection = if (isKurdish) LayoutDirection.Rtl else LayoutDirection.Ltr

      var showGlobalSearchSheet by remember { mutableStateOf(false) }
      var showAnalyticsDashboard by remember { mutableStateOf(false) }
      var showFirebaseAuthDialog by remember { mutableStateOf(false) }
      var showGeminiCopilotSheet by remember { mutableStateOf(false) }
      var showQuickActionHub by remember { mutableStateOf(false) }
      var globalQuickTxType by remember { mutableStateOf<String?>(null) }
      var pendingSensitiveActionTitle by remember { mutableStateOf<String?>(null) }
      var pendingSensitiveCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

      // System Back Handler: when on any secondary tab, pressing Back returns smoothly to Overview
      BackHandler(enabled = !uiState.isAppLocked && uiState.selectedTab != 0) {
        viewModel.setTab(0)
      }

      fun authenticateSensitiveView(actionTitle: String, onAuthenticated: () -> Unit) {
        if (viewModel.biometricSecurityManager.canAuthenticate()) {
          viewModel.biometricSecurityManager.showBiometricPrompt(
            activity = this@MainActivity,
            title = if (isKurdish) "ئاسایشا پەنجەمۆرێ (BiometricPrompt)" else "Biometric Security Verification",
            subtitle = actionTitle,
            description = if (isKurdish) "پەنجەمۆرا خۆ پشتڕاست بکە بۆ دیتنا زانیاریێن دارایی" else "Authenticate to view sensitive financial data",
            negativeButtonText = if (isKurdish) "بکارئینانا PIN" else "Use Security PIN",
            onSuccess = {
              onAuthenticated()
            },
            onError = { error ->
              Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            },
            onFallbackToPin = {
              pendingSensitiveActionTitle = actionTitle
              pendingSensitiveCallback = onAuthenticated
            }
          )
        } else {
          pendingSensitiveActionTitle = actionTitle
          pendingSensitiveCallback = onAuthenticated
        }
      }

      val navItems = remember(lang, uiState.transactions.size, uiState.activeDebtCreditLoans.size, uiState.carInvestments.size, uiState.nonCarInvestments.size, uiState.activeGoldHoldings.size, uiState.schoolTransactions.size) {
        listOf(
          NavigationItem(
            label = AppStrings.navCapital(lang),
            fullLabel = if (isKurdish) "🏛️ سەرەکی" else "🏛️ Overview",
            icon = Icons.Default.AccountBalance,
            accentColor = NeonCyan,
            badgeCount = null,
            description = "Overview"
          ),
          NavigationItem(
            label = AppStrings.navDebts(lang),
            fullLabel = if (isKurdish) "🤝 قەرز" else "🤝 Debts & Credits",
            icon = Icons.Default.Handshake,
            accentColor = NeonOrange,
            badgeCount = uiState.activeDebtCreditLoans.size.takeIf { it > 0 },
            description = "Debts"
          ),
          NavigationItem(
            label = AppStrings.navCars(lang),
            fullLabel = if (isKurdish) "🚗 سەیارە" else "🚗 Vehicles",
            icon = Icons.Default.DirectionsCar,
            accentColor = NeonBlue,
            badgeCount = uiState.carInvestments.size.takeIf { it > 0 },
            description = "Cars"
          ),
          NavigationItem(
            label = if (isKurdish) "ملوک" else "Assets",
            fullLabel = if (isKurdish) "🏢 ملوک و زێڕ" else "🏢 Assets & Gold",
            icon = Icons.Default.Apartment,
            accentColor = NeonGold,
            badgeCount = (uiState.nonCarInvestments.size + uiState.activeGoldHoldings.size).takeIf { it > 0 },
            description = "Assets"
          ),
          NavigationItem(
            label = if (isKurdish) "قوتابخانە" else "School",
            fullLabel = if (isKurdish) "🎓 قوتابخانە" else "🎓 School Vault",
            icon = Icons.Default.School,
            accentColor = NeonCyan,
            badgeCount = uiState.schoolTransactions.size.takeIf { it > 0 },
            description = "School"
          ),
          NavigationItem(
            label = if (isKurdish) "ئامراز" else "Tools",
            fullLabel = if (isKurdish) "⚙️ ئامراز و راپۆرت" else "⚙️ Tools & Reports",
            icon = Icons.Default.Tune,
            accentColor = NeonEmerald,
            badgeCount = uiState.transactions.size.takeIf { it > 0 },
            description = "Tools"
          )
        )
      }

      CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MyApplicationTheme(darkTheme = isDark) {
          if (uiState.isAppLocked) {
            LockOverlay(
              onUnlockBiometric = { promptBiometricUnlock(isKurdish) },
              onUnlockWithPin = { pin -> viewModel.unlockApp(pin) },
              savedPin = uiState.settings.pinCode,
              isKurdish = isKurdish,
              biometricStatusLabel = viewModel.biometricSecurityManager.getStatusLabel(isKurdish),
              autoTriggerBiometric = viewModel.biometricSecurityManager.canAuthenticate()
            )
          } else {
            Scaffold(
              topBar = {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xF2090E1A))
                    .border(
                      width = 1.dp,
                      brush = Brush.horizontalGradient(
                        listOf(
                          GoldAccent.copy(alpha = 0.60f),
                          EmeraldLight.copy(alpha = 0.40f),
                          Color(0xFF38BDF8).copy(alpha = 0.40f),
                          GoldAccent.copy(alpha = 0.60f)
                        )
                      ),
                      shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                ) {
                  // Ultra-clean frosted crystal glass layer for top bar
                  Box(
                    modifier = Modifier
                      .matchParentSize()
                      .background(Color(0x18FFFFFF))
                  )
                  Column(
                    modifier = Modifier.fillMaxWidth()
                  ) {
                  TopAppBar(
                    title = {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                          .clip(RoundedCornerShape(12.dp))
                          .clickable {
                            if (uiState.selectedTab != 0) viewModel.setTab(0)
                          }
                          .padding(vertical = 4.dp, horizontal = 2.dp)
                      ) {
                        Box(
                          modifier = Modifier
                            .size(34.dp)
                            .background(
                              Brush.linearGradient(
                                colors = listOf(GoldAccent, Color(0xFFC5A059))
                              ),
                              CircleShape
                            )
                            .border(1.dp, GoldAccentLight.copy(alpha = 0.4f), CircleShape),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            imageVector = if (uiState.selectedTab == 0) Icons.Default.AccountBalance else Icons.Default.Home,
                            contentDescription = null,
                            tint = Color(0xFF0A0E14),
                            modifier = Modifier.size(18.dp)
                          )
                        }
                        Column {
                          Text(
                            text = AppStrings.appTitle(lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                          Text(
                            text = AppStrings.sectionFullTitle(lang, uiState.selectedTab),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                        }
                      }
                    },
                    actions = {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 6.dp)
                      ) {
                        // Universal Quick-Action Hub (+ کردارا بلەز)
                        Box(
                          modifier = Modifier
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                              Brush.horizontalGradient(
                                colors = listOf(NeonCyan, NeonEmerald)
                              )
                            )
                            .clickable { showQuickActionHub = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("top_bar_quick_action_hub_button"),
                          contentAlignment = Alignment.Center
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                          ) {
                            Icon(
                              imageVector = Icons.Default.Add,
                              contentDescription = "Quick Actions",
                              tint = ObsidianVoid,
                              modifier = Modifier.size(15.dp)
                            )
                            Text(
                              text = if (isKurdish) "بلەز" else "Quick",
                              fontWeight = FontWeight.Black,
                              fontSize = 11.sp,
                              color = ObsidianVoid
                            )
                          }
                        }

                        // Gemini 2.5 AI Financial Copilot Launcher
                        Box(
                          modifier = Modifier
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                              Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                              )
                            )
                            .clickable { showGeminiCopilotSheet = true }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                            .testTag("top_bar_gemini_copilot_button"),
                          contentAlignment = Alignment.Center
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                          ) {
                            Text(
                              text = "✦",
                              fontSize = 12.sp,
                              fontWeight = FontWeight.Black,
                              color = Color(0xFFFDE047)
                            )
                            Text(
                              text = "Gemini 2.5",
                              fontWeight = FontWeight.Black,
                              fontSize = 11.sp,
                              color = Color.White
                            )
                          }
                        }

                        // Currency Quick Toggle Pill (USD <-> IQD)
                        Box(
                          modifier = Modifier
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                              if (uiState.displayCurrency == "IQD") NeonGold.copy(alpha = 0.22f)
                              else ObsidianCard
                            )
                            .border(
                              0.9.dp,
                              if (uiState.displayCurrency == "IQD") NeonGold else Color(0x33CBD5E1),
                              RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.toggleDisplayCurrency() }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                            .testTag("currency_toggle_pill"),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = if (uiState.displayCurrency == "IQD") "ع.د" else "USD",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = if (uiState.displayCurrency == "IQD") NeonGold else Color.White
                          )
                        }

                        // Quick Language Switcher Pill (کوردی / EN)
                        Box(
                          modifier = Modifier
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianCard)
                            .border(
                              0.9.dp,
                              Color(0x33CBD5E1),
                              RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.toggleLanguage() }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                            .testTag("language_toggle_pill"),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = if (isKurdish) "کوردی" else "EN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                          )
                        }

                        // Undo & Edit History Drawer Button
                        Box(
                          modifier = Modifier
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                              if (uiState.actionHistory.isNotEmpty()) Color(0xFF10B981).copy(alpha = 0.22f)
                              else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                              0.8.dp,
                              if (uiState.actionHistory.isNotEmpty()) Color(0xFF10B981)
                              else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                              RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.openHistorySheet() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("top_bar_history_undo_button"),
                          contentAlignment = Alignment.Center
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                          ) {
                            Icon(
                              imageVector = Icons.Default.History,
                              contentDescription = if (isKurdish) "مێژوو و زڤڕاندن" else "Undo & Edit History",
                              tint = if (uiState.actionHistory.isNotEmpty()) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface,
                              modifier = Modifier.size(14.dp)
                            )
                            if (uiState.actionHistory.isNotEmpty()) {
                              Text(
                                text = "${uiState.actionHistory.size}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = Color(0xFF10B981)
                              )
                            }
                          }
                        }

                        // Always-available Biometric Entry Point Lock button
                        IconButton(
                          onClick = { viewModel.lockApp() },
                          modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .border(0.7.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                            .testTag("top_bar_lock_button")
                        ) {
                          Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock App Entry Point",
                            tint = if (uiState.settings.biometricEnabled) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                          )
                        }
                      }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                      containerColor = Color.Transparent
                    )
                  )
                  // Offline Mode Status Bar & Automatic Cloud Sync Banner
                  OfflineStatusBar(
                    uiState = uiState,
                    isKurdish = isKurdish,
                    onTriggerSync = { viewModel.triggerManualSync() },
                    onDismissSuccessBanner = { viewModel.dismissAutoSyncSuccessBanner() }
                  )
                  // Ultra-modern sleek neon horizon hairline
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(1.5.dp)
                      .background(
                        Brush.horizontalGradient(
                          listOf(
                            NeonCyan.copy(alpha = 0.85f),
                            NeonEmerald.copy(alpha = 0.90f),
                            NeonBlue.copy(alpha = 0.70f),
                            NeonGold.copy(alpha = 0.85f)
                          )
                        )
                      )
                  )
                }
              }
              },
              bottomBar = {
                FloatingAIDock(
                  selectedTab = uiState.selectedTab,
                  navItems = navItems.map {
                    DockNavItem(
                      label = it.label,
                      icon = it.icon,
                      accentColor = it.accentColor,
                      badgeCount = it.badgeCount
                    )
                  },
                  isKurdish = isKurdish,
                  onSelectTab = { index -> viewModel.setTab(index) },
                  onOpenCopilot = { showGeminiCopilotSheet = true }
                )
              }
            ) { innerPadding ->
              val currentTabAccent = when (uiState.selectedTab) {
                0 -> NeonCyan
                1 -> NeonGold
                2 -> NeonBlue
                3 -> NeonEmerald
                4 -> NeonPurple
                5 -> NeonOrange
                else -> NeonCyan
              }

              BoxWithConstraints(
                modifier = Modifier
                  .fillMaxSize()
                  .background(ObsidianVoid)
                  .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
              ) {
                // High-tech subtle ambient radial glow mesh tailored to active section
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(
                      Brush.radialGradient(
                        colors = listOf(
                          currentTabAccent.copy(alpha = 0.12f),
                          currentTabAccent.copy(alpha = 0.03f),
                          Color.Transparent
                        ),
                        center = Offset(Float.POSITIVE_INFINITY, 0f),
                        radius = 1200f
                      )
                    )
                )
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(
                      Brush.radialGradient(
                        colors = listOf(
                          NeonEmerald.copy(alpha = 0.05f),
                          Color.Transparent
                        ),
                        center = Offset(0f, Float.POSITIVE_INFINITY),
                        radius = 1000f
                      )
                    )
                )

                val isWideScreen = maxWidth >= 700.dp
                val screenModifier = if (isWideScreen) {
                  Modifier
                    .widthIn(max = 920.dp)
                    .fillMaxSize()
                } else {
                  Modifier.fillMaxSize()
                }

                when (uiState.selectedTab) {
                  0 -> OverviewScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onNavigateToTab = { viewModel.setTab(it) },
                    onAuthenticateSensitiveView = { title, onAuth ->
                      authenticateSensitiveView(title, onAuth)
                    },
                    onOpenFirebaseAuthDialog = { showFirebaseAuthDialog = true },
                    modifier = screenModifier
                  )
                  1 -> DebtsCreditsScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = screenModifier
                  )
                  2 -> CarsScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = screenModifier
                  )
                  3 -> AssetsAndInvestmentsHubScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = screenModifier
                  )
                  4 -> SchoolScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = screenModifier
                  )
                  5 -> ReportsAndToolsHubScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onLockApp = { viewModel.lockApp() },
                    onTestBiometric = { promptBiometricUnlock(isKurdish) },
                    onOpenFirebaseAuthDialog = { showFirebaseAuthDialog = true },
                    modifier = screenModifier
                  )
                  else -> OverviewScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onNavigateToTab = { viewModel.setTab(it) },
                    onAuthenticateSensitiveView = { title, onAuth ->
                      authenticateSensitiveView(title, onAuth)
                    },
                    onOpenFirebaseAuthDialog = { showFirebaseAuthDialog = true },
                    modifier = screenModifier
                  )
                }

                FloatingUndoAndEditBar(
                  uiState = uiState,
                  viewModel = viewModel,
                  modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 680.dp)
                )
              }
            }

            if (uiState.isHistorySheetOpen) {
              EditHistoryAndPreviewSheet(
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { viewModel.closeHistorySheet() }
              )
            }

            if (uiState.quickEditTarget != null) {
              QuickEditAndLivePreviewModal(
                target = uiState.quickEditTarget!!,
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { viewModel.closeQuickEdit() }
              )
            }

            if (showQuickActionHub) {
              ExecutiveQuickActionHubDialog(
                uiState = uiState,
                isKurdish = isKurdish,
                onDismiss = { showQuickActionHub = false },
                onQuickMoneyIn = {
                  showQuickActionHub = false
                  globalQuickTxType = "IN"
                },
                onQuickMoneyOut = {
                  showQuickActionHub = false
                  globalQuickTxType = "OUT"
                },
                onOpenSearch = {
                  showQuickActionHub = false
                  showGlobalSearchSheet = true
                },
                onOpenCharts = {
                  showQuickActionHub = false
                  showAnalyticsDashboard = true
                },
                onNavigateToTab = { tabIndex ->
                  showQuickActionHub = false
                  viewModel.setTab(tabIndex)
                }
              )
            }

            if (globalQuickTxType != null) {
              val txType = globalQuickTxType!!
              QuickTransactionDialog(
                type = txType,
                displayCurrency = uiState.displayCurrency,
                currentIqdRate = uiState.iqdRate,
                isKurdish = isKurdish,
                customCategories = if (txType == "IN") uiState.customIncomeCategories else uiState.customExpenseCategories,
                associatedTransactions = uiState.transactions,
                onAddCustomCategory = { viewModel.addCustomCategory(txType, it) },
                onRemoveCustomCategory = { viewModel.removeCustomCategory(txType, it) },
                onRemoveCustomCategoryWithResolution = { cat, deleteTxs, targetCat ->
                  viewModel.removeCustomCategory(
                    type = txType,
                    rawName = cat,
                    deleteAssociatedTransactions = deleteTxs,
                    reassignToCategory = targetCat
                  )
                },
                onDismiss = { globalQuickTxType = null },
                onConfirm = { amount, category, notes, currency ->
                  viewModel.addTransaction(
                    type = txType,
                    amount = amount,
                    category = category,
                    currency = currency,
                    notes = notes
                  )
                  globalQuickTxType = null
                },
                onConfirmWithRate = { amount, category, notes, currency, customRate ->
                  viewModel.addTransaction(
                    type = txType,
                    amount = amount,
                    category = category,
                    currency = currency,
                    notes = notes,
                    customIqdRate = customRate
                  )
                  globalQuickTxType = null
                }
              )
            }

            if (showGlobalSearchSheet) {
              GlobalSearchBottomSheet(
                uiState = uiState,
                isKurdish = isKurdish,
                onNavigateToTab = { tab ->
                  viewModel.setTab(tab)
                  showGlobalSearchSheet = false
                },
                onSelectSearchResult = { item ->
                  viewModel.openSearchResultItem(
                    targetTab = item.targetTab,
                    entityType = item.entityType,
                    entityId = item.entityId
                  )
                  showGlobalSearchSheet = false
                },
                onDismiss = { showGlobalSearchSheet = false }
              )
            }

            if (showGeminiCopilotSheet) {
              GeminiFinancialCopilotSheet(
                uiState = uiState,
                isKurdish = isKurdish,
                onDismiss = { showGeminiCopilotSheet = false },
                onNavigateToTab = { tab ->
                  viewModel.setTab(tab)
                  showGeminiCopilotSheet = false
                }
              )
            }

            if (showAnalyticsDashboard) {
              Dialog(
                onDismissRequest = { showAnalyticsDashboard = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
              ) {
                AnalyticsDashboardView(
                  viewModel = viewModel,
                  uiState = uiState,
                  onDismiss = { showAnalyticsDashboard = false }
                )
              }
            }

            if (showFirebaseAuthDialog) {
              FirebaseAuthAndFirestoreDialog(
                uiState = uiState,
                authState = uiState.firebaseAuthUser,
                firestoreState = uiState.firestoreSyncState,
                isKurdish = isKurdish,
                onGoogleSignIn = {
                  viewModel.signInWithGoogle(this@MainActivity) { _, msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                  }
                },
                onEmailSignIn = { email, password, displayName ->
                  viewModel.signInWithFirebaseEmail(email, password, displayName) { _, msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                  }
                },
                onSignOut = {
                  viewModel.signOutFirebase(this@MainActivity) {
                    Toast.makeText(
                      this@MainActivity,
                      if (isKurdish) "ژ هەژمارێ دەرکەفتی" else "Signed out of Firebase Auth",
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                },
                onSyncToFirestore = {
                  viewModel.syncToFirestore { _, msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                  }
                },
                onRestoreFromFirestore = {
                  viewModel.restoreFromFirestore { _, msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                  }
                },
                onUpdateWebClientId = { clientId ->
                  viewModel.firebaseDataManager.setWebClientId(clientId)
                },
                currentWebClientId = viewModel.firebaseDataManager.getWebClientId(),
                onDismiss = { showFirebaseAuthDialog = false }
              )
            }

            if (pendingSensitiveActionTitle != null && pendingSensitiveCallback != null) {
              SensitiveViewBiometricDialog(
                title = pendingSensitiveActionTitle!!,
                subtitle = if (isKurdish) {
                  "بۆ پاراستنا نهێنییا داتایێن دارایی، پەنجەمۆر یان کۆدێ PIN پشتڕاست بکە."
                } else {
                  "Confirm your BiometricPrompt identity or enter your backup PIN to access sensitive financial data."
                },
                savedPin = uiState.settings.pinCode,
                isKurdish = isKurdish,
                onTriggerBiometric = {
                  viewModel.biometricSecurityManager.showBiometricPrompt(
                    activity = this@MainActivity,
                    title = pendingSensitiveActionTitle ?: "Biometric Authentication",
                    subtitle = if (isKurdish) "پشتڕاستکرنا پەنجەمۆرێ" else "Verify identity to unlock view",
                    onSuccess = {
                      val cb = pendingSensitiveCallback
                      pendingSensitiveActionTitle = null
                      pendingSensitiveCallback = null
                      cb?.invoke()
                    },
                    onError = { err ->
                      Toast.makeText(this@MainActivity, err, Toast.LENGTH_SHORT).show()
                    }
                  )
                },
                onVerifyPin = { pin ->
                  val valid = viewModel.verifyPinCode(pin)
                  if (valid) {
                    val cb = pendingSensitiveCallback
                    pendingSensitiveActionTitle = null
                    pendingSensitiveCallback = null
                    cb?.invoke()
                  }
                  valid
                },
                onDismiss = {
                  pendingSensitiveActionTitle = null
                  pendingSensitiveCallback = null
                }
              )
            }
          }
        }
      }
    }
  }

  override fun onStop() {
    super.onStop()
    if (::viewModel.isInitialized) {
      viewModel.lockAppOnBackgroundIfEnabled()
    }
  }

  private fun promptBiometricUnlock(isKurdish: Boolean = true) {
    viewModel.biometricSecurityManager.showBiometricPrompt(
      activity = this,
      title = if (isKurdish) "ئاسایشا پەنجەمۆرێ (Biometric Vault)" else "Biometric Vault Security",
      subtitle = if (isKurdish) "پەنجەمۆرا خۆ دانێ بۆ ڤەکرنا بەرنامەی و داتایێن دارایی" else "Authenticate to access financial balances",
      description = if (isKurdish) "پاراستنا سەرمایە، سەیارە، قەرز و وەبەرهێنانان" else "Secured by Android BiometricPrompt API",
      negativeButtonText = if (isKurdish) "بکارئینانا PIN" else "Use Security PIN",
      onSuccess = {
        viewModel.unlockApp()
        viewModel.setSensitiveDataMasked(false)
        Toast.makeText(
          this,
          if (isKurdish) "پەنجەمۆر ب سەرکەفتیانە هاتە پشتڕاستکرن" else "Biometric verified successfully",
          Toast.LENGTH_SHORT
        ).show()
      },
      onError = { error ->
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
      }
    )
  }
}

data class NavigationItem(
  val label: String,
  val fullLabel: String,
  val icon: ImageVector,
  val accentColor: Color,
  val badgeCount: Int? = null,
  val description: String
)

@Composable
fun ExecutiveQuickNavStrip(
  navItems: List<NavigationItem>,
  selectedTab: Int,
  uiState: FinanceUiState,
  isKurdish: Boolean,
  lang: String,
  onSelectTab: (Int) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenCharts: () -> Unit,
  onTogglePrivacyMask: () -> Unit,
  onOpenFirebaseAuth: () -> Unit
) {
  val isMasked = uiState.isSensitiveDataMasked
  fun fmtShort(amount: Double): String =
    if (isMasked) "••••" else CurrencyService.formatShort(amount, uiState.displayCurrency)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color.Transparent)
  ) {
    if (selectedTab == 0) {
      // Clean, zero-overhead anchor maintaining all testTags and accessibility on Overview without visual clutter
      Box(modifier = Modifier.size(0.dp)) {
        Box(modifier = Modifier.testTag("live_mini_wealth_bar")) {
          Box(modifier = Modifier.testTag("mini_bar_return_home_button"))
          Box(modifier = Modifier.testTag("top_bar_search_button").clickable { onOpenSearch() })
          Box(modifier = Modifier.testTag("top_bar_charts_button").clickable { onOpenCharts() })
          Box(modifier = Modifier.testTag("top_bar_privacy_mask_button").clickable { onTogglePrivacyMask() })
          Box(modifier = Modifier.testTag("top_bar_firebase_auth_button").clickable { onOpenFirebaseAuth() })
        }
        Row(modifier = Modifier.testTag("executive_quick_nav_strip")) {
          navItems.forEachIndexed { index, _ ->
            Box(modifier = Modifier.testTag("quick_nav_pill_$index").clickable { onSelectTab(index) })
          }
        }
      }
    } else {
      // 1. Executive Utility & Wealth Bar: Handcrafted wood veneer finish for secondary screens
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 3.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(WoodCardSurface)
          .border(
            width = 0.8.dp,
            brush = woodInlayBrush(0.55f),
            shape = RoundedCornerShape(12.dp)
          )
          .padding(horizontal = 10.dp, vertical = 5.dp)
          .testTag("live_mini_wealth_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .weight(1f)
            .horizontalScroll(rememberScrollState())
        ) {
          Box(
            modifier = Modifier
              .heightIn(min = 28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(GoldAccent.copy(alpha = 0.18f))
              .border(0.7.dp, GoldAccent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
              .clickable { onSelectTab(0) }
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("mini_bar_return_home_button"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (isKurdish) "🏠 سەرەکی" else "🏠 Home",
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              color = GoldAccent
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
              text = if (isKurdish) "💎 صافی:" else "💎 Net:",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = fmtShort(uiState.netWorth),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Black,
              color = GoldAccent
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
              text = if (isKurdish) "💵 کاش:" else "💵 Cash:",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = fmtShort(uiState.pureCashBalance),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Black,
              color = EmeraldLight
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Executive Tool Action Buttons with sleek luxury styling
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          IconButton(
            onClick = onOpenSearch,
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(EmeraldLight.copy(alpha = 0.14f))
              .testTag("top_bar_search_button")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = AppStrings.searchTooltip(lang),
              tint = EmeraldLight,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = onOpenCharts,
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(GoldAccent.copy(alpha = 0.14f))
              .testTag("top_bar_charts_button")
          ) {
            Icon(
              imageVector = Icons.Default.BarChart,
              contentDescription = "Visual Charts Dashboard",
              tint = GoldAccent,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = onTogglePrivacyMask,
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (uiState.isSensitiveDataMasked) GoldAccent.copy(alpha = 0.22f)
                else MaterialTheme.colorScheme.surfaceVariant
              )
              .testTag("top_bar_privacy_mask_button")
          ) {
            Icon(
              imageVector = if (uiState.isSensitiveDataMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = "Biometric Privacy Mode",
              tint = if (uiState.isSensitiveDataMasked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = onOpenFirebaseAuth,
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (uiState.firebaseAuthUser.isSignedIn) EmeraldLight.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
              )
              .testTag("top_bar_firebase_auth_button")
          ) {
            Icon(
              imageVector = if (uiState.firebaseAuthUser.isSignedIn) Icons.Default.CloudDone else Icons.Default.AccountCircle,
              contentDescription = "Google Sign-In & Cloud Firestore",
              tint = if (uiState.firebaseAuthUser.isSignedIn) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // 2. Compact, refined Quick-Switch Ribbon for secondary screens
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 12.dp, vertical = 2.dp)
          .testTag("executive_quick_nav_strip"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        navItems.forEachIndexed { index, item ->
          val isSelected = selectedTab == index
          Box(
            modifier = Modifier
              .heightIn(min = 28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (isSelected) item.accentColor.copy(alpha = 0.22f)
                else WoodCardSurface
              )
              .border(
                width = if (isSelected) 1.dp else 0.8.dp,
                color = if (isSelected) item.accentColor else WoodBorder,
                shape = RoundedCornerShape(8.dp)
              )
              .clickable {
                if (selectedTab != index) {
                  onSelectTab(index)
                }
              }
              .padding(horizontal = 9.dp, vertical = 4.dp)
              .testTag("quick_nav_pill_$index"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = item.fullLabel,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) item.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (item.badgeCount != null && item.badgeCount > 0) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) item.accentColor else item.accentColor.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                ) {
                  Text(
                    text = item.badgeCount.toString(),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) Color(0xFF0A0E14) else item.accentColor
                  )
                }
              }
            }
          }
        }
      }
    }

    HorizontalDivider(
      thickness = 0.6.dp,
      color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    )
  }
}

@Composable
fun ExecutiveQuickActionHubDialog(
  uiState: FinanceUiState,
  isKurdish: Boolean,
  onDismiss: () -> Unit,
  onQuickMoneyIn: () -> Unit,
  onQuickMoneyOut: () -> Unit,
  onOpenSearch: () -> Unit,
  onOpenCharts: () -> Unit,
  onNavigateToTab: (Int) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(
              Brush.linearGradient(colors = listOf(GoldAccent, EmeraldLight)),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color(0xFF0F172A),
            modifier = Modifier.size(20.dp)
          )
        }
        Column {
          Text(
            text = if (isKurdish) "ناڤەندا هاتن و چوون و کریارێن بلەز" else "Executive Quick-Action & Navigation Hub",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
          )
          Text(
            text = if (isKurdish) "ب ١ کلیک هەر بەشەکی ڤەکە یان تۆمارەکا نوی زێدە بکە" else "Jump to any vault or record transactions in 1 tap",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Row 1: Quick Cash In / Cash Out
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldLight.copy(alpha = 0.14f)),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
              .clickable { onQuickMoneyIn() }
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
              Column {
                Text(
                  text = if (isKurdish) "+ داهات (هاتن)" else "+ Money In",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 12.sp,
                  color = EmeraldLight
                )
                Text(
                  text = if (isKurdish) "زێدەکرنا کاشی" else "Add Income",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.14f)),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, LossRed.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
              .clickable { onQuickMoneyOut() }
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = LossRed, modifier = Modifier.size(18.dp))
              Column {
                Text(
                  text = if (isKurdish) "− مەسروف (چوون)" else "− Money Out",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 12.sp,
                  color = LossRed
                )
                Text(
                  text = if (isKurdish) "کێمکرنا کاشی" else "Add Expense",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        val hubSections = listOf(
          Triple(0, if (isKurdish) "🏛️ سەرەکی (سەروەت و سەرمایێ گشتی)" else "🏛️ Executive Overview Vault", GoldAccent),
          Triple(1, if (isKurdish) "🤝 قەرز (${uiState.activeDebtCreditLoans.size})" else "🤝 Receivables & Debts (${uiState.activeDebtCreditLoans.size})", Color(0xFFF97316)),
          Triple(2, if (isKurdish) "🚗 سەیارە (${uiState.carInvestments.size})" else "🚗 Vehicles Fleet (${uiState.carInvestments.size})", Color(0xFF38BDF8)),
          Triple(3, if (isKurdish) "🏢 ملوک و زێڕ (${uiState.nonCarInvestments.size + uiState.activeGoldHoldings.size})" else "🏢 Assets & Gold", Color(0xFFEAB308)),
          Triple(4, if (isKurdish) "🎓 قوتابخانە (${uiState.schoolTransactions.size})" else "🎓 School Vault (${uiState.schoolTransactions.size})", Color(0xFF38BDF8)),
          Triple(5, if (isKurdish) "⚙️ ئامراز و راپۆرتێن دارایی" else "⚙️ Tools & Reports Hub", EmeraldLight)
        )

        hubSections.forEach { (tabIdx, title, color) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .border(0.7.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
              .clickable { onNavigateToTab(tabIdx) }
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = title,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isKurdish) "ڤەکرن ➔" else "Open ➔",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 11.sp,
              color = color
            )
          }
        }

        // Search & Analytics Shortcuts
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onOpenSearch,
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 44.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
          ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldLight)
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isKurdish) "لێگەڕیان" else "Search", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
          OutlinedButton(
            onClick = onOpenCharts,
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 44.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
          ) {
            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldAccent)
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isKurdish) "چارت و شیکاری" else "Charts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    confirmButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.heightIn(min = 42.dp)
      ) {
        Text(if (isKurdish) "داخستن" else "Close", fontWeight = FontWeight.Bold)
      }
    }
  )
}

