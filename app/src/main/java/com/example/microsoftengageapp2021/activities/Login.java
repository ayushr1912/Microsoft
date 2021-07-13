package com.example.microsoftengageapp2021.activities;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.example.microsoftengageapp2021.utilities.PreferenceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;

public class Login extends AppCompatActivity {
    private Button signupBtn, signInBtn, joinAno;
    private ImageView image;
    private TextView logoText, promptText;
    private TextInputLayout inputEmail, inputPassword;
    private ProgressBar signInProgressBar, joinAnoProgress;
    private PreferenceManager preferenceManager;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar
        setContentView(R.layout.activity_login);

        //hooks
        image = findViewById(R.id.logoImg);
        logoText = findViewById(R.id.message);
        promptText = findViewById(R.id.promptMsg);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        signInBtn = findViewById(R.id.signinBtn);
        signupBtn = findViewById(R.id.signupBtn);
        signInProgressBar = findViewById(R.id.signupProgressBar);
        joinAno = findViewById(R.id.joinAno);
        joinAnoProgress = findViewById(R.id.joinAnoProgressBar);
        preferenceManager = new PreferenceManager(getApplicationContext());
        Button googleSignInBtn = findViewById(R.id.googleSignIn);
        Button forgotBtn = findViewById(R.id.forgotPasswordButton);
        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        signupBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Signup.class);
            Pair[] pairs = new Pair[7];
            pairs[0] = new Pair<View, String>(image, "logoTrans");
            pairs[1] = new Pair<View, String>(logoText, "msgTrans");
            pairs[2] = new Pair<View, String>(promptText, "promptTrans");
            pairs[3] = new Pair<View, String>(inputEmail, "userTrans");
            pairs[4] = new Pair<View, String>(inputPassword, "passTrans");
            pairs[5] = new Pair<View, String>(signInBtn, "signinTrans");
            pairs[6] = new Pair<View, String>(signupBtn, "signupTrans");

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                    (Login.this, pairs);
            startActivity(intent, options.toBundle());
        }); //prompts to signup screen

        signInBtn.setOnClickListener(v -> {
            if (checkField()) {
                signIn();
            }
        }); //sign in button

        googleSignInBtn.setOnClickListener(v -> googlePrompt()); //sign in with google

        joinAno.setOnClickListener(v -> {
            joinAno.setVisibility(View.INVISIBLE);
            joinAnoProgress.setVisibility(View.VISIBLE);
            firebaseAuth.signInAnonymously()
                    .addOnCompleteListener(task -> {
                        joinAno.setVisibility(View.VISIBLE);
                        joinAnoProgress.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            preferenceManager.putBoolean(Constants.KEY_IS_ANO, true);
                            updateUI(firebaseAuth.getCurrentUser());
                        } else {
                            Toast.makeText(Login.this, "Failed to Create Account"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    });
        }); //create anonymous user

        forgotBtn.setOnClickListener(v -> {
            EditText passReset = new EditText(v.getContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Password Reset")
                    .setMessage("Enter your email")
                    .setView(passReset);
            builder.setPositiveButton("Proceed", (dialog, which) -> {
                String email = passReset.getText().toString();
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Login.this, "Reset link sent"
                                        , Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(Login.this, e.getMessage()
                                , Toast.LENGTH_SHORT).show());
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
        }); //forgot password button
    }

    public void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            Toast.makeText(this, "Signing back in", Toast.LENGTH_SHORT).show();
            updateUI(firebaseUser);
        }
    } //check if user is already signed in

    private void googlePrompt() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    } //google sign in prompt

    private boolean checkField() {
        boolean temp = false;
        if (inputEmail.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(Login.this, "Enter Username", Toast.LENGTH_SHORT).show();
            inputEmail.setError("Field Empty");
        } else if (inputPassword.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(Login.this, "Enter Password", Toast.LENGTH_SHORT).show();
            inputPassword.setError("Field Empty");
            inputEmail.setErrorEnabled(false);
        } else if (inputPassword.getEditText().getText().toString().trim().length() < 6) {
            Toast.makeText(Login.this, "Password must be >= 6 characters", Toast.LENGTH_SHORT).show();
            inputPassword.setError("Field Empty");
            inputEmail.setErrorEnabled(false);
        } else {
            temp = true;
        }
        return temp;
    } //check if fields are empty

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    } //google auth

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                            HashMap<String, Object> userAtt = new HashMap<>();
                            userAtt.put(Constants.KEY_FULL_NAME, signInAccount.getDisplayName());
                            userAtt.put(Constants.KEY_EMAIL, signInAccount.getEmail());
                            userAtt.put(Constants.KEY_IS_JOINED, false);
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            reference.child(Constants.KEY_COLLECTION_USERS).child(user.getUid()).updateChildren(userAtt);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Failed to Sign In", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    } //update UI with google account

    private void signIn() {
        signInBtn.setVisibility(View.INVISIBLE);
        signInProgressBar.setVisibility(View.VISIBLE);
        String email = inputEmail.getEditText().getText().toString();
        String pass = inputPassword.getEditText().getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateUI(firebaseAuth.getCurrentUser());
            } else {
                signInProgressBar.setVisibility(View.INVISIBLE);
                signInBtn.setVisibility(View.VISIBLE);
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    } //sign in procedure

    private void updateUI(FirebaseUser user) {
        if (user != null) {

            reference.child(Constants.KEY_COLLECTION_USERS).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    preferenceManager.putString(Constants.KEY_USER_ID, user.getUid());
                    preferenceManager.putString(Constants.KEY_FULL_NAME, snapshot.child(Constants.KEY_FULL_NAME).getValue(String.class));
                    preferenceManager.putString(Constants.KEY_EMAIL, snapshot.child(Constants.KEY_EMAIL).getValue(String.class));
                    preferenceManager.putString(Constants.KEY_CONTACT, snapshot.child(Constants.KEY_CONTACT).getValue(String.class));
                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(Login.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }

            });
        }
    } //update UI function

}