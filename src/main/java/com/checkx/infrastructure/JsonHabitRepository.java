package com.checkx.infrastructure;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JSON file-based implementation of HabitRepository.
 * Stores habits in ~/.checkx/habits.json
 */
public class JsonHabitRepository implements HabitRepository {
    
    private static final String DATA_DIR = System.getProperty("user.dir") + "/.checkx";
    private static final String DATA_FILE = DATA_DIR + "/habits.json";


    private final Gson gson;
    private List<Habit> habits;
    private LocalDate lastCheckedDate;

    public JsonHabitRepository() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        this.habits = new ArrayList<>();
        this.lastCheckedDate = LocalDate.now();
        ensureDataDirectoryExists();
        loadHabits();
    }

    private void ensureDataDirectoryExists() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory: " + DATA_DIR, e);
        }
    }

    private void loadHabits() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            habits = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Habit>>(){}.getType();
            List<Habit> loaded = gson.fromJson(reader, listType);
            habits = loaded != null ? loaded : new ArrayList<>();
            
            // Reset daily status for all habits if needed
            resetDailyStatusIfNeeded();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load habits from: " + DATA_FILE, e);
        }
    }

    private void saveHabits() {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            gson.toJson(habits, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save habits to: " + DATA_FILE, e);
        }
    }

    private void resetDailyStatusIfNeeded() {
        LocalDate today = LocalDate.now();
        habits.forEach(habit -> {
            if (habit.getLastCompletedDate() != null && 
                !habit.getLastCompletedDate().equals(today)) {
                habit.resetDailyStatus();
            }
        });
        saveHabits();
    }

    @Override
    public void save(Habit habit) {
        Optional<Habit> existing = findById(habit.getId());
        if (existing.isPresent()) {
            int index = habits.indexOf(existing.get());
            habits.set(index, habit);
        } else {
            habits.add(habit);
        }
        saveHabits();
    }

    @Override
    public Optional<Habit> findById(String id) {
        return habits.stream()
                .filter(h -> h.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Habit> findByName(String name) {
        return habits.stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Habit> searchByName(String query) {
        String lowerQuery = query.toLowerCase();
        return habits.stream()
                .filter(h -> h.getName().toLowerCase().contains(lowerQuery))
                .toList();
    }

    @Override
    public List<Habit> findAll() {
        checkDayChange();
        return new ArrayList<>(habits);
    }

    private void checkDayChange() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastCheckedDate)) {
            lastCheckedDate = today;
            resetDailyStatusIfNeeded();
        }
    }

    @Override
    public void delete(String id) {
        habits.removeIf(h -> h.getId().equals(id));
        saveHabits();
    }

    @Override
    public void saveAll(List<Habit> habits) {
        this.habits = new ArrayList<>(habits);
        saveHabits();
    }
}
