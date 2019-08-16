package sa.home.projects.emchat.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;
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

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        auth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.account_settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.account_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        avatar = findViewById(R.id.account_settings_image);
        changeAvatar = findViewById(R.id.account_change_image);
        changeStatus = findViewById(R.id.account_change_status);

        name = findViewById(R.id.account_settings_name);
        status = findViewById(R.id.account_settings_status);
        status.setSelected(true);
        updateUserData();
    }



    private void updateUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUid = currentUser.getUid();
        String pathOfUser = String.format("Users/%s", currentUid);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(pathOfUser);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (!TextUtils.isEmpty(user.getImage())) {
                        avatarUri = Uri.parse(user.getImage());
                        if (URLUtil.isValidUrl(avatarUri.toString())) {
                            Picasso.get().load(avatarUri).into(avatar);
                        }
                    }
                    Toasty.info(getApplicationContext(), "fetching data of " + user.toString(),
                                Toasty.LENGTH_SHORT).show();
                    name.setText(user.getName());
                    status.setText(user.getStatus());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(getApplicationContext(), databaseError.toString(), Toasty.LENGTH_LONG).show();
            }
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


    public void onClick(View view) {
        if (view == avatar || view == changeAvatar) {
            UiUtils.openImagePicker(this);
        }
    }

}
