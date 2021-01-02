package com.example.androidchatappjava.Profile;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.androidchatappjava.Common.NodeNames;
import com.example.androidchatappjava.Login.LoginActivity;
import com.example.androidchatappjava.Password.ChangePasswordActivity;
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

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private int SUCCESS = 101;

    private TextInputEditText editTextName, editTextEmail;
    private ImageView imageViewProfile;
    private String name, email;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Uri localFileUri, serverFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        findViewById(R.id.buttonSave).setOnClickListener(this::onClick);
        findViewById(R.id.imageViewProfile).setOnClickListener(this::onClick);
        findViewById(R.id.textViewChangePassword).setOnClickListener(this::onClick);

        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (firebaseUser != null) {
            editTextName.setText(firebaseUser.getDisplayName());
            editTextEmail.setText(firebaseUser.getEmail());
            serverFileUri = firebaseUser.getPhotoUrl();

            if (serverFileUri != null) {
                Glide.with(this).load(serverFileUri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile).into(imageViewProfile);
            }
        }
    }

    private void clickLogoutButton(View view) {
        firebaseAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void clickSaveButton(View view) {
        if ("".equals(editTextName.getText().toString().trim())) {
            editTextName.setError(getString(R.string.enter_name));
        } else {
            if (localFileUri != null) {
                updateNameAndPhoto();
            } else {
                updateOnlyName();
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
                            hashMap.put(NodeNames.getInstance().PHOTO, serverFileUri.getPath());

                            databaseReference.child(userId).setValue(hashMap).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
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

                databaseReference.child(userId).setValue(hashMap).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
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

    private void changeImage(View view) {
        if (serverFileUri == null) {
            pickImage();
        } else {
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_picture, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.menu_change_picture) {
                        pickImage();
                    } else if (id == R.id.menu_remove_picture) {
                        removePhoto();
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }

    private void pickImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SUCCESS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
    }

    private void removePhoto() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setDisplayName(editTextName.getText().toString().trim()).setPhotoUri(null).build();

        firebaseUser.updateProfile(request).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                String userId = firebaseUser.getUid();
                databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().USERS);
                imageViewProfile.setImageResource(R.drawable.default_profile);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(NodeNames.getInstance().PHOTO, "");

                databaseReference.child(userId).setValue(hashMap).addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.photo_removed_successfully), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.fail_to_update_profile, task2.getException()), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, getString(R.string.fail_to_update_profile, task1.getException()), Toast.LENGTH_SHORT).show();
            }
        });
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                clickSaveButton(v);
                break;
            case R.id.buttonLogout:
                clickLogoutButton(v);
                break;
            case R.id.imageViewProfile:
                changeImage(v);
                break;
            case R.id.textViewChangePassword:
                startActivity(new Intent(this, ChangePasswordActivity.class));
                break;
        }
    }
}