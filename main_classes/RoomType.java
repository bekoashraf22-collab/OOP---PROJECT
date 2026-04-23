package main_classes;

import exceptions.InvalidPricingException;

public class RoomType {
    private String typeId;
    private String typeName; 
    private double basePrice;
    private int capacity;

    public RoomType(String typeId, String typeName, double basePrice, int capacity) throws InvalidPricingException {
        if (basePrice < 0) throw new InvalidPricingException("Room price cannot be negative.");
        this.typeId = typeId;
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.capacity = capacity;
    }

    public String getTypeId() { return typeId; }
    public void setTypeId(String typeId) { this.typeId = typeId; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return typeName + " - $" + basePrice + "/night";
    }
