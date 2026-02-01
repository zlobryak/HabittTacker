package ru.netology.habittracker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.netology.habittracker.data.Habit
import ru.netology.habittracker.repository.HabitRepository
import ru.netology.habittracker.viewmodels.HabitListViewModel.Companion.getWeekStart
import java.time.LocalDate

/**
 * Immutable data class representing the complete UI state for the habit list screen.
 *
 * <p>Serves as a single source of truth for the UI layer, encapsulating all rendering
 * data required by composables or views. Follows MVI (Model-View-Intent) pattern principles
 * where state is consumed but never modified directly by UI components.</p>
 *
 * <p>State properties:
 * <ul>
 *   <li><strong>habits:</strong> Filtered list of habits based on current search query</li>
 *   <li><strong>searchQuery:</strong> Current text in search field for filtering</li>
 *   <li><strong>currentWeekStart:</strong> Monday of the week currently displayed in UI</li>
 *   <li><strong>showDeleteDialog:</strong> Flag controlling delete confirmation dialog visibility</li>
 *   <li><strong>habitToDelete:</strong> Habit pending deletion (null if no deletion in progress)</li>
 * </ul></p>
 *
 * <p>Design rationale: Using a single state object simplifies UI updates and prevents
 * inconsistent intermediate states. All state transitions are atomic and predictable.</p>
 *
 * @property habits Filtered collection of habits for current view (default: empty list)
 * @property searchQuery Current search filter text (default: empty string)
 * @property currentWeekStart Monday date of currently displayed week (default: current week's Monday)
 * @property showDeleteDialog Dialog visibility flag (default: false)
 * @property habitToDelete Habit targeted for deletion, or null if none selected (default: null)
 *
 * @constructor Creates a HabitListUiState instance with optional parameters
 * @see HabitListViewModel for state management implementation
 */
data class HabitListUiState(
    val habits: List<Habit> = emptyList(),
    val searchQuery: String = "",
    val currentWeekStart: LocalDate = getWeekStart(LocalDate.now()),
    val showDeleteDialog: Boolean = false,
    val habitToDelete: Habit? = null
)

/**
 * ViewModel responsible for managing habit list screen state and business logic.
 *
 * <p>Implements MVVM architecture pattern with reactive state management using Kotlin Flows.
 * Coordinates between UI layer and repository, handling user interactions, state transformations,
 * and side effects like dialog management and week navigation.</p>
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Aggregates multiple state sources into unified [HabitListUiState]</li>
 *   <li>Handles search filtering with debounced updates</li>
 *   <li>Manages delete confirmation dialog lifecycle</li>
 *   <li>Controls week-based navigation (previous/next week)</li>
 *   <li>Restricts habit completion toggling to current date only</li>
 * </ul></p>
 *
 * <p>State management strategy:
 * <ul>
 *   <li>Uses <code>combine</code> operator to merge 5 independent state flows</li>
 *   <li>Employs <code>stateIn</code> with <code>WhileSubscribed</code> caching strategy</li>
 *   <li>Maintains 5-second replay buffer to survive configuration changes</li>
 *   <li>Provides immutable state snapshots to prevent unintended mutations</li>
 * </ul></p>
 *
 * <p>Thread safety: All operations occur on ViewModel's coroutine scope, ensuring
 * sequential consistency for state updates.</p>
 *
 * @property repository Data access layer for habit operations (injected, defaults to in-memory implementation)
 * @constructor Creates HabitListViewModel with optional repository parameter
 * @see androidx.lifecycle.ViewModel for lifecycle ownership semantics
 * @see HabitRepository for data persistence layer
 */
class HabitListViewModel(
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    /**
     * Internal mutable state flow for search query text.
     *
     * <p>Updated whenever user types in search field. Triggers immediate re-filtering
     * of habits through the combined state flow. Empty string returns all habits.</p>
     *
     * <p>UI binding: Typically collected by TextField's onValueChange callback in composables.</p>
     *
     * @see onSearchQueryChange for public mutation method
     */
    private val _searchQuery = MutableStateFlow("")

    /**
     * Internal mutable state flow tracking the currently displayed week's start date.
     *
     * <p>Represents Monday of the week shown in the UI calendar/grid. Updated when user
     * navigates to previous or next week using arrow buttons.</p>
     *
     * <p>Initial value: Monday of current week based on system date.</p>
     *
     * @see navigateWeek for navigation logic
     * @see getWeekStart for week boundary calculation
     */
    private val _currentWeekStart = MutableStateFlow(getWeekStart(LocalDate.now()))

    /**
     * Internal mutable state flow controlling delete confirmation dialog visibility.
     *
     * <p>True when dialog should be displayed, false when hidden. Works in conjunction
     * with [_habitToDelete] to provide complete dialog context.</p>
     *
     * <p>Lifecycle: Set to true in [showDeleteDialog], reset to false in [hideDeleteDialog]
     * and after successful deletion in [deleteHabit].</p>
     *
     * @see showDeleteDialog
     * @see hideDeleteDialog
     */
    private val _showDeleteDialog = MutableStateFlow(false)

    /**
     * Internal mutable state flow holding the habit pending deletion.
     *
     * <p>Contains the Habit instance selected for deletion when confirmation dialog is active.
     * Null when no deletion is in progress or dialog is hidden.</p>
     *
     * <p>Used by dialog composable to display habit name and confirmation message.</p>
     *
     * @see showDeleteDialog
     * @see deleteHabit
     */
    private val _habitToDelete = MutableStateFlow<Habit?>(null)

    /**
     * Public read-only state flow exposing combined UI state to observers.
     *
     * <p>Aggregates five independent state sources:
     * <ol>
     *   <li>Repository habits (live data from persistence layer)</li>
     *   <li>Search query (user input filter)</li>
     *   <li>Current week start (navigation state)</li>
     *   <li>Delete dialog visibility flag</li>
     *   <li>Habit pending deletion</li>
     * </ol></p>
     *
     * <p>Transformation logic:
     * <ul>
     *   <li>Applies search filter to repository habits when query is non-empty</li>
     *   <li>Performs case-insensitive substring matching on habit names</li>
     *   <li>Constructs new HabitListUiState instance on every source emission</li>
     * </ul></p>
     *
     * <p>Sharing strategy: <code>WhileSubscribed(5000)</code> maintains state for 5 seconds
     * after last collector unsubscribes, optimizing for configuration changes while
     * preventing memory leaks during extended background periods.</p>
     *
     * <p>Typical consumption pattern:
     * <pre>
     * viewModel.uiState.collect { state ->
     *     // Update UI with state.habits, state.searchQuery, etc.
     * }
     * </pre></p>
     *
     * @return StateFlow emitting HabitListUiState snapshots on any state change
     * @see kotlinx.coroutines.flow.combine for multi-source aggregation
     * @see SharingStarted.WhileSubscribed for caching behavior
     */
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

    /**
     * Updates the search query filter and triggers habit list re-filtering.
     *
     * <p>Called by UI layer when user types in search field. Empty or blank query
     * returns all habits from repository. Non-empty query performs case-insensitive
     * substring matching on habit names.</p>
     *
     * <p>Performance: O(n) operation where n = habit count. Filtering occurs on every
     * keystroke, but repository data is in-memory so impact is minimal for typical
     * habit counts (<100).</p>
     *
     * @param query New search text from user input (may be empty)
     * @throws NullPointerException if query is null
     * @see _searchQuery for internal state storage
     * @see uiState for filtered results exposure
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Toggles completion status for a habit on a specific date, with date validation.
     *
     * <p>Business rule: Only allows toggling completion for the current system date.
     * Attempts to modify past or future dates are silently ignored (no-op).</p>
     *
     * <p>Rationale: Prevents accidental historical data corruption and maintains
     * data integrity for progress tracking statistics.</p>
     *
     * <p>Delegates to repository for actual state mutation, which emits updated
     * habit list through StateFlow pipeline.</p>
     *
     * @param habitId Unique identifier of habit to toggle
     * @param date Calendar date for completion status (only current date is accepted)
     * @throws NullPointerException if habitId or date parameters are null
     * @see ru.netology.habittracker.repository.HabitRepository.toggleHabitCompletion for persistence logic
     * @see LocalDate.now for current date comparison
     */
    fun toggleHabitCompletion(habitId: String, date: LocalDate) {
        // Только для сегодняшнего дня
        if (date == LocalDate.now()) {
            repository.toggleHabitCompletion(habitId, date)
        }
    }

    /**
     * Initiates delete confirmation dialog workflow for a specific habit.
     *
     * <p>Sets internal state to display delete dialog with the specified habit's details.
     * Typically triggered when user long-presses a habit item or clicks delete icon.</p>
     *
     * <p>State changes:
     * <ul>
     *   <li>_habitToDelete: Set to target habit instance</li>
     *   <li>_showDeleteDialog: Set to true</li>
     * </ul></p>
     *
     * <p>Dialog displays habit name and confirmation message. User can confirm deletion
     * via [deleteHabit] or cancel via [hideDeleteDialog].</p>
     *
     * @param habit Habit instance selected for potential deletion (must not be null)
     * @throws NullPointerException if habit parameter is null
     * @see hideDeleteDialog for cancellation path
     * @see deleteHabit for confirmation path
     */
    fun showDeleteDialog(habit: Habit) {
        _habitToDelete.value = habit
        _showDeleteDialog.value = true
    }

    /**
     * Cancels pending deletion and hides confirmation dialog.
     *
     * <p>Resets deletion-related state to neutral values. Called when user:
     * <ul>
     *   <li>Clicks "Cancel" button in delete dialog</li>
     *   <li>Dismisses dialog via back press</li>
     *   <li>Navigates away from screen</li>
     * </ul></p>
     *
     * <p>State changes:
     * <ul>
     *   <li>_showDeleteDialog: Set to false (hides dialog)</li>
     *   <li>_habitToDelete: Set to null (clears selection)</li>
     * </ul></p>
     *
     * @see showDeleteDialog for dialog initiation
     * @see deleteHabit for successful deletion path
     */
    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
        _habitToDelete.value = null
    }

    /**
     * Permanently removes a habit from the repository and closes deletion dialog.
     *
     * <p>Deletes habit by ID from persistent storage via repository layer. After successful
     * deletion, automatically resets dialog state by calling [hideDeleteDialog].</p>
     *
     * <p>Side effects:
     * <ul>
     *   <li>Repository emits updated habit list through StateFlow</li>
     *   <li>UI state automatically refreshes with filtered habits</li>
     *   <li>Delete dialog closes immediately</li>
     * </ul></p>
     *
     * <p>Irreversible: No undo mechanism is provided. Consider adding undo functionality
     * in production applications.</p>
     *
     * @param habitId Unique identifier of habit to delete (must not be null)
     * @throws NullPointerException if habitId parameter is null
     * @see ru.netology.habittracker.repository.HabitRepository.deleteHabit for persistence logic
     * @see hideDeleteDialog for state cleanup
     */
    fun deleteHabit(habitId: String) {
        repository.deleteHabit(habitId)
        hideDeleteDialog()
    }

    /**
     * Navigates the displayed week forward or backward by one week increment.
     *
     * <p>Updates [_currentWeekStart] to adjacent week's Monday. Positive direction
     * (forward=true) moves to next week, negative direction moves to previous week.</p>
     *
     * <p>Use case: User clicks left/right arrow buttons in week header to view
     * historical or future habit completion data.</p>
     *
     * <p>Calculation: Uses LocalDate.plusWeeks(1) and minusWeeks(1) for precise
     * week boundary navigation while preserving day-of-week alignment (Monday).</p>
     *
     * @param forward Direction flag: true for next week, false for previous week
     * @see _currentWeekStart for state storage
     * @see getWeekStart for week boundary calculation
     */
    fun navigateWeek(forward: Boolean) {
        _currentWeekStart.value = if (forward) {
            _currentWeekStart.value.plusWeeks(1)
        } else {
            _currentWeekStart.value.minusWeeks(1)
        }
    }

    /**
     * Companion object providing utility functions shared across the application.
     *
     * <p>Contains pure functions without dependencies on instance state, suitable for
     * static-style invocation from UI composables or other view models.</p>
     */
    companion object {
        /**
         * Calculates the Monday date for the week containing the specified date.
         *
         * <p>Implements ISO-8601 week definition where Monday is the first day of the week.
         * Returns the date of the Monday in the same week as the input date.</p>
         *
         * <p>Algorithm:
         * <ol>
         *   <li>Get day-of-week value (1=Monday, 7=Sunday)</li>
         *   <li>Subtract (dayOfWeek - 1) days to reach Monday</li>
         *   <li>Return resulting date</li>
         * </ol></p>
         *
         * <p>Examples:
         * <ul>
         *   <li>Input: Wednesday 2026-02-04 → Output: Monday 2026-02-02</li>
         *   <li>Input: Monday 2026-02-02 → Output: Monday 2026-02-02 (unchanged)</li>
         *   <li>Input: Sunday 2026-02-01 → Output: Monday 2026-01-26</li>
         * </ul></p>
         *
         * <p>Use cases:
         * <ul>
         *   <li>Initializing week grid views in UI</li>
         *   <li>Calculating week-based progress statistics</li>
         *   <li>Normalizing date inputs to week boundaries</li>
         * </ul></p>
         *
         * @param date Reference date for week calculation (must not be null)
         * @return LocalDate representing Monday of the week containing the input date
         * @throws NullPointerException if date parameter is null
         * @see java.time.DayOfWeek for day value semantics
         * @see LocalDate.minusDays for date arithmetic
         */
        fun getWeekStart(date: LocalDate): LocalDate {
            // Понедельник как начало недели
            val dayOfWeek = date.dayOfWeek.value
            return date.minusDays((dayOfWeek - 1).toLong())
        }
    }
}