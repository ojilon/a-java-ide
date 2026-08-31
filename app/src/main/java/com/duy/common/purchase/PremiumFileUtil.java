/*
 * Copyright (C) 2018 Tran Le Duy
 */

package com.duy.common.purchase;

import android.content.Context;

import androidx.annotation.NonNull;

/** License file helpers retained as no-ops (premium is always on). */
@SuppressWarnings("unused")
final class PremiumFileUtil {

    private PremiumFileUtil() {}

    static void saveLicence(@NonNull Context context) {
        // no-op
    }

    static boolean licenseCached(@NonNull Context context) {
        return true;
    }

    static void clearLicence(Context context) {
        // no-op
    }
}
