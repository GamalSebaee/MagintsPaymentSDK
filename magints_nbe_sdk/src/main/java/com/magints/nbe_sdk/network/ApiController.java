package com.magints.nbe_sdk.network;

import static android.text.TextUtils.isEmpty;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.magints.nbe_sdk.utils.SDKConfigurations;
import com.mastercard.gateway.android.sdk.GatewayMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

/**
 * ApiController object used to send create and update session requests. Conforms to the singleton
 * pattern.
 * <p>
 * NOTE: This code is sample code only and is not intended to be used for production applications.  Any use in production applications is at your own risk.
 */
public class ApiController {

    private static final ApiController INSTANCE = new ApiController();

    static final Gson GSON = new GsonBuilder().create();

    String merchantServerUrl;

    static final String USER_AGENT = "Gateway-Android-SDK/1.1.5";

    public interface CreateSessionCallback {
        void onSuccess(String sessionId, String apiVersion);

        void onError(Throwable throwable);
    }

    public interface Check3DSecureEnrollmentCallback {
        void onSuccess(GatewayMap response);

        void onError(Throwable throwable);
    }

    public interface InitiateAuthenticationCallback {
        void onSuccess(Boolean response);

        void onError(Throwable throwable);
    }

    public interface CompleteSessionCallback {
        void onSuccess(String result);

        void onError(Throwable throwable);
    }
    public interface GenerateTokenCallback {
        void onSuccess(GatewayMap cardToken);

        void onError(Throwable throwable);
    }

    private ApiController() {
    }

    public static ApiController getInstance() {
        return INSTANCE;
    }

    public void setMerchantServerUrl(String url) {
        merchantServerUrl = url;
        Log.d("merchantServerUrl", "merchantServerUrl : " + merchantServerUrl);
    }

    public void createSession(final CreateSessionCallback callback) {
        final Handler handler = new Handler(message -> {
            if (callback != null) {
                if (message.obj instanceof Throwable) {
                    callback.onError((Throwable) message.obj);
                } else {
                    Pair<String, String> pair = (Pair<String, String>) message.obj;
                    callback.onSuccess(pair.first, pair.second);
                }
            }
            return true;
        });

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeCreateSession();
            } catch (Exception e) {
                m.obj = e;
            }
            handler.sendMessage(m);
        }).start();
    }

    public void check3DSecureEnrollment(final String sessionId, final String amount, final String currency, final String threeDSecureId, final Check3DSecureEnrollmentCallback callback) {
        final Handler handler = new Handler(message -> {
            if (callback != null) {
                if (message.obj instanceof Throwable) {
                    callback.onError((Throwable) message.obj);
                } else {
                    callback.onSuccess((GatewayMap) message.obj);
                }
            }
            return true;
        });

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeCheck3DSEnrollment(sessionId, amount, currency, threeDSecureId);
            } catch (Exception e) {
                m.obj = e;
            }
            handler.sendMessage(m);
        }).start();
    }

    public void initiateAuthentication(final String sessionId, final String orderId, final String transactionId, final String amount, final String currency, final Boolean isGooglePay, final InitiateAuthenticationCallback callback) {

        final Handler handler = new Handler(message -> {
            if (callback != null) {
                if (message.obj instanceof Throwable) {
                    callback.onError((Throwable) message.obj);
                } else {
                    callback.onSuccess((Boolean) message.obj);
                }
            }
            return true;
        });

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeInitiateAuthentication(sessionId, orderId, transactionId, amount, currency, isGooglePay);
            } catch (Exception e) {
                m.obj = e;
            }
            handler.sendMessage(m);
        }).start();
    }

    public void checkAuthenticatePayer(final String sessionId, final String orderId, final String transactionId, final String amount, final String currency, final Check3DSecureEnrollmentCallback callback) {
        final Handler handler = new Handler(message -> {
            if (callback != null) {
                if (message.obj instanceof Throwable) {
                    callback.onError((Throwable) message.obj);
                } else {
                    callback.onSuccess((GatewayMap) message.obj);
                }
            }
            return true;
        });

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeAuthenticatePayer(sessionId, orderId, transactionId, amount, currency);
            } catch (Exception e) {
                m.obj = e;
            }
            handler.sendMessage(m);
        }).start();
    }

    public void completeSession(final String sessionId, final String orderId, final String transactionId, final String amount, final String currency, final String threeDSecureId, final Boolean isGooglePay, final CompleteSessionCallback callback) {
        final Handler handler = new Handler(message -> {
            if (callback != null) {
                if (message.obj instanceof Throwable) {
                    callback.onError((Throwable) message.obj);
                } else {
                    callback.onSuccess((String) message.obj);
                }
            }
            return true;
        });

        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeCompleteSession(sessionId, orderId, transactionId, amount, currency, threeDSecureId, isGooglePay);
            } catch (Exception e) {
                m.obj = e;
            }
            handler.sendMessage(m);
        }).start();
    }
    public void generateToken(final String sessionId, final GenerateTokenCallback callback) {
        final Handler handler = new Handler(message -> {
            if (callback != null) {
                if (message.obj instanceof Throwable) {
                    callback.onError((Throwable) message.obj);
                } else {
                    callback.onSuccess((GatewayMap) message.obj);
                }
            }
            return true;
        });
        new Thread(() -> {
            Message m = handler.obtainMessage();
            try {
                m.obj = executeGenerateToken(sessionId);
            } catch (Exception e) {
                m.obj = e;
            }
            handler.sendMessage(m);
        }).start();
    }

    Pair<String, String> executeCreateSession() throws Exception {

        String requestUrl = SDKConfigurations.requestUrl + "/session";
        Log.d("merchantServerUrl", "requestUrl : " + requestUrl);
        String jsonResponse = doJsonRequest(new URL(requestUrl), "", "POST", SDKConfigurations.requestUsername, SDKConfigurations.requestUserPassword, HttpsURLConnection.HTTP_OK, HttpsURLConnection.HTTP_CREATED);

        GatewayMap response = new GatewayMap(jsonResponse);

        if (!response.containsKey("session")) {
            throw new RuntimeException("Could not read gateway response");
        }

        if (!response.containsKey("result") || !"SUCCESS".equalsIgnoreCase((String) response.get("result"))) {
            throw new RuntimeException("Create session result: " + response.get("result"));
        }

        //  String apiVersion = (String) response.get("version");
        String apiVersion = "" + SDKConfigurations.getApiVersion();
        String sessionId = (String) response.get("session.id");
/*        String apiVersion = "61";
        String sessionId = "SESSION0002327725356E4964924H12";*/
        Log.i("createSession", "Created session with ID " + sessionId + " with API version " + apiVersion);

        return new Pair<>(sessionId, apiVersion);
    }

    GatewayMap executeCheck3DSEnrollment(String sessionId, String amount, String currency, String threeDSecureId) throws Exception {

        String requestUrl = SDKConfigurations.requestUrl + "/3DSecureId/" + threeDSecureId;
        GatewayMap request = new GatewayMap().set("apiOperation", "CHECK_3DS_ENROLLMENT").set("session.id", sessionId).set("order.amount", amount).set("order.currency", currency)
                //.set("3DSecure.authenticationRedirect.responseUrl", "https://eu.gateway.mastercard.com/3DSecureResult?3DSecureId=" + threeDSecureId);
                .set("3DSecure.authenticationRedirect.responseUrl", "https://magints.com/NBECallBack.php?3DSecureId=" + threeDSecureId);


        //   .set("3DSecure.authenticationRedirect.responseUrl", "https://www.google.com");
        // .set("3DSecure.authenticationRedirect.responseUrl", merchantServerUrl + "/3DSecureResult.php?3DSecureId=" + threeDSecureId);
        // .set("3DSecure.authenticationRedirect.responseUrl", requestUrl);

        String jsonRequest = GSON.toJson(request);
        Log.d("requestUrl", "requestUrl : " + requestUrl);
        Log.d("requestUrlParameters", "jsonRequest : " + jsonRequest);
        String jsonResponse = doJsonRequest(new URL(requestUrl), jsonRequest, "PUT", SDKConfigurations.requestUsername, SDKConfigurations.requestUserPassword, HttpsURLConnection.HTTP_OK, HttpsURLConnection.HTTP_CREATED);

        GatewayMap response = new GatewayMap(jsonResponse);

        if (!response.containsKey("3DSecure")) {
            throw new RuntimeException("Could not read gateway response");
        }

        // if there is an error result, throw it
        if (response.containsKey("result") && "ERROR".equalsIgnoreCase((String) response.get("result"))) {
            throw new RuntimeException("Check 3DS Enrollment Error: " + response.get("error.explanation"));
        }

        return response;
    }


    Boolean executeInitiateAuthentication(String sessionId, String orderId, String transactionId, String amount, String currency, Boolean isGooglePay) throws Exception {
        String redirectResponseUrl = "https://magints.com/3DSecureResult.php?orderId=" + orderId + "&transactionId=" + transactionId + "&session=" + sessionId + "&amount=" + amount + "&currency=" + currency;

        Log.d("redirectResponseUrl", "" + redirectResponseUrl);

        GatewayMap request = new GatewayMap()
                .set("apiOperation", "INITIATE_AUTHENTICATION")
                .set("session.id", sessionId)
                .set("authentication.acceptVersions", "3DS1,3DS2")
                .set("authentication.channel", "PAYER_BROWSER")
                .set("authentication.purpose", "PAYMENT_TRANSACTION")
                //  .set("order.amount", amount)
                .set("order.currency", currency);

        String jsonRequest = GSON.toJson(request);

        String requestUrl = SDKConfigurations.requestUrl + "/order/" + orderId + "/transaction/" + transactionId;

        Log.d("requestUrl", "requestUrl : " + requestUrl);
        Log.d("requestUrlParameter", "jsonRequest : " + jsonRequest);
        String jsonResponse = doJsonRequest(new URL(requestUrl), jsonRequest, "PUT", SDKConfigurations.requestUsername, SDKConfigurations.requestUserPassword, HttpsURLConnection.HTTP_OK, HttpsURLConnection.HTTP_CREATED);

        Log.d("InitiateAuthentication", "InitiateAuthentication : " + jsonResponse);
        GatewayMap response = new GatewayMap(jsonResponse);


        if (!response.containsKey("authentication")) {
            throw new RuntimeException("Could not read gateway response");
        }

        if (!response.containsKey("result") || !"SUCCESS".equalsIgnoreCase((String) response.get("result"))) {
            throw new RuntimeException("Error processing payment");
        }
        Log.d("InitiateAuthentication", "InitiateAuthentication is : " + response.get("authentication.version"));
        return (response.containsKey("authentication.version") && ("3DS2".equalsIgnoreCase((String) response.get("authentication.version")) || "3DS1".equalsIgnoreCase((String) response.get("authentication.version"))));
    }

    GatewayMap executeAuthenticatePayer(String sessionId, String orderId, String transactionId, String amount, String currency) throws Exception {
        GatewayMap deviceMap = new GatewayMap()
                .set("browser", USER_AGENT)
                .set("browserDetails.javaEnabled", "true")
                .set("browserDetails.language", "json")
                .set("browserDetails.screenHeight", "500")
                .set("browserDetails.screenWidth", "500")
                .set("browserDetails.timeZone", "+200")
                .set("browserDetails.colorDepth", "20")
                .set("browserDetails.acceptHeaders", "512")
                .set("browserDetails.3DSecureChallengeWindowSize", "FULL_SCREEN");


        String redirectResponseUrl = "https://magints.com/3DSecureResult.php?3DSecureId=" + transactionId;

        GatewayMap request = new GatewayMap()
                .set("apiOperation", "AUTHENTICATE_PAYER")
                .set("session.id", sessionId)
                .set("authentication.redirectResponseUrl", "" + redirectResponseUrl)
                .set("order.amount", amount)
                .set("order.currency", currency)
                .set("device", deviceMap)
              /*  .set("device.browser","Chrome")
                .set("device.browserDetails.javaEnabled", "true")
                .set("device.browserDetails.language", "json")
                .set("device.browserDetails.screenHeight", "500")
                .set("device.browserDetails.screenWidth", "500")
                .set("device.browserDetails.timeZone", "+200")
                .set("device.browserDetails.colorDepth", "20")
                .set("device.browserDetails.acceptHeaders", "512")
                .set("device.browserDetails.3DSecureChallengeWindowSize", "FULL_SCREEN")*/;

        String jsonRequest = GSON.toJson(request);

        String requestUrl = SDKConfigurations.requestUrl + "/order/" + orderId + "/transaction/" + transactionId;
        //String requestUrl = "https://test-nbe.gateway.mastercard.com/api/rest/version/61/merchant/TESTEGPTEST/order/"+orderId+"/transaction/"+transactionId;

        Log.d("requestUrl", "requestUrl : " + requestUrl);
        Log.d("requestUrlParameter", "jsonRequest : " + jsonRequest);
        String jsonResponse = doJsonRequest(new URL(requestUrl), jsonRequest, "PUT",
                SDKConfigurations.requestUsername, SDKConfigurations.requestUserPassword,
                HttpsURLConnection.HTTP_OK, HttpsURLConnection.HTTP_CREATED);

        Log.d("jsonResponse", "jsonResponse : " + jsonResponse);

        GatewayMap response = new GatewayMap(jsonResponse);


        if (!response.containsKey("authentication")) {
            throw new RuntimeException("Could not read gateway response");
        }

       /* if (!response.containsKey("result") || !"SUCCESS".equalsIgnoreCase((String) response.get("result"))) {
            throw new RuntimeException("Error processing payment");
        }*/

        // if there is an error result, throw it
        if (response.containsKey("result") && "ERROR".equalsIgnoreCase((String) response.get("result"))) {
            throw new RuntimeException("execute Authenticate Payer Error: " + response.get("error.explanation"));
        }

        return response;
    }


    String executeCompleteSession(String sessionId, String orderId, String transactionId, String amount, String currency, String threeDSecureId, Boolean isGooglePay) throws Exception {
        GatewayMap request = new GatewayMap()
                .set("apiOperation", "PAY")
                .set("session.id", sessionId)
                .set("order.amount", amount)
                .set("order.currency", currency)
                .set("sourceOfFunds.type", "CARD")
                .set("authentication.transactionId", threeDSecureId);
        // .set("transaction.frequency", "SINGLE") // NOTE: 'transaction.frequency` is only applicable to API versions <=53
        // .set("transaction.source", "INTERNET");


        if (isGooglePay) {
            request.put("order.walletProvider", "GOOGLE_PAY");
        }

        String jsonRequest = GSON.toJson(request);

        String requestUrl = SDKConfigurations.requestUrl + "/order/" + orderId + "/transaction/" + transactionId;

        Log.d("requestUrl", "requestUrl : " + requestUrl);
        Log.d("requestUrlParameter", "jsonRequest : " + jsonRequest);
        String jsonResponse = doJsonRequest(new URL(requestUrl), jsonRequest, "PUT", SDKConfigurations.requestUsername, SDKConfigurations.requestUserPassword, HttpsURLConnection.HTTP_OK, HttpsURLConnection.HTTP_CREATED);

        GatewayMap response = new GatewayMap(jsonResponse);


      /*  if (!response.containsKey("response")) {
            throw new RuntimeException("Could not read gateway response");
        }*/

        if (!response.containsKey("result") || !"SUCCESS".equalsIgnoreCase((String) response.get("result"))) {
            throw new RuntimeException("Error processing payment");
        }
        try {
            String responseResult = new Gson().toJson(response);
            /*   Log.d("executeCompleteSession", "response : " + responseResult);*/
            if (responseResult != null) {
                return responseResult;
            } else {
                return (String) response.get("result");
            }
        } catch (Exception ex) {
            return (String) response.get("result");
        }
    }

    GatewayMap executeGenerateToken(String sessionId) throws Exception {
        GatewayMap request = new GatewayMap()
                .set("session.id", sessionId)
                .set("sourceOfFunds.type", "CARD");

        String jsonRequest = GSON.toJson(request);

        String requestUrl = SDKConfigurations.requestUrl + "/token";

        Log.d("requestUrl", "requestUrl : " + requestUrl);
        Log.d("requestUrlParameter", "jsonRequest : " + jsonRequest);
        String jsonResponse = doJsonRequest(new URL(requestUrl), jsonRequest, "POST", SDKConfigurations.requestUsername, SDKConfigurations.requestUserPassword, HttpsURLConnection.HTTP_OK, HttpsURLConnection.HTTP_CREATED);

        GatewayMap response = new GatewayMap(jsonResponse);


        if (!response.containsKey("result") || !"SUCCESS".equalsIgnoreCase((String) response.get("result"))) {
            throw new RuntimeException("Error processing payment");
        }

        return response;
    }


    /**
     * Initialise a new SSL context using the algorithm, key manager(s), trust manager(s) and
     * source of randomness.
     *
     * @throws NoSuchAlgorithmException if the algorithm is not supported by the android platform
     * @throws KeyManagementException   if initialization of the context fails
     */
    void initialiseSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
    }

    /**
     * Open an HTTP or HTTPS connection to a particular URL
     *
     * @param address a valid HTTP[S] URL to connect to
     * @return an HTTP or HTTPS connection as appropriate
     * @throws KeyManagementException   if initialization of the SSL context fails
     * @throws NoSuchAlgorithmException if the SSL algorithm is not supported by the android platform
     * @throws MalformedURLException    if the address was not in the HTTP or HTTPS scheme
     * @throws IOException              if the connection could not be opened
     */
    HttpURLConnection openConnection(URL address) throws KeyManagementException, NoSuchAlgorithmException, IOException {

        switch (address.getProtocol().toUpperCase()) {
            case "HTTPS":
                initialiseSslContext();
                break;
            case "HTTP":
                break;
            default:
                throw new MalformedURLException("Not an HTTP[S] address");
        }

        HttpURLConnection connection = (HttpURLConnection) address.openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);
        return connection;
    }

    /**
     * Send a JSON object to an open HTTP[S] connection
     *
     * @param connection an open HTTP[S] connection, as returned by {@link #openConnection(URL)}
     * @param method     an HTTP method, e.g. PUT, POST or GET
     * @param json       a valid JSON-formatted object
     * @param username   user name for basic authorization (can be null for no auth)
     * @param password   password for basic authorization (can be null for no auth)
     * @return an HTTP response code
     * @throws IOException if the connection could not be written to
     */
    int makeJsonRequest(HttpURLConnection connection, String method, String json, String username, String password) throws IOException {
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setDoOutput(true);
        connection.setRequestMethod(method);
        connection.setFixedLengthStreamingMode(json.getBytes().length);
        connection.setRequestProperty("User-Agent",USER_AGENT);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(json.getBytes().length));
        if (!isEmpty(username) && !isEmpty(password)) {
            String basicAuth = username + ':' + password;
            basicAuth = Base64.encodeToString(basicAuth.getBytes(), Base64.DEFAULT);
            connection.setRequestProperty("Authorization", "Basic " + basicAuth);
        }

        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print(json);
        out.close();

        return connection.getResponseCode();
    }

    /**
     * Retrieve a JSON response from an open HTTP[S] connection. This would typically be called
     * after {@link #makeJsonRequest(HttpURLConnection, String, String, String, String)}
     *
     * @param connection an open HTTP[S] connection
     * @return a json object in string form
     * @throws IOException if the connection could not be read from
     */
    String getJsonResponse(HttpURLConnection connection) throws IOException {
        StringBuilder responseOutput = new StringBuilder();
        String line;
        BufferedReader br = null;

        try {
            // If the HTTP response code is 4xx or 5xx, we need error rather than input stream
            InputStream stream = (connection.getResponseCode() < 400) ? connection.getInputStream() : connection.getErrorStream();

            br = new BufferedReader(new InputStreamReader(stream));

            while ((line = br.readLine()) != null) {
                responseOutput.append(line);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    /* Ignore close exception */
                }
            }
        }

        return responseOutput.toString();
    }

    /**
     * End-to-end method to send some json to an url and retrieve a response
     *
     * @param address             url to send the request to
     * @param jsonRequest         a valid JSON-formatted object
     * @param httpMethod          an HTTP method, e.g. PUT, POST or GET
     * @param username            user name for basic authorization (can be null for no auth)
     * @param password            password for basic authorization (can be null for no auth)
     * @param expectResponseCodes permitted HTTP response codes, e.g. HTTP_OK (200)
     * @return a json response object in string form
     */
    String doJsonRequest(URL address, String jsonRequest, String httpMethod, String username, String password, int... expectResponseCodes) {

        HttpURLConnection connection;
        int responseCode;

        try {
            connection = openConnection(address);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Couldn't initialise SSL context", e);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't open an HTTP[S] connection", e);
        }

        try {
            responseCode = makeJsonRequest(connection, httpMethod, jsonRequest, username, password);

            if (!contains(expectResponseCodes, responseCode)) {
                String responseBody = getJsonResponse(connection);
                throw new RuntimeException("Unexpected response code " + responseCode + " responseBody : " + responseBody);
            }
        } catch (SocketTimeoutException e) {
            throw new RuntimeException("Timeout whilst sending JSON data");
        } catch (IOException e) {
            throw new RuntimeException("Error sending JSON data", e);
        }

        try {
            String responseBody = getJsonResponse(connection);

            if (responseBody == null) {
                throw new RuntimeException("No data in response");
            }

            return responseBody;
        } catch (SocketTimeoutException e) {
            throw new RuntimeException("Timeout whilst retrieving JSON response");
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving JSON response", e);
        }
    }


    static boolean contains(int[] haystack, int needle) {
        for (int candidate : haystack) {
            if (candidate == needle) {
                return true;
            }
        }

        return false;
    }
}

