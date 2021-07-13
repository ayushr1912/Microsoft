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
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingCall extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String meetingRoom = null;
    private String meetingType = null;
    private TextView receiverName;
    private int rejectCount = 0;
    private int totalRecipients = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        preferenceManager = new PreferenceManager(getApplicationContext());
        receiverName = findViewById(R.id.receiverUsername);
        TextView receiverEmail = findViewById(R.id.receiverEmail);
        ImageView imageMeetingType = findViewById(R.id.meetingTypeImg);
        ImageView cancelCall = findViewById(R.id.cancelCall);

        meetingType = getIntent().getStringExtra("type");
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imageMeetingType.setImageResource(R.drawable.ic_video);
            } else{
                imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        } //check for meeting type

        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            receiverName.setText(String.format("%s", user.fullName));
            if(user.email==null){
                receiverEmail.setText("Unknown User");
            } else{
                receiverEmail.setText(String.format("%s", user.email));
            }
        } //check for anonymous user

        cancelCall.setOnClickListener(v ->{
            if(getIntent().getBooleanExtra("isMultiple", false)){
                Type type = new TypeToken<ArrayList<User>>(){}.getType();
                ArrayList<User> recipients = new Gson().fromJson(
                        getIntent().getStringExtra("selectedUsers"), type);
                cancelCall(null, recipients);
            }else{
                if(user!=null) {
                    cancelCall(user.token, null);
                }
            }
        }); //to cancel outgoing call

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult();
                if(meetingType!=null){
                    if(getIntent().getBooleanExtra("isMultiple", false)){
                        Type type = new TypeToken<ArrayList<User>>(){}.getType();
                        ArrayList<User> recipients = new Gson().fromJson(
                                getIntent().getStringExtra("selectedUsers"), type);
                        if(recipients != null){
                            totalRecipients = recipients.size();
                        }
                        initiateCall(meetingType, null, recipients);
                    } else{
                        if (user != null) {
                            totalRecipients = 1;
                            initiateCall(meetingType, user.token, null);
                        }
                    }
                }

            }
        });
        //to initiate call
    }

    private void initiateCall(String meetingType, String receiverToken, ArrayList<User> recipients)
    {
        try {
            JSONArray tokens = new JSONArray();
            if(receiverToken!=null){
                tokens.put(receiverToken);
            }
            if(recipients!=null&&recipients.size()>0){
                StringBuilder recNames = new StringBuilder();
                for(int i=0;i<recipients.size();i++){
                    tokens.put(recipients.get(i).token);
                    recNames.append(recipients.get(i).fullName).append("\n");
                }
                receiverName.setText(recNames.toString());
            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_CALL_TYPE, Constants.REMOTE_CALL_INVITATION);
            data.put(Constants.REMOTE_CALL_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FULL_NAME, preferenceManager.getString(Constants.KEY_FULL_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_CALL_INVITER_TOKEN, inviterToken);

            meetingRoom = preferenceManager.getString(Constants.KEY_JITSI_MEET);
            data.put(Constants.REMOTE_CALL_MEETING_ROOM, meetingRoom);

            body.put(Constants.REMOTE_CALL_DATA, data);
            body.put(Constants.REMOTE_CALL_REGISTRATION_IDS, tokens);

            sendRemoteCall(body.toString(), Constants.REMOTE_CALL_INVITATION);

        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    } //initiate call function

    private void sendRemoteCall(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteCallHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_CALL_INVITATION)) {
                        Toast.makeText(OutgoingCall.this, "Outgoing Call",
                                Toast.LENGTH_SHORT).show();
                    } else if (type.equals(Constants.REMOTE_CALL_INVITE_RESPONSE)) {
                        Toast.makeText(OutgoingCall.this, "Call Cancelled",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(OutgoingCall.this, response.message(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(OutgoingCall.this, t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    } //sending call token

    private void cancelCall(String receiverToken, ArrayList<User> recipients){
        try {
            JSONArray tokens = new JSONArray();

            if(receiverToken!=null){
                tokens.put(receiverToken);
            }

            if(recipients!=null && recipients.size()>0){
                for(User user : recipients){
                    tokens.put(user.token);
                }
            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_CALL_TYPE, Constants.REMOTE_CALL_INVITE_RESPONSE);
            data.put(Constants.REMOTE_CALL_INVITE_RESPONSE, Constants.REMOTE_CALL_INVITE_CANCEL);

            body.put(Constants.REMOTE_CALL_DATA, data);
            body.put(Constants.REMOTE_CALL_REGISTRATION_IDS, tokens);

            sendRemoteCall(body.toString(), Constants.REMOTE_CALL_INVITE_RESPONSE);

        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    } //cancel call

    private BroadcastReceiver callResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_CALL_INVITE_RESPONSE);
            if(type!=null){
                if(type.equals(Constants.REMOTE_CALL_INVITE_ACCEPT)){
                    try {
                        if(!preferenceManager.getBoolean(Constants.KEY_IN_MEETING)){
                            URL serverURL = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions.Builder builder =
                                    new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(meetingRoom);
                            if(meetingType.equals("audio")){
                                builder.setVideoMuted(true);
                            }
                            JitsiMeetActivity.launch(OutgoingCall.this, builder.build());
                        }
                        finish();
                    }catch (Exception e){
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else if(type.equals(Constants.REMOTE_CALL_INVITE_REJECT)){
                    rejectCount += 1;
                    if(rejectCount==totalRecipients){
                        Toast.makeText(context, "Call Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    }; //call response tracker

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