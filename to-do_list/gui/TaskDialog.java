package gui;

import model.Task;
import gui.styles.Colors;
import gui.styles.Fonts;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TaskDialog extends JDialog {
    private Task task;
    private boolean saved = false;
    private JTextField titleField;
    private JTextField dateField;
    private JComboBox<Task.Priority> priorityCombo;

    public TaskDialog(Frame parent) {
        this(parent, null);
    }
    // создание + редактирование
    public TaskDialog(Frame parent, Task task) {
        super(parent, true);
        this.task = task;
        initializeComponents();
        setupLayout();
        setupListeners();
        setTitle(task == null ? "СОЗДАНИЕ НОВОЙ ЗАДАЧИ" : "РЕДАКТИРОВАНИЕ ЗАДАЧИ");
        setSize(500, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // создание элементов
    private void initializeComponents() {
        // название
        titleField = new JTextField(30);
        titleField.setFont(Fonts.getInputFont());
        if (task != null) titleField.setText(task.getTitle());

        // дата
        dateField = new JTextField(10);
        dateField.setFont(Fonts.getInputFont());
        if (task != null) {
            dateField.setText(task.getFormattedDueDate());
        } else {
            dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }

        // приоритет
        priorityCombo = new JComboBox<>(Task.Priority.values());
        priorityCombo.setFont(Fonts.getInputFont());
        if (task != null) {
            priorityCombo.setSelectedItem(task.getPriority());
        }

        applyStyles();
    }

    private void applyStyles() {
        Component[] components = {titleField, dateField};
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                ((JTextField) comp).setBackground(Colors.INPUT_BACKGROUND);
                ((JTextField) comp).setForeground(Colors.TEXT_DARK);
                ((JTextField) comp).setCaretColor(Colors.TEXT_DARK);
                ((JTextField) comp).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.BORDER_LIGHT, 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        }

        priorityCombo.setBackground(Colors.INPUT_BACKGROUND);
        priorityCombo.setForeground(Colors.TEXT_DARK);
        priorityCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void setupLayout() {
        getContentPane().setBackground(Colors.BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        // Основная панель с полями
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Colors.BACKGROUND);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 12, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(createLabel("Название задачи:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        fieldsPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        fieldsPanel.add(createLabel("Срок выполнения:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0;
        fieldsPanel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        fieldsPanel.add(createLabel("Приоритет:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0;
        fieldsPanel.add(priorityCombo, gbc);

        add(fieldsPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.getBoldFont());
        label.setForeground(Colors.TEXT_LIGHT);
        return label;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Colors.BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));

        JButton saveButton = createStyledButton("СОХРАНИТЬ", Colors.SUCCESS, 120, 40);
        JButton cancelButton = createStyledButton("ОТМЕНА", Colors.DANGER, 120, 40);

        saveButton.addActionListener(e -> saveTask());
        cancelButton.addActionListener(e -> dispose());

        // горячие клавиши
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
                e -> saveTask(),
                KeyStroke.getKeyStroke("ENTER"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    // настройка вида кнопок
    private JButton createStyledButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
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
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        return button;
    }

    private void setupListeners() {
        titleField.addActionListener(e -> saveTask());
        dateField.addActionListener(e -> saveTask());
    }

    private void saveTask() {
        if (titleField.getText().trim().isEmpty()) {
            showError("Введите название задачи!");
            titleField.requestFocus();
            return;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dateField.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e) {
            showError("Неверный формат даты!\nИспользуйте формат: дд.мм.гггг");
            dateField.requestFocus();
            return;
        }

        if (task == null) {
            task = new Task(
                    titleField.getText().trim(),
                    dueDate,
                    (Task.Priority) priorityCombo.getSelectedItem()
            );
        } else {
            task.setTitle(titleField.getText().trim());
            task.setDueDate(dueDate);
            task.setPriority((Task.Priority) priorityCombo.getSelectedItem());
        }

        saved = true;
        dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public Task getTask() {
        return saved ? task : null;
    }

    public boolean isSaved() {
        return saved;
    }
}