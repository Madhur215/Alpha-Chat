package com.example.alphachat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.alphachat.R;
import com.example.alphachat.Util.PrefUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;


public class SplashActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 101;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private GoogleApiClient googleApiClient;
    private PrefUtils pr;
    String idToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        pr = new PrefUtils(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
//        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            idToken = account.getIdToken();
            String name = account.getDisplayName();
            String email = account.getEmail();
            String image = account.getPhotoUrl().toString();
            String uid = account.getId();
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuthWithGoogle(credential, email, name, image, uid);
        }
        else{
            Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(AuthCredential credential, final String email,
                                        final String name, final String photoUrl, final String uid) {
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            pr.SaveDetails(name, uid);
                            Toast.makeText(SplashActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
                            mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
                            mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(
                                        @NotNull DataSnapshot snapshot) {
                                    if (!snapshot.hasChild(uid)) {
                                        mDatabaseReference.child(uid + "/" + "name").setValue(name);
                                        mDatabaseReference.child(uid + "/" + "image").setValue(photoUrl);
                                        mDatabaseReference.child(uid + "/" + "uid").setValue(uid);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(SplashActivity.this, "An error occurred!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(SplashActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    }

}