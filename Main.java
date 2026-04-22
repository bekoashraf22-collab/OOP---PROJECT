import main_classes.*;
import enums.*;
import exceptions.*;

import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=========================================");
        System.out.println("  Welcome to the Hotel Management System ");
        System.out.println("=========================================");

        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Login");
            System.out.println("2. Register as new Guest");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");
            
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter Password: ");
                    String password = scanner.nextLine();

                    // Attempt login
                    User currentUser = AuthService.login(username, password);
                    
                    if (currentUser != null) {
                        boolean loggedIn = true;
                        
                        // User-specific session loop
                        while (loggedIn) {
                            
                            currentUser.displayMenu();
                            System.out.print("Select an action: ");
                            String userChoice = scanner.nextLine();
                            
                            // Handling actions based on User type
                            if (currentUser instanceof Admin) {
                                if (userChoice.equals("4")) {
                                    loggedIn = false; // Admin logout
                                } else if (userChoice.equals("1")) {
                                    ((Admin) currentUser).manageRooms();
                                } else {
                                    System.out.println("Feature coming soon!");
                                }
                            } 
                            else if (currentUser instanceof Receptionist) {
                                if (userChoice.equals("4")) {
                                    loggedIn = false; // Receptionist logout
                                } else {
                                    System.out.println("Feature coming soon!");
                                }
                            } 
                            else if (currentUser instanceof Guest) {
                                if (userChoice.equals("5")) {
                                    loggedIn = false; // Guest logout
                                } else if (userChoice.equals("4")) {
                                    // Testing the payment exception handling
                                    try {
                                        System.out.print("Enter amount to pay: $");
                                        double amount = Double.parseDouble(scanner.nextLine());
                                        currentUser.processPayment(amount, PaymentMethod.CREDIT_CARD);
                                    } catch (InvalidPaymentException e) {
                                        System.out.println(e.getMessage());
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid input. Please enter a valid number.");
                                    }
                                } else {
                                    System.out.println("Feature coming soon!");
                                }
                            }
                        }
                        System.out.println("Logged out successfully.");
                    }
                    break;

                case "2":
                    System.out.println("\n--- GUEST REGISTRATION ---");
                    System.out.print("Choose Username: ");
                    String newRegUser = scanner.nextLine();
                    
                    System.out.print("Choose Password (min 8 chars): ");
                    String newRegPass = scanner.nextLine();
                    
                    double balance = 0;
                    try {
                        System.out.print("Enter Initial Balance: $");
                        balance = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Balance set to $0.0");
                    }

                    System.out.print("Enter Address: ");
                    String address = scanner.nextLine();
                    
                    System.out.print("Enter Gender (1 for MALE, 2 for FEMALE): ");
                    String genderChoice = scanner.nextLine();
                    Gender gender = genderChoice.equals("1") ? Gender.MALE : Gender.FEMALE;

                    System.out.print("Room Preferences (e.g., High floor): ");
                    String pref = scanner.nextLine();

                    // Dummy date for testing
                    LocalDate dob = LocalDate.of(2000, 1, 1); 

                    // Register the user
                    AuthService.registerGuest(newRegUser, newRegPass, dob, balance, address, gender, pref);
                    break;

                case "3":
                    System.out.println("Exiting the system. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please enter 1, 2, or 3.");
            }
        }
        scanner.close();
    }
}