package ru.netology.habitttacker.repository


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.habitttacker.data.Habit
import java.time.LocalDate

/**
 * In-memory repository for managing habit tracking data with reactive state propagation.
 *
 * <p>Provides a single source of truth for habit-related operations including CRUD functionality,
 * completion toggling, and search capabilities. Internally uses Kotlin coroutines StateFlow
 * to enable reactive UI updates - any modification to habits automatically notifies observers
 * (typically ViewModels or composables) without manual callback registration.</p>
 *
 * <p>Implementation characteristics:
 * <ul>
 *   <li><strong>In-memory only:</strong> Data persists only during application runtime.
 *       Production implementations should wrap this with persistent storage (Room, DataStore).</li>
 *   <li><strong>Thread-safe for coroutines:</strong> StateFlow operations are thread-safe,
 *       but this implementation assumes single-threaded coroutine dispatcher usage.</li>
 *   <li><strong>Immutable updates:</strong> All modifications create new list instances
 *       to maintain StateFlow's referential transparency requirements.</li>
 * </ul></p>
 *
 * <p>Typical architecture placement: Repository layer in MVVM/MVI patterns, consumed by ViewModel.</p>
 *
 * @see Habit for domain model documentation
 * @see kotlinx.coroutines.flow.StateFlow for reactive stream semantics
 * @see ru.netology.habitttacker.viewmodel.HabitViewModel for typical consumer pattern
 */
class HabitRepository {
    /**
     * Mutable backing property for habit state updates.
     *
     * <p>Package-private mutable flow that allows internal state modifications.
     * Direct external access is prohibited by Kotlin visibility rules - consumers
     * must use the read-only [habits] property to observe state changes.</p>
     *
     * <p>Initial state is an empty list, representing no habits created at startup.</p>
     *
     * @see habits for public read-only exposure
     */
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())

    /**
     * Public read-only state flow exposing current habit collection to observers.
     *
     * <p>Emits new list instances on every mutation (add/update/delete/toggle). Observers
     * should collect this flow in coroutines (typically in ViewModel's init block or
     * composable's LaunchedEffect) to receive real-time updates.</p>
     *
     * <p>Guarantees:
     * <ul>
     *   <li>Always emits initial value immediately upon collection</li>
     *   <li>Emits distinct consecutive values (though this implementation always creates
     *       new list instances, so all emissions are distinct by reference)</li>
     *   <li>Never throws exceptions during normal operation</li>
     * </ul></p>
     *
     * @return StateFlow that emits the current list of habits on every state change
     * @see MutableStateFlow.asStateFlow for exposure pattern rationale
     */
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    /**
     * Adds a new habit to the repository's collection.
     *
     * <p>Performs immutable update by creating a new list containing all existing habits
     * plus the new habit instance. Preserves insertion order with new habit appended to end.</p>
     *
     * <p>Side effects:
     * <ul>
     *   <li>Triggers emission on [habits] StateFlow with updated list</li>
     *   <li>Does not validate habit uniqueness by name - allows duplicate names</li>
     *   <li>Uses habit's existing ID; does not regenerate UUID</li>
     * </ul></p>
     *
     * @param habit the fully constructed Habit instance to add (must not be null)
     * @throws NullPointerException if habit parameter is null
     * @see Habit for construction requirements
     */
    fun addHabit(habit: Habit) {
        _habits.value = _habits.value + habit
    }

    /**
     * Replaces an existing habit with a modified version using matching ID.
     *
     * <p>Performs identity-based replacement: finds habit with matching [Habit.id] and
     * substitutes it with the provided instance. All other habits remain unchanged
     * and retain their original list positions.</p>
     *
     * <p>Behavioral notes:
     * <ul>
     *   <li>If no habit matches the ID, collection remains unchanged</li>
     *   <li>Replacement is reference-based - new instance must have identical ID</li>
     *   <li>Does not merge partial updates; replaces entire habit object</li>
     * </ul></p>
     *
     * @param habit the updated Habit instance containing modified properties (must not be null)
     * @throws NullPointerException if habit parameter is null
     * @see Habit.copy for creating modified instances with preserved ID
     */
    fun updateHabit(habit: Habit) {
        _habits.value = _habits.value.map {
            if (it.id == habit.id) habit else it
        }
    }

    /**
     * Removes a habit from the collection by its unique identifier.
     *
     * <p>Performs immutable filter operation that excludes the habit with matching ID.
     * Preserves order of remaining habits.</p>
     *
     * <p>Edge cases:
     * <ul>
     *   <li>ID not found: collection remains unchanged, no error thrown</li>
     *   <li>Empty collection: remains empty after operation</li>
     *   <li>Multiple habits with same ID (invalid state): removes all matches</li>
     * </ul></p>
     *
     * @param habitId unique identifier of habit to remove (must not be null or empty)
     * @throws NullPointerException if habitId parameter is null
     * @see Habit.id for identifier semantics
     */
    fun deleteHabit(habitId: String) {
        _habits.value = _habits.value.filter { it.id != habitId }
    }

    /**
     * Toggles completion status for a specific habit on a given date.
     *
     * <p>Implements idempotent toggle logic:
     * <ul>
     *   <li>If no record exists for date: creates entry with value=true (completed)</li>
     *   <li>If record exists with true: flips to false (incomplete)</li>
     *   <li>If record exists with false: flips to true (completed)</li>
     * </ul></p>
     *
     * <p>Implementation details:
     * <ul>
     *   <li>Creates mutable copy of completion history map to modify state</li>
     *   <li>Uses [Habit.copy] to produce new habit instance with updated history</li>
     *   <li>Performs identity-based lookup using habit ID</li>
     *   <li>Does not remove entries when toggling to false (preserves explicit skip records)</li>
     * </ul></p>
     *
     * @param habitId unique identifier of target habit (must not be null)
     * @param date calendar date for which to toggle completion status (must not be null)
     * @throws NullPointerException if habitId or date parameters are null
     * @see Habit.completionHistory for storage semantics
     * @see Habit.isCompletedOn for query counterpart
     */
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

    /**
     * Filters habits by case-insensitive substring match on habit name.
     *
     * <p>Performs client-side filtering on in-memory collection. Returns new list instance
     * containing only habits whose [Habit.name] contains the query string.</p>
     *
     * <p>Search behavior:
     * <ul>
     *   <li>Blank/empty query: returns full unfiltered collection</li>
     *   <li>Case-insensitive: "run" matches "Morning Run" and "RUNNING"</li>
     *   <li>Substring match: "water" matches "Drink water daily"</li>
     *   <li>No fuzzy matching or ranking: binary inclusion/exclusion only</li>
     * </ul></p>
     *
     * <p>Performance note: O(n) operation where n = habit count. Suitable for small datasets
     * (<1000 habits). For larger datasets, consider indexing or database-backed search.</p>
     *
     * @param query search term to match against habit names (may be null or empty)
     * @return new list containing matching habits; empty list if no matches found
     * @see Habit.name for searchable property
     */
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