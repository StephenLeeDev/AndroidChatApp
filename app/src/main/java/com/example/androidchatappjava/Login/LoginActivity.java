package com.example.androidchatappjava.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidchatappjava.Common.Util;
import com.example.androidchatappjava.MainActivity;
import com.example.androidchatappjava.MessageActivity;
import com.example.androidchatappjava.Password.ResetPasswordActivity;
import com.example.androidchatappjava.R;
import com.example.androidchatappjava.SignUp.SignUpActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText editTextEmail, editTextPassword;
    private String email, password;
    private View progressBar;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.buttonLogin).setOnClickListener(this::onClick);
        findViewById(R.id.textViewSignUp).setOnClickListener(this::onClick);
        findViewById(R.id.textViewResetPassword).setOnClickListener(this::onClick);
    }

    private void clickLoginButton() {
        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        if ("".equals(email)) {
            editTextEmail.setError(getString(R.string.enter_email));
        } else if ("".equals(password)) {
            editTextPassword.setError(getString(R.string.enter_password));
        } else {
            if (Util.getInstance().connectionAvailable(this)) {
                progressBar.setVisibility(View.VISIBLE);
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.login_failed) + " : " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                startActivity(new Intent(this, MessageActivity.class));
            }
        }
    }

    private void clickSignUp() {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLogin:
                clickLoginButton();
                break;
            case R.id.textViewSignUp:
                clickSignUp();
                break;
            case R.id.textViewResetPassword:
                startActivity(new Intent(this, ResetPasswordActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}