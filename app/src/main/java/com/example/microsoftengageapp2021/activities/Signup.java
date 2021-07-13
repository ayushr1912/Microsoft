package com.example.microsoftengageapp2021.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.example.microsoftengageapp2021.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class Signup extends AppCompatActivity {


    private Button signupBtn, signinBtn, connAcc;
    private ImageView image;
    private TextView logoText, promptText;
    private TextInputLayout regPass, regEmail, regContact, regName;
    private ProgressBar signupProgressBar;
    private PreferenceManager preferenceManager;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private String  fullName, email, contact, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        setContentView(R.layout.activity_signup);

        preferenceManager = new PreferenceManager(getApplicationContext());
        reference = FirebaseDatabase.getInstance().getReference();

        //hooks
        image = findViewById(R.id.logoImg);
        logoText = findViewById(R.id.message);
        promptText = findViewById(R.id.promptMsg);
        regContact = findViewById(R.id.contact);
        regEmail = findViewById(R.id.email);
        regName = findViewById(R.id.fullname);
        regPass = findViewById(R.id.password);
        signinBtn = findViewById(R.id.signinBtn);
        signupBtn = findViewById(R.id.signupBtn);
        signupProgressBar = findViewById(R.id.signupProgressBar);
        connAcc = findViewById(R.id.connAccBtn);
        firebaseAuth = FirebaseAuth.getInstance();

        signinBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Signup.this, Login.class);
            Pair[] pairs = new Pair[7];
            pairs[0] = new Pair<View, String>(image, "logoTrans");
            pairs[1] = new Pair<View, String>(logoText, "msgTrans");
            pairs[2] = new Pair<View, String>(promptText, "promptTrans");
            pairs[3] = new Pair<View, String>(regName, "userTrans");
            pairs[4] = new Pair<View, String>(regPass, "passTrans");
            pairs[5] = new Pair<View, String>(signinBtn, "signinTrans");
            pairs[6] = new Pair<View, String>(signupBtn, "signupTrans");

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                    (Signup.this, pairs);
            startActivity(intent, options.toBundle());
        }); //prompts back to login page

        String source = getIntent().getStringExtra("source");
        if(source!=null&&source.equals("connect")){
            signupBtn.setVisibility(View.GONE);
            signinBtn.setVisibility(View.GONE);
            connAcc.setVisibility(View.VISIBLE);
        } //check for link account

        connAcc.setOnClickListener(v -> {
            AuthCredential credential = EmailAuthProvider.getCredential(
                    regEmail.getEditText().getText().toString().trim()
                    , regPass.getEditText().getText().toString().trim());
            connectAccount(credential);
        }); //link account

        signupBtn.setOnClickListener(v -> {
            if(checkFields()){
                signup();
            }
        }); //sign up button
    }

    private void connectAccount(AuthCredential credential) {
        firebaseAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            preferenceManager.putBoolean(Constants.KEY_IS_ANO, false);
                            fullName = regName.getEditText().getText().toString();
                            email = regEmail.getEditText().getText().toString();
                            contact = regContact.getEditText().getText().toString();
                            Toast.makeText(Signup.this, "Account linked successfully"
                                    , Toast.LENGTH_SHORT).show();
                            FirebaseUser user = task.getResult().getUser();
                            HashMap<String, Object> linkUser = new HashMap<>();
                            linkUser.put(Constants.KEY_FULL_NAME, fullName);
                            linkUser.put(Constants.KEY_EMAIL, email);
                            linkUser.put(Constants.KEY_CONTACT, contact);
                            linkUser.put(Constants.KEY_IS_JOINED, false);
                            updateUI(linkUser);
                        } else {
                            Toast.makeText(Signup.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    } //link account

    private boolean checkFields(){
        fullName = regName.getEditText().getText().toString();
        email = regEmail.getEditText().getText().toString();
        password= regPass.getEditText().getText().toString();
        contact = regContact.getEditText().getText().toString();
        boolean flag = false;
        if (fullName.isEmpty()) {
            Toast.makeText(Signup.this, "Enter Full Name", Toast.LENGTH_SHORT).show();
        } else if (email.isEmpty()) {
            Toast.makeText(Signup.this, "Enter Email", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Signup.this, "Enter VALID Email", Toast.LENGTH_SHORT).show();
        } else if (contact.isEmpty()) {
            Toast.makeText(Signup.this, "Enter Contact Number", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(Signup.this, "Enter Password", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(Signup.this, "Password must be >= 6 characters",
                    Toast.LENGTH_SHORT).show();
        } else {
            flag=true;
        }
        return flag;
    } //check for empty fields

    private void signup() {
        signupBtn.setVisibility(View.INVISIBLE);
        signupProgressBar.setVisibility(View.VISIBLE);
        fullName = regName.getEditText().getText().toString();
        email = regEmail.getEditText().getText().toString();
        password= regPass.getEditText().getText().toString();
        contact = regContact.getEditText().getText().toString();

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FULL_NAME, fullName);
        user.put(Constants.KEY_EMAIL, email);
        user.put(Constants.KEY_CONTACT, contact);
        user.put(Constants.KEY_IS_JOINED, false);

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task ->
        {
            if (task.isSuccessful()) {
                updateUI(user);
            } else {
                signupProgressBar.setVisibility(View.INVISIBLE);
                signupBtn.setVisibility(View.VISIBLE);
                Toast.makeText(Signup.this, task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    } //sign up process

    private void updateUI(HashMap<String, Object> user){
        String userID = firebaseAuth.getCurrentUser().getUid();
        reference.child(Constants.KEY_COLLECTION_USERS).child(userID).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        preferenceManager.putBoolean(Constants.KEY_IS_JOINED, false);
                        preferenceManager.putString(Constants.KEY_USER_ID, userID);
                        preferenceManager.putString(Constants.KEY_FULL_NAME, fullName);
                        preferenceManager.putString(Constants.KEY_EMAIL, email);
                        preferenceManager.putString(Constants.KEY_CONTACT, contact);
                        Toast.makeText(Signup.this, "User Created",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Signup.this.getApplicationContext(), Home.class);
                        Signup.this.startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Signup.this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    } //update UI function

}