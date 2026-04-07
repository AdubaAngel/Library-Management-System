import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class PatronApp extends JFrame {
    private static LibraryRegistry registry;
    private static PublicLibrary currentLibrary;
    private static User currentUser;

    private JTextArea outputArea;
    private JTable historyTable;
    private DefaultTableModel historyModel;

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
        currentLibrary = (PublicLibrary) registry.getLibraryById(1);

        // Pre-create some sample books
        Book book1 = new Book("The Hobbit", "J.R.R. Tolkien", "9780547928227", 1937);
        Book book2 = new Book("Dune", "Frank Herbert", "9780441172719", 1965);
        Book book3 = new Book("1984", "George Orwell", "9780451524935", 1949);
        currentLibrary.addBook(book1);
        currentLibrary.addBook(book2);
        currentLibrary.addBook(book3);

        showLoginDialog();
    }

    private static void showLoginDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Patron Login");
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
                if (user != null && user.getRole() == UserRole.PATRON) {
                    currentUser = user;
                    dialog.dispose();
                    SwingUtilities.invokeLater(() -> new PatronApp().setVisible(true));
                } else {
                    JOptionPane.showMessageDialog(dialog, "Invalid patron credentials!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid User ID!");
            }
        });
        dialog.add(loginBtn, gbc);

        gbc.gridy = 3;
        JButton registerBtn = new JButton("Don't have an account? Register");
        registerBtn.addActionListener(e -> {
            dialog.dispose();
            showRegistrationDialog();
        });
        dialog.add(registerBtn, gbc);

        dialog.setVisible(true);
    }

    private static void showRegistrationDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Patron Registration");
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

            User newUser = currentLibrary.preRegisterUser(name, email, phone, UserRole.PATRON);
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

    public PatronApp() {
        setTitle("📚 Library System - Patron: " + currentUser.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        initComponents();
        refreshDisplay();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(236, 240, 241));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📖 Borrow/Return", createBorrowReturnPanel());
        tabbedPane.addTab("📋 My History", createHistoryPanel());
        tabbedPane.addTab("💰 Fees & Payments", createPaymentPanel());
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

        JLabel title = new JLabel("Welcome, " + currentUser.getName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel statusLabel = new JLabel("Status: " + currentUser.getUserStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.setOpaque(false);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            showLoginDialog();
        });
        infoPanel.add(logoutBtn);

        panel.add(title, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.EAST);

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
        borrowPanel.add(new JLabel("Book ID:"), bgbc);
        bgbc.gridx = 1;
        JTextField borrowBookField = new JTextField(15);
        borrowPanel.add(borrowBookField, bgbc);

        bgbc.gridx = 0; bgbc.gridy = 1;
        bgbc.gridwidth = 2;
        JButton borrowBtn = new JButton("Borrow Book");
        borrowBtn.setBackground(new Color(46, 204, 113));
        borrowBtn.setForeground(Color.WHITE);
        borrowBtn.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(borrowBookField.getText());
                BorrowRecord record = currentLibrary.borrowBook(currentUser.getId(), bookId);
                if (record != null) {
                    outputArea.append("✅ Borrowed! Due: " + record.getDueDate() + "\n");
                    refreshDisplay();
                } else {
                    outputArea.append("❌ Borrowing failed\n");
                }
            } catch (NumberFormatException ex) {
                outputArea.append("❌ Invalid Book ID\n");
            }
        });
        borrowPanel.add(borrowBtn, bgbc);

        JPanel returnPanel = new JPanel(new GridBagLayout());
        returnPanel.setBorder(BorderFactory.createTitledBorder("Return Book"));
        bgbc = new GridBagConstraints();
        bgbc.insets = new Insets(5, 5, 5, 5);

        bgbc.gridx = 0; bgbc.gridy = 0;
        returnPanel.add(new JLabel("Book ID:"), bgbc);
        bgbc.gridx = 1;
        JTextField returnBookField = new JTextField(15);
        returnPanel.add(returnBookField, bgbc);

        bgbc.gridx = 0; bgbc.gridy = 1;
        bgbc.gridwidth = 2;
        JButton returnBtn = new JButton("Return Book");
        returnBtn.setBackground(new Color(241, 196, 15));
        returnBtn.setForeground(Color.WHITE);
        returnBtn.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(returnBookField.getText());
                double fee = currentLibrary.returnBook(currentUser.getId(), bookId);
                if (fee >= 0) {
                    outputArea.append("✅ Returned! Fee: $" + fee + "\n");
                    refreshDisplay();
                } else {
                    outputArea.append("❌ Return failed\n");
                }
            } catch (NumberFormatException ex) {
                outputArea.append("❌ Invalid Book ID\n");
            }
        });
        returnPanel.add(returnBtn, bgbc);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(borrowPanel, gbc);
        gbc.gridx = 1;
        panel.add(returnPanel, gbc);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"Book ID", "Book Name", "Borrow Date", "Due Date", "Return Date", "Late Fee", "Status"};
        historyModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea unpaidArea = new JTextArea();
        unpaidArea.setEditable(false);
        unpaidArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JButton refreshBtn = new JButton("Refresh Fees");
        refreshBtn.addActionListener(e -> {
            double total = currentLibrary.getTotalUnpaidFees(currentUser.getId());
            unpaidArea.setText("Total Unpaid Fees: $" + String.format("%.2f", total) + "\n\n");

            List<BorrowRecord> unpaidRecords = currentLibrary.getUserUnpaidRecords(currentUser.getId());
            for (BorrowRecord record : unpaidRecords) {
                unpaidArea.append("Book: " + record.getBookName() + "\n");
                unpaidArea.append("  Fee: $" + record.getLateFee() + "\n");
                unpaidArea.append("  Outstanding: $" + record.getOutstandingBalance() + "\n\n");
            }
        });

        JButton payBtn = new JButton("Make Payment");
        payBtn.addActionListener(e -> {
            String bookIdStr = JOptionPane.showInputDialog(this, "Enter Book ID to pay:");
            if (bookIdStr != null) {
                try {
                    int bookId = Integer.parseInt(bookIdStr);
                    BorrowRecord record = currentLibrary.findUnpaidRecord(currentUser.getId(), bookId);
                    if (record != null) {
                        double fee = record.getLateFee();
                        currentUser.payFee(fee);
                        record.markAsPaid(LocalDate.now());
                        outputArea.append("✅ Payment of $" + fee + " recorded\n");
                        refreshDisplay();
                    } else {
                        outputArea.append("❌ No unpaid record found for this book\n");
                    }
                } catch (NumberFormatException ex) {
                    outputArea.append("❌ Invalid input\n");
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshBtn);
        buttonPanel.add(payBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(unpaidArea), BorderLayout.CENTER);

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

    private void refreshDisplay() {
        historyModel.setRowCount(0);
        List<BorrowRecord> history = currentLibrary.getUserBorrowHistory(currentUser.getId());
        for (BorrowRecord record : history) {
            String status;
            if (record.getReturnDate() != null) {
                status = "Returned";
            } else if (record.isOverdue()) {
                status = "OVERDUE";
            } else {
                status = "Borrowed";
            }

            historyModel.addRow(new Object[]{
                    record.getBookId(),
                    record.getBookName(),
                    record.getBorrowDate(),
                    record.getDueDate(),
                    record.getReturnDate() != null ? record.getReturnDate() : "Not returned",
                    record.getLateFee() > 0 ? "$" + record.getLateFee() : "$0",
                    status
            });
        }
    }

}