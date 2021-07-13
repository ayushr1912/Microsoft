package com.example.microsoftengageapp2021.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.models.User;
import com.example.microsoftengageapp2021.network.ApiClient;
import com.example.microsoftengageapp2021.network.ApiService;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.example.microsoftengageapp2021.utilities.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendingInvitation extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private TextView receiverName;
    private int rejectCount = 0;
    private int totalRecipients = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_invitation);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        receiverName = findViewById(R.id.receiverUsername);
        TextView receiverEmail = findViewById(R.id.receiverEmail);
        preferenceManager = new PreferenceManager(getApplicationContext());

        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            receiverName.setText(String.format("%s", user.fullName));
            if(user.email==null){
                receiverEmail.setText("Unknown User");
            } else{
                receiverEmail.setText(String.format("%s", user.email));
            }
        } //check anonymous user

        ImageView cancelInvite = findViewById(R.id.cancelInvitation);
        cancelInvite.setOnClickListener(v -> {
            if (getIntent().getBooleanExtra("isMultipleP", false)) {
                Type type = new TypeToken<ArrayList<User>>() {
                }.getType();
                ArrayList<User> recipients = new Gson().fromJson(
                        getIntent().getStringExtra("selectedPendingUsers"), type);
                cancelInvitation(null, recipients);
            } else {
                if (user != null) {
                    cancelInvitation(user.token, null);
                }
            }
        }); //cancel invite

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult();
                if (getIntent().getBooleanExtra("isMultipleP", false)) {
                    Type type = new TypeToken<ArrayList<User>>() {
                    }.getType();
                    ArrayList<User> recipients = new Gson().fromJson(
                            getIntent().getStringExtra("selectedPendingUsers"), type);
                    if(recipients != null){
                        totalRecipients = recipients.size();
                    }
                    initiateInvitation(null, recipients);
                } else {
                    if (user != null) {
                        totalRecipients = 1;
                        initiateInvitation(user.token, null);
                    }
                }
            }
        });
        //to initiate incite
    }

    private void initiateInvitation(String receiverToken, ArrayList<User> recipients) {
        try {
            JSONArray tokens = new JSONArray();
            if (receiverToken != null) {
                tokens.put(receiverToken);
            }
            if (recipients != null && recipients.size() > 0) {
                StringBuilder recNames = new StringBuilder();
                for (int i = 0; i < recipients.size(); i++) {
                    tokens.put(recipients.get(i).token);
                    recNames.append(recipients.get(i).fullName).append("\n");
                }
                receiverName.setText(recNames.toString());
            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.KEY_FULL_NAME, preferenceManager.getString(Constants.KEY_FULL_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);
            data.put(Constants.REMOTE_MSG_WAITING_ROOM, preferenceManager
                    .getString(Constants.KEY_MEETING_ID));

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    //initiate invite function

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                        Toast.makeText(SendingInvitation.this, "Invite Sent",
                                Toast.LENGTH_SHORT).show();
                    } else if (type.equals(Constants.REMOTE_MSG_INVITE_RESPONSE)) {
                        Toast.makeText(SendingInvitation.this, "Invitation Cancelled",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(SendingInvitation.this, response.message(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(SendingInvitation.this, t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    } //send msg token

    private void cancelInvitation(String receiverToken, ArrayList<User> recipients) {
        try {
            JSONArray tokens = new JSONArray();
            if (receiverToken != null) {
                tokens.put(receiverToken);
            }

            if (recipients != null && recipients.size() > 0) {
                for (User user : recipients) {
                    tokens.put(user.token);
                }
            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITE_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITE_RESPONSE, Constants.REMOTE_MSG_INVITE_CANCEL);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITE_RESPONSE);

        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    //cancel invite

    private BroadcastReceiver inviteResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITE_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITE_ACCEPT)) {
                    Toast.makeText(context, "Invitation Accepted", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), PendingUser.class);
                    startActivity(i);
                } else if (type.equals(Constants.REMOTE_MSG_INVITE_REJECT)) {
                    rejectCount += 1;
                    if(rejectCount==totalRecipients){
                        Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    }; //check response

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                inviteResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITE_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                inviteResponseReceiver
        );
    }
}