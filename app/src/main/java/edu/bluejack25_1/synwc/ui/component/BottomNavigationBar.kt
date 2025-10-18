package edu.bluejack25_1.synwc.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Todo : BottomNavItem(
        route = "todo",
        title = "To-Do",
        selectedIcon = Icons.Outlined.Checklist,
        unselectedIcon = Icons.Outlined.Checklist
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

@Composable
fun BeautifulBottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Todo,
        BottomNavItem.Settings
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                clip = false
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                // Scale animation for icon
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.0f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "iconScale"
                )

                // Offset animation for selected item (bounce up effect)
                val offsetY by animateDpAsState(
                    targetValue = if (selected) (-4).dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "offsetY"
                )

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.offset(y = offsetY)
                        ) {
                            // Icon container with gradient background for selected item
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(if (selected) 56.dp else 48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (selected) {
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Transparent
                                                )
                                            )
                                        }
                                    )
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(if (selected) 26.dp else 24.dp)
                                        .scale(scale),
                                    tint = if (selected) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    }
                                )
                            }

                            // Animated spacer
                            Spacer(modifier = Modifier.height(if (selected) 6.dp else 4.dp))

                            // Label with animation
                            Text(
                                text = item.title,
                                fontSize = if (selected) 12.sp else 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                },
                                maxLines = 1
                            )

                            // Active indicator dot
                            if (selected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
fun FloatingBottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Todo,
        BottomNavItem.Settings
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .height(80.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(35.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(35.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    FloatingNavItem(
                        item = item,
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .height(94.dp)
            .widthIn(min = 70.dp),
        shape = RoundedCornerShape(27.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )

            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}