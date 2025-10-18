package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Priority {
        HIGH("Высокий", 3),
        MEDIUM("Средний", 2),
        LOW("Низкий", 1);

        private final String displayName;
        private final int level;

        Priority(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }

        public String getDisplayName() {
            return displayName;
        }
        public int getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // поля задачи
    private final int id;
    private String title;
    private LocalDate dueDate;
    private Priority priority;
    private boolean completed;
    private LocalDate createdAt;

    private static int nextId = 1;

    // создание новой задачи
    public Task(String title, LocalDate dueDate, Priority priority) {
        this.id = nextId++;
        this.title = title;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
        this.createdAt = LocalDate.now();
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public Priority getPriority() {
        return priority;
    }
    public boolean isCompleted() {
        return completed;
    }


    public void setTitle(String title) {
        this.title = title;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }


    public String getFormattedDueDate() {
        return dueDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }


    public boolean isOverdue() {
        return !completed && dueDate.isBefore(LocalDate.now());
    }


    public String getStatusText() {
        if (completed) {
            return "Выполнена";
        } else if (isOverdue()) {
            return "Просрочена";
        } else {
            return "В процессе";
        }
    }

    @Override
    public String toString() {
        return String.format("Задача %d: %s", id, title);
    }
}