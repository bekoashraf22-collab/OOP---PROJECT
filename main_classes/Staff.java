package main_classes ;
import java.time.LocalDate;
// Importing from your other packages
import enums.Role;
import exceptions.WeakPasswordException;
import exceptions.InvalidUsernameException;

public abstract class Staff extends User {
    private int workingHours;
    private Role role; // ADMIN or RECEPTIONIST

    public Staff(String username, String password, LocalDate dateOfBirth, Role role, int workingHours) 
           throws WeakPasswordException, InvalidUsernameException {
        
        // Send identity data up to the User parent constructor
        super(username, password, dateOfBirth);
        
        this.role = role;
        this.workingHours = workingHours;
    }

    // --- Getters & Setters ---
    public int getWorkingHours() { return workingHours; }
    
    public void setWorkingHours(int workingHours) {
        if (workingHours > 0) {
            this.workingHours = workingHours;
        } else {
            System.out.println("Working hours must be positive.");
        }
    }

    public Role getRole() { return role; }

    // Staff-specific behavior
    public void clockIn() {
        System.out.println(getUsername() + " clocked in as " + role);
    }
}

