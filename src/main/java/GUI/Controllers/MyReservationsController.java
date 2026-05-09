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
import java.util.List;

public class MyReservationsController {
    @FXML private ListView<Reservations> reservationsList;
    @FXML private Label detailLabel;
    @FXML private DatePicker extensionDatePicker;

    @FXML
    private void initialize() {
        extensionDatePicker.setValue(LocalDate.now().plusDays(1));
        refreshList();
        reservationsList.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> show(r));
    }

    private void refreshList() {
        Guest guest = (Guest) AppSession.getCurrentUser();
        List<Reservations> reservations = HotelGuiService.allReservations(guest);
        reservationsList.setItems(FXCollections.observableArrayList(reservations));
        reservationsList.setCellFactory(v -> new ListCell<Reservations>() {
            @Override protected void updateItem(Reservations r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) setText(null);
                else setText("Room " + r.getRoom().getRoomNumber() + " | " + r.getGuestCount() + " guest(s) | " + r.getStatus() + " | checkout " + r.getCheckOutDate() + " | paid $" + String.format("%.2f", r.getPaidAmount()) + (r.isExtensionRequested() ? " | extension pending" : ""));
            }
        });
    }

    private void show(Reservations r) {
        if (r == null) { detailLabel.setText("Select a reservation."); return; }
        detailLabel.setText("Room: " + r.getRoom().getRoomNumber() +
                "\nType: " + r.getRoom().getRoomType().getTypeName() +
                "\nCheck-in: " + r.getCheckInDate() +
                "\nCheck-out: " + r.getCheckOutDate() +
                "\nGuests: " + r.getGuestCount() + " / " + r.getRoom().getCapacity() +
                "\nNights: " + r.calculateTotalNights() +
                "\nPaid: $" + String.format("%.2f", r.getPaidAmount()) +
                "\nStatus: " + r.getStatus() +
                "\nExtension: " + (r.isExtensionRequested() ? "Requested until " + r.getRequestedCheckOutDate() : "No request") +
                "\nAmenities: " + (r.getRoom().getAmenities().isEmpty() ? "None" : r.getRoom().getAmenities()));
        if (r.isActive()) extensionDatePicker.setValue(r.getCheckOutDate().plusDays(1));
    }

    @FXML private void requestExtension() {
        Reservations selected = reservationsList.getSelectionModel().getSelectedItem();
        LocalDate newDate = extensionDatePicker.getValue();
        AsyncService.runAsync(() -> {
            HotelGuiService.requestExtension((Guest) AppSession.getCurrentUser(), selected, newDate);
            return null;
        }, ok -> {
            GuiUtils.info("Request Sent", "Staff can now accept or decline the extension.");
            refreshList();
        }, error -> GuiUtils.error("Extension Failed", rootMessage(error)));
    }

    private String rootMessage(Throwable t) { Throwable x = t; while (x.getCause() != null) x = x.getCause(); return x.getMessage(); }
    @FXML private void checkout() { HotelApp.show("/GUI/FXML/CheckoutView.fxml"); }
    @FXML private void back() { HotelApp.show("/GUI/FXML/GuestDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
