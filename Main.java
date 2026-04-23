import main_classes.*;
import enums.*;
import exceptions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.println("\n=========================================");
            System.out.println("      HOTEL MANAGEMENT SYSTEM            ");
            System.out.println("=========================================");
            System.out.println("1. Login");
            System.out.println("2. Register as new Guest");
            System.out.println("3. Exit");
            System.out.print("Select: ");
            
            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) handleLoginFlow(scanner);
            else if (choice.equals("2")) handleRegistration(scanner);
            else if (choice.equals("3")) running = false;
        }
        scanner.close();
    }

    private static void handleLoginFlow(Scanner scanner) {
        System.out.print("Username: ");
        String u = scanner.nextLine().trim();
        System.out.print("Password: ");
        String p = scanner.nextLine().trim();

        User currentUser = AuthService.login(u, p);
        if (currentUser != null) {
            HotelDatabase.logAction(currentUser.getUsername() + " logged in.");
            boolean loggedIn = true;
            
            if (currentUser instanceof Guest) {
                HotelDatabase.guestReservations.putIfAbsent(currentUser.getUsername(), new ArrayList<>());
            }

            while (loggedIn) {
                currentUser.displayMenu();
                String choice = scanner.nextLine().trim();
                
                if (currentUser instanceof Admin) {
                    loggedIn = handleAdminActions((Admin) currentUser, choice, scanner);
                } else if (currentUser instanceof Guest) {
                    loggedIn = handleGuestActions((Guest) currentUser, choice, scanner);
                } else if (currentUser instanceof Receptionist) {
                    loggedIn = handleReceptionistActions((Receptionist) currentUser, choice, scanner);
                }
            }
        }
    }

    // ==========================================
    // RECEPTIONIST ACTIONS
    // ==========================================
    private static boolean handleReceptionistActions(Receptionist receptionist, String choice, Scanner scanner) {
        switch (choice) {
            case "1": 
                System.out.print("Enter Guest Username to check-in: ");
                String checkinName = scanner.nextLine().trim();
                
                Guest actualGuest = null;
                for (Guest g : HotelDatabase.guests) {
                    if (g.getUsername().equalsIgnoreCase(checkinName)) {
                        actualGuest = g;
                        break;
                    }
                }
                
                if (actualGuest == null) {
                    System.out.println("Error: Guest '" + checkinName + "' not found. They must register first!");
                    break;
                }

                System.out.print("Enter Room Number to assign: ");
                String rNum = scanner.nextLine().trim();
                
                Room r = HotelDatabase.findRoom(rNum);
                if (r != null && r.isAvailable()) {
                    try {
                        System.out.print("Enter Check-Out Date (YYYY-MM-DD): ");
                        LocalDate checkOut = LocalDate.parse(scanner.nextLine().trim());
                        LocalDate checkIn = LocalDate.now(); 
                        
                        // FIXED: Uses Reservations
                        Reservations newReservation = new Reservations(actualGuest, r, checkIn, checkOut);
                        newReservation.setStatus(ReservationStatus.CONFIRMED);
                        
                        r.setAvailable(false);
                        String officialName = actualGuest.getUsername();
                        HotelDatabase.guestReservations.putIfAbsent(officialName, new ArrayList<>());
                        HotelDatabase.guestReservations.get(officialName).add(newReservation);
                        
                        System.out.println("Success: Checked " + officialName + " into Room " + rNum + " until " + checkOut);
                        HotelDatabase.logAction("Receptionist checked " + officialName + " into Room " + rNum);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Booking Error: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Error: Invalid date format. Use YYYY-MM-DD.");
                    }
                } else {
                    System.out.println("Error: Room is unavailable or does not exist.");
                }
                break;

            case "2": 
                System.out.print("Enter Guest Username to check-out: ");
                String checkoutName = scanner.nextLine().trim();
                
                Guest guestToCheckout = null;
                for (Guest g : HotelDatabase.guests) {
                    if (g.getUsername().equalsIgnoreCase(checkoutName)) {
                        guestToCheckout = g;
                        break;
                    }
                }
                
                if (guestToCheckout != null) {
                    handleAutomatedCheckout(guestToCheckout, receptionist, scanner);
                } else {
                    System.out.println("Error: Guest account not found in the system.");
                }
                break;

            case "3": 
                System.out.println("\n--- OCCUPIED ROOMS (KEY MANAGEMENT) ---");
                boolean anyOccupied = false;
                for (Room room : HotelDatabase.rooms) {
                    if (!room.isAvailable()) {
                        System.out.println("Room " + room.getRoomNumber() + " [" + room.getRoomType().getTypeName() + "] - OCCUPIED");
                        anyOccupied = true;
                    }
                }
                if (!anyOccupied) System.out.println("All rooms are currently vacant.");
                break;

            case "4": 
                return false;
        }
        return true;
    }

    // ==========================================
    // ADMIN ACTIONS 
    // ==========================================
    private static boolean handleAdminActions(Admin admin, String choice, Scanner scanner) {
        switch (choice) {
            case "1": 
                System.out.print("New Room Number: ");
                String rNum = scanner.nextLine().trim();
                System.out.println("Select Type:");
                for (int i = 0; i < HotelDatabase.roomTypes.size(); i++) {
                    System.out.println((i+1) + ". " + HotelDatabase.roomTypes.get(i).getTypeName() + " ($" + HotelDatabase.roomTypes.get(i).getBasePrice() + "/night)");
                }
                try {
                    int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    
                    if (HotelDatabase.findRoom(rNum) != null) {
                        throw new DuplicateRoomException("Room " + rNum + " already exists in the system!");
                    }

                    HotelDatabase.rooms.add(new Room(rNum, HotelDatabase.roomTypes.get(idx)));
                    System.out.println("Success: Room " + rNum + " created.");
                    HotelDatabase.logAction("Admin added room " + rNum);
                } catch (DuplicateRoomException e) {
                    System.out.println("Creation Failed: " + e.getMessage());
                } catch (Exception e) { 
                    System.out.println("Error: Invalid selection."); 
                }
                break;

            case "2": 
                System.out.print("Room Number to Modify: ");
                String rid = scanner.nextLine().trim();
                Room room = HotelDatabase.findRoom(rid);
                
                if (room != null) {
                    System.out.println("Select Amenity to Add:");
                    for (int i = 0; i < HotelDatabase.globalAmenities.size(); i++) {
                        System.out.println((i+1) + ". " + HotelDatabase.globalAmenities.get(i).getName() + " ($" + HotelDatabase.globalAmenities.get(i).getExtraCost() + "/night)");
                    }
                    try {
                        int aidx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        Amenity selectedAmenity = HotelDatabase.globalAmenities.get(aidx);
                        room.addAmenity(selectedAmenity);
                        System.out.println("Success: " + selectedAmenity.getName() + " added to Room " + rid);
                        HotelDatabase.logAction("Admin added " + selectedAmenity.getName() + " to room " + rid);
                    } catch (Exception e) { System.out.println("Error: Invalid selection."); }
                } else { System.out.println("Error: Room not found."); }
                break;

            case "3": 
                System.out.println("\n--- SYSTEM LOGS ---");
                if (HotelDatabase.systemLogs.isEmpty()) System.out.println("No logs available.");
                else for (String log : HotelDatabase.systemLogs) System.out.println(log);
                break;

            case "4": return false;
        }
        return true;
    }

    // ==========================================
    // GUEST ACTIONS 
    // ==========================================
    private static boolean handleGuestActions(Guest guest, String choice, Scanner scanner) {
        switch (choice) {
            case "1": 
                System.out.println("\n--- AVAILABLE ROOMS ---");
                boolean found = false;
                for (Room r : HotelDatabase.rooms) {
                    if (r.isAvailable()) {
                        System.out.println("Room " + r.getRoomNumber() + " [" + r.getRoomType().getTypeName() + 
                                           "] | Base Price: $" + r.getRoomType().getBasePrice() + "/night");
                        
                        if (r.getAmenities().isEmpty()) {
                            System.out.println("   -> Amenities: None\n");
                        } else {
                            System.out.println("   -> Amenities Included: " + r.getAmenities() + "\n");
                        }
                        found = true;
                    }
                }
                if (!found) System.out.println("No rooms currently available.");
                break;

            case "2": 
                System.out.print("Enter Room Number to Book: ");
                String bNum = scanner.nextLine().trim();
                Room bRoom = HotelDatabase.findRoom(bNum);
                
                if (bRoom == null || !bRoom.isAvailable()) {
                    System.out.println("Error: Room is unavailable or does not exist.");
                    break;
                }

                try {
                    System.out.print("Enter your Check-Out Date (YYYY-MM-DD): ");
                    LocalDate checkOut = LocalDate.parse(scanner.nextLine().trim());
                    LocalDate checkIn = LocalDate.now(); 
                    
                    Reservations newReservation = new Reservations(guest, bRoom, checkIn, checkOut);

                    List<Reservations> currentReservations = HotelDatabase.guestReservations.get(guest.getUsername());
                    
                    double pendingDebt = 0;
                    for (Reservations res : currentReservations) {
                        pendingDebt += res.calculateTotalPrice(); 
                    }
                    
                    double newBookingCost = newReservation.calculateTotalPrice();
                    double totalRequired = pendingDebt + newBookingCost;

                    System.out.println("\n--- FINANCIAL CHECK ---");
                    System.out.println("Your Current Balance: $" + guest.getBalance());
                    System.out.println("Total Amount Required: $" + totalRequired);
                    
                    if (guest.getBalance() >= totalRequired) {
                        bRoom.setAvailable(false);
                        newReservation.setStatus(ReservationStatus.CONFIRMED); 
                        currentReservations.add(newReservation);
                        System.out.println("SUCCESS: Room " + bNum + " reserved until " + checkOut + "!");
                        HotelDatabase.logAction(guest.getUsername() + " reserved room " + bNum);
                    } else {
                        System.out.println("BOOKING DENIED: You do not have enough funds.");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Booking Error: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Error: Invalid date format. Please use YYYY-MM-DD.");
                }
                break;

            case "3": 
                List<Reservations> myRooms = HotelDatabase.guestReservations.get(guest.getUsername());
                System.out.println("\n--- YOUR BOOKINGS ---");
                if (myRooms.isEmpty()) System.out.println("No active bookings.");
                else {
                    for (Reservations res : myRooms) {
                        System.out.println("Room " + res.getRoom().getRoomNumber() + " (" + res.getRoom().getRoomType().getTypeName() + ") | " + 
                                           "Check-out: " + res.getCheckOutDate() + " | Total: $" + res.calculateTotalPrice() + " | Status: " + res.getStatus());
                        
                        if (res.getRoom().getAmenities().isEmpty()) {
                            System.out.println("   -> Amenities: None\n");
                        } else {
                            System.out.println("   -> Amenities Included: " + res.getRoom().getAmenities() + "\n");
                        }
                    }
                }
                break;

            case "4": 
                handleAutomatedCheckout(guest, guest, scanner);
                break;

            case "5": return false;
        }
        return true;
    }

    // ==========================================
    // AUTOMATED CHECKOUT
    // ==========================================
    private static void handleAutomatedCheckout(Guest targetGuest, User performingUser, Scanner scanner) {
        // FIXED: Now uses a List of Reservations
        List<Reservations> myRooms = HotelDatabase.guestReservations.get(targetGuest.getUsername());
        
        if (myRooms == null || myRooms.isEmpty()) {
            System.out.println("No active reservations to pay for.");
            return;
        }

        String invoiceId = "INV-" + System.currentTimeMillis() % 100000;
        double discountAmount = 0.0;
        
        if (performingUser instanceof Staff) {
            try {
                System.out.print("STAFF OVERRIDE: Enter discount amount to apply (Enter 0 for none): $");
                discountAmount = Double.parseDouble(scanner.nextLine().trim());
                if (discountAmount < 0) {
                    System.out.println("Invalid discount. Setting to $0.0");
                    discountAmount = 0.0;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. No discount applied.");
            }
        }
        
        Invoice currentInvoice = new Invoice(invoiceId, targetGuest, myRooms, discountAmount);
        
        currentInvoice.printInvoice();
        double finalAmountDue = currentInvoice.calculateFinalAmount();
        
        System.out.println("Select Payment Method:");
        System.out.println("1. Cash\n2. Credit Card\n3. Online Payment");
        System.out.print("Choice: ");
        
        PaymentMethod method = PaymentMethod.CREDIT_CARD;
        String pChoice = scanner.nextLine().trim();
        if (pChoice.equals("1")) method = PaymentMethod.CASH;
        else if (pChoice.equals("2")) method = PaymentMethod.CREDIT_CARD;
        else if (pChoice.equals("3")) method = PaymentMethod.ONLINE;

        try {
            targetGuest.processPayment(finalAmountDue, method);
            
            for (Reservations res : myRooms) {
                res.getRoom().setAvailable(true); 
                res.setStatus(ReservationStatus.COMPLETED); // Changing the status!
            }
            myRooms.clear();
            
            HotelDatabase.logAction(performingUser.getUsername() + " processed checkout for " + targetGuest.getUsername() + ". Invoice " + invoiceId + " paid.");
            System.out.println("CHECKOUT SUCCESSFUL! Thank you for staying with us.");
            
        } catch (InvalidPaymentException e) {
            System.out.println("TRANSACTION FAILED: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("System Error during payment.");
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private static void handleRegistration(Scanner scanner) {
        System.out.println("\n--- REGISTRATION ---");
        System.out.print("Username: ");
        String u = scanner.nextLine().trim();
        System.out.print("Password: ");
        String p = scanner.nextLine().trim();
        
        try {
            System.out.print("Birth Year (YYYY): ");
            int year = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Initial Deposit: ");
            double b = Double.parseDouble(scanner.nextLine().trim());
            
            if (HotelDatabase.isUsernameTaken(u)) {
                throw new DuplicateUsernameException("The username '" + u + "' is already taken.");
            }

            Guest g = new Guest(u, p, LocalDate.of(year, 1, 1), b, "Unknown", Gender.MALE, "None");
            HotelDatabase.guests.add(g);
            HotelDatabase.logAction("Registered guest: " + u);
            System.out.println("Account created successfully.");
            
        } catch (DuplicateUsernameException | UnderageGuestException | WeakPasswordException | InvalidUsernameException e) {
            System.out.println("Registration Failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Registration Failed: Please enter valid numbers for Year and Deposit.");
        }
    }
}