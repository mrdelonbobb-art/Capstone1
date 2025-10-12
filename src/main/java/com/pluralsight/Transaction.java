package com.pluralsight;
import java.math.BigDecimal;//avoid rounding errors with float and doubles
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;//Formatting time when saving to CSV

public class Transaction {
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
        public static com.pluralsight.Transaction fromCSV(String line) {
            String[] parts = line.split("\\|");
            return new com.pluralsight.Transaction(
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

