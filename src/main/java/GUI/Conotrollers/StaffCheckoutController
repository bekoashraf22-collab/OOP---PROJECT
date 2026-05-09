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
import java.util.ArrayList;
import java.util.List;

public class StaffCheckoutController {
    @FXML private ListView<Reservations> reservationsList;
    @FXML private TextField refundField;
    @FXML private Label totalLabel;
    @FXML private Button checkoutButton;

    @FXML private void initialize() {
        refundField.setText("0");
        refreshReservations();
        reservationsList.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> show(r));
    }

    private void refreshReservations() {
        List<Reservations> active = new ArrayList<>();
        for (Guest guest : HotelDatabase.guests) active.addAll(HotelGuiService.activeReservations(guest));
        reservationsList.setItems(FXCollections.observableArrayList(active));
        reservationsList.setCellFactory(v -> new ListCell<Reservations>() {
            @Override protected void updateItem(Reservations r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) setText(null);
                else setText(r.getGuest().getUsername() + " | Room " + r.getRoom().getRoomNumber() + " | checkout " + r.getCheckOutDate() + " | paid $" + String.format("%.2f", r.getPaidAmount()));
            }
        });
        if (!active.isEmpty()) reservationsList.getSelectionModel().selectFirst();
    }

    private void show(Reservations r) {
        if (r == null) { totalLabel.setText("Select an active stay."); return; }
        totalLabel.setText("Guest: " + r.getGuest().getUsername() +
                "\nRoom: " + r.getRoom().getRoomNumber() +
                "\nPaid: $" + String.format("%.2f", r.getPaidAmount()) +
                "\nCurrent checkout date: " + r.getCheckOutDate() +
                "\nMaximum refund allowed: $" + String.format("%.2f", r.getPaidAmount()));
    }

    @FXML private void checkout() {
        double refund;
        try { refund = Double.parseDouble(refundField.getText().trim()); }
        catch (NumberFormatException e) { GuiUtils.error("Invalid Refund", "Refund must be a number."); return; }
        Reservations selected = reservationsList.getSelectionModel().getSelectedItem();
        checkoutButton.setDisable(true);
        AsyncService.runAsync(() -> HotelGuiService.staffEarlyCheckout(selected, AppSession.getCurrentUser(), refund), result -> {
            checkoutButton.setDisable(false);
            GuiUtils.info("Early Checkout Complete", result.guestName + " checked out. Refunded: $" + String.format("%.2f", result.total));
            refreshReservations();
        }, error -> {
            checkoutButton.setDisable(false);
            GuiUtils.error("Checkout Failed", rootMessage(error));
        });
    }

    private String rootMessage(Throwable t) { Throwable x = t; while (x.getCause() != null) x = x.getCause(); return x.getMessage(); }
    @FXML private void back() { HotelApp.show("/GUI/FXML/ReceptionistDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
