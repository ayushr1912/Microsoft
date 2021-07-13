package com.example.microsoftengageapp2021.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.activities.Home;
import com.example.microsoftengageapp2021.models.Chat;
import com.example.microsoftengageapp2021.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private final Context context;
    private final List<Chat> chats;
    private FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> chats) {
        this.context = context;
        this.chats = chats;
    }

    @NotNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent,
                                                        int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent,
                    false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent,
                    false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NotNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.showMsgContent.setText(chat.getMessage());
        if (chat.getMessage().startsWith("https://meet.jit.si")) {
            holder.showMsgContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] parts = chat.getMessage().split("/");
                    AlertDialog box = new AlertDialog.Builder(context).create();
                    box.setTitle("Alert");
                    box.setMessage("Do you want to continue?");
                    box.setButton(AlertDialog.BUTTON_POSITIVE, "Join Room", (dialog, which) -> {
                        try {
                            Intent i = new Intent(context, Home.class);
                            i.putExtra(
                                    Constants.REMOTE_MSG_WAITING_ROOM,
                                    parts[3]
                            );
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);
                        } catch (Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    box.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", (dialog, which) ->
                            box.dismiss());
                    box.show();

                }
            });
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(Constants.KEY_COLLECTION_USERS).child(chat.getSender())
                .child(Constants.KEY_FULL_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String temp = snapshot.getValue(String.class);
                if (temp == null) {
                    holder.firstChar.setText("#");
                } else {
                    holder.firstChar.setText(temp.substring(0, 1));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView showMsgContent, firstChar;

        public ViewHolder(View view) {
            super(view);
            showMsgContent = view.findViewById(R.id.textMessage);
            firstChar = view.findViewById(R.id.senderFirstChar);
        }

    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chats.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
