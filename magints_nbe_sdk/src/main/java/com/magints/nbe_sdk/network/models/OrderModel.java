package com.magints.nbe_sdk.network.models;

public class OrderModel {
    private String currency;
    private String orderId;
    private String command;
    private String amount;
    private String merchantReference;
    private String sessionToken;
    private boolean createSessionAutomatically = true;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMerchantReference() {
        return merchantReference;
    }

    public void setMerchantReference(String merchantReference) {
        this.merchantReference = merchantReference;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public boolean isCreateSessionAutomatically() {
        return createSessionAutomatically;
    }

    public void setCreateSessionAutomatically(boolean createSessionAutomatically) {
        this.createSessionAutomatically = createSessionAutomatically;
    }
}
