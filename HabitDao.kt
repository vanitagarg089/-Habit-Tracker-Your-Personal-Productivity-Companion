package com.habittracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.habittracker.data.entity.Habit
import com.habittracker.data.entity.HabitCompletion

@Dao
interface HabitDao {

    // Habit CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits ORDER BY name ASC")
    fun getAllHabits(): LiveData<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    // Completion operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND completionDate = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)

    @Query("SELECT EXISTS(SELECT 1 FROM habit_completions WHERE habitId = :habitId AND completionDate = :date)")
    suspend fun isCompletedOnDate(habitId: Long, date: String): Boolean

    @Query("SELECT completionDate FROM habit_completions WHERE habitId = :habitId ORDER BY completionDate DESC")
    suspend fun getCompletionDates(habitId: Long): List<String>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completionDate = :date LIMIT 1")
    suspend fun getCompletionForDate(habitId: Long, date: String): HabitCompletion?

    @Query("""
        SELECT h.*, 
               CASE WHEN c.id IS NOT NULL THEN 1 ELSE 0 END as isCompletedToday
        FROM habits h
        LEFT JOIN habit_completions c ON h.id = c.habitId AND c.completionDate = :today
        ORDER BY isCompletedToday ASC, h.name ASC
    """)
    fun getHabitsWithTodayCompletion(today: String): LiveData<List<HabitRawResult>>

    @Query("SELECT COUNT(*) FROM habits")
    fun getTotalHabitsCount(): LiveData<Int>

    @Query("""
        SELECT COUNT(DISTINCT habitId) FROM habit_completions WHERE completionDate = :today
    """)
    fun getCompletedTodayCount(today: String): LiveData<Int>
}

data class HabitRawResult(
    val id: Long,
    val name: String,
    val description: String,
    val emoji: String,
    val colorHex: String,
    val createdDate: String,
    val streakCount: Int,
    val longestStreak: Int,
    val lastCompletedDate: String,
    val totalCompletions: Int,
    val isCompletedToday: Int // 0 or 1 from SQLite
)
