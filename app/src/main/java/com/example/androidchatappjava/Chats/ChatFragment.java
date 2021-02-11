package com.example.androidchatappjava.Chats;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidchatappjava.Common.NodeNames;
import com.example.androidchatappjava.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewChat;
    private View progressBar;
    private TextView textViewEmptyChatList;
    private ChatListAdapter chatListAdapter;
    private List<ChatListModel> chatListModelList;

    private DatabaseReference databaseReferenceChat, databaseReferenceUser;
    private FirebaseUser currentUser;

    private ChildEventListener childEventListener;
    private Query query;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmptyChatList = view.findViewById(R.id.textViewEmptyChatList);

        chatListModelList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(getActivity(), chatListModelList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);

        recyclerViewChat.setAdapter(chatListAdapter);

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().USERS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceChat = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().CHATS).child(currentUser.getUid());

        query = databaseReferenceChat.orderByChild(NodeNames.getInstance().TIME_STAMP);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot, true, snapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        query.addChildEventListener(childEventListener);
        progressBar.setVisibility(View.VISIBLE);
        textViewEmptyChatList.setVisibility(View.VISIBLE);
    }

    private void updateList(DataSnapshot dataSnapshot, boolean isNew, String userId) {

        progressBar.setVisibility(View.GONE);
        textViewEmptyChatList.setVisibility(View.GONE);

        final String lastMessage = "";
        final String lastMessageTime = "";
        final String unreadCount = "";

        databaseReferenceUser.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String fullName = snapshot.child(NodeNames.getInstance().NAME).getValue() != null ?
                        snapshot.child(NodeNames.getInstance().NAME).getValue().toString() : "";
                String photoName = snapshot.child(NodeNames.getInstance().PHOTO).getValue() != null ?
                        snapshot.child(NodeNames.getInstance().PHOTO).getValue().toString() : "";

                ChatListModel model = new ChatListModel(userId, fullName, photoName, unreadCount, lastMessage, lastMessageTime);

                chatListModelList.add(model);
                chatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.failed_to_fetch_chat_list, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        query.removeEventListener(childEventListener);
    }
}