package main_classes ;
import java.time.LocalDate;
import enums.Role;
import exceptions.WeakPasswordException;
import exceptions.InvalidUsernameException;
import exceptions.UnderageGuestException;

public abstract class Staff extends User {
    private int workingHours;
    private Role role; 

    
    public Staff(String username, String password, LocalDate dateOfBirth, Role role, int workingHours) 
           throws WeakPasswordException, InvalidUsernameException, UnderageGuestException {
        
        super(username, password, dateOfBirth);
        
        this.role = role;
        this.workingHours = workingHours;
    }

    public int getWorkingHours() { return workingHours; }
    
    public void setWorkingHours(int workingHours) {
        if (workingHours > 0) {
            this.workingHours = workingHours;
        } else {
            System.out.println("Working hours must be positive.");
        }
    }

    public Role getRole() { return role; }

    public void clockIn() {
        System.out.println(getUsername() + " clocked in as " + role);
    }
}