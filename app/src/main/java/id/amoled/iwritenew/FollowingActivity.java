package id.amoled.iwritenew;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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

import de.hdodenhof.circleimageview.CircleImageView;
import id.amoled.iwritenew.tools.Follower;

public class FollowingActivity extends AppCompatActivity {

    private static final String TAG = "FollowingActivity";

    private DatabaseReference mDbRefFollowing, mDbRefUsers;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String target_user_id = null;

    public RecyclerView mFollowingList;

    private TextView mNoFollowing;

    /** menginisialisasi Progress Bar
     */
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.pbFollowing);
        progressBar.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(loginIntent);
                } else {

                    mNoFollowing = (TextView) findViewById(R.id.tv_rv_no_following);

                    mDbRefUsers = FirebaseDatabase.getInstance().getReference().child("users");
                    mDbRefFollowing = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("following");

                    mDbRefUsers.keepSynced(true);
                    mDbRefFollowing.keepSynced(true);

                    mFollowingList = (RecyclerView) findViewById(R.id.rv_following_list);

                    // Replace 'android.R.id.list' with the 'id' of your RecyclerView
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    mLayoutManager.setReverseLayout(true);
                    mLayoutManager.setStackFromEnd(true);

                    mFollowingList.setHasFixedSize(true);
                    mFollowingList.setLayoutManager(mLayoutManager);

                    firebaseAdaper();
                }

            }
        };

    }

    public void firebaseAdaper() {

        FirebaseRecyclerAdapter<Follower, FollowingViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Follower, FollowingViewHolder>(

                Follower.class,
                R.layout.row_follow,
                FollowingViewHolder.class,
                mDbRefFollowing
        ) {
            @Override
            protected void populateViewHolder(final FollowingViewHolder viewHolder, Follower model, final int position) {

                viewHolder.setName(model.getUser_id());
                viewHolder.setImageProfil(getApplicationContext(), model.getUser_id());
                progressBar.setVisibility(View.GONE);
                mNoFollowing.setVisibility(View.GONE);
            }
        };

        firebaseRecyclerAdapter.notifyDataSetChanged();

        mFollowingList.setAdapter(firebaseRecyclerAdapter);

        mDbRefFollowing.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                mNoFollowing.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static class FollowingViewHolder extends RecyclerView.ViewHolder {

        View mView;

        FirebaseAuth mAuth;
        DatabaseReference mDbRefUsers;

        CircleImageView fotoProfil;

        public FollowingViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mAuth = FirebaseAuth.getInstance();

            mDbRefUsers = FirebaseDatabase.getInstance().getReference().child("users");

            fotoProfil = (CircleImageView) mView.findViewById(R.id.imgRowFollowFoto);
        }

        private void setName(String current_userID) {

            final TextView user_name = mView.findViewById(R.id.tvRowFollowUsername);

            mDbRefUsers.child(current_userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String nama = (String) dataSnapshot.child("name").getValue();
                    user_name.setText(nama);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        private void setImageProfil(final Context context, final String current_userID) {
            final CircleImageView foto = mView.findViewById(R.id.imgRowFollowFoto);

            mDbRefUsers.child(current_userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String mImage = (String) dataSnapshot.child("image").getValue();

                    Picasso.with(context).load(mImage).networkPolicy(NetworkPolicy.OFFLINE).into(foto, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.with(context).load(mImage).placeholder(R.drawable.default_avatar).into(foto);
                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(mImage).placeholder(R.drawable.default_avatar).into(foto);
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
