package com.chat.omar.simplechat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        setUpFirebase();
        listRooms = new ArrayList<>();
        listRooms.add("School");
        listRooms.add("Golf club");
        listRooms.add("Teachers");
        listRooms.add("Students");
        listDesc = new ArrayList<>();
        listDesc.add("School chat, school only");
        listDesc.add("Teachers only chat");
        listDesc.add("Students only chat");
        listDesc.add("Golf chat for everyone who plays golf");

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


        DatabaseReference roomDB = fbase.getReference("chatStats");
        roomDB.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setUpListvew(){
        ListView chatRooms = findViewById(R.id.listOfChat);

        ChatAdapter adapter = new ChatAdapter(this, listOfCR, description);

        chatRooms.setAdapter(adapter);

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
        // Firebase sign out
        fireAuth.signOut();

        // Google sign out
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
            for (UserInfo userInfo : user.getProviderData()) {
                if (userInfo.getProviderId().equals("facebook.com")) {
                    findViewById(R.id.buttonFacebookSignout).setVisibility(View.VISIBLE);
                    return;
                }
            }
            findViewById(R.id.buttonGoogleSignout).setVisibility(View.VISIBLE);

        } else {
            Intent intent = new Intent(ChatScreen.this, LogIn.class);
            Toast.makeText(ChatScreen.this, "Logged out", Toast.LENGTH_SHORT).show();
            ChatScreen.this.startActivity(intent);
            ChatScreen.this.finish();
        }
    }

}
