import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface Library {
    // Basic info
    String getName();

    // Book management
    int addBook(Book book);
    List<Book> findBooksByTitle(String title);

    // User management
    int registerUser(User user);
    User findUserById(int userId);

    // Borrowing operations
    BorrowRecord borrowBook(int userId, int bookId);
    double returnBook(int userId, int bookId);

    // Reports
    List<BorrowRecord> getOverdueBooks();
    List<BorrowRecord> getUserBorrowHistory(int userId);
    double getTotalLateFeesForUser(int userId);

    // Count methods
    int getBookCount();
    int getUserCount();
    int getActiveLoanCount();
    int getBorrowHistoryCount();

    // Collection methods
    Collection<Book> getAllBooks();
    Collection<User> getAllUsers();

    // Permission methods
    boolean hasPermission(int userId, String action);
    boolean canViewUserInfo(int viewerId, int targetUserId);
    boolean canChangeUserRole(int managerId, int targetUserId, UserRole newRole);

    // Validation methods
    boolean isValidEmail(String email);
    boolean isValidPhone(String phone);
}