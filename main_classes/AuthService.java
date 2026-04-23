package main_classes;

public class AuthService {

    public static User login(String username, String password) {
        
        // 1. Check if the user is a Guest
        for (Guest g : HotelDatabase.guests) {
            if (g.getUsername().equals(username) && g.getPassword().equals(password)) {
                return g;
            }
        }
        
        // 2. Check if the user is a Staff member (Admin or Receptionist)
        for (Staff s : HotelDatabase.staffMembers) {
            if (s.getUsername().equals(username) && s.getPassword().equals(password)) {
                return s;
            }
        }
        
        // 3. If no match is found
        System.out.println("Login Failed: Invalid credentials.");
        return null;
    }
}