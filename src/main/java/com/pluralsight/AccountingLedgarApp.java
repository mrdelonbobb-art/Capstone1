package com.pluralsight;
import java.util.Scanner;//gets user input
import java.nio.file.*;//creating, reading, and writing the transaction.csv file
import java.time.*;//brings LocalDate, LocalTime, and LocalDateTime classes into program.
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

            // Reads input
            String choice = scanner.nextLine().trim().toUpperCase();

            // Handle choices with if-else statements
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
                System.out.println("Bye!");
                running = false;
            } else {
                // Invalid input
                System.out.println("Invalid.try again.");
            }
        }
        // Closes the scanner
        scanner.close();
    }

    private static void showLedgerMenu(Scanner scanner, TransactionManager manager, Reports reports) {
        boolean inLedger = true;
        while (inLedger) { //creates loop to stay in Ledgermenu until user exits
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
    //Reports Menu________________
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

            String choice = scanner.nextLine().trim().toUpperCase();

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