package com.example.androidchatappjava.Chats;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidchatappjava.Common.Constants;
import com.example.androidchatappjava.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messageList;
    private FirebaseAuth firebaseAuth;

    private ActionMode actionMode;
    private ConstraintLayout constraintLayoutSelectedView;

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
        MessageModel message = messageList.get(position);
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUser = firebaseAuth.getCurrentUser().getUid();
        String fromUser = message.getMessageFrom();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        String dateTime = simpleDateFormat.format(new Date(message.getMessageTime()));
        String [] splitString = dateTime.split(" ");
        String messageTime = splitString[1];

        if(fromUser.equals(currentUser)){
            if(message.getMessageType().equals(Constants.getInstance().MESSAGE_TYPE_TEXT)) {
                holder.linearLayoutSent.setVisibility(View.VISIBLE);
                holder.linearLayoutSentImage.setVisibility(View.GONE);
            } else {
                holder.linearLayoutSent.setVisibility(View.GONE);
                holder.linearLayoutSentImage.setVisibility(View.VISIBLE);
            }

            holder.linearLayoutReceived.setVisibility(View.GONE);
            holder.linearLayoutReceivedImage.setVisibility(View.GONE);

            holder.textViewSentMessage.setText(message.getMessage());
            holder.textViewSentMessageTime.setText(messageTime);
            holder.textViewSentImageTime.setText(messageTime);
            Glide.with(context).load(message.getMessage()).placeholder(R.drawable.ic_image).into(holder.imageViewSent);
        } else {
            if(message.getMessageType().equals(Constants.getInstance().MESSAGE_TYPE_TEXT)) {
                holder.linearLayoutReceived.setVisibility(View.VISIBLE);
                holder.linearLayoutReceivedImage.setVisibility(View.GONE);
            } else {
                holder.linearLayoutReceived.setVisibility(View.GONE);
                holder.linearLayoutReceivedImage.setVisibility(View.VISIBLE);
            }

            holder.linearLayoutSent.setVisibility(View.GONE);
            holder.linearLayoutSentImage.setVisibility(View.GONE);

            holder.textViewReceivedMessage.setText(message.getMessage());
            holder.textViewReceivedMessageTime.setText(messageTime);
            holder.textViewReceivedImageTime.setText(messageTime);

            Glide.with(context).load(message.getMessage()).placeholder(R.drawable.ic_image).into(holder.imageViewReceived);
        }

        holder.constraintLayoutMessage.setTag(R.id.TAG_MESSAGE, message.getMessage());
        holder.constraintLayoutMessage.setTag(R.id.TAG_MESSAGE_ID, message.getMessageId());
        holder.constraintLayoutMessage.setTag(R.id.TAG_MESSAGE_TYPE, message.getMessageType());

        holder.constraintLayoutMessage.setOnClickListener(view -> {
            String messageType = view.getTag(R.id.TAG_MESSAGE_TYPE).toString();
            Uri uri = Uri.parse(view.getTag(R.id.TAG_MESSAGE).toString());
            if(messageType.equals(Constants.getInstance().MESSAGE_TYPE_VIDEO)) {
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                intent.setDataAndType(uri, "video/mp4");
                context.startActivity(intent);
            } else if(messageType.equals(Constants.getInstance().MESSAGE_TYPE_IMAGE)){
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                intent.setDataAndType(uri, "image/jpg");
                context.startActivity(intent);
            }
        });

        holder.constraintLayoutMessage.setOnLongClickListener(v -> {
            if (actionMode != null) {
                return false;
            }

            constraintLayoutSelectedView = holder.constraintLayoutMessage;
            actionMode = ((AppCompatActivity)context).startSupportActionMode(actionModeCallback);
            holder.constraintLayoutMessage.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutSent, linearLayoutReceived, linearLayoutSentImage, linearLayoutReceivedImage;
        private TextView textViewSentMessage, textViewSentMessageTime, textViewReceivedMessage, textViewReceivedMessageTime, textViewSentImageTime, textViewReceivedImageTime;
        private ImageView imageViewSent, imageViewReceived;
        private ConstraintLayout constraintLayoutMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutSent = itemView.findViewById(R.id.linearLayoutSent);
            linearLayoutSentImage = itemView.findViewById(R.id.linearLayoutSentImage);
            linearLayoutReceived = itemView.findViewById(R.id.linearLayoutReceived);
            linearLayoutReceivedImage = itemView.findViewById(R.id.linearLayoutReceivedImage);
            textViewSentMessage = itemView.findViewById(R.id.textViewSentMessage);
            textViewSentMessageTime = itemView.findViewById(R.id.textViewSentMessageTime);
            textViewReceivedMessage = itemView.findViewById(R.id.textViewReceivedMessage);
            textViewReceivedMessageTime = itemView.findViewById(R.id.textViewReceivedMessageTime);
            textViewSentImageTime = itemView.findViewById(R.id.textViewSentImageTime);
            textViewReceivedImageTime = itemView.findViewById(R.id.textViewReceivedImageTime);
            imageViewSent = itemView.findViewById(R.id.imageViewSent);
            imageViewReceived = itemView.findViewById(R.id.imageViewReceived);
            constraintLayoutMessage = itemView.findViewById(R.id.constraintLayoutMessage);
        }
    }

    public ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_chat_options, menu);

            String selectedMessageType = String.valueOf(constraintLayoutSelectedView.getTag(R.id.TAG_MESSAGE_TYPE));
            if(selectedMessageType.equals(Constants.getInstance().MESSAGE_TYPE_TEXT))
            {
                MenuItem itemDownload = menu.findItem(R.id.menuDownload);
                itemDownload.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();

            String selectedMessageId = String.valueOf(constraintLayoutSelectedView.getTag(R.id.TAG_MESSAGE_ID));
            String selectedMessage = String.valueOf(constraintLayoutSelectedView.getTag(R.id.TAG_MESSAGE));
            String selectedMessageType = String.valueOf(constraintLayoutSelectedView.getTag(R.id.TAG_MESSAGE_TYPE));

            switch (itemId)
            {
                case  R.id.menuDelete:
                    if(context instanceof  ChatActivity) {
                        ((ChatActivity)context).deleteMessage(selectedMessageId, selectedMessageType);
                    }
                    actionMode.finish();
                    break;
                case  R.id.menuDownload:
                    Toast.makeText(context, "menuDownload", Toast.LENGTH_SHORT).show();
                    actionMode.finish();
                    break;
                case  R.id.menuShare:
                    Toast.makeText(context, "menuShare", Toast.LENGTH_SHORT).show();
                    actionMode.finish();
                    break;
                case  R.id.menuForward:
                    Toast.makeText(context, "menuForward", Toast.LENGTH_SHORT).show();
                    actionMode.finish();
                    break;

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mode = null;
            constraintLayoutSelectedView.setBackgroundColor(context.getResources().getColor(R.color.chat_background));

        }
    };
}
