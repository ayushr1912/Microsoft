package com.example.microsoftengageapp2021.utilities;

import java.util.HashMap;

public class Constants {

    public static final String KEY_COLLECTION_USERS = "Users";
    public static final String KEY_FULL_NAME = "Full Name";
    public static final String KEY_EMAIL = "Email";
    public static final String KEY_CONTACT = "Contact";
    public static final String KEY_USER_ID = "User ID";
    public static final String KEY_FCM_TOKEN = "FCM Token";
    public static final String KEY_IS_ANO = "Is Anonymous";
    public static final String KEY_PREFERENCE_NAME = "appPreference";

    public static final String KEY_COLLECTION_MEETINGS = "Meetings";
    public static final String KEY_MEETING_ID = "Meeting ID";
    public static final String KEY_MEETING_PASS = "Meeting Password";
    public static final String KEY_HOST_ID = "Host ID";
    public static final String KEY_IS_JOINED = "Is Joined";
    public static final String KEY_JITSI_MEET = "Jitsi Meet ID";
    public static final String KEY_PART_NAME = "Participant Name";
    public static final String KEY_MEET_FCM_TOKEN = "FCM Token";
    public static final String KEY_MEETING_USER_COLLECTION = "User Collection";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "Message";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";


    public static final String REMOTE_MSG_INVITE_RESPONSE = "invitationResponse";

    public static final String REMOTE_MSG_INVITE_ACCEPT = "accept";
    public static final String REMOTE_MSG_INVITE_REJECT = "reject";
    public static final String REMOTE_MSG_INVITE_CANCEL = "cancel";

    public static final String REMOTE_MSG_WAITING_ROOM = "waitingRoom";


    public static final String GROUP_MESSAGE_COLLECTION = "Group Messages";


    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAAZW9ctiY:APA91bEFrQFTCnIT3Shq9pNtqNJ1RWVkkQs_XMoZcGRXwWZuG0iV2nVl7c7_l9ittnv5KJCo7FTgHfwEhnSJzwnb840uzNrThsrDO6nDUROm6ttMLKhq3WMwoIFUrO220jlD4AgKxNnE"
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE, "application/json");
        return headers;

    }

    public static final String REMOTE_CALL_AUTHORIZATION = "Authorization";
    public static final String REMOTE_CALL_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_CALL_TYPE = "type";
    public static final String REMOTE_CALL_INVITATION = "Call";
    public static final String REMOTE_CALL_MEETING_TYPE = "meetingType";
    public static final String REMOTE_CALL_INVITER_TOKEN = "inviterToken";
    public static final String KEY_IN_MEETING = "In Meeting";
    public static final String REMOTE_CALL_DATA = "data";
    public static final String REMOTE_CALL_REGISTRATION_IDS = "registration_ids";

    public static final String REMOTE_CALL_INVITE_RESPONSE = "callResponse";

    public static final String REMOTE_CALL_INVITE_ACCEPT = "accept";
    public static final String REMOTE_CALL_INVITE_REJECT = "reject";
    public static final String REMOTE_CALL_INVITE_CANCEL = "cancel";

    public static final String REMOTE_CALL_MEETING_ROOM = "meetingRoom";

    public static HashMap<String, String> getRemoteCallHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_CALL_AUTHORIZATION,
                "key=AAAAZW9ctiY:APA91bEFrQFTCnIT3Shq9pNtqNJ1RWVkkQs_XMoZcGRXwWZuG0iV2nVl7c7_l9ittnv5KJCo7FTgHfwEhnSJzwnb840uzNrThsrDO6nDUROm6ttMLKhq3WMwoIFUrO220jlD4AgKxNnE"
        );
        headers.put(Constants.REMOTE_CALL_CONTENT_TYPE, "application/json");
        return headers;

    }

}
