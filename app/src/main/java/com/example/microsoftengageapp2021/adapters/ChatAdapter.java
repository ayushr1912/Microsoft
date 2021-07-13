package com.example.microsoftengageapp2021.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.activities.MessagingActivity;
import com.example.microsoftengageapp2021.models.Chat;
import com.example.microsoftengageapp2021.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<User> users;

    String lastMessage;

    public ChatAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.display_chat_users, parent,
                false);
        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChatAdapter.ViewHolder holder, int position) {

        User user = users.get(position);

        if (user.email == null) {
            holder.fullName.setText(user.fullName);
            holder.firstChar.setText("#");
        } //check for anonymous user
        else {
            holder.fullName.setText(user.fullName);
            holder.firstChar.setText(user.fullName.substring(0, 1));
        }

        if (user.token != null) {
            holder.onlineUser.setVisibility(View.VISIBLE);
        } //check if user is online

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessagingActivity.class);
            intent.putExtra("userID", user.userID);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }); //open chat

        showLastMessage(user.userID, holder.lastMsg); //show last message under user
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fullName, firstChar, lastMsg;
        public ImageView onlineUser;

        public ViewHolder(View view) {
            super(view);
            //hooks
            fullName = view.findViewById(R.id.displayChatUserName);
            firstChar = view.findViewById(R.id.userFirstChar);
            onlineUser = view.findViewById(R.id.ic_online);
            lastMsg = view.findViewById(R.id.displayLastMessage);
        }
    }

    private void showLastMessage(final String userID, final TextView viewLastMsg) {
        lastMessage = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender()
                            .equals(userID) || chat.getReceiver().equals(userID) && chat.getSender()
                            .equals(firebaseUser.getUid())) {
                        lastMessage = chat.getMessage();
                    }
                }
                switch (lastMessage) {
                    case "default":
                        viewLastMsg.setText("No Message");
                        break;

                    default:
                        viewLastMsg.setText(lastMessage);
                        break;
                }
                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    } //last msg fn
}
