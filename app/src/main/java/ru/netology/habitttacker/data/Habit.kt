package ru.netology.habitttacker.data

import java.time.LocalDate
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val completionHistory: Map<LocalDate, Boolean> = emptyMap()
) {
    fun isCompletedOn(date: LocalDate): Boolean {
        return completionHistory[date] ?: false
    }

    fun getWeekProgress(startDate: LocalDate): List<DayProgress> {
        return (0..6).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            DayProgress(
                date = date,
                isCompleted = isCompletedOn(date),
                isToday = date == LocalDate.now()
            )
        }
    }

    fun getCompletionPercentage(startDate: LocalDate): Float {
        val weekProgress = getWeekProgress(startDate)
        val completedDays = weekProgress.count { it.isCompleted }
        return if (weekProgress.isEmpty()) 0f else completedDays.toFloat() / 7f
    }
}

data class DayProgress(
    val date: LocalDate,
    val isCompleted: Boolean,
    val isToday: Boolean
)