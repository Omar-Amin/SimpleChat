package com.chat.omar.simplechat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


public class MessageRecyclerView extends RecyclerView.Adapter<MessageRecyclerView.RCViewHolder> {

    private Context context;
    private List<Room> room;
    private final int SEND_IMG = 3;
    private final int RECEIVE_IMG = 2;
    private final int SEND_TXT = 1;
    private final int RECEIVE_TXT = 0;

    public MessageRecyclerView(Context context,List<Room> room){
        this.context = context;
        this.room = room;
    }

    @Override
    public MessageRecyclerView.RCViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        //Check the type of the message, if it is from someone else or you sent it
        //and make layout based on what type of message it is
        if (viewType == SEND_IMG){
            view = LayoutInflater.from(context).inflate(R.layout.img_send,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }else if(viewType == RECEIVE_IMG){
            view = LayoutInflater.from(context).inflate(R.layout.img_receive,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }
        else if(viewType == SEND_TXT){
            view = LayoutInflater.from(context).inflate(R.layout.send_layout,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }else if(viewType == RECEIVE_TXT){
            view = LayoutInflater.from(context).inflate(R.layout.receive_layout,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RCViewHolder holder, final int position) {
        Room msg = room.get(position);
        holder.time.setText(msg.getTime());
        if(holder.name != null){ //means it is a receive type of message
            holder.name.setText(room.get(position).getSender());
        }
        if(holder.avatar != null){ //means it is a receive type of message
            //Used library in order to get picture from url
            Glide.with(context).load(msg.getAvatar()).into(holder.avatar);
        }
        //Check which type of message it is, image or text
        if (msg.getMsgType().equals("msg")){
            holder.msgTxt.setText(msg.getMsg());
        }else{
            //Using the URL and crop the picture
            Glide.with(context).load(msg.getMsg()).override(512,512).fitCenter().into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return room.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        Room temp = room.get(position);
        if(temp.getSuid().equals(firebaseUser.getUid()) && !temp.getMsgType().equals("msg")){
            return SEND_IMG;
        }else if (!temp.getSuid().equals(firebaseUser.getUid()) && !temp.getMsgType().equals("msg")){
            return RECEIVE_IMG;
        }else if( temp.getSuid().equals(firebaseUser.getUid())&& temp.getMsgType().equals("msg")){
            return SEND_TXT;
        }else {
            return RECEIVE_TXT;
        }
    }

    class RCViewHolder extends RecyclerView.ViewHolder{

        TextView msgTxt, time,name;
        ImageView avatar,image;

        RCViewHolder(View itemView) {
            super(itemView);
            //Check which layout is created
            if(itemView.getId() == R.id.receive_layout){
                msgTxt = itemView.findViewById(R.id.txt_receive);
                time = itemView.findViewById(R.id.receive_time);
                name = itemView.findViewById(R.id.receive_name);
                avatar = itemView.findViewById(R.id.img_profile);
            }else if(itemView.getId() == R.id.send_layout){
                msgTxt = itemView.findViewById(R.id.txt_send);
                time = itemView.findViewById(R.id.send_time);
            }else if(itemView.getId() == R.id.img_receive_layout){
                image = itemView.findViewById(R.id.img_receive);
                time = itemView.findViewById(R.id.receive_time);
                name = itemView.findViewById(R.id.receive_name);
                avatar = itemView.findViewById(R.id.img_profile);
            }else if(itemView.getId() == R.id.img_send_layout){
                image= itemView.findViewById(R.id.img_send);
                time = itemView.findViewById(R.id.send_time);
            }
        }
    }
}
