package com.example.androidchatappjava.Chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidchatappjava.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messageList;
    private FirebaseAuth firebaseAuth;

    public MessageAdapter(Context context, List<MessageModel> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel model = messageList.get(position);
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUser = firebaseAuth.getCurrentUser().getUid();
        String fromUser = model.getMessageFrom();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        String dateTime = simpleDateFormat.format(new Date(model.getMessageTime()));
        String [] splitString = dateTime.split(" ");
        String messageTime = splitString[1];

        if (fromUser.equals(currentUser)) {
            holder.linearLayoutSent.setVisibility(View.VISIBLE);
            holder.linearLayoutReceived.setVisibility(View.GONE);
            holder.textViewSentMessage.setText(model.getMessage());
            holder.textViewSentMessageTime.setText(model.getMessageTime());
        } else {
            holder.linearLayoutSent.setVisibility(View.GONE);
            holder.linearLayoutReceived.setVisibility(View.VISIBLE);
            holder.textViewReceivedMessage.setText(model.getMessage());
            holder.textViewReceivedMessageTime.setText(model.getMessageTime());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutSent, linearLayoutReceived;
        private TextView textViewSentMessage, textViewSentMessageTime, textViewReceivedMessage, textViewReceivedMessageTime;
        private ConstraintLayout constraintLayoutMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutSent = itemView.findViewById(R.id.linearLayoutSent);
            linearLayoutReceived = itemView.findViewById(R.id.linearLayoutReceived);
            textViewSentMessage = itemView.findViewById(R.id.textViewSentMessage);
            textViewSentMessageTime = itemView.findViewById(R.id.textViewSentMessageTime);
            textViewReceivedMessage = itemView.findViewById(R.id.textViewReceivedMessage);
            textViewReceivedMessageTime = itemView.findViewById(R.id.textViewReceivedMessageTime);
            constraintLayoutMessage = itemView.findViewById(R.id.constraintLayoutMessage);
        }
    }
}
