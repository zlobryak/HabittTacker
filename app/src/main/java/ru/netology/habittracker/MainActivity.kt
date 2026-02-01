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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.netology.habittracker.theme.HabitTrackerTheme
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