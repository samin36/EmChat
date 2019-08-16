package sa.home.projects.emchat.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import sa.home.projects.emchat.Model.User;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.InputChecker;
import sa.home.projects.emchat.Utils.UiUtils;


public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout username;
    private TextInputLayout email_Id;
    private TextInputLayout password;

    private Button registerButton;

    private Toolbar toolbar;

    private CircleImageView avatar;

    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private Uri avatarUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();

//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");


        username = findViewById(R.id.register_username);
        email_Id = findViewById(R.id.register_email_id);
        password = findViewById(R.id.register_password);

        progressBar = findViewById(R.id.register_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(view -> {
            UiUtils.closeKeyboard(this);
            String usernameStr = InputChecker.parseString(username);
            String emailStr = InputChecker.parseString(email_Id);
            String passwordStr = InputChecker.parseString(password);
            if (!(TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(emailStr) || TextUtils
                    .isEmpty(passwordStr))) {
                progressBar.setVisibility(View.VISIBLE);
                registerUser(usernameStr, emailStr, passwordStr);
            }
        });

        toolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.register_toolbar_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        avatar = findViewById(R.id.register_avatar);
        avatar.setOnClickListener(view -> {
            UiUtils.openImagePicker(this);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                avatarUri = result.getUri();
                Picasso.get().load(avatarUri).into(avatar);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toasty.error(this, result.getError().getMessage(), Toasty.LENGTH_LONG).show();
            }
        }
    }

    private void registerUser(String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            progressBar.setVisibility(View.INVISIBLE);
            if (task.isSuccessful()) {
                Toasty.success(this, getResources().getString(R.string.successful_registration),
                               Toast.LENGTH_LONG).show();
                addImageToStorage(username);
                sendToMainActivity();
            } else {
                Toasty.error(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addImageToStorage(String username) {
        if (avatarUri != null) {
            String currentUid = auth.getCurrentUser().getUid();
            String imageFileName = String.format("images/%s.jpg", currentUid);
            StorageReference storageRef =
                    FirebaseStorage.getInstance().getReference().child(imageFileName);
            storageRef.putFile(avatarUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        addUserToDatabase(username, uri.toString());
                    }
                });
            });
        } else {
            addUserToDatabase(username, "");
        }
    }

    private void addUserToDatabase(String username, String imageLink) {
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUid = currentUser.getUid();
        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        User userToAdd = new User(username, imageLink, "Hey, EmChat is great!");
        databaseReference.setValue(userToAdd).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toasty.success(this, "User added to database successfully", Toast.LENGTH_LONG)
                        .show();
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
