package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AppSession;
import GUI.Services.HotelGuiService;
import main_classes.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.List;

public class GuestDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private Label reservationsLabel;
    @FXML private Label availableLabel;

    @FXML
    private void initialize() {
        HotelGuiService.processAutomaticCheckouts();
        Guest guest = (Guest) AppSession.getCurrentUser();
        List<Reservations> reservations = HotelGuiService.activeReservations(guest);
        long available = HotelDatabase.rooms.stream().filter(Room::isAvailable).count();
        welcomeLabel.setText("Welcome, " + guest.getUsername());
        balanceLabel.setText("$" + String.format("%.2f", guest.getBalance()));
        reservationsLabel.setText(String.valueOf(reservations.size()));
        availableLabel.setText(String.valueOf(available));
    }

    @FXML private void availableRooms() { HotelApp.show("/GUI/FXML/AvailableRoomsView.fxml"); }
    @FXML private void makeReservation() { HotelApp.show("/GUI/FXML/MakeReservationView.fxml"); }
    @FXML private void myReservations() { HotelApp.show("/GUI/FXML/MyReservationsView.fxml"); }
    @FXML private void checkout() { HotelApp.show("/GUI/FXML/CheckoutView.fxml"); }
    @FXML private void creditCard() { HotelApp.show("/GUI/FXML/CreditCardView.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
