package project.fragments.self.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import adapter.JournalRecyclerAdapter;
import model.Journal;
import project.fragments.self.R;
import util.JournalAPI;

public class JournalListActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore database;
    private CollectionReference journalCollectionReference;
    private StorageReference storageReference;

    private List<Journal> journalList = new ArrayList<>();

    private RecyclerView rvJournalList;
    private JournalRecyclerAdapter journalRecyclerAdapter;

    private TextView tvNoThoughts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        configSupportActionBar();
        initActivityVariables();
        initFirebaseVariables();
    }

    private void configSupportActionBar() {
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
    }

    private void initActivityVariables() {
        tvNoThoughts = findViewById(R.id.tvNoThoughts);
        tvNoThoughts.setVisibility(View.INVISIBLE);

        rvJournalList = findViewById(R.id.rvJournalList);
        rvJournalList.setHasFixedSize(true);
        rvJournalList.setLayoutManager(new LinearLayoutManager(this));

    }

    private void initFirebaseVariables() {
        database = FirebaseFirestore.getInstance();
        journalCollectionReference = database.collection("Journal");
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

    }

    @Override
    protected void onStart() {
        super.onStart();

        journalCollectionReference.whereEqualTo("userId", JournalAPI.getInstance().getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            attachJournalsToRecyclerView(queryDocumentSnapshots);
                        } else {
                            tvNoThoughts.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void attachJournalsToRecyclerView(QuerySnapshot queryDocumentSnapshots) {
        for (QueryDocumentSnapshot journal : queryDocumentSnapshots) {
            Journal newJournal = journal.toObject(Journal.class);
            journalList.add(newJournal);
        }

        //Invoke RecyclerView
        journalRecyclerAdapter = new JournalRecyclerAdapter(JournalListActivity.this, journalList);
        rvJournalList.setAdapter(journalRecyclerAdapter);
        journalRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                onActionAdd();
                break;
            case R.id.action_signout:
                onActionSignout();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onActionAdd() {
        if (user != null && firebaseAuth != null) {
            startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
//            finish();
        }
    }

    private void onActionSignout() {
        if (user != null && firebaseAuth != null) {
            firebaseAuth.signOut();
            startActivity(new Intent(JournalListActivity.this, MainActivity.class));
//            finish();
        }
    }
}