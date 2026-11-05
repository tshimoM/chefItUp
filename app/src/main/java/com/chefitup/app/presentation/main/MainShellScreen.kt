package com.chefitup.app.presentation.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chefitup.app.R
import com.chefitup.app.presentation.components.AnimatedOfflineBanner
import com.chefitup.app.presentation.home.HomeScreen
import com.chefitup.app.presentation.mealplan.MealPlannerScreen
import com.chefitup.app.presentation.navigation.Screen
import com.chefitup.app.presentation.profile.ProfileScreen
import com.chefitup.app.presentation.saved.SavedScreen
import com.chefitup.app.presentation.search.SearchScreen
import com.chefitup.app.presentation.theme.ChefMotion
import com.chefitup.app.presentation.theme.FlameOrange

private data class TabItem(
    val screen: Screen,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainShellScreen(
    rootNavController: NavHostController,
    onLogout: () -> Unit,
    connectivityViewModel: ConnectivityViewModel = hiltViewModel()
) {
    val tabNavController = rememberNavController()
    val isOffline by connectivityViewModel.isOffline.collectAsStateWithLifecycle()
    val tabs = listOf(
        TabItem(Screen.Home, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
        TabItem(Screen.Search, R.string.nav_search, Icons.Filled.Search, Icons.Outlined.Search),
        TabItem(
            Screen.MealPlanner,
            R.string.nav_meal_planner,
            Icons.Filled.CalendarMonth,
            Icons.Outlined.CalendarMonth
        ),
        TabItem(
            Screen.Saved,
            R.string.nav_saved,
            Icons.Filled.Bookmark,
            Icons.Outlined.BookmarkBorder
        ),
        TabItem(Screen.Profile, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
    )
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ChefBottomBar(
                tabs = tabs,
                selectedRoute = currentDestination?.route,
                onTabSelected = { route ->
                    tabNavController.navigate(route) {
                        popUpTo(tabNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedOfflineBanner(visible = isOffline)
            NavHost(
                navController = tabNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onOpenSearch = {
                            tabNavController.navigate(Screen.Search.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenCookWithIngredients = {
                            rootNavController.navigate(Screen.CookWithIngredients.route)
                        },
                        onOpenRecipe = { id ->
                            rootNavController.navigate(Screen.RecipeDetail.create(id))
                        },
                        onOpenMealPlanner = {
                            tabNavController.navigate(Screen.MealPlanner.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenSaved = {
                            tabNavController.navigate(Screen.Saved.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenProfile = {
                            tabNavController.navigate(Screen.Profile.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        onOpenRecipe = { id ->
                            rootNavController.navigate(Screen.RecipeDetail.create(id))
                        }
                    )
                }
                composable(Screen.MealPlanner.route) {
                    MealPlannerScreen(
                        onOpenRecipe = { id ->
                            rootNavController.navigate(Screen.RecipeDetail.create(id))
                        },
                        onOpenShoppingList = {
                            rootNavController.navigate(Screen.ShoppingList.route)
                        }
                    )
                }
                composable(Screen.Saved.route) {
                    SavedScreen(
                        onOpenRecipe = { id ->
                            rootNavController.navigate(Screen.RecipeDetail.create(id))
                        }
                    )
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onOpenSettings = {
                            rootNavController.navigate(Screen.Settings.route)
                        },
                        onLoggedOut = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun ChefBottomBar(
    tabs: List<TabItem>,
    selectedRoute: String?,
    onTabSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                ChefBottomBarItem(
                    label = stringResource(tab.labelRes),
                    selectedIcon = tab.selectedIcon,
                    unselectedIcon = tab.unselectedIcon,
                    selected = selectedRoute == tab.screen.route,
                    onClick = { onTabSelected(tab.screen.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChefBottomBarItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) FlameOrange else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = ChefMotion.decelerateTween(ChefMotion.Fast),
        label = "nav_icon_color"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        },
        animationSpec = ChefMotion.decelerateTween(ChefMotion.Fast),
        label = "nav_label_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.96f,
        animationSpec = ChefMotion.softSpring(),
        label = "nav_scale"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = ChefMotion.decelerateTween(ChefMotion.Fast),
        label = "nav_indicator"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 30.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    FlameOrange.copy(alpha = 0.14f * indicatorAlpha)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
