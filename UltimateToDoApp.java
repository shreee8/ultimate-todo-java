import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Task implements Serializable {
    private String title;
    private String description;
    private String dueDate;
    private String priority; // High, Medium, Low
    private boolean completed;

    public Task(String title, String description, String dueDate, String priority) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public boolean isCompleted() { return completed; }
    public void toggleCompleted() { completed = !completed; }

    @Override
    public String toString() {
        return title + " | " + dueDate + " | " + priority + " | " + (completed ? "Done" : "Pending");
    }
}

class TaskRenderer extends JCheckBox implements ListCellRenderer<Task> {
    public Component getListCellRendererComponent(JList<? extends Task> list, Task task, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        setText(task.toString());
        setSelected(task.isCompleted());

        switch (task.getPriority().toLowerCase()) {
            case "high": setForeground(Color.RED); break;
            case "medium": setForeground(Color.ORANGE); break;
            case "low": setForeground(Color.GREEN.darker()); break;
            default: setForeground(Color.BLACK);
        }

        setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
        return this;
    }
}

public class UltimateToDoApp extends JFrame {
    private DefaultListModel<Task> taskListModel = new DefaultListModel<>();
    private JList<Task> taskList = new JList<>(taskListModel);
    private JTextField titleField = new JTextField(10);
    private JTextField dueDateField = new JTextField(8);
    private JTextArea descriptionArea = new JTextArea(3, 20);
    private JComboBox<String> priorityBox = new JComboBox<>(new String[]{"High", "Medium", "Low"});
    private JTextField searchField = new JTextField(10);
    private JLabel statsLabel = new JLabel("Tasks: 0 | Completed: 0");

    public UltimateToDoApp() {
        setTitle("Ultimate To-Do List ✨");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        taskList.setCellRenderer(new TaskRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(taskList);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Due Date (optional):"));
        inputPanel.add(dueDateField);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityBox);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(new JScrollPane(descriptionArea));
        inputPanel.add(new JLabel("Search:"));
        inputPanel.add(searchField);

        JButton addBtn = new JButton("Add Task");
        JButton deleteBtn = new JButton("Delete");
        JButton toggleBtn = new JButton("Toggle Complete");
        JButton sortBtn = new JButton("Sort by Priority");
        JButton clearBtn = new JButton("Clear All");
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addBtn); buttonPanel.add(toggleBtn); buttonPanel.add(deleteBtn);
        buttonPanel.add(sortBtn); buttonPanel.add(clearBtn);
        buttonPanel.add(saveBtn); buttonPanel.add(loadBtn);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statsLabel, BorderLayout.WEST);

        addBtn.addActionListener(e -> addTask());
        deleteBtn.addActionListener(e -> deleteTask());
        toggleBtn.addActionListener(e -> toggleTask());
        sortBtn.addActionListener(e -> sortTasks());
        clearBtn.addActionListener(e -> { taskListModel.clear(); updateStats(); });
        saveBtn.addActionListener(e -> saveTasks());
        loadBtn.addActionListener(e -> loadTasks());

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchTasks(searchField.getText());
            }
        });

        updateStats();
    }

    private void addTask() {
        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String date = dueDateField.getText().trim();
        String priority = (String) priorityBox.getSelectedItem();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.");
            return;
        }

        taskListModel.addElement(new Task(title, desc, date, priority));
        titleField.setText(""); descriptionArea.setText(""); dueDateField.setText("");
        updateStats();
    }

    private void deleteTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) taskListModel.remove(index);
        updateStats();
    }

    private void toggleTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            taskListModel.get(index).toggleCompleted();
            taskList.repaint(); updateStats();
        }
    }

    private void sortTasks() {
        ArrayList<Task> tasks = Collections.list(taskListModel.elements());
        tasks.sort(Comparator.comparing(Task::getPriority));
        taskListModel.clear();
        tasks.forEach(taskListModel::addElement);
    }

    private void searchTasks(String keyword) {
        if (keyword.isEmpty()) {
            loadTasks(); return;
        }

        DefaultListModel<Task> filtered = new DefaultListModel<>();
        for (int i = 0; i < taskListModel.getSize(); i++) {
            Task t = taskListModel.get(i);
            if (t.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                t.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.addElement(t);
            }
        }
        taskList.setModel(filtered);
    }

    private void updateStats() {
        int total = taskListModel.getSize();
        int done = 0;
        for (int i = 0; i < total; i++) {
            if (taskListModel.get(i).isCompleted()) done++;
        }
        statsLabel.setText("Tasks: " + total + " | Completed: " + done);
    }

    private void saveTasks() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("tasks.dat"))) {
            ArrayList<Task> tasks = Collections.list(taskListModel.elements());
            out.writeObject(tasks);
            JOptionPane.showMessageDialog(this, "Tasks saved successfully!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving tasks.");
        }
    }

    private void loadTasks() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("tasks.dat"))) {
            ArrayList<Task> tasks = (ArrayList<Task>) in.readObject();
            taskListModel.clear();
            tasks.forEach(taskListModel::addElement);
            taskList.setModel(taskListModel);
            updateStats();
        } catch (Exception e) {
            // Ignore errors or empty file
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UltimateToDoApp().setVisible(true));
    }
}
