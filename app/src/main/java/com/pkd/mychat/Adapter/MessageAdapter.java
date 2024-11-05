package com.pkd.mychat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.pkd.mychat.Model.Chat;
import com.pkd.mychat.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    public static final int MSG_FROM_ME = 0;
    public static final int MSG_FROM_FRIEND = 1;

    private final Context context;
    private final List<Chat> chats;
    private final Map<Integer, String> userImageMap;
    private int contextMenuPosition;

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat timeFormat = new SimpleDateFormat(" HH:mm", Locale.getDefault());
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
    FirebaseUser authUser;

    public MessageAdapter(Context context, List<Chat> chats, Map<Integer, String> userImageMap){
        this.context = context;
        this.chats = chats;
        this.userImageMap = userImageMap;
    }

    public int getContextMenuPosition() {
        return contextMenuPosition;
    }

    public void setContextMenuPosition(int contextMenuPosition) {
        this.contextMenuPosition = contextMenuPosition;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_FROM_ME) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_me_layout, parent, false);
            return new MessageAdapter.ViewHolder(view, MSG_FROM_ME);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.chat_friend_layout, parent, false);
            return new MessageAdapter.ViewHolder(view, MSG_FROM_FRIEND);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = chats.get(position);
        holder.message.setText(chat.getMessage());
        holder.messageId = chat.getId();

        String imageUrl = userImageMap.get(getItemViewType(position));
        String messageDeliverTime = formatDateRelativeToToday(new Date(chat.getCreatedAt()))
                + timeFormat.format(new Date(chat.getCreatedAt()));
        String messageSeenTime = chat.getLastSeenAt() != null
                ? (formatDateRelativeToToday(new Date(chat.getLastSeenAt()))
                + timeFormat.format(new Date(chat.getLastSeenAt())))
                : "Yet to seen";

        StringBuilder messageInfoBuilder = new StringBuilder();
        messageInfoBuilder.append("Delivered: "+ messageDeliverTime);
        messageInfoBuilder.append("\n");
        messageInfoBuilder.append("Seen: "+ messageSeenTime);
        holder.messageInfo = messageInfoBuilder.toString();

        if (imageUrl.equals("default")){
            holder.userImage.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(context).load(imageUrl).into(holder.userImage);
        }

        if (getItemViewType(position) == MSG_FROM_ME && position == getItemCount()-1){
            if(chat.isSeen()){
                holder.messageStatus.setText(messageDeliverTime);
                holder.messageStatus.setVisibility(View.VISIBLE);
                holder.icSeen.setVisibility(View.VISIBLE);
                holder.icDelivered.setVisibility(View.GONE);

            }else {
                holder.messageStatus.setText(messageDeliverTime);
                holder.messageStatus.setVisibility(View.VISIBLE);
                holder.icDelivered.setVisibility(View.VISIBLE);
                holder.icSeen.setVisibility(View.GONE);

            }
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public TextView message;
        public ImageView userImage;
        public TextView messageStatus;
        public ImageView icDelivered;
        public ImageView icSeen;

        public String messageId;
        public String messageInfo;

        public ViewHolder(View itemView, int type){
            super(itemView);
            message = itemView.findViewById(R.id.show_message);
            userImage = itemView.findViewById(R.id.profile_image);
            messageStatus = itemView.findViewById(R.id.msg_status);
            icDelivered = itemView.findViewById(R.id.ic_delivered);
            icSeen = itemView.findViewById(R.id.ic_seen);

            if (type == MSG_FROM_ME){
                messageStatus.setVisibility(View.GONE);
                icDelivered.setVisibility(View.GONE);
                icSeen.setVisibility(View.GONE);

                message.setOnCreateContextMenuListener(this);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {

            menu.add(0, view.getId(), 0, "Info")
                    .setOnMenuItemClickListener(listener -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Message Info")
                                .setMessage(messageInfo).create();
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        return true;
                    });

            menu.add(0, view.getId(), 0, "Delete")
                    .setOnMenuItemClickListener(listener -> {
                        FirebaseDatabase.getInstance().getReference("Chats").child(messageId).removeValue();
                        setContextMenuPosition(getAdapterPosition());
                        return true;
                    });
        }
    }

    @Override
    public int getItemViewType(int position) {
        authUser = FirebaseAuth.getInstance().getCurrentUser();

        if (chats.get(position).getSender().equals(authUser.getUid())){
            return MSG_FROM_ME;
        }else {
            return MSG_FROM_FRIEND;
        }
    }

    private String formatDateRelativeToToday(Date date) {
        calendar.setTime(date);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        if (calendar.before(yesterday)) {
            return dateFormat.format(calendar.getTime());

        } else if (calendar.before(today)) {
            return "Yesterday";

        } else {
            return "Today";
        }
    }
}