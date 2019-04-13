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

    public MessageRecyclerView(Context context,List<Room> room){
        this.context = context;
        this.room = room;
    }

    @Override
    public MessageRecyclerView.RCViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == 1){
            view = LayoutInflater.from(context).inflate(R.layout.send_layout,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.receive_layout,parent,false);
            return new MessageRecyclerView.RCViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RCViewHolder holder, final int position) {
        Room msg = room.get(position);
        holder.msgTxt.setText(msg.getMsg());
        holder.time.setText(msg.getTime());
        if(holder.name != null){
            holder.name.setText(room.get(position).getSender());
        }
        if(holder.avatar != null){
            //Used library in order to get picture from url
            Glide.with(context).load(msg.getAvatar()).into(holder.avatar);
        }

    }

/*    private class BackgroundThread extends AsyncTask<URL,Void,Drawable>{

        @Override
        protected Drawable doInBackground(URL... urls) {
            Drawable d = null;
            try {
                System.out.println("HALLOO :" + urls);
                InputStream inputStream = (InputStream) new URL(room.get(position).getAvatar()).getContent();
                d = Drawable.createFromStream(inputStream,room.get(position).getAvatar());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return d;
        }
    }*/


    @Override
    public int getItemCount() {
        return room.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        if(room.get(position).getSuid().equals(firebaseUser.getUid())){
            return 1;
        }else {
            return 0;
        }
    }

    class RCViewHolder extends RecyclerView.ViewHolder{

        TextView msgTxt, time,name;
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
            }
        }
    }
}
