package com.redeban.payment.rest;

import com.redeban.payment.model.Card;
import com.redeban.payment.rest.model.CreateTokenResponse;
import com.redeban.payment.rest.model.RedebanError;

/**
 * An interface representing a callback to be notified about the results of
 * {@link CreateTokenResponse} creation or requests
 */
public interface TokenCallback {

    /**
     * RedebanError callback method.
     * @param error the error that occurred.
     */
    void onError(RedebanError error);

    /**
     * Success callback method.
     * @param card the {@link Card} that was found or created.
     */
    void onSuccess(Card card);
}