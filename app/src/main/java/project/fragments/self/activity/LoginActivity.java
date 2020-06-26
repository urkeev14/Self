package project.fragments.self.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import project.fragments.self.R;
import util.JournalAPI;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private AutoCompleteTextView tvEmailLogin;
    private EditText etPasswordLogin;
    private ProgressBar pbLogin;
    private Button btnLogin;
    private Button btnSignUp;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener fireAuthStateListener;
    private FirebaseUser currentUser;

    //Firestore connection
    private FirebaseFirestore database;
    private CollectionReference userCollectionReference;
    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        configSupportActionBar();
        initActivityElements();
        initFirebaseElements();
    }

    private void configSupportActionBar() {
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
    }

    private void initActivityElements() {
        tvEmailLogin = findViewById(R.id.tvEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        pbLogin = findViewById(R.id.pbLogin);
        pbLogin.setVisibility(View.INVISIBLE);

        btnLogin = findViewById(R.id.btnLoginFromSignUp);
        btnSignUp = findViewById(R.id.btnSignUpFromLogin);
        btnLogin.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);

    }

    private void initFirebaseElements() {
        database = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userCollectionReference = database.collection("Users");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLoginFromSignUp:
                onBtnLogin();
                break;
            case R.id.btnSignUpFromLogin:
                onBtnSignUp();
                break;
            default:
                break;
        }
    }

    private void onBtnLogin() {
        String email = tvEmailLogin.getText().toString().trim();
        String password = etPasswordLogin.getText().toString().trim();

        if (fieldsNotEmpty(email, password)) {
            pbLogin.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user != null;
                            final String currentUserId = user.getUid();

                            userCollectionReference.whereEqualTo("userId", currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                            @Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.d(TAG, "onEvent: QueryDocumentSnapshot EXCEPTION");
                                                return;
                                            }
                                            assert queryDocumentSnapshots != null;
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                pbLogin.setVisibility(View.INVISIBLE);

                                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                    JournalAPI.getInstance().setUsername(snapshot.getString("username"));
                                                    JournalAPI.getInstance().setUserId(currentUserId);
                                                }

                                                startActivity(new Intent(LoginActivity.this, PostJournalActivity.class));

                                            }
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception e) {
                    pbLogin.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            pbLogin.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean fieldsNotEmpty(String email, String password) {
        return !TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password);
    }

    private void onBtnSignUp() {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }
}