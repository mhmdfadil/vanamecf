package com.example.cfvaname.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.cfvaname.data.UserSession
import com.example.cfvaname.navigation.Screen
import com.example.cfvaname.navigation.SidebarMenuItem
import com.example.cfvaname.navigation.getSidebarMenuItems
import com.example.cfvaname.ui.theme.*
import com.example.cfvaname.ui.localization.stringResource
import com.example.cfvaname.ui.localization.AppStrings
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

// ════════════════════════════════════════════════════════════════
//  SKY BLUE PREMIUM COLOR PALETTE
// ════════════════════════════════════════════════════════════════
private val SkyBlueLight = Color(0xFF87CEEB)
private val SkyBlueMedium = Color(0xFF4AADE8)
private val SkyBlueDark = Color(0xFF2196F3)
private val SkyBlueDeep = Color(0xFF1976D2)
private val SkyBlueAccent = Color(0xFF03A9F4)

// Light mode navbar
private val NavbarStartLight = Color(0xFF1565C0)
private val NavbarEndLight = Color(0xFF42A5F5)

// Dark mode navbar - lebih gelap & subtle
private val NavbarStartDark = Color(0xFF0D47A1)
private val NavbarEndDark = Color(0xFF1565C0)

// Sidebar gradient
private val SidebarGradientStartLight = Color(0xFF2196F3)
private val SidebarGradientEndLight = Color(0xFF64B5F6)
private val SidebarGradientStartDark = Color(0xFF0D47A1)
private val SidebarGradientEndDark = Color(0xFF1565C0)

// Bottom nav selected
private val BottomNavSelectedLight = Color(0xFF1E88E5)
private val BottomNavSelectedDark = Color(0xFF64B5F6)

// ════════════════════════════════════════════════════════════════
//  FILE-LEVEL DATA CLASS FOR BOTTOM NAV
// ════════════════════════════════════════════════════════════════
data class BottomNavItem(
    val icon: ImageVector,
    val iconOutlined: ImageVector,
    val labelRes: AppStrings,
    val route: String
)

/**
 * Layout utama yang dipakai oleh semua Activity setelah login.
 * SUDAH DILENGKAPI dengan Pull-to-Refresh!
 */
@Composable
fun AppScaffold(
    currentRoute: String,
    userSession: UserSession?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    title: String = "Vename",
    enablePullRefresh: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    var isSidebarOpen by remember { mutableStateOf(false) }
    val sidebarWidth = 300.dp
    val isDark = isSystemInDarkTheme()

    val sidebarOffset by animateDpAsState(
        targetValue = if (isSidebarOpen) 0.dp else (-sidebarWidth),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "sidebarOffset"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isSidebarOpen) 0.6f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "overlayAlpha"
    )

    val contentScale by animateFloatAsState(
        targetValue = if (isSidebarOpen) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "contentScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 30 && !isSidebarOpen) {
                        isSidebarOpen = true
                    } else if (dragAmount < -30 && isSidebarOpen) {
                        isSidebarOpen = false
                    }
                }
            }
    ) {
        // Main Content with scale animation
        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(contentScale)
                .clip(RoundedCornerShape(if (isSidebarOpen) 24.dp else 0.dp))
        ) {
            // === PREMIUM TOP NAVBAR ===
            PremiumTopNavbar(
                title = title,
                onMenuClick = { isSidebarOpen = !isSidebarOpen },
                userSession = userSession
            )

            // === CONTENT AREA ===
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (enablePullRefresh && onRefresh != null) {
                    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = onRefresh,
                        indicator = { state, trigger ->
                            CustomRefreshIndicator(
                                state = state,
                                refreshTriggerDistance = trigger,
                                isRefreshing = isRefreshing
                            )
                        }
                    ) {
                        content(PaddingValues(16.dp))
                    }
                } else {
                    content(PaddingValues(16.dp))
                }
            }

            // === PREMIUM BOTTOM NAVIGATION ===
            BottomFooter(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }

        // === OVERLAY ===
        if (isSidebarOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = overlayAlpha * 0.3f),
                                Color.Black.copy(alpha = overlayAlpha)
                            ),
                            radius = 1500f
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isSidebarOpen = false }
                    .zIndex(10f)
            )
        }

        // === PREMIUM SIDEBAR ===
        PremiumSidebarDrawer(
            offset = sidebarOffset,
            width = sidebarWidth,
            currentRoute = currentRoute,
            userSession = userSession,
            menuItems = getSidebarMenuItems(),
            onNavigate = { route ->
                onNavigate(route)
                isSidebarOpen = false
            },
            onLogout = {
                onLogout()
                isSidebarOpen = false
            },
            onClose = { isSidebarOpen = false }
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  PREMIUM TOP NAVBAR (Sky Blue, theme-aware)
// ════════════════════════════════════════════════════════════════
@Composable
fun PremiumTopNavbar(
    title: String,
    onMenuClick: () -> Unit,
    userSession: UserSession?
) {
    val isDark = isSystemInDarkTheme()
    val navbarStart = if (isDark) NavbarStartDark else NavbarStartLight
    val navbarEnd = if (isDark) NavbarEndDark else NavbarEndLight

    val infiniteTransition = rememberInfiniteTransition(label = "navbarGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(navbarStart, navbarEnd)
                )
            )
            .drawBehind {
                // Subtle accent line at bottom
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            SkyBlueAccent.copy(alpha = glowAlpha),
                            SkyBlueLight.copy(alpha = glowAlpha),
                            Color.White.copy(alpha = glowAlpha * 0.4f)
                        )
                    ),
                    topLeft = Offset(0f, size.height - 3f),
                    size = Size(size.width, 3f)
                )
                // Decorative circle
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = 100f,
                    center = Offset(size.width - 60f, -20f)
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onMenuClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = stringResource(AppStrings.Menu),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 22.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.weight(1f)
            )

            // Premium avatar with gradient ring
            Box(contentAlignment = Alignment.Center) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.8f),
                                    SkyBlueLight,
                                    Color.White.copy(alpha = 0.6f),
                                    SkyBlueMedium,
                                    Color.White.copy(alpha = 0.8f)
                                )
                            )
                        )
                )
                // Inner avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SkyBlueDark, SkyBlueAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (userSession?.fullName?.firstOrNull()?.toString() ?: "U").uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  PREMIUM SIDEBAR DRAWER (Sky Blue, theme-aware)
// ════════════════════════════════════════════════════════════════
@Composable
fun PremiumSidebarDrawer(
    offset: Dp,
    width: Dp,
    currentRoute: String,
    userSession: UserSession?,
    menuItems: List<SidebarMenuItem>,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val sidebarGradientStart = if (isDark) SidebarGradientStartDark else SidebarGradientStartLight
    val sidebarGradientEnd = if (isDark) SidebarGradientEndDark else SidebarGradientEndLight

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .offset(x = offset)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
            )
            .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .zIndex(20f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── HEADER ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(sidebarGradientStart, sidebarGradientEnd)
                        )
                    )
                    .drawBehind {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.06f),
                            radius = 120f,
                            center = Offset(size.width - 40f, 30f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.04f),
                            radius = 80f,
                            center = Offset(60f, size.height - 20f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.03f),
                            radius = 160f,
                            center = Offset(size.width + 20f, size.height + 40f)
                        )
                    }
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (userSession?.fullName?.firstOrNull()?.toString() ?: "U").uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                )
                            }
                        }

                        // Close button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(AppStrings.Close),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userSession?.fullName ?: "User",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = userSession?.email ?: "",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Role badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (userSession?.role ?: "user").uppercase(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // ── MENU ITEMS ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp, horizontal = 10.dp)
            ) {
                Text(
                    text = "MENU",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                menuItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    PremiumSidebarItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }

            // ── LOGOUT ──
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLogout() }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Logout,
                        contentDescription = stringResource(AppStrings.Logout),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(AppStrings.Logout),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun PremiumSidebarItem(
    item: SidebarMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val sidebarGradientStart = if (isDark) SidebarGradientStartDark else SidebarGradientStartLight
    val sidebarGradientEnd = if (isDark) SidebarGradientEndDark else SidebarGradientEndLight

    val bgBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                SkyBlueDark.copy(alpha = if (isDark) 0.20f else 0.12f),
                SkyBlueMedium.copy(alpha = if (isDark) 0.10f else 0.05f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }

    val selectedTextColor = if (isDark) SkyBlueLight else SkyBlueDeep
    val textColor = if (isSelected) selectedTextColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    val selectedIconColor = if (isDark) SkyBlueMedium else SkyBlueDark
    val iconColor = if (isSelected) selectedIconColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val iconToUse = if (isSelected) item.icon else item.iconOutlined

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (isSelected) SkyBlueDark.copy(alpha = if (isDark) 0.20f else 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconToUse,
                contentDescription = stringResource(item.titleRes),
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = stringResource(item.titleRes),
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        // Selected indicator bar
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(sidebarGradientStart, sidebarGradientEnd)
                        )
                    )
            )
        }

        if (item.badge > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.error
            ) {
                Text(
                    text = item.badge.toString(),
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  PREMIUM BOTTOM NAVIGATION (Sky Blue, theme-aware)
// ════════════════════════════════════════════════════════════════
@Composable
fun BottomFooter(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bottomNavBg = MaterialTheme.colorScheme.surface
    val bottomNavSelected = if (isDark) BottomNavSelectedDark else BottomNavSelectedLight

    val navItems = listOf(
        BottomNavItem(
            Icons.Filled.Dashboard,
            Icons.Outlined.Dashboard,
            AppStrings.Home,
            Screen.Dashboard.route
        ),
        BottomNavItem(
            Icons.Filled.ListAlt,
            Icons.Outlined.ListAlt,
            AppStrings.Questionnaire,
            Screen.Kuesioner.route
        ),
        BottomNavItem(
            Icons.Filled.Assessment,
            Icons.Outlined.Assessment,
            AppStrings.Reports,
            Screen.Reports.route
        ),
        BottomNavItem(
            Icons.Filled.Settings,
            Icons.Outlined.Settings,
            AppStrings.Settings,
            Screen.Settings.route
        )
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bottomNavBg,
        shadowElevation = 24.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column {
            // Premium accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                SkyBlueMedium.copy(alpha = if (isDark) 0.3f else 0.4f),
                                SkyBlueAccent.copy(alpha = if (isDark) 0.4f else 0.5f),
                                SkyBlueMedium.copy(alpha = if (isDark) 0.3f else 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val icon = if (isSelected) item.icon else item.iconOutlined
                    val label = stringResource(item.labelRes)

                    BottomNavItemView(
                        icon = icon,
                        label = label,
                        isSelected = isSelected,
                        selectedColor = bottomNavSelected,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItemView(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bottomNavScale"
    )

    val animatedIconSize by animateDpAsState(
        targetValue = if (isSelected) 26.dp else 22.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "iconSize"
    )

    val iconColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val labelColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(animatedScale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 6.dp)
    ) {
        // Icon with animated pill background
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(36.dp)
        ) {
            // Pill background for selected
            val pillAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = tween(200),
                label = "pillAlpha"
            )
            val pillScale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.6f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "pillScale"
            )

            if (pillAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(34.dp)
                        .scale(pillScale)
                        .clip(RoundedCornerShape(17.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    selectedColor.copy(alpha = 0.12f * pillAlpha),
                                    SkyBlueMedium.copy(alpha = 0.08f * pillAlpha)
                                )
                            )
                        )
                )
            }

            // THE ICON - always visible
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(animatedIconSize)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Label
        Text(
            text = label,
            color = labelColor,
            fontSize = if (isSelected) 11.sp else 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = if (isSelected) 0.3.sp else 0.sp,
            maxLines = 1
        )

        // Active dot indicator
        Spacer(modifier = Modifier.height(4.dp))
        val dotWidth by animateDpAsState(
            targetValue = if (isSelected) 16.dp else 0.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "dotWidth"
        )
        Box(
            modifier = Modifier
                .width(dotWidth)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(SkyBlueDark, SkyBlueAccent)
                    )
                )
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  REFRESH INDICATORS (warna sky blue)
// ════════════════════════════════════════════════════════════════
@Composable
fun CustomRefreshIndicator(
    state: com.google.accompanist.swiperefresh.SwipeRefreshState,
    refreshTriggerDistance: Dp,
    isRefreshing: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isRefreshing -> LoadingAnimation()
            state.indicatorOffset > 0 -> {
                val pullProgress = (state.indicatorOffset / refreshTriggerDistance.value).coerceIn(0f, 1f)
                PullAnimation(pullProgress = pullProgress)
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = SkyBlueDark.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {}

        Canvas(
            modifier = Modifier
                .size(36.dp)
                .rotate(rotation)
        ) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(SkyBlueDark, SkyBlueAccent, SkyBlueLight)
                ),
                startAngle = 0f,
                sweepAngle = 280f,
                useCenter = false,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
fun PullAnimation(pullProgress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = pullProgress,
        animationSpec = tween(100),
        label = "pullProgress"
    )

    val rotation = animatedProgress * 180f

    Box(
        modifier = Modifier
            .size((32 + (pullProgress * 16)).dp)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = SkyBlueDark.copy(alpha = 0.1f + (pullProgress * 0.1f)),
            modifier = Modifier.fillMaxSize()
        ) {}

        Canvas(
            modifier = Modifier.size((24 + (pullProgress * 12)).dp)
        ) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(SkyBlueDark, SkyBlueAccent)
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  LEGACY ALIASES (agar file lain yang memanggil nama lama tidak error)
// ════════════════════════════════════════════════════════════════
@Composable
fun TopNavbar(
    title: String,
    onMenuClick: () -> Unit,
    userSession: UserSession?
) = PremiumTopNavbar(title = title, onMenuClick = onMenuClick, userSession = userSession)

@Composable
fun SidebarDrawer(
    offset: Dp,
    width: Dp,
    currentRoute: String,
    userSession: UserSession?,
    menuItems: List<SidebarMenuItem>,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) = PremiumSidebarDrawer(
    offset = offset,
    width = width,
    currentRoute = currentRoute,
    userSession = userSession,
    menuItems = menuItems,
    onNavigate = onNavigate,
    onLogout = onLogout,
    onClose = onClose
)

@Composable
fun SidebarItem(
    item: SidebarMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) = PremiumSidebarItem(item = item, isSelected = isSelected, onClick = onClick)