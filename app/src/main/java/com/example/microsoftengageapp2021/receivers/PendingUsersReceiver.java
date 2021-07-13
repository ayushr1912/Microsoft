package com.example.microsoftengageapp2021.receivers;

import com.example.microsoftengageapp2021.models.User;

public interface PendingUsersReceiver {

    void sendRoomCode(User user);

    void sendInvitation(User user);

    void onMultiplePendingUsersAction(Boolean isMultiplePendingUserSelected);
}
