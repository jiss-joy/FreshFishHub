package com.smartechBrainTechnologies.freshFishHub;

public class ModelShortOrder {

    private String orderID;
    private String orderFishImage;
    private String orderFishName;
    private String orderFishQty;
    private String orderFishPrice;
    private String orderStatus;

    public ModelShortOrder() {
    }

    public ModelShortOrder(String orderID, String orderFishImage, String orderFishName, String orderFishQty, String orderFishPrice, String orderStatus) {
        this.orderID = orderID;
        this.orderFishImage = orderFishImage;
        this.orderFishName = orderFishName;
        this.orderFishQty = orderFishQty;
        this.orderFishPrice = orderFishPrice;
        this.orderStatus = orderStatus;
    }


    public String getOrderID() {
        return orderID;
    }

    public String getOrderFishImage() {
        return orderFishImage;
    }

    public String getOrderFishName() {
        return orderFishName;
    }

    public String getOrderFishQty() {
        return orderFishQty;
    }

    public String getOrderFishPrice() {
        return orderFishPrice;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}
