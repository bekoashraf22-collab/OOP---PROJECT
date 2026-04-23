package main_classes;

import java.time.LocalDateTime;

public class Invoice {
    private String invoiceId;
    private Reservations reservation;
    private double taxRate = 0.14;    // 14% standard tax
    private double discount;
    private LocalDateTime issuedDate;

    public Invoice(String invoiceId, Reservations reservation, double discount) {
        this.invoiceId = invoiceId;
        this.reservation = reservation;
        this.discount = discount;
        this.issuedDate = LocalDateTime.now();
    }

    //Method to calculate price after tax and discount
    public double calculateFinalAmount() {
        double basePrice = reservation.calculateTotalPrice(); // بينادي الميثود اللي عملناها سوا
        double taxAmount = basePrice * taxRate;
        return (basePrice + taxAmount) - discount;
    }

    // Method to print the bill
    public void printInvoice() {
        System.out.println("----- HOTEL INVOICE -----");
        System.out.println("Invoice ID: " + invoiceId);
        System.out.println("Guest: " + reservation.getGuest().getUsername());
        System.out.println("Room: " + reservation.getRoom().getRoomNumber());
        System.out.println("Total Price: " + reservation.calculateTotalPrice() + " EGP");
        System.out.println("Final (with Tax & Discount): " + calculateFinalAmount() + " EGP");
        System.out.println("Issued on: " + issuedDate);
        System.out.println("-------------------------");
    }
    public void processPayment() {
        double finalAmount = calculateFinalAmount();
        double guestBalance = reservation.getGuest().getBalance(); //

        if (guestBalance < finalAmount) {
            // customized exception for the app to not crash
            System.out.println("Payment Failed: Insufficient balance!");
        } else {
            // updating balance and reservation status
            reservation.getGuest().setBalance(guestBalance - finalAmount);
            reservation.setStatus(enums.ReservationStatus.COMPLETED);
            System.out.println("Payment Successful! Reservation is now COMPLETED.");
        }
    }
}