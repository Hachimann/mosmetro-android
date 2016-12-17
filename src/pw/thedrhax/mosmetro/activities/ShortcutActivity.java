package pw.thedrhax.mosmetro.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import pw.thedrhax.mosmetro.R;

public class ShortcutActivity extends Activity {
    private String SSID = "";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shortcut_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Show back button in menu
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException ignored) {}

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void network_selected (View view) {
        switch (view.getId()) {
            case R.id.radio_auto:
                SSID = ""; break;

            case R.id.radio_mosmetro:
                SSID = "MosMetro_Free"; break;

            case R.id.radio_mosgortrans:
                SSID = "MosGorTrans_Free"; break;
        }
    }

    public void button_save (View view) {
        Intent result = new Intent();

        Intent shortcut_intent = new Intent(this, DebugActivity.class);
        shortcut_intent.putExtra("SSID", SSID);
        shortcut_intent.putExtra("background", ((CheckBox)findViewById(R.id.check_background)).isChecked());

        result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut_intent);
        result.putExtra(Intent.EXTRA_SHORTCUT_NAME, SSID.isEmpty() ?
                getString(R.string.connect) : SSID
        );
        result.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher)
        );

        if ("android.intent.action.CREATE_SHORTCUT".equals(getIntent().getAction())) {
            setResult(RESULT_OK, result);
        } else {
            result.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            sendBroadcast(result);
        }

        finish();
    }

    public void button_cancel (View view) {
        finish();
    }
}
