package com.checkx.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Habit persistence.
 */
public interface HabitRepository {
    
    /**
     * Saves a habit.
     */
    void save(Habit habit);
    
    /**
     * Finds a habit by ID.
     */
    Optional<Habit> findById(String id);
    
    /**
     * Finds a habit by name (case-insensitive).
     */
    Optional<Habit> findByName(String name);
    
    /**
     * Gets all habits.
     */
    List<Habit> findAll();
    
    /**
     * Deletes a habit by ID.
     */
    void delete(String id);
    
    /**
     * Saves all habits at once.
     */
    void saveAll(List<Habit> habits);
}
