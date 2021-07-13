package com.example.microsoftengageapp2021.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.adapters.UsersAdapter;
import com.example.microsoftengageapp2021.models.User;
import com.example.microsoftengageapp2021.receivers.UsersReceiver;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.example.microsoftengageapp2021.utilities.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Meeting extends AppCompatActivity implements UsersReceiver {
    private DrawerLayout drawerLayout;
    private PreferenceManager preferenceManager;
    private List<User> users;
    private UsersAdapter usersAdapter;
    private TextView textError, meetTitle;
    private LinearLayout home, prof, chat;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView multiCallImg;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        //hooks
        drawerLayout = findViewById(R.id.drawer_layout);
        preferenceManager = new PreferenceManager(getApplicationContext());
        home = findViewById(R.id.homeBtn);
        prof = findViewById(R.id.profBtn);
        chat = findViewById(R.id.chatBtn);
        meetTitle = findViewById(R.id.meetTitle);
        Button leaveMeetingBtn = findViewById(R.id.leaveMeetingBtn);
        Button groupChatBtn = findViewById(R.id.groupChatButton);
        Button shareMeet = findViewById(R.id.shareCodeBtn);
        multiCallImg = findViewById(R.id.multicallImg);
        Button joinCallBtn = findViewById(R.id.joinCallBtn);
        reference = FirebaseDatabase.getInstance().getReference();
        Button endRoomBtn = findViewById(R.id.endMeetingBtn);
        home.setBackgroundResource(R.color.black);
        RecyclerView usersRecyclerView = findViewById(R.id.userRecyclerView);
        textError = findViewById(R.id.displayError);

        findViewById(R.id.signOutBtn).setOnClickListener(v -> signOut());

        shareMeet.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PendingUser.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }); //share meet code button

        if (preferenceManager.getString(Constants.KEY_USER_ID).equals(
                preferenceManager.getString(Constants.KEY_HOST_ID))) {
            leaveMeetingBtn.setText("End Room");
        } //check for host

        joinCallBtn.setOnClickListener(v -> {
            String temp = preferenceManager.getString(Constants.KEY_JITSI_MEET);
            try {
                URL serverURL = new URL("https://meet.jit.si");
                JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                builder.setServerURL(serverURL);
                builder.setWelcomePageEnabled(false);
                builder.setRoom(temp);
                JitsiMeetActivity.launch(Meeting.this, builder.build());
                preferenceManager.putBoolean(Constants.KEY_IN_MEETING, true);
            } catch (Exception exception) {
                Toast.makeText(Meeting.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }); //join call button

        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);
        usersRecyclerView.setAdapter(usersAdapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);
        getUsers(); //refresh list

        leaveMeetingBtn.setOnClickListener(v -> leaveMeeting()); //leave meeting button
        endRoomBtn.setOnClickListener(v -> leaveMeeting());

        groupChatBtn.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), GroupChatActivity.class);
            i.putExtra(Constants.KEY_MEETING_ID, preferenceManager.getString(Constants.KEY_MEETING_ID));
            startActivity(i);
        }); //to open group chat
    }

    private void getUsers() {
        swipeRefreshLayout.setRefreshing(true);
        String myUserID = preferenceManager.getString(Constants.KEY_USER_ID);
        String myMeetingID = preferenceManager.getString(Constants.KEY_MEETING_ID);
        meetTitle.setText(String.format("%s\n%s", "Room Code", myMeetingID));
        DatabaseReference ref = reference.child(Constants.KEY_COLLECTION_MEETINGS).child(myMeetingID)
                .child(Constants.KEY_MEETING_USER_COLLECTION);
        ref.addValueEventListener(new ValueEventListener() {
            boolean isListening = true;
            int count = 0;

            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                users.clear();
                usersAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                if (isListening) {
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String temp = dataSnapshot.getKey();
                            if (!myUserID.equals(temp)) {
                                textError.setVisibility(View.GONE);
                                User user = new User();
                                user.fullName = dataSnapshot.child(Constants.KEY_PART_NAME).getValue(String.class);
                                user.email = dataSnapshot.child(Constants.KEY_EMAIL).getValue(String.class);
                                user.token = dataSnapshot.child(Constants.KEY_MEET_FCM_TOKEN).getValue(String.class);
                                users.add(user);
                            }
                        }
                        if (users.size() > 0) {
                            usersAdapter.notifyDataSetChanged();
                        } else {
                            textError.setText(String.format("%s", "No participants"));
                            textError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        isListening = false;
                        leaveMeeting();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(Meeting.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                preferenceManager.putBoolean(Constants.KEY_IS_JOINED, false);
                Map<String, Object> isJoined = new HashMap<>();
                isJoined.put(Constants.KEY_IS_JOINED, false);
                reference.child(Constants.KEY_COLLECTION_USERS).child(myUserID).updateChildren(isJoined);
            }
        });

    } //get users in the meeting

    public void clickMenu(View view) {
        if (preferenceManager.getString(Constants.KEY_EMAIL) == null) {
            Toast.makeText(this, "Anonymous User forbidden!", Toast.LENGTH_SHORT).show();
        } else {
            openDrawer(drawerLayout);
        }
    } //menu icon

    public void clickHelp(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                .setTitle("Need help?")
                .setMessage("Please send an email to \nayushr1900@gmail.com")
                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        builder.show();
    } //help icon

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void clickLogo(View view) {
        closeDrawer(drawerLayout);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void clickHome(View view) {
        recreate();
        home.setBackgroundResource(R.color.black);
        chat.setBackgroundResource(R.color.white);
        prof.setBackgroundResource(R.color.white);
    } //home tab

    public void clickProfile(View view) {
        redirectActivity(this, Profile.class);
        home.setBackgroundResource(R.color.white);
        chat.setBackgroundResource(R.color.white);
        prof.setBackgroundResource(R.color.black);
    } //profile tab

    public void clickChat(View view) {
        redirectActivity(Meeting.this, ChatActivity.class);
        home.setBackgroundResource(R.color.white);
        chat.setBackgroundResource(R.color.black);
        prof.setBackgroundResource(R.color.white);
    } //chat tab

    public static void redirectActivity(Activity activity, Class tempClass) {
        Intent intent = new Intent(activity, tempClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);

    }

    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }

    private void signOut() {
        if (preferenceManager.getBoolean(Constants.KEY_IS_JOINED)) {
            Toast.makeText(this, "Leave the room to sign out", Toast.LENGTH_SHORT).show();
        }
    }

    private void leaveMeeting() {
        if (preferenceManager.getBoolean(Constants.KEY_IS_JOINED)) {
            String myUserID = preferenceManager.getString(Constants.KEY_USER_ID);
            String myMeetingID = preferenceManager.getString(Constants.KEY_MEETING_ID);

            DatabaseReference temp;
            //checking for host
            if (myUserID.trim().equals(preferenceManager.getString(Constants.KEY_HOST_ID))) {
                temp = reference.child(Constants.KEY_COLLECTION_MEETINGS).child(myMeetingID);
                preferenceManager.putString(Constants.KEY_HOST_ID, null);
                reference.child(Constants.GROUP_MESSAGE_COLLECTION).child(myMeetingID).removeValue();
            }
            //checking for normal user
            else {
                temp = reference.child(Constants.KEY_COLLECTION_MEETINGS).child(myMeetingID)
                        .child(Constants.KEY_MEETING_USER_COLLECTION).child(myUserID);
            }
            Toast.makeText(Meeting.this, "Meeting ENDED!", Toast.LENGTH_SHORT).show();
            temp.removeValue()
                    .addOnSuccessListener(unused -> {
                        preferenceManager.putBoolean(Constants.KEY_IS_JOINED, false);
                        preferenceManager.putString(Constants.KEY_MEETING_ID, null);
                        preferenceManager.putBoolean(Constants.KEY_IN_MEETING, false);
                        Map<String, Object> isJoined = new HashMap<>();
                        isJoined.put(Constants.KEY_IS_JOINED, false);
                        reference.child(Constants.KEY_COLLECTION_USERS).child(myUserID).updateChildren(isJoined);

                        startActivity(new Intent(getApplicationContext(), Home.class));
                    })
                    .addOnFailureListener(e -> Toast.makeText(Meeting.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void startVideoCall(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(this, "Unable to start Video call", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingCall.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    } //initiate video call

    @Override
    public void startAudioCall(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(this, "Unable to start Audio call", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingCall.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    } //initiate audio call

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUserSelected) {
        if (isMultipleUserSelected) {
            multiCallImg.setVisibility(View.VISIBLE);
            multiCallImg.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), OutgoingCall.class);
                intent.putExtra("selectedUsers", new Gson().toJson(usersAdapter.getSelectedUsers()));
                intent.putExtra("type", "video");
                intent.putExtra("isMultiple", true);
                startActivity(intent);
            });
        } else {
            multiCallImg.setVisibility(View.GONE);
        }
    } //check if multiple users
                                                                            //are selected
}

