package main_classes;

public class Booking {
    private Room room;
    private int nights;

    public Booking(Room room, int nights) {
        this.room = room;
        this.nights = nights;
    }

    // Getters
    public Room getRoom() { return room; }
    public int getNights() { return nights; }
}