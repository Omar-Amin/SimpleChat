package com.chat.omar.simplechat;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.ValueEventListener;

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
    private String name;
    private Intent intent;
    private String roomname;
    private ArrayList<Room> msgs;
    private MessageRecyclerView messageRecyclerView;
    private RecyclerView recyclerView;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        user = FirebaseAuth.getInstance().getCurrentUser(); //TODO: Make it safer (what if user == null)
        rootDB = FirebaseDatabase.getInstance().getReference();
        intent = getIntent();

        switch (intent.getIntExtra("Chatroom",-1)){
            case 0:
                roomname = "School";
                break;
            case 1:
                roomname = "Teachers";
                break;
            case 2:
                roomname = "Students";
                break;
            case 3:
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

    private void sendMsg(String sender,String chat,String msg,String suid){
        DatabaseReference sendDB = rootDB;

        HashMap<String,String> hm = new HashMap<>();
        hm.put("sender",sender);
        hm.put("chat",chat); //TODO: work on this later
        hm.put("msg",msg);
        hm.put("suid",suid);
        Date timeSent = Calendar.getInstance().getTime();
        Date timeSent2 = Calendar.getInstance().getTime();
        hm.put("time",timeSent.getHours() + ":" + timeSent.getMinutes());
        hm.put("avatar", String.valueOf(user.getPhotoUrl()));
        sendDB.child(chat).push().setValue(hm);
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
