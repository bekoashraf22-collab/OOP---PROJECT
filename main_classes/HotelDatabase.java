package main_classes;

import java.util.ArrayList;
import java.time.LocalDate;
import enums.Gender;

public class HotelDatabase {
    public static ArrayList<Guest> guests = new ArrayList<>();
    public static ArrayList<Staff> staffMembers = new ArrayList<>();

    // Static block to pre-populate data on startup
    static {
        try {
            // Adding a Guest
            guests.add(new Guest("Abdullah", "pass1234", LocalDate.of(2005, 1, 1), 
                                 3000.0, "Cairo", Gender.MALE, "High floor"));
            
            // Adding an Admin
            staffMembers.add(new Admin("Admin_User", "adminPass789", LocalDate.of(1990, 5, 20), 40));
            
            // Adding a Receptionist
            staffMembers.add(new Receptionist("Staff_A", "staffPass123", LocalDate.of(1998, 3, 10), 35));
            
        } catch (Exception e) {
            System.out.println("System Error: Failed to pre-populate dummy data: " + e.getMessage());
        }
    }
}