package com.example.cfvaname.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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

/**
 * Layout utama yang dipakai oleh semua Activity setelah login.
 * Berisi:
 * - Top Navbar (dengan ikon hamburger menu)
 * - Sidebar (slide dari kiri ke kanan)
 * - Footer navigation
 * - Content area di tengah
 */
@Composable
fun AppScaffold(
    currentRoute: String,
    userSession: UserSession?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    title: String = "Vename",
    content: @Composable (PaddingValues) -> Unit
) {
    var isSidebarOpen by remember { mutableStateOf(false) }
    val sidebarWidth = 280.dp

    val sidebarOffset by animateDpAsState(
        targetValue = if (isSidebarOpen) 0.dp else (-sidebarWidth),
        animationSpec = tween(durationMillis = 300),
        label = "sidebarOffset"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isSidebarOpen) 0.5f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "overlayAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
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
        // Main Content
        Column(modifier = Modifier.fillMaxSize()) {
            // === TOP NAVBAR ===
            TopNavbar(
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
                content(PaddingValues(16.dp))
            }

            // === BOTTOM FOOTER ===
            BottomFooter(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }

        // === OVERLAY (saat sidebar terbuka) ===
        if (isSidebarOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha))
                    .clickable { isSidebarOpen = false }
                    .zIndex(10f)
            )
        }

        // === SIDEBAR ===
        SidebarDrawer(
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

// ===================================================
// TOP NAVBAR
// ===================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavbar(
    title: String,
    onMenuClick: () -> Unit,
    userSession: UserSession?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger menu icon
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = VenamePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // App title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = VenamePrimary
                ),
                modifier = Modifier.weight(1f)
            )

            // User avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (userSession?.fullName?.firstOrNull()?.toString() ?: "U").uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

// ===================================================
// SIDEBAR DRAWER (slide horizontal dari kiri)
// ===================================================
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
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .offset(x = offset)
            .shadow(16.dp)
            .background(SidebarBg)
            .zIndex(20f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Sidebar Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(VenamePrimaryDark, VenamePrimary)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User avatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (userSession?.fullName?.firstOrNull()?.toString() ?: "U").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }

                        // Close button
                        IconButton(onClick = onClose) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Tutup",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userSession?.fullName ?: "User",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = userSession?.email ?: "",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = (userSession?.role ?: "user").uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Menu Items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                menuItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    SidebarItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }

            // Logout button
            Divider(color = Color.White.copy(alpha = 0.1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Logout,
                    contentDescription = "Logout",
                    tint = StatusError,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Keluar",
                    color = StatusError,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun SidebarItem(
    item: SidebarMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) VenamePrimary.copy(alpha = 0.15f) else Color.Transparent
    val textColor = if (isSelected) VenamePrimary else TextOnDark
    val iconColor = if (isSelected) VenamePrimary else TextOnDark.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        if (item.badge > 0) {
            Surface(
                shape = CircleShape,
                color = StatusError
            ) {
                Text(
                    text = item.badge.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ===================================================
// BOTTOM FOOTER NAVIGATION
// ===================================================
@Composable
fun BottomFooter(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val footerItems = listOf(
        Triple(Icons.Filled.Dashboard, "Home", Screen.Dashboard.route),
        Triple(Icons.Filled.Assessment, "Laporan", Screen.Reports.route),
        Triple(Icons.Filled.Person, "Profil", Screen.Profile.route),
        Triple(Icons.Filled.Settings, "Setting", Screen.Settings.route)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            footerItems.forEach { (icon, label, route) ->
                val isSelected = currentRoute == route
                val color = if (isSelected) VenamePrimary else TextSecondary

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigate(route) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}