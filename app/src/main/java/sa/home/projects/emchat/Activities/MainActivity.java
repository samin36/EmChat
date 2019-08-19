package sa.home.projects.emchat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import sa.home.projects.emchat.Adapters.PageAdapter;
import sa.home.projects.emchat.R;

public class MainActivity extends AppCompatActivity {


    private TabLayout mainTabLayout;
    private PageAdapter mainPageAdapter;
    private ViewPager mainViewPager;

    private FirebaseAuth auth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

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
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
//            Toasty.warning(this, getResources().getString(R.string.no_loggedin_user),
//                           Toast.LENGTH_SHORT).show();
            sendToAuthActivity();
        }
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
