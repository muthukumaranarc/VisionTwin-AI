package com.visiontwin.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.screens.*
import com.visiontwin.app.ui.screens.admin.*

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: VisionTwinRepository
) {
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // ─── Main tabs (with bottom navigation) ───────────────────────────────
        composable("dashboard") {
            DashboardScreen(
                repository = repository,
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onReportClick = { reportId ->
                    navController.navigate("result/$reportId")
                },
                onDiagnose = {
                    navController.navigate("diagnose") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onAddMachine = { navController.navigate("admin/add-machine") },
                onReports = { navController.navigate("admin/reports") }
            )
        }

        composable("machines") {
            MachineListScreen(
                repository = repository,
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo("machines") { inclusive = true }
                    }
                },
                onMachineSelected = { machineId ->
                    navController.navigate("machine/$machineId")
                },
                onMachineLongSelected = { machineId, machineName ->
                    navController.navigate("admin/ref-images/$machineId/$machineName")
                }
            )
        }

        composable("diagnose") {
            DiagnoseScreen(
                repository = repository,
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo("diagnose") { inclusive = true }
                    }
                },
                onResult = { reportId ->
                    navController.navigate("result/$reportId")
                }
            )
        }

        composable("announcements") {
            AnnouncementScreen(
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo("announcements") { inclusive = true }
                    }
                }
            )
        }

        composable("learn") {
            LearnScreen(
                repository = repository,
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo("learn") { inclusive = true }
                    }
                }
            )
        }

        composable("call-experts") {
            CallExpertsScreen(
                repository = repository,
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo("call-experts") { inclusive = true }
                    }
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                repository = repository,
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                onAddMachine = { navController.navigate("admin/add-machine") },
                onReports = { navController.navigate("admin/reports") }
            )
        }

        // ─── Machine detail ───────────────────────────────────────────────────
        composable(
            route = "machine/{machineId}",
            arguments = listOf(navArgument("machineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            MachineDetailScreen(
                machineId = machineId,
                repository = repository,
                onBack = { navController.popBackStack() },
                onDiagnose = { machineIdArg, machineName ->
                    navController.navigate("upload/$machineIdArg/$machineName")
                },
                onManageRefImages = { machineIdArg, machineName ->
                    navController.navigate("admin/ref-images/$machineIdArg/$machineName")
                }
            )
        }

        // ─── Diagnosis flow ───────────────────────────────────────────────────
        composable(
            route = "upload/{machineId}/{machineName}",
            arguments = listOf(
                navArgument("machineId") { type = NavType.StringType },
                navArgument("machineName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            val machineName = backStackEntry.arguments?.getString("machineName") ?: ""
            UploadDiagnoseScreen(
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

        // ─── Admin routes ─────────────────────────────────────────────────────
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

        composable(
            route = "admin/ref-images/{machineId}/{machineName}",
            arguments = listOf(
                navArgument("machineId") { type = NavType.StringType },
                navArgument("machineName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            val machineName = backStackEntry.arguments?.getString("machineName") ?: ""
            AdminRefImagesScreen(
                machineId = machineId,
                machineName = machineName,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
