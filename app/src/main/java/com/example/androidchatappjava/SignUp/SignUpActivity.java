package com.example.androidchatappjava.SignUp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.androidchatappjava.Common.NodeNames;
import com.example.androidchatappjava.Login.LoginActivity;
import com.example.androidchatappjava.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private int SUCCESS = 101;

    private TextInputEditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private ImageView imageViewProfile;
    private String name, email, password, confirmPassword;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Uri localFileUri, serverFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        findViewById(R.id.buttonSignUp).setOnClickListener(this::onClick);
        findViewById(R.id.imageViewProfile).setOnClickListener(this::onClick);

        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void pickImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SUCCESS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SUCCESS) {
            if (resultCode == RESULT_OK) {
                localFileUri = data.getData();
                imageViewProfile.setImageURI(localFileUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_OK);
            } else {
                Toast.makeText(this, getString(R.string.access_permission_is_required), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateNameAndPhoto() {
        String fileName = firebaseUser.getUid() + ".jpg";

        final StorageReference reference = storageReference.child("images/" + fileName);

        reference.putFile(localFileUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    serverFileUri = uri;
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setDisplayName(editTextName.getText().toString().trim()).setPhotoUri(serverFileUri).build();

                    firebaseUser.updateProfile(request).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            String userId = firebaseUser.getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().USERS);

                            HashMap<String, String> hashMap = new HashMap<>();

                            hashMap.put(NodeNames.getInstance().NAME, editTextName.getText().toString());
                            hashMap.put(NodeNames.getInstance().EMAIL, editTextEmail.getText().toString());
                            hashMap.put(NodeNames.getInstance().ONLINE, "true");
                            hashMap.put(NodeNames.getInstance().PHOTO, serverFileUri.getPath());

                            databaseReference.child(userId).setValue(hashMap).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    Toast.makeText(this, getString(R.string.user_created_successfully), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, getString(R.string.fail_to_update_profile, task2.getException()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(this, getString(R.string.fail_to_update_profile, task1.getException()), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
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
            editTextPassword.setError(getString(R.string.enter_password));
        } else if ("".equals(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.enter_confirm_password));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError(getString(R.string.enter_correct_email));
        } else if (!password.equals(confirmPassword)) {
            editTextEmail.setError(getString(R.string.password_mismatch));
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    firebaseUser = firebaseAuth.getCurrentUser();

                    if (localFileUri == null) {
                        updateOnlyName();
                    } else {
                        updateNameAndPhoto();
                    }
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
            case R.id.imageViewProfile:
                pickImage();
                break;
        }
    }
}