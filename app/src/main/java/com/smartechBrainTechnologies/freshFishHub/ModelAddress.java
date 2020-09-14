package com.smartechBrainTechnologies.freshFishHub;

public class ModelAddress {

    private String addressArea;
    private String addressBuilding;
    private String addressCity;
    private String addressLandmark;
    private String addressName;
    private String addressPin;
    private String addressState;
    private String addressStatus;

    public ModelAddress() {
    }

    public ModelAddress(String addressArea, String addressBuilding, String addressCity,
                        String addressLandmark, String addressName, String addressPin, String addressState, String addressStatus) {
        this.addressArea = addressArea;
        this.addressBuilding = addressBuilding;
        this.addressCity = addressCity;
        this.addressLandmark = addressLandmark;
        this.addressName = addressName;
        this.addressPin = addressPin;
        this.addressState = addressState;
        this.addressStatus = addressStatus;
    }


    public String getAddressArea() {
        return addressArea;
    }

    public String getAddressBuilding() {
        return addressBuilding;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public String getAddressLandmark() {
        return addressLandmark;
    }

    public String getAddressName() {
        return addressName;
    }

    public String getAddressPin() {
        return addressPin;
    }

    public String getAddressState() {
        return addressState;
    }

    public String getAddressStatus() {
        return addressStatus;
    }
}
