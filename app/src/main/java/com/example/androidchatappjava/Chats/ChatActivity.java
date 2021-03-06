package com.example.androidchatappjava.Chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.androidchatappjava.Common.Constants;
import com.example.androidchatappjava.Common.Extras;
import com.example.androidchatappjava.Common.NodeNames;
import com.example.androidchatappjava.Common.Util;
import com.example.androidchatappjava.R;
import com.example.androidchatappjava.SelectFriend.SelectFriendActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 102;
    private static final int REQUEST_CODE_PICK_VIDEO = 103;
    private static final int REQUEST_CODE_FORWARD_MESSAGE = 104;

    private LinearLayout linearLayoutProgress;

    private Context context;
    private ImageView imageViewAttachment, imageViewSend, imageViewProfile;
    private EditText editTextMessage;
    private TextView textViewUserName;

    private DatabaseReference rootReference;
    private FirebaseAuth firebaseAuth;
    private String currentUserId, chatUserId;

    private RecyclerView recyclerViewMessages;
    private SwipeRefreshLayout swipeRefreshLayoutMessages;
    private MessageAdapter messageAdapter;
    private List<MessageModel> messageList;

    private int currentPage = 1;
    private static final int RECORD_PER_PAGE = 30;

    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;

    private BottomSheetDialog bottomSheetDialog;
    private String userName, photoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = this;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_action_bar, null);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);

            actionBar.setCustomView(actionBarLayout);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions()|ActionBar.DISPLAY_SHOW_CUSTOM);
        }

        linearLayoutProgress = findViewById(R.id.linearLayoutProgress);
        imageViewSend = findViewById(R.id.imageViewSend);
        imageViewAttachment = findViewById(R.id.imageViewAttachment);
        editTextMessage = findViewById(R.id.editTextMessage);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        swipeRefreshLayoutMessages = findViewById(R.id.swipeRefreshLayoutMessages);
        textViewUserName = findViewById(R.id.textViewUserName);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        findViewById(R.id.imageViewSend).setOnClickListener(this::onClick);
        findViewById(R.id.imageViewAttachment).setOnClickListener(this::onClick);

        firebaseAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        if (getIntent().hasExtra(Extras.getInstance().USER_KEY)) {
            chatUserId = getIntent().getStringExtra(Extras.getInstance().USER_KEY);
            photoName = chatUserId + ".jpg";
        }

        if (getIntent().hasExtra(Extras.getInstance().USER_NAME)) {
            userName = getIntent().getStringExtra(Extras.getInstance().USER_NAME);
        }

        textViewUserName.setText(userName);

        if(!TextUtils.isEmpty(photoName) && photoName!=null) {
            StorageReference storageReferencePhoto = FirebaseStorage.getInstance().getReference().child(Constants.getInstance().IMAGE_FOLDER).child(photoName);
            storageReferencePhoto.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this).load(uri).placeholder(R.drawable.default_profile).into(imageViewProfile);
            });
        }

        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        recyclerViewMessages.setAdapter(messageAdapter);

        loadMessage();
        recyclerViewMessages.scrollToPosition(messageList.size() - 1);
        swipeRefreshLayoutMessages.setOnRefreshListener(() -> {
            currentPage ++;
            loadMessage();
        });

        bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.chat_file_options, null);
        view.findViewById(R.id.linearLayoutCamera).setOnClickListener(this);
        view.findViewById(R.id.linearLayoutGallery).setOnClickListener(this);
        view.findViewById(R.id.linearLayoutVideo).setOnClickListener(this);
        view.findViewById(R.id.imageViewClose).setOnClickListener(this);
        bottomSheetDialog.setContentView(view);

        if (getIntent().hasExtra(Extras.MESSAGE) && getIntent().hasExtra(Extras.MESSAGE_ID) && getIntent().hasExtra(Extras.MESSAGE_TYPE)) {
            String message = getIntent().getStringExtra(Extras.MESSAGE);
            String messageId = getIntent().getStringExtra(Extras.MESSAGE_ID);
            final String messageType = getIntent().getStringExtra(Extras.MESSAGE_TYPE);

            DatabaseReference messageRef = rootReference.child(NodeNames.getInstance().MESSAGES).child(currentUserId).child(chatUserId).push();
            final String newMessageId = messageRef.getKey();

            if(messageType.equals(Constants.getInstance().MESSAGE_TYPE_TEXT)) {
                sendMessage(message, messageType, newMessageId);
            } else{
                StorageReference rootRef = FirebaseStorage.getInstance().getReference();
                String folder = messageType.equals( Constants.getInstance().MESSAGE_TYPE_VIDEO)? Constants.getInstance().MESSAGE_VIDEOS:Constants.getInstance().MESSAGE_IMAGES;
                String oldFileName = messageType.equals( Constants.getInstance().MESSAGE_TYPE_VIDEO)?messageId + ".mp4": messageId+".jpg";
                String newFileName = messageType.equals( Constants.getInstance().MESSAGE_TYPE_VIDEO)?newMessageId + ".mp4": newMessageId+".jpg";

                final String localFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + oldFileName;
                final File localFile = new File(localFilePath);

                final StorageReference newFileRef = rootRef.child(folder).child(newFileName);
                rootRef.child(folder).child(oldFileName).getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    UploadTask uploadTask = newFileRef.putFile(Uri.fromFile(localFile));
                    uploadProgress(uploadTask, newFileRef, newMessageId, messageType);
                });
            }
        }
    }

    private void loadMessage() {
        messageList.clear();
        databaseReference = rootReference.child(NodeNames.getInstance().MESSAGES).child(currentUserId).child(chatUserId);

        Query query = databaseReference.limitToLast(currentPage * RECORD_PER_PAGE);

        if (childEventListener != null) {
            query.removeEventListener(childEventListener);
        }

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageModel message = snapshot.getValue(MessageModel.class);

                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                swipeRefreshLayoutMessages.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                loadMessage();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayoutMessages.setRefreshing(false);
            }
        };
        query.addChildEventListener(childEventListener);
    }

    private void sendMessage(String message, String messageType, String pushId) {
        try {
            if (!"".equals(message)) {
                HashMap messageMap = new HashMap();
                messageMap.put(NodeNames.getInstance().MESSAGE_ID, pushId);
                messageMap.put(NodeNames.getInstance().MESSAGE, message);
                messageMap.put(NodeNames.getInstance().MESSAGE_TYPE, messageType);
                messageMap.put(NodeNames.getInstance().MESSAGE_FROM, currentUserId);
                messageMap.put(NodeNames.getInstance().MESSAGE_TIME, ServerValue.TIMESTAMP);

                String currentUserReference = NodeNames.getInstance().MESSAGES + "/" + currentUserId + "/" + chatUserId;
                String chatUserReference = NodeNames.getInstance().MESSAGES + "/" + chatUserId + "/" + currentUserId;

                HashMap messageUserMap = new HashMap();
                messageUserMap.put(currentUserReference + "/" + pushId, messageMap);
                messageUserMap.put(chatUserReference + "/" + pushId, messageMap);

                editTextMessage.setText("");

                rootReference.updateChildren(messageUserMap, (error, ref) -> {
                    if (error != null) {
                        Toast.makeText(context, getString(R.string.failed_to_send_message, error.getMessage()), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, getString(R.string.message_sent_successfully), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(context, getString(R.string.failed_to_send_message, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageViewSend: {
                if (Util.connectionAvailable(this)) {
                    DatabaseReference userMessagePush = rootReference.child(NodeNames.getInstance().MESSAGES).child(currentUserId).child(chatUserId).push();
                    String pushId = userMessagePush.getKey();
                    sendMessage(editTextMessage.getText().toString().trim(), Constants.getInstance().MESSAGE_TYPE_TEXT, pushId);
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.imageViewAttachment: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (bottomSheetDialog != null) {
                        bottomSheetDialog.show();
                    }
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                break;
            }
            case R.id.linearLayoutCamera: {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
                break;
            }

            case R.id.linearLayoutGallery: {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                break;
            }
            case R.id.linearLayoutVideo: {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_PICK_VIDEO);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAPTURE_IMAGE) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                uploadBytes(bytes, Constants.getInstance().MESSAGE_TYPE_IMAGE);
            } else if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                Uri uri = data.getData();
                uploadFile(uri, Constants.getInstance().MESSAGE_TYPE_IMAGE);
            } else if (requestCode == REQUEST_CODE_PICK_VIDEO) {
                Uri uri = data.getData();
                uploadFile(uri, Constants.getInstance().MESSAGE_TYPE_VIDEO);
            } else if(requestCode == REQUEST_CODE_FORWARD_MESSAGE){

                Intent intent = new Intent( this, ChatActivity.class);
                intent.putExtra(Extras.getInstance().USER_KEY, data.getStringExtra(Extras.getInstance().USER_KEY));
                intent.putExtra(Extras.getInstance().USER_NAME, data.getStringExtra(Extras.getInstance().USER_NAME));
                intent.putExtra(Extras.getInstance().PHOTO_NAME, data.getStringExtra(Extras.getInstance().PHOTO_NAME));

                intent.putExtra(Extras.getInstance().MESSAGE, data.getStringExtra(Extras.getInstance().MESSAGE));
                intent.putExtra(Extras.getInstance().MESSAGE_ID, data.getStringExtra(Extras.getInstance().MESSAGE_ID));
                intent.putExtra(Extras.getInstance().MESSAGE_TYPE, data.getStringExtra(Extras.getInstance().MESSAGE_TYPE));

                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.show();
                } else {
                    Toast.makeText(this, getString(R.string.permission_required_to_access_files), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadFile(Uri uri, String messageType) {
        DatabaseReference databaseReference = rootReference.child(NodeNames.getInstance().MESSAGES).child(currentUserId).child(chatUserId).push();
        String pushId = databaseReference.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO) ? Constants.getInstance().MESSAGE_VIDEOS : Constants.getInstance().MESSAGE_IMAGES;
        String fileName = messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO) ? pushId + ".mp4" : pushId + ".jpg";

        StorageReference fileRef = storageReference.child(folderName).child(fileName);
        UploadTask uploadTask = fileRef.putFile(uri);

        uploadProgress(uploadTask, fileRef, pushId, messageType);
    }

    private void uploadBytes(ByteArrayOutputStream bytes, String messageType) {
        DatabaseReference databaseReference = rootReference.child(NodeNames.getInstance().MESSAGES).child(currentUserId).child(chatUserId).push();
        String pushId = databaseReference.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO) ? Constants.getInstance().MESSAGE_VIDEOS : Constants.getInstance().MESSAGE_IMAGES;
        String fileName = messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO) ? pushId + ".mp4" : pushId + ".jpg";

        StorageReference fileRef = storageReference.child(folderName).child(fileName);
        UploadTask uploadTask = fileRef.putBytes(bytes.toByteArray());

        uploadProgress(uploadTask, fileRef, pushId, messageType);
    }

    private void uploadProgress(final UploadTask task, final StorageReference filePath, final String pushId, final String messageType) {

        final View view = getLayoutInflater().inflate(R.layout.file_progress, null);
        final ProgressBar progressBarProgress = view.findViewById(R.id.progressBarProgress);
        final TextView textViewFileProgress = view.findViewById(R.id.textViewFileProgress);
        final ImageView imageViewPlay = view.findViewById(R.id.imageViewPlay);
        final ImageView imageViewPause = view.findViewById(R.id.imageViewPause);
        ImageView imageViewCancel = view.findViewById(R.id.imageViewCancel);

        imageViewPause.setOnClickListener(v -> {
            task.pause();
            imageViewPlay.setVisibility(View.VISIBLE);
            imageViewPause.setVisibility(View.GONE);
        });

        imageViewPlay.setOnClickListener(v -> {
            task.resume();
            imageViewPause.setVisibility(View.VISIBLE);
            imageViewPlay.setVisibility(View.GONE);
        });

        imageViewCancel.setOnClickListener(v -> task.cancel());

        linearLayoutProgress.addView(view);
        textViewFileProgress.setText(getString(R.string.upload_progress, messageType, "0"));

        task.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

            progressBarProgress.setProgress((int) progress);
            textViewFileProgress.setText(getString(R.string.upload_progress, messageType, String.valueOf(progressBarProgress.getProgress())));

        });

        task.addOnCompleteListener(task1 -> {
            linearLayoutProgress.removeView(view);
            if (task1.isSuccessful()) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        sendMessage(downloadUrl, messageType, pushId);
                    }
                });
            }
        });

        task.addOnFailureListener(e -> {
            linearLayoutProgress.removeView(view);
            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_upload, e.getMessage()), Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteMessage(final String messageId, final String messageType){

        DatabaseReference databaseReference = rootReference.child(NodeNames.getInstance().MESSAGES)
                .child(currentUserId).child(chatUserId).child(messageId);

        databaseReference.removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DatabaseReference databaseReferenceChatUser = rootReference.child(NodeNames.getInstance().MESSAGES)
                        .child(chatUserId).child(currentUserId).child(messageId);

                databaseReferenceChatUser.removeValue().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, R.string.message_deleted_successfully, Toast.LENGTH_SHORT).show();
                        if(!messageType.equals(Constants.getInstance().MESSAGE_TYPE_TEXT)) {
                            StorageReference rootRef = FirebaseStorage.getInstance().getReference();
                            String folder = messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO)?Constants.getInstance().MESSAGE_VIDEOS:Constants.getInstance().MESSAGE_IMAGES;
                            String fileName = messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO)?messageId +".mp4": messageId+".jpg";
                            StorageReference fileRef = rootRef.child(folder).child(fileName);

                            fileRef.delete().addOnCompleteListener(task2 -> {
                                if(!task2.isSuccessful())
                                {
                                    Toast.makeText(ChatActivity.this,
                                            getString(R.string.failed_to_delete_file, task2.getException()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(ChatActivity.this, getString(R.string.failed_to_delete_message, task1.getException()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ChatActivity.this, getString( R.string.failed_to_delete_message, task.getException()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void forwardMessage(String selectedMessageId, String selectedMessage, String selectedMessageType) {
        Intent intent = new Intent(this, SelectFriendActivity.class);
        intent.putExtra(Extras.MESSAGE, selectedMessage);
        intent.putExtra(Extras.MESSAGE_ID, selectedMessageId);
        intent.putExtra(Extras.MESSAGE_TYPE, selectedMessageType);
        startActivityForResult(intent , REQUEST_CODE_FORWARD_MESSAGE);
    }

    public void downloadFile(String messageId, final String messageType, final boolean isShare){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        } else {
            String folderName = messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO) ? Constants.getInstance().MESSAGE_VIDEOS : Constants.getInstance().MESSAGE_IMAGES;
            String fileName = messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO) ? messageId + ".mp4": messageId + ".jpg";

            StorageReference fileRef= FirebaseStorage.getInstance().getReference().child(folderName).child(fileName);
            final String localFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName;

            File localFile = new File(localFilePath);

            try {
                if(localFile.exists() || localFile.createNewFile()) {
                    final FileDownloadTask downloadTask =  fileRef.getFile(localFile);

                    final View view = getLayoutInflater().inflate(R.layout.file_progress, null);
                    final ProgressBar progressBarProgress = view.findViewById(R.id.progressBarProgress);
                    final TextView textViewFileProgress = view.findViewById(R.id.textViewFileProgress);
                    final ImageView imageViewPlay = view.findViewById(R.id.imageViewPlay);
                    final ImageView imageViewPause = view.findViewById(R.id.imageViewPause);
                    ImageView imageViewCancel = view.findViewById(R.id.imageViewCancel);

                    imageViewPause.setOnClickListener(view1 -> {
                        downloadTask.pause();
                        imageViewPlay.setVisibility(View.VISIBLE);
                        imageViewPause.setVisibility(View.GONE);
                    });

                    imageViewPlay.setOnClickListener(view2 -> {
                        downloadTask.resume();
                        imageViewPause.setVisibility(View.VISIBLE);
                        imageViewPlay.setVisibility(View.GONE);
                    });

                    imageViewCancel.setOnClickListener(view3 -> downloadTask.cancel());

                    linearLayoutProgress.addView(view);
                    textViewFileProgress.setText(getString(R.string.download_progress, messageType, "0"));

                    downloadTask.addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        progressBarProgress.setProgress((int) progress);
                        textViewFileProgress.setText(getString(R.string.download_progress, messageType, String.valueOf(progressBarProgress.getProgress())));
                    });

                    downloadTask.addOnCompleteListener(task -> {
                        linearLayoutProgress.removeView(view);
                        if (task.isSuccessful()) {
                            if(isShare){
                                Intent intentShare = new Intent();
                                intentShare.setAction(Intent.ACTION_SEND);
                                intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse(localFilePath));
                                if(messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO)) {
                                    intentShare.setType("video/mp4");
                                }
                                if(messageType.equals(Constants.getInstance().MESSAGE_TYPE_IMAGE)) {
                                    intentShare.setType("image/jpg");
                                }
                                startActivity(Intent.createChooser(intentShare, getString(R.string.share_with)));
                            } else {
                                Snackbar snackbar = Snackbar.make(linearLayoutProgress, getString(R.string.file_downloaded_successfully), Snackbar.LENGTH_INDEFINITE);

                                snackbar.setAction(R.string.view, view4 -> {
                                    Uri uri = Uri.parse(localFilePath);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    if (messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO)) {
                                        intent.setDataAndType(uri, "video/mp4");
                                    } else if (messageType.equals(Constants.getInstance().MESSAGE_TYPE_IMAGE)) {
                                        intent.setDataAndType(uri, "image/jpg");
                                    }
                                    startActivity(intent);
                                });
                                snackbar.show();
                            }
                        }
                    });

                    downloadTask.addOnFailureListener(e -> {
                        linearLayoutProgress.removeView(view);
                        Toast.makeText(ChatActivity.this, getString(R.string.failed_to_download, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
                }
                else {
                    Toast.makeText(this, R.string.failed_to_store_file, Toast.LENGTH_SHORT).show();
                }
            }
            catch(Exception ex){
                Toast.makeText(ChatActivity.this, getString(R.string.failed_to_download, ex.getMessage()), Toast.LENGTH_SHORT).show();
            }
        }
    }
}