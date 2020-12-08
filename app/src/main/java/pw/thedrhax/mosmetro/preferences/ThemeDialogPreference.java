package pw.thedrhax.mosmetro.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import pw.thedrhax.mosmetro.R;
import pw.thedrhax.mosmetro.activities.SettingsActivity;

public class ThemeDialogPreference extends DialogPreference {
    public static final int THEME_DEFAULT = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    public static final int THEME_OLED = 3;
    private SharedPreferences settings;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ThemeDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
        this.context = context;
    }

    public ThemeDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        this.context = context;
    }

    public ThemeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ThemeDialogPreference(Context context) {
        super(context);
        init(context);
        this.context = context;
    }

    private void init(Context context) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    };

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        final String[] list = context.getResources().getStringArray(R.array.theme_options);

        int selected = settings.getInt("pref_theme", 0);
        if (selected < 0 || selected > list.length) {
            selected = 0;
        }
        final int[] selection = {selected};

        builder.setTitle(R.string.pref_theme);
        builder.setSingleChoiceItems(list, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selection[0] = i;
            }
        });
        int finalSelected = selected;
        builder.setPositiveButton(context.getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (selection[0] == finalSelected) {
                    dialogInterface.dismiss();
                    return;
                }
                settings.edit().putInt("pref_theme", selection[0]).apply();
                dialogInterface.dismiss();
                ((SettingsActivity)context).recreate();
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }
}
