import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

// アプリケーションのエントリポイント
public class ToDoApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ToDoController().startApp());
    }
}

// タスクモデル
class Task implements Serializable {
    private String title;
    private String deadline;
    private boolean isCompleted;

    public Task(String title, String deadline) {
        this.title = title;
        this.deadline = deadline;
        this.isCompleted = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void toggleCompleted() {
        this.isCompleted = !this.isCompleted;
    }
}

// データ管理クラス
class TaskRepository {
    private static final String DATA_FILE = "tasks.dat";

    public ArrayList<Task> loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (ArrayList<Task>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public void saveTasks(ArrayList<Task> tasks) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// コントローラクラス
class ToDoController {
    private final TaskRepository taskRepository;
    private final ToDoView view;
    private final ArrayList<Task> tasks;

    public ToDoController() {
        this.taskRepository = new TaskRepository();
        this.tasks = taskRepository.loadTasks();
        this.view = new ToDoView(this, tasks);
    }

    public void startApp() {
        view.createAndShowGUI();
    }

    public void addTask(String title, String deadline) {
        if (!title.isEmpty() && !deadline.isEmpty()) {
            Task task = new Task(title, deadline);
            tasks.add(task);
            view.updateTaskTable();
        } else {
            view.showError("タイトルと期限を入力してください！");
        }
    }

    public void editTask(int index, String newTitle, String newDeadline) {
        if (index >= 0 && index < tasks.size() && newTitle != null && newDeadline != null) {
            Task task = tasks.get(index);
            task.setTitle(newTitle);
            task.setDeadline(newDeadline);
            view.updateTaskTable();
        } else {
            view.showError("編集するタスクを正しく選択してください！");
        }
    }

    public void deleteTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
            view.updateTaskTable();
        } else {
            view.showError("削除するタスクを選択してください！");
        }
    }

    public void toggleTaskCompletion(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.get(index).toggleCompleted();
            view.updateTaskTable();
        }
    }

    public void saveTasksOnExit() {
        taskRepository.saveTasks(tasks);
    }
}

// ビュークラス
class ToDoView {
    private final ToDoController controller;
    private final ArrayList<Task> tasks;
    private DefaultTableModel tableModel;

    public ToDoView(ToDoController controller, ArrayList<Task> tasks) {
        this.controller = controller;
        this.tasks = tasks;
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("ToDoアプリ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // テーブルモデル
        String[] columnNames = {"完了", "タイトル", "期限"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 完了列のみ編集可能
            }
        };
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // 初期データ表示
        updateTaskTable();

        // フォームパネル
        JPanel formPanel = new JPanel(new FlowLayout());
        JTextField titleField = new JTextField(15);
        JTextField deadlineField = new JTextField(10);
        JButton addButton = new JButton("追加");
        formPanel.add(new JLabel("タイトル:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("期限:"));
        formPanel.add(deadlineField);
        formPanel.add(addButton);

        // ボタンパネル
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton editButton = new JButton("編集");
        JButton deleteButton = new JButton("削除");
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // イベントリスナー
        addButton.addActionListener(e -> controller.addTask(titleField.getText(), deadlineField.getText()));

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String newTitle = JOptionPane.showInputDialog("新しいタイトルを入力:", tasks.get(selectedRow).getTitle());
                String newDeadline = JOptionPane.showInputDialog("新しい期限を入力:", tasks.get(selectedRow).getDeadline());
                controller.editTask(selectedRow, newTitle, newDeadline);
            } else {
                showError("編集するタスクを選択してください！");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            controller.deleteTask(selectedRow);
        });

        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 0) {
                int row = e.getFirstRow();
                controller.toggleTaskCompletion(row);
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.saveTasksOnExit();
            }
        });

        // フレームにコンポーネントを追加
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(formPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public void updateTaskTable() {
        tableModel.setRowCount(0);
        for (Task task : tasks) {
            tableModel.addRow(new Object[]{task.isCompleted(), task.getTitle(), task.getDeadline()});
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(null, message);
    }
}
