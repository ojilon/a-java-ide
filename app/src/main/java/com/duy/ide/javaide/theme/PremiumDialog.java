/*
 * Copyright (C) 2018 Tran Le Duy
 */

package com.duy.ide.javaide.theme;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.duy.common.purchase.InAppPurchaseHelper;
import com.duy.ide.R;
import com.jecelyin.editor.v2.dialog.AbstractDialog;

/** Informational only — billing removed; app is always premium. */
public class PremiumDialog extends AbstractDialog {
    private final InAppPurchaseHelper mPurchaseHelper;

    public PremiumDialog(Context context, InAppPurchaseHelper purchaseHelper) {
        super(context);
        this.mPurchaseHelper = purchaseHelper;
    }

    @Override
    public void show() {
        AlertDialog.Builder builder = getBuilder();
        builder.setTitle(R.string.title_premium_version);
        builder.setMessage(R.string.message_premium);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.create().show();
    }
}
