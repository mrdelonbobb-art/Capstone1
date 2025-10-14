package com.pluralsight;
import java.time.LocalDate;
import java.util.ArrayList;//imports arraylist from TransactionManager

public class Reports {
    private TransactionManager manager; // Reference to the TransactionManager

    // Constructor: connects this class to the TransactionManager
    public Reports(TransactionManager manager) {
        this.manager = manager;
    }
    //MONTH TO DATE REPORT______
    public void showMonthToDate() {
        // Gets all transactions from the manager
        ArrayList<Transaction> allTransactions = manager.getAll();
        //Create a new list to store transactions only for this month
        ArrayList<Transaction> monthTransactions = new ArrayList<>();
        //Determines start and end dates for current month
        LocalDate startDate = LocalDate.now().withDayOfMonth(1); // First day of this month
        LocalDate endDate = LocalDate.now(); // Today

        //Loops through each transaction to check if it falls in this month
        for (Transaction transaction : allTransactions) {
            LocalDate transactionDate = transaction.getDate(); // Gets transaction date
            boolean afterOrSameAsStart = !transactionDate.isBefore(startDate); // On or after start
            boolean beforeOrSameAsEnd = !transactionDate.isAfter(endDate); // On or before today

            //If transaction is in current month add it to the list
            if (afterOrSameAsStart && beforeOrSameAsEnd) {
                monthTransactions.add(transaction);
            }
        }
        //Displays the filtered transactions
        System.out.println("\n=== Month To Date Transactions ===");
        manager.display(monthTransactions);
    }

    //PREVIOUS MONTH REPORT_________________________________
    public void showPreviousMonth() {
        // Determine the first and last day of the previous month
        LocalDate firstDayLastMonth = LocalDate.now() // Current date
                .minusMonths(1) // Move back one month
                .withDayOfMonth(1); // First day of that month

        LocalDate lastDayLastMonth = firstDayLastMonth// First day of previous month
                .plusMonths(1)  // Move to next month
                .minusDays(1); // Go back one day :last day of previous month

        //Gets all transactions from the manager
        ArrayList<Transaction> allTransactions = manager.getAll();

        //Creates list to store transactions from the previous month
        ArrayList<Transaction> previousMonthTransactions = new ArrayList<>();

        //Go through each transaction and check if it falls in previous month
        for (Transaction transaction : allTransactions) {
            LocalDate transactionDate = transaction.getDate(); // Get the date of the transaction
            boolean onOrAfterFirstDay = !transactionDate.isBefore(firstDayLastMonth); // On/after start
            boolean onOrBeforeLastDay = !transactionDate.isAfter(lastDayLastMonth);   // On/before end

            //If transaction is within the previous month, add it to the list
            if (onOrAfterFirstDay && onOrBeforeLastDay) {
                previousMonthTransactions.add(transaction);
            }
        }

        //Displays the filtered transactions
        System.out.println("\n=== Previous Month Transactions ===");
        manager.display(previousMonthTransactions);
    }

    //YEAR TO DATE REPORT______________________________________
    public void showYearToDate() {
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1); // January 1 of current year
        LocalDate today = LocalDate.now(); // today's date

        ArrayList<Transaction> allTransactions = manager.getAll();
        ArrayList<Transaction> results = new ArrayList<>();

        for (Transaction t : allTransactions) {
            if (!t.getDate().isBefore(firstDayOfYear) && !t.getDate().isAfter(today)) {
                results.add(t);
            }
        }

        System.out.println("\n=== Year To Date Transactions ===");
        manager.display(results);
    }

    //PREVIOUS YEAR REPORT________________________
    public void showPreviousYear() {
        LocalDate firstDayPrevYear = LocalDate.now().minusYears(1).withDayOfYear(1); // January 1 last year
        LocalDate lastDayPrevYear = firstDayPrevYear.withMonth(12).withDayOfMonth(31); // December 31 last year

        ArrayList<Transaction> allTransactions = manager.getAll();
        ArrayList<Transaction> results = new ArrayList<>();

        for (Transaction t : allTransactions) {
            if (!t.getDate().isBefore(firstDayPrevYear) && !t.getDate().isAfter(lastDayPrevYear)) {
                results.add(t);
            }
        }

        System.out.println("\n=== Previous Year Transactions ===");
        manager.display(results);
    }

    //SEARCH BY VENDOR REPORT ____________________________________
    public void showByVendor(String vendorName) {
        ArrayList<Transaction> allTransactions = manager.getAll();
        ArrayList<Transaction> results = new ArrayList<>();

        // Loops through all transactions t0 find specific vendor
        for (Transaction t : allTransactions) {
            if (t.getVendor().equalsIgnoreCase(vendorName)) {
                results.add(t);
            }
        }

        System.out.println("\n=== Transactions for Vendor: " + vendorName + " ===");
        manager.display(results);
    }
}
