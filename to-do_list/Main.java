import gui.ToDoListGUI;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // установка системного стиля
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // запуск
        SwingUtilities.invokeLater(() -> {
            new ToDoListGUI();
        });
    }
}