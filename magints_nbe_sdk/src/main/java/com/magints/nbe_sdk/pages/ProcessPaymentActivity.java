package com.magints.nbe_sdk.pages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.magints.nbe_sdk.network.ApiController;
import com.magints.nbe_sdk.network.models.CallbackResultModel;
import com.magints.nbe_sdk.network.models.SavedCardInfoModel;
import com.magints.nbe_sdk.utils.Config;
import com.magints.nbe_sdk.utils.SDKConfigurations;
import com.magints.nbe_sdk.R;
import com.magints.nbe_sdk.databinding.ActivityProcessPaymentBinding;
import com.mastercard.gateway.android.sdk.Gateway;
import com.mastercard.gateway.android.sdk.Gateway3DSecureCallback;
import com.mastercard.gateway.android.sdk.GatewayCallback;
import com.mastercard.gateway.android.sdk.GatewayMap;

import java.util.UUID;

public class ProcessPaymentActivity extends BaseActivity {

    static final int REQUEST_CARD_INFO = 100;

    // static for demo
    static String AMOUNT = "1.00";
    static String CURRENCY = "EGP";

    ActivityProcessPaymentBinding binding;
    Gateway gateway;
    String sessionId, apiVersion, threeDSecureId, orderId, transactionId;
    String threeDSTransactionId;
    boolean isGooglePay = false;
    ApiController apiController = ApiController.getInstance();

    boolean saveCard = false;

    SavedCardInfoModel savedCardInfo;

    boolean isOperationsCanceled=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProcessPaymentBinding.inflate(LayoutInflater.from(this)); //initializing the binding class
        setContentView(binding.getRoot());
        AMOUNT = SDKConfigurations.getOrderModel().getAmount();
        CURRENCY = SDKConfigurations.getOrderModel().getCurrency();
        // init api controller
        apiController.setMerchantServerUrl(Config.MERCHANT_URL.getValue(this));

        Log.d("SDKConfigurations", "SDKConfigurations: " + SDKConfigurations.getMerchantId());
        // init gateway
        gateway = new Gateway();
        gateway.setMerchantId(SDKConfigurations.getMerchantId());
        try {
            Gateway.Region region = SDKConfigurations.getDefaultGatewayRegion();
            gateway.setRegion(region);
        } catch (Exception e) {
            Log.e(ProcessPaymentActivity.class.getSimpleName(), "Invalid Gateway region value provided", e);
        }

        orderId = SDKConfigurations.getOrderModel().getOrderId();
        transactionId = SDKConfigurations.getOrderModel().getMerchantReference();
        // bind buttons
        binding.startButton.setOnClickListener(v -> createSession());
        binding.confirmButton.setOnClickListener(v -> {
            // 3DS is not applicable to Google Pay transactions
            if (isGooglePay) {
                processPayment();
            } else {
                check3dsEnrollment();
            }
        });
        binding.doneButton.setOnClickListener(v -> finish());
        binding.doneButtonTest.setOnClickListener(v -> check3dsEnrollment());

        initUI();
        if (SDKConfigurations.getOrderModel() != null && SDKConfigurations.getOrderModel().getSessionToken() != null && !SDKConfigurations.getOrderModel().getSessionToken().isEmpty()) {
            Log.i("CreateSessionTask", "Session established");
            binding.createSessionProgress.setVisibility(View.GONE);
            binding.createSessionSuccess.setVisibility(View.VISIBLE);

            ProcessPaymentActivity.this.sessionId = SDKConfigurations.getOrderModel().getSessionToken();
            ProcessPaymentActivity.this.apiVersion = "" + SDKConfigurations.getApiVersion();

            collectCardInfo();
        } else {
            createSession();
        }
        binding.toolbarClose.setOnClickListener(v -> super.onBackPressed());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle the 3DSecure lifecycle
        if (Gateway.handle3DSecureResult(requestCode, resultCode, data, new ThreeDSecureCallback())) {
            return;
        }

        if (requestCode == REQUEST_CARD_INFO) {
            binding.collectCardInfoProgress.setVisibility(View.GONE);

            if (resultCode == Activity.RESULT_OK) {
                binding.collectCardInfoSuccess.setVisibility(View.VISIBLE);

                String googlePayToken = data.getStringExtra(CollectCardInfoActivity.EXTRA_PAYMENT_TOKEN);

               /* String cardDescription = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_DESCRIPTION);
                binding.confirmCardDescription.setText(cardDescription);*/

                if (googlePayToken != null) {
                    isGooglePay = true;

                    binding.check3dsLabel.setPaintFlags(binding.check3dsLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    String paymentToken = data.getStringExtra(CollectCardInfoActivity.EXTRA_PAYMENT_TOKEN);

                    updateSession(paymentToken);
                } else {
                    isGooglePay = false;

                    String cardName = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_NAME);
                    String cardNumber = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_NUMBER);
                    String cardExpiryMonth = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_EXPIRY_MONTH);
                    String cardExpiryYear = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_EXPIRY_YEAR);
                    String cardCvv = data.getStringExtra(CollectCardInfoActivity.EXTRA_CARD_CVV);
                    saveCard = data.getBooleanExtra(CollectCardInfoActivity.EXTRA_SAVE_CARD, false);
                    updateSession(cardName, cardNumber, cardExpiryMonth, cardExpiryYear, cardCvv);
                }

            } else {
                binding.collectCardInfoError.setVisibility(View.VISIBLE);

                showResult(R.drawable.failed, R.string.pay_error_card_info_not_collected);
            }

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("SetTextI18n")
    void initUI() {
        binding.startButton.setText("Process Payment for " + AMOUNT + " " + CURRENCY);
        binding.createSessionProgress.setVisibility(View.GONE);
        binding.createSessionSuccess.setVisibility(View.GONE);
        binding.createSessionError.setVisibility(View.GONE);

        binding.collectCardInfoProgress.setVisibility(View.GONE);
        binding.collectCardInfoSuccess.setVisibility(View.GONE);
        binding.collectCardInfoError.setVisibility(View.GONE);

        binding.updateSessionProgress.setVisibility(View.GONE);
        binding.updateSessionSuccess.setVisibility(View.GONE);
        binding.updateSessionError.setVisibility(View.GONE);

        binding.check3dsProgress.setVisibility(View.GONE);
        binding.check3dsSuccess.setVisibility(View.GONE);
        binding.check3dsError.setVisibility(View.GONE);

        binding.processPaymentProgress.setVisibility(View.GONE);
        binding.processPaymentSuccess.setVisibility(View.GONE);
        binding.processPaymentError.setVisibility(View.GONE);

        binding.startButton.setEnabled(true);
        binding.confirmButton.setEnabled(true);

        binding.startButton.setVisibility(View.VISIBLE);
        binding.groupConfirm.setVisibility(View.GONE);
        binding.groupResult.setVisibility(View.GONE);
    }

    void createSession() {
        setProcessTitle("Creating Session");
        binding.startButton.setEnabled(false);
        binding.createSessionProgress.setVisibility(View.VISIBLE);

        apiController.createSession(new CreateSessionCallback());
    }

    void collectCardInfo() {
        setProcessTitle("Collecting Card Info");
        binding.collectCardInfoProgress.setVisibility(View.VISIBLE);

        Intent i = new Intent(this, CollectCardInfoActivity.class);
        i.putExtra(CollectCardInfoActivity.EXTRA_GOOGLE_PAY_TXN_AMOUNT, AMOUNT);
        i.putExtra(CollectCardInfoActivity.EXTRA_GOOGLE_PAY_TXN_CURRENCY, CURRENCY);

        startActivityForResult(i, REQUEST_CARD_INFO);
    }

    void updateSession(String paymentToken) {
        setProcessTitle("Updating Session");
        binding.updateSessionProgress.setVisibility(View.VISIBLE);

        GatewayMap request = new GatewayMap().set("sourceOfFunds.provided.card.devicePayment.paymentToken", paymentToken);

        gateway.updateSession(sessionId, apiVersion, request, new UpdateSessionCallback());
    }

    void updateSession(String name, String number, String expiryMonth, String expiryYear, String cvv) {

        if (saveCard) {
            savedCardInfo = new SavedCardInfoModel();
            savedCardInfo.setSecurityCode(cvv);
            savedCardInfo.setNameOnCard(name);
            savedCardInfo.setExpiryDate(expiryMonth, expiryYear);
        }

        binding.updateSessionProgress.setVisibility(View.VISIBLE);
        setProcessTitle("Updating Session");

        // build the gateway request
        GatewayMap request = new GatewayMap().set("sourceOfFunds.provided.card.nameOnCard", name).set("sourceOfFunds.provided.card.number", number).set("sourceOfFunds.provided.card.securityCode", cvv).set("sourceOfFunds.provided.card.expiry.month", expiryMonth).set("sourceOfFunds.provided.card.expiry.year", expiryYear);

        gateway.updateSession(sessionId, apiVersion, request, new UpdateSessionCallback());
    }

    void check3dsEnrollment() {
        binding.check3dsProgress.setVisibility(View.VISIBLE);
        binding.confirmButton.setEnabled(false);
        setProcessTitle("Initiate Authentication");

        // generate a random 3DSecureId for testing
        String threeDSId = UUID.randomUUID().toString();
        threeDSTransactionId = threeDSId.substring(0, threeDSId.indexOf('-'));
        apiController.initiateAuthentication(sessionId, orderId, threeDSTransactionId, AMOUNT, CURRENCY, isGooglePay, new InitiateAuthenticationCallback());

    }

    void generateTokenRequest() {
        Log.d("savedCardToken", "inside  generate Token Request");
        apiController.generateToken(sessionId, new ApiController.GenerateTokenCallback() {
            @Override
            public void onSuccess(GatewayMap response) {
                if (savedCardInfo != null && response != null) {
                    savedCardInfo.setSavedCardToken((String) response.get("token"));
                    if (response.containsKey("sourceOfFunds") &&
                            response.containsKey("sourceOfFunds.provided") &&
                            response.containsKey("sourceOfFunds.provided.card")) {
                        savedCardInfo.setNumber((String) response.get("sourceOfFunds.provided.card.number"));
                        savedCardInfo.setScheme((String) response.get("sourceOfFunds.provided.card.scheme"));
                        savedCardInfo.setFundingMethod((String) response.get("sourceOfFunds.provided.card.fundingMethod"));
                        savedCardInfo.setBrand((String) response.get("sourceOfFunds.provided.card.brand"));
                        savedCardInfo.setExpiry((String) response.get("sourceOfFunds.provided.card.expiry"));
                    }
                    Log.d("savedCardToken", "inside savedCardToken date ");
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d("savedCardToken", "fail savedCardToken : " + throwable.getMessage());
            }
        });

    }

    void processAuthenticatePayer() {
        setProcessTitle("Process Authenticate Payer");
        binding.check3dsProgress.setVisibility(View.VISIBLE);
        binding.confirmButton.setEnabled(false);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            apiController.checkAuthenticatePayer(sessionId, orderId, threeDSTransactionId, AMOUNT, CURRENCY, new CheckAuthenticatePayerCallback());
        }, 10000);


    }

    void processPayment() {
        setProcessTitle("Process Payment");
        binding.processPaymentProgress.setVisibility(View.VISIBLE);

        apiController.completeSession(sessionId, orderId, transactionId, AMOUNT, CURRENCY, threeDSTransactionId, isGooglePay, new CompleteSessionCallback());
    }

    void showResult(@DrawableRes int iconId, @StringRes int messageId) {
        binding.resultIcon.setImageResource(iconId);
        binding.resultText.setText(messageId);

        binding.groupConfirm.setVisibility(View.GONE);
        binding.groupResult.setVisibility(View.VISIBLE);
        if (iconId == R.drawable.failed) {
            setFailResultCallBack(getString(messageId));
        }
        finish();
    }

    void showResult(@DrawableRes int iconId, String messageId) {
        binding.resultIcon.setImageResource(iconId);
        binding.resultText.setText(messageId);

        binding.groupConfirm.setVisibility(View.GONE);
        binding.groupResult.setVisibility(View.VISIBLE);
        if (iconId == R.drawable.failed) {
            setFailResultCallBack(messageId);
        }
        finish();
    }

    void setProcessTitle(String processTitle) {
        binding.tvProcessTitle.setText(processTitle);
    }


    class CreateSessionCallback implements ApiController.CreateSessionCallback {
        @Override
        public void onSuccess(String sessionId, String apiVersion) {
            Log.i("CreateSessionTask", "Session established");
            binding.createSessionProgress.setVisibility(View.GONE);
            binding.createSessionSuccess.setVisibility(View.VISIBLE);

            ProcessPaymentActivity.this.sessionId = sessionId;
            ProcessPaymentActivity.this.apiVersion = apiVersion;

            collectCardInfo();
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(ProcessPaymentActivity.class.getSimpleName(), throwable.getMessage(), throwable);

            binding.createSessionProgress.setVisibility(View.GONE);
            binding.createSessionError.setVisibility(View.VISIBLE);

            showResult(R.drawable.failed, R.string.pay_error_unable_to_create_session);
        }
    }

    class UpdateSessionCallback implements GatewayCallback {
        @Override
        public void onSuccess(GatewayMap response) {
            Log.i(ProcessPaymentActivity.class.getSimpleName(), "Successfully updated session");
            binding.updateSessionProgress.setVisibility(View.GONE);
            binding.updateSessionSuccess.setVisibility(View.VISIBLE);

            binding.startButton.setVisibility(View.GONE);
            binding.groupConfirm.setVisibility(View.VISIBLE);

            check3dsEnrollment();
            Log.d("saveCard", "saveCard : " + saveCard);
            if (saveCard) {
                generateTokenRequest();
            } else {
                savedCardInfo = null;
            }
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(ProcessPaymentActivity.class.getSimpleName(), throwable.getMessage(), throwable);

            binding.updateSessionProgress.setVisibility(View.GONE);
            binding.updateSessionError.setVisibility(View.VISIBLE);

            showResult(R.drawable.failed, R.string.pay_error_unable_to_update_session);
        }
    }

    class Check3DSecureEnrollmentCallback implements ApiController.Check3DSecureEnrollmentCallback {
        @Override
        public void onSuccess(GatewayMap response) {
            int apiVersionInt = Integer.valueOf(apiVersion);
            String threeDSecureId = (String) response.get("3DSecureId");

            String html = null;
            if (response.containsKey("3DSecure.authenticationRedirect.simple.htmlBodyContent")) {
                html = (String) response.get("3DSecure.authenticationRedirect.simple.htmlBodyContent");
            }

            // for API versions <= 46, you must use the summary status field to determine next steps for 3DS
            if (apiVersionInt <= 46) {
                String summaryStatus = (String) response.get("3DSecure.summaryStatus");

                if ("CARD_ENROLLED".equalsIgnoreCase(summaryStatus)) {
                    Gateway.start3DSecureActivity(ProcessPaymentActivity.this, html);
                    return;
                }

                binding.check3dsProgress.setVisibility(View.GONE);
                binding.check3dsSuccess.setVisibility(View.VISIBLE);
                ProcessPaymentActivity.this.threeDSecureId = null;

                // for these 2 cases, you still provide the 3DSecureId with the pay operation
                if ("CARD_NOT_ENROLLED".equalsIgnoreCase(summaryStatus) || "AUTHENTICATION_NOT_AVAILABLE".equalsIgnoreCase(summaryStatus)) {
                    ProcessPaymentActivity.this.threeDSecureId = threeDSecureId;
                }

                processPayment();
            }

            // for API versions >= 47, you must look to the gateway recommendation and the presence of 3DS info in the payload
            else {
                String gatewayRecommendation = (String) response.get("response.gatewayRecommendation");

                // if DO_NOT_PROCEED returned in recommendation, should stop transaction
                if ("DO_NOT_PROCEED".equalsIgnoreCase(gatewayRecommendation)) {
                    binding.check3dsProgress.setVisibility(View.GONE);
                    binding.check3dsError.setVisibility(View.VISIBLE);

                    showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed);
                    return;
                }

                // if PROCEED in recommendation, and we have HTML for 3ds, perform 3DS
                if (html != null) {
                    Gateway.start3DSecureActivity(ProcessPaymentActivity.this, html);
                    return;
                }

                ProcessPaymentActivity.this.threeDSecureId = threeDSecureId;

                processPayment();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(ProcessPaymentActivity.class.getSimpleName(), throwable.getMessage(), throwable);

            binding.check3dsProgress.setVisibility(View.GONE);
            binding.check3dsError.setVisibility(View.VISIBLE);

            showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed);
        }
    }


    class CheckAuthenticatePayerCallback implements ApiController.Check3DSecureEnrollmentCallback {
        @Override
        public void onSuccess(GatewayMap response) {
            if(!isOperationsCanceled){
                int apiVersionInt = Integer.valueOf(apiVersion);
                //String threeDSecureId = (String) response.get("3DSecureId");

                String html = null;
                if (response.containsKey("authentication.redirectHtml")) {
                    html = (String) response.get("authentication.redirectHtml");
                }

                // for API versions <= 46, you must use the summary status field to determine next steps for 3DS
                if (apiVersionInt <= 46) {
                    String summaryStatus = (String) response.get("3DSecure.summaryStatus");

                    if ("CARD_ENROLLED".equalsIgnoreCase(summaryStatus)) {
                        Gateway.start3DSecureActivity(ProcessPaymentActivity.this, html);
                        return;
                    }

                    binding.check3dsProgress.setVisibility(View.GONE);
                    binding.check3dsSuccess.setVisibility(View.VISIBLE);
                    ProcessPaymentActivity.this.threeDSecureId = null;

                    // for these 2 cases, you still provide the 3DSecureId with the pay operation
                    if ("CARD_NOT_ENROLLED".equalsIgnoreCase(summaryStatus) || "AUTHENTICATION_NOT_AVAILABLE".equalsIgnoreCase(summaryStatus)) {
                        ProcessPaymentActivity.this.threeDSecureId = threeDSecureId;
                    }

                    processPayment();
                }

                // for API versions >= 47, you must look to the gateway recommendation and the presence of 3DS info in the payload
                else {
                    String gatewayRecommendation = (String) response.get("response.gatewayRecommendation");

                    // if DO_NOT_PROCEED returned in recommendation, should stop transaction
                    if ("DO_NOT_PROCEED".equalsIgnoreCase(gatewayRecommendation)) {
                        binding.check3dsProgress.setVisibility(View.GONE);
                        binding.check3dsError.setVisibility(View.VISIBLE);

                        showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed);
                        return;
                    }

                    String transactionStatus = (String) response.get("transaction.authenticationStatus");
                    boolean isRequestNotCompleted = transactionStatus == null || !transactionStatus.equalsIgnoreCase("AUTHENTICATION_SUCCESSFUL");


                    // if PROCEED in recommendation, and we have HTML for 3ds, perform 3DS
                    if (html != null && isRequestNotCompleted) {
                        Gateway.start3DSecureActivity(ProcessPaymentActivity.this, html);
                        return;
                    }
                    binding.check3dsProgress.setVisibility(View.GONE);
                    binding.check3dsSuccess.setVisibility(View.VISIBLE);
                    ProcessPaymentActivity.this.threeDSecureId = threeDSecureId;

                    processPayment();
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            if(!isOperationsCanceled) {
                Log.e(ProcessPaymentActivity.class.getSimpleName(), throwable.getMessage(), throwable);

                binding.check3dsProgress.setVisibility(View.GONE);
                binding.check3dsError.setVisibility(View.VISIBLE);

                showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed);
            }
        }
    }

    class ThreeDSecureCallback implements Gateway3DSecureCallback {
        @Override
        public void on3DSecureCancel() {
            showError();
        }

        @Override
        public void on3DSecureComplete(GatewayMap result) {
            int apiVersionInt = Integer.valueOf(apiVersion);

            if (result != null && result.get("transaction.id") != null) {
                processAuthenticatePayer();
            }



           /* if (apiVersionInt <= 46) {
                if ("AUTHENTICATION_FAILED".equalsIgnoreCase((String) result.get("3DSecure.summaryStatus"))) {
                    showError();
                    return;
                }
            } else { // version >= 47
                if ("DO_NOT_PROCEED".equalsIgnoreCase((String) result.get("response.gatewayRecommendation"))) {
                    showError();
                    return;
                }
            }

            binding.check3dsProgress.setVisibility(View.GONE);
            binding.check3dsSuccess.setVisibility(View.VISIBLE);

            ProcessPaymentActivity.this.threeDSecureId = threeDSecureId;

            processPayment();*/

        }

        void showError() {
            binding.check3dsProgress.setVisibility(View.GONE);
            binding.check3dsError.setVisibility(View.VISIBLE);

            showResult(R.drawable.failed, R.string.pay_error_3ds_authentication_failed);
        }
    }

    class CompleteSessionCallback implements ApiController.CompleteSessionCallback {
        @Override
        public void onSuccess(String result) {
            binding.processPaymentProgress.setVisibility(View.GONE);
            binding.processPaymentSuccess.setVisibility(View.VISIBLE);
            setProcessTitle(getString(R.string.pay_you_payment_was_successful));
            showResult(R.drawable.success, R.string.pay_you_payment_was_successful);
            setSuccessResultCallBack(result);
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(ProcessPaymentActivity.class.getSimpleName(), throwable.getMessage(), throwable);

            binding.processPaymentProgress.setVisibility(View.GONE);
            binding.processPaymentError.setVisibility(View.VISIBLE);

            showResult(R.drawable.failed, R.string.pay_error_processing_your_payment);
        }
    }

    class InitiateAuthenticationCallback implements ApiController.InitiateAuthenticationCallback {
        @Override
        public void onSuccess(Boolean response) {
            Log.d(ProcessPaymentActivity.class.getSimpleName(), "initiateAuthentication is : " + response);

            if (response) {
                binding.confirmButton.setEnabled(true);
                binding.processPaymentProgress.setVisibility(View.GONE);
                processAuthenticatePayer();
                Toast.makeText(ProcessPaymentActivity.this, "Done initiateAuthentication ", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(ProcessPaymentActivity.class.getSimpleName(), "initiateAuthentication fail");

                binding.processPaymentProgress.setVisibility(View.GONE);
                binding.processPaymentError.setVisibility(View.VISIBLE);

                showResult(R.drawable.failed, R.string.pay_error_processing_your_payment);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(ProcessPaymentActivity.class.getSimpleName(), throwable.getMessage(), throwable);

            binding.processPaymentProgress.setVisibility(View.GONE);
            binding.processPaymentError.setVisibility(View.VISIBLE);

            showResult(R.drawable.failed, R.string.pay_error_processing_your_payment);
        }
    }

    private void setSuccessResultCallBack(Object response) {
        CallbackResultModel callbackResultModel = new CallbackResultModel();
        callbackResultModel.setStatus(true);
        callbackResultModel.setResult(response);
        callbackResultModel.setSavedCardInfo(savedCardInfo);
        if (SDKConfigurations.magintsNBECallback != null) {
            SDKConfigurations.magintsNBECallback.onResult(callbackResultModel);
        }
    }

    private void setFailResultCallBack(String errorMsg) {
        CallbackResultModel callbackResultModel = new CallbackResultModel();
        callbackResultModel.setStatus(false);
        callbackResultModel.setFailMessage(errorMsg);
        if (SDKConfigurations.magintsNBECallback != null) {
            SDKConfigurations.magintsNBECallback.onResult(callbackResultModel);
        }
    }

    @Override
    public void onBackPressed() {
        isOperationsCanceled=true;
        super.onBackPressed();
    }
}
