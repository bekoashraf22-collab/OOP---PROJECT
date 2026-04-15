package main_classes;

import java.time.LocalDate;

import enums.Gender;
import exceptions.InvalidUsernameException;
import exceptions.WeakPasswordException;

public class AuthService {

     
    public static void registerGuest(String user, String pass, LocalDate dob, double bal, String addr, Gender gen, String pref) {
        try {
            Guest newGuest = new Guest(user, pass, dob, bal, addr, gen, pref);
            HotelDatabase.guests.add(newGuest);
            System.out.println("Registration Successful for: " + user);
        } catch (WeakPasswordException | InvalidUsernameException e) {
            System.out.println("Registration Failed: " + e.getMessage());
        }
    }

    public static User login(String username, String password) {
        // Check Guests
        for (Guest g : HotelDatabase.guests) {
            if (g.getUsername().equals(username) && g.getPassword().equals(password)) {
                return g;
            }
        }
        for (Staff s : HotelDatabase.staffMembers) {
            if (s.getUsername().equals(username) && s.getPassword().equals(password)) {
                return s;
            }
        }
        System.out.println("Login Failed: Invalid credentials.");
        return null;
    }
}
