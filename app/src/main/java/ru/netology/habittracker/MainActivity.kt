package ru.netology.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import ru.netology.habittracker.navigation.NavGraph
import ru.netology.habittracker.ui.theme.HabitTrackerTheme

/**
 * Главная Activity приложения Habit Tracker
 * 
 * Настраивает навигацию и тему приложения
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
