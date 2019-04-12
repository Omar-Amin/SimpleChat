package com.chat.omar.simplechat;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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

public class ChatScreen extends AppCompatActivity {
    private FirebaseAuth fireAuth;
    private GoogleSignInClient googleSignInClient;
    private ListView chatRooms;
    private ChatAdapter adapter;
    private String[] listOfCR = {"School","Teachers","Students","Golf club"};
    private int chevron;
    private String[] description = {"School chat, school only","Teachers only chat","Students only chat","Golf chat for everyone who plays golf"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        setUpFirebase();

        chatRooms = findViewById(R.id.listOfChat);

        adapter = new ChatAdapter(this,listOfCR,description);

        FirebaseDatabase fbase = FirebaseDatabase.getInstance();

        for (String s:listOfCR) {
            DatabaseReference roomDB = fbase.getReference(s);
            roomDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    DataSnapshot snapshot = null;

                    Iterable dsnap = dataSnapshot.getChildren();
                    while (dsnap.iterator().hasNext()){
                        snapshot = (DataSnapshot) dsnap.iterator().next();
                    }
                    try {
                        Room room = snapshot.getValue(Room.class);
                    }catch (NullPointerException e){
                        System.out.println("Chat not used yet");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        chatRooms.setAdapter(adapter);

        chatRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ChatScreen.this,ChatRoom.class);
                intent.putExtra("Chatroom",i);

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
