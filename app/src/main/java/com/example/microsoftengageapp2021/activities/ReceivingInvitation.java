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
import com.example.microsoftengageapp2021.network.ApiClient;
import com.example.microsoftengageapp2021.network.ApiService;
import com.example.microsoftengageapp2021.utilities.Constants;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceivingInvitation extends AppCompatActivity {
    TextView senderName, senderEmail;
    String meetCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_invitation);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        senderName = findViewById(R.id.senderUsername);
        senderEmail = findViewById(R.id.senderEmail);
        String fullName = getIntent().getStringExtra(Constants.KEY_FULL_NAME);
        String email = getIntent().getStringExtra(Constants.KEY_EMAIL);
        if(fullName!=null){
            senderName.setText(String.format("%s", fullName));
        }
        if(email==null){
            senderEmail.setText("Unknown User");
        } //check if user is anonymous
        else{
            senderEmail.setText(String.format("%s", email));
        }

        meetCode = getIntent().getStringExtra(Constants.REMOTE_MSG_WAITING_ROOM);

        ImageView acceptImg = findViewById(R.id.acceptInvitation);
        acceptImg.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITE_ACCEPT,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN))); //accept invite

        ImageView rejectImg = findViewById(R.id.rejectInvitation);
        rejectImg.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITE_REJECT,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN))); //reject invite

    }

    private void sendInvitationResponse(String type, String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITE_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITE_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    } //sending response

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(Constants.REMOTE_MSG_INVITE_ACCEPT)){

                        try {
                            Intent i = new Intent(getApplicationContext(), Home.class);
                            i.putExtra(
                                    Constants.REMOTE_MSG_WAITING_ROOM,
                                    meetCode);
                            i.putExtra("source", "invite");
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            Toast.makeText(ReceivingInvitation.this, "Invitation accepted"
                                    , Toast.LENGTH_SHORT).show();
                        }catch(Exception e){
                            Toast.makeText(ReceivingInvitation.this, e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else{
                        Toast.makeText(ReceivingInvitation.this, "Invitation Rejected",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else{
                    Toast.makeText(ReceivingInvitation.this,  response.message(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call,@NotNull Throwable t) {
                Toast.makeText(ReceivingInvitation.this, t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    } //send message token

    private BroadcastReceiver inviteResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITE_RESPONSE);
            if(type!=null){
                if(type.equals(Constants.REMOTE_CALL_INVITE_CANCEL)){
                    Toast.makeText(context, "Invitation cancelled", Toast.LENGTH_SHORT).show();
                    finish();
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