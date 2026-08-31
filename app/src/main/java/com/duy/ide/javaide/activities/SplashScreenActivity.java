/*
 * Copyright (C) 2018 Tran Le Duy
 */

package com.duy.ide.javaide.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.duy.android.compiler.env.Environment;
import com.duy.ide.R;
import com.duy.ide.javaide.JavaIdeActivity;

/**
 * Launch gate: permissions + optional SDK install, then main IDE.
 * AOSP SdkManager harness removed with the APK-builder cut.
 */
public class SplashScreenActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 11;
    private static final int REQUEST_INSTALL_SYSTEM = 12;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        if (!permissionGranted()) {
            requestPermissions();
        } else if (systemInstalled()) {
            startMainActivity();
        } else {
            installSystem();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },
                MY_PERMISSIONS_REQUEST);
    }

    private boolean permissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void installSystem() {
        Intent intent = new Intent(this, InstallActivity.class);
        startActivityForResult(intent, REQUEST_INSTALL_SYSTEM);
    }

    private boolean systemInstalled() {
        return Environment.isSdkInstalled(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INSTALL_SYSTEM) {
            if (resultCode == RESULT_OK) {
                startMainActivity();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults.length >= 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (systemInstalled()) {
                    startMainActivity();
                } else {
                    installSystem();
                }
            } else {
                Toast.makeText(this, R.string.permission_denied_storage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startMainActivity() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, JavaIdeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            overridePendingTransition(0, 0);
            startActivity(intent);
            finish();
        }, 400);
    }
}
