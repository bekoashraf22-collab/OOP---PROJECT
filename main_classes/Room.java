package main_classes;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomNumber;
    private RoomType roomType; // Associated with ONE RoomType
    private List<Amenity> amenities; // Associated with a LIST of Amenities
    private boolean isAvailable;

    public Room(String roomNumber, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.amenities = new ArrayList<>(); // Initialize the empty list
        this.isAvailable = true; // Rooms are available by default when created
    }

    // Getters and Setters
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public List<Amenity> getAmenities() { return amenities; }
    // Note: Usually, we don't set the whole list, we add/remove individual items.
    
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    // Behaviors to manage amenities inside the room
    public void addAmenity(Amenity amenity) {
        if (!this.amenities.contains(amenity)) {
            this.amenities.add(amenity);
        }
    }

    public void removeAmenity(Amenity amenity) {
        this.amenities.remove(amenity);
    }
}
