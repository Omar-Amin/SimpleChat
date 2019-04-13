package com.chat.omar.simplechat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String newToken = FirebaseInstanceId.getInstance().getToken();
        renewToken(newToken);
    }

    private void renewToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }
}
