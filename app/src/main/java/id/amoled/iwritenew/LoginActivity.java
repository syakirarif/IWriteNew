package id.amoled.iwritenew;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import id.amoled.iwritenew.tools.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText mEmail, mPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDataRefUser;

    /** menginisialisasi Progress Dialog
     */
    private ProgressDialog mProgress;

    // Session Manager Class
    SessionManager session;
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = (EditText) findViewById(R.id.etLoginEmail);
        mPassword = (EditText) findViewById(R.id.etLoginPass);

        mAuth = FirebaseAuth.getInstance();
        mDataRefUser = FirebaseDatabase.getInstance().getReference().child("users");
        mDataRefUser.keepSynced(true);
        mProgress = new ProgressDialog(this);

        // Session Manager
        session = new SessionManager(getApplicationContext());

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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void userLogin(View view) {

        mProgress.setMessage("Please wait");
        mProgress.show();

        String email = mEmail.getText().toString().trim();
        String pass = mPassword.getText().toString().trim();

        if (!email.isEmpty() && !pass.isEmpty()) {

            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    mProgress.dismiss();

                    if (task.isSuccessful()) {

                        checkUserExist();

                    } else {
                        makeToast("Login failed.");
                    }
                }
            });

        }


    }

    public void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void checkUserExist() {
        final String userID = mAuth.getCurrentUser().getUid();

        mDataRefUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mProgress.dismiss();

                if (dataSnapshot.hasChild(userID)){

                    mDataRefUser.child(userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String email = (String) dataSnapshot.child("email").getValue();
                            String name = (String) dataSnapshot.child("name").getValue();
                            String nope = (String) dataSnapshot.child("nope").getValue();
                            String image = (String) dataSnapshot.child("image").getValue();

                            session.createLoginSession(name, email, userID);
                            session.createSetupProfilSession(name, nope, image);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();

                    makeToastShort("Login success");

                }else{
                    makeToastShort("Account not found. Please create a new one.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void toRegister(View view) {

        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    public void makeToastShort(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }

    public void toResetPassword(View view) {
        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
    }
}
