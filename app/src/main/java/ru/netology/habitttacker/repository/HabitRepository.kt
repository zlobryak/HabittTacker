package ru.netology.habitttacker.repository


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.habitttacker.data.Habit
import java.time.LocalDate

class HabitRepository {
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    fun addHabit(habit: Habit) {
        _habits.value = _habits.value + habit
    }

    fun updateHabit(habit: Habit) {
        _habits.value = _habits.value.map {
            if (it.id == habit.id) habit else it
        }
    }

    fun deleteHabit(habitId: String) {
        _habits.value = _habits.value.filter { it.id != habitId }
    }

    fun toggleHabitCompletion(habitId: String, date: LocalDate) {
        _habits.value = _habits.value.map { habit ->
            if (habit.id == habitId) {
                val newHistory = habit.completionHistory.toMutableMap()
                val currentStatus = newHistory[date] ?: false
                newHistory[date] = !currentStatus
                habit.copy(completionHistory = newHistory)
            } else {
                habit
            }
        }
    }

    fun searchHabits(query: String): List<Habit> {
        return if (query.isBlank()) {
            _habits.value
        } else {
            _habits.value.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }
}