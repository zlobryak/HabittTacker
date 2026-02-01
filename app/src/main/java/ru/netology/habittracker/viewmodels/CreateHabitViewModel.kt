package ru.netology.habittracker.viewmodels

import androidx.lifecycle.ViewModel
import ru.netology.habittracker.data.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.habittracker.repository.HabitRepository

data class CreateHabitUiState(
    val habitName: String = "",
    val isNameError: Boolean = false,
    val errorMessage: String = ""
)

class CreateHabitViewModel(
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    fun onHabitNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            habitName = name,
            isNameError = false,
            errorMessage = ""
        )
    }

    fun saveHabit(): Boolean {
        val name = _uiState.value.habitName.trim()

        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isNameError = true,
                errorMessage = "Название не может быть пустым"
            )
            return false
        }

        val newHabit = Habit(name = name)
        repository.addHabit(newHabit)

        return true
    }

    fun clearState() {
        _uiState.value = CreateHabitUiState()
    }
}