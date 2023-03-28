package com.magints.nbe_sdk.utils;

import android.util.Log;

import com.magints.nbe_sdk.MagintsNBESDK;
import com.magints.nbe_sdk.network.models.CallbackResultModel;
import com.magints.nbe_sdk.network.models.OrderModel;
import com.mastercard.gateway.android.sdk.Gateway;

public class SDKConfigurations {
    private static Gateway.Region defaultGatewayRegion = Gateway.Region.EUROPE;
    private static String merchantId = "TESTEGPTEST";

    private static String VERSION_NAME = "27";
    private static Integer apiVersion = 61;

    public static final String USER_AGENT = "Gateway-Android-SDK/1.1.5";
    private static String baseUrl = null;

    public static String requestUrl = null;
    public static String requestUsername = "merchant." + merchantId;
    public static String requestUserPassword = null;

    private static OrderModel orderModel = null;

    public static MagintsNBECallback magintsNBECallback;

    public static Gateway.Region getDefaultGatewayRegion() {
        return defaultGatewayRegion;
    }

    public static void setDefaultGatewayRegion(Gateway.Region defaultGatewayRegion) {
        SDKConfigurations.defaultGatewayRegion = defaultGatewayRegion;
    }

    public static String getMerchantId() {
        return merchantId;
    }

    public static void setMerchantId(String merchantId) {
        SDKConfigurations.merchantId = merchantId;
    }

    public static void setBaseUrl(PaymentEnvironment paymentEnvironment) {
        SDKConfigurations.baseUrl = paymentEnvironment.environment;

        SDKConfigurations.requestUrl = baseUrl + "/api/rest/version/" + apiVersion + "/merchant/" + merchantId;
        Log.d("SDKConfigurations.baseUrl","SDKConfigurations baseUrl : "+baseUrl+"" +
                " - "+SDKConfigurations.requestUrl);
    }

    public static Integer getApiVersion() {
        return apiVersion;
    }

    public static void setApiVersion(Integer apiVersion) {
        SDKConfigurations.apiVersion = apiVersion;
    }

    public static OrderModel getOrderModel() {
        return orderModel;
    }

    public static void setOrderModel(OrderModel orderModel) {
        SDKConfigurations.orderModel = orderModel;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String baseUrl) {
        SDKConfigurations.baseUrl = baseUrl;
    }


    public interface MagintsNBECallback {
        void onResult(CallbackResultModel callbackResultModel);
    }

}
