package id.amoled.iwritenew;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import id.amoled.iwritenew.tools.Karyaku;

public class KaryakuActivity extends AppCompatActivity {

    private static final String TAG = "KaryakuActivity";

    private DatabaseReference mDbRefKarya, mDbRefUsers;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String current_user_id = null;

    public RecyclerView mKaryakuList;

    private TextView mNoKarya;

    /** menginisialisasi Progress Bar
     */
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_karyaku);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.pbKaryaku);
        progressBar.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(loginIntent);
                } else {

                    mDbRefUsers = FirebaseDatabase.getInstance().getReference().child("users");

                    mDbRefUsers.keepSynced(true);

                    mNoKarya = (TextView) findViewById(R.id.tv_rv_no_karya);

                    mKaryakuList = (RecyclerView) findViewById(R.id.rv_karyaku_list);

                    tampilkanData();
                }

            }
        };

    }

    private void tampilkanData() {

        mDbRefKarya = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("karya");
        mDbRefKarya.keepSynced(true);


        if (getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mKaryakuList.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        } else {
            mKaryakuList.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        }

        firebaseAdaper();

    }

    public void firebaseAdaper() {

        FirebaseRecyclerAdapter<Karyaku, KaryakuViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Karyaku, KaryakuViewHolder>(

                Karyaku.class,
                R.layout.row_karyaku,
                KaryakuViewHolder.class,
                mDbRefKarya
        ) {
            @Override
            protected void populateViewHolder(final KaryakuViewHolder viewHolder, Karyaku model, final int position) {

                viewHolder.setJudulKarya(model.getPost_id());
                viewHolder.setImageKarya(getApplicationContext(), model.getPost_id());
                progressBar.setVisibility(View.GONE);
                mNoKarya.setVisibility(View.GONE);
            }
        };

        firebaseRecyclerAdapter.notifyDataSetChanged();

        mKaryakuList.setAdapter(firebaseRecyclerAdapter);

        mDbRefKarya.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                mNoKarya.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private static class KaryakuViewHolder extends RecyclerView.ViewHolder {

        View mView;

        FirebaseAuth mAuth;
        DatabaseReference mDbRefBlog;

        public KaryakuViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mAuth = FirebaseAuth.getInstance();

            mDbRefBlog = FirebaseDatabase.getInstance().getReference().child("blog");
            mDbRefBlog.keepSynced(true);
        }

        private void setJudulKarya(String postID) {

            final TextView judul = mView.findViewById(R.id.tvRowKaryakuJudul);

            mDbRefBlog.child(postID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String judulnya = (String) dataSnapshot.child("title").getValue();
                    judul.setText(judulnya);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        private void setImageKarya(final Context context, final String postID) {
            final ImageView foto = mView.findViewById(R.id.imgRowKaryakuGambar);

            mDbRefBlog.child(postID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String mImage = (String) dataSnapshot.child("image").getValue();

                    Picasso.with(context).load(mImage).networkPolicy(NetworkPolicy.OFFLINE).into(foto, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.with(context).load(mImage).placeholder(R.drawable.default_image).into(foto);
                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(mImage).placeholder(R.drawable.default_image).into(foto);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


    }

    public void checkUserExist() {

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    final String userID = mAuth.getCurrentUser().getUid();

                    mDbRefUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(userID)) {

                                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(loginIntent);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(loginIntent);
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthStateListener);

        checkUserExist();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
