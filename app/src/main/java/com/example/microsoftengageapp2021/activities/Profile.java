package com.example.microsoftengageapp2021.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.example.microsoftengageapp2021.utilities.PreferenceManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private PreferenceManager preferenceManager;
    private LinearLayout home, prof, chat;
    private TextView fNameLabel, firstChar;
    private TextInputLayout fName, contact;
    private DatabaseReference reference;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        setContentView(R.layout.activity_profile);

        //hooks
        drawerLayout = findViewById(R.id.drawer_layout);
        preferenceManager = new PreferenceManager(getApplicationContext());
        reference = FirebaseDatabase.getInstance().getReference();
        home = findViewById(R.id.homeBtn);
        prof = findViewById(R.id.profBtn);
        chat = findViewById(R.id.chatBtn);
        fNameLabel = findViewById(R.id.textFullName);
        firstChar = findViewById(R.id.firstChar);
        fName = findViewById(R.id.updateFullName);
        contact = findViewById(R.id.updateContact);
        Button updateBtn = findViewById(R.id.updateBtn);
        userID = preferenceManager.getString(Constants.KEY_USER_ID);
        prof.setBackgroundResource(R.color.black);

        findViewById(R.id.signOutBtn).setOnClickListener(v -> signOut()); //call sign out

        displayUserData();

        updateBtn.setOnClickListener(v -> updateUserDetails()); //update user details
    }

    private void updateUserDetails() {
        if (fName.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(Profile.this, "Enter Full Name", Toast.LENGTH_SHORT).show();
        } else if (contact.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(Profile.this, "Enter Contact Number", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            Map<String, Object> update = new HashMap<>();
            update.put(Constants.KEY_FULL_NAME, fName.getEditText().getText().toString());
            update.put(Constants.KEY_CONTACT, contact.getEditText().getText().toString());
            reference.child(Constants.KEY_COLLECTION_USERS).child(userID).updateChildren(update);
            preferenceManager.putString(Constants.KEY_FULL_NAME, fName.getEditText()
                    .getText().toString());
            preferenceManager.putString(Constants.KEY_CONTACT, contact.getEditText()
                    .getText().toString());
            Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show();
        }
        displayUserData();
    } //update user details when update button clicked

    private void displayUserData() {
        fNameLabel.setText(preferenceManager.getString(Constants.KEY_FULL_NAME));
        firstChar.setText(preferenceManager.getString(Constants.KEY_FULL_NAME).substring(0, 1));
        fName.getEditText().setText(preferenceManager.getString(Constants.KEY_FULL_NAME));
        contact.getEditText().setText(preferenceManager.getString(Constants.KEY_CONTACT));
    } //display user details

    public void clickMenu(View view) {
        Home.openDrawer(drawerLayout);
    } //menu icon

    public void clickLogo(View view) {
        Home.closeDrawer(drawerLayout);
    }

    public void clickHome(View view) {
        Home.redirectActivity(this, Home.class);
        home.setBackgroundResource(R.color.black);
        chat.setBackgroundResource(R.color.white);
        prof.setBackgroundResource(R.color.white);
    } //home tab

    public void clickProfile(View view) {
        recreate();
        home.setBackgroundResource(R.color.white);
        chat.setBackgroundResource(R.color.white);
        prof.setBackgroundResource(R.color.black);
    } //profile tab

    public void clickHelp(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                .setTitle("Need help?")
                .setMessage("Please send an email to \nayushr1900@gmail.com")
                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        builder.show();
    } //help icon

    public void clickChat(View view) {
        Home.redirectActivity(this, ChatActivity.class);
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
}