package id.amoled.iwritenew;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import id.amoled.iwritenew.tools.SessionManager;

public class SetupActivity extends AppCompatActivity {

    private ImageButton imageButton;
    private Button button;
    private EditText etNama, etNope;

    // menginisialisasi Progress Dialog
    private ProgressDialog progressDialog;

    private Uri mImageUri = null;

    private static final int GALLRY_REQUEST = 1;

    private FirebaseAuth mAuth;

    private StorageReference mStorageImage;

    private DatabaseReference mDbRefImageProfile;

    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        sessionManager = new SessionManager(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        mDbRefImageProfile = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");

        imageButton = (ImageButton)findViewById(R.id.imgBtnFotoProfil);
        etNama = (EditText)findViewById(R.id.etSetupNama);
        etNope = (EditText) findViewById(R.id.etSetupNope);
        button = (Button)findViewById(R.id.btnSetupSimpan);
    }


    public void selectProfilePic(View view) {

        Intent imgIntent = new Intent();
        imgIntent.setType("image/*");
        imgIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(imgIntent, "Select your profile picture"), GALLRY_REQUEST);

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

                imageButton.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void setupProfile(View view) {

        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        final String nama = etNama.getText().toString().trim();
        final String nope = etNope.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();

        if (!nama.isEmpty() && mImageUri != null){

            StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    progressDialog.dismiss();

                    String downloadUri = taskSnapshot.getDownloadUrl().toString();

                    mDbRefImageProfile.child(user_id).child("disp_name").setValue(nama);
                    mDbRefImageProfile.child(user_id).child("disp_nope").setValue(nope);
                    mDbRefImageProfile.child(user_id).child("disp_image").setValue(downloadUri);

                    sessionManager.createSetupProfilSession(nama, nope, downloadUri);

                    makeToast("Profile setup is success!");

                    startActivity(new Intent(SetupActivity.this, MainActivity.class));
                }
            });

        }else{
            progressDialog.dismiss();
            makeToast("Cannot be empty");
        }

    }

    public void makeToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}