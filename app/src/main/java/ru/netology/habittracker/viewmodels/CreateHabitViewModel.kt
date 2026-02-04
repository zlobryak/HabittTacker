package ru.netology.habittracker.viewmodels

import androidx.lifecycle.ViewModel
import ru.netology.habittracker.data.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.habittracker.repository.HabitRepository

/**
 * Immutable data class representing the UI state for the habit creation screen.
 *
 * <p>Encapsulates all form data and validation state required by the UI layer
 * when creating a new habit. Follows MVI (Model-View-Intent) pattern principles
 * where UI components consume state but never modify it directly.</p>
 *
 * <p>State properties:
 * <ul>
 *   <li><strong>habitName:</strong> Current text input for the habit name field</li>
 *   <li><strong>isNameError:</strong> Validation flag indicating whether name input is invalid</li>
 *   <li><strong>errorMessage:</strong> Human-readable error message for display in UI</li>
 * </ul></p>
 *
 * <p>Validation workflow:
 * <ol>
 *   <li>User types habit name → habitName updated</li>
 *   <li>User submits form → validation occurs in [CreateHabitViewModel.saveHabit]</li>
 *   <li>If validation fails → isNameError=true, errorMessage populated</li>
 *   <li>If validation passes → new Habit created and saved to repository</li>
 * </ol></p>
 *
 * <p>Design rationale: Using a single state object simplifies UI updates and prevents
 * inconsistent intermediate states. All state transitions are atomic and predictable.</p>
 *
 * @property habitName Current text input for habit name (default: empty string)
 * @property isNameError Validation error flag for name field (default: false)
 * @property errorMessage Error message to display when validation fails (default: empty string)
 *
 * @constructor Creates a CreateHabitUiState instance with optional parameters
 * @see CreateHabitViewModel for state management implementation
 */
data class CreateHabitUiState(
    val habitName: String = "",
    val isNameError: Boolean = false,
    val errorMessage: String = ""
)

/**
 * ViewModel responsible for managing habit creation screen state and business logic.
 *
 * <p>Implements MVVM architecture pattern for the habit creation form. Handles user input,
 * validation, persistence operations, and state transitions. Coordinates between UI layer
 * and repository to create new habit entries.</p>
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Manages form input state (habit name)</li>
 *   <li>Performs input validation with error feedback</li>
 *   <li>Creates and persists new Habit instances</li>
 *   <li>Provides state reset functionality for form reuse</li>
 * </ul></p>
 *
 * <p>State management strategy:
 * <ul>
 *   <li>Uses MutableStateFlow for internal state mutations</li>
 *   <li>Exposes read-only StateFlow to UI layer for safe consumption</li>
 *   <li>Immutably updates state using Kotlin's copy() mechanism</li>
 *   <li>Clears error state on user input to provide immediate feedback</li>
 * </ul></p>
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Habit name must not be blank (after trimming whitespace)</li>
 *   <li>Error message: "Название не может быть пустым" (Name cannot be empty)</li>
 *   <li>No length restrictions or character validation (expandable for production)</li>
 * </ul></p>
 *
 * <p>Thread safety: All operations occur on the main thread (ViewModel lifecycle),
 * ensuring sequential consistency for state updates.</p>
 *
 * @property repository Data access layer for habit persistence (injected, defaults to in-memory implementation)
 * @constructor Creates CreateHabitViewModel with optional repository parameter
 * @see androidx.lifecycle.ViewModel for lifecycle ownership semantics
 * @see HabitRepository for data persistence layer
 * @see CreateHabitUiState for UI state representation
 */
class CreateHabitViewModel(
) : ViewModel() {
    /**
     * Internal mutable state flow for managing habit creation form state.
     *
     * <p>Holds the current [CreateHabitUiState] instance representing form data,
     * validation status, and error messages. Updated reactively in response to:
     * <ul>
     *   <li>User typing in name field ([onHabitNameChange])</li>
     *   <li>Form submission with validation ([saveHabit])</li>
     *   <li>State reset requests ([clearState])</li>
     * </ul></p>
     *
     * <p>Initial state: Fresh [CreateHabitUiState] with empty name and no errors.</p>
     *
     * <p>State mutation pattern: Uses immutable copy() to create new state instances,
     * ensuring referential transparency and predictable UI updates.</p>
     *
     * @see uiState for public read-only exposure
     * @see CreateHabitUiState for state structure
     */
    private val _uiState = MutableStateFlow(CreateHabitUiState())

    /**
     * Public read-only state flow exposing current form state to UI observers.
     *
     * <p>Emits [CreateHabitUiState] snapshots whenever form state changes. UI components
     * (typically composables or activities) collect this flow to render:
     * <ul>
     *   <li>Text field content (habitName)</li>
     *   <li>Error indicators (isNameError)</li>
     *   <li>Error messages (errorMessage)</li>
     * </ul></p>
     *
     * <p>Guarantees:
     * <ul>
     *   <li>Always emits initial state immediately upon collection</li>
     *   <li>Emits new state on every user interaction or validation result</li>
     *   <li>Never throws exceptions during normal operation</li>
     * </ul></p>
     *
     * <p>Typical consumption pattern in Jetpack Compose:
     * <pre>
     * val uiState by viewModel.uiState.collectAsState()
     *
     * OutlinedTextField(
     *     value = uiState.habitName,
     *     onValueChange = { viewModel.onHabitNameChange(it) },
     *     isError = uiState.isNameError,
     *     supportingText = if (uiState.isNameError) uiState.errorMessage else null
     * )
     * </pre></p>
     *
     * @return StateFlow emitting CreateHabitUiState instances on every state change
     * @see MutableStateFlow.asStateFlow for exposure pattern rationale
     */
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    /**
     * Updates the habit name input field and clears any previous validation errors.
     *
     * <p>Called by UI layer whenever user types in the habit name text field. Clears
     * validation error state to provide immediate feedback - errors are only shown
     * after form submission attempt.</p>
     *
     * <p>State changes:
     * <ul>
     *   <li>habitName: Updated to new user input</li>
     *   <li>isNameError: Reset to false</li>
     *   <li>errorMessage: Cleared to empty string</li>
     * </ul></p>
     *
     * <p>User experience rationale: Clearing errors on input prevents persistent
     * error messages from discouraging user interaction. Errors reappear only after
     * explicit submission attempt.</p>
     *
     * @param name New text input from user (may be empty)
     * @throws NullPointerException if name parameter is null
     * @see _uiState for state storage
     * @see CreateHabitUiState.copy for immutable update pattern
     */
    fun onHabitNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            habitName = name,
            isNameError = false,
            errorMessage = ""
        )
    }

    /**
     * Validates form input and persists a new habit to the repository.
     *
     * <p>Implements the complete save workflow:
     * <ol>
     *   <li>Trims whitespace from habit name input</li>
     *   <li>Validates that name is not blank</li>
     *   <li>If invalid: Updates state with error message and returns false</li>
     *   <li>If valid: Creates new Habit instance and saves to repository</li>
     *   <li>Returns true on successful save</li>
     * </ol></p>
     *
     * <p>Validation rules:
     * <ul>
     *   <li>Blank/empty name after trimming → validation error</li>
     *   <li>Whitespace-only name → validation error</li>
     *   <li>Non-empty name → valid, proceeds to save</li>
     * </ul></p>
     *
     * <p>Side effects:
     * <ul>
     *   <li>On success: Repository emits updated habit list to observers</li>
     *   <li>On failure: UI state updated with error message for display</li>
     *   <li>Does not automatically clear form after save (caller responsibility)</li>
     * </ul></p>
     *
     * <p>Return value usage: UI layer typically checks return value to determine
     * whether to navigate away (true) or show error (false).</p>
     *
     * @return true if habit was successfully created and saved; false if validation failed
     * @see ru.netology.habittracker.repository.HabitRepository.addHabit for persistence logic
     * @see Habit for domain model creation
     * @see String.trim for whitespace normalization
     * @see String.isBlank for validation check
     */
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
        HabitRepository.addHabit(newHabit)


        return true
    }

    /**
     * Resets the form state to its initial values.
     *
     * <p>Clears all form fields and validation errors, returning UI to pristine state.
     * Typically called after successful habit creation when user navigates back to
     * the creation screen, or when explicitly resetting the form.</p>
     *
     * <p>State changes:
     * <ul>
     *   <li>habitName: Reset to empty string</li>
     *   <li>isNameError: Reset to false</li>
     *   <li>errorMessage: Reset to empty string</li>
     * </ul></p>
     *
     * <p>Use cases:
     * <ul>
     *   <li>Navigating back to creation screen after saving a habit</li>
     *   <li>User clicks "Clear" or "Reset" button</li>
     *   <li>Canceling habit creation and returning to default state</li>
     * </ul></p>
     *
     * <p>Note: This method does not affect repository state - only UI form state.</p>
     *
     * @see CreateHabitUiState for initial state values
     * @see _uiState for state mutation
     */
    fun clearState() {
        _uiState.value = CreateHabitUiState()
    }
}