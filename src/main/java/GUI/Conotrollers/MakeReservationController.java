package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AppSession;
import GUI.Services.AsyncService;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import main_classes.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class MakeReservationController {
    @FXML private ComboBox<Room> roomBox;
    @FXML private DatePicker checkoutPicker;
    @FXML private TextField guestCountField;
    @FXML private Label summaryLabel;
    @FXML private Button reserveButton;

    @FXML
    private void initialize() {
        roomBox.setCellFactory(v -> roomCell());
        roomBox.setButtonCell(roomCell());
        roomBox.setItems(FXCollections.observableArrayList(HotelGuiService.availableRooms()));
        checkoutPicker.setValue(LocalDate.now().plusDays(1));
        guestCountField.setText("1");
        roomBox.getSelectionModel().selectedItemProperty().addListener((obs, old, room) -> updateSummary());
        checkoutPicker.valueProperty().addListener((obs, old, date) -> updateSummary());
        guestCountField.textProperty().addListener((obs, old, val) -> updateSummary());
        if (!roomBox.getItems().isEmpty()) roomBox.getSelectionModel().selectFirst();
        updateSummary();
    }

    private ListCell<Room> roomCell() {
        return new ListCell<Room>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : "Room " + r.getRoomNumber() + " - " + r.getRoomType().getTypeName() + " - cap " + r.getCapacity() + " ($" + r.getRoomType().getBasePrice() + "/night)");
            }
        };
    }

    private void updateSummary() {
        Room room = roomBox.getValue();
        LocalDate out = checkoutPicker.getValue();
        if (room == null || out == null) { summaryLabel.setText("Choose a room and check-out date."); return; }
        try {
            int guests = Integer.parseInt(guestCountField.getText().trim());
            Reservations preview = new Reservations((Guest) AppSession.getCurrentUser(), room, LocalDate.now(), out, guests);
            summaryLabel.setText("Guests staying: " + guests + " / " + room.getCapacity() + " capacity" +
                    "\nTotal nights: " + preview.calculateTotalNights() +
                    "\nWill be deducted now: $" + String.format("%.2f", preview.calculateTotalPrice()) +
                    "\nAmenities: " + (room.getAmenities().isEmpty() ? "None" : room.getAmenities()));
        } catch (NumberFormatException e) { summaryLabel.setText("Enter a valid number of guests."); }
        catch (Exception e) { summaryLabel.setText(e.getMessage()); }
    }

    @FXML private void reserve() {
        Guest guest = (Guest) AppSession.getCurrentUser();
        Room room = roomBox.getValue();
        LocalDate out = checkoutPicker.getValue();
        int guestCount;
        try { guestCount = Integer.parseInt(guestCountField.getText().trim()); }
        catch (NumberFormatException e) { GuiUtils.error("Invalid Guests", "Number of guests must be a whole number."); return; }
        reserveButton.setDisable(true);
        AsyncService.runAsync(() -> {
            try { return HotelGuiService.reserveRoom(guest, room, out, guestCount); }
            catch (Exception e) { throw new RuntimeException(e); }
        }, reservation -> {
            reserveButton.setDisable(false);
            GuiUtils.info("Reservation Confirmed", "Room " + reservation.getRoom().getRoomNumber() + " reserved until " + reservation.getCheckOutDate() + ". Payment deducted: $" + String.format("%.2f", reservation.getPaidAmount()));
            HotelApp.show("/GUI/FXML/GuestDashboard.fxml");
        }, error -> {
            reserveButton.setDisable(false);
            GuiUtils.error("Reservation Failed", rootMessage(error));
        });
    }

    private String rootMessage(Throwable t) { Throwable x = t; while (x.getCause() != null) x = x.getCause(); return x.getMessage(); }

    @FXML private void back() { HotelApp.show("/GUI/FXML/GuestDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
