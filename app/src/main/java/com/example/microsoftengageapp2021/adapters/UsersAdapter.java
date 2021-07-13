package com.example.microsoftengageapp2021.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.models.User;
import com.example.microsoftengageapp2021.receivers.UsersReceiver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;
    private final UsersReceiver usersReceiver;
    private final List<User> selectedUsers;

    public UsersAdapter(List<User> users, UsersReceiver usersReceiver) {
        this.users = users;
        this.usersReceiver = usersReceiver;
        selectedUsers = new ArrayList<>();
    }

    public List<User> getSelectedUsers() {
        return selectedUsers;
    }

    @NotNull
    @Override
    public UserViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.display_user,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NotNull UsersAdapter.UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        TextView displayUser;
        ImageView audioCall, videoCall, verifiedUserImg, unknownUserImg;
        ConstraintLayout onlineUserContainer;
        ImageView selectedUserImg;

        public UserViewHolder(@NotNull View itemView) {
            super(itemView);
            displayUser = itemView.findViewById(R.id.displayFullName);
            audioCall = itemView.findViewById(R.id.IC_audioCall);
            videoCall = itemView.findViewById(R.id.IC_videoCall);
            verifiedUserImg = itemView.findViewById(R.id.verifiedUserImg);
            unknownUserImg = itemView.findViewById(R.id.unknownUserImg);
            onlineUserContainer = itemView.findViewById(R.id.onlineUserContainer);
            selectedUserImg = itemView.findViewById(R.id.selectedUserImg);
        }

        void setUserData(User user) {
            displayUser.setText(String.format("%s", user.fullName));
            if (user.email == null) {
                unknownUserImg.setVisibility(View.VISIBLE);
                verifiedUserImg.setVisibility(View.INVISIBLE);
            } else {
                unknownUserImg.setVisibility(View.INVISIBLE);
                verifiedUserImg.setVisibility(View.VISIBLE);
            }
            audioCall.setOnClickListener(v -> usersReceiver.startAudioCall(user));
            videoCall.setOnClickListener(v -> usersReceiver.startVideoCall(user));

            onlineUserContainer.setOnLongClickListener(v -> {

                if (selectedUserImg.getVisibility() != View.VISIBLE) {
                    selectedUsers.add(user);
                    selectedUserImg.setVisibility(View.VISIBLE);
                    audioCall.setVisibility(View.GONE);
                    videoCall.setVisibility(View.GONE);
                    if (verifiedUserImg.getVisibility() == View.VISIBLE) {
                        verifiedUserImg.setVisibility(View.GONE);
                    }
                    if (unknownUserImg.getVisibility() == View.VISIBLE) {
                        unknownUserImg.setVisibility(View.GONE);
                    }
                    usersReceiver.onMultipleUsersAction(true);
                }
                return true;
            });

            onlineUserContainer.setOnClickListener(v -> {
                if (selectedUserImg.getVisibility() == View.VISIBLE) {
                    selectedUsers.remove(user);
                    selectedUserImg.setVisibility(View.GONE);
                    videoCall.setVisibility(View.VISIBLE);
                    audioCall.setVisibility(View.VISIBLE);
                    if (verifiedUserImg.getVisibility() == View.GONE) {
                        verifiedUserImg.setVisibility(View.VISIBLE);
                    }
                    if (unknownUserImg.getVisibility() == View.GONE) {
                        unknownUserImg.setVisibility(View.VISIBLE);
                    }
                    if (selectedUsers.size() == 0) {
                        usersReceiver.onMultipleUsersAction(false);
                    }
                } else {
                    if (selectedUsers.size() > 0) {
                        selectedUsers.add(user);
                        selectedUserImg.setVisibility(View.VISIBLE);
                        videoCall.setVisibility(View.GONE);
                        audioCall.setVisibility(View.GONE);
                        if (verifiedUserImg.getVisibility() == View.VISIBLE) {
                            verifiedUserImg.setVisibility(View.GONE);
                        }
                        if (unknownUserImg.getVisibility() == View.VISIBLE) {
                            unknownUserImg.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }
}
