package ru.netology.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.netology.habittracker.ui.theme.HabitTrackerTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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

// Preview функции
@Preview(name = "Обычный день", showBackground = true)
@Composable
private fun DayItemPreview() {
    HabitTrackerTheme {
        Surface {
            DayItem(
                date = LocalDate.of(2024, 1, 15),
                isCompleted = false,
                isToday = false,
                isClickable = false,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Сегодняшний день", showBackground = true)
@Composable
private fun DayItemTodayPreview() {
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
