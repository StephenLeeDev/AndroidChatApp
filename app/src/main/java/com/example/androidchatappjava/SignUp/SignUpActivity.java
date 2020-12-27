package com.example.androidchatappjava.SignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.androidchatappjava.Login.LoginActivity;
import com.example.androidchatappjava.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    String name, email, password, confirmPassword;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        findViewById(R.id.buttonSignUp).setOnClickListener(this::onClick);
    }

    private void clickSignUpButton() {

        name = editTextName.getText().toString();
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        confirmPassword = editTextConfirmPassword.getText().toString();

        if ("".equals(name)) {
            editTextName.setError(getString(R.string.enter_name));
        } else if ("".equals(email)) {
            editTextEmail.setError(getString(R.string.enter_email));
        } else if ("".equals(password)) {
            editTextEmail.setError(getString(R.string.enter_password));
        } else if ("".equals(confirmPassword)) {
            editTextEmail.setError(getString(R.string.enter_confirm_password));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError(getString(R.string.enter_correct_email));
        } else if (!password.equals(confirmPassword)) {
            editTextEmail.setError(getString(R.string.password_mismatch));
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, getString(R.string.user_created_successfully), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.sign_up_failed) + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSignUp:
                clickSignUpButton();
                break;
        }
    }
}