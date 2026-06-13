package com.example.profilediscoveryapp.mvvm.view.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.profile_discovery.mvvm.viewmodel.DiscoveryViewModel
import com.example.profilediscoveryapp.mvvm.viewmodel.ProfileDetailViewModel
import com.example.profilediscoveryapp.mvvm.model.repository.ProfileRepository
import com.example.profilediscoveryapp.mvvm.viewmodel.ProfileViewModel
import com.example.profilediscoveryapp.mvvm.viewmodel.SettingsViewModel

@Composable
fun MainScreen(
    userId: String,
    profileRepository: ProfileRepository,
    settingsViewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val profileViewModel = remember { ProfileViewModel(profileRepository, userId) }
    val discoveryViewModel = remember { DiscoveryViewModel(profileRepository, userId) }
    val context = LocalContext.current

    // Double back press to exit
    var backPressedTime by remember { mutableStateOf(0L) }

    // Handle system back button
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentBackStackEntry?.destination?.route == "profile") {
                    // Double back press to exit
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        (context as? Activity)?.finishAffinity()
                    } else {
                        backPressedTime = System.currentTimeMillis()
                        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    // Navigate back normally
                    navController.popBackStack()
                }
            }
        }
        backDispatcher?.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = currentDestination == "profile",
                    onClick = {
                        navController.navigate("profile") {
                            popUpTo("profile") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Discover") },
                    label = { Text("Discover") },
                    selected = currentDestination == "discover",
                    onClick = {
                        navController.navigate("discover") {
                            popUpTo("discover") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentDestination == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo("settings") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "profile",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("profile") {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToEditProfile = {
                        navController.navigate("edit_profile")
                    }
                )
            }

            composable("edit_profile") {
                val editProfileViewModel = remember {
                    ProfileViewModel(
                        profileRepository = profileRepository,
                        userId = userId
                    )
                }
                EditProfileScreen(
                    viewModel = editProfileViewModel,
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable("discover") {
                DiscoveryScreen(
                    viewModel = discoveryViewModel,
                    onNavigateToProfileDetail = { profileId ->
                        navController.navigate("profile_detail/$profileId")
                    },
                    onSyncClick = {
                        discoveryViewModel.syncWithApi()
                    }
                )
            }

            composable(
                "profile_detail/{profileId}",
                arguments = listOf(navArgument("profileId") { type = NavType.StringType })
            ) { backStackEntry ->
                val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
                val detailViewModel = remember {
                    ProfileDetailViewModel(profileRepository, userId, profileId)
                }
                ProfileDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onLogout = {
                        onLogout()
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}