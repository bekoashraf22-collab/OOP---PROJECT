
package main_classes;

import java.time.LocalDate;
import enums.Gender;
import enums.PaymentMethod;
import exceptions.*; // This imports all your exceptions safely

public class Guest extends User {
    private double balance;
    private String address;
    private Gender gender; 
    private String roomPreferences;

    // Exception is passed up the chain here
    public Guest(String username, String password, LocalDate dateOfBirth, 
                 double balance, String address, Gender gender, String roomPreferences) 
                 throws WeakPasswordException, InvalidUsernameException, UnderageGuestException {
        
        super(username, password, dateOfBirth);
        
        setBalance(balance);
        this.address = address;
        this.gender = gender;
        this.roomPreferences = roomPreferences;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if (balance >= 0) {
            this.balance = balance;
        } else {
            System.out.println("Validation Error: Balance cannot be negative. Setting to 0.");
            this.balance = 0;
        }
    }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRoomPreferences() { return roomPreferences; }
    public void setRoomPreferences(String roomPreferences) { this.roomPreferences = roomPreferences; }

    @Override
    public void processPayment(double amount, PaymentMethod method) throws InvalidPaymentException {
        if (amount <= 0) {
            throw new InvalidPaymentException("Payment Error: Invalid amount requested.");
        }
        
        if (amount > this.balance) {
            throw new InvalidPaymentException("Security Alert: Insufficient funds for transaction.");
        }

        this.balance -= amount;
        System.out.println("Payment Successful! $" + amount + " deducted via " + method);
    }

    @Override
    public void displayMenu() {
        System.out.println("\n********** GUEST MENU **********");
        System.out.println("User: " + getUsername());
        System.out.println("Balance: $" + balance);
        System.out.println("1. View Available Rooms");
        System.out.println("2. Make a Reservation");
        System.out.println("3. View My Reservations");
        System.out.println("4. Checkout & Pay Invoice");
        System.out.println("5. Logout");
        System.out.print("Select an option: ");
    }
}