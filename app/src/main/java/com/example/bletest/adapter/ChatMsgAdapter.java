package com.example.bletest.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bletest.ChatMsg;
import com.example.bletest.R;

import java.util.List;

public class ChatMsgAdapter extends RecyclerView.Adapter<ChatMsgAdapter.ViewHolder> {

    private List<ChatMsg> mChatMsgList;

    public ChatMsgAdapter(List<ChatMsg> chatMsgList) {
        mChatMsgList = chatMsgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.char_msg_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMsg chatMsg = mChatMsgList.get(position);

        if (chatMsg.getType() == ChatMsg.TYPE_RECEIVED) {
            holder.recvLayout.setVisibility(View.VISIBLE);
            holder.sendLayout.setVisibility(View.GONE);

            holder.recvMsg.setText(chatMsg.getContent());
        } else if (chatMsg.getType() == ChatMsg.TYPE_SENT) {
            holder.recvLayout.setVisibility(View.GONE);
            holder.sendLayout.setVisibility(View.VISIBLE);

            holder.sendMsg.setText(chatMsg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mChatMsgList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout recvLayout;
        LinearLayout sendLayout;

        TextView recvMsg;
        TextView sendMsg;

        public ViewHolder(View view) {
            super(view);

            recvLayout = (LinearLayout) view.findViewById(R.id.char_msg_recv_layout);
            sendLayout = (LinearLayout) view.findViewById(R.id.char_msg_send_layout);

            recvMsg = (TextView) view.findViewById(R.id.char_msg_recv_tv);
            sendMsg = (TextView) view.findViewById(R.id.char_msg_send_tv);
        }
    }


}
