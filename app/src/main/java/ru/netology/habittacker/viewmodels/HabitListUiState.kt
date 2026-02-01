package ru.netology.habittacker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.netology.habittacker.data.Habit
import ru.netology.habittacker.repository.HabitRepository
import ru.netology.habittacker.viewmodels.HabitListViewModel.Companion.getWeekStart
import java.time.LocalDate

data class HabitListUiState(
    val habits: List<Habit> = emptyList(),
    val searchQuery: String = "",
    val currentWeekStart: LocalDate = getWeekStart(LocalDate.now()),
    val showDeleteDialog: Boolean = false,
    val habitToDelete: Habit? = null
)

class HabitListViewModel(
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _currentWeekStart = MutableStateFlow(getWeekStart(LocalDate.now()))
    private val _showDeleteDialog = MutableStateFlow(false)
    private val _habitToDelete = MutableStateFlow<Habit?>(null)

    val uiState: StateFlow<HabitListUiState> = combine(
        repository.habits,
        _searchQuery,
        _currentWeekStart,
        _showDeleteDialog,
        _habitToDelete
    ) { habits, query, weekStart, showDialog, habitToDelete ->
        val filteredHabits = if (query.isBlank()) {
            habits
        } else {
            habits.filter { it.name.contains(query, ignoreCase = true) }
        }

        HabitListUiState(
            habits = filteredHabits,
            searchQuery = query,
            currentWeekStart = weekStart,
            showDeleteDialog = showDialog,
            habitToDelete = habitToDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitListUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleHabitCompletion(habitId: String, date: LocalDate) {
        // Только для сегодняшнего дня
        if (date == LocalDate.now()) {
            repository.toggleHabitCompletion(habitId, date)
        }
    }

    fun showDeleteDialog(habit: Habit) {
        _habitToDelete.value = habit
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
        _habitToDelete.value = null
    }

    fun deleteHabit(habitId: String) {
        repository.deleteHabit(habitId)
        hideDeleteDialog()
    }

    fun navigateWeek(forward: Boolean) {
        _currentWeekStart.value = if (forward) {
            _currentWeekStart.value.plusWeeks(1)
        } else {
            _currentWeekStart.value.minusWeeks(1)
        }
    }

    companion object {
        fun getWeekStart(date: LocalDate): LocalDate {
            // Понедельник как начало недели
            val dayOfWeek = date.dayOfWeek.value
            return date.minusDays((dayOfWeek - 1).toLong())
        }
    }
}