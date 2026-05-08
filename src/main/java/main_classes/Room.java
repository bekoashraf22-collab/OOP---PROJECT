package main_classes;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomNumber;
    private RoomType roomType;
    private int capacity;
    private List<Amenity> amenities;
    private boolean isAvailable;

    public Room(String roomNumber, RoomType roomType) {
        this(roomNumber, roomType, roomType == null ? 1 : roomType.getCapacity());
    }

    public Room(String roomNumber, RoomType roomType, int capacity) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = Math.max(1, capacity);
        this.amenities = new ArrayList<>();
        this.isAvailable = true;
    }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = Math.max(1, capacity); }

    public List<Amenity> getAmenities() { return amenities; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public void addAmenity(Amenity amenity) {
        if (!this.amenities.contains(amenity)) {
            this.amenities.add(amenity);
        }
    }

    public void removeAmenity(Amenity amenity) {
        this.amenities.remove(amenity);
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " - " + roomType.getTypeName() + " - capacity " + capacity + " - " + (isAvailable ? "Available" : "Occupied");
    }
}
