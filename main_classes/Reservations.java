package main_classes;

import enums.ReservationStatus;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservations {
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private ReservationStatus status;

    public Reservations(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        // Task: Handle date validations
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }

        this.guest = guest;
        this.room = room;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.status = ReservationStatus.PENDING; 
    }

    // method to calculate number of nights
    public long calculateTotalNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    // FIXED: Now calculates price including our stacked amenities!
    public double calculateTotalPrice() {
        double dailyRate = room.getRoomType().getBasePrice();
        for (Amenity a : room.getAmenities()) {
            dailyRate += a.getExtraCost();
        }
        return calculateTotalNights() * dailyRate;
    }

    // Getters
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public ReservationStatus getStatus() { return status; }

    // Setters
    public void setStatus(ReservationStatus status) { this.status = status; }
}