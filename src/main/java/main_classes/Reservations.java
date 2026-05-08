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
    private int guestCount;
    private double paidAmount;
    private LocalDate requestedCheckOutDate;
    private boolean extensionRequested;
    private boolean keyIssued;

    public Reservations(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        this(guest, room, checkIn, checkOut, 1);
    }

    public Reservations(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut, int guestCount) {
        if (checkIn == null || checkOut == null) throw new IllegalArgumentException("Dates cannot be empty.");
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) throw new IllegalArgumentException("Check-out date must be after check-in date.");
        if (guestCount < 1) throw new IllegalArgumentException("Number of guests must be at least 1.");
        if (room != null && guestCount > room.getCapacity()) throw new IllegalArgumentException("This room capacity is " + room.getCapacity() + ". You cannot reserve it for " + guestCount + " guests.");

        this.guest = guest;
        this.room = room;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.status = ReservationStatus.PENDING;
        this.guestCount = guestCount;
        this.paidAmount = 0;
        this.extensionRequested = false;
        this.keyIssued = false;
    }

    public long calculateTotalNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public double calculateDailyRate() {
        double dailyRate = room.getRoomType().getBasePrice();
        for (Amenity a : room.getAmenities()) dailyRate += a.getExtraCost();
        return dailyRate;
    }

    public double calculateTotalPrice() {
        return calculateTotalNights() * calculateDailyRate();
    }

    public double calculateExtensionPrice(LocalDate newCheckOut, double extraRatePerNight) {
        if (newCheckOut == null || !newCheckOut.isAfter(checkOutDate)) throw new IllegalArgumentException("New check-out date must be after the current check-out date.");
        if (extraRatePerNight < 0) throw new IllegalArgumentException("Extra extension rate cannot be negative.");
        long extraNights = ChronoUnit.DAYS.between(checkOutDate, newCheckOut);
        return extraNights * (calculateDailyRate() + extraRatePerNight);
    }

    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.STAYING;
    }

    public boolean canAskForExtension(LocalDate today) {
        return isActive() && !today.isBefore(checkInDate) && today.isBefore(checkOutDate);
    }

    public void requestExtension(LocalDate newCheckOutDate) {
        if (!canAskForExtension(LocalDate.now())) throw new IllegalArgumentException("Extension can only be requested during the stay.");
        if (newCheckOutDate == null || !newCheckOutDate.isAfter(checkOutDate)) throw new IllegalArgumentException("New check-out date must be after the current check-out date.");
        this.requestedCheckOutDate = newCheckOutDate;
        this.extensionRequested = true;
    }

    public void clearExtensionRequest() {
        this.requestedCheckOutDate = null;
        this.extensionRequested = false;
    }

    public void addPaidAmount(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Paid amount cannot be negative.");
        paidAmount += amount;
    }

    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public ReservationStatus getStatus() { return status; }
    public int getGuestCount() { return guestCount; }
    public double getPaidAmount() { return paidAmount; }
    public LocalDate getRequestedCheckOutDate() { return requestedCheckOutDate; }
    public boolean isExtensionRequested() { return extensionRequested; }
    public boolean isKeyIssued() { return keyIssued; }

    public void issueKey() {
        if (!isActive()) throw new IllegalArgumentException("Keys can only be issued for active reservations.");
        if (keyIssued) throw new IllegalArgumentException("Room key is already issued.");
        keyIssued = true;
    }

    public void returnKey() {
        if (!keyIssued) throw new IllegalArgumentException("Room key is already marked as returned.");
        keyIssued = false;
    }

    public void setStatus(ReservationStatus status) { this.status = status; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
}
