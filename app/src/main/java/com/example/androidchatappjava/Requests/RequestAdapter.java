package com.example.androidchatappjava.Requests;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<RequestModel> list;

    public RequestAdapter(Context context, List<RequestModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_layout, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, int position) {
        RequestModel model = list.get(position);

        holder.textViewFullName.setText(model.getUserName());

        StorageReference reference = FirebaseStorage.getInstance().getReference().child(Constants.getInstance().IMAGE_FOLDER + "/" + model.getPhotoName());

        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile).into(holder.imageViewProfile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewFullName;
        private ImageView imageViewProfile;
        private Button buttonAccept, buttonDeny;
        private ProgressBar progressBarDecision;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewFullName = itemView.findViewById(R.id.textViewFullName);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            buttonAccept = itemView.findViewById(R.id.buttonAccept);
            buttonDeny = itemView.findViewById(R.id.buttonDeny);
            progressBarDecision = itemView.findViewById(R.id.progressBarDecision);
        }
    }
}
