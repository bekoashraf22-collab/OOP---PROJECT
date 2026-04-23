import main_classes.*;
import enums.*;
import exceptions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    
    // NEW: We created a small 'Wrapper Class' to hold both the Room and the Number of Nights
    static class Booking {
        Room room;
        int nights;
        
        public Booking(Room room, int nights) {
            this.room = room;
            this.nights = nights;
        }
    }

    // System Database
    static List<RoomType> roomTypes = new ArrayList<>();
    static List<Room> rooms = new ArrayList<>();
    static List<Amenity> globalAmenities = new ArrayList<>();
    
    // NEW: The map now stores a List of 'Booking' objects instead of just 'Room' objects
    static Map<String, List<Booking>> guestReservations = new HashMap<>();
    
    static List<String> systemLogs = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        initializeHotelData(); 

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
            logAction(currentUser.getUsername() + " logged in.");
            boolean loggedIn = true;
            
            if (currentUser instanceof Guest) {
                guestReservations.putIfAbsent(currentUser.getUsername(), new ArrayList<>());
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
            case "1": // Check-in Guest
                System.out.print("Enter Guest Username to check-in: ");
                String guestName = scanner.nextLine().trim();
                System.out.print("Enter Room Number to assign: ");
                String rNum = scanner.nextLine().trim();
                
                Room r = findRoom(rNum);
                if (r != null && r.isAvailable()) {
                    try {
                        // NEW: Receptionist also inputs number of nights during check-in
                        System.out.print("Enter number of nights for this stay: ");
                        int nights = Integer.parseInt(scanner.nextLine().trim());
                        
                        r.setAvailable(false);
                        guestReservations.putIfAbsent(guestName, new ArrayList<>());
                        
                        // NEW: Save the room and nights as a Booking
                        guestReservations.get(guestName).add(new Booking(r, nights));
                        
                        System.out.println("Success: Checked " + guestName + " into Room " + rNum + " for " + nights + " nights.");
                        logAction("Receptionist checked " + guestName + " into Room " + rNum);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Please enter a valid number for nights.");
                    }
                } else {
                    System.out.println("Error: Room is unavailable or does not exist.");
                }
                break;

            case "2": // Check-out Guest
                System.out.print("Enter Guest Username to check-out: ");
                String checkoutName = scanner.nextLine().trim();
                List<Booking> gRooms = guestReservations.get(checkoutName); // NEW: List of Bookings
                
                if (gRooms != null && !gRooms.isEmpty()) {
                    for (Booking booking : gRooms) {
                        booking.room.setAvailable(true); // NEW: Access the room inside the booking
                    }
                    gRooms.clear();
                    System.out.println("Success: Guest " + checkoutName + " has been checked out of all rooms.");
                    logAction("Receptionist checked out " + checkoutName);
                } else {
                    System.out.println("Error: No active reservations found for this guest.");
                }
                break;

            case "3": // Manage Keys
                System.out.println("\n--- OCCUPIED ROOMS (KEY MANAGEMENT) ---");
                boolean anyOccupied = false;
                for (Room room : rooms) {
                    if (!room.isAvailable()) {
                        System.out.println("Room " + room.getRoomNumber() + " [" + room.getRoomType().getTypeName() + "] - OCCUPIED");
                        anyOccupied = true;
                    }
                }
                if (!anyOccupied) System.out.println("All rooms are currently vacant.");
                break;

            case "4": // Logout
                return false;
        }
        return true;
    }

    // ==========================================
    // ADMIN ACTIONS (Remains mostly the same)
    // ==========================================
    private static boolean handleAdminActions(Admin admin, String choice, Scanner scanner) {
        switch (choice) {
            case "1": 
                System.out.print("New Room Number: ");
                String rNum = scanner.nextLine().trim();
                System.out.println("Select Type:");
                for (int i = 0; i < roomTypes.size(); i++) {
                    System.out.println((i+1) + ". " + roomTypes.get(i).getTypeName() + " ($" + roomTypes.get(i).getBasePrice() + "/night)");
                }
                try {
                    int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    rooms.add(new Room(rNum, roomTypes.get(idx)));
                    System.out.println("Success: Room " + rNum + " created.");
                    logAction("Admin added room " + rNum);
                } catch (Exception e) { System.out.println("Error: Invalid selection."); }
                break;

            case "2": 
                System.out.print("Room Number to Modify: ");
                String rid = scanner.nextLine().trim();
                Room room = findRoom(rid);
                
                if (room != null) {
                    System.out.println("Select Amenity to Add:");
                    for (int i = 0; i < globalAmenities.size(); i++) {
                        System.out.println((i+1) + ". " + globalAmenities.get(i).getName() + " ($" + globalAmenities.get(i).getExtraCost() + "/night)");
                    }
                    try {
                        int aidx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        room.addAmenity(globalAmenities.get(aidx));
                        System.out.println("Success: Amenity added to Room " + rid);
                    } catch (Exception e) { System.out.println("Error: Invalid selection."); }
                } else { System.out.println("Error: Room not found."); }
                break;

            case "3": 
                System.out.println("\n--- SYSTEM LOGS ---");
                if (systemLogs.isEmpty()) System.out.println("No logs available.");
                else for (String log : systemLogs) System.out.println(log);
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
            case "1": // View Available
                System.out.println("\n--- AVAILABLE ROOMS ---");
                boolean found = false;
                for (Room r : rooms) {
                    if (r.isAvailable()) {
                        // NEW: Explicitly stating this is the DAILY rate
                        System.out.println("Room " + r.getRoomNumber() + " [" + r.getRoomType().getTypeName() + 
                                           "] | Daily Rate: $" + calculateDailyRate(r) + " | Amenities: " + r.getAmenities().size());
                        found = true;
                    }
                }
                if (!found) System.out.println("No rooms currently available.");
                break;

            case "2": // Reservation 
                System.out.print("Enter Room Number to Book: ");
                String bNum = scanner.nextLine().trim();
                Room bRoom = findRoom(bNum);
                
                if (bRoom == null || !bRoom.isAvailable()) {
                    System.out.println("Error: Room is unavailable or does not exist.");
                    break;
                }

                try {
                    // NEW: Ask for the number of nights
                    System.out.print("How many nights will you be staying? ");
                    int nights = Integer.parseInt(scanner.nextLine().trim());
                    
                    if (nights <= 0) {
                        System.out.println("Error: You must stay at least 1 night.");
                        break;
                    }

                    // NEW: Calculate debt using the Booking objects
                    List<Booking> currentReservations = guestReservations.get(guest.getUsername());
                    double pendingDebt = 0;
                    for (Booking b : currentReservations) {
                        pendingDebt += (calculateDailyRate(b.room) * b.nights);
                    }
                    
                    // NEW: Calculate the cost for THIS specific booking
                    double dailyRate = calculateDailyRate(bRoom);
                    double newBookingCost = dailyRate * nights;
                    double totalRequired = pendingDebt + newBookingCost;

                    System.out.println("\n--- FINANCIAL CHECK ---");
                    System.out.println("Your Current Balance: $" + guest.getBalance());
                    System.out.println("Cost per night: $" + dailyRate + " x " + nights + " nights = $" + newBookingCost);
                    System.out.println("Total Amount Required (including past bookings): $" + totalRequired);
                    
                    if (guest.getBalance() >= totalRequired) {
                        bRoom.setAvailable(false);
                        // NEW: Save the new Booking object
                        currentReservations.add(new Booking(bRoom, nights));
                        System.out.println("SUCCESS: Room " + bNum + " reserved for " + nights + " nights!");
                        logAction(guest.getUsername() + " reserved room " + bNum + " for " + nights + " nights");
                    } else {
                        System.out.println("BOOKING DENIED: You do not have enough funds.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid number of nights entered.");
                }
                break;

            case "3": // View My Bookings
                List<Booking> myRooms = guestReservations.get(guest.getUsername());
                System.out.println("\n--- YOUR BOOKINGS ---");
                if (myRooms.isEmpty()) System.out.println("No active bookings.");
                else {
                    for (Booking b : myRooms) {
                        // NEW: Show the daily rate, nights, and the total for that room
                        double daily = calculateDailyRate(b.room);
                        double totalForRoom = daily * b.nights;
                        System.out.println("Room " + b.room.getRoomNumber() + " (" + b.room.getRoomType().getTypeName() + ") | " + 
                                           "Nights: " + b.nights + " | Total: $" + totalForRoom);
                    }
                }
                break;

            case "4": // Automated Checkout
                handleAutomatedCheckout(guest, scanner);
                break;

            case "5": return false;
        }
        return true;
    }

    // ==========================================
    // AUTOMATED CHECKOUT
    // ==========================================
    private static void handleAutomatedCheckout(Guest guest, Scanner scanner) {
        List<Booking> myRooms = guestReservations.get(guest.getUsername());
        if (myRooms == null || myRooms.isEmpty()) {
            System.out.println("You have no active reservations to pay for.");
            return;
        }

        // NEW: Calculate Grand Total by multiplying each room's daily rate by its nights
        double grandTotal = 0;
        for (Booking b : myRooms) {
            grandTotal += (calculateDailyRate(b.room) * b.nights);
        }

        System.out.println("\n--- AUTOMATED CHECKOUT ---");
        System.out.println("Total Outstanding Balance: $" + grandTotal);
        
        System.out.println("Select Payment Method:");
        System.out.println("1. Cash\n2. Credit Card\n3. Online Payment");
        System.out.print("Choice: ");
        
        PaymentMethod method = PaymentMethod.CREDIT_CARD;
        String pChoice = scanner.nextLine().trim();
        if (pChoice.equals("1")) method = PaymentMethod.CASH;
        else if (pChoice.equals("2")) method = PaymentMethod.CREDIT_CARD;
        else if (pChoice.equals("3")) method = PaymentMethod.ONLINE;

        try {
            guest.processPayment(grandTotal, method);
            
            // Release the rooms back to the hotel
            for (Booking b : myRooms) {
                b.room.setAvailable(true); 
            }
            myRooms.clear();
            
            logAction(guest.getUsername() + " checked out and paid $" + grandTotal);
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
    
    // NEW: Renamed this to clarify it calculates the cost for ONE night
    private static double calculateDailyRate(Room r) {
        double dailyTotal = r.getRoomType().getBasePrice();
        for (Amenity a : r.getAmenities()) {
            dailyTotal += a.getExtraCost();
        }
        return dailyTotal;
    }

    private static void handleRegistration(Scanner scanner) {
        System.out.println("\n--- REGISTRATION ---");
        System.out.print("Username: ");
        String u = scanner.nextLine().trim();
        System.out.print("Password: ");
        String p = scanner.nextLine().trim();
        try {
            System.out.print("Initial Deposit: ");
            double b = Double.parseDouble(scanner.nextLine().trim());
            Guest g = new Guest(u, p, LocalDate.of(2000, 1, 1), b, "Unknown", Gender.MALE, "None");
            HotelDatabase.guests.add(g);
            logAction("Registered guest: " + u);
            System.out.println("Account created successfully.");
        } catch (WeakPasswordException | InvalidUsernameException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error creating account. Please check your inputs.");
        }
    }

    private static void initializeHotelData() {
        roomTypes.add(new RoomType("RT1", "Economy", 75.0, 1));
        roomTypes.add(new RoomType("RT2", "Single", 100.0, 2));
        roomTypes.add(new RoomType("RT3", "Double", 150.0, 3));
        roomTypes.add(new RoomType("RT4", "Deluxe", 250.0, 4));
        roomTypes.add(new RoomType("RT5", "Royal Suite", 500.0, 5));

        globalAmenities.add(new Amenity("A1", "Fast WiFi", 15.0));
        globalAmenities.add(new Amenity("A2", "Hot Tub", 100.0));
        globalAmenities.add(new Amenity("A3", "Mini-bar", 25.0));
        globalAmenities.add(new Amenity("A4", "Ocean View", 50.0));
        globalAmenities.add(new Amenity("A5", "Breakfast In Room", 20.0));
        globalAmenities.add(new Amenity("A7", "Late Checkout", 30.0));
        
        rooms.add(new Room("101", roomTypes.get(0))); 
        rooms.add(new Room("201", roomTypes.get(1))); 
        logAction("Hotel initialized.");
    }

    private static Room findRoom(String n) {
        for (Room r : rooms) if (r.getRoomNumber().equals(n)) return r;
        return null;
    }

    private static void logAction(String m) {
        systemLogs.add(LocalTime.now().withNano(0) + " -> " + m);
    }
}