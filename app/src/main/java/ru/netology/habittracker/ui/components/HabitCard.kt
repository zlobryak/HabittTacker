package ru.netology.habittracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.netology.habittracker.data.Habit
import ru.netology.habittracker.ui.theme.HabitTrackerTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habit: Habit,
    weekStart: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Название привычки
                Text(
                    text = habit.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Прогресс
                val progress = habit.getCompletionPercentage(weekStart)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${(progress * 100).toInt()}% выполнено",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Дни недели
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    habit.getWeekProgress(weekStart).forEach { dayProgress ->
                        DayItem(
                            date = dayProgress.date,
                            isCompleted = dayProgress.isCompleted,
                            isToday = dayProgress.isToday,
                            isClickable = dayProgress.isToday,
                            onClick = { onDayClick(dayProgress.date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// Preview функции
@Preview(name = "Новая привычка (0%)", showBackground = true)
@Composable
private fun HabitCardNewPreview() {
    HabitTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            val today = LocalDate.now()
            val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            
            HabitCard(
                habit = Habit(
                    name = "Утренняя зарядка",
                    completionHistory = emptyMap()
                ),
                weekStart = weekStart,
                onDayClick = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "Привычка с прогрессом 50%", showBackground = true)
@Composable
private fun HabitCardHalfCompletedPreview() {
    HabitTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            val today = LocalDate.now()
            val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            
            val completionHistory = mapOf(
                weekStart to true,
                weekStart.plusDays(2) to true,
                weekStart.plusDays(4) to true
            )
            
            HabitCard(
                habit = Habit(
                    name = "Читать книгу 30 минут",
                    completionHistory = completionHistory
                ),
                weekStart = weekStart,
                onDayClick = {},
                onDelete = {}
            )
        }
    }
}
