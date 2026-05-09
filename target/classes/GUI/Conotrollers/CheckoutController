package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AppSession;
import GUI.Services.HotelGuiService;
import main_classes.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public class CheckoutController {
    @FXML private ListView<String> invoiceList;
    @FXML private Label totalLabel;
    @FXML private Button checkoutButton;

    @FXML private void initialize() { refreshInvoice(); }

    private void refreshInvoice() {
        HotelGuiService.processAutomaticCheckouts();
        Guest guest = currentGuest();
        List<Reservations> reservations = HotelGuiService.allReservations(guest);
        invoiceList.getItems().clear();
        double activePaid = 0;
        for (Reservations r : reservations) {
            invoiceList.getItems().add("Room " + r.getRoom().getRoomNumber() + " | " + r.getGuestCount() + " guest(s) | " + r.getStatus() + " | checkout " + r.getCheckOutDate() + " | paid $" + String.format("%.2f", r.getPaidAmount()));
            if (r.isActive()) activePaid += r.getPaidAmount();
        }
        totalLabel.setText("Active prepaid amount: $" + String.format("%.2f", activePaid) + "   Current balance: $" + String.format("%.2f", guest.getBalance()));
    }

    private Guest currentGuest() { return (Guest) AppSession.getCurrentUser(); }
    @FXML private void pay() { refreshInvoice(); }
    @FXML private void back() { HotelApp.show("/GUI/FXML/GuestDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
