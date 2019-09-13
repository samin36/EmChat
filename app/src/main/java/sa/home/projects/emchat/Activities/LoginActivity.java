package sa.home.projects.emchat.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import es.dmoral.toasty.Toasty;
import me.aflak.libraries.dialog.FingerprintDialog;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.Consts;
import sa.home.projects.emchat.Utils.InputChecker;
import sa.home.projects.emchat.Utils.UiUtils;

public class LoginActivity extends AppCompatActivity{

    private TextInputLayout emailId;
    private TextInputLayout password;

    private Button loginButton;

    private AlertDialog progressDialog;
    private AlertDialog passwordResetDialog;

    private TextView forgotPassword;

    private Toolbar toolbar;

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        progressDialog = UiUtils.createProgressDialog(this);

        emailId = findViewById(R.id.login_email_id);
        password = findViewById(R.id.login_password);

        forgotPassword = findViewById(R.id.login_reset_password);

        loginButton = findViewById(R.id.login_button);
        UiUtils.addButtonEffect(loginButton);

        toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.login_welcome);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        loginButton.setOnClickListener(view -> {
            UiUtils.closeKeyboard(this);
            String emailIdStr = InputChecker.parseString(emailId);
            String passwordStr = InputChecker.parseString(password);

            if (!(TextUtils.isEmpty(emailIdStr) || TextUtils.isEmpty(passwordStr))) {
                progressDialog.show();
                loginUser(emailIdStr, passwordStr);
            }
        });

        forgotPassword.setOnClickListener(view -> {
            if (passwordResetDialog == null) {
                passwordResetDialog = createForgotPasswordDialog();
            }
            passwordResetDialog.show();
        });
    }



    private void loginUser(String emailId, String password) {
        auth.signInWithEmailAndPassword(emailId, password).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toasty.success(this, getResources().getString(R.string.successful_login),
                               Toast.LENGTH_LONG).show();

                String currentUid = auth.getCurrentUser().getUid();
                String path = String.format("Users/%s/%s", currentUid, Consts.DB_USERS_TOKEN_ID);

                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String deviceToken = task.getResult().getToken();
                            databaseRef.child(path).setValue(deviceToken).addOnCompleteListener(task1 -> {
                                sendToMainActivity();
                            });
                        } else {
                            Toasty.success(LoginActivity.this, task.getException().getMessage(),
                                           Toast.LENGTH_LONG).show();

                            sendToMainActivity();
                        }
                    }
                });
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

    public AlertDialog createForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.forgot_password_dialog, null);
        EditText email = dialogView.findViewById(R.id.forgot_password_email);
        Button resetPassword = dialogView.findViewById(R.id.forgot_password_reset);
        Button cancelChange = dialogView.findViewById(R.id.forgot_password_cancel);
        UiUtils.addButtonEffect(resetPassword);
        UiUtils.addButtonEffect(cancelChange);

        resetPassword.setOnClickListener(view -> {
            String emailId = email.getText().toString();
            UiUtils.closeKeyboard(LoginActivity.this);
            if (!TextUtils.isEmpty(emailId)) {
                progressDialog.show();
                passwordResetDialog.dismiss();
                auth.sendPasswordResetEmail(emailId).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toasty.success(getApplicationContext(),
                                       "Password reset instructions have been sent to email",
                                       Toasty.LENGTH_LONG).show();
                    } else {
                        Toasty.error(getApplicationContext(), task.getException().getMessage(),
                                     Toasty.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                });
            }
        });

        cancelChange.setOnClickListener(view -> {
            passwordResetDialog.dismiss();
        });

        builder.setView(dialogView).setCancelable(false);
        return  builder.create();
    }

}
