package com.chat.omar.simplechat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ChatScreen extends AppCompatActivity {
    private FirebaseAuth fireAuth;
    private GoogleSignInClient googleSignInClient;
    private ArrayList<String> listRooms;
    private ArrayList<String> listDesc;
    private String[] listOfCR;
    private String[] description;
    private ArrayList<ChatRanking> tempList = new ArrayList<>();
    private Button button;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        setUpFirebase();

        //Predefined rooms and descriptions
        listRooms = new ArrayList<>();
        listRooms.add("School");
        listRooms.add("Golf club");
        listRooms.add("Teachers");
        listRooms.add("Students");
        listDesc = new ArrayList<>();
        listDesc.add("School chat, school only");
        listDesc.add("Golf chat for everyone who plays golf");
        listDesc.add("Teachers only chat");
        listDesc.add("Students only chat");
        //Let the user know that the chatrooms is being sat up
        progressDialog = new ProgressDialog(ChatScreen.this);
        progressDialog.setTitle("Setting up chatrooms...");
        progressDialog.show();
        //Setting up and sorting the chatrooms
        dataBaseCall();
        //Isn't really usefull, because the list refresh when a data is written to the db
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataBaseCall();
                pullToRefresh.setRefreshing(false);
            }
        });

    }

    private void dataBaseCall(){
        FirebaseDatabase fbase = FirebaseDatabase.getInstance();
        //For every message sent, the room and description is stored in
        //"chatStats", and the last element on the database is the newest message
        //so in order to find the latest message sent, we check all the elements in the db
        //trim out duplicates, revers the list and then make a listview out of it.
        DatabaseReference roomDB = fbase.getReference("chatStats");
        roomDB.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Store data in list
                for (DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    ChatRanking cr = snapshot.getValue(ChatRanking.class);
                    tempList.add(cr);
                }

                ArrayList<String> nameHolder = new ArrayList<>();
                ArrayList<ChatRanking> trimmed = new ArrayList<>();
                //Remove duplicates by making two list, one for storing
                // the ChatRanking object and name of the chatroom in string
                Collections.reverse(tempList);
                for (int i = 0; i < tempList.size(); i++) {
                    if(!nameHolder.contains(tempList.get(i).getChatRanking())){
                        nameHolder.add(tempList.get(i).getChatRanking());
                        trimmed.add(tempList.get(i));
                    }
                }

                listOfCR = new String[listRooms.size()];
                description = new String[listDesc.size()];
                //Making sure that if the database isn't used yet, the chatrooms that isn't yet used still works
                if(trimmed.size() < listRooms.size()){
                    for (int i = 0; i < trimmed.size(); i++) {
                        listOfCR[i] = trimmed.get(i).getChatRanking();
                        description[i] = trimmed.get(i).getDescription();
                    }
                    //Checking what is missing, and adding them to the list
                    int arrayCounter = 0;
                    int listCounter = trimmed.size();
                    for (String s:listRooms) {
                        if(!nameHolder.contains(s)){
                            listOfCR[listCounter] = listRooms.get(arrayCounter);
                            description[listCounter] = listDesc.get(arrayCounter);
                            listCounter++;
                        }
                        arrayCounter++;
                    }
                }else{ //If all chats are used, then no need for all that
                    for (int i = 0; i < trimmed.size(); i++) {
                        listOfCR[i] = trimmed.get(i).getChatRanking();
                        description[i] = trimmed.get(i).getDescription();
                    }
                }
                //Making adapter by calling ChatAdapter (own version of the ListView)
                setUpListvew();

            }

            @SuppressLint("ShowToast")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatScreen.this,"Error : " + databaseError,Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void setUpListvew(){
        ListView chatRooms = findViewById(R.id.listOfChat);
        //Making listview by using our own version
        ChatAdapter adapter = new ChatAdapter(this, listOfCR, description);

        chatRooms.setAdapter(adapter);

        progressDialog.dismiss();
        button.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);

        chatRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ChatScreen.this,ChatRoom.class);
                intent.putExtra("Chatroom",listOfCR[i]);

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = fireAuth.getCurrentUser();
        updateUI(user);
    }

    private void setUpFirebase(){
        fireAuth = FirebaseAuth.getInstance();

        findViewById(R.id.buttonFacebookSignout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireAuth.signOut();
                LoginManager.getInstance().logOut();
                updateUI(null);
            }
        });

        findViewById(R.id.buttonGoogleSignout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    private void signOut() {
        //Firebase sign out
        fireAuth.signOut();

        //Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            //Look for the user if he is logged as google or facebook
            for (UserInfo userInfo : user.getProviderData()) {
                if (userInfo.getProviderId().equals("facebook.com")) {
                    button = findViewById(R.id.buttonFacebookSignout);
                    return;
                }
            }
            button = findViewById(R.id.buttonGoogleSignout);

        } else {
            //Failed session, back to LogIn screen
            Intent intent = new Intent(ChatScreen.this, LogIn.class);
            Toast.makeText(ChatScreen.this, "Logged out", Toast.LENGTH_SHORT).show();
            ChatScreen.this.startActivity(intent);
            ChatScreen.this.finish();
        }
    }

}
