package com.visiontwin.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.visiontwin.app.data.cache.CacheManager
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.screens.*
import com.visiontwin.app.ui.screens.admin.*

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: VisionTwinRepository,
    cacheManager: CacheManager
) {
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(
                onNavigateToMachines = {
                    navController.navigate("machines") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("machines") {
            MachineListScreen(
                repository = repository,
                onMachineSelected = { machineId, machineName ->
                    navController.navigate("upload/$machineId/$machineName")
                },
                onAdminLogin = {
                    navController.navigate("admin/login")
                }
            )
        }

        composable(
            route = "upload/{machineId}/{machineName}",
            arguments = listOf(
                navArgument("machineId") { type = NavType.StringType },
                navArgument("machineName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            val machineName = backStackEntry.arguments?.getString("machineName") ?: ""
            ImageUploadScreen(
                machineId = machineId,
                machineName = machineName,
                repository = repository,
                onBack = { navController.popBackStack() },
                onResult = { reportId ->
                    navController.navigate("result/$reportId")
                }
            )
        }

        composable(
            route = "result/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            ResultScreen(
                reportId = reportId,
                repository = repository,
                onBack = { navController.popBackStack() },
                onChat = { id -> navController.navigate("chat/$id") }
            )
        }

        composable(
            route = "chat/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            ChatScreen(
                reportId = reportId,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        // Admin routes
        composable("admin/login") {
            AdminLoginScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate("admin/dashboard") {
                        popUpTo("admin/login") { inclusive = true }
                    }
                }
            )
        }

        composable("admin/dashboard") {
            AdminDashboardScreen(
                repository = repository,
                onLogout = {
                    cacheManager.clearAdminToken()
                    navController.navigate("machines") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onMachines = {
                    navController.navigate("machines")
                },
                onAddMachine = {
                    navController.navigate("admin/add-machine")
                },
                onReports = {
                    navController.navigate("admin/reports")
                }
            )
        }

        composable("admin/add-machine") {
            AdminAddMachineScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable("admin/reports") {
            AdminReportsScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onReportClick = { id -> navController.navigate("admin/report/$id") }
            )
        }

        composable(
            route = "admin/report/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            AdminReportDetailScreen(
                reportId = reportId,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
