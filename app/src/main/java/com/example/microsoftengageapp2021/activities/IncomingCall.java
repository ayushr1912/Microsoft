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
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingCall extends AppCompatActivity {
    private String meetingType = null;
    private String meetRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        ImageView imageMeetingType = findViewById(R.id.meetingTypeImg);
        meetingType = getIntent().getStringExtra(Constants.REMOTE_CALL_MEETING_TYPE);

        if(meetingType!=null) {
            if(meetingType.equals("video")) //check for video meeting
            {
                imageMeetingType.setImageResource(R.drawable.ic_video);
            } else{
                imageMeetingType.setImageResource(R.drawable.ic_audio); //audio meeting
            }
        } //check meeting type

        //hooks
        TextView callerName = findViewById(R.id.senderUsername);
        TextView senderEmail = findViewById(R.id.senderEmail);
        String sender = getIntent().getStringExtra(Constants.KEY_FULL_NAME);
        String email = getIntent().getStringExtra(Constants.KEY_EMAIL);
        if(sender==null){
            callerName.setText("Anonymous");
        } else{
            callerName.setText(sender);
        } //check for anonymous user

        if(email==null){
            senderEmail.setText("Unknown User");
        } else{
            senderEmail.setText(String.format("%s", email));
        } // set name for anonymous user

        ImageView acceptImg = findViewById(R.id.acceptCall);
        acceptImg.setOnClickListener(v -> sendCallResponse(
                Constants.REMOTE_CALL_INVITE_ACCEPT,
                getIntent().getStringExtra(Constants.REMOTE_CALL_INVITER_TOKEN))); //accept call

        ImageView rejectImg = findViewById(R.id.rejectCall);
        rejectImg.setOnClickListener(v -> sendCallResponse(
                Constants.REMOTE_CALL_INVITE_REJECT,
                getIntent().getStringExtra(Constants.REMOTE_CALL_INVITER_TOKEN))); //reject call

        meetRoom = getIntent().getStringExtra(Constants.REMOTE_CALL_MEETING_ROOM);
    }

    private void sendCallResponse(String type, String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_CALL_TYPE, Constants.REMOTE_CALL_INVITE_RESPONSE);
            data.put(Constants.REMOTE_CALL_INVITE_RESPONSE, type);

            body.put(Constants.REMOTE_CALL_DATA, data);
            body.put(Constants.REMOTE_CALL_REGISTRATION_IDS, tokens);

            sendRemoteCall(body.toString(), type);

        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    } //send call response
    
    private void sendRemoteCall(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteCallHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(Constants.REMOTE_CALL_INVITE_ACCEPT)){
                        try {
                            URL serverURL = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(meetRoom);
                            if(meetingType.equals("audio")){
                                builder.setVideoMuted(true);
                            }
                            JitsiMeetActivity.launch(IncomingCall.this, builder.build());
                            finish();
                        }catch (Exception exception){
                            Toast.makeText(IncomingCall.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    } else{
                        Toast.makeText(IncomingCall.this, "Call Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else{
                    Toast.makeText(IncomingCall.this,  response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call,@NotNull Throwable t) {
                Toast.makeText(IncomingCall.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    } //to accept call

    private BroadcastReceiver callResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_CALL_INVITE_RESPONSE);
            if(type!=null) {
                if (type.equals(Constants.REMOTE_CALL_INVITE_CANCEL)) {
                    Toast.makeText(context, "Call cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }; //check for cancellation of call

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                callResponseReceiver,
                new IntentFilter(Constants.REMOTE_CALL_INVITE_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                callResponseReceiver
        );
    }
}