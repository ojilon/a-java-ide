/*
 * Copyright (C) 2018 Tran Le Duy
 */

package com.duy.ide.javaide;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.duy.ide.javaide.setting.IdePreferenceManager;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Application entry — no Crashlytics / Firebase.
 */
public class JavaApplication extends MultiDexApplication {
    private final ArrayList<PrintStream> out = new ArrayList<>();
    private final ArrayList<PrintStream> err = new ArrayList<>();

    private InterceptorOutputStream systemOut;
    private InterceptorOutputStream systemErr;

    @Override
    public void onCreate() {
        super.onCreate();
        systemOut = new InterceptorOutputStream(System.out, out);
        systemErr = new InterceptorOutputStream(System.err, err);
        System.setOut(systemOut);
        System.setErr(systemErr);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        IdePreferenceManager.setDefaultValues(this);
    }

    public void addStdOut(PrintStream out) {
        systemOut.add(out);
    }

    public void addStdErr(PrintStream err) {
        systemErr.add(err);
    }

    public void removeOutStream(PrintStream out) {
        systemOut.remove(out);
    }

    public void removeErrStream(PrintStream err) {
        systemErr.remove(err);
    }

    private static class InterceptorOutputStream extends PrintStream {

        private static final String TAG = "InterceptorOutputStream";
        private ArrayList<PrintStream> streams;

        InterceptorOutputStream(@NonNull OutputStream file, ArrayList<PrintStream> streams) {
            super(file, true);
            this.streams = streams;
        }

        void add(PrintStream out) {
            Log.d(TAG, "add() called with: out = [" + out + "]");
            this.streams.add(out);
        }

        void remove(PrintStream out) {
            Log.d(TAG, "remove() called with: out = [" + out + "]");
            this.streams.remove(out);
        }

        @Override
        public void write(@NonNull byte[] buf, int off, int len) {
            super.write(buf, off, len);
            if (streams != null) {
                for (PrintStream printStream : streams) {
                    printStream.write(buf, off, len);
                }
            }
        }
    }
}
