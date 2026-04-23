package main_classes;

import exceptions.InvalidPricingException;

public class Amenity {
    private String amenityId;
    private String name; 
    private double extraCost; 

    public Amenity(String amenityId, String name, double extraCost) throws InvalidPricingException {
        if (extraCost < 0) throw new InvalidPricingException("Amenity cost cannot be negative.");
        this.amenityId = amenityId;
        this.name = name;
        this.extraCost = extraCost;
    }

    public String getAmenityId() { return amenityId; }
    public void setAmenityId(String amenityId) { this.amenityId = amenityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getExtraCost() { return extraCost; }
    public void setExtraCost(double extraCost) { this.extraCost = extraCost; }

    @Override
    public String toString() {
        return name + " (+$" + extraCost + ")";
    }
}