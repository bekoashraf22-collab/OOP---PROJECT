package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AppSession;
import GUI.Services.AsyncService;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main_classes.Guest;
import main_classes.HotelDatabase;

public class StaffBalanceController {
    @FXML private ListView<Guest> guestList;
    @FXML private TextField amountField;
    @FXML private TextField noteField;
    @FXML private Label detailLabel;
    @FXML private Label statusLabel;

    @FXML private void initialize() {
        refresh();
        guestList.setCellFactory(v -> new ListCell<Guest>() {
            @Override protected void updateItem(Guest guest, boolean empty) {
                super.updateItem(guest, empty);
                setText(empty || guest == null ? null : guest.getUsername() + " | balance $" + String.format("%.2f", guest.getBalance()));
            }
        });
        guestList.getSelectionModel().selectedItemProperty().addListener((obs, old, guest) -> showGuest(guest));
        if (!guestList.getItems().isEmpty()) guestList.getSelectionModel().selectFirst();
    }

    private void refresh() {
        guestList.setItems(FXCollections.observableArrayList(HotelDatabase.guests));
    }

    private void showGuest(Guest guest) {
        if (guest == null) {
            detailLabel.setText("Select a guest.");
            return;
        }
        detailLabel.setText("Guest: " + guest.getUsername()
                + "\nBalance: $" + String.format("%.2f", guest.getBalance())
                + "\nAddress: " + guest.getAddress());
    }

    @FXML private void addBalance() {
        Guest guest = guestList.getSelectionModel().getSelectedItem();
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (Exception e) {
            GuiUtils.error("Invalid amount", "Enter a valid positive number.");
            return;
        }
        statusLabel.setText("Updating balance...");
        AsyncService.runAsync(() -> {
            HotelGuiService.staffAddGuestBalance(guest, amount, noteField.getText(), AppSession.getCurrentUser());
            return guest;
        }, updated -> {
            refresh();
            guestList.getSelectionModel().select(updated);
            amountField.clear();
            noteField.clear();
            statusLabel.setText("Balance updated.");
            showGuest(updated);
        }, error -> {
            statusLabel.setText("");
            GuiUtils.error("Could not add balance", error.getMessage());
        });
    }

    @FXML private void back() { HotelApp.show("/GUI/FXML/ReceptionistDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
