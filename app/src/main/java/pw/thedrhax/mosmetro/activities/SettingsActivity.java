package pw.thedrhax.mosmetro.activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;

import org.acra.ACRA;

import java.util.Map;

import pw.thedrhax.mosmetro.R;
import pw.thedrhax.mosmetro.preferences.LoginFormPreference;
import pw.thedrhax.mosmetro.preferences.LoginFormPreferenceDialogFragmentCompat;
import pw.thedrhax.mosmetro.preferences.RangeBarPreference;
import pw.thedrhax.mosmetro.preferences.RangeBarPreferenceDialogFragmentCompat;
import pw.thedrhax.mosmetro.services.ConnectionService;
import pw.thedrhax.mosmetro.updater.UpdateCheckTask;
import pw.thedrhax.util.Listener;
import pw.thedrhax.util.Logger;
import pw.thedrhax.util.PermissionUtils;
import pw.thedrhax.util.Randomizer;
import pw.thedrhax.util.Util;
import pw.thedrhax.util.Version;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";
    static boolean isThemeChanged;
    private SettingsFragment2 fragment;
    private static Listener<Map<String,UpdateCheckTask.Branch>> branches;
    private static SharedPreferences settings;
    private static SettingsActivity mInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Util.getSavedTheme(this));
        setContentView(R.layout.settings_activity);

        settings = android.preference.PreferenceManager.getDefaultSharedPreferences(this);

        mInstance = this;

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = new SettingsFragment2();

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.settings, fragment)
                    .commit();
            fragmentManager.executePendingTransactions();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (fragmentManager.getBackStackEntryCount() == 0) {
                    setTitle(R.string.app_name);

                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setDisplayHomeAsUpEnabled(false);
                    }
                }
            }
        });
        if (isThemeChanged) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        if (Build.VERSION.SDK_INT >= 23)
            energy_saving_setup();
        if (Build.VERSION.SDK_INT >= 28)
            location_permission_setup();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
        setTheme(Util.getSavedTheme(this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        if(pref.getTitle().equals(getResources().getString(R.string.more_information))) {
            setTitle(R.string.about);
        }
        else {
            setTitle(pref.getTitle());
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_donate:
                donate_dialog();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static void replaceFragment(String id, Fragment fragment) {
        try {
            mInstance.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings, fragment)
                    .addToBackStack(id)
                    .commit();
            ActionBar actionBar = mInstance.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (IllegalStateException ex) { // https://stackoverflow.com/q/7575921
            ACRA.getErrorReporter().handleException(ex);
        }
    }

    private void donate_dialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip;

                switch (i) {
                    case 0: // Yandex.Money
                        startActivity(new Intent(SettingsActivity.this, SafeViewActivity.class)
                                .putExtra("data", getString(R.string.donate_yandex_data))
                        );
                        break;

                    case 1: // Bitcoin
                        clip = ClipData.newPlainText("", getString(R.string.donate_bitcoin_data));
                        clipboard.setPrimaryClip(clip);

                        Toast.makeText(SettingsActivity.this,
                                R.string.clipboard_copy,
                                Toast.LENGTH_SHORT
                        ).show();
                        break;

                    case 2: // Ethereum
                        clip = ClipData.newPlainText("", getString(R.string.donate_ethereum_data));
                        clipboard.setPrimaryClip(clip);

                        Toast.makeText(SettingsActivity.this,
                                R.string.clipboard_copy,
                                Toast.LENGTH_SHORT
                        ).show();
                        break;

                    case 3: // Communities
                        setTitle(R.string.about);
                        replaceFragment("about", new AboutFragment());
                        break;
                }
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.action_donate)
                .setItems(R.array.donate_options, listener)
                .show();
    }

    @RequiresApi(23)
    private void energy_saving_setup() {
        final PermissionUtils pu = new PermissionUtils(this);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_battery_saving)
                .setMessage(R.string.dialog_battery_saving_summary)
                .setPositiveButton(R.string.permission_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pu.requestBatterySavingIgnore();
                    }
                })
                .setNeutralButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pu.openBatterySavingSettings();
                    }
                })
                .setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings.edit()
                                .putBoolean("pref_battery_saving_ignore", true)
                                .apply();
                        dialog.dismiss();
                    }
                });

        if (!settings.getBoolean("pref_battery_saving_ignore", false))
            if (!pu.isBatterySavingIgnored())
                dialog.show();
    }

    @RequiresApi(28)
    private void location_permission_setup() {
        final PermissionUtils pu = new PermissionUtils(this);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.location_permission)
                .setMessage(R.string.location_permission_saving)
                .setPositiveButton(R.string.permission_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pu.requestCoarseLocation();
                    }
                })
                .setNeutralButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pu.openAppSettings();
                    }
                })
                .setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings.edit()
                                .putBoolean("pref_location_ignore", true)
                                .apply();
                        dialog.dismiss();
                    }
                });

        if (!settings.getBoolean("pref_location_ignore", false))
            if (!pu.isCoarseLocationGranted())
                dialog.show();
    }

    public static class SettingsFragment2 extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            PreferenceScreen screen = getPreferenceScreen();

            // Hide shortcut button on Android 8+ (issue #211)
            if (Build.VERSION.SDK_INT >= 26) {
                Preference pref_shortcut = screen.findPreference("pref_shortcut");
                assert pref_shortcut != null;
                screen.removePreference(pref_shortcut);
            }

            // Add version name and code
            Preference app_name = screen.findPreference("app_name");
            assert app_name != null;
            app_name.setSummary(getString(R.string.version, Version.getFormattedVersion()));

            // Start/stop service on pref_autoconnect change
            final CheckBoxPreference pref_autoconnect =
                    (CheckBoxPreference) screen.findPreference("pref_autoconnect");
            assert pref_autoconnect != null;
            pref_autoconnect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Context context = ((SettingsActivity) requireActivity());
                    Intent service = new Intent(context, ConnectionService.class);
                    if (pref_autoconnect.isChecked())
                        service.setAction("STOP");
                    context.startService(service);
                    return true;
                }
            });

            // Branch Selector
            Preference pref_updater_branch = screen.findPreference("pref_updater_branch");
            assert pref_updater_branch != null;
            pref_updater_branch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Map<String,UpdateCheckTask.Branch> branch_list = branches.get();
                    if (branch_list != null) {
                        mInstance.setTitle(R.string.pref_updater_branch);
                        replaceFragment("branch", new BranchFragment().branches(branch_list));
                    } else {
                        preference.setEnabled(false);
                    }
                    return true;
                }
            });

            branches = new Listener<Map<String,UpdateCheckTask.Branch>>(null) {
                @Override
                public void onChange(Map<String, UpdateCheckTask.Branch> new_value) {
                    pref_updater_branch.setEnabled(new_value != null && new_value.size() > 0);
                }
            };

            update_checker_setup();
        }

        private void update_checker_setup() {
            // Force check
            PreferenceScreen screen = getPreferenceScreen();
            final Preference pref_updater_check = screen.findPreference("pref_updater_check");
            assert pref_updater_check != null;
            pref_updater_check.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new UpdateCheckTask((SettingsActivity)requireActivity()) {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            pref_updater_check.setEnabled(false);
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            pref_updater_check.setEnabled(true);
                        }

                        @Override
                        public void result(@Nullable Map<String, Branch> result) {
                            branches.set(result);
                        }
                    }.ignore(preference == null).force(preference != null).execute();
                    return false;
                }
            });

            // Check for updates on start if enabled
            if (settings.getBoolean("pref_updater_enabled", true))
                pref_updater_check
                        .getOnPreferenceClickListener()
                        .onPreferenceClick(null);
        }
    }

    public static class ConnectionSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setHasOptionsMenu(true);
            setPreferencesFromResource(R.xml.pref_conn, rootKey);

            // Generate random User-Agent if it is unset
            new Randomizer(getActivity()).cached_useragent();

            PreferenceScreen screen = getPreferenceScreen();

            final CheckBoxPreference pref_mainet = (CheckBoxPreference)
                    screen.findPreference("pref_mainet");
            final LoginFormPreference pref_mainet_creds = (LoginFormPreference)
                    screen.findPreference("pref_mainet_credentials");
            assert pref_mainet_creds != null;
            assert pref_mainet != null;
            pref_mainet_creds.setEnabled(pref_mainet.isChecked());
            pref_mainet.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    pref_mainet_creds.setEnabled((Boolean) newValue);
                    return true;
                }
            });
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {

            DialogFragment dialogFragment = null;
            if (preference instanceof RangeBarPreference) {
                dialogFragment = RangeBarPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            }

            if (preference instanceof LoginFormPreference) {
                dialogFragment = LoginFormPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            }

            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getParentFragmentManager(), "androidx.preference" +
                        ".PreferenceFragment.DIALOG");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        @Override
        public void onPrepareOptionsMenu(@NonNull Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.clear();
        }
    }

    public static class NotificationSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setHasOptionsMenu(true);
            setPreferencesFromResource(R.xml.pref_notify, rootKey);

            PreferenceScreen screen = getPreferenceScreen();

            // Link pref_notify_foreground and pref_notify_success_lock
            final CheckBoxPreference foreground = (CheckBoxPreference)
                    screen.findPreference("pref_notify_foreground");
            final CheckBoxPreference success = (CheckBoxPreference)
                    screen.findPreference("pref_notify_success");
            final CheckBoxPreference success_lock = (CheckBoxPreference)
                    screen.findPreference("pref_notify_success_lock");
            assert foreground != null;
            foreground.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    assert success != null;
                    success.setEnabled(!((Boolean) newValue));
                    assert success_lock != null;
                    success_lock.setEnabled(!((Boolean) newValue));
                    return true;
                }
            });
            foreground
                    .getOnPreferenceChangeListener()
                    .onPreferenceChange(foreground, foreground.isChecked());
        }

        @Override
        public void onPrepareOptionsMenu(@NonNull Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.clear();
        }
    }

    public static class DebugSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setHasOptionsMenu(true);
            setPreferencesFromResource(R.xml.pref_debug, rootKey);

            Preference.OnPreferenceChangeListener reload_logger = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    PreferenceManager.getDefaultSharedPreferences(requireActivity())
                            .edit()
                            .putBoolean(preference.getKey(), (Boolean) newValue)
                            .apply();
                    Logger.configure(getActivity());
                    return true;
                }
            };

            CheckBoxPreference pref_debug_logcat =
                    (CheckBoxPreference) getPreferenceScreen().findPreference("pref_debug_logcat");
            assert pref_debug_logcat != null;
            pref_debug_logcat.setOnPreferenceChangeListener(reload_logger);
        }

        @Override
        public void onPrepareOptionsMenu(@NonNull Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.clear();
        }
    }

    public static class ThemesSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_themes, rootKey);

            PreferenceScreen screen = getPreferenceScreen();

            final CheckBoxPreference darkTheme = (CheckBoxPreference)
                    screen.findPreference("pref_dark_theme");
            final CheckBoxPreference AMOLEDTheme = (CheckBoxPreference)
                    screen.findPreference("pref_AMOLED_theme");

            Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    Intent intent = ((SettingsActivity) requireActivity()).getIntent();
//                    ((SettingsActivity) requireActivity()).finish();
//                    startActivity(intent);
                    isThemeChanged = true;
                    ((SettingsActivity) requireActivity()).recreate();
                    return true;
                }
            };

            assert darkTheme != null;
            darkTheme.setOnPreferenceChangeListener(listener);
            assert AMOLEDTheme != null;
            AMOLEDTheme.setOnPreferenceChangeListener(listener);
        }
    }

    public static class BranchFragment extends PreferenceFragmentCompat {

        private Map<String,UpdateCheckTask.Branch> branches;

        public BranchFragment branches(@NonNull Map<String, UpdateCheckTask.Branch> branches) {
            this.branches = branches; return this;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setHasOptionsMenu(true);

            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(requireActivity());
            setPreferenceScreen(screen);

            PreferenceCategory stable = new PreferenceCategory(requireActivity());
            stable.setTitle(R.string.pref_updater_branch_stable);
            screen.addPreference(stable);

            PreferenceCategory experimental = new PreferenceCategory(requireActivity());
            experimental.setTitle(R.string.pref_updater_branch_experimental);
            screen.addPreference(experimental);

            if (branches == null) return;
            final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            for (final UpdateCheckTask.Branch branch : branches.values()) {
                CheckBoxPreference pref = new CheckBoxPreference(requireActivity()) {
                    @Override
                    public void onBindViewHolder(PreferenceViewHolder holder) {
                        super.onBindViewHolder(holder);

                        // Increase number of lines on Android 4.x
                        // Source: https://stackoverflow.com/a/2615650
                        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
                        summary.setMaxLines(15);
                    }
                };
                pref.setTitle(branch.name);
                pref.setSummary(branch.description);
                pref.setChecked(Version.getBranch().equals(branch.name));
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        boolean same = Version.getBranch().equals(branch.name);
                        ((CheckBoxPreference)preference).setChecked(same);
                        if (!same) {
                            settings.edit().putInt("pref_updater_ignore", 0).apply();
                            branch.dialog().show();
                        }
                        requireActivity().onBackPressed();
                        return true;
                    }
                });

                if (branch.stable) {
                    stable.addPreference(pref);
                } else {
                    experimental.addPreference(pref);
                }
            }
        }

        @Override
        public void onPrepareOptionsMenu(@NonNull Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.clear();
        }
    }

    public static class AboutFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.about, rootKey);
            setHasOptionsMenu(true);
        }

        @Override
        public void onPrepareOptionsMenu(@NonNull Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.clear();
        }
    }
}