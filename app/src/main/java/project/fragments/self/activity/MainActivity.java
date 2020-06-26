package project.fragments.self.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Currency;
import java.util.Objects;

import project.fragments.self.R;
import util.JournalAPI;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStart;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore database;
    private CollectionReference userCollectionReference;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configSupportActionBar();
        initActivityElements();
        initFirebaseElements();
    }
    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void configSupportActionBar() {
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
    }

    private void initActivityElements() {
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);
    }

    private void initFirebaseElements() {
        database = FirebaseFirestore.getInstance();
        userCollectionReference = database.collection("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        createAuthStateListeren();
    }

    private void createAuthStateListeren() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUser = firebaseAuth.getCurrentUser();
                    String currentUserId = currentUser.getUid();

                    userCollectionReference
                            .whereEqualTo("userId", currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.d(TAG, "onEvent: " + e.getMessage());
                                    } else {
                                        String name;

                                        assert queryDocumentSnapshots != null;
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                JournalAPI journalAPI = JournalAPI.getInstance();
                                                journalAPI.setUserId(snapshot.getString("userId"));
                                                journalAPI.setUsername(snapshot.getString("username"));

                                                startActivity(new Intent(MainActivity.this, JournalListActivity.class));
                                                finish();
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                onBtnStart();
                break;
            default:
                break;
        }
    }

    private void onBtnStart() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }
}