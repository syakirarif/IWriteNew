package id.amoled.iwritenew;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.amoled.iwritenew.tools.Shortcut;

public class DetailBlogActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "DetailBlogActivity";

    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean mIsAvatarShown = true;

    private int mMaxScrollSize;

    private String terget_post_key = null;
    private String target_user_id = null;
    private String following_key = null;

    private DatabaseReference dbRefBlog, dbRefUser, dbRefFollowing, dbRefFollower;
    private FirebaseAuth mAuth;

    private TextView user_nama, blog_judul, blog_isi;
    private ImageView blog_images;
    private CircleImageView user_image;

    private boolean isFollowing = false;
    private boolean isLiked = false;
    private boolean isShown = false;

    private ImageButton mImgBtnLike, mImgBtnComment;

    private CoordinatorLayout coordinatorLayout;

    Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_blog);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutDetailBlog);

        dbRefBlog = FirebaseDatabase.getInstance().getReference().child("blog");
        dbRefUser = FirebaseDatabase.getInstance().getReference().child("users");
        dbRefFollowing = FirebaseDatabase.getInstance().getReference().child("following");
        dbRefFollower = FirebaseDatabase.getInstance().getReference().child("follower");

        mAuth = FirebaseAuth.getInstance();

        terget_post_key = getIntent().getExtras().getString("blog_id");

        AppBarLayout appbarLayout = (AppBarLayout) findViewById(R.id.materialup_appbar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.materialup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();

        user_nama = (TextView) findViewById(R.id.detailBlog_user_name);
        blog_judul = (TextView) findViewById(R.id.detailBlog_judul);
        blog_isi = (TextView) findViewById(R.id.detailBlog_detail_blog);

        blog_images = (ImageView) findViewById(R.id.detailBlog_image_blog);
        user_image = (CircleImageView) findViewById(R.id.detailBlog_user_image);

        mImgBtnLike = (ImageButton) findViewById(R.id.imgBtnPostLike);
        mImgBtnComment = (ImageButton) findViewById(R.id.imgBtnCommentPost);

        setLikeBtn();

        dbRefBlog.child(terget_post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String bTitle = (String) dataSnapshot.child("title").getValue();
                final String bIsi = (String) dataSnapshot.child("desc").getValue();
                final String blog_image = (String) dataSnapshot.child("image").getValue();
                target_user_id = (String) dataSnapshot.child("user_id").getValue();

                dbRefUser.child(target_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String user_name = (String) dataSnapshot.child("name").getValue();
                        final String user_photo = (String) dataSnapshot.child("image").getValue();

                        user_nama.setText(user_name);
                        Picasso.with(getApplicationContext()).load(user_photo).networkPolicy(NetworkPolicy.OFFLINE).into(user_image, new Callback() {
                            @Override
                            public void onSuccess() {
                                Picasso.with(getApplicationContext()).load(user_photo).placeholder(R.drawable.default_avatar).into(user_image);
                            }

                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext()).load(user_photo).placeholder(R.drawable.default_avatar).into(user_image);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Picasso.with(getApplicationContext()).load(blog_image).networkPolicy(NetworkPolicy.OFFLINE).into(blog_images, new Callback() {
                    @Override
                    public void onSuccess() {
                        Picasso.with(getApplicationContext()).load(blog_image).placeholder(R.drawable.default_image).into(blog_images);
                    }

                    @Override
                    public void onError() {
                        Picasso.with(getApplicationContext()).load(blog_image).placeholder(R.drawable.default_image).into(blog_images);
                    }
                });

                blog_judul.setText(bTitle);

                blog_isi.setText(bIsi);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dialog = new Dialog(this);

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dbRefUser.child(target_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String user_photo = (String) dataSnapshot.child("image").getValue();

                        //Shortcut.makeToast(user_photo, getApplicationContext());

                        showPopup(user_photo);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

        blog_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dbRefBlog.child(terget_post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String imageBlog = (String) dataSnapshot.child("image").getValue();

                        showPopupImageBlog(imageBlog);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });


    }

    public void showPopup(final String uPhoto) {

        final ImageButton mImgBtnSubscribe;

        dialog.setContentView(R.layout.popup_user);

        final ImageView imgPhotoUser = dialog.findViewById(R.id.imgPopupFotoUser);
        mImgBtnSubscribe = (ImageButton) dialog.findViewById(R.id.imgBtnPopupFollow);

        dbRefFollower.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(target_user_id).hasChild(mAuth.getCurrentUser().getUid())) {

                    mImgBtnSubscribe.setImageResource(R.drawable.ic_add_alert_blue_24dp);

                } else {

                    mImgBtnSubscribe.setImageResource(R.drawable.ic_add_alert_black_24dp);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!target_user_id.equals(mAuth.getCurrentUser().getUid())) {

            mImgBtnSubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    isFollowing = true;

                    dbRefFollower.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (isFollowing) {

                                if (dataSnapshot.child(target_user_id).hasChild(mAuth.getCurrentUser().getUid())) {

                                    dbRefUser.child(target_user_id)
                                            .child("follower")
                                            .child(mAuth.getCurrentUser().getUid())
                                            .removeValue();

                                    dbRefUser.child(mAuth.getCurrentUser().getUid())
                                            .child("following")
                                            .child(target_user_id)
                                            .removeValue();

                                    dbRefFollower.child(target_user_id)
                                            .child(mAuth.getCurrentUser().getUid())
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Shortcut.makeSnackBar(coordinatorLayout, "Unfollowed");
                                            }
                                        }
                                    });

                                    isFollowing = false;

                                } else {

                                    HashMap<String, String> hmFollowing = new HashMap<String, String>();
                                    hmFollowing.put("user_id", target_user_id);

                                    HashMap<String, String> hmFollower = new HashMap<String, String>();
                                    hmFollower.put("user_id", mAuth.getCurrentUser().getUid());

                                    dbRefUser.child(target_user_id)
                                            .child("follower")
                                            .child(mAuth.getCurrentUser().getUid())
                                            .setValue(hmFollower);

                                    dbRefUser.child(mAuth.getCurrentUser().getUid())
                                            .child("following")
                                            .child(target_user_id)
                                            .setValue(hmFollowing);

                                    dbRefFollower.child(target_user_id)
                                            .child(mAuth.getCurrentUser().getUid())
                                            .setValue(mAuth.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Shortcut.makeSnackBar(coordinatorLayout, "Followed");
                                            }
                                        }
                                    });

                                    isFollowing = false;

                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });

        } else {
            mImgBtnSubscribe.setVisibility(View.INVISIBLE);
        }

        Picasso.with(getApplicationContext()).load(uPhoto).networkPolicy(NetworkPolicy.OFFLINE).into(imgPhotoUser, new Callback() {
            @Override
            public void onSuccess() {
                Picasso.with(getApplicationContext()).load(uPhoto).placeholder(R.drawable.default_avatar).into(imgPhotoUser);
            }

            @Override
            public void onError() {
                Picasso.with(getApplicationContext()).load(uPhoto).placeholder(R.drawable.default_avatar).into(imgPhotoUser);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    public void showPopupImageBlog(final String uImageBlog) {

        dialog.setContentView(R.layout.popup_image_blog);

        final ImageView imgBlog = dialog.findViewById(R.id.imgPopupImageBlog);

        Picasso.with(getApplicationContext()).load(uImageBlog).networkPolicy(NetworkPolicy.OFFLINE).into(imgBlog, new Callback() {
            @Override
            public void onSuccess() {
                Picasso.with(getApplicationContext()).load(uImageBlog).placeholder(R.drawable.default_avatar).into(imgBlog);
            }

            @Override
            public void onError() {
                Picasso.with(getApplicationContext()).load(uImageBlog).placeholder(R.drawable.default_avatar).into(imgBlog);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

    public static void start(Context c) {
        c.startActivity(new Intent(c, DetailBlogActivity.class));
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int percentage = (Math.abs(i)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

            user_image.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            user_image.animate()
                    .scaleY(1).scaleX(1)
                    .start();
        }
    }

    public void likePost(View view) {

        isLiked = true;

        dbRefBlog.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (isLiked) {

                    if (dataSnapshot.child(terget_post_key).child("likes").hasChild(mAuth.getCurrentUser().getUid())) {

                        dbRefBlog.child(terget_post_key).child("likes").child(mAuth.getCurrentUser().getUid()).removeValue();

                        Shortcut.makeSnackBar(coordinatorLayout, "You unlike this.");

                        isLiked = false;

                    } else {

                        dbRefBlog.child(terget_post_key).child("likes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());

                        Shortcut.makeSnackBar(coordinatorLayout, "You like this.");

                        isLiked = false;

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setLikeBtn() {

        dbRefBlog.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(terget_post_key).child("likes").hasChild(mAuth.getCurrentUser().getUid())) {

                    mImgBtnLike.setImageResource(R.drawable.ic_thumb_up_blue_24dp);

                } else {

                    mImgBtnLike.setImageResource(R.drawable.ic_thumb_up_black_24dp);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void commentPost(View view) {

        LinearLayout llComment = (LinearLayout) findViewById(R.id.llCommentBox);

        if (isShown) {

            llComment.setVisibility(View.GONE);
            mImgBtnComment.setImageResource(R.drawable.ic_insert_comment_black_24dp);
            isShown = false;


        } else {

            llComment.setVisibility(View.VISIBLE);
            mImgBtnComment.setImageResource(R.drawable.ic_insert_comment_blue_24dp);
            isShown = true;

        }
    }

    public void sharePost(View view) {



    }
}
