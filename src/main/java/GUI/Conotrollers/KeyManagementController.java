package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AppSession;
import GUI.Services.AsyncService;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main_classes.Reservations;

public class KeyManagementController {
    @FXML private ListView<Reservations> reservationList;
    @FXML private Label detailLabel;
    @FXML private Label statusLabel;

    @FXML private void initialize() {
        refresh();
        reservationList.setCellFactory(v -> new ListCell<Reservations>() {
            @Override protected void updateItem(Reservations r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) setText(null);
                else setText("Room " + r.getRoom().getRoomNumber() + " | " + r.getGuest().getUsername() + " | " + (r.isKeyIssued() ? "Key issued" : "Key not issued"));
            }
        });
        reservationList.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> show(r));
        if (!reservationList.getItems().isEmpty()) reservationList.getSelectionModel().selectFirst();
    }

    private void refresh() {
        reservationList.setItems(FXCollections.observableArrayList(HotelGuiService.activeHotelReservations()));
    }

    private void show(Reservations r) {
        if (r == null) {
            detailLabel.setText("No active reservation selected.");
            return;
        }
        detailLabel.setText("Guest: " + r.getGuest().getUsername()
                + "\nRoom: " + r.getRoom().getRoomNumber()
                + "\nType: " + r.getRoom().getRoomType().getTypeName()
                + "\nGuests staying: " + r.getGuestCount()
                + "\nCheck-out: " + r.getCheckOutDate()
                + "\nKey status: " + (r.isKeyIssued() ? "Issued" : "Not issued"));
    }

    @FXML private void issueKey() {
        Reservations selected = reservationList.getSelectionModel().getSelectedItem();
        statusLabel.setText("Updating key status...");
        AsyncService.runAsync(() -> {
            HotelGuiService.issueRoomKey(selected, AppSession.getCurrentUser());
            return selected;
        }, r -> {
            refresh();
            reservationList.getSelectionModel().select(r);
            statusLabel.setText("Key issued.");
            show(r);
        }, error -> {
            statusLabel.setText("");
            GuiUtils.error("Key update failed", error.getMessage());
        });
    }

    @FXML private void returnKey() {
        Reservations selected = reservationList.getSelectionModel().getSelectedItem();
        statusLabel.setText("Updating key status...");
        AsyncService.runAsync(() -> {
            HotelGuiService.returnRoomKey(selected, AppSession.getCurrentUser());
            return selected;
        }, r -> {
            refresh();
            reservationList.getSelectionModel().select(r);
            statusLabel.setText("Key returned.");
            show(r);
        }, error -> {
            statusLabel.setText("");
            GuiUtils.error("Key update failed", error.getMessage());
        });
    }

    @FXML private void back() { HotelApp.show("/GUI/FXML/ReceptionistDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
