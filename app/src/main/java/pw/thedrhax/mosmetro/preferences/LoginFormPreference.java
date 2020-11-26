package pw.thedrhax.mosmetro.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceManager;

import pw.thedrhax.mosmetro.R;

public class LoginFormPreference extends DialogPreference {
    private SharedPreferences settings;
    private String key_login = getKey() + "_login";
    private String key_password = getKey() + "_password";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LoginFormPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public LoginFormPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public LoginFormPreference(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.preferenceStyle);
        init(context);
        setPositiveButtonText(R.string.positive_button_text);
        setNegativeButtonText(R.string.cancel);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LoginFormPreference(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.loginform_preference;
    }

    private void init(Context context) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getSettings() {
        return settings;
    }

    public String getKey_login() {
        return key_login;
    }

    public String getKey_password() {
        return key_password;
    }
}