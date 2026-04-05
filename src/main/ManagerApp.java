import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ManagerApp extends JFrame {
    private static LibraryRegistry registry;
    private static PublicLibrary currentLibrary;
    private static User currentUser;

    private JTextArea outputArea;
    private JTable userTable;
    private JTable pendingTable;
    private JTable bookTable;
    private DefaultTableModel userModel;
    private DefaultTableModel pendingModel;
    private DefaultTableModel bookModel;

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

        // Pre-create a demo manager account (optional)
        User demoManager = currentLibrary.preRegisterUser("Demo Manager", "demo@library.com", "555-0000", UserRole.MANAGER);
        currentLibrary.completeRegistration(demoManager.getId(), "demo123");

        showLoginDialog();
    }

    private static void showLoginDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Manager Login");
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
                if (user != null && user.getRole() == UserRole.MANAGER) {
                    currentUser = user;
                    dialog.dispose();
                    SwingUtilities.invokeLater(() -> new ManagerApp().setVisible(true));
                } else {
                    JOptionPane.showMessageDialog(dialog, "Invalid manager credentials!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid User ID!");
            }
        });
        dialog.add(loginBtn, gbc);

        gbc.gridy = 3;
        JButton registerBtn = new JButton("Register New Manager");
        registerBtn.addActionListener(e -> {
            dialog.dispose();
            showRegistrationDialog();
        });
        dialog.add(registerBtn, gbc);

        dialog.setVisible(true);
    }

    private static void showRegistrationDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Register New Manager");
        dialog.setModal(true);
        dialog.setSize(400, 350);
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
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        dialog.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField(15);
        dialog.add(confirmField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
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

            User newUser = currentLibrary.preRegisterUser(name, email, phone, UserRole.MANAGER);
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

    public ManagerApp() {
        setTitle("📚 Library System - Manager: " + currentUser.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initComponents();
        refreshAll();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(236, 240, 241));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("👥 Users", createUserPanel());
        tabbedPane.addTab("📚 Books", createBookPanel());
        tabbedPane.addTab("💰 Payment Plans", createPaymentPlanPanel());
        tabbedPane.addTab("📊 Reports", createReportsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Output
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

        JLabel title = new JLabel("Manager Dashboard - " + currentUser.getName());
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

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane userTabs = new JTabbedPane();

        String[] userColumns = {"ID", "Name", "Email", "Role", "Status", "Infractions", "Books", "Unpaid Fees"};
        userModel = new DefaultTableModel(userColumns, 0);
        userTable = new JTable(userModel);
        userTable.setRowHeight(25);
        userTabs.addTab("Active Users", new JScrollPane(userTable));

        String[] pendingColumns = {"ID", "Name", "Email", "Phone", "Role"};
        pendingModel = new DefaultTableModel(pendingColumns, 0);
        pendingTable = new JTable(pendingModel);
        pendingTable.setRowHeight(25);
        userTabs.addTab("Pending Registrations", new JScrollPane(pendingTable));

        panel.add(userTabs, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton generateIdBtn = new JButton("Generate User ID");
        generateIdBtn.addActionListener(e -> showGenerateIdDialog());
        JButton addInfractionBtn = new JButton("Add Infraction");
        addInfractionBtn.addActionListener(e -> showAddInfractionDialog());
        JButton resetInfractionsBtn = new JButton("Reset Infractions (Annual)");
        resetInfractionsBtn.addActionListener(e -> {
            currentLibrary.resetInfractions();
            refreshAll();
            outputArea.append("✅ Annual infraction reset complete\n");
        });

        actionPanel.add(generateIdBtn);
        actionPanel.add(addInfractionBtn);
        actionPanel.add(resetInfractionsBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] bookColumns = {"ID", "Title", "Author", "ISBN", "Available"};
        bookModel = new DefaultTableModel(bookColumns, 0);
        bookTable = new JTable(bookModel);
        bookTable.setRowHeight(25);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton addBookBtn = new JButton("Add Book");
        addBookBtn.addActionListener(e -> showAddBookDialog());
        actionPanel.add(addBookBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPaymentPlanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Set Payment Plan"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        JTextField userIdField = new JTextField(10);
        inputPanel.add(userIdField, gbc);

        gbc.gridx = 2;
        inputPanel.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 3;
        JTextField bookIdField = new JTextField(10);
        inputPanel.add(bookIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Installments (1-12):"), gbc);
        gbc.gridx = 1;
        JSpinner installmentSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 12, 1));
        inputPanel.add(installmentSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 4;
        JButton setPlanBtn = new JButton("Create Payment Plan");
        setPlanBtn.addActionListener(e -> {
            try {
                int userId = Integer.parseInt(userIdField.getText());
                int bookId = Integer.parseInt(bookIdField.getText());
                int installments = (int) installmentSpinner.getValue();

                currentLibrary.setPaymentPlan(userId, bookId, installments);
                outputArea.append("✅ Payment plan created for user " + userId + " (Book " + bookId + ")\n");
                refreshAll();
            } catch (NumberFormatException ex) {
                outputArea.append("❌ Invalid ID format\n");
            }
        });
        inputPanel.add(setPlanBtn, gbc);

        JTextArea unpaidArea = new JTextArea();
        unpaidArea.setEditable(false);
        unpaidArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JButton refreshBtn = new JButton("Refresh Unpaid Fees");
        refreshBtn.addActionListener(e -> {
            unpaidArea.setText("");
            for (User user : currentLibrary.getAllUsers()) {
                double total = currentLibrary.getTotalUnpaidFees(user.getId());
                if (total > 0) {
                    unpaidArea.append("User: " + user.getName() + " (ID: " + user.getId() + ") - Total: $" + total + "\n");
                    List<BorrowRecord> unpaid = currentLibrary.getUserUnpaidRecords(user.getId());
                    for (BorrowRecord record : unpaid) {
                        unpaidArea.append("  Book: " + record.getBookName() + " - Fee: $" + record.getLateFee() +
                                " - Outstanding: $" + record.getOutstandingBalance() + "\n");
                    }
                    unpaidArea.append("\n");
                }
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(unpaidArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton overdueBtn = new JButton("Overdue Books");
        overdueBtn.addActionListener(e -> showOverdueReport());

        JButton userHistoryBtn = new JButton("User Borrow History");
        userHistoryBtn.addActionListener(e -> showUserHistoryDialog());

        JButton feeReportBtn = new JButton("Fee Collection Report");
        feeReportBtn.addActionListener(e -> showFeeReport());

        JButton statsBtn = new JButton("Library Statistics");
        statsBtn.addActionListener(e -> showStatistics());

        panel.add(overdueBtn);
        panel.add(userHistoryBtn);
        panel.add(feeReportBtn);
        panel.add(statsBtn);

        return panel;
    }

    private void showGenerateIdDialog() {
        JDialog dialog = new JDialog(this, "Generate User ID", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Name:"), gbc);
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
        JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());
        dialog.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton generateBtn = new JButton("Generate ID");
        generateBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            UserRole role = (UserRole) roleCombo.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!");
                return;
            }

            User newUser = currentLibrary.preRegisterUser(name, email, phone, role);
            JOptionPane.showMessageDialog(dialog, "ID Generated: " + newUser.getId() +
                    "\nGive this ID to the user to complete registration.");
            dialog.dispose();
            refreshAll();
        });
        dialog.add(generateBtn, gbc);

        dialog.setVisible(true);
    }

    private void showAddInfractionDialog() {
        String userIdStr = JOptionPane.showInputDialog(this, "Enter User ID:");
        if (userIdStr == null) return;

        String pointsStr = JOptionPane.showInputDialog(this, "Enter Infraction Points:");
        if (pointsStr == null) return;

        try {
            int userId = Integer.parseInt(userIdStr);
            int points = Integer.parseInt(pointsStr);
            currentLibrary.addInfraction(userId, points);
            refreshAll();
            outputArea.append("✅ Added " + points + " infraction points to user " + userId + "\n");
        } catch (NumberFormatException e) {
            outputArea.append("❌ Invalid input\n");
        }
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add Book", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(15);
        dialog.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        JTextField authorField = new JTextField(15);
        dialog.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        JTextField isbnField = new JTextField(15);
        dialog.add(isbnField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        JTextField yearField = new JTextField(15);
        dialog.add(yearField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton addBtn = new JButton("Add Book");
        addBtn.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String isbn = isbnField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());

                Book book = new Book(title, author, isbn, year);
                int bookId = currentLibrary.addBook(book);
                JOptionPane.showMessageDialog(dialog, "Book added! ID: " + bookId);
                dialog.dispose();
                refreshAll();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid year!");
            }
        });
        dialog.add(addBtn, gbc);

        dialog.setVisible(true);
    }

    private void showOverdueReport() {
        List<BorrowRecord> overdue = currentLibrary.getOverdueBooks();
        StringBuilder sb = new StringBuilder();
        sb.append("📋 OVERDUE BOOKS REPORT\n");
        sb.append(repeat("=", 50)).append("\n\n");

        if (overdue.isEmpty()) {
            sb.append("No overdue books.\n");
        } else {
            for (BorrowRecord record : overdue) {
                sb.append("Book: ").append(record.getBookName()).append("\n");
                sb.append("User ID: ").append(record.getUserId()).append("\n");
                sb.append("Due Date: ").append(record.getDueDate()).append("\n");
                sb.append("Days Overdue: ").append(ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now())).append("\n\n");
            }
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Overdue Books", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showUserHistoryDialog() {
        String userIdStr = JOptionPane.showInputDialog(this, "Enter User ID:");
        if (userIdStr == null) return;

        try {
            int userId = Integer.parseInt(userIdStr);
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
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "User History", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            outputArea.append("❌ Invalid User ID\n");
        }
    }

    private void showFeeReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("💰 FEE COLLECTION REPORT\n");
        sb.append(repeat("=", 50)).append("\n\n");

        double totalCollected = 0;
        double totalPending = 0;

        for (User user : currentLibrary.getAllUsers()) {
            double pending = currentLibrary.getTotalUnpaidFees(user.getId());
            double paid = user.getPaidLateFees();

            totalCollected += paid;
            totalPending += pending;

            if (pending > 0 || paid > 0) {
                sb.append("User: ").append(user.getName()).append(" (ID: ").append(user.getId()).append(")\n");
                sb.append("  Paid: $").append(String.format("%.2f", paid)).append("\n");
                sb.append("  Pending: $").append(String.format("%.2f", pending)).append("\n\n");
            }
        }

        sb.append(repeat("=", 50)).append("\n");
        sb.append("TOTAL COLLECTED: $").append(String.format("%.2f", totalCollected)).append("\n");
        sb.append("TOTAL PENDING: $").append(String.format("%.2f", totalPending)).append("\n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Fee Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 LIBRARY STATISTICS\n");
        sb.append(repeat("=", 50)).append("\n\n");
        sb.append("Total Books: ").append(currentLibrary.getBookCount()).append("\n");
        sb.append("Total Users: ").append(currentLibrary.getUserCount()).append("\n");
        sb.append("Active Loans: ").append(currentLibrary.getActiveLoanCount()).append("\n");
        sb.append("Overdue Books: ").append(currentLibrary.getOverdueBooks().size()).append("\n");
        sb.append("Borrow History Records: ").append(currentLibrary.getBorrowHistoryCount()).append("\n\n");

        for (UserRole role : UserRole.values()) {
            int count = currentLibrary.getAllUsersByRole(role).size();
            if (count > 0) {
                sb.append(role).append(": ").append(count).append("\n");
            }
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshAll() {
        userModel.setRowCount(0);
        for (User user : currentLibrary.getAllUsers()) {
            if (user.getUserStatus() != UserStatus.PENDING_REGISTRATION) {
                userModel.addRow(new Object[]{
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getUserStatus(),
                        user.getInfractionPoints(),
                        user.getBorrowedBookCount(),
                        String.format("%.2f", currentLibrary.getTotalUnpaidFees(user.getId()))
                });
            }
        }

        pendingModel.setRowCount(0);
        for (User user : currentLibrary.getAllUsers()) {
            if (user.getUserStatus() == UserStatus.PENDING_REGISTRATION) {
                pendingModel.addRow(new Object[]{
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole()
                });
            }
        }

        bookModel.setRowCount(0);
        for (Book book : currentLibrary.getAllBooks()) {
            bookModel.addRow(new Object[]{
                    book.getBookID(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.isAvailable() ? "Yes" : "No"
            });
        }
    }
}