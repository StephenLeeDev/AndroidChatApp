package com.example.androidchatappjava.Requests;

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

import com.example.androidchatappjava.Common.Constants;
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

public class RequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private List<RequestModel> list;
    private TextView textViewEmptyRequestsList;

    private DatabaseReference referenceRequest, referenceUser;
    private FirebaseUser currentUser;
    private View progressBar;

    public RequestsFragment() {

    }

    public static RequestsFragment newInstance() {
        RequestsFragment fragment = new RequestsFragment();
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
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        textViewEmptyRequestsList = view.findViewById(R.id.textViewEmptyRequestsList);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        list = new ArrayList<>();
        adapter = new RequestAdapter(getActivity(), list);
        recyclerView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        referenceUser = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().USERS);
        referenceRequest = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().FRIEND_REQUESTS).child(currentUser.getUid());

        textViewEmptyRequestsList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        referenceRequest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                list.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        String requestType = dataSnapshot.child(NodeNames.getInstance().REQUEST_TYPE).getValue().toString();
                        if (Constants.getInstance().REQUEST_STATUS_RECEIVED.equals(requestType)) {
                            String userId = dataSnapshot.getKey();
                            referenceUser.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                    String userName = snapshot2.child(NodeNames.getInstance().NAME).getValue().toString();
                                    String photoName = "";

                                    if (snapshot2.child(NodeNames.getInstance().PHOTO).getValue() != null) {
                                        photoName = snapshot2.child(NodeNames.getInstance().PHOTO).getValue().toString();
                                    }

                                    RequestModel model = new RequestModel(userId, userName, photoName);
                                    list.add(model);
                                    adapter.notifyDataSetChanged();
                                    textViewEmptyRequestsList.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.failed_to_fetch_friends, error.getMessage()), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.failed_to_fetch_friends, error.getMessage()), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}