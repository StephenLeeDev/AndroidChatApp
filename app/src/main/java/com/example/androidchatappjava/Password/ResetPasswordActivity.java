package com.example.androidchatappjava.Password;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.androidchatappjava.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText editTextEmail;
    TextView textViewMessage;
    LinearLayout linearLayoutMessage, linearLayoutResetPassword;
    Button buttonRetry, buttonClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        textViewMessage = findViewById(R.id.textViewMessage);
        linearLayoutMessage = findViewById(R.id.linearLayoutMessage);
        linearLayoutResetPassword = findViewById(R.id.linearLayoutResetPassword);
        buttonRetry = findViewById(R.id.buttonRetry);
        buttonClose = findViewById(R.id.buttonClose);

        findViewById(R.id.buttonRetry).setOnClickListener(this::onClick);
        findViewById(R.id.buttonClose).setOnClickListener(this::onClick);
        findViewById(R.id.btnResetPassword).setOnClickListener(this::onClick);
    }

    private void clickResetPasswordButton(View view) {
        String email = editTextEmail.getText().toString().trim();

        if ("".equals(email)) {
            editTextEmail.setError(getString(R.string.enter_email));
        } else {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                linearLayoutResetPassword.setVisibility(View.GONE);
                linearLayoutMessage.setVisibility(View.VISIBLE);
                if (task.isSuccessful()) {
                    textViewMessage.setText(getString(R.string.reset_password_instructions, email));
                    new CountDownTimer(60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            buttonRetry.setText(getString(R.string.resend_timer, String.valueOf(millisUntilFinished / 1000)));
                            buttonRetry.setOnClickListener(null);
                        }

                        @Override
                        public void onFinish() {
                            buttonRetry.setText(getString(R.string.retry));
                            buttonRetry.setOnClickListener(v -> {
                                linearLayoutResetPassword.setVisibility(View.VISIBLE);
                                linearLayoutMessage.setVisibility(View.GONE);
                            });
                        }
                    };
                } else {
                    textViewMessage.setText(getString(R.string.email_sent_failed, task.getException()));
                    buttonRetry.setText(getString(R.string.retry));
                    buttonRetry.setOnClickListener(v -> {
                        linearLayoutResetPassword.setVisibility(View.VISIBLE);
                        linearLayoutMessage.setVisibility(View.GONE);
                    });
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonClose:
                finish();
                break;
            case R.id.buttonRetry:
            case R.id.btnResetPassword:
                clickResetPasswordButton(v);
                break;
        }
    }
}