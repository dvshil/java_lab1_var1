package manager;

import model.Task;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {
    private List<Task> tasks;
    private static final String FILE_NAME = "tasks.dat";

    public TaskManager() {
        tasks = new ArrayList<>();
        loadTasksFromFile();
    }

    public void addTask(Task task) {
        tasks.add(task);
        saveTasksToFile();
    }

    public boolean deleteTask(int id) {
        Task taskToRemove = findTaskById(id);
        if (taskToRemove != null) {
            tasks.remove(taskToRemove);
            saveTasksToFile();
            return true;
        }
        return false;
    }

    public boolean updateTask(Task updatedTask) {
        Task existingTask = findTaskById(updatedTask.getId());
        if (existingTask != null) {
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDueDate(updatedTask.getDueDate());
            existingTask.setPriority(updatedTask.getPriority());
            existingTask.setCompleted(updatedTask.isCompleted());
            saveTasksToFile();
            return true;
        }
        return false;
    }

    public Task findTaskById(int id) {
        return tasks.stream()
                .filter(task -> task.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public List<Task> getTasksByCompletion(boolean completed) {
        return tasks.stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByPriority(Task.Priority priority) {
        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    public List<Task> getTasksSortedByDate() {
        return tasks.stream()
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Task> searchTasks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllTasks();

        String lowerKeyword = keyword.toLowerCase();
        return tasks.stream()
                .filter(task -> task.getTitle().toLowerCase().contains(lowerKeyword)) // ← ТОЛЬКО ПОИСК ПО НАЗВАНИЮ
                .collect(Collectors.toList());
    }

    private void saveTasksToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTasksFromFile() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
                tasks = (List<Task>) ois.readObject();
                // присваивание уникального айдишника новой задаче
                int maxId = tasks.stream().mapToInt(Task::getId).max().orElse(0);
                try {
                    java.lang.reflect.Field nextIdField = Task.class.getDeclaredField("nextId");
                    nextIdField.setAccessible(true);
                    nextIdField.set(null, maxId + 1);
                } catch (Exception e) {
                    System.err.println("Ошибка обновления nextId: " + e.getMessage());
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Ошибка загрузки: " + e.getMessage());
                tasks = new ArrayList<>();
            }
        }
    }
}