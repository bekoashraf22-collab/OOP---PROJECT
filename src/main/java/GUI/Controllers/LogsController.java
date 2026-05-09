package GUI.Controllers;

import GUI.CODE.HotelApp;
import main_classes.HotelDatabase;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class LogsController {
    @FXML private ListView<String> logsList;
    @FXML private void initialize() { logsList.setItems(FXCollections.observableArrayList(HotelDatabase.systemLogs)); }
    @FXML private void refresh() { initialize(); }
    @FXML private void back() { HotelApp.show("/GUI/FXML/AdminDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
