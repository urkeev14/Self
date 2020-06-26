package project.fragments.self.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

import model.Journal;
import project.fragments.self.R;
import util.JournalAPI;

import static constants.Constants.GALLERY_CODE;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivImagePostCameraButton;
    private ImageView ivPostImage;
    private TextView tvPostUsername;
    private TextView tvPostDate;
    private EditText etPostTitle;
    private EditText etPostDescription;
    private ProgressBar pbPostJournal;
    private Button btnPostSaveJournal;

    private String currentUserId;
    private String currentUsername;

    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseFirestore database;
    private StorageReference storageReference;
    private CollectionReference journalCollectionReference;

    private Uri imageURI;
    private String postTitle;
    private String postThoughts;
    private String TAG = "PostJournalActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        configSupportActionBar();
        initActivityElements();
        configureButtonFunctionality();
        fetchActivityData();
        initFirebaseElements();
        configureAuthStateListener();
    }

    private void configSupportActionBar() {
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void initActivityElements() {
        tvPostUsername = findViewById(R.id.tvPostUsername);
        tvPostDate = findViewById(R.id.tvPostDate);
        ivImagePostCameraButton = findViewById(R.id.ivImagePostCameraButton);
        ivPostImage = findViewById(R.id.ivPostImage);
        etPostTitle = findViewById(R.id.etPostTitle);
        etPostDescription = findViewById(R.id.etDescription);
        pbPostJournal = findViewById(R.id.pbPostJournal);
        pbPostJournal.setVisibility(View.INVISIBLE);
        btnPostSaveJournal = findViewById(R.id.btnPostSaveJournal);

    }

    private void configureButtonFunctionality() {
        btnPostSaveJournal.setOnClickListener(this);
        ivImagePostCameraButton.setOnClickListener(this);
    }

    private void fetchActivityData() {
        pbPostJournal.setVisibility(View.INVISIBLE);

        if (JournalAPI.getInstance() != null) {
            currentUserId = JournalAPI.getInstance().getUserId();
            currentUsername = JournalAPI.getInstance().getUsername();

            tvPostUsername.setText(currentUsername);
        }
    }

    private void initFirebaseElements() {
        database = FirebaseFirestore.getInstance();
        journalCollectionReference = database.collection("Journal");
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void configureAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
            }
        };
    }

    //OnClickListener

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPostSaveJournal:
                onBtnPostSaveJournal();
                break;
            case R.id.ivImagePostCameraButton:
                onIvImagePostCameraButton();
                break;
            default:
                break;
        }
    }

    private void onBtnPostSaveJournal() {

        postTitle = etPostTitle.getText().toString().trim();
        postThoughts = etPostDescription.getText().toString().trim();

        if (postElementsNotEmpty()) {
            /*
             * String and similar data go to Firebase Database.
             * Pictures go to Firebase Storage.
             * We will get picture link from Firebase Storage
             * */
            pbPostJournal.setVisibility(View.VISIBLE);

            final StorageReference imageFilepath = storageReference
                    .child("journal_images")
                    .child("my_image" + Timestamp.now().getSeconds());

            imageFilepath.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    imageFilepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String imageURL = uri.toString();

                            Journal journal = new Journal();
                            journal.setTitle(postTitle);
                            journal.setThought(postThoughts);
                            journal.setImageUrl(imageURL);
                            journal.setTimeAdded(new Timestamp(new Date()));
                            journal.setUserName(currentUsername);
                            journal.setUserId(currentUserId);

                            //TODO: Invoke our collectionReference and save Journal instance
                            journalCollectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    pbPostJournal.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                }
                            });
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pbPostJournal.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            pbPostJournal.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Fill all field before saving ! :)", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean postElementsNotEmpty() {
        return !TextUtils.isEmpty(postTitle) &&
                !TextUtils.isEmpty(postThoughts) &&
                imageURI != null;
    }

    private void onIvImagePostCameraButton() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {

            if (data != null) {
                imageURI = data.getData();
                ivPostImage.setImageURI(imageURI); //Showing selected image
            } else {

            }

        }
    }
}