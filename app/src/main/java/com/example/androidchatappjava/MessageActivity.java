package com.example.androidchatappjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.androidchatappjava.Common.Util;

public class MessageActivity extends AppCompatActivity {

    private TextView textViewMessage;
    private ProgressBar progressBar;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        textViewMessage = findViewById(R.id.textViewMessage);
        progressBar = findViewById(R.id.progressBar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    finish();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    textViewMessage.setText(R.string.no_internet);
                }
            };

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), networkCallback);
        }
    }

    private void clickRetryButton(View view) {
        progressBar.setVisibility(View.VISIBLE);

        if (Util.getInstance().connectionAvailable(this)) {
            finish();
        } else {
            new android.os.Handler().postDelayed(() -> progressBar.setVisibility(View.GONE), 1000);
        }
    }

    private void clickCloseButton(View view) {
        finishAffinity();
    }
}