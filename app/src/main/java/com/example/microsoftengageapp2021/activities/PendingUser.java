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
import com.example.microsoftengageapp2021.adapters.PendingUsersAdapter;
import com.example.microsoftengageapp2021.models.User;
import com.example.microsoftengageapp2021.receivers.PendingUsersReceiver;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.example.microsoftengageapp2021.utilities.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PendingUser extends AppCompatActivity implements PendingUsersReceiver {
    private DrawerLayout drawerLayout;
    private PreferenceManager preferenceManager;
    private List<User> users;
    private PendingUsersAdapter pendingUsersAdapter;
    private TextView textError;
    private LinearLayout home, prof, chat;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView multiAddImg;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_user);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        //hooks
        drawerLayout = findViewById(R.id.drawer_layout);
        preferenceManager = new PreferenceManager(getApplicationContext());
        home = findViewById(R.id.homeBtn);
        prof = findViewById(R.id.profBtn);
        chat = findViewById(R.id.chatBtn);
        reference = FirebaseDatabase.getInstance().getReference();
        multiAddImg = findViewById(R.id.multiaddImg);
        textError = findViewById(R.id.displayError_p);
        Button goBack = findViewById(R.id.goBackBtn);
        home.setBackgroundResource(R.color.black);
        RecyclerView usersRecyclerView = findViewById(R.id.userRecyclerView_p);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout_p);

        findViewById(R.id.signOutBtn).setOnClickListener(v -> signOut()); //sign out call

        goBack.setOnClickListener(v -> {
            Intent intent = new Intent(PendingUser.this, Meeting.class);
            startActivity(intent);
        }); //go back to meeting page

        users = new ArrayList<>();
        pendingUsersAdapter = new PendingUsersAdapter(users, this);
        usersRecyclerView.setAdapter(pendingUsersAdapter);
        swipeRefreshLayout.setOnRefreshListener(this::getPendingUsers);
        users.clear();
        getPendingUsers();
    }

    private void getPendingUsers() {
        swipeRefreshLayout.setRefreshing(true);

        reference.child(Constants.KEY_COLLECTION_USERS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        users.clear();
                        pendingUsersAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.child(Constants.KEY_IS_JOINED).exists()) {
                                if (!dataSnapshot.child(Constants.KEY_IS_JOINED)
                                        .getValue(Boolean.class)) {
                                    User user = new User();
                                    user.userID = dataSnapshot.getKey();
                                    user.fullName = dataSnapshot.child(Constants.KEY_FULL_NAME)
                                            .getValue(String.class);
                                    user.email = dataSnapshot.child(Constants.KEY_EMAIL)
                                            .getValue(String.class);
                                    user.token = dataSnapshot.child(Constants.KEY_FCM_TOKEN)
                                            .getValue(String.class);
                                    users.add(user);
                                }
                                if (users.size() > 0) {
                                    textError.setVisibility(View.GONE);
                                    pendingUsersAdapter.notifyDataSetChanged();
                                } else {
                                    textError.setText(String.format("%s", "No Pending User"));
                                    textError.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(PendingUser.this, error.getMessage(),
                                Toast.LENGTH_SHORT).show();

                    }
                });
    } //get pending users list

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
        redirectActivity(this, ChatActivity.class);
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

    @Override
    public void sendInvitation(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(this, "User Offline", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), SendingInvitation.class);
            intent.putExtra("user", user);
            startActivity(intent);
        }
    } //send direct invitation to user

    public void sendRoomCode(User user) {
        if (user == null) {
            Toast.makeText(this, "Failed to send message!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("meetCode", preferenceManager.getString(Constants.KEY_JITSI_MEET));
            intent.putExtra("roomCode", preferenceManager.getString(Constants.KEY_MEETING_ID));
            intent.putExtra(Constants.KEY_MEETING_PASS, preferenceManager
                    .getString(Constants.KEY_MEETING_PASS));
            intent.putExtra("source", "pendingUser");
            startActivity(intent);
            Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show();
        }
    } // send message invite to user

    @Override
    public void onMultiplePendingUsersAction(Boolean isMultiplePendingUserSelected) {
        if (isMultiplePendingUserSelected) {
            multiAddImg.setVisibility(View.VISIBLE);
            multiAddImg.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), SendingInvitation.class);
                intent.putExtra("selectedPendingUsers", new Gson().toJson(
                        pendingUsersAdapter.getSelectedPendingUsers()));
                intent.putExtra("isMultipleP", true);
                startActivity(intent);
            });
        } else {
            multiAddImg.setVisibility(View.GONE);
        }

    }
    //check if multiple users are selected

}

