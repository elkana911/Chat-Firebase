package com.example.eric.chat_firebase.fcm;

import android.util.Log;

import com.example.eric.chat_firebase.util.FirebaseConst;
import com.example.eric.chat_firebase.util.SharedPrefUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Eric on 13-Dec-16.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.e(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
//        Storage.savePref(Storage.KEY_ANDROID_ID, refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
        new SharedPrefUtil(getApplicationContext()).saveString(FirebaseConst.ARG_FIREBASE_TOKEN, token);

        // just send the token even no name
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child(FirebaseConst.DB_ROOT_USERS)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(FirebaseConst.ARG_FIREBASE_TOKEN)
                    .setValue(token);
        }
    }
}