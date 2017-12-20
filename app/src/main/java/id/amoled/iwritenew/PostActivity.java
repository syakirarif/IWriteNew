package id.amoled.iwritenew;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import id.amoled.iwritenew.tools.CheckNetwork;
import id.amoled.iwritenew.tools.Shortcut;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = "PostActivity";

    public Button mBtnSubmit;
    public EditText mPostTitle, mPostDesc;
    public ImageButton mImg;

    public Uri mImageUri = null;

    public StorageReference mStorageRef;
    public DatabaseReference mDatabaseRef, mDbUserRef, mDbKaryaRef;

    private FirebaseUser mCurrentUser;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    final String userID = user.getUid();

    // menginisialisasi Progress Dialog
    public ProgressDialog mProgress;

    public static final int GALLERY_REQ = 1;
    public static final int SELECT_PICTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("blog");
        mDbUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());
        mDbKaryaRef = FirebaseDatabase.getInstance().getReference().child("karya");

        mProgress = new ProgressDialog(this);

        mBtnSubmit = (Button) findViewById(R.id.btn_AddPost_publish);
        mPostTitle = (EditText) findViewById(R.id.et_AddPost_title);
        mPostDesc = (EditText) findViewById(R.id.et_AddPost_body);
        mImg = (ImageButton) findViewById(R.id.imgBtnPost);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    public void selectImage(View view) {

        /*Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQ);*/

        Intent imgIntent = new Intent();
        imgIntent.setType("image/*");
        imgIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(imgIntent, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {

                mImageUri = data.getData();

                mImg.setImageURI(mImageUri);

                Picasso.with(PostActivity.this).load(data.getData()).noPlaceholder().centerCrop().fit()
                        .into((ImageView) findViewById(R.id.imgBtnPost));
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void submitPost(final View view) {

        mProgress.setMessage("Please wait...");

        final String title = mPostTitle.getText().toString().trim();
        final String body = mPostDesc.getText().toString().trim();

        if (CheckNetwork.isInternetAvailable(getApplicationContext())) {

            if (!title.isEmpty() && !body.isEmpty() && mImageUri != null) {

                mProgress.show();
                StorageReference filepath = mStorageRef.child("img_blog").child(userID).child(Shortcut.random());

                if (user != null) {

                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            DatabaseReference newPost = mDatabaseRef.push();

                            newPost.child("title").setValue(title);
                            newPost.child("desc").setValue(body);
                            newPost.child("user_id").setValue(userID);
                            newPost.child("image").setValue(downloadUrl.toString());
                            newPost.child("date_post").setValue(Shortcut.currentDate());
                            newPost.child("time_post").setValue(Shortcut.currentTime());

                            mDbUserRef.child("karya")
                                    .push()
                                    .child("post_id").setValue(newPost.getKey())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mProgress.dismiss();
                                                makeAlertDialogSimpleSuccess("Notifikasi", "Berhasil di posting.");
                                            }
                                        }
                                    });


                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgress.dismiss();
                                    makeAlertDialogSimpleFailed("Notifikasi", "Gagal memposting.");
                                }
                            });

                } else {
                    Shortcut.makeToast("Terjadi kesalahan, anda harus login dulu.", getApplicationContext());
                }

            }

        } else {
            makeAlertDialogSimpleFailed("Peringatan", "Tidak ada koneksi internet.");
            mProgress.dismiss();
        }
    }

    public void makeAlertDialogSimpleSuccess(String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PostActivity.this);

        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void makeAlertDialogSimpleFailed(String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PostActivity.this);

        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
