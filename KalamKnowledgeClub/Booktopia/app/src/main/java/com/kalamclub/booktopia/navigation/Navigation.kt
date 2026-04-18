package com.kalamclub.booktopia.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kalamclub.booktopia.ui.screens.*
import com.kalamclub.booktopia.viewmodel.AuthViewModel

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Landing : Screen("landing")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Dashboard : Screen("dashboard")
    data object Community : Screen("community")
}

/**
 * Main navigation component.
 */
@Composable
fun BooktopiaNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLanding = {
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Landing.route) {
            LandingScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onNavigateToCommunity = { navController.navigate(Screen.Community.route) }
            )
        }
        
        composable(Screen.Login.route) {
            // Auto-navigate to dashboard when logged in
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn && !isLoading) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                }
            }
            
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onNavigateBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToLogin = { 
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Dashboard.route) {
            // Redirect to login if not logged in
            LaunchedEffect(isLoggedIn, isLoading) {
                if (!isLoggedIn && !isLoading) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            }
            
            DashboardScreen(
                onNavigateToHome = { 
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToCommunity = { navController.navigate(Screen.Community.route) },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Community.route) {
            CommunityScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) }
            )
        }
    }
}
