package com.sajdah.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sajdah.app.ui.screens.home.HomeScreen
import com.sajdah.app.ui.screens.qibla.QiblaScreen
import com.sajdah.app.ui.screens.settings.SettingsScreen
import com.sajdah.app.ui.screens.zakat.ZakatScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val rootNavController = rememberNavController()
    
    NavHost(
        navController = rootNavController,
        startDestination = "home_pager"
    ) {
        composable("home_pager") {
            HomePagerScreen(rootNavController)
        }
        composable(
            route = "zakat",
            enterTransition = { slideInVertically(initialOffsetY = { it }) + fadeIn() },
            exitTransition = { slideOutVertically(targetOffsetY = { it }) + fadeOut() }
        ) {
            ZakatScreen(onBack = { rootNavController.popBackStack() })
        }
    }
}

class BottomNavItem(val title: String, val iconRes: Int)

val bottomItems = listOf(
    BottomNavItem("Home", com.sajdah.app.R.drawable.nav_home),
    BottomNavItem("Qibla", com.sajdah.app.R.drawable.nav_qibla),
    BottomNavItem("Settings", com.sajdah.app.R.drawable.nav_settings)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePagerScreen(rootNavController: NavController) {
    val pagerState = rememberPagerState(pageCount = { bottomItems.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.height(80.dp)
            ) {
                bottomItems.forEachIndexed { index, item ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(if (isSelected) 34.dp else 28.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    alpha = if (isSelected) 1f else 0.45f
                                )
                            }
                            AnimatedVisibility(visible = isSelected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
            beyondBoundsPageCount = 2 // Keep all 3 screens loaded to completely eliminate swipe lag
        ) { page ->
            when (page) {
                0 -> HomeScreen()
                1 -> QiblaScreen()
                2 -> SettingsScreen(onNavigateToZakat = { rootNavController.navigate("zakat") })
            }
        }
    }
}
