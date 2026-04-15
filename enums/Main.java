import main_classes.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Hotel System Login ===");
        
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        // Simple Login Test logic
        boolean found = false;
        for (Guest g : HotelDatabase.guests) {
            if (g.login(user, pass)) {
                g.displayMenu();
                found = true;
                break;
            }
        }
        
        if (!found) {
            for (Staff s : HotelDatabase.staffMembers) {
                if (s.login(user, pass)) {
                    s.displayMenu();
                    found = true;
                    break;
                }
            }
        }

        if (!found) System.out.println("Login Failed: Incorrect username or password.");
    }
}