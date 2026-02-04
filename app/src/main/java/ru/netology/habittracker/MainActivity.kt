package ru.netology.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.netology.habittracker.ui.theme.HabittTackerTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.netology.habittracker.data.Habit
import ru.netology.habittracker.ui.theme.HabitTrackerTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabittTackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HabitTrackerTheme  {
                        Surface {
                            DayItem(
                                date = LocalDate.now(),
                                isCompleted = true,
                                isToday = true,
                                isClickable = true,
                                onClick = {},
                                Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayItem(
    date: LocalDate,
    isCompleted: Boolean,
    isToday: Boolean,
    isClickable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val dayNumber = date.dayOfMonth

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = dayName,
            fontSize = 12.sp,
            color = if (isToday) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .clickable(enabled = isClickable, onClick = onClick)
        ) {
            Text(
                text = dayNumber.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview(name = "Сегодня выполнено", showBackground = true)
@Composable
private fun DayItemTodayCompletedPreview() {
    HabitTrackerTheme  {
        Surface {
            DayItem(
                date = LocalDate.now(),
                isCompleted = true,
                isToday = true,
                isClickable = true,
                onClick = {}
            )
        }


    }
}

@Preview(name = "Выполненный день", showBackground = true)
@Composable
private fun DayItemCompletedPreview() {
    HabitTrackerTheme {
        Surface {
            DayItem(
                date = LocalDate.of(2024, 1, 14),
                isCompleted = true,
                isToday = false,
                isClickable = false,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Неделя дней", showBackground = true)
@Composable
private fun DayItemWeekPreview() {
    HabitTrackerTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val today = LocalDate.now()
                val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

                (0..6).forEach { dayOffset ->
                    val date = weekStart.plusDays(dayOffset.toLong())
                    DayItem(
                        date = date,
                        isCompleted = dayOffset % 2 == 0, // Каждый второй день выполнен
                        isToday = date == today,
                        isClickable = date == today,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(name = "Dark Theme - Сегодня", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayItemTodayDarkPreview() {
    HabitTrackerTheme {
        Surface {
            DayItem(
                date = LocalDate.now(),
                isCompleted = false,
                isToday = true,
                isClickable = true,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Dark Theme - Выполнено", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayItemCompletedDarkPreview() {
    HabitTrackerTheme {
        Surface {
            DayItem(
                date = LocalDate.of(2024, 1, 14),
                isCompleted = true,
                isToday = false,
                isClickable = false,
                onClick = {}
            )
        }
    }
}


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

            // Создаем привычку с выполненными днями (пн, ср, пт)
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

@Preview(name = "Полностью выполненная привычка (100%)", showBackground = true)
@Composable
private fun HabitCardFullyCompletedPreview() {
    HabitTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            val today = LocalDate.now()
            val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

            // Все дни выполнены
            val completionHistory = (0..6).associate { dayOffset ->
                weekStart.plusDays(dayOffset.toLong()) to true
            }

            HabitCard(
                habit = Habit(
                    name = "Пить 2 литра воды",
                    completionHistory = completionHistory
                ),
                weekStart = weekStart,
                onDayClick = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "Длинное название привычки", showBackground = true)
@Composable
private fun HabitCardLongNamePreview() {
    HabitTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            val today = LocalDate.now()
            val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

            val completionHistory = mapOf(
                weekStart to true,
                weekStart.plusDays(1) to true
            )

            HabitCard(
                habit = Habit(
                    name = "Медитация и дыхательные упражнения каждое утро перед завтраком",
                    completionHistory = completionHistory
                ),
                weekStart = weekStart,
                onDayClick = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "Несколько привычек", showBackground = true)
@Composable
private fun HabitCardListPreview() {
    HabitTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            val today = LocalDate.now()
            val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Привычка 1
                HabitCard(
                    habit = Habit(
                        name = "Утренняя зарядка",
                        completionHistory = mapOf(
                            weekStart to true,
                            weekStart.plusDays(1) to true,
                            weekStart.plusDays(2) to true
                        )
                    ),
                    weekStart = weekStart,
                    onDayClick = {},
                    onDelete = {}
                )

                // Привычка 2
                HabitCard(
                    habit = Habit(
                        name = "Читать книгу",
                        completionHistory = mapOf(
                            weekStart to true,
                            weekStart.plusDays(3) to true
                        )
                    ),
                    weekStart = weekStart,
                    onDayClick = {},
                    onDelete = {}
                )

                // Привычка 3
                HabitCard(
                    habit = Habit(
                        name = "Пить воду",
                        completionHistory = (0..6).associate { dayOffset ->
                            weekStart.plusDays(dayOffset.toLong()) to true
                        }
                    ),
                    weekStart = weekStart,
                    onDayClick = {},
                    onDelete = {}
                )
            }
        }
    }
}

@Preview(name = "Dark Theme - Привычка с прогрессом",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HabitCardDarkPreview() {
    HabitTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            val today = LocalDate.now()
            val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

            val completionHistory = mapOf(
                weekStart to true,
                weekStart.plusDays(2) to true,
                weekStart.plusDays(4) to true,
                weekStart.plusDays(5) to true
            )

            HabitCard(
                habit = Habit(
                    name = "Вечерняя прогулка",
                    completionHistory = completionHistory
                ),
                weekStart = weekStart,
                onDayClick = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "Текущий день выполнен", showBackground = true)
@Composable
private fun HabitCardTodayCompletedPreview() {
    HabitTrackerTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            val today = LocalDate.now()
            val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)

            // Сегодняшний день выполнен
            val completionHistory = mapOf(
                today to true,
                weekStart to true,
                weekStart.plusDays(2) to true
            )

            HabitCard(
                habit = Habit(
                    name = "Изучение английского",
                    completionHistory = completionHistory
                ),
                weekStart = weekStart,
                onDayClick = {},
                onDelete = {}
            )
        }
    }
}