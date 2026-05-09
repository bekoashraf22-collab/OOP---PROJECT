package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AppSession;
import GUI.Services.AsyncService;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import enums.Gender;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main_classes.Admin;
import main_classes.Receptionist;

import java.time.LocalDate;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private PasswordField cvvField;
    @FXML private TextField balanceField;
    @FXML private TextField addressField;
    @FXML private ComboBox<Gender> genderBox;
    @FXML private ComboBox<String> accountTypeBox;
    @FXML private TextField workingHoursField;
    @FXML private PasswordField overrideKeyField;
    @FXML private Label staffHelpLabel;
    @FXML private Label titleLabel;
    @FXML private Button registerButton;
    @FXML private VBox guestDetailsBox;
    @FXML private VBox staffDetailsBox;

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
        boolean guestAccount = "Guest".equals(type);
        titleLabel.setText(guestAccount ? "Create Guest Account" : "Create " + type + " Account");
        registerButton.setText(guestAccount ? "Create Guest" : "Create " + type);
        staffHelpLabel.setText(guestAccount ? "" : "Admin key creates admin/receptionist. Staff key creates receptionist only.");
        guestDetailsBox.setVisible(guestAccount);
        guestDetailsBox.setManaged(guestAccount);
        staffDetailsBox.setVisible(!guestAccount);
        staffDetailsBox.setManaged(!guestAccount);
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
                    try {
                        return HotelGuiService.registerGuestWithCard(username, password, dob, address, gender,
                                cardNumberField.getText(), expiryField.getText(), cvvField.getText(), balance);
                    } catch (Exception e) { throw new RuntimeException(e); }
                }, guest -> {
                    registerButton.setDisable(false);
                    GuiUtils.info("Account Created", "Guest account created.");
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
                    GuiUtils.info("Account Created", type + " account created.");
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
