package com.chat.omar.simplechat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Calendar;

public class ChatRoom extends AppCompatActivity {

    private FirebaseUser user;
    private String name;
    private String roomname;
    private MessageRecyclerView messageRecyclerView;
    private RecyclerView recyclerView;
    private String uid;
    private final int UPLOAD_PICTURE = 201;
    private final int UPLOAD_FROM_CAMERA = 200;
    private Uri path;
    private ProgressDialog progressChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        user = FirebaseAuth.getInstance().getCurrentUser();
        //If the user is null, means that session is expired or doesn't have any connection
        //Throw the user back to login
        if (user == null){
            Intent intent = new Intent(ChatRoom.this, LogIn.class);
            startActivity(intent);
        }
        //Used when sending messages, adding elements in the root of database
        //"path" used for path of image selected
        path = null;

        progressChat = new ProgressDialog(ChatRoom.this);
        progressChat.setTitle("Loading messages...");
        progressChat.show();

        //Get from previous activity which room is entered
        Intent intent = getIntent();
        roomname = intent.getStringExtra("Chatroom");

        recyclerView = findViewById(R.id.room_message);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //Later used when sending messages
        name = user.getDisplayName();
        uid = user.getUid();

        receiveMsg(roomname);

        //Setup for upload button
        findViewById(R.id.upload_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        findViewById(R.id.upload_cam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture,UPLOAD_FROM_CAMERA);
            }
        });

        //Setup for send button
        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView msgBox = findViewById(R.id.message_box);
                String msg = msgBox.getText().toString();
                //Check what kind of message the user should send: text msg, image msg or both
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
        }
        if (requestCode == UPLOAD_FROM_CAMERA && resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            //We extract the picture taken from data, make it as an bytearray
            //so we can upload it to the storage database
            if (bundle != null) {
                Bitmap bitmap = (Bitmap) bundle.get("data");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                uploadFromCamera(imageBytes);
            }
        }
    }

    private void uploadFromCamera(byte[] imageBytes){
        final ProgressDialog progressDialog = new ProgressDialog(ChatRoom.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        Date date = new Date();
        final int timeSent = (int) date.getTime();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + timeSent);
        storageReference.putBytes(imageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressLint("ShowToast")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ChatRoom.this,"Uploaded",Toast.LENGTH_LONG);
                path = null; //Changing path to null, so the user doesn't upload the same picture over and over agian
                //Get the url from image, and send it to the database
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        sendMsg(name,roomname,uid, String.valueOf(uri), String.valueOf(timeSent));
                    }
                });
                progressDialog.dismiss();

            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                progressDialog.dismiss();
                path = null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ChatRoom.this,"Error uploading picture", Toast.LENGTH_LONG);
                path = null;
            }
        });
    }

    private void uploadToStorage(){
        //Progressdialog indicating that something is uploading
        final ProgressDialog progressDialog = new ProgressDialog(ChatRoom.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        //When adding a picture with the same name in storage, the picture will disappear
        //thus using "date" will make it unlikely for it to happen
        Date date = new Date();
        final int timeSent = (int) date.getTime();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + timeSent);
        storageReference.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressLint("ShowToast")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ChatRoom.this,"Uploaded",Toast.LENGTH_LONG);
                path = null; //Changing path to null, so the user doesn't upload the same picture over and over agian
                //Get the url from image, and send it to the database
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        sendMsg(name,roomname,uid, String.valueOf(uri), String.valueOf(timeSent));
                    }
                });
                progressDialog.dismiss();

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
        //Popup to choose an image from your phone
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Choose an image"),UPLOAD_PICTURE);
    }

    @SuppressLint("ShowToast")
    private void sendMsg(String sender, final String chat, String suid, String msg, String msgType){
        DatabaseReference sendDB = FirebaseDatabase.getInstance().getReference();

        HashMap<String,String> hm = new HashMap<>();
        hm.put("sender",sender);
        hm.put("chat",chat);
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
        //Make room, push all information with the values given in hashmap
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
                Toast.makeText(ChatRoom.this,"Error, please reload the app",Toast.LENGTH_LONG);
        }
        //In order to check which chat was used the latest
        sendDB.child("chatStats").push().setValue(hm);

        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
        messaging.subscribeToTopic("Test");
        messaging.send(new RemoteMessage.Builder(uid + "@gcm.googleapis.com").setMessageId("21").addData("Test","Test").build());
        //sendNotification(chat,sender,msg);
    }
    private void sendNotification(String receiver, final String username, final String message){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"ETELLERANDET");
    }

    private void receiveMsg(final String chat){
        //Get all messages from the chatroom
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference(chat);
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Room> msgs = new ArrayList<>();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Room room = snapshot.getValue(Room.class);
                    assert room != null;
                    msgs.add(room);
                }

                messageRecyclerView = new MessageRecyclerView(ChatRoom.this,msgs);
                recyclerView.setAdapter(messageRecyclerView);
                progressChat.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressChat.dismiss();
                Toast.makeText(ChatRoom.this, "Error receiving message", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
