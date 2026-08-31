/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.duy.common.purchase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * No-op billing helper. Play Billing / anjlab were removed for the phone-first cut.
 * Call sites can keep constructing this type without crashing.
 */
public class InAppPurchaseHelper {

    public interface PurchaseCallback {
        void updateUI(boolean premium);
    }

    public InAppPurchaseHelper(@NonNull AppCompatActivity activity) {
        // no billing processor
    }

    public void purchase(String sku) {
        // no-op
    }

    public void upgradePremium() {
        // no-op — already premium via Premium.isPremiumUser
    }

    public void restorePurchase() {
        // no-op
    }

    public void destroy() {
        // no-op
    }
}
