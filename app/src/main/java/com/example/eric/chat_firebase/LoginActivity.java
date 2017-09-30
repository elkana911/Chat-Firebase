package com.example.eric.chat_firebase;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eric.chat_firebase.pojo.User;
import com.example.eric.chat_firebase.util.FirebaseConst;
import com.example.eric.chat_firebase.util.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Contoh register/login menggunakan firebaseauth
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
//    private FirebaseUser user;

    private View mProgressView;
    private View mLoginFormView;

    @BindView(R.id.name)
    EditText mName;

    @BindView(R.id.email)
    EditText etEmail;

    @BindView(R.id.password)
    EditText etPassword;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        // Set up the login form.

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        progressDialog = new ProgressDialog(this);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        initFirebase();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();

        // ide brilian, langsung pindah ke screen mainactivity kalo auth sukses diawal !
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConst.DB_ROOT_USERS);
                FirebaseUser fbUser = firebaseAuth.getCurrentUser();

                if (fbUser != null) {
                    // mark as online
                    DatabaseReference userIdRef = databaseReference.child(fbUser.getUid());

                    if (userIdRef != null) {
                        userIdRef.child("online").setValue(Boolean.TRUE);
                    }

                    new SharedPrefUtil(getApplicationContext()).saveString(FirebaseConst.ARG_LAST_FIREBASE_UID, fbUser.getUid());

                    startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                } else {
                    // cant set offline. got Permission Denied error
                    /*
                    String lastFirebaseUid = new SharedPrefUtil(getApplicationContext()).getString(FirebaseConst.ARG_LAST_FIREBASE_UID);

                    if (!TextUtils.isEmpty(lastFirebaseUid)) {

                        DatabaseReference userIdRef = databaseReference.child(lastFirebaseUid);

                        if (userIdRef != null) {
                            userIdRef.child("online").setValue(Boolean.FALSE).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Offline success.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Offline failed.\n" + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }*/
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
//        ServiceUtils.stopServiceFriendChat(getApplicationContext(), false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {


//        if (true) {
//            startActivity(new Intent(this, MainActivity.class));
//            return;
//        }

        String email = etEmail.getText().toString();
        String pwd = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(getApplicationContext(), "Cannot empty email or pwd",
                    Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Sign In..\nPlease Wait...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();

                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Authentication failed.\n" + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @OnClick(R.id.btnRegister)
    public void onClickRegister() {
        String email = etEmail.getText().toString();
        String pwd = etPassword.getText().toString();
        final String name = mName.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Cannot empty email or pwd or name",
                    Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Registering..\nPlease Wait...");
        progressDialog.show();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseConst.DB_ROOT_USERS);

//        final User user = new User(name, "Available", false);

        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Registered successfully", Toast.LENGTH_SHORT).show();

                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            DatabaseReference userIdRef = databaseReference.child(currentUser.getUid());

                            String firebaseToken =  new SharedPrefUtil(getApplicationContext()).getString(FirebaseConst.ARG_FIREBASE_TOKEN);
                            User user = new User(currentUser.getUid(), name, "Available", false, firebaseToken);
                            if (userIdRef != null) {
                                user.setOnline(true);
                                userIdRef.setValue(user);
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "Register failed.\n" + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
/*
    public void addUserToDatabase(Context context, FirebaseUser firebaseUser) {
        User user = new User(firebaseUser.getUid(),
                firebaseUser.getEmail(),
                new SharedPrefUtil(context).getString(SyncStateContract.Constants.ARG_FIREBASE_TOKEN));
        FirebaseDatabase.getInstance()
                .getReference()
                .child(SyncStateContract.Constants.ARG_USERS)
                .child(firebaseUser.getUid())
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            // failed to add user
                        }
                    }
                });
    }
*/
}

