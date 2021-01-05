package com.example.androidchatappjava.Common;

import android.content.Context;
import android.net.ConnectivityManager;

public class Util {

    public static Util uniqueInstance = new Util();

    public Util() {

    }

    public static Util getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Util();
        }
        return uniqueInstance;
    }

    public boolean connectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null && connectivityManager.getActiveNetwork() != null) {
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        } else {
            return false;
        }
    }
}
