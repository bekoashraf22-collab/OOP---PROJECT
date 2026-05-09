package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AppSession;
import GUI.Services.AsyncService;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import main_classes.Admin;
import main_classes.Guest;
import main_classes.Receptionist;
import main_classes.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        statusLabel.setText("");
    }

    @FXML
    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Enter username and password.");
            return;
        }
        loginButton.setDisable(true);
        statusLabel.setText("Signing in...");
        AsyncService.runAsync(() -> HotelGuiService.login(username, password), user -> {
            loginButton.setDisable(false);
            if (user == null) {
                statusLabel.setText("Invalid credentials.");
                return;
            }
            AppSession.setCurrentUser(user);
            if (user instanceof Admin) HotelApp.show("/GUI/FXML/AdminDashboard.fxml");
            else if (user instanceof Receptionist) HotelApp.show("/GUI/FXML/ReceptionistDashboard.fxml");
            else if (user instanceof Guest) HotelApp.show("/GUI/FXML/GuestDashboard.fxml");
        }, error -> {
            loginButton.setDisable(false);
            GuiUtils.error("Login Error", error.getMessage());
        });
    }

    @FXML private void openRegister() { HotelApp.show("/GUI/FXML/RegisterView.fxml"); }
}
