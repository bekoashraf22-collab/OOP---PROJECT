package GUI.Services;

import enums.Gender;
import enums.PaymentMethod;
import enums.ReservationStatus;
import exceptions.*;
import main_classes.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class HotelGuiService {
    private static final Object DB_LOCK = new Object();
    public static final String ADMIN_OVERRIDE_KEY = "ADMIN-2026";
    public static final String STAFF_OVERRIDE_KEY = "STAFF-2026";

    private HotelGuiService() {}

    public static User login(String username, String password) {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            User user = AuthService.login(username, password);
            if (user != null) {
                if (user instanceof Guest) HotelDatabase.guestReservations.putIfAbsent(user.getUsername(), new ArrayList<>());
                HotelDatabase.logAction(user.getUsername() + " logged in from JavaFX GUI.");
            }
            return user;
        }
    }

    public static Guest registerGuest(String username, String password, LocalDate dob, double balance,
                                      String address, Gender gender)
            throws DuplicateUsernameException, WeakPasswordException, InvalidUsernameException, UnderageGuestException {
        synchronized (DB_LOCK) {
            if (HotelDatabase.isUsernameTaken(username)) throw new DuplicateUsernameException("The username '" + username + "' is already taken.");
            Guest guest = new Guest(username, password, dob, balance, address, gender, "None");
            HotelDatabase.guests.add(guest);
            HotelDatabase.guestReservations.putIfAbsent(guest.getUsername(), new ArrayList<>());
            HotelDatabase.logAction("Registered new guest from GUI: " + username);
            return guest;
        }
    }

    public static Guest registerGuestWithCard(String username, String password, LocalDate dob, String address,
                                             Gender gender, String cardNumber, String expiry, String cvv, double amount)
            throws DuplicateUsernameException, WeakPasswordException, InvalidUsernameException, UnderageGuestException {
        synchronized (DB_LOCK) {
            if (HotelDatabase.isUsernameTaken(username)) throw new DuplicateUsernameException("The username '" + username + "' is already taken.");
            String cleanCard = validateCardDetails(cardNumber, expiry, cvv, amount);
            Guest guest = new Guest(username, password, dob, amount, address, gender, "None");
            HotelDatabase.guests.add(guest);
            HotelDatabase.guestReservations.putIfAbsent(guest.getUsername(), new ArrayList<>());
            HotelDatabase.logAction("Registered new guest from GUI: " + username + " with card ending " + cleanCard.substring(12) + ".");
            return guest;
        }
    }

    public static Staff registerStaffAccount(String username, String password, LocalDate dob, int workingHours,
                                             String accountType, String overrideKey)
            throws DuplicateUsernameException, WeakPasswordException, InvalidUsernameException, UnderageGuestException {
        synchronized (DB_LOCK) {
            if (HotelDatabase.isUsernameTaken(username)) throw new DuplicateUsernameException("The username '" + username + "' is already taken.");
            boolean wantsAdmin = "ADMIN".equalsIgnoreCase(accountType);
            boolean adminKey = ADMIN_OVERRIDE_KEY.equals(overrideKey);
            boolean staffKey = STAFF_OVERRIDE_KEY.equals(overrideKey);
            if (wantsAdmin && !adminKey) throw new IllegalArgumentException("Only the admin override key can create admin accounts.");
            if (!wantsAdmin && !adminKey && !staffKey) throw new IllegalArgumentException("Use the staff override key to create staff accounts, or the admin override key for staff/admin accounts.");

            Staff staff = wantsAdmin ? new Admin(username, password, dob, workingHours) : new Receptionist(username, password, dob, workingHours);
            HotelDatabase.staffMembers.add(staff);
            HotelDatabase.logAction("Created new " + (wantsAdmin ? "admin" : "receptionist") + " account from GUI: " + username);
            return staff;
        }
    }

    public static void processAutomaticCheckouts() {
        synchronized (DB_LOCK) {
            LocalDate today = LocalDate.now();
            for (List<Reservations> list : HotelDatabase.guestReservations.values()) {
                for (Reservations r : list) {
                    if (r.isActive() && !today.isBefore(r.getCheckOutDate())) {
                        r.setStatus(ReservationStatus.COMPLETED);
                        r.getRoom().setAvailable(true);
                        if (r.isKeyIssued()) r.returnKey();
                        r.clearExtensionRequest();
                        HotelDatabase.logAction("Automatic checkout completed for " + r.getGuest().getUsername() + " from room " + r.getRoom().getRoomNumber() + ".");
                    } else if (r.getStatus() == ReservationStatus.CONFIRMED && !today.isBefore(r.getCheckInDate()) && today.isBefore(r.getCheckOutDate())) {
                        r.setStatus(ReservationStatus.STAYING);
                    }
                }
            }
        }
    }

    public static List<Room> availableRooms() {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            List<Room> out = new ArrayList<>();
            for (Room room : HotelDatabase.rooms) if (room.isAvailable()) out.add(room);
            return out;
        }
    }

    public static List<Room> occupiedRooms() {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            List<Room> out = new ArrayList<>();
            for (Room room : HotelDatabase.rooms) if (!room.isAvailable()) out.add(room);
            return out;
        }
    }

    public static List<Reservations> activeReservations(Guest guest) {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            List<Reservations> out = new ArrayList<>();
            if (guest == null) return out;
            List<Reservations> list = HotelDatabase.guestReservations.getOrDefault(guest.getUsername(), new ArrayList<>());
            for (Reservations r : list) if (r.isActive()) out.add(r);
            return out;
        }
    }

    public static List<Reservations> allReservations(Guest guest) {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            if (guest == null) return new ArrayList<>();
            return new ArrayList<>(HotelDatabase.guestReservations.getOrDefault(guest.getUsername(), new ArrayList<>()));
        }
    }

    public static Reservations reserveRoom(Guest guest, Room room, LocalDate checkOut, int guestCount) throws InvalidPaymentException {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            if (guest == null) throw new IllegalArgumentException("No guest selected.");
            if (room == null) throw new IllegalArgumentException("No room selected.");
            if (!room.isAvailable()) throw new IllegalArgumentException("Room is not available.");
            HotelDatabase.guestReservations.putIfAbsent(guest.getUsername(), new ArrayList<>());

            Reservations reservation = new Reservations(guest, room, LocalDate.now(), checkOut, guestCount);
            double total = reservation.calculateTotalPrice();
            guest.processPayment(total, PaymentMethod.CREDIT_CARD);
            reservation.addPaidAmount(total);
            reservation.setStatus(ReservationStatus.STAYING);
            room.setAvailable(false);
            HotelDatabase.guestReservations.get(guest.getUsername()).add(reservation);
            HotelDatabase.logAction(guest.getUsername() + " reserved and prepaid room " + room.getRoomNumber() + " with " + guestCount + " guest(s). Amount: $" + String.format("%.2f", total));
            return reservation;
        }
    }

    public static Reservations reserveRoom(Guest guest, Room room, LocalDate checkOut) throws InvalidPaymentException {
        return reserveRoom(guest, room, checkOut, 1);
    }

    public static Reservations checkInGuest(Guest guest, Room room, LocalDate checkOut) throws InvalidPaymentException {
        return checkInGuest(guest, room, checkOut, 1);
    }

    public static Reservations checkInGuest(Guest guest, Room room, LocalDate checkOut, int guestCount) throws InvalidPaymentException {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            if (guest == null) throw new IllegalArgumentException("Guest not found.");
            if (room == null || !room.isAvailable()) throw new IllegalArgumentException("Room is unavailable or does not exist.");
            HotelDatabase.guestReservations.putIfAbsent(guest.getUsername(), new ArrayList<>());
            Reservations reservation = new Reservations(guest, room, LocalDate.now(), checkOut, guestCount);
            double total = reservation.calculateTotalPrice();
            guest.processPayment(total, PaymentMethod.CREDIT_CARD);
            reservation.addPaidAmount(total);
            reservation.setStatus(ReservationStatus.STAYING);
            room.setAvailable(false);
            HotelDatabase.guestReservations.get(guest.getUsername()).add(reservation);
            HotelDatabase.logAction("Receptionist checked " + guest.getUsername() + " into room " + room.getRoomNumber() + " with prepaid amount $" + String.format("%.2f", total) + ".");
            return reservation;
        }
    }

    public static void staffAddGuestBalance(Guest guest, double amount, String note, User actor) {
        synchronized (DB_LOCK) {
            if (guest == null) throw new IllegalArgumentException("Choose a guest first.");
            if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
            guest.addBalance(amount);
            String by = actor == null ? "Staff" : actor.getUsername();
            String cleanNote = note == null || note.trim().isEmpty() ? "front desk balance update" : note.trim();
            HotelDatabase.logAction(by + " added $" + String.format("%.2f", amount) + " to " + guest.getUsername() + " balance (" + cleanNote + ").");
        }
    }

    public static List<Reservations> activeHotelReservations() {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            List<Reservations> out = new ArrayList<>();
            for (List<Reservations> list : HotelDatabase.guestReservations.values()) {
                for (Reservations r : list) if (r.isActive()) out.add(r);
            }
            return out;
        }
    }

    public static void issueRoomKey(Reservations reservation, User actor) {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            if (reservation == null) throw new IllegalArgumentException("Choose an active reservation first.");
            reservation.issueKey();
            HotelDatabase.logAction((actor == null ? "Staff" : actor.getUsername()) + " issued room key for room " + reservation.getRoom().getRoomNumber() + " to " + reservation.getGuest().getUsername() + ".");
        }
    }

    public static void returnRoomKey(Reservations reservation, User actor) {
        synchronized (DB_LOCK) {
            if (reservation == null) throw new IllegalArgumentException("Choose an active reservation first.");
            reservation.returnKey();
            HotelDatabase.logAction((actor == null ? "Staff" : actor.getUsername()) + " marked room key returned for room " + reservation.getRoom().getRoomNumber() + " by " + reservation.getGuest().getUsername() + ".");
        }
    }

    public static void addGuestBalanceWithCard(Guest guest, String cardNumber, String expiry, String cvv, double amount) {
        synchronized (DB_LOCK) {
            if (guest == null) throw new IllegalArgumentException("No guest is logged in.");
            String cleanCard = validateCardDetails(cardNumber, expiry, cvv, amount);
            guest.addBalance(amount);
            HotelDatabase.logAction(guest.getUsername() + " added $" + String.format("%.2f", amount) + " to balance using card ending " + cleanCard.substring(12) + ".");
        }
    }

    private static String validateCardDetails(String cardNumber, String expiry, String cvv, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        String cleanCard = cardNumber == null ? "" : cardNumber.replace(" ", "").replace("-", "");
        if (!cleanCard.matches("\\d{16}")) throw new IllegalArgumentException("Card number must be 16 digits, for example 1234 5678 9012 3456.");
        if (cvv == null || !cvv.matches("\\d{3}")) throw new IllegalArgumentException("CVV must be exactly 3 digits.");
        if (expiry == null || !expiry.matches("\\d{2}/\\d{2}")) throw new IllegalArgumentException("Expiry must use MM/YY format.");
        try {
            YearMonth exp = YearMonth.parse(expiry, DateTimeFormatter.ofPattern("MM/yy"));
            if (exp.isBefore(YearMonth.now())) throw new IllegalArgumentException("Card expiry date has already passed.");
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Expiry must use MM/YY format.");
        }
        return cleanCard;
    }

    public static void requestExtension(Guest guest, Reservations reservation, LocalDate newCheckOut) {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            if (guest == null || reservation == null) throw new IllegalArgumentException("Choose a reservation first.");
            if (reservation.getGuest() != guest) throw new IllegalArgumentException("This reservation does not belong to the logged-in guest.");
            reservation.requestExtension(newCheckOut);
            HotelDatabase.logAction(guest.getUsername() + " requested extension for room " + reservation.getRoom().getRoomNumber() + " until " + newCheckOut + ".");
        }
    }

    public static List<Reservations> extensionRequests() {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            List<Reservations> out = new ArrayList<>();
            for (List<Reservations> list : HotelDatabase.guestReservations.values()) {
                for (Reservations r : list) if (r.isExtensionRequested() && r.isActive()) out.add(r);
            }
            return out;
        }
    }

    public static double acceptExtension(Reservations reservation, User actor, double extraRatePerNight) throws InvalidPaymentException {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            if (reservation == null) throw new IllegalArgumentException("Choose a request first.");
            if (!reservation.isExtensionRequested()) throw new IllegalArgumentException("This reservation has no extension request.");
            double cost = reservation.calculateExtensionPrice(reservation.getRequestedCheckOutDate(), extraRatePerNight);
            reservation.getGuest().processPayment(cost, PaymentMethod.CREDIT_CARD);
            reservation.addPaidAmount(cost);
            reservation.setCheckOutDate(reservation.getRequestedCheckOutDate());
            reservation.clearExtensionRequest();
            HotelDatabase.logAction((actor == null ? "Staff" : actor.getUsername()) + " accepted extension for " + reservation.getGuest().getUsername() + ". Extra charged: $" + String.format("%.2f", cost));
            return cost;
        }
    }

    public static void declineExtension(Reservations reservation, User actor) {
        synchronized (DB_LOCK) {
            if (reservation == null) throw new IllegalArgumentException("Choose a request first.");
            reservation.clearExtensionRequest();
            HotelDatabase.logAction((actor == null ? "Staff" : actor.getUsername()) + " declined extension for " + reservation.getGuest().getUsername() + ".");
        }
    }

    public static InvoiceResult staffEarlyCheckout(Reservations reservation, User actor, double refundAmount) {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            if (reservation == null) throw new IllegalArgumentException("Choose a reservation first.");
            if (!reservation.isActive()) throw new IllegalArgumentException("This reservation is not active.");
            if (refundAmount < 0) throw new IllegalArgumentException("Refund cannot be negative.");
            if (refundAmount > reservation.getPaidAmount()) throw new IllegalArgumentException("Refund cannot be greater than the amount paid for this reservation.");
            reservation.getGuest().addBalance(refundAmount);
            reservation.getRoom().setAvailable(true);
            if (reservation.isKeyIssued()) reservation.returnKey();
            reservation.setStatus(ReservationStatus.COMPLETED);
            reservation.clearExtensionRequest();
            String invoiceId = "EARLY-" + (System.currentTimeMillis() % 100000);
            List<Reservations> one = new ArrayList<>();
            one.add(reservation);
            HotelDatabase.logAction((actor == null ? "Staff" : actor.getUsername()) + " processed early checkout for " + reservation.getGuest().getUsername() + ". Refund: $" + String.format("%.2f", refundAmount));
            return new InvoiceResult(invoiceId, reservation.getGuest().getUsername(), one, refundAmount, refundAmount, PaymentMethod.CASH);
        }
    }

    public static InvoiceResult checkoutGuest(Guest guest, User actor, double discount, PaymentMethod method) throws InvalidPaymentException {
        synchronized (DB_LOCK) {
            throw new IllegalArgumentException("Guest checkout is automatic on the check-out date. Staff can process early checkout and refunds.");
        }
    }

    public static Room addRoom(String roomNumber, RoomType type, int capacity) throws DuplicateRoomException {
        synchronized (DB_LOCK) {
            if (roomNumber == null || roomNumber.trim().isEmpty()) throw new IllegalArgumentException("Room number cannot be empty.");
            if (type == null) throw new IllegalArgumentException("Choose a room type.");
            if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least 1.");
            if (HotelDatabase.findRoom(roomNumber.trim()) != null) throw new DuplicateRoomException("Room " + roomNumber + " already exists.");
            Room room = new Room(roomNumber.trim(), type, capacity);
            HotelDatabase.rooms.add(room);
            HotelDatabase.logAction("Admin added room " + roomNumber + " from GUI with capacity " + capacity + ".");
            return room;
        }
    }

    public static Room addRoom(String roomNumber, RoomType type) throws DuplicateRoomException {
        return addRoom(roomNumber, type, type == null ? 1 : type.getCapacity());
    }


    public static void updateRoom(Room room, String newRoomNumber, RoomType newType, int newCapacity) {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            if (room == null) throw new IllegalArgumentException("Choose a room first.");
            if (newRoomNumber == null || newRoomNumber.trim().isEmpty()) throw new IllegalArgumentException("Room number cannot be empty.");
            if (newType == null) throw new IllegalArgumentException("Choose a room type.");
            if (newCapacity < 1) throw new IllegalArgumentException("Capacity must be at least 1.");

            String cleanNumber = newRoomNumber.trim();
            Room existing = HotelDatabase.findRoom(cleanNumber);
            if (existing != null && existing != room) throw new IllegalArgumentException("Room " + cleanNumber + " already exists.");

            int highestActiveGuestCount = 0;
            for (List<Reservations> list : HotelDatabase.guestReservations.values()) {
                for (Reservations r : list) {
                    if (r.getRoom() == room && r.isActive()) {
                        highestActiveGuestCount = Math.max(highestActiveGuestCount, r.getGuestCount());
                    }
                }
            }
            if (highestActiveGuestCount > 0 && newCapacity < highestActiveGuestCount) {
                throw new IllegalArgumentException("This room currently has an active stay with " + highestActiveGuestCount + " guest(s). Capacity cannot be lower than that.");
            }

            String oldNumber = room.getRoomNumber();
            room.setRoomNumber(cleanNumber);
            room.setRoomType(newType);
            room.setCapacity(newCapacity);
            HotelDatabase.logAction("Admin edited room " + oldNumber + " from GUI. New details: room " + cleanNumber + ", type " + newType.getTypeName() + ", capacity " + newCapacity + ".");
        }
    }

    public static void removeRoom(Room room) {
        synchronized (DB_LOCK) {
            processAutomaticCheckouts();
            if (room == null) throw new IllegalArgumentException("Choose a room first.");
            if (!room.isAvailable()) throw new IllegalArgumentException("Cannot remove an occupied room.");
            HotelDatabase.rooms.remove(room);
            HotelDatabase.logAction("Admin removed room " + room.getRoomNumber() + " from GUI.");
        }
    }

    public static RoomType addRoomType(String id, String name, double price, int capacity) throws InvalidPricingException {
        synchronized (DB_LOCK) {
            RoomType type = new RoomType(id, name, price, capacity);
            HotelDatabase.roomTypes.add(type);
            HotelDatabase.logAction("Admin added room type " + name + " from GUI.");
            return type;
        }
    }

    public static Amenity addAmenity(String id, String name, double cost) throws InvalidPricingException {
        synchronized (DB_LOCK) {
            Amenity amenity = new Amenity(id, name, cost);
            HotelDatabase.globalAmenities.add(amenity);
            HotelDatabase.logAction("Admin added global amenity " + name + " from GUI.");
            return amenity;
        }
    }

    public static void addAmenityToRoom(Room room, Amenity amenity) {
        synchronized (DB_LOCK) {
            if (room == null || amenity == null) throw new IllegalArgumentException("Choose a room and amenity.");
            room.addAmenity(amenity);
            HotelDatabase.logAction("Admin added " + amenity.getName() + " to room " + room.getRoomNumber() + " from GUI.");
        }
    }

    public static void removeAmenityFromRoom(Room room, Amenity amenity) {
        synchronized (DB_LOCK) {
            if (room == null || amenity == null) throw new IllegalArgumentException("Choose a room and amenity.");
            room.removeAmenity(amenity);
            HotelDatabase.logAction("Admin removed " + amenity.getName() + " from room " + room.getRoomNumber() + " from GUI.");
        }
    }

    public static Guest findGuest(String username) {
        synchronized (DB_LOCK) {
            if (username == null) return null;
            for (Guest guest : HotelDatabase.guests) if (guest.getUsername().equalsIgnoreCase(username.trim())) return guest;
            return null;
        }
    }

    public static final class InvoiceResult {
        public final String invoiceId;
        public final String guestName;
        public final List<Reservations> reservations;
        public final double discount;
        public final double total;
        public final PaymentMethod method;

        public InvoiceResult(String invoiceId, String guestName, List<Reservations> reservations, double discount, double total, PaymentMethod method) {
            this.invoiceId = invoiceId;
            this.guestName = guestName;
            this.reservations = reservations;
            this.discount = discount;
            this.total = total;
            this.method = method;
        }
    }
}
