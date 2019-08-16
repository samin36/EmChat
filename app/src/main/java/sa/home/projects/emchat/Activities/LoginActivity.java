package sa.home.projects.emchat.Activities;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.InputChecker;
import sa.home.projects.emchat.Utils.UiUtils;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailId;
    private TextInputLayout password;

    private Button loginButton;

    private ProgressBar progressBar;

    private TextView forgotPassword;

    private Toolbar toolbar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = findViewById(R.id.login_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        emailId = findViewById(R.id.login_email_id);
        password = findViewById(R.id.login_password);

        forgotPassword = findViewById(R.id.login_reset_password);

        loginButton = findViewById(R.id.login_button);

        toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.login_welcome);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(view -> {
            UiUtils.closeKeyboard(this);
            String emailIdStr = InputChecker.parseString(emailId);
            String passwordStr = InputChecker.parseString(password);

            if (!(TextUtils.isEmpty(emailIdStr) || TextUtils.isEmpty(passwordStr))) {
                progressBar.setVisibility(View.VISIBLE);
                loginUser(emailIdStr, passwordStr);
            }
        });

        forgotPassword.setOnClickListener(view -> {
            Toasty.info(this, "Password Forgotten:<(", Toast.LENGTH_LONG).show();
        });
    }

    private void loginUser(String emailId, String password) {
        auth.signInWithEmailAndPassword(emailId, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.INVISIBLE);
            if (task.isSuccessful()) {
                Toasty.success(this, getResources().getString(R.string.successful_login),
                               Toast.LENGTH_LONG).show();
                sendToMainActivity();
            } else {
                Toasty.error(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
