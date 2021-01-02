package com.example.androidchatappjava.Password;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.androidchatappjava.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText editTextPassword, editTextConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        findViewById(R.id.buttonChangePassword).setOnClickListener(this::onClick);
    }

    private void clickChangePasswordButton(View v) {
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        if ("".equals(password)) {
            editTextPassword.setError(getString(R.string.enter_password));
        } else if ("".equals(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.enter_confirm_password));
        } else if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null) {
                firebaseUser.updatePassword(password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.something_went_wrong) + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonChangePassword:
                clickChangePasswordButton(v);
                break;
        }
    }
}