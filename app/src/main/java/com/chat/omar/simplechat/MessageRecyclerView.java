package com.chat.omar.simplechat;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MessageRecyclerView extends RecyclerView.Adapter {

    private Context context;
    private List<Room> room;
    private FirebaseUser firebaseUser;

    public MessageRecyclerView(Context context,List<Room> room){
        this.context = context;
        this.room = room;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        System.out.println("ONCREAT");
        if(viewType == 1){
            view = LayoutInflater.from(context).inflate(R.layout.send_layout,parent,false);
            System.out.println("AFTER CREATING");
            return new SendViewHolder(view);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.receive_layout,parent,false);
            System.out.println("AFTER CREATING");
            return new ReceiveViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        System.out.println("HALLO");
        switch (holder.getItemViewType()) {
            case 1:
                System.out.println("RIGTH");
                SendViewHolder svh = (SendViewHolder) holder;
                svh.bind(room.get(position));
                System.out.println("IN BINDVIEWHOLDER");
                break;
            case 0:
                System.out.println("LEFT");
                ReceiveViewHolder rvh = (ReceiveViewHolder) holder;
                rvh.bind(room.get(position));
                System.out.println("IN BINDVIEWHOLDER");
                break;
        }
    }


    @Override
    public int getItemCount() {
        return room.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        System.out.println("HALLO");
        if(room.get(position).getSender().equals(firebaseUser.getUid())){
            System.out.println("RIGTH");
            return 1;
        }else {
            System.out.println("LEFT");
            return 0;
        }
    }

    private class SendViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SendViewHolder(View itemView) {
            super(itemView);
            System.out.println("SENDVIEWHOLDER CONSTRUCTOR");
            messageText = itemView.findViewById(R.id.txt_send);
            timeText = itemView.findViewById(R.id.send_time);
            System.out.println("LEAVING SENDVIEWHOLDER CONSTRUCTOR");
        }

        void bind(Room message) {
            System.out.println("BINDER");
            messageText.setText(message.getMsg());

            timeText.setText("Change this");
            System.out.println("LEAVING BINDER");
        }
    }

    private class ReceiveViewHolder extends RecyclerView.ViewHolder {
        TextView msgTxt, time, name;

        ReceiveViewHolder(View itemView) {
            super(itemView);


            System.out.println("RECEIVEVIEWHOLDER CONSTRUCTOR");
            msgTxt = itemView.findViewById(R.id.txt_receive);
            time = itemView.findViewById(R.id.receive_time);
            name = itemView.findViewById(R.id.receive_name);
            System.out.println("LEAVING RECEIVEVIEWHOLDER CONSTRUCTOR");
        }

        void bind(Room message) {
            System.out.println("BINDER");
            msgTxt.setText(message.getMsg());

            time.setText("Change this");

            name.setText(message.getSender());
            System.out.println("LEAVING BINDER");
        }
    }
}
