package com.example.microsoftengageapp2021.activities;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.adapters.GroupMessageAdapter;
import com.example.microsoftengageapp2021.models.GroupChat;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    private TextView fullName, firstChar;
    private EditText textContent;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private GroupMessageAdapter groupMessageAdapter;
    private List<GroupChat> groupChatList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        //hooks
        fullName = findViewById(R.id.chatFullName);
        firstChar = findViewById(R.id.chatFirstChar);
        ImageView goBackBtn = findViewById(R.id.goBack);
        ImageView messageSendBtn = findViewById(R.id.sendBtn);
        textContent = findViewById(R.id.textContent);
        recyclerView = findViewById(R.id.messageRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        String meetingID = getIntent().getStringExtra(Constants.KEY_MEETING_ID);
        reference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        reference.child(Constants.GROUP_MESSAGE_COLLECTION).child(meetingID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                fullName.setText(String.format("%s %s", "Room:", meetingID));
                firstChar.setText(meetingID.substring(0, 1));
                readMessage(meetingID);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(GroupChatActivity.this, error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }); //create group chat header

        groupMessageAdapter = new GroupMessageAdapter(GroupChatActivity.this, groupChatList);
        recyclerView.setAdapter(groupMessageAdapter);
        goBackBtn.setOnClickListener(v -> onBackPressed());

        messageSendBtn.setOnClickListener(v -> {
            String msg = textContent.getText().toString();
            if (!msg.equals("")) {
                sendMessage(firebaseUser.getUid(), msg, meetingID);
            } else {
                Toast.makeText(GroupChatActivity.this, "Empty Message",
                        Toast.LENGTH_SHORT).show();
            }
            textContent.setText("");
        }); //send message
    }

    private void sendMessage(String sender, String message, String meetID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("message", message);

        reference.child(Constants.GROUP_MESSAGE_COLLECTION).child(meetID).push().setValue(hashMap);
    }  //send message fn

    private void readMessage(String meetID) {
        reference.child(Constants.GROUP_MESSAGE_COLLECTION).child(meetID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                groupChatList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    GroupChat groupChat = dataSnapshot.getValue(GroupChat.class);
                    groupChatList.add(groupChat);
                    groupMessageAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(GroupChatActivity.this, error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    } //read message function
}