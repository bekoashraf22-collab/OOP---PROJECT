
import main_classes.*;
import enums.*;
import exceptions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    // System State Data
    static List<RoomType> roomTypes = new ArrayList<>();
    static List<Room> rooms = new ArrayList<>();
    static List<Amenity> globalAmenities = new ArrayList<>();
    static Map<String, List<Room>> guestReservations = new HashMap<>();
    static List<String> systemLogs = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        initializeHotelData();

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

                    User currentUser = AuthService.login(username, password);
                    
                    if (currentUser != null) {
                        logAction(currentUser.getUsername() + " logged in.");
                        boolean loggedIn = true;
                        
                        // Ensure the guest has a reservation list ready
                        if (currentUser instanceof Guest && !guestReservations.containsKey(currentUser.getUsername())) {
                            guestReservations.put(currentUser.getUsername(), new ArrayList<>());
                        }

                        while (loggedIn) {
                            currentUser.displayMenu();
                            System.out.print("Select an action: ");
                            String userChoice = scanner.nextLine();
                            
                            if (currentUser instanceof Admin) {
                                loggedIn = handleAdminActions((Admin) currentUser, userChoice, scanner);
                            } else if (currentUser instanceof Receptionist) {
                                loggedIn = handleReceptionistActions((Receptionist) currentUser, userChoice, scanner);
                            } else if (currentUser instanceof Guest) {
                                loggedIn = handleGuestActions((Guest) currentUser, userChoice, scanner);
                            }
                        }
                    }
                    break;

                case "2":
                    handleRegistration(scanner);
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

    // ==========================================
    // ACTION HANDLERS
    // ==========================================

    private static boolean handleGuestActions(Guest guest, String choice, Scanner scanner) {
        switch (choice) {
            case "1": // View Available Rooms
                System.out.println("\n--- AVAILABLE ROOMS ---");
                boolean found = false;
                for (Room r : rooms) {
                    if (r.isAvailable()) {
                        System.out.println("Room " + r.getRoomNumber() + " | Type: " + r.getRoomType().getTypeName() + " | Price: $" + r.getRoomType().getBasePrice());
                        found = true;
                    }
                }
                if (!found) System.out.println("No rooms currently available.");
                break;

            case "2": // Make Reservation
                System.out.print("Enter the Room Number you want to book: ");
                String roomNum = scanner.nextLine();
                Room roomToBook = findRoom(roomNum);
                
                if (roomToBook != null && roomToBook.isAvailable()) {
                    roomToBook.setAvailable(false);
                    guestReservations.get(guest.getUsername()).add(roomToBook);
                    logAction("Guest " + guest.getUsername() + " booked Room " + roomNum);
                    System.out.println("Successfully booked Room " + roomNum + "!");
                } else {
                    System.out.println("Room not found or already booked.");
                }
                break;

            case "3": // View My Reservations
                System.out.println("\n--- MY RESERVATIONS ---");
                List<Room> myRooms = guestReservations.get(guest.getUsername());
                if (myRooms == null || myRooms.isEmpty()) {
                    System.out.println("You have no current reservations.");
                } else {
                    for (Room r : myRooms) {
                        System.out.println("Room " + r.getRoomNumber() + " (" + r.getRoomType().getTypeName() + ")");
                    }
                }
                break;

            case "4": // Checkout & Pay
                List<Room> checkoutRooms = guestReservations.get(guest.getUsername());
                if (checkoutRooms == null || checkoutRooms.isEmpty()) {
                    System.out.println("You have no rooms to check out of.");
                    break;
                }

                double totalBill = 0;
                for (Room r : checkoutRooms) {
                    totalBill += r.getRoomType().getBasePrice();
                    for (Amenity a : r.getAmenities()) {
                        totalBill += a.getExtraCost();
                    }
                }

                System.out.println("Your total bill is: $" + totalBill);
                try {
                    System.out.print("Enter amount to pay: $");
                    double amount = Double.parseDouble(scanner.nextLine());
                    guest.processPayment(amount, PaymentMethod.CREDIT_CARD);
                    
                    // Free the rooms
                    for (Room r : checkoutRooms) r.setAvailable(true);
                    checkoutRooms.clear();
                    logAction("Guest " + guest.getUsername() + " checked out and paid $" + amount);
                    
                } catch (InvalidPaymentException e) {
                    System.out.println(e.getMessage());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Payment failed.");
                }
                break;

            case "5": // Logout
                System.out.println("Logging out...");
                return false;

            default:
                System.out.println("Invalid option.");
        }
        return true; // Keep logged in
    }

    private static boolean handleAdminActions(Admin admin, String choice, Scanner scanner) {
        switch (choice) {
            case "1": // Add/Remove Rooms
                System.out.print("Type 'add' to add a room or 'remove' to remove: ");
                String action = scanner.nextLine();
                if (action.equalsIgnoreCase("add")) {
                    System.out.print("Enter new Room Number: ");
                    String rNum = scanner.nextLine();
                    rooms.add(new Room(rNum, roomTypes.get(0))); // Defaults to first room type
                    System.out.println("Room " + rNum + " added.");
                    logAction("Admin added room " + rNum);
                } else if (action.equalsIgnoreCase("remove")) {
                    System.out.print("Enter Room Number to remove: ");
                    String rNum = scanner.nextLine();
                    Room toRemove = findRoom(rNum);
                    if (toRemove != null) {
                        rooms.remove(toRemove);
                        System.out.println("Room removed.");
                        logAction("Admin removed room " + rNum);
                    } else {
                        System.out.println("Room not found.");
                    }
                }
                break;

            case "2": // Edit Amenities
                System.out.print("Enter Room Number to add WiFi ($15.0) to: ");
                String rNum = scanner.nextLine();
                Room r = findRoom(rNum);
                if (r != null) {
                    r.addAmenity(globalAmenities.get(0));
                    System.out.println("WiFi added to Room " + rNum);
                } else {
                    System.out.println("Room not found.");
                }
                break;

            case "3": // View Logs
                System.out.println("\n--- SYSTEM LOGS ---");
                for (String log : systemLogs) System.out.println(log);
                break;

            case "4": // Logout
                System.out.println("Logging out...");
                return false;
                
            default:
                System.out.println("Invalid option.");
        }
        return true;
    }

    private static boolean handleReceptionistActions(Receptionist rec, String choice, Scanner scanner) {
        switch (choice) {
            case "1": // Check-in Guest
                System.out.print("Enter Guest Username to check-in: ");
                String checkInName = scanner.nextLine();
                System.out.print("Enter Room Number to assign: ");
                String rNum = scanner.nextLine();
                
                Room r = findRoom(rNum);
                if (r != null && r.isAvailable()) {
                    r.setAvailable(false);
                    guestReservations.putIfAbsent(checkInName, new ArrayList<>());
                    guestReservations.get(checkInName).add(r);
                    System.out.println("Successfully checked " + checkInName + " into Room " + rNum);
                    logAction("Receptionist checked " + checkInName + " into Room " + rNum);
                } else {
                    System.out.println("Room unavailable or does not exist.");
                }
                break;

            case "2": // Check-out Guest
                System.out.print("Enter Guest Username to check-out: ");
                String checkOutName = scanner.nextLine();
                List<Room> gRooms = guestReservations.get(checkOutName);
                
                if (gRooms != null && !gRooms.isEmpty()) {
                    for (Room room : gRooms) room.setAvailable(true);
                    gRooms.clear();
                    System.out.println("Guest checked out successfully.");
                    logAction("Receptionist checked out " + checkOutName);
                } else {
                    System.out.println("No active reservations found for this guest.");
                }
                break;

            case "3": // Manage Keys (View Occupied)
                System.out.println("\n--- OCCUPIED ROOMS ---");
                for (Room room : rooms) {
                    if (!room.isAvailable()) {
                        System.out.println("Room " + room.getRoomNumber() + " is occupied.");
                    }
                }
                break;

            case "4": // Logout
                System.out.println("Logging out...");
                return false;

            default:
                System.out.println("Invalid option.");
        }
        return true;
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private static void handleRegistration(Scanner scanner) {
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

        AuthService.registerGuest(newRegUser, newRegPass, LocalDate.of(2000, 1, 1), balance, address, gender, pref);
        logAction("New user registered: " + newRegUser);
    }

    private static void initializeHotelData() {
        // Setup Room Types
        RoomType single = new RoomType("RT1", "Single", 100.0, 1);
        RoomType doubleRoom = new RoomType("RT2", "Double", 150.0, 2);
        RoomType suite = new RoomType("RT3", "Suite", 300.0, 4);
        
        roomTypes.add(single);
        roomTypes.add(doubleRoom);
        roomTypes.add(suite);

        // Setup Amenities
        globalAmenities.add(new Amenity("A1", "High-Speed WiFi", 15.0));
        globalAmenities.add(new Amenity("A2", "Mini-Bar Access", 50.0));

        // Setup Rooms
        rooms.add(new Room("101", single));
        rooms.add(new Room("102", doubleRoom));
        rooms.add(new Room("201", suite));
        rooms.add(new Room("202", doubleRoom));

        logAction("System initialized with default rooms and amenities.");
    }

    private static Room findRoom(String roomNumber) {
        for (Room r : rooms) {
            if (r.getRoomNumber().equals(roomNumber)) {
                return r;
            }
        }
        return null;
    }

    private static void logAction(String message) {
        systemLogs.add(java.time.LocalTime.now() + " - " + message);
    }
}