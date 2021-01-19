package com.example.androidchatappjava.FindFriends;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidchatappjava.Common.Constants;
import com.example.androidchatappjava.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder>{

    private Context context;
    private List<FindFriendModel> list;

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
