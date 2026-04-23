package main_classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalTime;
import enums.Gender;

public class HotelDatabase {
    // 1. ACTORS
    public static ArrayList<Guest> guests = new ArrayList<>();
    public static ArrayList<Staff> staffMembers = new ArrayList<>();

    // 2. PHYSICAL HOTEL DATA (Moved from Main)
    public static List<RoomType> roomTypes = new ArrayList<>();
    public static List<Room> rooms = new ArrayList<>();
    public static List<Amenity> globalAmenities = new ArrayList<>();
    
    // 3. OPERATIONAL DATA (Moved from Main)
    public static Map<String, List<Booking>> guestReservations = new HashMap<>();
    public static List<String> systemLogs = new ArrayList<>();

    // The static block runs once the moment the program starts
    static {
        try {
            // Pre-populate Users
            guests.add(new Guest("Abdullah", "pass1234", LocalDate.of(2005, 1, 1), 500.0, "Cairo", Gender.MALE, "High floor"));
            staffMembers.add(new Admin("Admin_User", "adminPass789", LocalDate.of(1990, 5, 20), 40));
            staffMembers.add(new Receptionist("Staff_A", "staffPass123", LocalDate.of(1998, 3, 10), 35));

            // Pre-populate Hotel Physical Data
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
            rooms.add(new Room("301", roomTypes.get(2)));
            rooms.add(new Room("401", roomTypes.get(3)));
            rooms.add(new Room("501", roomTypes.get(4)));

            logAction("System Initialized and Dummy Data Loaded.");
        } catch (Exception e) {
            System.out.println("System Error: Failed to pre-populate dummy data.");
        }
    }

    // --- DATABASE HELPER METHODS (Moved from Main) ---
    
    // Logs an action with a timestamp
    public static void logAction(String action) {
        systemLogs.add(LocalTime.now().withNano(0) + " -> " + action);
    }

    // Searches the database for a specific room number
    public static Room findRoom(String roomNumber) {
        for (Room r : rooms) {
            if (r.getRoomNumber().equals(roomNumber)) return r;
        }
        return null;
    }
}