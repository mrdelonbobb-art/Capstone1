package com.pluralsight;
import java.util.Scanner;//gets user input
import java.io.BufferedWriter;//stores text in a buffer before writing to the file.
import java.io.IOException;//catches errors and prints a message instead of crashing the program.
import java.nio.file.*;//creating, reading, and writing the transaction.csv file
import java.time.*;//brings LocalDate, LocalTime, and LocalDateTime classes into program.
import java.time.format.DateTimeFormatter;//Formatting time when saving to CSV
import java.math.BigDecimal;//for more precise decimal numbers than double.
import java.util.ArrayList;//stores transactions
import java.util.Comparator;//sorts transactions by date/time
import java.util.stream.Collectors;//filters and collects lists
public class AccountingLedgarApp {

    public static void main(String[] args) {
        TransactionManager manager = new TransactionManager("transactions.csv");//manages and displays transactions from the CSV file.
        Reports reports = new Reports(manager);//allows Reports class to access transactions in TransactionManager
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            // Display home menu
            System.out.println("\n=== HOME MENU ===");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");
            System.out.print("Choose: ");

            // Read user input and convert to uppercase
            String choice = scanner.nextLine().trim().toUpperCase();

            // Handle choices using if-else statements
            if (choice.equals("D")) {
                // Add deposit transaction (positive amount)
                manager.addTransaction(scanner, true);
            } else if (choice.equals("P")) {
                // Add payment (negative amount)
                manager.addTransaction(scanner, false);
            } else if (choice.equals("L")) {
                // Open ledger menu
                showLedgerMenu (scanner, manager, reports);
            } else if (choice.equals("X")) {
                // Exit program
                System.out.println("Exiting... Goodbye!");
                running = false;
            } else {
                // Invalid input
                System.out.println("Invalid.try again.");
            }
        }
        // Close the scanner
        scanner.close();
    }

    private static void showLedgerMenu(Scanner scanner, TransactionManager manager, Reports reports) {
        boolean inLedger = true;
        while (inLedger) {
            System.out.println("\n=== LEDGER MENU ===");
            System.out.println("A) All Transactions");
            System.out.println("D) Deposits Only");
            System.out.println("P) Payments Only");
            System.out.println("R) Reports");
            System.out.println("H) Back to Home");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.equals("A")) {
                // Display all transactions
                manager.display(manager.getAll());
            } else if (choice.equals("D")) {
                // Display deposits only
                manager.display(manager.getDeposits());
            } else if (choice.equals("P")) {
                // Display payments only
                manager.display(manager.getPayments());
            } else if (choice.equals("R")) {
                // Go to reports menu
                showReportsMenu(scanner, reports);
            } else if (choice.equals("H")) {
                // Return to home menu
                inLedger = false;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    // ------------------ Reports Menu ------------------
    private static void showReportsMenu(Scanner scanner, Reports reports) {
        boolean inReports = true;
        while (inReports) {
            System.out.println("\n=== REPORTS MENU ===");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("0) Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                reports.showMonthToDate();
            } else if (choice.equals("2")) {
                reports.showPreviousMonth();
            } else if (choice.equals("3")) {
                reports.showYearToDate();
            } else if (choice.equals("4")) {
                reports.showPreviousYear();
            } else if (choice.equals("5")) {
                System.out.print("Enter vendor name: ");
                String vendor = scanner.nextLine();
                reports.showByVendor(vendor);
            } else if (choice.equals("0")) {
                inReports = false; // Back to ledger menu
            } else {
                System.out.println("Invalid. try again.");
            }
        }
    }
}
// ------------------ Transaction Class ------------------
class Transaction {
    private LocalDate date;
    private LocalTime time;
    private String description;
    private String vendor;
    private BigDecimal amount;

    // Constructor to create a new transaction
    public Transaction(LocalDate date, LocalTime time, String description, String vendor, BigDecimal amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
    }

    // Getter methods for getting private fields
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getVendor() { return vendor; }
    public BigDecimal getAmount() { return amount; }

    // Converts transaction into a CSV string to be saved in csv file
    public String toCSV() {
        return date + "|" + time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                "|" + description + "|" + vendor + "|" + amount;
    }

    // Reads a transaction from a CSV line and creates a Transaction object
    public static Transaction fromCSV(String line) {
        String[] parts = line.split("\\|");
        return new Transaction(
                LocalDate.parse(parts[0]),         // date (e.g., 2023-04-15)
                LocalTime.parse(parts[1]),         // time (e.g., 10:13:25)
                parts[2],                          // description
                parts[3],                          // vendor
                new BigDecimal(parts[4])           // amount
        );
    }

    // Custom display method
    public String displayTransaction() {
        // Shorten long descriptions to 25 characters max
        String desc = description.length() > 25
                ? description.substring(0, 22) + "..."
                : description;

        // Pad shorter descriptions with spaces
        while (desc.length() < 25) {
            desc += " ";
        }

        // Shorten vendor names longer than 15 characters
        String vend = vendor.length() > 15
                ? vendor.substring(0, 12) + "..."
                : vendor;

        // Pad shorter vendor names with spaces
        while (vend.length() < 15) {
            vend += " ";
        }

        // formats transaction into a table-like string
        return date + "  " + time + "  " + desc + "  " + vend + "  " + String.format("%.2f", amount);
    }
}

// ------------------ TransactionManager Class ------------------
class TransactionManager {
    private String filePath;                   // Path to CSV file
    private ArrayList<Transaction> transactions; // List to store all transactions
    private static final String HEADER = "date|time|description|vendor|amount";

    // Constructor
    public TransactionManager(String filePath) {
        this.filePath = filePath;
        transactions = new ArrayList<>();
        createFileIfMissing(); // Ensure CSV exists with header
        loadTransactions();    // Load transactions from file
    }

    // Create CSV file if it doesn't exist
    private void createFileIfMissing() {
        Path path = Path.of(filePath);
        if (Files.exists(path)) {
            return; // File already exists, nothing to do
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            // Write header
            writer.write(HEADER);
            writer.newLine();
            // Add some default transactions
            writer.write("2023-04-15|10:13:25|ergonomic keyboard|Amazon|-89.50");
            writer.newLine();
            writer.write("2023-04-15|11:15:00|Invoice 1001 paid|Joe|1500.00");
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error creating CSV: " + e.getMessage());
        }
    }

    // Load transactions from CSV
    private void loadTransactions() {
        try {
            ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(Path.of(filePath));
            for (String line : lines) {
                // Skip header or blank lines
                if (line.startsWith("date|") || line.isBlank()) {
                    continue;
                }
                Transaction t = Transaction.fromCSV(line); // Convert line to Transaction object
                transactions.add(t);
            }
        } catch (IOException e) {
            System.out.println("Error loading CSV: " + e.getMessage());
        }
    }

    // Save transactions to CSV
    private void saveTransactions() {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filePath))) {
            writer.write(HEADER);
            writer.newLine();
            for (Transaction t : transactions) {
                writer.write(t.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving CSV: " + e.getMessage());
        }
    }

    // Add a new transaction
    public void addTransaction(Scanner scanner, boolean isDeposit) {
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        System.out.print("Enter vendor: ");
        String vendor = scanner.nextLine();

        System.out.print("Enter amount: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        if (!isDeposit) {
            amount = amount.negate(); // Make payment negative
        }

        Transaction t = new Transaction(LocalDate.now(), LocalTime.now(), description, vendor, amount);
        transactions.add(t);

        saveTransactions(); // Save updated list to file
        System.out.println("Transaction added successfully!");
    }

    // Get all transactions (newest first)
    public ArrayList<Transaction> getAll() {
        ArrayList<Transaction> all = new ArrayList<>();
        // Loop through transactions from last to first
        for (int i = transactions.size() - 1; i >= 0; i--) {
            all.add(transactions.get(i));
        }
        return all;
    }

    // Get only deposits
    public ArrayList<Transaction> getDeposits() {
        ArrayList<Transaction> deposits = new ArrayList<>();
        for (Transaction t : getAll()) {
            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                deposits.add(t);
            }
        }
        return deposits;
    }

    // Get only payments
    public ArrayList<Transaction> getPayments() {
        ArrayList<Transaction> payments = new ArrayList<>();
        for (Transaction t : getAll()) {
            if (t.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                payments.add(t);
            }
        }
        return payments;
    }

    // Get transactions for a specific vendor
    public ArrayList<Transaction> getByVendor(String vendorName) {
        ArrayList<Transaction> results = new ArrayList<>();
        for (Transaction t : getAll()) {
            if (t.getVendor().equalsIgnoreCase(vendorName)) {
                results.add(t);
            }
        }
        return results;
    }

    // Get transactions within a date range
    public ArrayList<Transaction> getByDateRange(LocalDate start, LocalDate end) {
        ArrayList<Transaction> results = new ArrayList<>();
        for (Transaction t : getAll()) {
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                results.add(t);
            }
        }
        return results;
    }

    // Display a list of transactions
    public void display(ArrayList<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.printf("%-12s %-10s %-25s %-15s %10s%n", "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("===========================================================================");

        for (Transaction t : list) {
            System.out.println(t.displayTransaction());
        }
    }
}

// ------------------ Reports Class ------------------
class Reports {
    private TransactionManager manager; // Reference to the transaction manager

    // Constructor
    public Reports(TransactionManager manager) {
        this.manager = manager;
    }

    // Show transactions for the current month
    public void showMonthToDate() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1); // First day of this month
        LocalDate endDate = LocalDate.now(); // Today's date
        ArrayList<Transaction> results = manager.getByDateRange(startDate, endDate); // Get transactions in range
        manager.display(results); // Display results
    }

    // Show transactions for the previous month
    public void showPreviousMonth() {
        LocalDate firstDayLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1); // First day of previous month
        LocalDate lastDayLastMonth = firstDayLastMonth.plusMonths(1).minusDays(1); // Last day of previous month
        ArrayList<Transaction> results = manager.getByDateRange(firstDayLastMonth, lastDayLastMonth);
        manager.display(results);
    }

    // Show transactions for the current year
    public void showYearToDate() {
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1); // January 1st of current year
        LocalDate today = LocalDate.now(); // Today's date
        ArrayList<Transaction> results = manager.getByDateRange(firstDayOfYear, today);
        manager.display(results);
    }

    // Show transactions for the previous year
    public void showPreviousYear() {
        LocalDate firstDayPrevYear = LocalDate.now().minusYears(1).withDayOfYear(1); // Jan 1 of last year
        LocalDate lastDayPrevYear = firstDayPrevYear.withMonth(12).withDayOfMonth(31); // Dec 31 of last year
        ArrayList<Transaction> results = manager.getByDateRange(firstDayPrevYear, lastDayPrevYear);
        manager.display(results);
    }

    // Show transactions for a specific vendor
    public void showByVendor(String vendor) {
        ArrayList<Transaction> results = manager.getByVendor(vendor); // Get all transactions for vendor
        manager.display(results); // Display results
    }
}