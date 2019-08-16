package sa.home.projects.emchat.Utils;

import android.support.design.widget.TextInputLayout;

public class InputChecker {

    public static String parseString(TextInputLayout toParse) {
        if (toParse.getEditText() != null) {
            return toParse.getEditText().getText().toString();
        } else {
            return null;
        }
    }
}
