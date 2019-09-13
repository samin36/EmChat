package sa.home.projects.emchat.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import sa.home.projects.emchat.Model.User;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.Consts;
import sa.home.projects.emchat.Utils.InputChecker;
import sa.home.projects.emchat.Utils.UiUtils;


public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout username;
    private TextInputLayout email_Id;
    private TextInputLayout password;

    private Button registerButton;

    private Toolbar toolbar;

    private CircleImageView avatar;

    private AlertDialog progressDialog;
    private AlertDialog saveFingerprintDialog;

    private FirebaseAuth auth;

    private Uri avatarUri;

    private Bitmap avatarThumbImage;

    private Boolean saveFingerPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();

        saveFingerPrint = false;

        username = findViewById(R.id.register_username);
        email_Id = findViewById(R.id.register_email_id);
        password = findViewById(R.id.register_password);

        progressDialog = UiUtils.createProgressDialog(this);
        if (saveFingerprintDialog == null) {
            saveFingerprintDialog = createSaveFingerprintDialog();
        }

        registerButton = findViewById(R.id.register_button);
        UiUtils.addButtonEffect(registerButton);
        registerButton.setOnClickListener(view -> {
            UiUtils.closeKeyboard(this);
            String usernameStr = InputChecker.parseString(username);
            String emailStr = InputChecker.parseString(email_Id);
            String passwordStr = InputChecker.parseString(password);

            askForPermissions();

            if (!(TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(emailStr) || TextUtils
                    .isEmpty(passwordStr))) {
                progressDialog.show();
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

    private void askForPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!UiUtils.arePermissionsGranted(this, permissions)) {
            String denyMessage =
                    "These permissions must be granted for the application to function correctly";
            UiUtils.requestPermissions(this, permissions, denyMessage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                avatarUri = result.getUri();
                avatarThumbImage = UiUtils.createAvatarThumbImage(this, avatarUri);
                Picasso.get().load(avatarUri).placeholder(R.drawable.default_profile_pic)
                        .into(avatar);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toasty.error(this, result.getError().getMessage(), Toasty.LENGTH_LONG).show();
            }
        }
    }

    private void registerUser(String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toasty.success(this, getResources().getString(R.string.successful_registration),
                               Toast.LENGTH_LONG).show();
                saveFingerprintDialog.show();

                //No longer needed here. This is taken care of in the createSaveFingerPrintDialog
//                addImagesToStorage(username);
//                progressDialog.dismiss();
//                sendToMainActivity();
            } else {
                Toasty.error(this, "Error authenticating user: " + task.getException().getMessage(),
                             Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void addImagesToStorage(String username) {
        if (avatarUri != null) {
            String currentUid = auth.getCurrentUser().getUid();


            String imageFileName = String.format("avatar_images/%s.jpg", currentUid);
            StorageReference imageStorageRef =
                    FirebaseStorage.getInstance().getReference().child(imageFileName);


            String thumbImageFileName =
                    String.format("avatar_images/thumb_images/%s.jpg", currentUid);
            StorageReference thumbStorageRef =
                    FirebaseStorage.getInstance().getReference().child(thumbImageFileName);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            avatarThumbImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumbImageData = baos.toByteArray();


            imageStorageRef.putFile(avatarUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                imageStorageRef.getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri avatarImageUri) {

                                                //Now uploading the thumbnail image
                                                UploadTask thumbUploadTask =
                                                        thumbStorageRef.putBytes(thumbImageData);
                                                thumbUploadTask.addOnCompleteListener(
                                                        new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onComplete(
                                                                    @NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                if (task.isSuccessful()) {

                                                                    thumbStorageRef.getDownloadUrl()
                                                                            .addOnSuccessListener(
                                                                                    new OnSuccessListener<Uri>() {
                                                                                        @Override
                                                                                        public void onSuccess(
                                                                                                Uri thumbImageUri) {
                                                                                            addUserToDatabase(
                                                                                                    username,
                                                                                                    avatarImageUri
                                                                                                            .toString(),
                                                                                                    thumbImageUri
                                                                                                            .toString());
                                                                                        }
                                                                                    });

                                                                } else {
                                                                    Toasty.error(
                                                                            RegisterActivity.this,
                                                                            "Error uploading "
                                                                            + "thumbnail "
                                                                            + "image",
                                                                            Toasty.LENGTH_LONG)
                                                                            .show();
                                                                    addUserToDatabase(username,
                                                                                      avatarImageUri
                                                                                              .toString(),
                                                                                      "");
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            } else {
                                Toasty.error(RegisterActivity.this,
                                             "Error uploading images to storage",
                                             Toasty.LENGTH_LONG).show();
                                addUserToDatabase(username, "", "");
                            }
                        }
                    });
        } else {
            addUserToDatabase(username, "", "");
        }
    }

    private void addUserToDatabase(String username, String avatarImageLink, String thumbImageLink) {
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUid = currentUser.getUid();
        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                String tokenId = "";
                if (task.isSuccessful()) {
                    tokenId = task.getResult().getToken();
                }

                User userToAdd = new User(username, avatarImageLink, thumbImageLink,
                                     getResources().getString(R.string.default_status),
                                     saveFingerPrint, tokenId);

                databaseReference.setValue(userToAdd).addOnCompleteListener(taskSave -> {
                    if (taskSave.isSuccessful()) {
                        Toasty.success(RegisterActivity.this, "User added to database successfully", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        Toasty.error(RegisterActivity.this,
                                     "Error adding user to database: " + taskSave.getException().getMessage(),
                                     Toasty.LENGTH_LONG).show();
                    }
                });
            }
        });

    }


    private AlertDialog createSaveFingerprintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.fingerprint_dialog_style);
        builder.setCancelable(false).setTitle(getResources().getString(R.string.fingerprint_title))
                .setMessage(getResources().getString(R.string.fingerprint_choice))
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveFingerPrint = false;
                        saveFingerprintDialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveFingerPrint = true;
                        saveFingerprintDialog.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        addImagesToStorage(InputChecker.parseString(username));
                        progressDialog.dismiss();
                        sendToMainActivity();
                    }
                });
        return builder.create();
    }


    private void sendToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Consts.REGISTER_TO_MAIN, true);
        startActivity(intent);
        finish();
    }
}
