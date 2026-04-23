package main_classes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Invoice {
    private String invoiceId;
    private Guest guest;
    private List<Booking> bookings; 
    private double taxRate = 0.14;  
    private double discount;
    private LocalDateTime issuedDate;

    public Invoice(String invoiceId, Guest guest, List<Booking> bookings, double discount) {
        this.invoiceId = invoiceId;
        this.guest = guest;
        this.bookings = bookings;
        this.discount = discount;
        this.issuedDate = LocalDateTime.now();
    }

    // Helper method to calculate the base price
    private double calculateBasePrice() {
        double total = 0;
        for (Booking b : bookings) {
            double dailyRate = b.getRoom().getRoomType().getBasePrice();
            for (Amenity a : b.getRoom().getAmenities()) {
                dailyRate += a.getExtraCost();
            }
            total += (dailyRate * b.getNights());
        }
        return total;
    }

    // Method to calculate price after tax and discount
    public double calculateFinalAmount() {
        double basePrice = calculateBasePrice(); 
        double taxAmount = basePrice * taxRate;
        return (basePrice + taxAmount) - discount;
    }

    // Method to print the bill 
    public void printInvoice() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        System.out.println("\n=========================================");
        System.out.println("             HOTEL INVOICE               ");
        System.out.println("=========================================");
        System.out.println("Invoice ID : " + invoiceId);
        System.out.println("Date       : " + issuedDate.format(formatter));
        System.out.println("Guest Name : " + guest.getUsername());
        System.out.println("-----------------------------------------");
        
        for (Booking b : bookings) {
            System.out.println("Room " + b.getRoom().getRoomNumber() + " (" + b.getNights() + " nights)");
        }
        
        System.out.println("-----------------------------------------");
        System.out.println("Subtotal   : $" + String.format("%.2f", calculateBasePrice()));
        System.out.println("Tax (14%)  : $" + String.format("%.2f", (calculateBasePrice() * taxRate)));
        System.out.println("Discount   : -$" + String.format("%.2f", discount));
        System.out.println("-----------------------------------------");
        System.out.println("FINAL DUE  : $" + String.format("%.2f", calculateFinalAmount()));
        System.out.println("=========================================\n");
    }
}