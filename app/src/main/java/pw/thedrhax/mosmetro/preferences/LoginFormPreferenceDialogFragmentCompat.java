package pw.thedrhax.mosmetro.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import pw.thedrhax.mosmetro.R;

public class LoginFormPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private SharedPreferences settings;
    private EditText text_login;
    private EditText text_password;
    private String key_login;
    private String key_password;

    public static LoginFormPreferenceDialogFragmentCompat newInstance(String key) {
        final LoginFormPreferenceDialogFragmentCompat
                fragment = new LoginFormPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        DialogPreference preference = getPreference();
        if (preference instanceof LoginFormPreference) {
            settings = ((LoginFormPreference) preference).getSettings();
            key_login = ((LoginFormPreference) preference).getKey_login();
            key_password = ((LoginFormPreference) preference).getKey_password();
        }

        text_login = (EditText) view.findViewById(R.id.text_login);
        text_login.setText(settings.getString(key_login, ""));

        text_password = (EditText) view.findViewById(R.id.text_password);
        text_password.setText(settings.getString(key_password, ""));
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            settings.edit()
                    .putString(key_login, text_login.getText().toString())
                    .putString(key_password, text_password.getText().toString())
                    .apply();
        }
    }
}