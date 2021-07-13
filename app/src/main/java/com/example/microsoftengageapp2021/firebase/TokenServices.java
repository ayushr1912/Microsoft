package com.example.microsoftengageapp2021.firebase;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.microsoftengageapp2021.activities.IncomingCall;
import com.example.microsoftengageapp2021.activities.ReceivingInvitation;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

public class TokenServices extends FirebaseMessagingService {
    private static final String TAG = TokenServices.class.getSimpleName();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);

        if (type != null) {
            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                Intent intent = new Intent(getApplicationContext(), ReceivingInvitation.class);
                intent.putExtra(
                        Constants.KEY_FULL_NAME,
                        remoteMessage.getData().get(Constants.KEY_FULL_NAME)
                );
                intent.putExtra(
                        Constants.KEY_EMAIL,
                        remoteMessage.getData().get(Constants.KEY_EMAIL)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_CALL_INVITER_TOKEN)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_WAITING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_WAITING_ROOM)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (type.equals(Constants.REMOTE_MSG_INVITE_RESPONSE)) {
                Intent intent = new Intent(Constants.REMOTE_MSG_INVITE_RESPONSE);
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITE_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITE_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else if (type.equals(Constants.REMOTE_CALL_INVITATION)) {
                Intent intent = new Intent(getApplicationContext(), IncomingCall.class);
                intent.putExtra(
                        Constants.REMOTE_CALL_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_CALL_MEETING_TYPE)
                );
                intent.putExtra(
                        Constants.KEY_FULL_NAME,
                        remoteMessage.getData().get(Constants.KEY_FULL_NAME)
                );
                intent.putExtra(
                        Constants.KEY_EMAIL,
                        remoteMessage.getData().get(Constants.KEY_EMAIL)
                );
                intent.putExtra(
                        Constants.REMOTE_CALL_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_CALL_INVITER_TOKEN)
                );
                intent.putExtra(
                        Constants.REMOTE_CALL_MEETING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_CALL_MEETING_ROOM)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (type.equals(Constants.REMOTE_CALL_INVITE_RESPONSE)) {
                Intent intent = new Intent(Constants.REMOTE_CALL_INVITE_RESPONSE);
                intent.putExtra(
                        Constants.REMOTE_CALL_INVITE_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_CALL_INVITE_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }

    }
}
