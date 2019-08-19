package sa.home.projects.emchat.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import sa.home.projects.emchat.Model.User;
import sa.home.projects.emchat.R;
import sa.home.projects.emchat.Utils.UiUtils;

public class AccountSettings extends AppCompatActivity {


    private Toolbar toolbar;

    private CircleImageView avatar;
    private Button changeAvatar;
    private Button changeStatus;

    private TextView name;
    private TextView status;

    private Uri avatarUri;

    private Bitmap avatarThumbImage;

    private FirebaseAuth auth;

    private ValueEventListener listener;

    private DatabaseReference reference;

    private AlertDialog progressDialog;
    private AlertDialog statusChangeDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        auth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.account_settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.account_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = UiUtils.createProgressDialog(this);

        avatar = findViewById(R.id.account_settings_image);
        changeAvatar = findViewById(R.id.account_change_image);
        changeStatus = findViewById(R.id.account_change_status);

        name = findViewById(R.id.account_settings_name);
        status = findViewById(R.id.account_settings_status);
        status.setSelected(true);
        updateUiWithUserData();

        changeAvatar.setOnClickListener(view -> onClick(view));
        avatar.setOnClickListener(view -> onClick(view));
        changeStatus.setOnClickListener(view -> onClick(view));
    }


    private void updateUiWithUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUid = currentUser.getUid();
        String pathOfUser = String.format("Users/%s", currentUid);
        reference = FirebaseDatabase.getInstance().getReference(pathOfUser);

        listener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (!TextUtils.isEmpty(user.getImage())) {
                        avatarUri = Uri.parse(user.getImage());
                        if (URLUtil.isValidUrl(avatarUri.toString())) {
                            Picasso.get().load(avatarUri).placeholder(R.drawable.default_profile_pic).into(avatar);
                        }
                    }
//                    Toasty.info(getApplicationContext(), "fetching data of " + user.toString(),
//                                Toasty.LENGTH_SHORT).show();
                    name.setText(user.getName());
                    status.setText(user.getStatus());
                    if (statusChangeDialog == null) {
                        statusChangeDialog = createStatusChangeDialog();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(getApplicationContext(), "DB error: " + databaseError.toString(),
                             Toasty.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressDialog.show();
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                avatarUri = result.getUri();
                avatarThumbImage = UiUtils.createAvatarThumbImage(this, avatarUri);
                Picasso.get().load(avatarUri).placeholder(R.drawable.default_profile_pic).into(avatar);
                updateUserImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                progressDialog.dismiss();
                Toasty.error(this, result.getError().getMessage(), Toasty.LENGTH_LONG).show();
            }
        }
        progressDialog.dismiss();
    }


    public void onClick(View view) {
        if (view == avatar || view == changeAvatar) {
            UiUtils.openImagePicker(this);
        } else if (view == changeStatus) {
            statusChangeDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reference != null) {
            reference.removeEventListener(listener);
        }

    }


    /**
     * Updates the user image file in Firebase Storage
     */
    public void updateUserImage() {
        String currentUid = auth.getCurrentUser().getUid();
        String imageFileName = String.format("avatar_images/%s.jpg", currentUid);
        StorageReference imageStorageRef =
                FirebaseStorage.getInstance().getReference().child(imageFileName);


        String thumbImageFileName = String.format("avatar_images/thumb_images/%s.jpg", currentUid);
        StorageReference thumbStorageRef = FirebaseStorage.getInstance().getReference().child(thumbImageFileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        avatarThumbImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumbImageData = baos.toByteArray();


        imageStorageRef.putFile(avatarUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {

                    imageStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri avatarImageUri) {

                            //Now uploading the thumbnail image
                            UploadTask thumbUploadTask = thumbStorageRef.putBytes(thumbImageData);
                            thumbUploadTask
                                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {

                                                thumbStorageRef.getDownloadUrl()
                                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri thumbImageUri) {
                                                                updateImageLink(avatarImageUri, thumbImageUri);
                                                            }
                                                        });

                                            } else {
                                                Toasty.error(AccountSettings.this,
                                                             "Error updating " + "thumbnail " + "image",
                                                             Toasty.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    });
                } else {
                    Toasty.error(AccountSettings.this, "Error updating images to storage", Toasty.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    /**
     * Updates the image link in Firebase database
     * @param avatarImageUri the new avatar uri
     * @param thumbImageUri the new thumbnail avatar uri
     */
    private void updateImageLink(Uri avatarImageUri, Uri thumbImageUri) {
        String currentUid = auth.getCurrentUser().getUid();
        String userPath = String.format("Users/%s", currentUid);
        DatabaseReference databaseRef =
                FirebaseDatabase.getInstance().getReference().child(userPath);

        Map<String, Object> imageLinkUpdate = new HashMap<>();
        imageLinkUpdate.put("image", avatarImageUri.toString());
        imageLinkUpdate.put("thumbImage", thumbImageUri.toString());

        databaseRef.updateChildren(imageLinkUpdate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toasty.success(getApplicationContext(), "Image link updated",
                                           Toasty.LENGTH_LONG).show();
                        } else {
                            Toasty.error(getApplicationContext(), "Error updating image link",
                                         Toasty.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void updateUserStatus(String newStatus) {
        String currentUid = auth.getCurrentUser().getUid();
        String userPath = String.format("Users/%s", currentUid);
        DatabaseReference databaseRef =
                FirebaseDatabase.getInstance().getReference().child(userPath);

        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("status", newStatus);

        databaseRef.updateChildren(statusUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
//                    updateUiWithUserData();
                    status.setText(newStatus);
                    progressDialog.dismiss();
                } else {
                    Toasty.error(getApplicationContext(), "Error updating status",
                                 Toasty.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }

            }
        });
    }

    public AlertDialog createStatusChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.status_change_dialog, null);
        EditText newStatus = dialogView.findViewById(R.id.status_change_edit_text);
        Button confirmChange = dialogView.findViewById(R.id.status_change_confirm);
        Button cancelChange = dialogView.findViewById(R.id.status_change_cancel);

        newStatus.setText(status.getText());
        newStatus.setSelection(newStatus.getText().length());

        confirmChange.setOnClickListener(view -> {
                progressDialog.show();
                statusChangeDialog.dismiss();
                if (!TextUtils.equals(newStatus.getText(), status.getText())) {
                    updateUserStatus(newStatus.getText().toString());
                } else {
                    progressDialog.dismiss();
                }

        });

        cancelChange.setOnClickListener(view -> {
            statusChangeDialog.dismiss();
        });


        builder.setView(dialogView).setCancelable(false);

        return builder.create();
    }

}
