package com.example.microsoftengageapp2021.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.example.microsoftengageapp2021.utilities.PreferenceManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Home extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private PreferenceManager preferenceManager;
    private LinearLayout home, prof, chat;
    private Button startMeetingBtn;
    private TextInputLayout meetingCode;
    public String MeetCode, userID;
    private DatabaseReference reference;
    private ProgressBar startMeetProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        //hooks
        drawerLayout = findViewById(R.id.drawer_layout);
        home = findViewById(R.id.homeBtn);
        prof = findViewById(R.id.profBtn);
        chat = findViewById(R.id.chatBtn);
        TextView fNameLabel = findViewById(R.id.textFullName);
        TextView firstChar = findViewById(R.id.firstChar);
        startMeetingBtn = findViewById(R.id.startMeetingBtn);
        Button joinMeetingBtn = findViewById(R.id.joinMeetingBtn);
        meetingCode = findViewById(R.id.meetingCode);
        Button linkAccountBtn = findViewById(R.id.linkAccBtn);
        Button signOutAno = findViewById(R.id.signOutAno);
        TextView accNotLinkedErr = findViewById(R.id.accNotLinkedText);
        startMeetProgress = findViewById(R.id.startMeetingProgressBar);
        preferenceManager = new PreferenceManager(getApplicationContext());
        reference = FirebaseDatabase.getInstance().getReference();
        userID = preferenceManager.getString(Constants.KEY_USER_ID);
        home.setBackgroundResource(R.color.black);
        String email = preferenceManager.getString(Constants.KEY_EMAIL);
        String name = preferenceManager.getString(Constants.KEY_FULL_NAME);

        if (email == null) {
            if (name == null) {
                fNameLabel.setText("Anonymous");
                firstChar.setText("A");
                AlertDialog.Builder inputName = new AlertDialog.Builder(Home.this);
                inputName.setTitle("Display Name");
                final EditText temp = new EditText(getApplicationContext());
                temp.setInputType(InputType.TYPE_CLASS_TEXT);
                inputName.setView(temp);
                inputName.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferenceManager.putString(Constants.KEY_FULL_NAME, temp.getText().toString());
                        Map<String, Object> update = new HashMap<>();
                        update.put(Constants.KEY_FULL_NAME, temp.getText().toString());
                        update.put(Constants.KEY_IS_JOINED, false);
                        reference.child(Constants.KEY_COLLECTION_USERS).child(userID)
                                .updateChildren(update);
                        recreate();
                    }
                });
                inputName.show();
            } else {
                fNameLabel.setText(preferenceManager.getString(Constants.KEY_FULL_NAME));
                firstChar.setText(preferenceManager.getString(Constants.KEY_FULL_NAME).substring(0, 1));
            }

        } //check for anonymous user
        else {
            fNameLabel.setText(preferenceManager.getString(Constants.KEY_FULL_NAME));
            firstChar.setText(preferenceManager.getString(Constants.KEY_FULL_NAME).substring(0, 1));
            linkAccountBtn.setVisibility(View.INVISIBLE);
            signOutAno.setVisibility(View.INVISIBLE);
            accNotLinkedErr.setVisibility(View.INVISIBLE);
        } //verified user

        signOutAno.setOnClickListener(v -> new AlertDialog.Builder(Home.this)
                .setTitle("ALERT!").setMessage("You will loose all your data. Do you want " +
                        "to continue?").setPositiveButton("Yes", (dialog, which) -> signOut())
                .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show());
        //confirm sign out

        linkAccountBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Signup.class);
            intent.putExtra("source", "connect");
            startActivity(intent);
        }); //link account

        findViewById(R.id.signOutBtn).setOnClickListener(v -> signOut());

        startMeetingBtn.setOnClickListener(v -> {
            startMeetingBtn.setVisibility(View.INVISIBLE);
            startMeetProgress.setVisibility(View.VISIBLE);
            createPass();
        }); //start meeting button

        joinMeetingBtn.setOnClickListener(v -> {
            MeetCode = meetingCode.getEditText().getText().toString();
            joinMeeting(MeetCode);
        }); //join meeting button

        if (preferenceManager.getBoolean(Constants.KEY_IS_JOINED)) {
            Intent intent = new Intent(getApplicationContext(), Meeting.class);
            startActivity(intent);
            finish();
        } //check if user is in
                                                                        // a meeting

        String codeReceived = getIntent().getStringExtra(Constants.REMOTE_MSG_WAITING_ROOM);
        String source = getIntent().getStringExtra("source");
        if (codeReceived != null) {
            if(source!=null&&source.equals("invite"))
            {
                joinMeeting(codeReceived); //direct invite (no password required)
            }
            else{
                checkMeeting(codeReceived); //message invite (password required)
            }
        } //check for requests to join meeting

        String signOut = getIntent().getStringExtra("SignOut");
        if (signOut != null && signOut.equals("Proceed")) {
            signOut();
        } //check if sign out is requested
        else {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String temp = task.getResult();
                    sendFCMToken(temp);
                }
            });
        } // update FCM token
    }

    private void checkPass(String codeReceived) {
        AlertDialog.Builder inputPass = new AlertDialog.Builder(Home.this);
        inputPass.setTitle("Enter Password");
        final EditText pass = new EditText(getApplicationContext());
        pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputPass.setView(pass);
        inputPass.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reference.child(Constants.KEY_COLLECTION_MEETINGS).child(codeReceived)
                        .child("Host").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (pass.getText().toString().equals(snapshot.child("Password").getValue(String.class))) {
                            joinMeeting(codeReceived);
                        } else {
                            Toast.makeText(Home.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(Home.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        inputPass.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        inputPass.show();

    } //check password for meeting

    private void createPass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Password?").setMessage("Would you like to set up a password?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder inputPass = new AlertDialog.Builder(Home.this);
                        inputPass.setTitle("Create Password");
                        final EditText pass = new EditText(getApplicationContext());
                        pass.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputPass.setView(pass);
                        inputPass.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startMeeting(pass.getText().toString());
                            }
                        });
                        inputPass.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startMeetingBtn.setVisibility(View.VISIBLE);
                                startMeetProgress.setVisibility(View.INVISIBLE);
                                dialog.cancel();
                            }
                        });
                        inputPass.show();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startMeeting(null);
                dialog.cancel();
            }
        });
        builder.show();
    } //create password for meeting

    private void startMeeting(String pass) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder code = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            code.append(AlphaNumericString
                    .charAt(index));
        }
        MeetCode = code.toString();
        String jitsiID = preferenceManager.getString(Constants.KEY_USER_ID) + "_" +
                UUID.randomUUID().toString().substring(0, 5);
        HashMap<String, Object> meeting = new HashMap<>();
        String hostID = preferenceManager.getString(Constants.KEY_USER_ID);
        meeting.put("Host Name", preferenceManager.getString(Constants.KEY_FULL_NAME));
        meeting.put(Constants.KEY_HOST_ID, hostID);
        meeting.put(Constants.KEY_JITSI_MEET, jitsiID);
        if (pass != null) {
            meeting.put("Password", pass);
            preferenceManager.putString(Constants.KEY_MEETING_PASS, pass);
        }
        reference.child(Constants.KEY_COLLECTION_MEETINGS).child(MeetCode).child("Host")
                .setValue(meeting).addOnSuccessListener(unused -> {
            preferenceManager.putString(Constants.KEY_HOST_ID, hostID);
            preferenceManager.putString(Constants.KEY_JITSI_MEET, jitsiID);
            joinMeeting(MeetCode);
        })
                .addOnFailureListener(e ->
                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    } //start meeting

    public void checkMeeting(String meetCode) {
        reference.child(Constants.KEY_COLLECTION_MEETINGS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if (snapshot.hasChild(meetCode)) {
                    if (snapshot.child(meetCode).child("Host").hasChild("Password")) {
                        checkPass(meetCode);
                    } else {
                        joinMeeting(meetCode);
                    }
                } else {
                    Toast.makeText(Home.this, "Meeting does not exist!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    } //check if meeting exits

    public void joinMeeting(String meetCode) {
        String userID = preferenceManager.getString(Constants.KEY_USER_ID);
        String userName = preferenceManager.getString(Constants.KEY_FULL_NAME);
        String fcmToken = preferenceManager.getString(Constants.KEY_FCM_TOKEN);
        String email = preferenceManager.getString(Constants.KEY_EMAIL);
        HashMap<String, Object> userList = new HashMap<>();
        userList.put(Constants.KEY_PART_NAME, userName);
        userList.put(Constants.KEY_EMAIL, email);
        userList.put(Constants.KEY_MEET_FCM_TOKEN, fcmToken);
        reference.child(Constants.KEY_COLLECTION_MEETINGS).child(meetCode)
                .child(Constants.KEY_MEETING_USER_COLLECTION).child(userID)
                .setValue(userList).addOnSuccessListener(unused -> {
            preferenceManager.putString(Constants.KEY_MEETING_ID, meetCode);
            preferenceManager.putString(Constants.KEY_PART_NAME, userName);
            preferenceManager.putBoolean(Constants.KEY_IS_JOINED, true);
            preferenceManager.putString(Constants.KEY_MEET_FCM_TOKEN, fcmToken);
            Toast.makeText(Home.this, "Joining Room", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), Meeting.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            Map<String, Object> isJoined = new HashMap<>();
            isJoined.put(Constants.KEY_IS_JOINED, true);
            reference.child(Constants.KEY_COLLECTION_USERS).child(userID).updateChildren(isJoined);

            startActivity(intent);
        }).addOnFailureListener(e -> Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        reference.child(Constants.KEY_COLLECTION_MEETINGS).child(meetCode).child("Host")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        preferenceManager.putString(Constants.KEY_JITSI_MEET,
                                snapshot.child(Constants.KEY_JITSI_MEET).getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(Home.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        startMeetingBtn.setVisibility(View.VISIBLE);
        startMeetProgress.setVisibility(View.INVISIBLE);
    } //join meeting

    public void clickMenu(View view) {
        if (preferenceManager.getString(Constants.KEY_EMAIL) == null) {
            Toast.makeText(this, "Anonymous User forbidden!", Toast.LENGTH_SHORT).show();
        } else {
            openDrawer(drawerLayout);
        }
    } //menu icon

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void clickLogo(View view) {
        closeDrawer(drawerLayout);
    } //logo icon

    public void clickHelp(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                .setTitle("Need help?")
                .setMessage("Please send an email to \nayushr1900@gmail.com")
                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        builder.show();
    } //help icon

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

    private void sendFCMToken(String token) {
        Map<String, Object> updateFCM = new HashMap<>();
        updateFCM.put(Constants.KEY_FCM_TOKEN, token);
        reference.child(Constants.KEY_COLLECTION_USERS).child(userID)
                .updateChildren(updateFCM)
                .addOnFailureListener(e -> Toast.makeText(Home.this, "Token failed!",
                        Toast.LENGTH_SHORT).show());
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
    } //update FCM token onto database

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        if (preferenceManager.getBoolean(Constants.KEY_IS_ANO)) {
            reference.child(Constants.KEY_COLLECTION_USERS).child(userID).removeValue();
        } else {
            reference.child(Constants.KEY_COLLECTION_USERS).child(userID)
                    .child(Constants.KEY_FCM_TOKEN).removeValue();
        }
        Toast.makeText(this, "Signing out", Toast.LENGTH_SHORT).show();
        preferenceManager.clearPreference();
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
    }
}

