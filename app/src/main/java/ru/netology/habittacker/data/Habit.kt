package ru.netology.habittacker.data

import java.time.LocalDate
import java.util.UUID

/**
 * Represents a user-defined habit with tracking capabilities for daily completion status.
 *
 * <p>Each habit maintains a unique identifier, a descriptive name, and a historical record
 * of completion status mapped by date. The class provides utility methods for querying
 * completion status on specific dates, generating weekly progress snapshots, and calculating
 * completion rates over a 7-day period.</p>
 *
 * <p>Immutable by design (using Kotlin data class semantics) to ensure thread-safe usage
 * in ViewModel and repository layers. Completion history follows value-object semantics
 * where each modification should produce a new instance.</p>
 *
 * @property id Unique identifier for the habit, generated automatically using UUID if not provided
 * @property name Human-readable description of the habit (e.g., "Morning jogging", "Drink water")
 * @property completionHistory Map of dates to completion status (true = completed, false = skipped).
 *                            Defaults to empty map for newly created habits.
 *
 * @constructor Creates a habit instance with optional parameters. UUID generation occurs at construction time.
 * @see DayProgress for weekly progress representation
 * @see java.time.LocalDate for date handling semantics
 */
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val completionHistory: Map<LocalDate, Boolean> = emptyMap()
) {
    /**
     * Determines whether the habit was completed on a specific date.
     *
     * <p>Queries the completion history map. Returns false for:
     * <ul>
     *   <li>Dates not present in the history map (treated as not completed)</li>
     *   <li>Dates explicitly mapped to false</li>
     * </ul></p>
     *
     * <p>Timezone-aware: Relies on system default timezone through LocalDate.now() usage elsewhere.
     * All date comparisons use LocalDate's natural ordering without time components.</p>
     *
     * @param date the calendar date to check (must not be null)
     * @return true if the habit was completed on the specified date; false otherwise
     * @throws NullPointerException if date parameter is null
     * @see completionHistory
     */
    fun isCompletedOn(date: LocalDate): Boolean {
        return completionHistory[date] ?: false
    }

    /**
     * Generates a 7-day progress snapshot starting from the specified start date.
     *
     * <p>Constructs a list of [DayProgress] objects representing consecutive days from
     * startDate (inclusive) through startDate + 6 days. Each entry contains:
     * <ul>
     *   <li>The actual calendar date</li>
     *   <li>Completion status for that date</li>
     *   <li>Whether the date matches the current system date</li>
     * </ul></p>
     *
     * <p>Use case: UI components (e.g., weekly calendar views) can render this list
     * with visual indicators for today's date and completion status.</p>
     *
     * @param startDate the first day of the 7-day period (must not be null)
     * @return List of exactly 7 DayProgress objects in chronological order
     * @throws NullPointerException if startDate parameter is null
     * @see DayProgress
     * @see LocalDate.plusDays
     */
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

    /**
     * Calculates the completion percentage for a 7-day period starting at startDate.
     *
     * <p>Computes the ratio of completed days to total days (7) in the specified week.
     * Formula: (completedDays / 7.0) * 100, returned as a float between 0.0 and 1.0.</p>
     *
     * <p>Edge cases:
     * <ul>
     *   <li>Empty progress list (should not occur with valid startDate) returns 0.0</li>
     *   <li>Partial weeks always calculate against full 7-day denominator</li>
     *   <li>Future dates without history entries count as incomplete (false)</li>
     * </ul></p>
     *
     * <p>Typical usage: Displaying progress bars or statistics in habit tracking UIs.</p>
     *
     * @param startDate the first day of the evaluation period (must not be null)
     * @return completion ratio as float (0.0 = 0%, 1.0 = 100%)
     * @throws NullPointerException if startDate parameter is null
     * @see getWeekProgress
     */
    fun getCompletionPercentage(startDate: LocalDate): Float {
        val weekProgress = getWeekProgress(startDate)
        val completedDays = weekProgress.count { it.isCompleted }
        return if (weekProgress.isEmpty()) 0f else completedDays.toFloat() / 7f
    }
}

/**
 * Immutable value object representing a single day's habit tracking state within a weekly view.
 *
 * <p>Designed for UI consumption in RecyclerView adapters or composables where each item
 * represents one day in a weekly calendar grid. Contains all necessary rendering data
 * without requiring additional lookups.</p>
 *
 * @property date The calendar date this progress entry represents (e.g., 2026-02-01)
 * @property isCompleted Whether the associated habit was completed on this date
 * @property isToday Whether this date matches the current system date (for visual highlighting)
 *
 * @constructor Creates a DayProgress instance with all properties required for rendering
 * @see Habit.getWeekProgress for creation context
 */
data class DayProgress(
    val date: LocalDate,
    val isCompleted: Boolean,
    val isToday: Boolean
)