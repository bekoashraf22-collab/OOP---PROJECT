package main_classes;

import java.time.LocalDate;
import enums.Role;
import exceptions.WeakPasswordException;
import exceptions.InvalidUsernameException;
import exceptions.UnderageGuestException;

public class Admin extends Staff {

    // FIX: Added UnderageGuestException to the throws clause
    public Admin(String username, String password, LocalDate dateOfBirth, int workingHours) 
           throws WeakPasswordException, InvalidUsernameException, UnderageGuestException {
        super(username, password, dateOfBirth, Role.ADMIN, workingHours);
    }

    @Override
    public void displayMenu() {
        System.out.println("\n--- ADMIN CONTROL PANEL ---");
        System.out.println("1. Add/Remove Rooms (CRUD)");
        System.out.println("2. Edit Room Amenities");
        System.out.println("3. View System Logs");
        System.out.println("4. Logout");
    }

    public void manageRooms() {
        System.out.println("Admin " + getUsername() + " is accessing room database.");
    }
    
    @Override
    public void processPayment(double amount, enums.PaymentMethod method) throws exceptions.InvalidPaymentException {
        System.out.println("Staff payment not applicable.");
    }
}