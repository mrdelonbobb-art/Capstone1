package com.pluralsight;
import java.util.Scanner;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
public class AccountingApp {

    public static void main(String[] args) {
        TransactionManager manager = new TransactionManager("transactions.csv");
        Reports reports = new Reports(manager);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== HOME MENU ===");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "D" -> manager.addTransaction(scanner, true);
                case "P" -> manager.addTransaction(scanner, false);
                case "L" -> ledgerMenu(scanner, manager, reports);
                case "X" -> { System.out.println("Exiting... Goodbye!"); return; }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void ledgerMenu(Scanner scanner, TransactionManager manager, Reports reports) {
        while (true) {
            System.out.println("\n=== LEDGER MENU ===");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "A" -> manager.display(manager.getAll());
                case "D" -> manager.display(manager.getDeposits());
                case "P" -> manager.display(manager.getPayments());
                case "R" -> reportsMenu(scanner, reports);
                case "H" -> { return; }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void reportsMenu(Scanner scanner, Reports reports) {
        while (true) {
            System.out.println("\n=== REPORTS MENU ===");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("0) Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> reports.showMonthToDate();
                case "2" -> reports.showPreviousMonth();
                case "3" -> reports.showYearToDate();
                case "4" -> reports.showPreviousYear();
                case "5" -> {
                    System.out.print("Enter vendor name: ");
                    reports.showByVendor(scanner.nextLine());
                }
                case "0" -> { return; }
                default -> System.out.println("Invalid choice. Try again.");
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

    public Transaction(LocalDate date, LocalTime time, String description, String vendor, BigDecimal amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
    }

    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getVendor() { return vendor; }
    public BigDecimal getAmount() { return amount; }

    public String toCSV() {
        return date + "|" + time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                "|" + description + "|" + vendor + "|" + amount;
    }

    public static Transaction fromCSV(String line) {
        String[] parts = line.split("\\|");
        return new Transaction(
                LocalDate.parse(parts[0]),
                LocalTime.parse(parts[1]),
                parts[2],
                parts[3],
                new BigDecimal(parts[4])
        );
    }

    @Override
    public String toString() {
        String desc = description.length() > 25 ? description.substring(0, 22) + "..." : description;
        while (desc.length() < 25) desc += " ";
        String vend = vendor.length() > 15 ? vendor.substring(0, 12) + "..." : vendor;
        while (vend.length() < 15) vend += " ";
        return date + "  " + time + "  " + desc + "  " + vend + "  " + String.format("%.2f", amount);
    }
}

// ------------------ TransactionManager Class ------------------
class TransactionManager {
    private String filePath;
    private ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String HEADER = "date|time|description|vendor|amount";

    public TransactionManager(String filePath) {
        this.filePath = filePath;
        createFileIfMissing();
        loadTransactions();
    }

    private void createFileIfMissing() {
        Path path = Path.of(filePath);
        if (Files.exists(path)) return;
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(HEADER);
            writer.newLine();
            writer.write("2023-04-15|10:13:25|ergonomic keyboard|Amazon|-89.50");
            writer.newLine();
            writer.write("2023-04-15|11:15:00|Invoice 1001 paid|Joe|1500.00");
            writer.newLine();
        } catch (IOException e) { System.out.println("Error creating CSV: " + e.getMessage()); }
    }

    private void loadTransactions() {
        try {
            for (String line : Files.readAllLines(Path.of(filePath))) {
                if (line.startsWith("date|") || line.isBlank()) continue;
                transactions.add(Transaction.fromCSV(line));
            }
        } catch (IOException e) { System.out.println("Error loading CSV: " + e.getMessage()); }
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filePath))) {
            writer.write(HEADER); writer.newLine();
            for (Transaction t : transactions) { writer.write(t.toCSV()); writer.newLine(); }
        } catch (IOException e) { System.out.println("Error saving CSV: " + e.getMessage()); }
    }

    public void addTransaction(Scanner scanner, boolean isDeposit) {
        System.out.print("Enter description: "); String desc = scanner.nextLine();
        System.out.print("Enter vendor: "); String vendor = scanner.nextLine();
        System.out.print("Enter amount: "); BigDecimal amount = new BigDecimal(scanner.nextLine());
        if (!isDeposit) amount = amount.negate();
        transactions.add(new Transaction(LocalDate.now(), LocalTime.now(), desc, vendor, amount));
        save(); System.out.println("Transaction added!");
    }

    public ArrayList<Transaction> getAll() {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate)
                        .thenComparing(Transaction::getTime).reversed())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Transaction> getDeposits() {
        return getAll().stream().filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Transaction> getPayments() {
        return getAll().stream().filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Transaction> getByVendor(String vendor) {
        return getAll().stream().filter(t -> t.getVendor().equalsIgnoreCase(vendor))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Transaction> getByDateRange(LocalDate start, LocalDate end) {
        return getAll().stream().filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void display(ArrayList<Transaction> list) {
        if (list.isEmpty()) { System.out.println("No transactions found."); return; }
        System.out.printf("%-12s %-10s %-25s %-15s %10s%n","Date","Time","Description","Vendor","Amount");
        System.out.println("=".repeat(75));
        list.forEach(System.out::println);
    }
}

// ------------------ Reports Class ------------------
class Reports {
    private TransactionManager manager;
    public Reports(TransactionManager manager) { this.manager = manager; }

    public void showMonthToDate() { manager.display(manager.getByDateRange(LocalDate.now().withDayOfMonth(1), LocalDate.now())); }
    public void showPreviousMonth() {
        LocalDate start = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        manager.display(manager.getByDateRange(start, end));
    }
    public void showYearToDate() { manager.display(manager.getByDateRange(LocalDate.now().withDayOfYear(1), LocalDate.now())); }
    public void showPreviousYear() {
        LocalDate start = LocalDate.now().minusYears(1).withDayOfYear(1);
        LocalDate end = start.withMonth(12).withDayOfMonth(31);
        manager.display(manager.getByDateRange(start, end));
    }
    public void showByVendor(String vendor) { manager.display(manager.getByVendor(vendor)); }
}
