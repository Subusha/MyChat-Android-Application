package com.pkd.mychat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pkd.mychat.Adapter.MessageAdapter;
import com.pkd.mychat.Model.Chat;
import com.pkd.mychat.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    Intent intent;
    TextView username, btnSend;
    EditText typingBox;
    RecyclerView recyclerView;
    CircleImageView profileImage;
    ValueEventListener lastSeenListener;

    FirebaseUser authUser;
    DatabaseReference userReference;
    DatabaseReference chatReference;

    MessageAdapter messageAdapter;
    List<Chat> chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.tbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        chatReference = FirebaseDatabase.getInstance().getReference("Chats");

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.user);
        btnSend = findViewById(R.id.send);
        typingBox = findViewById(R.id.msg);

        recyclerView = findViewById(R.id.recycle_view_chats);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        final String userId = intent.getStringExtra("userId");
        loadUserTitleAndImage(userId);

        btnSend.setOnClickListener(view -> {

            String msg = typingBox.getText().toString();
            if (!msg.equals("")){
                sendMessage(authUser.getUid(),userId, msg);
                btnSend.setBackgroundResource(R.drawable.msg_delivered_bg);
            }else {
                Toast.makeText(getApplicationContext(),"Cannot Send Empty Message.",Toast.LENGTH_SHORT).show();
                btnSend.setBackgroundResource(R.drawable.msg_info_bg);
            }
            typingBox.setText("");
        });

        seenMessage(userId);

    }

    private void loadUserTitleAndImage(String userId) {
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        Map<Integer, String> userImageMap = new HashMap<>();

        userReference.child(authUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userImageMap.put(0, (String) dataSnapshot.child("imageURL").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getUsername());
                userImageMap.put(1, user.getImageURL());

                if (user.getImageURL().equals("default")){
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }

                readMessages(authUser.getUid(), userId, userImageMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(final String userId){
        lastSeenListener = chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(authUser.getUid()) && chat.getSender().equals(userId)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("seen",true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext()," lastSeen fetching failed!",Toast.LENGTH_SHORT).show();
                Log.e("MESSAGE ACTIVITY", " lastSeen fetching failed!, ERROR: "+ databaseError.getMessage());
            }
        });
    }

    public void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("seen",false);
        hashMap.put("message",message);
        hashMap.put("receiver",receiver);
        hashMap.put("sender",sender);
        hashMap.put("createdAt",ServerValue.TIMESTAMP);
        hashMap.put("lastSeenAt", null);

        reference.child("Chats").push().setValue(hashMap);
    }

    public void readMessages(final String myId, final String userId, final Map<Integer, String> userImageMap){
        chats = new ArrayList<>();

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    assert chat != null;
                    chat.setId(ds.getKey());
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        chats.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, chats, userImageMap);
                    recyclerView.setAdapter(messageAdapter);
                    btnSend.setBackgroundResource(R.drawable.msg_info_bg);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void status(String status){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        userReference.child(authUser.getUid()).updateChildren(hashMap);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            int position = messageAdapter.getContextMenuPosition();
            chats.remove(position);
            messageAdapter.notifyItemRemoved(position);
            return true;

        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Message deleting Failed! ER: "+e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("MESSAGE ACTIVITY", "message deleting failed!, ERROR: "+ e.getMessage());
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatReference.removeEventListener(lastSeenListener);
        status("offline");
    }
}
