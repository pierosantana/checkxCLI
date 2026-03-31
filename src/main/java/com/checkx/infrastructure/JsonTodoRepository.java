package com.checkx.infrastructure;

import com.checkx.domain.TodoTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JSON file-based persistence for TodoTasks.
 * Stores tasks in .checkx/todos.json
 */
public class JsonTodoRepository {

    private static final String DATA_DIR = System.getProperty("user.dir") + "/.checkx";
    private static final String DATA_FILE = DATA_DIR + "/todos.json";

    private final Gson gson;
    private List<TodoTask> tasks;

    public JsonTodoRepository() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        this.tasks = new ArrayList<>();
        loadTasks();
    }

    private void loadTasks() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            tasks = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<TodoTask>>(){}.getType();
            List<TodoTask> loaded = gson.fromJson(reader, listType);
            tasks = loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load todos from: " + DATA_FILE, e);
        }
    }

    private void saveTasks() {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save todos to: " + DATA_FILE, e);
        }
    }

    public void save(TodoTask task) {
        Optional<TodoTask> existing = findById(task.getId());
        if (existing.isPresent()) {
            int index = tasks.indexOf(existing.get());
            tasks.set(index, task);
        } else {
            tasks.add(task);
        }
        saveTasks();
    }

    public Optional<TodoTask> findById(String id) {
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    /**
     * Search tasks by partial text match (case-insensitive).
     * Only searches within visible dates (today and yesterday).
     */
    public List<TodoTask> searchByText(String query) {
        String lowerQuery = query.toLowerCase();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        return tasks.stream()
                .filter(t -> t.getDate().equals(today) || t.getDate().equals(yesterday))
                .filter(t -> t.getText().toLowerCase().contains(lowerQuery))
                .toList();
    }

    /**
     * Find exact match by text among visible tasks.
     */
    public Optional<TodoTask> findByText(String text) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        return tasks.stream()
                .filter(t -> t.getDate().equals(today) || t.getDate().equals(yesterday))
                .filter(t -> t.getText().equalsIgnoreCase(text))
                .findFirst();
    }

    /**
     * Get tasks for a specific date.
     */
    public List<TodoTask> findByDate(LocalDate date) {
        return tasks.stream()
                .filter(t -> t.getDate().equals(date))
                .toList();
    }

    /**
     * Get all tasks (today + yesterday).
     */
    public List<TodoTask> findVisible() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        return tasks.stream()
                .filter(t -> t.getDate().equals(today) || t.getDate().equals(yesterday))
                .toList();
    }

    public void delete(String id) {
        tasks.removeIf(t -> t.getId().equals(id));
        saveTasks();
    }
}
