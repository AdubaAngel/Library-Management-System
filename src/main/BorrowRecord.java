import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecord {
    private int bookId;
    private int userId;
    private String bookName;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double lateFee;

    // Payment tracking fields
    private boolean feePaid;
    private LocalDate paymentDate;
    private List<Installment> installments;
    private double remainingBalance;
    private LocalDate paymentDueDate;

    // Inner class for installment tracking
    public static class Installment {
        private double amount;
        private LocalDate dueDate;
        private boolean isPaid;
        private LocalDate paidDate;

        public Installment(double amount, LocalDate dueDate) {
            this.amount = amount;
            this.dueDate = dueDate;
            this.isPaid = false;
            this.paidDate = null;
        }

        public double getAmount() { return amount; }
        public LocalDate getDueDate() { return dueDate; }
        public boolean isPaid() { return isPaid; }
        public LocalDate getPaidDate() { return paidDate; }

        public void markAsPaid(LocalDate paymentDate) {
            this.isPaid = true;
            this.paidDate = paymentDate;
        }

        public boolean isOverdue() {
            return !isPaid && LocalDate.now().isAfter(dueDate);
        }

        @Override
        public String toString() {
            return "$" + amount + " due " + dueDate + (isPaid ? " (PAID on " + paidDate + ")" : " (PENDING)");
        }
    }

    // Constructor
    public BorrowRecord(int bookId, int userId, String bookName,
                        LocalDate borrowDate, LocalDate dueDate) {
        this.bookId = bookId;
        this.userId = userId;
        this.bookName = bookName;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.lateFee = 0.0;
        this.feePaid = false;
        this.paymentDate = null;
        this.installments = new ArrayList<>();
        this.remainingBalance = 0.0;
        this.paymentDueDate = null;
    }

    // Getters
    public int getBookId() { return bookId; }
    public int getUserId() { return userId; }
    public String getBookName() { return bookName; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getLateFee() { return lateFee; }
    public boolean isFeePaid() { return feePaid; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public List<Installment> getInstallments() { return installments; }
    public double getRemainingBalance() { return remainingBalance; }
    public LocalDate getPaymentDueDate() { return paymentDueDate; }

    // Setters
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }

    // Payment methods
    public void markAsPaid(LocalDate paymentDate) {
        this.feePaid = true;
        this.paymentDate = paymentDate;
        this.remainingBalance = 0.0;
        this.paymentDueDate = null;
        this.installments.clear();
    }

    public void createPaymentPlan(int numberOfInstallments) {
        if (lateFee <= 0) return;

        double installmentAmount = lateFee / numberOfInstallments;
        installments = new ArrayList<>();
        LocalDate startDate = LocalDate.now();

        for (int i = 1; i <= numberOfInstallments; i++) {
            installments.add(new Installment(installmentAmount, startDate.plusWeeks(i)));
        }
        this.remainingBalance = lateFee;
        this.paymentDueDate = startDate.plusWeeks(numberOfInstallments);
    }

    public void makeInstallmentPayment(int installmentIndex, LocalDate paymentDate) {
        if (installmentIndex < 0 || installmentIndex >= installments.size()) {
            return;
        }

        Installment installment = installments.get(installmentIndex);
        if (!installment.isPaid()) {
            installment.markAsPaid(paymentDate);
            this.remainingBalance -= installment.getAmount();

            // Check if all installments are paid
            boolean allPaid = installments.stream().allMatch(Installment::isPaid);
            if (allPaid) {
                markAsPaid(paymentDate);
            }
        }
    }

    public double getOutstandingBalance() {
        if (feePaid) return 0.0;
        if (!installments.isEmpty()) {
            return installments.stream()
                    .filter(i -> !i.isPaid())
                    .mapToDouble(Installment::getAmount)
                    .sum();
        }
        return lateFee;
    }

    public boolean hasOverdueInstallment() {
        if (installments.isEmpty()) return false;
        return installments.stream().anyMatch(Installment::isOverdue);
    }

    // Existing methods
    public boolean isOverdue() {
        return returnDate == null && LocalDate.now().isAfter(dueDate);
    }

    public boolean wasOverdue() {
        if (returnDate == null) return false;
        return returnDate.isAfter(dueDate);
    }

    public long getDaysLate() {
        if (returnDate == null || !returnDate.isAfter(dueDate)) return 0;
        return ChronoUnit.DAYS.between(dueDate, returnDate);
    }

    public void returnBook(LocalDate returnDate, double dailyRate) {
        this.returnDate = returnDate;
        this.lateFee = calculateLateFee(dailyRate);
    }

    private double calculateLateFee(double dailyRate) {
        if (!wasOverdue()) return 0.0;
        long daysLate = getDaysLate();
        double fee = daysLate * dailyRate;
        double MAX_FEE = 20.00;
        return Math.min(fee, MAX_FEE);
    }

    public double calculateLateFee() {
        return calculateLateFee(0.50);
    }

    @Override
    public String toString() {
        String status = (returnDate == null) ?
                (isOverdue() ? "OVERDUE" : "Borrowed") : "Returned";

        String paymentStatus = "";
        if (returnDate != null && lateFee > 0) {
            if (feePaid) {
                paymentStatus = " (PAID on " + paymentDate + ")";
            } else if (!installments.isEmpty()) {
                paymentStatus = " (Payment Plan: " + getOutstandingBalance() + " remaining)";
            } else {
                paymentStatus = " (UNPAID: $" + lateFee + ")";
            }
        }

        return bookName + " (User " + userId + ") - " +
                "Borrowed: " + borrowDate + ", Due: " + dueDate +
                ", Status: " + status + paymentStatus;
    }
}