package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import main_classes.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminRoomsController {
    @FXML private ListView<Room> roomsList;
    @FXML private ComboBox<RoomType> typeBox;
    @FXML private TextField roomNumberField;
    @FXML private TextField capacityField;
    @FXML private Label detailsLabel;

    @FXML
    private void initialize() {
        roomsList.setCellFactory(v -> new ListCell<Room>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : "🏨 Room " + r.getRoomNumber() + "  •  " + r.getRoomType().getTypeName() + "  •  Capacity " + r.getCapacity() + "  •  " + (r.isAvailable() ? "Available" : "Occupied"));
            }
        });
        refresh();
        roomsList.getSelectionModel().selectedItemProperty().addListener((obs, old, room) -> {
            showDetails(room);
            loadSelectedRoomIntoForm(room);
        });
    }

    private void refresh() {
        roomsList.setItems(FXCollections.observableArrayList(HotelDatabase.rooms));
        typeBox.setItems(FXCollections.observableArrayList(HotelDatabase.roomTypes));
        if (!typeBox.getItems().isEmpty()) typeBox.getSelectionModel().selectFirst();
    }

    private void showDetails(Room r) {
        if (r == null) {
            detailsLabel.setText("Select a room to see full information.");
            return;
        }
        detailsLabel.setText("Room " + r.getRoomNumber() + "\nType: " + r.getRoomType().getTypeName() +
                "\nBase price: $" + r.getRoomType().getBasePrice() +
                "\nCapacity: " + r.getCapacity() +
                "\nStatus: " + (r.isAvailable() ? "Available" : "Occupied") +
                "\nAmenities: " + (r.getAmenities().isEmpty() ? "None" : r.getAmenities()));
    }

    private void loadSelectedRoomIntoForm(Room r) {
        if (r == null) return;
        roomNumberField.setText(r.getRoomNumber());
        capacityField.setText(String.valueOf(r.getCapacity()));
        typeBox.getSelectionModel().select(r.getRoomType());
    }

    @FXML private void addRoom() {
        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            HotelGuiService.addRoom(roomNumberField.getText(), typeBox.getValue(), capacity);
            roomNumberField.clear(); capacityField.clear(); refresh();
            GuiUtils.info("Room Added", "Room added successfully.");
        } catch (NumberFormatException e) { GuiUtils.error("Invalid Capacity", "Capacity must be a whole number."); }
        catch (Exception e) { GuiUtils.error("Could Not Add Room", e.getMessage()); }
    }


    @FXML private void updateRoom() {
        Room selected = roomsList.getSelectionModel().getSelectedItem();
        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            HotelGuiService.updateRoom(selected, roomNumberField.getText(), typeBox.getValue(), capacity);
            refresh();
            roomsList.getSelectionModel().select(selected);
            showDetails(selected);
            GuiUtils.info("Room Updated", "Room details updated successfully.");
        } catch (NumberFormatException e) { GuiUtils.error("Invalid Capacity", "Capacity must be a whole number."); }
        catch (Exception e) { GuiUtils.error("Could Not Update Room", e.getMessage()); }
    }

    @FXML private void clearForm() {
        roomsList.getSelectionModel().clearSelection();
        roomNumberField.clear();
        capacityField.clear();
        if (!typeBox.getItems().isEmpty()) typeBox.getSelectionModel().selectFirst();
        detailsLabel.setText("Select a room.");
    }

    @FXML private void removeRoom() {
        Room selected = roomsList.getSelectionModel().getSelectedItem();
        if (!GuiUtils.confirm("Remove Room", "Remove selected room?")) return;
        try {
            HotelGuiService.removeRoom(selected);
            refresh();
            clearForm();
            detailsLabel.setText("Room removed.");
        }
        catch (Exception e) { GuiUtils.error("Could Not Remove Room", e.getMessage()); }
    }

    @FXML private void back() { HotelApp.show("/GUI/FXML/AdminDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
