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
import com.example.microsoftengageapp2021.adapters.MessageAdapter;
import com.example.microsoftengageapp2021.models.Chat;
import com.example.microsoftengageapp2021.models.User;
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

public class MessagingActivity extends AppCompatActivity {

    private TextView fullName, firstChar;
    private EditText textContent;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private MessageAdapter messageAdapter;
    private final List<Chat> chats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(getIntent().getStringExtra("source")!=null){
            User user = (User)getIntent().getSerializableExtra("user");
            String message = "https://meet.jit.si/" + getIntent().getStringExtra("roomCode")
            + "/" + getIntent().getStringExtra("meetCode");
            sendMessage(firebaseUser.getUid(), user.userID, message);
            message = "Click the message above to join the room!";
            sendMessage(firebaseUser.getUid(), user.userID, message);
            if(getIntent().getStringExtra(Constants.KEY_MEETING_PASS)!=null)
            {
                message = "Password: " + getIntent().getStringExtra(Constants.KEY_MEETING_PASS);
                sendMessage(firebaseUser.getUid(), user.userID, message);
            }
            finish();

        } //check if called to invite users
        else {
            setContentView(R.layout.activity_messaging);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

            //hooks
            fullName = findViewById(R.id.chatFullName);
            firstChar = findViewById(R.id.chatFirstChar);
            ImageView goBackBtn = findViewById(R.id.goBack);
            ImageView messageSendBtn = findViewById(R.id.sendBtn);
            textContent = findViewById(R.id.textContent);
            RecyclerView recyclerView = findViewById(R.id.messageRecyclerView);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            String userID = getIntent().getStringExtra("userID");
            reference = FirebaseDatabase.getInstance().getReference();

            reference.child(Constants.KEY_COLLECTION_USERS).child(userID)
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    String temp = snapshot.child(Constants.KEY_FULL_NAME).getValue(String.class);
                    if(temp==null){
                        fullName.setText("Unknown");
                        firstChar.setText("#");
                    } else{
                        fullName.setText(temp);
                        firstChar.setText(temp.substring(0, 1));
                    }
                    readMessage(firebaseUser.getUid(), userID);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(MessagingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }); //check for messages

            messageAdapter = new MessageAdapter(MessagingActivity.this, chats);
            recyclerView.setAdapter(messageAdapter);
            goBackBtn.setOnClickListener(v -> onBackPressed());

            messageSendBtn.setOnClickListener(v -> {
                String msg = textContent.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(firebaseUser.getUid(), userID, msg);
                } else {
                    Toast.makeText(MessagingActivity.this, "Empty Message",
                            Toast.LENGTH_SHORT).show();
                }
                textContent.setText("");
            }); //send message
        }
    }

    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Messages").push().setValue(hashMap);
    } //send message fn

    private void readMessage(String myUserID, String userID){
        reference.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                chats.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myUserID)&&chat.getSender().equals(userID) ||
                            chat.getReceiver().equals(userID)&&chat.getSender().equals(myUserID)){
                        chats.add(chat);
                        messageAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(MessagingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    } //read message function
}