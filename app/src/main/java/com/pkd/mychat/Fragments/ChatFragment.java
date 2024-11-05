package com.pkd.mychat.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pkd.mychat.Adapter.UserAdapter;
import com.pkd.mychat.Model.Chat;
import com.pkd.mychat.Model.User;
import com.pkd.mychat.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatFragment extends Fragment {

    private FirebaseUser authUser;
    private DatabaseReference userReference;
    private Set<User> friends;
    private UserAdapter userAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chats");

        authUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_friends);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        friends = new HashSet<>();
        userAdapter  = new UserAdapter(getContext(), new ArrayList<>(friends),true);
        recyclerView.setAdapter(userAdapter);

        chatReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat != null && (chat.getSender().equals(authUser.getUid()) || chat.getReceiver().equals(authUser.getUid()))){
                        String friendId = chat.getSender().equals(authUser.getUid()) ? chat.getReceiver() : chat.getSender();

                        DatabaseReference friendUserReference = userReference.child(friendId);
                        friendUserReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                assert user != null;
                                friends.add(user);
                                userAdapter.setData(new ArrayList<>(friends));
                                userAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getActivity(),friendId+" data fetching failed!",Toast.LENGTH_SHORT).show();
                                Log.e("CHAT FRAGMENT", friendId+" data fetching failed!, ERROR: "+ databaseError.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(),"messages fetching failed!",Toast.LENGTH_SHORT).show();
                Log.e("CHAT FRAGMENT", "messages fetching failed!, ERROR: "+ databaseError.getMessage());
            }
        });

        return view;
    }

}
