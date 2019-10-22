package com.redeban.payment;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kount.api.DataCollector;
import com.redeban.payment.model.Card;
import com.redeban.payment.rest.RedebanClient;
import com.redeban.payment.rest.RedebanService;
import com.redeban.payment.rest.TokenCallback;
import com.redeban.payment.rest.model.CardBinResponse;
import com.redeban.payment.rest.model.CreateTokenRequest;
import com.redeban.payment.rest.model.CreateTokenResponse;
import com.redeban.payment.rest.model.ErrorResponse;
import com.redeban.payment.rest.model.RedebanError;
import com.redeban.payment.rest.model.User;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Payment {
    private static boolean TEST_MODE;
    private static String REDEBAN_CLIENT_APP_CODE;
    private static String REDEBAN_CLIENT_APP_KEY;

    static int MERCHANT_ID = 500005;
    static int KOUNT_ENVIRONMENT = DataCollector.ENVIRONMENT_TEST;

    static RedebanService redebanService;

    /**
     * Init library
     *
     * @param test_mode               false to use production environment
     * @param redeban_client_app_code provided by Redeban.
     * @param redeban_client_app_key  provided by Redeban.
     */
    public static void setEnvironment(boolean test_mode, String redeban_client_app_code, String redeban_client_app_key) {
        TEST_MODE = test_mode;
        REDEBAN_CLIENT_APP_CODE = redeban_client_app_code;
        REDEBAN_CLIENT_APP_KEY = redeban_client_app_key;
        if (TEST_MODE) {
            KOUNT_ENVIRONMENT = DataCollector.ENVIRONMENT_TEST;

        } else {
            KOUNT_ENVIRONMENT = DataCollector.ENVIRONMENT_PRODUCTION;

        }
    }

    /**
     * Set your Risk Merchant ID
     *
     * @param merchant_id Insert your valid merchant ID
     */
    public static void setRiskMerchantId(int merchant_id) {
        MERCHANT_ID = merchant_id;
    }

    public static RedebanService getRedebanService(Context mContext) {
        redebanService = RedebanClient.getClient(mContext, TEST_MODE, REDEBAN_CLIENT_APP_CODE, REDEBAN_CLIENT_APP_KEY).create(RedebanService.class);

        return redebanService;
    }

    public static void getImageBin(Context mContext, String bin) {
        redebanService = RedebanClient.getClient(mContext, TEST_MODE, REDEBAN_CLIENT_APP_CODE, REDEBAN_CLIENT_APP_KEY).create(RedebanService.class);
        redebanService.cardBin(bin).enqueue(new Callback<CardBinResponse>() {
            @Override
            public void onResponse(Call<CardBinResponse> call, Response<CardBinResponse> response) {
                CardBinResponse cardBinResponse = response.body();
                if (response.isSuccessful()) {

                } else {

                }
            }

            @Override
            public void onFailure(Call<CardBinResponse> call, Throwable e) {

            }
        });
    }

    /**
     * The simplest way to create a token, using a {@link Card} and {@link TokenCallback}. T
     *
     * @param mContext Context of the Main Activity
     * @param uid      User identifier. This is the identifier you use inside your application; you will receive it in notifications.
     * @param email    Email of the user initiating the purchase. Format: Valid e-mail format.
     * @param card     the {@link Card} used to create this redeban token
     * @param callback a {@link TokenCallback} to receive either the token or an error
     */
    public static void addCard(Context mContext, @NonNull final String uid, @NonNull final String email, @NonNull final Card card, @NonNull final TokenCallback callback) {

        redebanService = RedebanClient.getClient(mContext, TEST_MODE, REDEBAN_CLIENT_APP_CODE, REDEBAN_CLIENT_APP_KEY).create(RedebanService.class);
        User user = new User();
        user.setId(uid);
        user.setEmail(email);
        user.setFiscal_number(card.getFiscal_number());

        CreateTokenRequest createTokenRequest = new CreateTokenRequest();
        createTokenRequest.setSessionId(getSessionId(mContext));
        createTokenRequest.setCard(card);
        createTokenRequest.setUser(user);

        redebanService.createToken(createTokenRequest).enqueue(new Callback<CreateTokenResponse>() {
            @Override
            public void onResponse(Call<CreateTokenResponse> call, Response<CreateTokenResponse> response) {
                CreateTokenResponse createTokenResponse = response.body();
                if (response.isSuccessful()) {
                    callback.onSuccess(createTokenResponse.getCard());
                    return;
                } else {
                    RedebanError error
                            = new RedebanError("Exception", "", "General Error");
                    try {
                        Gson gson = new GsonBuilder().create();
                        ErrorResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                        callback.onError(errorResponse.getError());
                        return;
                    } catch (Exception e) {
                        try {
                            error = new RedebanError("Exception", "Http Code: " + response.code(), response.message());
                        } catch (Exception e2) {
                        }
                    }
                    callback.onError(error);
                    return;

                }
            }

            @Override
            public void onFailure(Call<CreateTokenResponse> call, Throwable e) {
                RedebanError error
                        = new RedebanError("Network Exception",
                        "Invoked when a network exception occurred communicating to the server.", e.getLocalizedMessage());
                callback.onError(error);
                return;
            }
        });
    }


    /**
     * The session ID is a parameter Redeban use for fraud purposes.
     *
     * @return session_id
     */
    public static String getSessionId(Context mContext) {
        String sessionID = UUID.randomUUID().toString();
        final String deviceSessionID = sessionID.replace("-", "");


        // Configure the collector
        final DataCollector dataCollector = com.kount.api.DataCollector.getInstance();
        if (TEST_MODE)
            dataCollector.setDebug(true);
        else
            dataCollector.setDebug(false);
        dataCollector.setContext(mContext);
        dataCollector.setMerchantID(MERCHANT_ID);
        dataCollector.setEnvironment(KOUNT_ENVIRONMENT);
        dataCollector.setLocationCollectorConfig(DataCollector.LocationConfig.COLLECT);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                dataCollector.collectForSession(deviceSessionID, new com.kount.api.DataCollector.CompletionHandler() {
                    @Override
                    public void completed(String s) {

                    }

                    @Override
                    public void failed(String s, final DataCollector.Error error) {

                    }

                });
            }
        });

        return deviceSessionID;
    }
}
