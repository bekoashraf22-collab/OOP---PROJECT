package main_classes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit; 
import enums.PaymentMethod;
import exceptions.*;

public abstract class User {
    private String username;
    private String password;
    private LocalDate dateOfBirth;

    
    public User(String username, String password, LocalDate dateOfBirth) 
           throws WeakPasswordException, InvalidUsernameException, UnderageGuestException {
        setUsername(username);
        setPassword(password);
        setDateOfBirth(dateOfBirth); 
    }

    public String getUsername() { return username; }
    
    public void setUsername(String username) throws InvalidUsernameException {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUsernameException("Username cannot be empty or null.");
        }
        this.username = username;
    }
    
    public String getPassword() { return password; }
    
    public void setPassword(String password) throws WeakPasswordException {
        if (password == null || password.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters long.");
        }
        this.password = password;
    }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    
    
    public void setDateOfBirth(LocalDate dateOfBirth) throws UnderageGuestException {
        long age = ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now());
        if (age < 18) {
            throw new UnderageGuestException("User must be at least 18 years old. Calculated age: " + age);
        }
        this.dateOfBirth = dateOfBirth;
    }
    
    public boolean login(String inputUsername, String inputPassword) {
        return this.username.equals(inputUsername) && this.password.equals(inputPassword);
    }
    
    public abstract void processPayment(double amount, PaymentMethod method) throws InvalidPaymentException;
    public abstract void displayMenu();
}