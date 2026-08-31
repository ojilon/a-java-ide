/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.duy.common.purchase;

import android.content.Context;

/**
 * Phone-first build: treat every install as premium.
 * Play Billing / license files are no longer used.
 */
public final class Premium {

    private Premium() {}

    /** Always unlocked for local / personal builds. */
    public static boolean isPremiumUser(Context context) {
        return true;
    }

    /** No-op — premium is always on. */
    public static void setPremiumUser(Context context, boolean isPremium) {
        // intentionally empty
    }
}
