package com.chat.omar.simplechat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class MessageRecyclerView extends RecyclerView.Adapter<MessageRecyclerView.RCViewHolder> {

    private Context context;
    private List<Room> room;

    public MessageRecyclerView(Context context,List<Room> room){
        this.context = context;
        this.room = room;
    }

    @Override
    public MessageRecyclerView.RCViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 3){
            view = LayoutInflater.from(context).inflate(R.layout.img_send,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }else if(viewType == 2){
            view = LayoutInflater.from(context).inflate(R.layout.img_receive,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }
        else if(viewType == 1){
            view = LayoutInflater.from(context).inflate(R.layout.send_layout,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.receive_layout,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RCViewHolder holder, final int position) {
        Room msg = room.get(position);
        holder.time.setText(msg.getTime());
        if(holder.name != null){
            holder.name.setText(room.get(position).getSender());
        }
        if(holder.avatar != null){
            //Used library in order to get picture from url
            Glide.with(context).load(msg.getAvatar()).into(holder.avatar);
        }

        if (msg.getMsgType().equals("msg")){
            holder.msgTxt.setText(msg.getMsg());
        }else{
            Glide.with(context).load(msg.getMsg()).into(holder.image);
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
            return 3;
        }else if (!temp.getSuid().equals(firebaseUser.getUid()) && !temp.getMsgType().equals("msg")){
            return 2;
        }else if( temp.getSuid().equals(firebaseUser.getUid())&& temp.getMsgType().equals("msg")){
            return 1;
        }else {
            return 0;
        }
    }

    class RCViewHolder extends RecyclerView.ViewHolder{

        TextView msgTxt, time,name;
        ImageView image;
        ImageView avatar;

        RCViewHolder(View itemView) {
            super(itemView);
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
