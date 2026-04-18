
 
public class Amenity {
    private String amenityId;
    private String name; // e.g., "WiFi", "Mini-bar"
    private double extraCost; // Optional, but good for a reservation system

    // Constructor
    public Amenity(String amenityId, String name, double extraCost) {
        this.amenityId = amenityId;
        this.name = name;
        this.extraCost = extraCost;
    }

    // Getters and Setters (Encapsulation)
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
