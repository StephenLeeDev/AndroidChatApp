package com.example.androidchatappjava;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText editTextEmail, editTextPassword;
    String email, password;
    Button buttonLogin;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        buttonLogin.setOnClickListener(this::onClick);
    }

    private void clickButtonLogin() {
        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        if ("".equals(email)) {
            editTextEmail.setError(getString(R.string.enter_email));
        } else if ("".equals(password)) {
            editTextPassword.setError(getString(R.string.enter_password));
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                } else {
                    Toast.makeText(this, getString(R.string.login_failed) + " : " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLogin:
                clickButtonLogin();
                break;
        }
    }
}