package com.smartechBrainTechnologies.freshFishHub;

public class ModelAddress {

    private String addressName;
    private String address;
    private String addressStatus;
    private String addressContact;
    private String addressDeliveryStatus;

    public ModelAddress() {
    }

    public ModelAddress(String addressName, String address, String addressStatus, String addressContact, String addressDeliveryStatus) {
        this.addressName = addressName;
        this.address = address;
        this.addressStatus = addressStatus;
        this.addressContact = addressContact;
        this.addressDeliveryStatus = addressDeliveryStatus;
    }

    public String getAddressName() {
        return addressName;
    }

    public String getAddress() {
        return address;
    }

    public String getAddressStatus() {
        return addressStatus;
    }

    public String getAddressContact() {
        return addressContact;
    }

    public String getAddressDeliveryStatus() {
        return addressDeliveryStatus;
    }
}
