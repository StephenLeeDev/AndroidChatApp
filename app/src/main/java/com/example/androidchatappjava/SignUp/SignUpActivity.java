package com.example.androidchatappjava.SignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.androidchatappjava.Common.NodeNames;
import com.example.androidchatappjava.Login.LoginActivity;
import com.example.androidchatappjava.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private String name, email, password, confirmPassword;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

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

    private void updateOnlyName() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setDisplayName(editTextName.getText().toString().trim()).build();

        firebaseUser.updateProfile(request).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = firebaseUser.getUid();
                databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().USERS);

                HashMap<String, String> hashMap = new HashMap<>();

                hashMap.put(NodeNames.getInstance().NAME, editTextName.getText().toString());
                hashMap.put(NodeNames.getInstance().EMAIL, editTextEmail.getText().toString());
                hashMap.put(NodeNames.getInstance().ONLINE, "true");
                hashMap.put(NodeNames.getInstance().PHOTO, "");

                databaseReference.child(userId).setValue(hashMap).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.user_created_successfully), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.fail_to_update_profile, task.getException()), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, getString(R.string.fail_to_update_profile, task.getException()), Toast.LENGTH_SHORT).show();
            }
        });
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
                    firebaseUser = firebaseAuth.getCurrentUser();
                    updateOnlyName();
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