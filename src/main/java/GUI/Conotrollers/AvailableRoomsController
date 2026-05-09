package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AsyncService;
import GUI.Services.HotelGuiService;
import main_classes.Room;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AvailableRoomsController {
    @FXML private ListView<Room> roomsList;
    @FXML private Label countLabel;

    @FXML
    private void initialize() {
        roomsList.setCellFactory(v -> new ListCell<Room>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : "🛏 Room " + r.getRoomNumber() + " | " + r.getRoomType().getTypeName() + " | cap " + r.getCapacity() + " | $" + r.getRoomType().getBasePrice() + "/night | Amenities: " + (r.getAmenities().isEmpty() ? "None" : r.getAmenities()));
            }
        });
        refresh();
    }

    @FXML private void refresh() {
        countLabel.setText("Loading rooms using JavaFX Task...");
        AsyncService.runAsync(HotelGuiService::availableRooms, rooms -> {
            roomsList.setItems(FXCollections.observableArrayList(rooms));
            countLabel.setText(rooms.size() + " available room(s)");
        }, error -> countLabel.setText("Could not load rooms."));
    }

    @FXML private void bookSelected() { HotelApp.show("/GUI/FXML/MakeReservationView.fxml"); }
    @FXML private void back() { HotelApp.show("/GUI/FXML/GuestDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
