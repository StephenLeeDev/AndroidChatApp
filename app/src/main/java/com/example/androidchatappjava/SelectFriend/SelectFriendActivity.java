package com.example.androidchatappjava.SelectFriend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.androidchatappjava.Common.Extras;
import com.example.androidchatappjava.Common.NodeNames;
import com.example.androidchatappjava.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSelectFriend;
    private SelectFriendAdapter selectFriendAdapter;
    private List<SelectFriendModel> selectFriendModels;
    private View progressBar;

    private DatabaseReference databaseReferenceUsers, databaseReferenceChats;

    private FirebaseUser currentUser;
    private ValueEventListener valueEventListener;

    private String selectedMessage, selectedMessageId, selectedMessageType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        if (getIntent().hasExtra(Extras.getInstance().MESSAGE)) {
            selectedMessage = getIntent().getStringExtra(Extras.getInstance().MESSAGE);
            selectedMessageId = getIntent().getStringExtra(Extras.getInstance().MESSAGE_ID);
            selectedMessageType = getIntent().getStringExtra(Extras.getInstance().MESSAGE_TYPE);
        }


        recyclerViewSelectFriend = findViewById(R.id.recyclerViewSelectFriend);
        progressBar = findViewById(R.id.progressBar);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewSelectFriend.setLayoutManager(linearLayoutManager);

        selectFriendModels = new ArrayList<>();
        selectFriendAdapter = new SelectFriendAdapter(this, selectFriendModels);
        recyclerViewSelectFriend.setAdapter(selectFriendAdapter);

        progressBar.setVisibility(View.VISIBLE);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().CHATS).child(currentUser.getUid());
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().USERS);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    final String userId = ds.getKey();
                    databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String userName = dataSnapshot.child(NodeNames.getInstance().NAME).getValue()!=null ?
                                    dataSnapshot.child(NodeNames.getInstance().NAME).getValue().toString() :
                                    "";

                            SelectFriendModel friendModel = new SelectFriendModel(userId, userName, userId + ".jpg");
                            selectFriendModels.add(friendModel);
                            selectFriendAdapter.notifyDataSetChanged();

                            progressBar.setVisibility(View.GONE);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(SelectFriendActivity.this, getString(R.string.failed_to_fetch_friend_list, databaseError.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SelectFriendActivity.this,
                        getString(R.string.failed_to_fetch_friend_list, databaseError.getMessage()), Toast.LENGTH_SHORT).show();
            }
        };

        databaseReferenceChats.addValueEventListener(valueEventListener);
    }

    public  void  returnSelectedFriend(String userId, String userName, String photoName) {

        databaseReferenceChats.removeEventListener(valueEventListener);
        Intent intent = new Intent();

        intent.putExtra(Extras.getInstance().USER_KEY, userId);
        intent.putExtra(Extras.getInstance().USER_NAME, userName);
        intent.putExtra(Extras.getInstance().PHOTO_NAME, photoName);

        intent.putExtra(Extras.getInstance().MESSAGE, selectedMessage);
        intent.putExtra(Extras.getInstance().MESSAGE_ID, selectedMessageId);
        intent.putExtra(Extras.getInstance().MESSAGE_TYPE, selectedMessageType);

        setResult(Activity.RESULT_OK, intent);
        finish();

    }
}