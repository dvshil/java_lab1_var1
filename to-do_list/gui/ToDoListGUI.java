package gui;

import model.Task;
import manager.TaskManager;
import gui.styles.Colors;
import gui.styles.Fonts;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Comparator;

// настройка графического интерфейса
public class ToDoListGUI extends JFrame {
    private TaskManager taskManager;
    private JTable tasksTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton sortDateButton, sortStatusButton, sortPriorityButton;
    private JButton deleteButton;
    private JPanel statsPanel;

    private final String[] COLUMN_NAMES = {"", "СТАТУС", "ЗАДАЧА", "СРОК", "ПРИОРИТЕТ"};

    public ToDoListGUI() {
        this.taskManager = new TaskManager();
        initializeGUI();
        refreshTasksTable();
    }

    private void initializeGUI() {
        setTitle("To-Do List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 1000);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        setupComponents();
        applyStyles();
        setVisible(true);
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Colors.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        mainPanel.add(createTablePanel(), BorderLayout.CENTER);

        mainPanel.add(createControlPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(Colors.BACKGROUND);

        JLabel titleLabel = new JLabel("TO-DO LIST");
        titleLabel.setFont(Fonts.getTitleFont());
        titleLabel.setForeground(Colors.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel searchSortPanel = new JPanel(new BorderLayout(10, 0));
        searchSortPanel.setBackground(Colors.BACKGROUND);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(Colors.BACKGROUND);

        searchField = new JTextField();
        searchField.setFont(Fonts.getInputFont());
        searchField.setBackground(Colors.INPUT_BACKGROUND);
        searchField.setForeground(Colors.TEXT_DARK);
        searchField.setCaretColor(Colors.TEXT_DARK);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Поиск задач...");

        JButton searchButton = createStyledButton("Поиск", Colors.SECONDARY, 100, 40);
        searchButton.addActionListener(this::performSearch);

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sortPanel.setBackground(Colors.BACKGROUND);

        sortDateButton = createStyledButton("По сроку", Colors.PRIMARY, 100, 40);
        sortStatusButton = createStyledButton("По статусу", Colors.SUCCESS, 100, 40);
        sortPriorityButton = createStyledButton("По важности", Colors.WARNING, 110, 40);

        sortDateButton.addActionListener(e -> sortByDate());
        sortStatusButton.addActionListener(e -> sortByStatus());
        sortPriorityButton.addActionListener(e -> sortByPriority());

        sortPanel.add(createSortLabel("Сортировать:"));
        sortPanel.add(sortDateButton);
        sortPanel.add(sortStatusButton);
        sortPanel.add(sortPriorityButton);

        searchSortPanel.add(searchPanel, BorderLayout.CENTER);
        searchSortPanel.add(sortPanel, BorderLayout.EAST);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(searchSortPanel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JLabel createSortLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.getBoldFont());
        label.setForeground(Colors.TEXT_DARK);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        return label;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBackground(Colors.BACKGROUND);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JLabel tableTitle = new JLabel("СПИСОК ЗАДАЧ");
        tableTitle.setFont(Fonts.getHeaderFont());
        tableTitle.setForeground(Colors.TEXT_DARK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }
        };

        tasksTable = new JTable(tableModel);
        tasksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tasksTable.setRowHeight(50);
        tasksTable.setFont(Fonts.getTableFont());
        tasksTable.setForeground(Colors.TEXT_DARK);
        tasksTable.setBackground(Colors.CARD_BACKGROUND);
        tasksTable.getTableHeader().setFont(Fonts.getBoldFont());
        tasksTable.getTableHeader().setBackground(Colors.PRIMARY);
        tasksTable.getTableHeader().setForeground(Colors.TEXT_ON_DARK);
        tasksTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tasksTable.setShowGrid(false);
        tasksTable.setIntercellSpacing(new Dimension(0, 0));
        tasksTable.setBorder(BorderFactory.createLineBorder(Colors.BORDER_LIGHT, 1));

        setupTableRenderer();

        // изменение чекбокса (отметить как выполненную или нет)
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 0) {
                int row = e.getFirstRow();
                Boolean completed = (Boolean) tableModel.getValueAt(row, 0);
                Task task = getTaskAtRow(row);
                if (task != null) {
                    task.setCompleted(completed);
                    taskManager.updateTask(task);
                    refreshTasksTable();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tasksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Colors.BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(Colors.CARD_BACKGROUND);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private void setupTableRenderer() {
        tasksTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Point mousePosition = table.getMousePosition();
                boolean isHover = false;
                if (mousePosition != null) {
                    int hoverRow = table.rowAtPoint(mousePosition);
                    isHover = (hoverRow == row);
                }

                Task task = getTaskAtRow(row);
                boolean isCompleted = task != null && task.isCompleted();

                if (isSelected) {
                    c.setBackground(Colors.HOVER_BACKGROUND);
                    c.setForeground(Colors.HOVER_TEXT);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (isHover && !isCompleted) {
                    c.setBackground(Colors.HOVER_ROW_BACKGROUND);
                    c.setForeground(Colors.TEXT_DARK);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (isHover && isCompleted) {
                    c.setBackground(new Color(240, 240, 240));
                    c.setForeground(Colors.TEXT_DARK);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setBackground(row % 2 == 0 ? Colors.CARD_BACKGROUND : new Color(250, 250, 250));
                    c.setForeground(Colors.TEXT_DARK);
                    setFont(getFont().deriveFont(Font.BOLD));
                }

                setFont(Fonts.getTableFont());
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                if (column == 1 && value != null) {
                    String status = value.toString();
                    if (isSelected) {
                        setForeground(Colors.TEXT_ON_DARK);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (isHover && !isCompleted) {
                        setForeground(Colors.TEXT_ON_DARK);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (isHover && isCompleted && status.contains("Выполнена")) {
                        setForeground(Colors.STATUS_COMPLETED);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (isHover && isCompleted && status.contains("Просрочена")) {
                        setForeground(Colors.STATUS_OVERDUE);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (isHover && isCompleted && status.contains("В процессе")) {
                        setForeground(Colors.TEXT_LIGHT);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.contains("Выполнена")) {
                        setForeground(Colors.STATUS_COMPLETED);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.contains("Просрочена")) {
                        setForeground(Colors.STATUS_OVERDUE);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.contains("В процессе")) {
                        setForeground(Colors.TEXT_LIGHT);
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }

                if (column == 4 && value != null) {
                    String priority = value.toString();
                    if (isSelected) {
                        setForeground(Colors.TEXT_ON_DARK);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (isHover && !isCompleted) {
                        setForeground(Colors.TEXT_ON_DARK);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (isHover && isCompleted && priority.contains("Высокий")) {
                        setForeground(Colors.STATUS_HIGH);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (isHover && isCompleted && priority.contains("Средний")) {
                        setForeground(Colors.STATUS_MEDIUM);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (isHover && isCompleted && priority.contains("Низкий")) {
                        setForeground(Colors.STATUS_LOW);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (priority.contains("Высокий")) {
                        setForeground(Colors.STATUS_HIGH);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (priority.contains("Средний")) {
                        setForeground(Colors.STATUS_MEDIUM);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (priority.contains("Низкий")) {
                        setForeground(Colors.STATUS_LOW);
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }

                return c;
            }
        });

        tasksTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            private JCheckBox checkBox = new JCheckBox();

            {
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                checkBox.setBackground(Colors.CARD_BACKGROUND);
                checkBox.setForeground(Colors.CHECKBOX_COLOR);

                Icon defaultIcon = createCheckBoxIcon(false, false, false);
                Icon selectedIcon = createCheckBoxIcon(true, false, false);
                Icon rolloverIcon = createCheckBoxIcon(false, true, false);
                Icon selectedRolloverIcon = createCheckBoxIcon(true, true, false);
                Icon selectedWhiteIcon = createCheckBoxIcon(true, false, true);

                checkBox.setIcon(defaultIcon);
                checkBox.setSelectedIcon(selectedIcon);
                checkBox.setRolloverIcon(rolloverIcon);
                checkBox.setRolloverSelectedIcon(selectedRolloverIcon);
            }

            // кастомизация чекбокса
            private Icon createCheckBoxIcon(boolean selected, boolean hover, boolean white) {
                return new Icon() {
                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        Color borderColor;
                        if (white) {
                            borderColor = Color.WHITE;
                        } else if (hover) {
                            borderColor = Colors.CHECKBOX_HOVER_COLOR;
                        } else {
                            borderColor = Colors.CHECKBOX_COLOR;
                        }

                        g2.setColor(borderColor);
                        g2.drawRoundRect(x + 1, y + 1, 12, 12, 2, 2);

                        if (selected) {
                            g2.setStroke(new BasicStroke(2f));
                            if (white) {
                                g2.setColor(Color.WHITE);
                            } else {
                                g2.setColor(borderColor);
                            }
                            g2.drawLine(x + 3, y + 6, x + 5, y + 9);
                            g2.drawLine(x + 5, y + 9, x + 10, y + 4);
                        }

                        g2.dispose();
                    }

                    @Override
                    public int getIconWidth() { return 16; }

                    @Override
                    public int getIconHeight() { return 16; }
                };
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Point mousePosition = table.getMousePosition();
                boolean isHover = false;
                if (mousePosition != null) {
                    int hoverRow = table.rowAtPoint(mousePosition);
                    isHover = (hoverRow == row);
                }

                if (isSelected) {
                    checkBox.setBackground(Colors.HOVER_BACKGROUND);
                    checkBox.setIcon(createCheckBoxIcon(false, false, true));
                    checkBox.setSelectedIcon(createCheckBoxIcon(true, false, true));
                } else if (isHover) {
                    checkBox.setBackground(Colors.HOVER_ROW_BACKGROUND);
                    checkBox.setIcon(createCheckBoxIcon(false, true, false));
                    checkBox.setSelectedIcon(createCheckBoxIcon(true, true, false));
                } else {
                    checkBox.setBackground(row % 2 == 0 ? Colors.CARD_BACKGROUND : new Color(250, 250, 250));
                    checkBox.setIcon(createCheckBoxIcon(false, false, false));
                    checkBox.setSelectedIcon(createCheckBoxIcon(true, false, false));
                }

                checkBox.setSelected(value != null && (Boolean) value);
                return checkBox;
            }
        });

        tasksTable.getTableHeader().setForeground(Colors.TEXT_DARK);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout(15, 0));
        controlPanel.setBackground(Colors.BACKGROUND);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionPanel.setBackground(Colors.BACKGROUND);

        JButton addButton = createStyledButton("НОВАЯ ЗАДАЧА", Colors.SUCCESS, 150, 45);
        JButton editButton = createStyledButton("РЕДАКТИРОВАТЬ", Colors.WARNING, 150, 45);
        deleteButton = createStyledButton("УДАЛИТЬ", Colors.DANGER, 120, 45);

        addButton.addActionListener(e -> addNewTask());
        editButton.addActionListener(e -> editSelectedTask());
        deleteButton.addActionListener(e -> deleteSelectedTask());

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        statsPanel = createStatsPanel();

        controlPanel.add(actionPanel, BorderLayout.WEST);
        controlPanel.add(statsPanel, BorderLayout.EAST);

        return controlPanel;
    }

    // статистика по задачам
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setBackground(Colors.BACKGROUND);

        int totalTasks = taskManager.getAllTasks().size();
        int completedTasks = taskManager.getTasksByCompletion(true).size();
        int pendingTasks = totalTasks - completedTasks;

        JLabel statsLabel = new JLabel(String.format(
                "<html><div style='text-align: right; color: %s;'>" +
                        "Всего задач: <b>%d</b><br>" +
                        "Выполнено: <b style='color: %s;'>%d</b><br>" +
                        "Осталось: <b style='color: %s;'>%d</b>" +
                        "</div></html>",
                "#3B3534", totalTasks, "#4B6C45", completedTasks, "#8A9B83", pendingTasks
        ));

        statsLabel.setFont(Fonts.getPrimaryFont());
        statsPanel.add(statsLabel);

        return statsPanel;
    }

    private void updateStatsPanel(JPanel statsPanel) {
        statsPanel.removeAll();

        int totalTasks = taskManager.getAllTasks().size();
        int completedTasks = taskManager.getTasksByCompletion(true).size();
        int pendingTasks = totalTasks - completedTasks;

        JLabel statsLabel = new JLabel(String.format(
                "<html><div style='text-align: right; color: %s;'>" +
                        "Всего задач: <b>%d</b><br>" +
                        "Выполнено: <b style='color: %s;'>%d</b><br>" +
                        "Осталось: <b style='color: %s;'>%d</b>" +
                        "</div></html>",
                "#3B3534", totalTasks, "#4B6C45", completedTasks, "#8A9B83", pendingTasks
        ));

        statsLabel.setFont(Fonts.getPrimaryFont());
        statsPanel.add(statsLabel);

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JButton createStyledButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(color.darker().darker());
                } else if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(Colors.TEXT_ON_DARK);
                g2.setFont(Fonts.getButtonFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void applyStyles() {
        getContentPane().setBackground(Colors.BACKGROUND);
    }

    private void refreshTasksTable() {
        tableModel.setRowCount(0);
        List<Task> tasks = taskManager.getAllTasks();
        for (Task task : tasks) {
            addTaskToTable(task);
        }
        updateStatsPanel(statsPanel);
        revalidate();
        repaint();
    }

    private void addTaskToTable(Task task) {
        Object[] row = {
                task.isCompleted(),
                task.getStatusText(),
                task.getTitle(),
                task.getFormattedDueDate(),
                task.getPriority().getDisplayName()
        };
        tableModel.addRow(row);
    }

    private Task getTaskAtRow(int row) {
        List<Task> allTasks = taskManager.getAllTasks();
        if (row >= 0 && row < allTasks.size()) {
            return allTasks.get(row);
        }
        return null;
    }

    private Task getSelectedTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) return null;
        return getTaskAtRow(selectedRow);
    }

    // сортировки
    private void sortByDate() {
        List<Task> sortedTasks = taskManager.getTasksSortedByDate();
        displaySortedTasks(sortedTasks, "отсортированы по сроку выполнения (ближайшие → долгосрочные)");
    }

    private void sortByStatus() {
        List<Task> sortedTasks = taskManager.getAllTasks().stream()
                .sorted(Comparator.comparing(Task::isCompleted))
                .toList();

        displaySortedTasks(sortedTasks, "отсортированы по статусу (в процессе → выполненные)");
    }

    private void sortByPriority() {
        List<Task> sortedTasks = taskManager.getAllTasks().stream()
                .sorted((t1, t2) -> Integer.compare(t2.getPriority().getLevel(), t1.getPriority().getLevel()))
                .toList();

        displaySortedTasks(sortedTasks, "отсортированы по приоритету (высокий → низкий)");
    }

    private void displaySortedTasks(List<Task> tasks, String message) {
        tableModel.setRowCount(0);
        for (Task task : tasks) {
            addTaskToTable(task);
        }
        showMessage("Задачи " + message, "Сортировка");
    }

    private void addNewTask() {
        TaskDialog dialog = new TaskDialog(this);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            taskManager.addTask(dialog.getTask());
            refreshTasksTable();
            showMessage("Задача успешно добавлена!", "Успех");
        }
    }

    private void editSelectedTask() {
        Task selectedTask = getSelectedTask();
        if (selectedTask == null) {
            showMessage("Пожалуйста, выберите задачу для редактирования!", "Внимание");
            return;
        }

        TaskDialog dialog = new TaskDialog(this, selectedTask);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            taskManager.updateTask(dialog.getTask());
            refreshTasksTable();
            showMessage("Задача успешно обновлена!", "Успех");
        }
    }

    private void deleteSelectedTask() {
        Task selectedTask = getSelectedTask();
        if (selectedTask == null) {
            showMessage("Пожалуйста, выберите задачу для удаления!", "Внимание");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить задачу: '" + selectedTask.getTitle() + "'?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            taskManager.deleteTask(selectedTask.getId());
            refreshTasksTable();
            showMessage("Задача успешно удалена!", "Успех");
        }
    }

    private void performSearch(ActionEvent e) {
        String keyword = searchField.getText().trim();
        List<Task> foundTasks = taskManager.searchTasks(keyword);

        tableModel.setRowCount(0);
        for (Task task : foundTasks) {
            addTaskToTable(task);
        }

        if (foundTasks.isEmpty() && !keyword.isEmpty()) {
            showMessage("Задачи по запросу '" + keyword + "' не найдены", "Результаты поиска");
        } else if (!keyword.isEmpty()) {
            showMessage("Найдено задач: " + foundTasks.size(), "Результаты поиска");
        }
    }

    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}