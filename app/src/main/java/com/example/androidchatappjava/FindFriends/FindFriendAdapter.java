package com.example.androidchatappjava.FindFriends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidchatappjava.Common.Constants;
import com.example.androidchatappjava.Common.NodeNames;
import com.example.androidchatappjava.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder>{

    private Context context;
    private List<FindFriendModel> list;

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private String userId;

    public FindFriendAdapter(Context context, List<FindFriendModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public FindFriendAdapter.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.find_friends_layout, parent, false);
        return new FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendAdapter.FindFriendViewHolder holder, int position) {
        FindFriendModel model = list.get(position);

        holder.textViewFullName.setText(model.getUserName());
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(Constants.getInstance().IMAGE_FOLDER + "/" + model.getPhotoName());
        reference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile).into(holder.imageViewProfile);
        });

        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().FRIEND_REQUESTS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.buttonSendRequest.setOnClickListener(v -> {
            holder.buttonSendRequest.setVisibility(View.GONE);
            holder.progressBarRequest.setVisibility(View.VISIBLE);

            userId = model.getUserId();
            databaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.getInstance().REQUEST_TYPE).setValue(Constants.getInstance().REQUEST_STATUS_SENT).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    databaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.getInstance().REQUEST_TYPE).setValue(Constants.getInstance().REQUEST_STATUS_RECEIVED).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(context, context.getString(R.string.request_sent_successfully), Toast.LENGTH_SHORT).show();
                            holder.buttonSendRequest.setVisibility(View.GONE);
                            holder.progressBarRequest.setVisibility(View.GONE);
                            holder.buttonCancelRequest.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(context, context.getString(R.string.failed_to_send_friend_request, task.getException()), Toast.LENGTH_SHORT).show();
                            holder.buttonSendRequest.setVisibility(View.VISIBLE);
                            holder.progressBarRequest.setVisibility(View.GONE);
                            holder.buttonCancelRequest.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_to_send_friend_request, task.getException()), Toast.LENGTH_SHORT).show();
                    holder.buttonSendRequest.setVisibility(View.VISIBLE);
                    holder.progressBarRequest.setVisibility(View.GONE);
                    holder.buttonCancelRequest.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewProfile;
        private TextView textViewFullName;
        private Button buttonSendRequest, buttonCancelRequest;
        private ProgressBar progressBarRequest;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewFullName = itemView.findViewById(R.id.textViewFullName);
            buttonSendRequest = itemView.findViewById(R.id.buttonSendRequest);
            buttonCancelRequest = itemView.findViewById(R.id.buttonCancelRequest);
            progressBarRequest = itemView.findViewById(R.id.progressBarRequest);
        }
    }
}
