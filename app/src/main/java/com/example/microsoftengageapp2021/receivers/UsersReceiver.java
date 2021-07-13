package com.example.microsoftengageapp2021.receivers;

import com.example.microsoftengageapp2021.models.User;

public interface UsersReceiver {

    void startVideoCall(User user);

    void startAudioCall(User user);

    void onMultipleUsersAction(Boolean isMultipleUserSelected);
}
