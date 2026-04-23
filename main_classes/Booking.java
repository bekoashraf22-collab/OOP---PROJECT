package main_classes;

import exceptions.InvalidDurationException;

public class Booking {
    private Room room;
    private int nights;

    public Booking(Room room, int nights) throws InvalidDurationException {
        if (nights <= 0) {
            throw new InvalidDurationException("A booking must be for at least 1 night.");
        }
        this.room = room;
        this.nights = nights;
    }

    public Room getRoom() { return room; }
    public int getNights() { return nights; }
}