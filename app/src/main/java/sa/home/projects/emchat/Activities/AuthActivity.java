package sa.home.projects.emchat.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import sa.home.projects.emchat.R;

public class AuthActivity extends AppCompatActivity {


    private Button registerButton;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        registerButton = findViewById(R.id.auth_register_button);
        loginButton = findViewById(R.id.auth_login_button);

        registerButton.setOnClickListener(view -> sendToRegisterActivity());
        loginButton.setOnClickListener(view -> sendToLoginActivity());
    }

    private void sendToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void sendToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
