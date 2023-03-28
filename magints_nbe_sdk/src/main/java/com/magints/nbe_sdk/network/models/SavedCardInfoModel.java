package com.magints.nbe_sdk.network.models;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SavedCardInfoModel {

    /*public SavedCardInfo(String jsonMapString) {
        super();

        Map<? extends String, ? extends Object> map = new Gson().fromJson(jsonMapString, new TypeToken<Map<? extends String, ? extends Object>>() {
        }.getType());

        putAll(map);
    }*/

    @SerializedName("brand")
    @Expose
    private String brand;
    @SerializedName("expiry")
    @Expose
    private SavedCardExpiryDateModel expiry;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public SavedCardExpiryDateModel getExpiry() {
        return expiry;
    }

    public void setExpiry(SavedCardExpiryDateModel expiry) {
        this.expiry = expiry;
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

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
