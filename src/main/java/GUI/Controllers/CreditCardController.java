package GUI.Controllers;

import GUI.CODE.HotelApp;
import GUI.Services.AppSession;
import GUI.Services.AsyncService;
import GUI.Services.HotelGuiService;
import GUI.Util.GuiUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main_classes.Guest;

public class CreditCardController {
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private PasswordField cvvField;
    @FXML private TextField amountField;
    @FXML private Label balanceLabel;
    @FXML private Button addButton;

    @FXML private void initialize() { refreshBalance(); }

    private void refreshBalance() {
        Guest guest = (Guest) AppSession.getCurrentUser();
        balanceLabel.setText("Current balance: $" + String.format("%.2f", guest.getBalance()));
    }

    @FXML private void addBalance() {
        double amount;
        try { amount = Double.parseDouble(amountField.getText().trim()); }
        catch (NumberFormatException e) { GuiUtils.error("Invalid Amount", "Amount must be a number."); return; }

        addButton.setDisable(true);
        AsyncService.runAsync(() -> {
            HotelGuiService.addGuestBalanceWithCard((Guest) AppSession.getCurrentUser(), cardNumberField.getText(), expiryField.getText(), cvvField.getText(), amount);
            return null;
        }, ok -> {
            addButton.setDisable(false);
            refreshBalance();
            GuiUtils.info("Balance Added", "Balance updated successfully.");
        }, error -> {
            addButton.setDisable(false);
            GuiUtils.error("Card Failed", rootMessage(error));
        });
    }

    private String rootMessage(Throwable t) { Throwable x = t; while (x.getCause() != null) x = x.getCause(); return x.getMessage(); }
    @FXML private void back() { HotelApp.show("/GUI/FXML/GuestDashboard.fxml"); }
    @FXML private void logout() { HotelApp.logout(); }
}
