package ru.netology.habittracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.netology.habittracker.ui.createhabit.CreateHabitScreen
import ru.netology.habittracker.ui.habitlist.HabitListScreen

/**
 * Определение маршрутов навигации приложения
 */
sealed class Screen(val route: String) {
    object HabitList : Screen("habit_list")
    object CreateHabit : Screen("create_habit")
}

/**
 * Навигационный граф приложения
 * 
 * @param navController контроллер навигации
 * @param startDestination начальный экран (по умолчанию список привычек)
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.HabitList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Экран списка привычек
        composable(Screen.HabitList.route) {
            HabitListScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.CreateHabit.route)
                }
            )
        }
        
        // Экран создания привычки
        composable(Screen.CreateHabit.route) {
            CreateHabitScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
