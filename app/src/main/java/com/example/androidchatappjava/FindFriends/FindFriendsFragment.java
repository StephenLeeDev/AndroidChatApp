package com.example.androidchatappjava.FindFriends;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindFriendsFragment extends Fragment {

    private RecyclerView recyclerViewFindFriends;
    private FindFriendAdapter findFriendAdapter;
    private List<FindFriendModel> findFriendModelList;
    private TextView textViewEmptyFriendsList;

    private DatabaseReference reference;
    private FirebaseUser currentUser;
    private View progressBar;

    public FindFriendsFragment() {
        // Required empty public constructor
    }

    public static FindFriendsFragment newInstance(String param1, String param2) {
        FindFriendsFragment fragment = new FindFriendsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewFindFriends = view.findViewById(R.id.recyclerViewFindFriends);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmptyFriendsList = view.findViewById(R.id.textViewEmptyFriendsList);

        recyclerViewFindFriends.setLayoutManager(new LinearLayoutManager(getActivity()));

        findFriendModelList = new ArrayList<>();
        findFriendAdapter = new FindFriendAdapter(getActivity(), findFriendModelList);
        recyclerViewFindFriends.setAdapter(findFriendAdapter);

        reference = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().USERS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        textViewEmptyFriendsList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Query query = reference.orderByChild(NodeNames.getInstance().NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                findFriendModelList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userId = dataSnapshot.getKey();

                    if (userId.equals(currentUser.getUid())) {
                        return;
                    }

                    if (dataSnapshot.child(NodeNames.getInstance().NAME).getValue() != null) {
                        String fullName = dataSnapshot.child(NodeNames.getInstance().NAME).getValue().toString();
                        String photoName = dataSnapshot.child(NodeNames.getInstance().PHOTO).getValue().toString();

                        findFriendModelList.add(new FindFriendModel(fullName, photoName, userId, false));
                        findFriendAdapter.notifyDataSetChanged();

                        textViewEmptyFriendsList.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getContext().getString(R.string.failed_to_fetch_friends, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}