package com.pluralsight;

import java.io.BufferedWriter;
import java.io.IOException;//catches errors and prints a message instead of crashing the program.
import java.math.BigDecimal;//for more precise decimal numbers than double.
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;//stores transactions
import java.util.Scanner;

public class TransactionManager {

    private String filePath; // path to the transactions.csv file
    private ArrayList<Transaction> transactions = new ArrayList<>(); // stores all transactions
    private static final String HEADER = "date|time|description|vendor|amount"; // CSV header

    //CONSTRUCTOR______________
    public TransactionManager(String filePath) {
        this.filePath = filePath;
        createFileIfMissing(); // make sure the CSV file exists
        loadTransactions();    // load transactions from the CSV file
    }

    // CREATE CSV FILE IF MISSING___________
    private void createFileIfMissing() {
        Path path = Path.of(filePath);
        // Check if file already exists
        if (Files.exists(path)) {
            return; // Do nothing if file already exists
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            // Write header and sample data
            writer.write(HEADER);
            writer.newLine();
            writer.write("2023-04-15|10:13:25|ergonomic keyboard|Amazon|-89.50");
            writer.newLine();
            writer.write("2023-04-15|11:15:00|Invoice 1001 paid|Joe|1500.00");
            writer.newLine();
            System.out.println("transactions.csv created with sample data.");
        } catch (IOException e) {
            System.out.println("Error creating CSV: " + e.getMessage());
        }
    }

    // LOAD TRANSACTIONS FROM FILE___________
    private void loadTransactions() {
        try {
            for (String line : Files.readAllLines(Path.of(filePath))) {
                // Skip header or blank lines
                if (line.startsWith("date|") || line.isBlank()) {
                    continue;
                }
                // Convert line to Transaction object and add to list
                Transaction t = Transaction.fromCSV(line);
                transactions.add(t);
            }
        } catch (IOException e) {
            System.out.println("Error loading CSV: " + e.getMessage());
        }
    }

    // SAVE TRANSACTIONS BACK TO FILE____________
    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filePath))) {
            writer.write(HEADER);
            writer.newLine();

            // Write each transaction as a CSV line
            for (Transaction t : transactions) {
                writer.write(t.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving CSV: " + e.getMessage());
        }
    }

    // ADDS NEW TRANSACTION_____________________
    public void addTransaction(Scanner scanner, boolean isDeposit) {
        System.out.print("Enter description: ");
        String desc = scanner.nextLine();

        System.out.print("Enter vendor: ");
        String vendor = scanner.nextLine();

        System.out.print("Enter amount: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        // If this is a payment, make it a negative number
        if (!isDeposit) {
            amount = amount.negate();
        }

        // Create a new Transaction with the current date and time
        Transaction newTransaction = new Transaction(
                LocalDate.now(),
                LocalTime.now(),
                desc,
                vendor,
                amount
        );

        transactions.add(newTransaction); // add it to our list
        save(); // save the updated list to CSV
        System.out.println("Transaction added successfully!");
    }

    //GET ALL TRANSACTIONS______________________
    public ArrayList<Transaction> getAll() { //declared public getAll method
        ArrayList<Transaction> sortedList = new ArrayList<>();  //Creates new empty list to sort data
        for (Transaction t : transactions) {
            sortedList.add(t);
        }   //Loops through each Transaction (t) in original list & puts them in sortedList. so the sorting doesn’t modify the original data.

        // newest transactions first
        for (int i = 0; i < sortedList.size(); i++) { //compares pairs of neighboring transactions
            for (int j = 0; j < sortedList.size() - 1 - i; j++) {
                Transaction t1 = sortedList.get(j);
                Transaction t2 = sortedList.get(j + 1);

                // Compare dates first
                if (t1.getDate().isBefore(t2.getDate())) {
                    // Swap if t1 is older than t2
                    sortedList.set(j, t2);
                    sortedList.set(j + 1, t1);
                }
                // If dates are equal, compare times
                else if (t1.getDate().isEqual(t2.getDate()) && t1.getTime().isBefore(t2.getTime())) {
                    // Swap if t1 is earlier in the same day
                    sortedList.set(j, t2);
                    sortedList.set(j + 1, t1);
                }
            }
        }

        // 3. Return the sorted list
        return sortedList;
    }

    // GET DEPOSITS_______________________________________
    public ArrayList<Transaction> getDeposits() { //public method named getDeposits which returns an ArrayList containing objects of type Transaction.
        ArrayList<Transaction> deposits = new ArrayList<>();//Creates empty list named deposits.This stores all Transaction objects that are deposits (positive).
        for (Transaction t : transactions) {
            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) { //Checks if the transaction amount is greater than zero.
                deposits.add(t);
            }
        }
        return deposits; //After looping through all transactions, the method returns the list with only deposits.
    }

    //GET PAYMENTS___________________________________________
    public ArrayList<Transaction> getPayments() {
        ArrayList<Transaction> payments = new ArrayList<>();
        for (Transaction t : transactions) {
            // Payments have negative amounts
            if (t.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                payments.add(t);
            }
        }
        return payments;
    }

    // GET TRANSACTIONS BY VENDOR --------------------
    public ArrayList<Transaction> getByVendor(String vendor) {
        ArrayList<Transaction> results = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getVendor().equalsIgnoreCase(vendor)) {
                results.add(t);
            }
        }
        return results;
    }

    //GET TRANSACTIONS BY DATE RANGE____________________________
    public ArrayList<Transaction> getByDateRange(LocalDate start, LocalDate end) {
        ArrayList<Transaction> results = new ArrayList<>();
        for (Transaction t : transactions) {
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                results.add(t);
            }
        }
        return results;
    }

    // DISPLAY TRANSACTIONS__________________________________________
    public void display(ArrayList<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        // Print a header row
        System.out.println("Date         Time       Description               Vendor          Amount");

        // Display each transaction using its displayTransaction() method
        for (Transaction t : list) {
            System.out.println(t.displayTransaction());
        }
    }
}
