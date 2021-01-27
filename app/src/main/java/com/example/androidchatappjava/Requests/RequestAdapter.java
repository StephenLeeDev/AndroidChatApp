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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidchatappjava.Common.Constants;
import com.example.androidchatappjava.Common.NodeNames;
import com.example.androidchatappjava.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<RequestModel> list = new ArrayList<>();
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    public RequestAdapter(Context context, List<RequestModel> list) {
        this.context = context;
        this.list.clear();
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

        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.getInstance().FRIEND_REQUESTS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.buttonDeny.setOnClickListener(v -> {
            holder.progressBarDecision.setVisibility(View.VISIBLE);
            holder.buttonDeny.setVisibility(View.GONE);
            holder.buttonAccept.setVisibility(View.GONE);

            String userId = model.getUserId();
            databaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.getInstance().REQUEST_TYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        databaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.getInstance().REQUEST_TYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task1) {
                                if (task1.isSuccessful()) {
                                    holder.progressBarDecision.setVisibility(View.GONE);
                                    holder.buttonDeny.setVisibility(View.VISIBLE);
                                    holder.buttonAccept.setVisibility(View.VISIBLE);
                                } else {
                                    Toast.makeText(context, context.getString(R.string.failed_to_deny_request, task.getException()), Toast.LENGTH_SHORT).show();
                                    holder.progressBarDecision.setVisibility(View.GONE);
                                    holder.buttonDeny.setVisibility(View.VISIBLE);
                                    holder.buttonAccept.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    } else {
                        Toast.makeText(context, context.getString(R.string.failed_to_deny_request, task.getException()), Toast.LENGTH_SHORT).show();
                        holder.progressBarDecision.setVisibility(View.GONE);
                        holder.buttonDeny.setVisibility(View.VISIBLE);
                        holder.buttonAccept.setVisibility(View.VISIBLE);
                    }
                }
            });
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
