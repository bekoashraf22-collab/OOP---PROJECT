package main_classes;

import java.time.LocalDate;
import enums.Role;
import exceptions.WeakPasswordException;
import exceptions.InvalidUsernameException;
import exceptions.UnderageGuestException;

public class Receptionist extends Staff {

    // FIX: Added UnderageGuestException to the throws clause
    public Receptionist(String username, String password, LocalDate dateOfBirth, int workingHours) 
           throws WeakPasswordException, InvalidUsernameException, UnderageGuestException {
        super(username, password, dateOfBirth, Role.RECEPTIONIST, workingHours);
    }

    @Override
    public void displayMenu() {
        System.out.println("\n--- RECEPTIONIST DESK ---");
        System.out.println("1. Check-in Guest");
        System.out.println("2. Check-out Guest");
        System.out.println("3. Manage Room Keys");
        System.out.println("4. Logout");
    }
    
    @Override
    public void processPayment(double amount, enums.PaymentMethod method) throws exceptions.InvalidPaymentException {
        System.out.println("Staff payment not applicable.");
    }
}