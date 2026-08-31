/*
 * Copyright (C) 2018 Tran Le Duy
 */

package com.duy.ide.javaide.activities;

import android.content.SharedPreferences;

import androidx.annotation.StyleRes;
import androidx.appcompat.widget.Toolbar;

import com.duy.ide.R;
import com.jecelyin.editor.v2.ThemeSupportActivity;

public class BaseActivity extends ThemeSupportActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @StyleRes
    @Override
    public int getThemeId() {
        return R.style.AppThemeDark;
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }
}
