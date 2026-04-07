import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EmployeeApp extends JFrame {
    private static LibraryRegistry registry;
    private static PublicLibrary currentLibrary;
    private static User currentUser;

    private JTextArea outputArea;
    private JTable patronTable;
    private JTable overdueTable;
    private DefaultTableModel patronModel;
    private DefaultTableModel overdueModel;

    private String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        registry = new LibraryRegistry();
        registry.registerLibrary("Downtown Library", 1000, 7, 17, 20);
        registry.registerLibrary("University Library", 2000, 5, 15, 25);
        currentLibrary = (PublicLibrary) registry.getLibraryById(1);

        // Pre-create some sample patrons (optional)
        User patron1 = currentLibrary.preRegisterUser("Alice Patron", "alice@email.com", "555-2222", UserRole.PATRON);
        currentLibrary.completeRegistration(patron1.getId(), "pat123");
        User patron2 = currentLibrary.preRegisterUser("Bob Patron", "bob@email.com", "555-3333", UserRole.PATRON);
        currentLibrary.completeRegistration(patron2.getId(), "pat456");

        showLoginDialog();
    }

    private static void showLoginDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Employee Login");
        dialog.setModal(true);
        dialog.setSize(350, 280);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        JTextField userIdField = new JTextField(15);
        dialog.add(userIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        dialog.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            try {
                int userId = Integer.parseInt(userIdField.getText());
                String password = new String(passwordField.getPassword());
                User user = currentLibrary.login(userId, password);
                if (user != null && (user.getRole() == UserRole.EMPLOYEE ||
                        user.getRole() == UserRole.JUNIOR_EMPLOYEE ||
                        user.getRole() == UserRole.VOLUNTEER)) {
                    currentUser = user;
                    dialog.dispose();
                    SwingUtilities.invokeLater(() -> new EmployeeApp().setVisible(true));
                } else {
                    JOptionPane.showMessageDialog(dialog, "Invalid employee credentials!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid User ID!");
            }
        });
        dialog.add(loginBtn, gbc);

        gbc.gridy = 3;
        JButton registerBtn = new JButton("Register New Employee");
        registerBtn.addActionListener(e -> {
            dialog.dispose();
            showRegistrationDialog();
        });
        dialog.add(registerBtn, gbc);

        dialog.setVisible(true);
    }

    private static void showRegistrationDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Register New Employee");
        dialog.setModal(true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        dialog.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        JTextField phoneField = new JTextField(15);
        dialog.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        JComboBox<UserRole> roleCombo = new JComboBox<>(new UserRole[]{UserRole.EMPLOYEE, UserRole.JUNIOR_EMPLOYEE, UserRole.VOLUNTEER});
        dialog.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        dialog.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField(15);
        dialog.add(confirmField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            UserRole role = (UserRole) roleCombo.getSelectedItem();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!");
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!");
                return;
            }
            if (password.length() < 4) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 4 characters!");
                return;
            }

            User newUser = currentLibrary.preRegisterUser(name, email, phone, role);
            if (currentLibrary.completeRegistration(newUser.getId(), password)) {
                JOptionPane.showMessageDialog(dialog, "✅ Registration complete!\nYour ID: " + newUser.getId() + "\nYou can now login.");
                dialog.dispose();
                showLoginDialog();
            } else {
                JOptionPane.showMessageDialog(dialog, "Registration failed!");
            }
        });
        dialog.add(registerBtn, gbc);

        dialog.setVisible(true);
    }

    public EmployeeApp() {
        setTitle("📚 Library System - Employee: " + currentUser.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        initComponents();
        refreshAll();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(236, 240, 241));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("👥 Patrons", createPatronPanel());
        tabbedPane.addTab("⚠️ Overdue Books", createOverduePanel());
        tabbedPane.addTab("📖 Borrow/Return", createBorrowReturnPanel());
        tabbedPane.addTab("🔍 Search Books", createSearchPanel());

        add(tabbedPane, BorderLayout.CENTER);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(800, 150));
        add(scrollPane, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Employee Dashboard - " + currentUser.getName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            showLoginDialog();
        });

        JPanel rightPanel = new JPanel(new FlowLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(logoutBtn);

        panel.add(title, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createPatronPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Name", "Email", "Status", "Books Borrowed", "Unpaid Fees"};
        patronModel = new DefaultTableModel(columns, 0);
        patronTable = new JTable(patronModel);
        patronTable.setRowHeight(25);
        panel.add(new JScrollPane(patronTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton viewHistoryBtn = new JButton("View Borrow History");
        viewHistoryBtn.addActionListener(e -> showPatronHistory());
        actionPanel.add(viewHistoryBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createOverduePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"Book ID", "Book Name", "User ID", "User Name", "Due Date", "Days Overdue"};
        overdueModel = new DefaultTableModel(columns, 0);
        overdueTable = new JTable(overdueModel);
        overdueTable.setRowHeight(25);
        panel.add(new JScrollPane(overdueTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshOverdue());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBorrowReturnPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel borrowPanel = new JPanel(new GridBagLayout());
        borrowPanel.setBorder(BorderFactory.createTitledBorder("Borrow Book"));
        GridBagConstraints bgbc = new GridBagConstraints();
        bgbc.insets = new Insets(5, 5, 5, 5);

        bgbc.gridx = 0; bgbc.gridy = 0;
        borrowPanel.add(new JLabel("User ID:"), bgbc);
        bgbc.gridx = 1;
        JTextField borrowUserIdField = new JTextField(10);
        borrowPanel.add(borrowUserIdField, bgbc);

        bgbc.gridx = 2;
        borrowPanel.add(new JLabel("Book ID:"), bgbc);
        bgbc.gridx = 3;
        JTextField borrowBookIdField = new JTextField(10);
        borrowPanel.add(borrowBookIdField, bgbc);

        bgbc.gridx = 0; bgbc.gridy = 1;
        bgbc.gridwidth = 4;
        JButton borrowBtn = new JButton("Borrow Book");
        borrowBtn.setBackground(new Color(46, 204, 113));
        borrowBtn.setForeground(Color.WHITE);
        borrowBtn.addActionListener(e -> {
            try {
                int userId = Integer.parseInt(borrowUserIdField.getText());
                int bookId = Integer.parseInt(borrowBookIdField.getText());
                BorrowRecord record = currentLibrary.borrowBook(userId, bookId);
                if (record != null) {
                    outputArea.append("✅ Book borrowed! Due: " + record.getDueDate() + "\n");
                    refreshAll();
                } else {
                    outputArea.append("❌ Borrowing failed\n");
                }
            } catch (NumberFormatException ex) {
                outputArea.append("❌ Invalid ID format\n");
            }
        });
        borrowPanel.add(borrowBtn, bgbc);

        JPanel returnPanel = new JPanel(new GridBagLayout());
        returnPanel.setBorder(BorderFactory.createTitledBorder("Return Book"));
        bgbc = new GridBagConstraints();
        bgbc.insets = new Insets(5, 5, 5, 5);

        bgbc.gridx = 0; bgbc.gridy = 0;
        returnPanel.add(new JLabel("User ID:"), bgbc);
        bgbc.gridx = 1;
        JTextField returnUserIdField = new JTextField(10);
        returnPanel.add(returnUserIdField, bgbc);

        bgbc.gridx = 2;
        returnPanel.add(new JLabel("Book ID:"), bgbc);
        bgbc.gridx = 3;
        JTextField returnBookIdField = new JTextField(10);
        returnPanel.add(returnBookIdField, bgbc);

        bgbc.gridx = 0; bgbc.gridy = 1;
        bgbc.gridwidth = 4;
        JButton returnBtn = new JButton("Return Book");
        returnBtn.setBackground(new Color(241, 196, 15));
        returnBtn.setForeground(Color.WHITE);
        returnBtn.addActionListener(e -> {
            try {
                int userId = Integer.parseInt(returnUserIdField.getText());
                int bookId = Integer.parseInt(returnBookIdField.getText());
                double fee = currentLibrary.returnBook(userId, bookId);
                if (fee >= 0) {
                    outputArea.append("✅ Book returned! Fee: $" + fee + "\n");
                    refreshAll();
                } else {
                    outputArea.append("❌ Return failed\n");
                }
            } catch (NumberFormatException ex) {
                outputArea.append("❌ Invalid ID format\n");
            }
        });
        returnPanel.add(returnBtn, bgbc);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(borrowPanel, gbc);
        gbc.gridx = 1;
        panel.add(returnPanel, gbc);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Search Title:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);

        JTextArea resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> {
            String title = searchField.getText().trim();
            if (!title.isEmpty()) {
                List<Book> results = currentLibrary.findBooksByTitle(title);
                resultsArea.setText("Found " + results.size() + " book(s):\n\n");
                for (Book book : results) {
                    resultsArea.append("ID: " + book.getBookID() + "\n");
                    resultsArea.append("Title: " + book.getTitle() + "\n");
                    resultsArea.append("Author: " + book.getAuthor() + "\n");
                    resultsArea.append("Available: " + (book.isAvailable() ? "Yes" : "No") + "\n\n");
                }
            }
        });
        searchPanel.add(searchBtn);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        return panel;
    }

    private void refreshAll() {
        refreshPatrons();
        refreshOverdue();
    }

    private void refreshPatrons() {
        patronModel.setRowCount(0);
        List<User> patrons = currentLibrary.getAllUsersByRole(UserRole.PATRON);
        for (User patron : patrons) {
            double unpaidFees = currentLibrary.getTotalUnpaidFees(patron.getId());
            patronModel.addRow(new Object[]{
                    patron.getId(),
                    patron.getName(),
                    patron.getEmail(),
                    patron.getUserStatus(),
                    patron.getBorrowedBookCount(),
                    String.format("%.2f", unpaidFees)
            });
        }
    }

    private void refreshOverdue() {
        overdueModel.setRowCount(0);
        List<BorrowRecord> overdue = currentLibrary.getOverdueBooks();
        for (BorrowRecord record : overdue) {
            User user = currentLibrary.findUserById(record.getUserId());
            long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
            overdueModel.addRow(new Object[]{
                    record.getBookId(),
                    record.getBookName(),
                    record.getUserId(),
                    user != null ? user.getName() : "Unknown",
                    record.getDueDate(),
                    daysOverdue
            });
        }
    }

    private void showPatronHistory() {
        int selectedRow = patronTable.getSelectedRow();
        if (selectedRow == -1) {
            outputArea.append("❌ Please select a patron first\n");
            return;
        }

        int userId = (int) patronModel.getValueAt(selectedRow, 0);
        User user = currentLibrary.findUserById(userId);
        List<BorrowRecord> history = currentLibrary.getUserBorrowHistory(userId);
        double totalFees = currentLibrary.getTotalLateFeesForUser(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("📋 BORROW HISTORY FOR ").append(user.getName()).append("\n");
        sb.append(repeat("=", 50)).append("\n\n");

        for (BorrowRecord record : history) {
            sb.append(record.toString()).append("\n");
        }
        sb.append("\n").append(repeat("=", 50)).append("\n");
        sb.append("💰 Total Late Fees: $").append(String.format("%.2f", totalFees)).append("\n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Patron History", JOptionPane.INFORMATION_MESSAGE);
    }
    
}