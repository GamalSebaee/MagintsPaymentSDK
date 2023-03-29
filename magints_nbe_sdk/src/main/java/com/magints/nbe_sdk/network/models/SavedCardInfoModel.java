package com.magints.nbe_sdk.network.models;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SavedCardInfoModel {

    @SerializedName("nameOnCard")
    @Expose
    private String nameOnCard;
    @SerializedName("brand")
    @Expose
    private String brand;
    @SerializedName("expiry")
    @Expose
    private String expiry;
    @SerializedName("fundingMethod")
    @Expose
    private String fundingMethod;
    @SerializedName("number")
    @Expose
    private String number;
    @SerializedName("scheme")
    @Expose
    private String scheme;
    @SerializedName("securityCode")
    @Expose
    private String securityCode;

    private String savedCardToken;

    private SavedCardExpiryDateModel expiryDate;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public SavedCardExpiryDateModel getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryMonth,String expiryYear) {
        SavedCardExpiryDateModel expiryDateVal=new SavedCardExpiryDateModel();
        expiryDateVal.setMonth(expiryMonth);
        expiryDateVal.setYear(expiryYear);
        this.expiryDate = expiryDateVal;
    }

    public String getFundingMethod() {
        return fundingMethod;
    }

    public void setFundingMethod(String fundingMethod) {
        this.fundingMethod = fundingMethod;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public String getSavedCardToken() {
        return savedCardToken;
    }

    public void setSavedCardToken(String savedCardToken) {
        this.savedCardToken = savedCardToken;
    }

    public static SavedCardInfoModel newInstance(Object obj) {
        return new Gson().fromJson(new Gson().toJson(obj), SavedCardInfoModel.class);
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
