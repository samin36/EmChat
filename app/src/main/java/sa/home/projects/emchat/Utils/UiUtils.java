package sa.home.projects.emchat.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;
import sa.home.projects.emchat.R;

public class UiUtils {

    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void openImagePicker(Activity activity) {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true).start(activity);
    }

    public static AlertDialog createProgressDialog(Activity activity) {
        SpotsDialog.Builder builder =
                new SpotsDialog.Builder().setContext(activity).setTheme(R.style.progress_dialog_style)
                        .setMessage(R.string.progress_message).setCancelable(false);
        return builder.build();
    }

    public static Bitmap createAvatarThumbImage(Activity activity, Uri avatarUri) {
        Bitmap avatarThumbImage;

        File avatarThumbFile = new File(avatarUri.getPath());
        try {
            int size = (int) activity.getResources().getDimension(R.dimen.avatar_image_small);
            avatarThumbImage = new Compressor(activity).setMaxHeight(size).setMaxWidth(size).setQuality(75).
                    compressToBitmap(avatarThumbFile);
        } catch (IOException e) {
            Toasty.error(activity, "Error creating thumbnail: " + e.getMessage(), Toasty.LENGTH_LONG).show();
            avatarThumbImage = null;
        }
        return avatarThumbImage;
    }

    public static void addButtonEffect(View button) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }

    public static void requestPermissions(Activity activity, String[] permissions, String denyMessage) {
        boolean result = false;
        PermissionListener listenPermission = new PermissionListener() {

            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toasty.warning(activity, "Permission Denied", Toasty.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(activity)
                .setPermissionListener(listenPermission)
                .setDeniedMessage(denyMessage)
                .setPermissions(permissions)
                .check();
    }

    public static boolean arePermissionsGranted(Activity activity, String ... permissions) {
        return TedPermission.isGranted(activity, permissions);
    }

    public static void clearNotificationDatabase(Context context) {
        DatabaseReference notificationDatabase = FirebaseDatabase.getInstance().getReference();

    }

}
