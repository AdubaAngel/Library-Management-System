import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Book {
    private String title;
    private String author;
    private String isbn;// Renamed from IBSN (typical naming)
    private int bookID;
    private boolean isAvailable;
    private LocalDate publicationDate;  // Just use this, remove publicationYear
    private LocalDate dueDate;
    private LocalDate borrowDate;
    private Integer borrowedByUserId; // To track who borrowed it

    //Next we will be back after working on a class to track the borrowing record of each book

    // Constructor - keep it simple
    // Keep your original simple constructor
    Book(String title, String author, String isbn, LocalDate publicationDate) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationDate = publicationDate;
        this.isAvailable = true;
        this.dueDate = null;
        this.borrowDate = null;
        this.borrowedByUserId = null;
        // bookID is NOT set here
    }

    // Alternative constructor for just year
    Book(String title, String author, String isbn, int publicationYear) {
        this(title, author, isbn, LocalDate.of(publicationYear, 1, 1));
    }

    //getter and setter for bookID
    public int getBookID() { return bookID; }
    public void setBookID(int bookID) { this.bookID = bookID; }

    // Getters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public boolean isAvailable() { return isAvailable; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public Integer getBorrowedByUserId() { return borrowedByUserId; }

    // Setters - only for fields that should change
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setAvailable(boolean available) { isAvailable = available; }

    // Business methods
    public boolean borrowBook(int userId) {
        if (!isAvailable) {
            return false;  // Can't borrow if not available
        }

        this.isAvailable = false;
        this.borrowDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(14);  // 2 week loan
        this.borrowedByUserId = userId;
        return true;
    }

    public boolean returnBook() {
        if (isAvailable) {
            return false;  // Can't return if not borrowed
        }

        this.isAvailable = true;
        this.borrowDate = null;
        this.dueDate = null;
        this.borrowedByUserId = null;
        return true;
    }

    // Returns days until due (negative if overdue, null if not borrowed)
    public Integer daysUntilDue() {
        if (dueDate == null) {
            return null;  // Book not borrowed
        }
        return (int) LocalDate.now().until(dueDate).getDays();
    }

    public boolean isOverdue() {
        if (dueDate == null) {
            return false;  // Not borrowed
        }
        return LocalDate.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        String status = isAvailable ? "Available" :
                "Borrowed until " + dueDate;
        return title + " by " + author + " (ISBN: " + isbn + ") - " + status;
    }
}