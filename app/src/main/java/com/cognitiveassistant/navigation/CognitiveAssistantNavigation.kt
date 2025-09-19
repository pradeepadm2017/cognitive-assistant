package com.cognitiveassistant.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cognitiveassistant.model.UserType
import com.cognitiveassistant.ui.screen.LoginScreen
import com.cognitiveassistant.ui.screen.PatientProfileScreen
import com.cognitiveassistant.ui.screen.TestSelectionScreen

@Composable
fun CognitiveAssistantNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { user ->
                    when (user.userType) {
                        UserType.PATIENT -> {
                            navController.navigate("patient_profile") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        UserType.DOCTOR -> {
                            navController.navigate("test_selection") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable("patient_profile") {
            PatientProfileScreen(
                onNext = { patient ->
                    navController.navigate("test_selection") {
                        popUpTo("patient_profile") { inclusive = true }
                    }
                }
            )
        }

        composable("test_selection") {
            TestSelectionScreen()
        }
    }
}