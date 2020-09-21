package com.smartechBrainTechnologies.freshFishHub;

public class ModelShortFishDetails {

    private String fishID;
    private String fishImage;
    private String fishName;
    private String fishPrice;
    private String fishAvailability;

    public ModelShortFishDetails() {
    }

    public ModelShortFishDetails(String fishID, String fishImage, String fishName, String fishPrice, String fishAvailability) {
        this.fishID = fishID;
        this.fishImage = fishImage;
        this.fishName = fishName;
        this.fishPrice = fishPrice;
        this.fishAvailability = fishAvailability;
    }

    public String getFishID() {
        return fishID;
    }

    public String getFishImage() {
        return fishImage;
    }

    public String getFishName() {
        return fishName;
    }

    public String getFishPrice() {
        return fishPrice;
    }

    public String getFishAvailability() {
        return fishAvailability;
    }

}
