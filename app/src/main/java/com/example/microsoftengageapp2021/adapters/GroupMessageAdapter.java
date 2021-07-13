package com.example.microsoftengageapp2021.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.microsoftengageapp2021.R;
import com.example.microsoftengageapp2021.models.GroupChat;
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

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private final Context context;
    private final List<GroupChat> groupChatList;

    public GroupMessageAdapter(Context context, List<GroupChat> groupChatList) {
        this.context = context;
        this.groupChatList = groupChatList;
    }

    @NotNull
    @Override
    public GroupMessageAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent,
                    false);
            return new GroupMessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent,
                    false);
            return new GroupMessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NotNull GroupMessageAdapter.ViewHolder holder, int position) {
        GroupChat groupChat = groupChatList.get(position);
        holder.showMsgContent.setText(groupChat.getMessage());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(Constants.KEY_COLLECTION_USERS).child(groupChat.getSender())
                .child(Constants.KEY_FULL_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String temp = snapshot.getValue(String.class);
                if (temp == null) {
                    holder.firstChar.setText("#");
                } else {
                    holder.firstChar.setText(temp.substring(0, 1));
                    holder.senderName.setText(temp);
                }
                holder.senderName.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView showMsgContent, firstChar, senderName;

        public ViewHolder(View view) {
            super(view);
            showMsgContent = view.findViewById(R.id.textMessage);
            firstChar = view.findViewById(R.id.senderFirstChar);
            senderName = view.findViewById(R.id.senderName);
        }
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (groupChatList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
