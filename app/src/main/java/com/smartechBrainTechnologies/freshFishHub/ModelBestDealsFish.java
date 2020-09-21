package com.smartechBrainTechnologies.freshFishHub;

public class ModelBestDealsFish {

    private String fishID;
    private String fishImage;
    private String fishName;
    private String fishOldPrice;
    private String fishNewPrice;
    private String fishAvailability;

    public ModelBestDealsFish() {
    }

    public ModelBestDealsFish(String fishID, String fishImage, String fishName, String fishOldPrice, String fishNewPrice, String fishAvailability) {
        this.fishID = fishID;
        this.fishImage = fishImage;
        this.fishName = fishName;
        this.fishOldPrice = fishOldPrice;
        this.fishNewPrice = fishNewPrice;
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

    public String getFishOldPrice() {
        return fishOldPrice;
    }

    public String getFishNewPrice() {
        return fishNewPrice;
    }

    public String getFishAvailability() {
        return fishAvailability;
    }
}
