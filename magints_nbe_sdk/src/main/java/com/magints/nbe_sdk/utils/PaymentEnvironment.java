package com.magints.nbe_sdk.utils;

public enum PaymentEnvironment {
    live("https://test-nbe.gateway.mastercard.com"),test("https://test-nbe.gateway.mastercard.com");
    String environment;
    PaymentEnvironment(String s) {
        this.environment=s;
    }
}
