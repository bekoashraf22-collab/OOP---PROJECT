package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AppSession;
import GUI.Services.AsyncService;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import enums.Gender;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main_classes.Admin;
import main_classes.Receptionist;
import java.time.LocalDate;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField balanceField;
    @FXML private TextField addressField;
    @FXML private ComboBox<Gender> genderBox;
    @FXML private ComboBox<String> accountTypeBox;
    @FXML private TextField workingHoursField;
    @FXML private PasswordField overrideKeyField;
    @FXML private Label staffHelpLabel;
    @FXML private Label titleLabel;
    @FXML private Button registerButton;

    @FXML
    private void initialize() {
        genderBox.getItems().setAll(Gender.values());
        genderBox.getSelectionModel().select(Gender.MALE);
        accountTypeBox.getItems().setAll("Guest", "Receptionist", "Admin");
        accountTypeBox.getSelectionModel().select("Guest");
        dobPicker.setValue(LocalDate.of(2000, 1, 1));
        balanceField.setText("1000");
        workingHoursField.setText("40");
        accountTypeBox.valueProperty().addListener((obs, old, val) -> updateMode());
        updateMode();
    }

    private void updateMode() {
        String type = accountTypeBox.getValue();
        boolean staffAccount = !"Guest".equals(type);
        titleLabel.setText(staffAccount ? "Create Staff/Admin Account" : "Create Guest Account");
        registerButton.setText(staffAccount ? "Create " + type + " Account" : "Create Guest Account");
        staffHelpLabel.setText(staffAccount
                ? "Override rules: ADMIN-2026 can create Admin or Receptionist. STAFF-2026 can create Receptionist only."
                : "Guest accounts do not need an override key.");
    }

    @FXML
    private void register() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            LocalDate dob = dobPicker.getValue();
            String type = accountTypeBox.getValue();
            registerButton.setDisable(true);

            if ("Guest".equals(type)) {
                double balance = Double.parseDouble(balanceField.getText().trim());
                String address = addressField.getText().trim().isEmpty() ? "Unknown" : addressField.getText().trim();
                Gender gender = genderBox.getValue();
                AsyncService.runAsync(() -> {
                    try { return HotelGuiService.registerGuest(username, password, dob, balance, address, gender); }
                    catch (Exception e) { throw new RuntimeException(e); }
                }, guest -> {
                    registerButton.setDisable(false);
                    GuiUtils.info("Account Created", "Guest account created successfully.");
                    goBackAfterCreate();
                }, error -> showRegistrationError(error));
            } else {
                int hours = Integer.parseInt(workingHoursField.getText().trim());
                String normalizedType = "Admin".equals(type) ? "ADMIN" : "RECEPTIONIST";
                String key = overrideKeyField.getText().trim();
                AsyncService.runAsync(() -> {
                    try { return HotelGuiService.registerStaffAccount(username, password, dob, hours, normalizedType, key); }
                    catch (Exception e) { throw new RuntimeException(e); }
                }, staff -> {
                    registerButton.setDisable(false);
                    GuiUtils.info("Account Created", type + " account created successfully.");
                    goBackAfterCreate();
                }, error -> showRegistrationError(error));
            }
        } catch (NumberFormatException e) {
            registerButton.setDisable(false);
            GuiUtils.error("Invalid Input", "Balance and working hours must be valid numbers.");
        }
    }

    private void showRegistrationError(Throwable error) {
        registerButton.setDisable(false);
        GuiUtils.error("Registration Failed", rootMessage(error));
    }

    private void goBackAfterCreate() {
        if (AppSession.getCurrentUser() instanceof Admin) HotelApp.show("/GUI/FXML/AdminDashboard.fxml");
        else if (AppSession.getCurrentUser() instanceof Receptionist) HotelApp.show("/GUI/FXML/ReceptionistDashboard.fxml");
        else HotelApp.show("/GUI/FXML/LoginView.fxml");
    }

    private String rootMessage(Throwable t) {
        Throwable x = t;
        while (x.getCause() != null) x = x.getCause();
        return x.getMessage();
    }

    @FXML private void back() { goBackAfterCreate(); }
}
