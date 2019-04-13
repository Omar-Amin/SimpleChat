package com.chat.omar.simplechat;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ChatScreen extends AppCompatActivity {
    private FirebaseAuth fireAuth;
    private GoogleSignInClient googleSignInClient;
    private ListView chatRooms;
    private ChatAdapter adapter;
    private String[] listOfCR;
    private String[] description;
    private int[] recentMessages;
    private BlockingDeque<String> blockingDeque = new LinkedBlockingDeque<>();
    private ArrayList<ChatRanking> tempList = new ArrayList<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        setUpFirebase();



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
                    System.out.println("CHATRANKING : " + cr.getChatRanking());
                    tempList.add(cr);
                }
                ArrayList<String> trimmed = new ArrayList<>();
                ArrayList<ChatRanking> ls = new ArrayList<>();
                String chats;
                Collections.reverse(tempList);
                for (int i = 0; i < tempList.size(); i++) {
                    if(!trimmed.contains(tempList.get(i).getChatRanking())){
                        trimmed.add(tempList.get(i).getChatRanking());
                        ls.add(tempList.get(i));
                    }
                    System.out.println("INSIDE ARRAYLIST: " + tempList.get(i));
                }
                if(ls.size() > 3 ){
                    listOfCR = new String[ls.size()];
                    description = new String[ls.size()];
                    for (int i = 0; i < ls.size(); i++) {
                        listOfCR[i] = ls.get(i).getChatRanking();
                        description[i] = ls.get(i).getDescription();
                    }
                }else{
                    listOfCR = new String[]{"School", "Golf club", "Students", "Teachers"};
                    description = new String[]{"s","s","s","s"};
                }


                setUpListvew();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setUpListvew(){
        chatRooms = findViewById(R.id.listOfChat);

        adapter = new ChatAdapter(this,listOfCR,description);

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
            ChatScreen.this.startActivity(intent);
            ChatScreen.this.finish();
        }
    }

}
