package com.chat.omar.simplechat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private StorageReference storRef;
    private String name;
    private Intent intent;
    private String roomname;
    private ArrayList<Room> msgs = new ArrayList<>();
    private MessageRecyclerView messageRecyclerView;
    private RecyclerView recyclerView;
    private String uid;
    private ImageButton uploadImg;
    private final int UPLOAD_PICTURE = 201;
    private Uri path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        user = FirebaseAuth.getInstance().getCurrentUser(); //TODO: Make it safer (what if user == null)
        rootDB = FirebaseDatabase.getInstance().getReference();
        intent = getIntent();
        path = null;
        roomname = intent.getStringExtra("Chatroom");

        storRef = FirebaseStorage.getInstance().getReference();

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
                Toast.makeText(ChatRoom.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });

        uploadImg = findViewById(R.id.upload_btn);
        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView msgBox = findViewById(R.id.message_box);
                String msg = msgBox.getText().toString();
                if(!msg.equals("") && path == null){
                    sendMsg(name,roomname,uid,msg,"msg");
                    Toast.makeText(ChatRoom.this, "Message sent", Toast.LENGTH_SHORT).show();
                }else if(path != null && msg.equals("")){
                    System.out.println("HALLOOO");
                    uploadToStorage();
                }else if(path != null && !msg.equals("")){
                    sendMsg(name,roomname,uid,msg,"msg");
                    Toast.makeText(ChatRoom.this, "Message sent", Toast.LENGTH_SHORT).show();
                    uploadToStorage();
                }
                msgBox.setText("");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UPLOAD_PICTURE){
            if(data != null && data.getData() != null){
                path = data.getData();
            }
            System.out.println("HALLOOOPATH: " + path);
        }
    }

    private void uploadToStorage(){
        final ProgressDialog progressDialog = new ProgressDialog(ChatRoom.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        Date date = new Date();
        final int timeSent = (int) date.getTime();
        final StorageReference storageReference = storRef.child("images/" + timeSent);

        storageReference.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressLint("ShowToast")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(ChatRoom.this,"Uploaded",Toast.LENGTH_LONG);
                path = null;

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        sendMsg(name,roomname,uid, String.valueOf(uri), String.valueOf(timeSent));
                    }
                });

            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                path = null;
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatRoom.this,"Error uploading picture", Toast.LENGTH_LONG);
                path = null;
                progressDialog.dismiss();
            }
        });
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Choose an image"),UPLOAD_PICTURE);
    }

    private void sendMsg(String sender, final String chat,String suid, String msg, String msgType){
        DatabaseReference sendDB = rootDB;
        DatabaseReference sendPriority = rootDB;

        HashMap<String,String> hm = new HashMap<>();
        hm.put("sender",sender);
        hm.put("chat",chat); //TODO: work on this later
        hm.put("msg",msg);
        hm.put("msgType",msgType);
        hm.put("suid",suid);
        //Storing time sent
        Date timeSent = Calendar.getInstance().getTime();
        String hours = "" + timeSent.getHours();
        String minutes = "" + timeSent.getMinutes();
        //Make sure we get the correct format HH:MM
        //and not H:MM, HH:M or H:M
        if(timeSent.getHours() < 10){
            hours = "0" + timeSent.getHours();
        }
        if (timeSent.getMinutes() < 10){
            minutes = "0" + timeSent.getMinutes();
        }

        hm.put("time",hours + ":" + minutes);
        hm.put("avatar", String.valueOf(user.getPhotoUrl()));
        hm.put("msg_token",FirebaseInstanceId.getInstance().getToken());
        sendDB.child(chat).push().setValue(hm);
        hm.clear();
        //Storing the description and chatroom which is used later when ranking the chatrooms
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

        userDB = FirebaseDatabase.getInstance().getReference(chat);
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                msgs.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Room room = snapshot.getValue(Room.class);
                    assert room != null;
                    msgs.add(room);
                }
                //TODO: On scrolled, maybe add more to the recycleview?
                messageRecyclerView = new MessageRecyclerView(ChatRoom.this,msgs);
                recyclerView.setAdapter(messageRecyclerView);
                System.out.println("HALLO");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatRoom.this, "Error receiving message", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
