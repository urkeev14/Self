package project.fragments.self.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import project.fragments.self.R;
import util.JournalAPI;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUsernameFromSignUp;
    private TextView tvEmailFromSignUp;
    private EditText etPasswordFromSignUp;
    private ProgressBar pbFromSignUp;
    private Button btnCreateAccountFromSignUp;
    private Button btnLoginFromSignUp;

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener fireAuthStateListener;

    private FirebaseFirestore database;
    private CollectionReference usersCollectionRef;

    //====================== Activity Lifecicle ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        configSupportActionBar();
        initActivityElements();
        initFirebaseElements();
        createAuthStateListener();
    }

    private void configSupportActionBar() {
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
    }

    private void initActivityElements() {

        etUsernameFromSignUp = findViewById(R.id.etUsernameFromSignUp);
        tvEmailFromSignUp = findViewById(R.id.tvEmailFromSignUp);
        etPasswordFromSignUp = findViewById(R.id.etPasswordFromSignUp);
        pbFromSignUp = findViewById(R.id.pbFromSignUp);

        btnCreateAccountFromSignUp = findViewById(R.id.btnCreateAccountFromSignUp);
        btnLoginFromSignUp = findViewById(R.id.btnLoginFromSignUp);
        btnLoginFromSignUp.setOnClickListener(this);
    }

    private void initFirebaseElements() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        usersCollectionRef = database.collection("Users");
    }

    private void createAuthStateListener() {
        fireAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {

                } else {

                }
            }
        };

        btnCreateAccountFromSignUp.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(fireAuthStateListener);
    }
    //=================================================================


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreateAccountFromSignUp:
                onBtnCreateAccountFromSignUp();
                break;
            case R.id.btnLoginFromSignUp:
                onBtnLoginFromSignUp();
            default:
                break;
        }
    }

    private void onBtnCreateAccountFromSignUp() {
        String username = etUsernameFromSignUp.getText().toString().trim();
        String password = etPasswordFromSignUp.getText().toString().trim();
        String email = tvEmailFromSignUp.getText().toString().trim();

        if (fieldsNotEmpty(email, password, username)) {
            createUserEmailAccount(email, password, username);
        } else {
            Toast.makeText(this, "Empty fields not allowed !", Toast.LENGTH_SHORT).show();
        }

    }

    private void onBtnLoginFromSignUp() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
    }

    private void createUserEmailAccount(String email, String password, final String username) {
        pbFromSignUp.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    onCreateUserTaskIsSuccessful(username);
                } else {
                    //onCreateUserTaskIsUnsuccessful();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, "Failed to create user...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onCreateUserTaskIsSuccessful(String username) {
        currentUser = firebaseAuth.getCurrentUser();
        assert currentUser != null;
        final String currentUserID = currentUser.getUid();

        Map<String, String> userObject = new HashMap<>();
        userObject.put("userId", currentUserID);
        userObject.put("username", username);

        usersCollectionRef.add(userObject)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        documentReference.get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (Objects.requireNonNull(task.getResult()).exists()) {
                                            pbFromSignUp.setVisibility(View.INVISIBLE);

                                            String name = task.getResult().getString("username");

                                            JournalAPI journalAPI = JournalAPI.getInstance();
                                            journalAPI.setUserId(currentUserID);
                                            journalAPI.setUsername(name);

                                            Intent intent = new Intent(SignUpActivity.this, PostJournalActivity.class);
                                            startActivity(intent);
                                        } else {
                                            pbFromSignUp.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, "userCollectionRef.AddOnFailureListener", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean fieldsNotEmpty(String email, String password, String username) {
        return !TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(username);
    }


}