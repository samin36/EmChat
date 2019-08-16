package sa.home.projects.emchat.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class UiUtils {

    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void openImagePicker(Activity activity) {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setCropShape(
                CropImageView.CropShape.OVAL).setFixAspectRatio(true).start(activity);
    }

}
