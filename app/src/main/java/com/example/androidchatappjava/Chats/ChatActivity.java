package com.example.androidchatappjava.Chats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.androidchatappjava.Common.Constants;
import com.example.androidchatappjava.Common.Extras;
import com.example.androidchatappjava.Common.NodeNames;
import com.example.androidchatappjava.Common.Util;
import com.example.androidchatappjava.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    private ImageView imageViewAttachment, imageViewSend;
    private EditText editTextMessage;

    private DatabaseReference rootReference;
    private FirebaseAuth firebaseAuth;
    private String currentUserId, chatUserId;

    private RecyclerView recyclerViewMessages;
    private SwipeRefreshLayout swipeRefreshLayoutMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = this;

        imageViewSend = findViewById(R.id.imageViewSend);
        editTextMessage = findViewById(R.id.editTextMessage);

        findViewById(R.id.imageViewSend).setOnClickListener(this::onClick);

        firebaseAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        if (getIntent().hasExtra(Extras.getInstance().USER_KEY)) {
            chatUserId = getIntent().getStringExtra(Extras.getInstance().USER_KEY);
        }
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
            case R.id.imageViewSend:
                if (Util.connectionAvailable(this)) {
                    DatabaseReference userMessagePush = rootReference.child(NodeNames.getInstance().MESSAGES).child(currentUserId).child(chatUserId).push();
                    String pushId = userMessagePush.getKey();
                    sendMessage(editTextMessage.getText().toString().trim(), Constants.getInstance().MESSAGE_TYPE_TEXT, pushId);
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}