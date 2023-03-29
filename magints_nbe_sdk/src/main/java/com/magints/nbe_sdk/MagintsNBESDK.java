package com.magints.nbe_sdk;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.magints.nbe_sdk.network.models.OrderModel;
import com.magints.nbe_sdk.pages.ProcessPaymentActivity;
import com.magints.nbe_sdk.utils.PaymentEnvironment;
import com.magints.nbe_sdk.utils.SDKConfigurations;

public class MagintsNBESDK {
    final private Context mContext;
    private String currency;
    private String orderId;
    private String command;
    private String amount;
    private String merchantReference;
    private String sessionToken;
    private Boolean createSessionAutomatically = true;

    public MagintsNBESDK(Context mContext, String merchantId, String merchantPassword, Integer apiVersion, PaymentEnvironment paymentEnvironment) {
        this.mContext = mContext;
        SDKConfigurations.setMerchantId(merchantId);
        SDKConfigurations.requestUserPassword = merchantPassword;
        if (apiVersion != null) {
            SDKConfigurations.setApiVersion(apiVersion);
        } else {
            SDKConfigurations.setApiVersion(61);
        }

        SDKConfigurations.setBaseUrl(paymentEnvironment);
    }

    private void displayMessage(String message) {
        Toast.makeText(mContext, "" + message, Toast.LENGTH_SHORT).show();
    }

    private boolean validateConfigurationParameters() {
        if (SDKConfigurations.getMerchantId() == null || SDKConfigurations.getMerchantId().isEmpty()) {
            displayMessage("invalid merchant Id");
            return false;
        } else if (SDKConfigurations.requestUserPassword == null || SDKConfigurations.requestUserPassword.isEmpty()) {
            displayMessage("invalid merchant password");
            return false;
        } else if (SDKConfigurations.getApiVersion() == null) {
            displayMessage("invalid api Version");
            return false;
        } else if (SDKConfigurations.getBaseUrl() == null) {
            displayMessage("invalid payment environment");
            return false;
        } else {
            return true;
        }
    }

    public void initPayment(SDKConfigurations.MagintsNBECallback magintsNBECallback) {

        if (!validateConfigurationParameters()) {
            return;
        }
        if (currency == null || currency.isEmpty()) {
            currency = "EGP";
        }
        boolean isValidOrderParameters = true;
        String message = "";
        OrderModel orderModel = new OrderModel();
        orderModel.setOrderId(orderId);
        orderModel.setCommand(command);
        orderModel.setCurrency(currency);
        orderModel.setSessionToken(sessionToken);
        orderModel.setMerchantReference(merchantReference);
        if (createSessionAutomatically != null) {
            orderModel.setCreateSessionAutomatically(createSessionAutomatically);
            if (!createSessionAutomatically) {
                if (sessionToken == null || sessionToken.isEmpty()) {
                    isValidOrderParameters = false;
                    message = "Invalid session token";
                }
            }
        } else {
            orderModel.setCreateSessionAutomatically(true);
        }

        if (amount != null && !amount.isEmpty()) {
            orderModel.setAmount(amount);
        } else {
            isValidOrderParameters = false;
            message = "Invalid amount to pay";
        }
        if (orderId == null || orderId.isEmpty()) {
            isValidOrderParameters = false;
            message = "Invalid order id";
        }
        if (merchantReference == null || merchantReference.isEmpty()) {
            isValidOrderParameters = false;
            message = "Invalid merchant Reference";
        }
        if (isValidOrderParameters) {
            SDKConfigurations.magintsNBECallback = magintsNBECallback;
            SDKConfigurations.setOrderModel(orderModel);
             mContext.startActivity(new Intent(mContext, ProcessPaymentActivity.class));
            //mContext.startActivity(new Intent(mContext, MagintsNbeActivity.class));
        } else {
            Toast.makeText(mContext, "" + message, Toast.LENGTH_SHORT).show();
        }
    }

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
