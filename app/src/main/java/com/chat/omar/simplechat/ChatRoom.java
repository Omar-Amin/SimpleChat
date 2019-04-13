package com.chat.omar.simplechat;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Calendar;
import java.util.concurrent.BlockingDeque;

public class ChatRoom extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference userDB;
    private DatabaseReference rootDB;
    private DatabaseReference chatRankDB;
    private String name;
    private Intent intent;
    private String roomname;
    private ArrayList<Room> msgs;
    private MessageRecyclerView messageRecyclerView;
    private RecyclerView recyclerView;
    private String uid;
    private boolean deleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        user = FirebaseAuth.getInstance().getCurrentUser(); //TODO: Make it safer (what if user == null)
        rootDB = FirebaseDatabase.getInstance().getReference();
        intent = getIntent();

        switch (intent.getStringExtra("Chatroom")){
            case "School":
                roomname = "School";
                break;
            case "Teachers":
                roomname = "Teachers";
                break;
            case "Students":
                roomname = "Students";
                break;
            case "Golf club":
                roomname = "Golf club";
                break;
            default:
                    System.out.println("ERROR");
        }

        recyclerView = findViewById(R.id.room_message);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        assert user != null;
        name = user.getDisplayName();
        uid = user.getUid();

        userDB = rootDB.child("users").child(uid);
        chatRankDB = rootDB.child("users").child(uid);

        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                receiveMsg(roomname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView msgBox = findViewById(R.id.message_box);
                String msg = msgBox.getText().toString();
                if(!msg.equals("")){
                    sendMsg(name,roomname,msg,uid); //change to getUid??
                }
                msgBox.setText("");
            }
        });

        System.out.println(name);
    }

    private void sendMsg(String sender, final String chat, String msg, String suid){
        DatabaseReference sendDB = rootDB;
        DatabaseReference sendPriority = rootDB;

        HashMap<String,String> hm = new HashMap<>();
        hm.put("sender",sender);
        hm.put("chat",chat); //TODO: work on this later
        hm.put("msg",msg);
        hm.put("suid",suid);
        Date timeSent = Calendar.getInstance().getTime();
        String hours = "";
        String minutes = "";
        if(timeSent.getHours() < 10){
            hours = "0" + timeSent.getHours();
        }
        if (timeSent.getMinutes() < 10){
            minutes = "0" + timeSent.getMinutes();
        }else {
            hours = "" + timeSent.getHours();
            minutes = "" + timeSent.getMinutes();
        }
        hm.put("time",hours + ":" + minutes);
        hm.put("avatar", String.valueOf(user.getPhotoUrl()));
        hm.put("msg_token",FirebaseInstanceId.getInstance().getToken());
        sendDB.child(chat).push().setValue(hm);
        hm.clear();
        hm.put("chatRanking",chat);
        switch (chat){
            case "School":
                hm.put("description","School chat, school only");
                break;
            case "Teachers":
                hm.put("description","Teachers only chat");
                break;
            case "Students":
                hm.put("description","Students only chat");
                break;
            case "Golf club":
                hm.put("description","Golf chat for everyone who plays golf");
                break;
            default:
                System.out.println("ERROR");
        }
        sendDB.child("chatStats").push().setValue(hm);

        //sendNotification(chat,sender,msg);
    }
    private void sendNotification(String receiver, final String username, final String message){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"ETELLERANDET");
    }

    private void receiveMsg(final String chat){
        msgs = new ArrayList<>();

        userDB = FirebaseDatabase.getInstance().getReference(chat);
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                msgs.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Room room = snapshot.getValue(Room.class);
                    assert room != null;
                    msgs.add(room);
                    messageRecyclerView = new MessageRecyclerView(ChatRoom.this,msgs);
                    recyclerView.setAdapter(messageRecyclerView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
