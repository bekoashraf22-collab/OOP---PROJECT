package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import main_classes.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminAmenitiesController {
    @FXML private ListView<Room> roomsList;
    @FXML private ListView<Amenity> globalAmenitiesList;
    @FXML private ListView<Amenity> roomAmenitiesList;
    @FXML private TextField amenityIdField;
    @FXML private TextField amenityNameField;
    @FXML private TextField amenityCostField;

    @FXML
    private void initialize() {
        refresh();
        roomsList.getSelectionModel().selectedItemProperty().addListener((obs, old, room) -> refreshRoomAmenities(room));
    }

    private void refresh() {
        roomsList.setItems(FXCollections.observableArrayList(HotelDatabase.rooms));
        globalAmenitiesList.setItems(FXCollections.observableArrayList(HotelDatabase.globalAmenities));
        if (!HotelDatabase.rooms.isEmpty()) roomsList.getSelectionModel().selectFirst();
    }

    private void refreshRoomAmenities(Room room) {
        if (room == null) {
            roomAmenitiesList.setItems(FXCollections.observableArrayList());
        } else {
            roomAmenitiesList.setItems(FXCollections.observableArrayList(room.getAmenities()));
        }
    }

    @FXML private void createAmenity() {
        try {
            double cost = Double.parseDouble(amenityCostField.getText().trim());
            HotelGuiService.addAmenity(amenityIdField.getText().trim(), amenityNameField.getText().trim(), cost);
            amenityIdField.clear(); amenityNameField.clear(); amenityCostField.clear(); refresh();
        } catch (NumberFormatException e) { GuiUtils.error("Invalid Input", "Cost must be a number."); }
        catch (Exception e) { GuiUtils.error("Could Not Create Amenity", e.getMessage()); }
    }

    @FXML private void addToRoom() {
        try {
            HotelGuiService.addAmenityToRoom(roomsList.getSelectionModel().getSelectedItem(), globalAmenitiesList.getSelectionModel().getSelectedItem());
            refreshRoomAmenities(roomsList.getSelectionModel().getSelectedItem());
        } catch (Exception e) { GuiUtils.error("Could Not Add Amenity", e.getMessage()); }
    }

    @FXML private void removeFromRoom() {
        try {
            HotelGuiService.removeAmenityFromRoom(roomsList.getSelectionModel().getSelectedItem(), roomAmenitiesList.getSelectionModel().getSelectedItem());
            refreshRoomAmenities(roomsList.getSelectionModel().getSelectedItem());
        } catch (Exception e) { GuiUtils.error("Could Not Remove Amenity", e.getMessage()); }
    }

    @FXML private void back() { HotelApp.show("/GUI/FXML/AdminDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
