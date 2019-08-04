package com.geekykel.travelnigeria;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by GeekyKel on 8/2/2019
 */
public class FirebaseUtil {

    public static ArrayList<TravelDeal> mDeals;
    public static final int RC_SIGN_IN = 123;
    private static MainActivity caller;
    public static boolean isAdmin;

    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    private static FirebaseUtil firebaseUtil;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthStateListener;
    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageReference;


    private FirebaseUtil() {}

    public static void openFirebaseReference(String ref, final MainActivity callerActivity) {
        if (firebaseUtil == null) {
            firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;

            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    }
                    else {
                        String userId = firebaseAuth.getUid();
                        checkAdmin(userId);
                    }
                    //Toast.makeText(callerActivity.getBaseContext(), "Welcome back!", Toast.LENGTH_LONG).show();


                }
            };

            connectStorage();

        }

        mDeals = new ArrayList<>();

        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);
    }

    public static void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    private static void checkAdmin(String uid) {
        FirebaseUtil.isAdmin = false;

        DatabaseReference ref = mFirebaseDatabase.getReference().child("administrators")
                .child(uid);

        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FirebaseUtil.isAdmin = true;
                Log.d("FIREBASE", "You Are An Administrator");
                caller.showMenu();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.addChildEventListener(listener);
    }

    public static void attachListener() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    public static void detachListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    public static void connectStorage() {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("traveldeals_pictures");
    }

}
