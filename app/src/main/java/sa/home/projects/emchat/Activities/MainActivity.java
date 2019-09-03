package sa.home.projects.emchat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;
import me.aflak.libraries.callback.FingerprintDialogCallback;
import me.aflak.libraries.dialog.FingerprintDialog;
import sa.home.projects.emchat.Adapters.PageAdapter;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.Consts;

public class MainActivity extends AppCompatActivity implements FingerprintDialogCallback {


    private TabLayout mainTabLayout;
    private PageAdapter mainPageAdapter;
    private ViewPager mainViewPager;

    private FirebaseAuth auth;
    private Toolbar toolbar;

    private boolean authenticated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        authenticated = false;


        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        mainTabLayout = findViewById(R.id.main_tab_layout);
        mainViewPager = findViewById(R.id.main_view_pager);

        mainPageAdapter = new PageAdapter(getSupportFragmentManager());
        mainViewPager.setAdapter(mainPageAdapter);
        mainViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(mainTabLayout));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(Consts.AUTHENTICATED, authenticated);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            authenticated = savedInstanceState.getBoolean(Consts.AUTHENTICATED);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_menu_logout:
                logoutCurrentUser();
                sendToAuthActivity();
                break;
            case R.id.main_menu_search:
                break;
            case R.id.main_menu_account_settings:
                sendToAccountSettingsActivity();
                break;
            case R.id.main_menu_all_users:
                sendToAllUsersActivity();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean fromRegister = getIntent().getBooleanExtra(Consts.REGISTER_TO_MAIN, false);
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toasty.warning(this, getResources().getString(R.string.no_loggedin_user),
                           Toasty.LENGTH_SHORT).show();
            sendToAuthActivity();
        } else if (!fromRegister && !authenticated) {
            String path = String.format("Users/%s", currentUser.getUid());
            DatabaseReference fingerprintRef = FirebaseDatabase.getInstance().getReference(path).child("saveFingerprint");
            fingerprintRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean saveFingerprint = dataSnapshot.getValue(Boolean.class);
                    if (saveFingerprint) {
                        initFingerprint();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    auth.signOut();
                    sendToAuthActivity();
                }
            });
        }
    }

    private void initFingerprint() {
        if (FingerprintDialog.isAvailable(this)) {
            FingerprintDialog.initialize(this)
                    .title("Authenticating...")
                    .message("Please place your finger on the sensor")
                    .callback(this)
                    .show();
        }
    }

    @Override
    public void onAuthenticationSucceeded() {
        Toasty.success(this, "Welcome!", Toasty.LENGTH_LONG).show();
        authenticated = true;
    }

    @Override
    public void onAuthenticationCancel() {
        auth.signOut();
        sendToAuthActivity();
    }

    private void sendToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendToAccountSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, AccountSettings.class);
        startActivity(intent);
    }

    private void sendToAllUsersActivity() {
        Intent intent = new Intent(MainActivity.this, AllUsers.class);
        startActivity(intent);
    }

    private void logoutCurrentUser() {
        auth.signOut();
    }
}
