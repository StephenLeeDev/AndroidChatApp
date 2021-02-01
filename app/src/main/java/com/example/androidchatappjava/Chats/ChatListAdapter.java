package com.example.androidchatappjava.Chats;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidchatappjava.Common.Constants;
import com.example.androidchatappjava.Common.Extras;
import com.example.androidchatappjava.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private Context context;
    private List<ChatListModel> chatListModelList;

    public ChatListAdapter(Context context, List<ChatListModel> chatListModelList) {
        this.context = context;
        this.chatListModelList = chatListModelList;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_layout, parent, false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {

        ChatListModel model = chatListModelList.get(position);
        holder.textViewFullName.setText(model.getUserName());

        StorageReference reference = FirebaseStorage.getInstance().getReference().child(Constants.getInstance().IMAGE_FOLDER + "/" + model.getPhotoName());
        reference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile).into(holder.imageViewProfile);
        });

        holder.linearLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra(Extras.getInstance().USER_KEY, model.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatListModelList.size() ;
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;
        private TextView textViewFullName, textViewLastMessage, textViewUnreadCount, textViewLastMessageTime;
        private ImageView imageViewProfile;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            textViewFullName = itemView.findViewById(R.id.textViewFullName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            textViewUnreadCount = itemView.findViewById(R.id.textViewUnreadCount);
            textViewLastMessageTime = itemView.findViewById(R.id.textViewLastMessageTime);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
        }
    }
}
