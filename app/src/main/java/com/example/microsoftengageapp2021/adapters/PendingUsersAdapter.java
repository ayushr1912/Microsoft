package com.example.microsoftengageapp2021.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.models.User;
import com.example.microsoftengageapp2021.receivers.PendingUsersReceiver;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class PendingUsersAdapter extends RecyclerView.Adapter<PendingUsersAdapter.UserViewHolder>{

    private final List<User> users;
    private final PendingUsersReceiver pendingUsersReceiver;
    private final List<User> selectedPendingUsers;

    public PendingUsersAdapter(List<User> users, PendingUsersReceiver pendingUsersReceiver) {
        this.users = users;
        this.pendingUsersReceiver = pendingUsersReceiver;
        selectedPendingUsers = new ArrayList<>();
    }

    public List<User> getSelectedPendingUsers(){
        return selectedPendingUsers;
    }

    @NotNull
    @Override
    public UserViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.display_pending_user,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PendingUsersAdapter.UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        TextView displayPendingUser;
        ImageView sendInvite, selectedPendingUserImg, verifiedUser, unknownUser, sendRoomCode;
        ConstraintLayout pendingUserContainer;

        UserViewHolder(@NotNull View itemView) {
            super(itemView);
            displayPendingUser = itemView.findViewById(R.id.displayFullName);
            sendInvite = itemView.findViewById(R.id.IC_addUser);
            sendRoomCode = itemView.findViewById(R.id.IC_chat);
            verifiedUser = itemView.findViewById(R.id.pendingVerifiedImg);
            unknownUser = itemView.findViewById(R.id.pendingUnknownImg);
            pendingUserContainer = itemView.findViewById(R.id.pendingUserContainer);
            selectedPendingUserImg = itemView.findViewById(R.id.selectedPendingUserImg);
        }

        void setUserData(User user)
        {
            displayPendingUser.setText(String.format("%s",user.fullName));
            if(user.email==null){
                unknownUser.setVisibility(View.VISIBLE);
                verifiedUser.setVisibility(View.INVISIBLE);
                sendRoomCode.setVisibility(View.INVISIBLE);
            } else{
                unknownUser.setVisibility(View.INVISIBLE);
                verifiedUser.setVisibility(View.VISIBLE);
            }
            sendInvite.setOnClickListener(v -> pendingUsersReceiver.sendInvitation(user));
            sendRoomCode.setOnClickListener(v -> pendingUsersReceiver.sendRoomCode(user));

            pendingUserContainer.setOnLongClickListener(v -> {
                if(selectedPendingUserImg.getVisibility()!= View.VISIBLE){
                    selectedPendingUsers.add(user);
                    selectedPendingUserImg.setVisibility(View.VISIBLE);
                    sendInvite.setVisibility(View.GONE);
                    sendRoomCode.setVisibility(View.GONE);
                    if(verifiedUser.getVisibility()==View.VISIBLE){
                        verifiedUser.setVisibility(View.GONE);
                    }
                    if(unknownUser.getVisibility()==View.VISIBLE){
                        unknownUser.setVisibility(View.GONE);
                    }
                    pendingUsersReceiver.onMultiplePendingUsersAction(true);
                }
                return true;
            });

            pendingUserContainer.setOnClickListener(v -> {
                if(selectedPendingUserImg.getVisibility()==View.VISIBLE) {
                    selectedPendingUsers.remove(user);
                    selectedPendingUserImg.setVisibility(View.GONE);
                    if(verifiedUser.getVisibility()==View.GONE){
                        verifiedUser.setVisibility(View.VISIBLE);
                    }
                    if(unknownUser.getVisibility()==View.GONE){
                        unknownUser.setVisibility(View.VISIBLE);
                    }
                    sendInvite.setVisibility(View.VISIBLE);
                    sendRoomCode.setVisibility(View.VISIBLE);
                    if (selectedPendingUsers.size() == 0) {
                        pendingUsersReceiver.onMultiplePendingUsersAction(false);
                    }
                }else{
                    if(selectedPendingUsers.size()>0){
                        selectedPendingUsers.add(user);
                        selectedPendingUserImg.setVisibility(View.VISIBLE);
                        sendInvite.setVisibility(View.GONE);
                        sendRoomCode.setVisibility(View.GONE);
                        if(verifiedUser.getVisibility()==View.VISIBLE){
                            verifiedUser.setVisibility(View.GONE);
                        }
                        if(unknownUser.getVisibility()==View.VISIBLE){
                            unknownUser.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }
}
