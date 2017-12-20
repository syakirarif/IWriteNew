package id.amoled.iwritenew.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import id.amoled.iwritenew.DetailBlogActivity;
import id.amoled.iwritenew.LoginActivity;
import id.amoled.iwritenew.R;
import id.amoled.iwritenew.tools.Blog;
import id.amoled.iwritenew.tools.Shortcut;

/** Fragment ini adalah untuk Home Fragment. Home Fragment menampilkan daftar list blog yang sudah
 * di posting oleh setiap user
 */

public class HomeFragment extends Fragment {

    private static final String TAG = "MainActivity";

    public RecyclerView mBlogList;

    private DatabaseReference mDbRefBlog;
    private DatabaseReference mDbRefUsers;
    private DatabaseReference mDbRefLikes;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private ProgressBar progressBar;
    private TextView mTvNoArticleInHome;

    public HomeFragment() {
        // Required empty public constructor
        // Constructor ini dibiarkan kosong
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        // menginisialisasi Progress Bar

        progressBar = view.findViewById(R.id.pbHome);
        progressBar.setVisibility(View.VISIBLE);

        // menginisialisasi Autentifikasi Firebase

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    // jika belum login (NULL) maka pergi ke LoginActivity.class

                    Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(loginIntent);
                } else {

                    mTvNoArticleInHome = view.findViewById(R.id.tv_rv_no_home_article);

                    // Referensi database di Firebase yang akan digunakan

                    // Referensi FirebaseDatabase untuk mengambil data-data blog
                    mDbRefBlog = FirebaseDatabase.getInstance().getReference().child("blog");

                    // Referensi FirebaseDatabase untuk mengambil data-data users
                    mDbRefUsers = FirebaseDatabase.getInstance().getReference().child("users");

                    // Referensi FirebaseDatabase untuk mengambil data-data likes
                    mDbRefLikes = FirebaseDatabase.getInstance().getReference().child("likes");

                    //membuat database agar tetap sinkron
                    mDbRefUsers.keepSynced(true);
                    mDbRefBlog.keepSynced(true);
                    mDbRefLikes.keepSynced(true);

                    //menginisialisasi Recycler View
                    mBlogList = view.findViewById(R.id.blog_list);

                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    mLayoutManager.setReverseLayout(true);
                    mLayoutManager.setStackFromEnd(true);

                    mBlogList.setHasFixedSize(true);
                    mBlogList.setLayoutManager(mLayoutManager);

                    //memanggil adapter yang dipakai untuk Recycler View-nya
                    firebaseAdaper();
                }

            }
        };

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthStateListener);

        // memanggil function untuk mengecek apakah user sudah melakukan login atau belum.
        checkUserExist();
    }

    public void firebaseAdaper() {

        // adapter yang digunakan adalah fitur dari library "Firebase UI Database"

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(

                Blog.class,
                R.layout.row_blog,
                BlogViewHolder.class,
                mDbRefBlog
        ) {
            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, Blog model, final int position) {

                final String post_key = getRef(position).getKey();

                // Koding untuk menempatkan setiap data dari JSON Firebase ke masing-masing function yang telah ditentukan.
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getContext(), model.getImage());
                viewHolder.setName(model.getUser_id());
                viewHolder.setImageProfil(getContext(), model.getUser_id());
                viewHolder.setTimePost(model.getDate_post(), model.getTime_post());

                // Koding untuk menampilkan detail artikel yang dipilih.
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent toDetailBlog = new Intent(getActivity(), DetailBlogActivity.class);
                        toDetailBlog.putExtra("blog_id", post_key);
                        startActivity(toDetailBlog);
                    }
                });

                // Koding untuk menampilkan Option Menu di samping kanan atas setiap artikel.
                viewHolder.optionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPopupMenu(viewHolder.optionButton, position);
                    }
                });

                // menghilangkan progressBar jika data dari Firebase sudah selesai dimuat
                progressBar.setVisibility(View.GONE);
                mTvNoArticleInHome.setVisibility(View.GONE);
            }
        };

        firebaseRecyclerAdapter.notifyDataSetChanged();

        // inisialisasi adapter
        mBlogList.setAdapter(firebaseRecyclerAdapter);

        // Koding dibawah berfungsi menampilkan tulisan "Tidak ada data" & cancel ProgressBar jika data dari Firebase nihil.
        mDbRefBlog.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                mTvNoArticleInHome.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static class BlogViewHolder extends RecyclerView.ViewHolder {

        View mView;

        FirebaseAuth mAuth;

        DatabaseReference dbRefUsers;

        ImageButton optionButton;

        public BlogViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mAuth = FirebaseAuth.getInstance();

            optionButton = mView.findViewById(R.id.imgBtnRowOption);

            dbRefUsers = FirebaseDatabase.getInstance().getReference().child("users");
        }

        // Function untuk meng-set setiap judul artikel
        private void setTitle(String title) {
            TextView post_title = mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        // Function untuk meng-set setiap deskripsi artikel
        private void setDesc(String desc) {
            TextView post_desc = mView.findViewById(R.id.post_text);
            post_desc.setText(desc);
        }

        // Function untuk meng-set setiap gambar setiap artikel
        private void setImage(final Context context, final String image) {
            final ImageView post_image = mView.findViewById(R.id.post_image);

            Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {
                    Picasso.with(context).load(image).placeholder(R.drawable.default_image).into(post_image);
                }

                @Override
                public void onError() {
                    Picasso.with(context).load(image).placeholder(R.drawable.default_image).into(post_image);
                }
            });
        }

        // Function untuk meng-set setiap nama pengguna
        private void setName(final String userID) {
            final TextView nama = mView.findViewById(R.id.tvBlogNamaUser);

            Log.d(TAG, "setName: " + userID);

            dbRefUsers.child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String namanya = (String) dataSnapshot.child("name").getValue();
                    nama.setText(namanya);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // Function untuk meng-set setiap foto profil pengguna
        private void setImageProfil(final Context context, final String userID) {
            final ImageView foto = mView.findViewById(R.id.fotoProfil);

            Log.d(TAG, "setImageProfil: " + userID);

            dbRefUsers.child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String fotonya = (String) dataSnapshot.child("image").getValue();
                    Picasso.with(context).load(fotonya).networkPolicy(NetworkPolicy.OFFLINE).into(foto, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.with(context).load(fotonya).placeholder(R.drawable.default_avatar).into(foto);
                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(fotonya).placeholder(R.drawable.default_avatar).into(foto);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // Function untuk meng-set setiap waktu posting setiap artikel
        private void setTimePost(final String date, final String time) {
            final TextView timePost = mView.findViewById(R.id.tvBlogTimePost);

            String waktuPost = date + " - " + time;
            timePost.setText(waktuPost);
        }


    }

    private void showPopupMenu(View view, int position) {
        // inflate popup menu yang dipakai
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_row_blog, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        private int position;

        public MyMenuItemClickListener(int positon) {
            this.position = positon;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            int id = item.getItemId();

            //menentukan action yang akan terjadi jika user memilih popup menu
            if (id == R.id.action_row_favorit) {
                // jika pengguna memilih ini, maka artikel yang dipilih masuk ke Favorit-nya.

                Shortcut.makeToast("belum tersedia", getContext());
            } else if (id == R.id.action_row_profil){
                // jika pengguna memilih ini, maka melihat detail profil penulis artikel yang dipilih.

                Shortcut.makeToast("belum tersedia", getContext());
            }
            return true;
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

                    mDbRefUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(loginIntent);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    // jika User is signed out di arahkan untuk login di LoginActivity.class
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(loginIntent);
                }
            }
        };
    }

}
