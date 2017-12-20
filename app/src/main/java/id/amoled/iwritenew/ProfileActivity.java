package id.amoled.iwritenew;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.amoled.iwritenew.tabs.SlidingTabLayout;
import id.amoled.iwritenew.tools.ProfilUser;
import id.amoled.iwritenew.tools.SessionManager;
import id.amoled.iwritenew.tools.Shortcut;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    private TextView mNama, mEmail, mNope;

    private CircleImageView mImg;

    private SessionManager sessionManager;

    private DatabaseReference mDbRefUser;

    private StorageReference mStorageImage;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseDatabase mFirebaseDatabase;

    private Uri mImageUri = null;

    // menginisialisasi Progress Dialog
    private ProgressDialog progressDialog;

    private static final int GALLRY_REQUEST = 1;

    private String UserID, userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sessionManager = new SessionManager(getApplicationContext());

        sessionManager = new SessionManager(this);
        //sessionManager.checkSetupProfil();

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mDbRefUser = mFirebaseDatabase.getReference().child("users");
        mStorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");

        FirebaseUser user = mAuth.getCurrentUser();
        UserID = user.getUid();

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

        mDbRefUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //showData(dataSnapshot);

                String image = (String) dataSnapshot.child(UserID).child("image").getValue();
                userImage = image;
                sessionManager.createImageProfileSession(image);
                //makeToast(image);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mNama = (TextView) findViewById(R.id.tvProfileNama);
        mEmail = (TextView) findViewById(R.id.tvProfileEmail);
        mNope = (TextView) findViewById(R.id.tvProfileNope);

        mImg = (CircleImageView) findViewById(R.id.imgBtnProfilFoto);

        HashMap<String, String> userSession = sessionManager.getUserDetails();

        String nama = userSession.get(SessionManager.KEY_NAME);
        String email = userSession.get(SessionManager.KEY_EMAIL);
        String nope = userSession.get(SessionManager.KEY_NOPE);
        final String image = userSession.get(SessionManager.KEY_IMG_PROFIL);

        mNama.setText(nama);
        mEmail.setText(email);
        mNope.setText(nope);
        //Picasso.with(getApplicationContext()).load(userImage).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(mImg);
        Picasso.with(getApplicationContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(mImg, new Callback() {
            @Override
            public void onSuccess() {
                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.default_avatar).into(mImg);
            }

            @Override
            public void onError() {
                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.default_avatar).into(mImg);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void selectProfileImg(View view) {

        Intent imgIntent = new Intent();
        imgIntent.setType("image/*");
        imgIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(imgIntent, "Select your profile picture"), GALLRY_REQUEST);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profil, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_profil_ubah) {

            Shortcut.makeToast("belum tersedia", getApplicationContext());

        } else if (id == R.id.action_profil_logout) {

            logoutProfile();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (requestCode == GALLERY_REQ && resultCode == RESULT_OK) {

            mImageUri = data.getData();

            mImg.setImageURI(mImageUri);
        }*/

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLRY_REQUEST) {

                //Get ImageURi and load with help of picasso
                //Uri selectedImageURI = data.getData();

                Uri mImageUri = data.getData();

                CropImage.activity(mImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();

                mImg.setImageURI(mImageUri);

                setupProfileImage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void setupProfileImage() {

        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        final String user_id = mAuth.getCurrentUser().getUid();

        StorageReference filepath = mStorageImage.child(user_id).child(mImageUri.getLastPathSegment());

        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                progressDialog.dismiss();

                String downloadUri = taskSnapshot.getDownloadUrl().toString();

                mDbRefUser.child(user_id).child("image").setValue(downloadUri);

                makeToast("Profile setup is success!");

                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
            }
        });

    }

    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            ProfilUser profilUser = new ProfilUser();

            profilUser.setDisp_name(ds.child(UserID).getValue(ProfilUser.class).getDisp_name());
            profilUser.setEmail(ds.child(UserID).getValue(ProfilUser.class).getEmail());
            profilUser.setDisp_nope(ds.child(UserID).getValue(ProfilUser.class).getDisp_nope());

            String nama = profilUser.getDisp_name().trim();
            String nope = profilUser.getDisp_nope().trim();
            String email = profilUser.getEmail().trim();

            mNama.setText(nama);
            mNope.setText(nope);
            mEmail.setText(email);

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

    public void logoutProfile() {

        sessionManager.logoutUser();
        mAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish();
    }

    public void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void showFollower(View view) {

        startActivity(new Intent(ProfileActivity.this, FollowerActivity.class));
    }

    public void showFollowing(View view) {

        startActivity(new Intent(ProfileActivity.this, FollowingActivity.class));
    }

    public void showKaryaku(View view) {
        startActivity(new Intent(ProfileActivity.this, KaryakuActivity.class));
    }
}
