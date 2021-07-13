package com.example.microsoftengageapp2021.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.adapters.ChatAdapter;
import com.example.microsoftengageapp2021.models.User;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.example.microsoftengageapp2021.utilities.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private PreferenceManager preferenceManager;
    private LinearLayout home, prof, chat;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference reference;
    private List<User> users;
    private String userID, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        setContentView(R.layout.activity_chat);
        preferenceManager = new PreferenceManager(getApplicationContext());
        drawerLayout = findViewById(R.id.drawer_layout);
        userID = preferenceManager.getString(Constants.KEY_USER_ID);
        email = preferenceManager.getString(Constants.KEY_EMAIL);

        RecyclerView recyclerView = findViewById(R.id.userRecyclerView_c);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout_c);
        swipeRefreshLayout.setOnRefreshListener(this::getChatUsers);
        reference = FirebaseDatabase.getInstance().getReference();
        users = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(getApplicationContext(), users);
        recyclerView.setAdapter(chatAdapter);
        getChatUsers();


        findViewById(R.id.signOutBtn).setOnClickListener(v -> signOut());
        home = findViewById(R.id.homeBtn);
        prof = findViewById(R.id.profBtn);
        chat = findViewById(R.id.chatBtn);
        chat.setBackgroundResource(R.color.black);
    }

    public void clickMenu(View view) {
        if (preferenceManager.getString(Constants.KEY_EMAIL) == null) {
            Toast.makeText(this, "Anonymous User forbidden!", Toast.LENGTH_SHORT).show();
        } else {
            openDrawer(drawerLayout);
        }
    } //opens menu

    public void clickHelp(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                .setTitle("Need help?")
                .setMessage("Please send an email to \nayushr1900@gmail.com")
                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        builder.show();
    } //help icon

    public void clickLogo(View view)
    {
        Home.closeDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void clickHome(View view) {
        Home.redirectActivity(this, Home.class);
        home.setBackgroundResource(R.color.black);
        chat.setBackgroundResource(R.color.white);
        prof.setBackgroundResource(R.color.white);
    } //home tab

    public void clickProfile(View view) {
        Home.redirectActivity(this, Profile.class);
        home.setBackgroundResource(R.color.white);
        chat.setBackgroundResource(R.color.white);
        prof.setBackgroundResource(R.color.black);
    } //profile tab

    public void clickChat(View view) {
        recreate();
        home.setBackgroundResource(R.color.white);
        chat.setBackgroundResource(R.color.black);
        prof.setBackgroundResource(R.color.white);
    } //chat tab

    @Override
    protected void onPause() {
        super.onPause();
        Home.closeDrawer(drawerLayout);
    }

    private void signOut() {
        Intent intent = new Intent(getApplicationContext(), Home.class);
        intent.putExtra("SignOut", "Proceed");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void getChatUsers(){
        swipeRefreshLayout.setRefreshing(true);
        users.clear();
        reference.child(Constants.KEY_COLLECTION_USERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                swipeRefreshLayout.setRefreshing(false);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String temp = dataSnapshot.getKey();
                    if (!userID.equals(temp)) {
                        if(email!=null){
                            User user = new User();
                            user.fullName = dataSnapshot.child(Constants.KEY_FULL_NAME).getValue(String.class);
                            user.token = dataSnapshot.child(Constants.KEY_MEET_FCM_TOKEN).getValue(String.class);
                            user.email = dataSnapshot.child(Constants.KEY_EMAIL).getValue(String.class);
                            user.userID = dataSnapshot.getKey();
                            users.add(user);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    } //get chat users
}