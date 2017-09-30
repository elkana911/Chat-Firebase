package com.example.eric.chat_firebase;

/*
mostly contek dari:
http://ghost-chdev.rhcloud.com/one-to-one-chat-using-firebase-for-android/

learn chat method:
https://www.androidtutorialpoint.com/firebase/real-time-android-chat-application-using-firebase-tutorial/

Firebase account at
    https://console.firebase.google.com/project/mychat-7a401/overview

 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.eric.chat_firebase.fcm.FirebaseChatMainApp;
import com.example.eric.chat_firebase.fragments.FragmentChatWith;
import com.example.eric.chat_firebase.fragments.FragmentContacts;
import com.example.eric.chat_firebase.pojo.User;
import com.example.eric.chat_firebase.util.FirebaseConst;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements FragmentContacts.OnFragmentInteractionListener,
    FragmentChatWith.OnFragmentInteractionListener{

    private FirebaseAuth fbAuth;
    private FirebaseDatabase fbDB;
    private DatabaseReference chat_users;

    private String userDisplayName;
    private String userFBToken;

    private ProgressDialog progressDialog;

    private final static String TAG_FRAGMENT = "ROOT_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        fb_Init();

        displayView(FragmentContacts.newInstance(null, null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseChatMainApp.setChatActivityOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseChatMainApp.setChatActivityOpen(false);
    }

    private void fb_Init() {
        fbDB = FirebaseDatabase.getInstance();
        fbAuth = FirebaseAuth.getInstance();
        chat_users = fbDB.getReference().child(FirebaseConst.DB_ROOT_USERS);

        //ambil displaynamenya
        chat_users.child(fbAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                userDisplayName = currentUser.getName();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

//    https://medium.com/@bherbst/managing-the-fragment-back-stack-373e87e4ff62
    private void displayView(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(TAG_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(TAG_FRAGMENT)
                .commit();
    }

    private void addFragmentOnTop(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {

        int x = getSupportFragmentManager().getBackStackEntryCount();

        // kalau x = 1 dan pencet backpress akan blank. maka diinginkan kalo x =1 tinggal minimize
        if (x < 2)
            moveTaskToBack(true);
        else
            super.onBackPressed();
    }

    @OnClick(R.id.btnLogout)
    public void onClickLogout() {

        if (fbAuth == null) {
            Toast.makeText(this, "Please relogin", Toast.LENGTH_LONG).show();

            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();

            return;
        }

        DatabaseReference currentUser = chat_users.child(fbAuth.getCurrentUser().getUid());

        if (currentUser != null) {
            //WARNING: meskipun currentUser not null, but if deleted on web, below listeners occurred nothing ! i dont know why
            /*
            currentUser.child("online").setValue(Boolean.FALSE, new DatabaseReference.CompletionListener() {

                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        System.out.println("Data could not be saved. " + databaseError.getMessage());
                    } else {
                        System.out.println("Data saved successfully.");
                    }
                }
            });
            */
            currentUser.child("online").setValue(Boolean.FALSE).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        fbAuth.signOut();

                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onContactSelected(User user) {
        addFragmentOnTop(FragmentChatWith.newInstance(fbAuth.getCurrentUser().getUid(), userDisplayName, user));
    }

    @Override
    public void onLoadContacts() {

    }
}
