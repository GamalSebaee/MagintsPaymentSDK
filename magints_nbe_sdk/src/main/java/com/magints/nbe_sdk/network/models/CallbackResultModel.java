package com.magints.nbe_sdk.network.models;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class CallbackResultModel {
    private Object result;
    private String failMessage;
    private SavedCardInfoModel savedCardInfo;
    private boolean status;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }


    public SavedCardInfoModel getSavedCardInfo() {
        return savedCardInfo;
    }

    public void setSavedCardInfo(SavedCardInfoModel savedCardInfo) {
        this.savedCardInfo = savedCardInfo;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
