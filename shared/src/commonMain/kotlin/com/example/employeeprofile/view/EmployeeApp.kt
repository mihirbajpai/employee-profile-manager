package com.example.employeeprofile.view

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.view.screen.detail.EmployeeDetailScreen
import com.example.employeeprofile.view.screen.form.EmployeeFormScreen
import com.example.employeeprofile.view.screen.list.EmployeeListScreen
import com.example.employeeprofile.view.screen.topearners.TopEarnersScreen
import com.example.employeeprofile.view.theme.EmployeeProfileTheme

@Composable
fun EmployeeApp() {
    EmployeeProfileTheme {
        val navController = rememberNavController()
        // Form and Detail both take an employee id; the form reads NEW_EMPLOYEE_ID as "create".
        val formRoute = "${Screen.FORM.route}/{$ARG_EMPLOYEE_ID}"
        val detailRoute = "${Screen.DETAIL.route}/{$ARG_EMPLOYEE_ID}"
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
            navController = navController,
            startDestination = Screen.LIST.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            composable(Screen.LIST.route) {
                EmployeeListScreen(
                    onAddEmployee = {
                        navController.navigate(Screen.FORM.withArgs(Employee.NO_ID))
                    },
                    onEditEmployee = { navController.navigate(Screen.FORM.withArgs(it)) },
                    onViewEmployee = { navController.navigate(Screen.DETAIL.withArgs(it)) },
                    onViewTopEarners = { navController.navigate(Screen.TOP_EARNERS.route) }
                )
            }

            composable(route = formRoute, arguments = employeeIdArgument) { entry ->
                EmployeeFormScreen(
                    employeeId = entry.employeeId(),
                    onDone = navController::navigateUp
                )
            }

            composable(route = detailRoute, arguments = employeeIdArgument) { entry ->
                EmployeeDetailScreen(
                    employeeId = entry.employeeId(),
                    onEdit = { navController.navigate(Screen.FORM.withArgs(it)) },
                    onBack = navController::navigateUp
                )
            }

            composable(Screen.TOP_EARNERS.route) {
                TopEarnersScreen(onBack = navController::navigateUp)
            }
        }
    }
}

/** Nav destinations. [withArgs] appends positional args — Form and Detail pass an id this way. */
enum class Screen(val route: String) {
    LIST("list"),
    FORM("form"),
    DETAIL("detail"),
    TOP_EARNERS("top_earners");

    fun withArgs(vararg args: Any): String {
        return "$route/${args.joinToString("/")}"
    }
}

private const val ARG_EMPLOYEE_ID = "employee_id"

private val employeeIdArgument =
    listOf(navArgument(ARG_EMPLOYEE_ID) { type = NavType.LongType })

private fun androidx.navigation.NavBackStackEntry.employeeId(): Long =
    arguments?.read { getLong(ARG_EMPLOYEE_ID) } ?: Employee.NO_ID
