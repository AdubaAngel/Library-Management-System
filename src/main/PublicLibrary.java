import java.time.LocalDate;
import java.util.*;

public class PublicLibrary implements Library {
    private String name;
    private int startId;
    private int idIncrements;
    private int employeeBaseLimit;
    private int userBaseLimit;

    private Map<Integer, Book> books;
    private Map<Integer, User> users;
    private List<BorrowRecord> borrowHistory;
    private List<BorrowRecord> activeLoans;

    private int nextBookId;

    // Role-based ID counters
    private int nextPatronId;
    private int nextEmployeeId;
    private int nextManagerId;
    private int nextGuestId;
    private int idIncrement;

    // Role-based rules maps
    private Map<UserRole, Integer> maxBooksByRole;
    private Map<UserRole, Integer> loanDaysByRole;
    private Map<UserRole, Double> lateFeeByRole;
    private Map<UserRole, Integer> infractionLimitByRole;

    public PublicLibrary(String name, int startId, int idIncrements,
                         int employeeBaseLimit, int userBaseLimit) {
        this.name = name;
        this.startId = startId;
        this.idIncrements = idIncrements;
        this.employeeBaseLimit = employeeBaseLimit;
        this.userBaseLimit = userBaseLimit;

        this.idIncrement = idIncrements;
        this.nextPatronId = startId;
        this.nextEmployeeId = startId + 4000;
        this.nextManagerId = startId + 8000;
        this.nextGuestId = startId + 2000;

        this.nextBookId = startId;
        this.books = new HashMap<>();
        this.users = new HashMap<>();
        this.borrowHistory = new ArrayList<>();
        this.activeLoans = new ArrayList<>();

        maxBooksByRole = new HashMap<>();
        loanDaysByRole = new HashMap<>();
        lateFeeByRole = new HashMap<>();
        infractionLimitByRole = new HashMap<>();

        initializeRoleRules();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int addBook(Book book) {
        int localId = nextBookId;
        book.setBookID(localId);
        books.put(localId, book);
        nextBookId += idIncrements;
        return localId;
    }

    @Override
    public List<Book> findBooksByTitle(String title) {
        List<Book> matchingBooks = new ArrayList<>();
        String searchLower = title.toLowerCase();

        for (Book book : books.values()) {
            if (book.getTitle().toLowerCase().contains(searchLower)) {
                matchingBooks.add(book);
            }
        }
        return matchingBooks;
    }

    // ========== ID GENERATION METHODS ==========

    public int generateIdForRole(UserRole role) {
        int id;
        switch (role) {
            case PATRON:
                id = nextPatronId;
                nextPatronId += idIncrement;
                break;
            case EMPLOYEE:
            case JUNIOR_EMPLOYEE:
            case VOLUNTEER:
                id = nextEmployeeId;
                nextEmployeeId += idIncrement;
                break;
            case MANAGER:
                id = nextManagerId;
                nextManagerId += idIncrement;
                break;
            case GUEST:
                id = nextGuestId;
                nextGuestId += idIncrement;
                break;
            default:
                id = nextPatronId;
                nextPatronId += idIncrement;
        }
        return id;
    }

    public User preRegisterUser(String name, String email, String phone, UserRole role) {
        int userId = generateIdForRole(role);
        User user = new User(name, email, phone, role);
        user.setId(userId);
        user.setUserStatus(UserStatus.PENDING_REGISTRATION);
        users.put(userId, user);
        System.out.println("📝 Pre-registered: " + name + " (ID: " + userId + ", Role: " + role + ")");
        return user;
    }

    public boolean completeRegistration(int userId, String password) {
        User user = users.get(userId);
        if (user == null) {
            System.out.println("❌ User ID " + userId + " not found!");
            return false;
        }
        if (user.getUserStatus() != UserStatus.PENDING_REGISTRATION) {
            System.out.println("❌ User is not pending registration!");
            return false;
        }

        String email = user.getEmail();
        String phone = user.getPhone();

        if (!isValidEmail(email)) {
            System.out.println("❌ Invalid email format!");
            return false;
        }

        if (!isValidPhone(phone)) {
            System.out.println("❌ Invalid phone number!");
            return false;
        }

        user.setPassword(password);
        user.setUserStatus(UserStatus.ACTIVE);
        System.out.println("✅ Registration complete for " + user.getName() + " (ID: " + userId + ")");
        return true;
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        if (!email.contains("@")) {
            return false;
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        String localPart = parts[0];
        String domain = parts[1];
        if (localPart.isEmpty()) {
            return false;
        }
        if (!domain.contains(".")) {
            return false;
        }
        String[] domainParts = domain.split("\\.");
        if (domainParts.length < 2) {
            return false;
        }
        for (String part : domainParts) {
            if (part.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (cleanPhone.isEmpty()) {
            return false;
        }
        if (cleanPhone.length() < 10 || cleanPhone.length() > 15) {
            return false;
        }
        return true;
    }

    public User login(int userId, String password) {
        User user = users.get(userId);
        if (user == null) {
            System.out.println("❌ User ID " + userId + " not found!");
            return null;
        }
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            System.out.println("❌ User is not active! Status: " + user.getUserStatus());
            return null;
        }
        if (user.verifyPassword(password)) {
            System.out.println("✅ Login successful: " + user.getName());
            return user;
        }
        System.out.println("❌ Incorrect password!");
        return null;
    }

    public List<User> getUsersWithStatus(UserStatus status) {
        List<User> result = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getUserStatus() == status) {
                result.add(user);
            }
        }
        return result;
    }

    public List<User> getAllUsersByRole(UserRole role) {
        List<User> result = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getRole() == role) {
                result.add(user);
            }
        }
        return result;
    }

    // ========== IMPLEMENTED INTERFACE METHODS ==========

    @Override
    public User findUserById(int userId) {
        return users.get(userId);
    }

    @Override
    public int registerUser(User user) {
        int userId = generateIdForRole(user.getRole());
        user.setId(userId);
        user.setUserStatus(UserStatus.ACTIVE);
        users.put(userId, user);
        return userId;
    }

    @Override
    public BorrowRecord borrowBook(int userId, int bookId) {
        if (!users.containsKey(userId)) {
            System.out.println("❌ User " + userId + " not found!");
            return null;
        }

        if (!books.containsKey(bookId)) {
            System.out.println("❌ Book " + bookId + " not found!");
            return null;
        }

        User user = users.get(userId);
        Book book = books.get(bookId);

        if (!book.isAvailable()) {
            System.out.println("❌ Book " + bookId + " is not available!");
            return null;
        }

        if (user.isCurrentlySuspended()) {
            System.out.println("❌ " + user.getName() + " is suspended until " + user.getSuspensionEndDate());
            return null;
        }

        if (user.isTerminated()) {
            System.out.println("❌ " + user.getName() + " has been terminated!");
            return null;
        }

        Integer maxBooks = maxBooksByRole.get(user.getRole());
        if (maxBooks == null) maxBooks = 5;

        if (user.getBorrowedBookCount() >= maxBooks) {
            System.out.println("❌ " + user.getName() + " has reached the limit of " + maxBooks + " books!");
            return null;
        }

        user.addBorrowedBook(bookId);

        Integer loanDays = loanDaysByRole.get(user.getRole());
        if (loanDays == null) loanDays = 14;

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(loanDays);

        BorrowRecord record = new BorrowRecord(bookId, userId, book.getTitle(), borrowDate, dueDate);

        activeLoans.add(record);
        borrowHistory.add(record);
        book.setAvailable(false);

        System.out.println("✅ " + user.getName() + " borrowed \"" + book.getTitle() + "\" (Due: " + dueDate + ")");
        return record;
    }

    @Override
    public double returnBook(int userId, int bookId) {
        if (!isValidLibraryId(bookId)) {
            System.out.println("❌ Invalid book ID format!");
            return -1.0;
        }

        BorrowRecord activeLoan = null;
        for (BorrowRecord record : activeLoans) {
            if (record.getUserId() == userId && record.getBookId() == bookId && record.getReturnDate() == null) {
                activeLoan = record;
                break;
            }
        }

        if (activeLoan == null) {
            System.out.println("❌ No active loan found for user " + userId + " and book " + bookId);
            return -1.0;
        }

        Book book = books.get(bookId);
        User user = users.get(userId);

        boolean wasOverdue = LocalDate.now().isAfter(activeLoan.getDueDate());

        if (wasOverdue) {
            user.addLateReturn();
            int points = getLateReturnPoints(user.getRole());
            addInfraction(userId, points);
        }

        Double lateFee = lateFeeByRole.get(user.getRole());
        if (lateFee == null) lateFee = 0.50;

        activeLoan.returnBook(LocalDate.now(), lateFee);
        double fee = activeLoan.getLateFee();

        if (fee > 0) {
            user.addPendingFee(fee);
            System.out.println("💰 Late fee of $" + fee + " added to " + user.getName() + "'s account");
        }

        book.setAvailable(true);
        user.removeBorrowedBook(bookId);
        activeLoans.remove(activeLoan);

        System.out.println("✅ " + user.getName() + " returned \"" + book.getTitle() + "\"");
        return fee;
    }

    private int getLateReturnPoints(UserRole role) {
        switch (role) {
            case MANAGER:
                return 3;
            case EMPLOYEE:
            case JUNIOR_EMPLOYEE:
            case VOLUNTEER:
                return 1;
            default:
                return 1;
        }
    }

    public void addInfraction(int userId, int points) {
        User user = users.get(userId);
        if (user == null) {
            System.out.println("❌ User not found!");
            return;
        }

        user.addInfractionPoints(points);

        int limit = infractionLimitByRole.get(user.getRole());
        int currentPoints = user.getInfractionPoints();

        double warningThreshold = limit * 0.15;
        double suspensionThreshold = limit * 0.50;

        System.out.println("⚠️ " + user.getName() + " received " + points + " infraction point(s). Total: " + currentPoints + "/" + limit);

        if (currentPoints >= limit) {
            user.setUserStatus(UserStatus.TERMINATED);
            System.out.println("🚫 " + user.getName() + " has been TERMINATED!");
        } else if (currentPoints >= suspensionThreshold) {
            if (user.getRole() == UserRole.EMPLOYEE || user.getRole() == UserRole.MANAGER) {
                user.setUserStatus(UserStatus.TEMPORARY_LEAVE);
                int weeks = user.getRole() == UserRole.MANAGER ? 0 : 2;
                user.applySuspension(weeks);
                System.out.println("⏸️ " + user.getName() + " has been placed on TEMPORARY LEAVE!");
            } else {
                user.setUserStatus(UserStatus.SUSPENDED);
                user.applySuspension(1);
                System.out.println("⏸️ " + user.getName() + " has been SUSPENDED for 1 week!");
            }
        } else if (currentPoints >= warningThreshold && user.getUserStatus() == UserStatus.ACTIVE) {
            user.setUserStatus(UserStatus.WARNING);
            System.out.println("⚠️ " + user.getName() + " has received a WARNING!");
        }
    }

    public void resetInfractions() {
        for (User user : users.values()) {
            user.resetInfractionPoints();
            if (user.getUserStatus() == UserStatus.WARNING) {
                user.setUserStatus(UserStatus.ACTIVE);
            }
        }
        System.out.println("📅 Annual infraction reset complete.");
    }

    private void initializeRoleRules() {
        maxBooksByRole.put(UserRole.PATRON, 7);
        maxBooksByRole.put(UserRole.GUEST, 2);
        maxBooksByRole.put(UserRole.EMPLOYEE, 10);
        maxBooksByRole.put(UserRole.MANAGER, 30);
        maxBooksByRole.put(UserRole.JUNIOR_EMPLOYEE, 8);
        maxBooksByRole.put(UserRole.VOLUNTEER, 12);

        loanDaysByRole.put(UserRole.PATRON, 14);
        loanDaysByRole.put(UserRole.GUEST, 7);
        loanDaysByRole.put(UserRole.EMPLOYEE, 20);
        loanDaysByRole.put(UserRole.MANAGER, 30);
        loanDaysByRole.put(UserRole.JUNIOR_EMPLOYEE, 15);
        loanDaysByRole.put(UserRole.VOLUNTEER, 10);

        lateFeeByRole.put(UserRole.PATRON, 0.50);
        lateFeeByRole.put(UserRole.GUEST, 1.00);
        lateFeeByRole.put(UserRole.EMPLOYEE, 0.25);
        lateFeeByRole.put(UserRole.MANAGER, 1.00);
        lateFeeByRole.put(UserRole.JUNIOR_EMPLOYEE, 0.15);
        lateFeeByRole.put(UserRole.VOLUNTEER, 0.10);

        infractionLimitByRole.put(UserRole.PATRON, userBaseLimit);
        infractionLimitByRole.put(UserRole.GUEST, userBaseLimit - 5);
        infractionLimitByRole.put(UserRole.EMPLOYEE, employeeBaseLimit);
        infractionLimitByRole.put(UserRole.JUNIOR_EMPLOYEE, employeeBaseLimit);
        infractionLimitByRole.put(UserRole.VOLUNTEER, employeeBaseLimit);
        infractionLimitByRole.put(UserRole.MANAGER, employeeBaseLimit + 3);
    }

    private boolean isValidLibraryId(int bookId) {
        if (bookId < startId) return false;
        return (bookId - startId) % idIncrements == 0;
    }

    @Override
    public List<BorrowRecord> getOverdueBooks() {
        List<BorrowRecord> overdueBooks = new ArrayList<>();
        for (BorrowRecord record : activeLoans) {
            if (record.isOverdue()) {
                overdueBooks.add(record);
            }
        }
        return overdueBooks;
    }

    @Override
    public List<BorrowRecord> getUserBorrowHistory(int userId) {
        List<BorrowRecord> userHistory = new ArrayList<>();
        for (BorrowRecord record : borrowHistory) {
            if (record.getUserId() == userId) {
                userHistory.add(record);
            }
        }
        return userHistory;
    }

    @Override
    public double getTotalLateFeesForUser(int userId) {
        double totalFees = 0.0;
        for (BorrowRecord record : borrowHistory) {
            if (record.getUserId() == userId) {
                totalFees += record.getLateFee();
            }
        }
        return totalFees;
    }

    @Override
    public int getBookCount() {
        return books.size();
    }

    @Override
    public int getUserCount() {
        return users.size();
    }

    @Override
    public int getActiveLoanCount() {
        return activeLoans.size();
    }

    @Override
    public int getBorrowHistoryCount() {
        return borrowHistory.size();
    }

    @Override
    public boolean hasPermission(int userId, String action) {
        User user = users.get(userId);
        if (user == null) return false;

        UserRole role = user.getRole();

        switch (action) {
            case "borrow":
                return role != UserRole.GUEST;
            case "view_all_users":
                return role == UserRole.EMPLOYEE || role == UserRole.MANAGER;
            case "add_book":
                return role == UserRole.EMPLOYEE || role == UserRole.MANAGER;
            case "view_employee_info":
                return role == UserRole.MANAGER;
            case "generate_id":
                return role == UserRole.MANAGER;
            case "pre_register":
                return role == UserRole.MANAGER;
            case "view_own_profile":
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean canViewUserInfo(int viewerId, int targetUserId) {
        User viewer = users.get(viewerId);
        User target = users.get(targetUserId);

        if (viewer == null || target == null) return false;
        if (viewerId == targetUserId) return true;

        UserRole viewerRole = viewer.getRole();

        switch (viewerRole) {
            case MANAGER:
                return true;
            case EMPLOYEE:
                return target.getRole() == UserRole.PATRON || target.getRole() == UserRole.GUEST;
            default:
                return false;
        }
    }

    @Override
    public boolean canChangeUserRole(int managerId, int targetUserId, UserRole newRole) {
        User manager = users.get(managerId);
        if (manager == null || manager.getRole() != UserRole.MANAGER) {
            return false;
        }
        if (managerId == targetUserId) {
            return false;
        }
        return true;
    }



    public BorrowRecord findUnpaidRecord(int userId, int bookId) {
        for (BorrowRecord record : borrowHistory) {
            if (record.getUserId() == userId &&
                    record.getBookId() == bookId &&
                    record.getReturnDate() != null &&
                    !record.isFeePaid() &&
                    record.getLateFee() > 0) {
                return record;
            }
        }
        return null;
    }

    public List<BorrowRecord> getUserUnpaidRecords(int userId) {
        List<BorrowRecord> unpaidRecords = new ArrayList<>();
        for (BorrowRecord record : borrowHistory) {
            if (record.getUserId() == userId &&
                    record.getReturnDate() != null &&
                    !record.isFeePaid() &&
                    record.getLateFee() > 0) {
                unpaidRecords.add(record);
            }
        }
        return unpaidRecords;
    }

    public void setPaymentPlan(int userId, int bookId, int numberOfInstallments) {
        BorrowRecord record = findUnpaidRecord(userId, bookId);
        if (record != null && record.getLateFee() > 0) {
            record.createPaymentPlan(numberOfInstallments);
            System.out.println("✅ Payment plan created with " + numberOfInstallments + " installments");
        } else {
            System.out.println("❌ No unpaid record found for this book");
        }
    }

    public void makeInstallmentPayment(int userId, int bookId, int installmentIndex) {
        BorrowRecord record = findUnpaidRecord(userId, bookId);
        if (record != null && !record.getInstallments().isEmpty()) {
            LocalDate paymentDate = LocalDate.now();
            LocalDate dueDate = record.getInstallments().get(installmentIndex).getDueDate();

            record.makeInstallmentPayment(installmentIndex, paymentDate);

            // Check if payment was late
            if (paymentDate.isAfter(dueDate)) {
                addInfraction(userId, 2);
                System.out.println("⚠️ Late payment - infraction added");
            }

            System.out.println("✅ Installment payment recorded");
        } else {
            System.out.println("❌ No payment plan found for this book");
        }
    }

    public double getTotalUnpaidFees(int userId) {
        double total = 0.0;
        for (BorrowRecord record : getUserUnpaidRecords(userId)) {
            total += record.getOutstandingBalance();
        }
        return total;
    }

    @Override
    public Collection<Book> getAllBooks() {
        return books.values();
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }
}