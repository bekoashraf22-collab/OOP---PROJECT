package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AsyncService;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import main_classes.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class CheckInController {
    @FXML private ComboBox<Guest> guestBox;
    @FXML private ComboBox<Room> roomBox;
    @FXML private TextField guestCountField;
    @FXML private DatePicker checkoutPicker;
    @FXML private Button checkInButton;
    @FXML private Label statusLabel;
    @FXML private Label threadLabel;

    @FXML private void initialize() {
        guestBox.setItems(FXCollections.observableArrayList(HotelDatabase.guests));
        roomBox.setItems(FXCollections.observableArrayList(HotelGuiService.availableRooms()));
        checkoutPicker.setValue(LocalDate.now().plusDays(1));
        guestCountField.setText("1");

        guestBox.setCellFactory(v -> guestCell());
        guestBox.setButtonCell(guestCell());
        roomBox.setCellFactory(v -> roomCell());
        roomBox.setButtonCell(roomCell());

        guestBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        roomBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        checkoutPicker.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        guestCountField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview());

        if (!guestBox.getItems().isEmpty()) guestBox.getSelectionModel().selectFirst();
        if (!roomBox.getItems().isEmpty()) roomBox.getSelectionModel().selectFirst();
        updatePreview();
    }

    private ListCell<Guest> guestCell() {
        return new ListCell<Guest>() {
            @Override protected void updateItem(Guest g, boolean empty) {
                super.updateItem(g, empty);
                setText(empty || g == null ? null : "👤 " + g.getUsername() + " | balance $" + String.format("%.2f", g.getBalance()));
            }
        };
    }

    private ListCell<Room> roomCell() {
        return new ListCell<Room>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : "🛏 Room " + r.getRoomNumber() + " | " + r.getRoomType().getTypeName() + " | cap " + r.getCapacity());
            }
        };
    }

    private void updatePreview() {
        Guest guest = guestBox.getValue();
        Room room = roomBox.getValue();
        LocalDate checkout = checkoutPicker.getValue();
        if (guest == null || room == null || checkout == null) {
            statusLabel.setText("Select a guest, available room, guest count, and check-out date.");
            return;
        }
        try {
            int guests = parseGuestCount();
            Reservations preview = new Reservations(guest, room, LocalDate.now(), checkout, guests);
            statusLabel.setText(
                    "Guest: " + guest.getUsername() +
                    "\nRoom: " + room.getRoomNumber() + " - " + room.getRoomType().getTypeName() +
                    "\nGuests staying: " + guests + " / " + room.getCapacity() + " capacity" +
                    "\nNights: " + preview.calculateTotalNights() +
                    "\nAmount to deduct: $" + String.format("%.2f", preview.calculateTotalPrice())
            );
        } catch (NumberFormatException e) {
            statusLabel.setText("Enter a valid whole number for guests staying.");
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }

    private int parseGuestCount() {
        return Integer.parseInt(guestCountField.getText().trim());
    }

    @FXML private void checkIn() {
        int guests;
        try {
            guests = parseGuestCount();
        } catch (NumberFormatException e) {
            GuiUtils.error("Invalid Guest Count", "Number of guests must be a whole number, for example 1, 2, or 3.");
            return;
        }

        checkInButton.setDisable(true);
        threadLabel.setText("Processing...");

        AsyncService.runAsync(() -> {
            try { return HotelGuiService.checkInGuest(guestBox.getValue(), roomBox.getValue(), checkoutPicker.getValue(), guests); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, reservation -> {
            checkInButton.setDisable(false);
            threadLabel.setText("Done.");
            GuiUtils.info("Check-in Complete", reservation.getGuest().getUsername() + " checked into room " + reservation.getRoom().getRoomNumber() + " with " + reservation.getGuestCount() + " guest(s). Payment deducted: $" + String.format("%.2f", reservation.getPaidAmount()));
            HotelApp.show("/GUI/FXML/ReceptionistDashboard.fxml");
        }, error -> {
            checkInButton.setDisable(false);
            threadLabel.setText("Failed.");
            GuiUtils.error("Check-in Failed", rootMessage(error));
        });
    }

    private String rootMessage(Throwable t) { Throwable x = t; while (x.getCause() != null) x = x.getCause(); return x.getMessage(); }

    @FXML private void back() { HotelApp.show("/GUI/FXML/ReceptionistDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
