package id.amoled.iwritenew;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import id.amoled.iwritenew.tabs.MyAdapter;
import id.amoled.iwritenew.tabs.SlidingTabLayout;
import id.amoled.iwritenew.tools.SessionManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fab;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDbRefUser;

    private FirebaseDatabase mFirebaseDatabase;

    // menginisialisasi Progress Dialog
    public ProgressDialog mProgress;

    private SessionManager sessionManager;
    SessionManager session;

    private TextView mNama, mEmail;

    private String UserID, nama, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(getApplicationContext());
        sessionManager.checkLogin();

        mAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mDbRefUser = mFirebaseDatabase.getReference().child("users");

        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {

                    startActivity(new Intent(MainActivity.this, LoginActivity.class));

                }else{
                    /*HashMap<String, String> userSession = sessionManager.getUserDetails();

                    mNama = (TextView) findViewById(R.id.tvNavHeaderNama);
                    mEmail = (TextView) findViewById(R.id.tvNavHeaderEmail);

                    String userNama = userSession.get(SessionManager.KEY_NAME);
                    String userEmail = userSession.get(SessionManager.KEY_EMAIL);

                    mNama.setText(userNama);
                    mEmail.setText(userEmail);*/
                }

            }
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.vp_tabs);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager(), this));

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.stl_tabs);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.colorAccent));
        slidingTabLayout.setCustomTabView(R.layout.tab_view, R.id.tv_tab);
        slidingTabLayout.setViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {

                    startActivity(new Intent(MainActivity.this, LoginActivity.class));

                }else{
                    mDbRefUser.child(mAuth.getCurrentUser().getUid()).child("lastActive").onDisconnect().setValue(ServerValue.TIMESTAMP);
                }

            }
        };
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            mauKeluar();
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_profil) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_karyaku) {

        } else if (id == R.id.nav_pencarian) {

        } else if (id == R.id.nav_bookmark) {

        } else if (id == R.id.nav_kontak) {
            kirimSaran();
        } else if (id == R.id.nav_about) {
            tampilkanAbout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void tampilkanAbout(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Tentang Aplikasi");
        builder.setMessage("Powered by AMOLED Academy, TI UNIDA Gontor.");
        builder.setCancelable(true);
        builder.setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
        builder.setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void kirimSaran() {

        Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("amoled.academy@gmail.com") +
                "?subject=" + Uri.encode("[iWrite] Keluhan/Saran") +
                "&body=" + Uri.encode("Keluhan/Saran saya adalah: ");
        Uri uri = Uri.parse(uriText);

        send.setData(uri);
        startActivity(Intent.createChooser(send, "Kirim via email"));
    }

    public void mauKeluar() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Anda yakin mau keluar?");
        builder.setCancelable(true);
        builder.setNegativeButton("TIDAK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("YA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                onDestroy();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
