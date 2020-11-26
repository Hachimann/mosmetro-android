/**
 * Wi-Fi в метро (pw.thedrhax.mosmetro, Moscow Wi-Fi autologin)
 * Copyright © 2015 Dmitry Karikh <the.dr.hax@gmail.com>
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pw.thedrhax.mosmetro.preferences;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

import com.edmodo.rangebar.RangeBar;

import pw.thedrhax.mosmetro.R;
import pw.thedrhax.util.Util;

public class RangeBarPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    public static RangeBarPreferenceDialogFragmentCompat newInstance(String key) {
        final RangeBarPreferenceDialogFragmentCompat
                fragment = new RangeBarPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    private String key_min;
    private String key_max;

    private int defaultMin, defaultMax, min, max;

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(requireContext());
        final View view = View.inflate(getContext(), R.layout.rangebar_preference, null);
        final RangeBar rangebar = (RangeBar) view.findViewById(R.id.rangebar);

        DialogPreference preference = getPreference();
        if (preference instanceof RangeBarPreference) {
            key_min = ((RangeBarPreference) preference).getKey_min();
            key_max = ((RangeBarPreference) preference).getKey_max();
            defaultMin = ((RangeBarPreference) preference).getDefaultMin();
            defaultMax = ((RangeBarPreference) preference).getDefaultMax();
            min = ((RangeBarPreference) preference).getMin();
            max = ((RangeBarPreference) preference).getMax();
        }

        rangebar.setTickCount(max - min + 1);
        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            private TextView rangetext = (TextView) view.findViewById(R.id.rangetext);

            @SuppressLint("SetTextI18n")
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int left, int right) {
                if (left < min || left > max) {
                    rangeBar.setLeft(min);
                    return;
                }

                if (right < min || right > max) {
                    rangeBar.setRight(max);
                    return;
                }

                rangetext.setText("" + (left + min) + " - " + (right + min));
            }
        });

        int current_min = Util.getIntPreference(getContext(), key_min, defaultMin);
        if (current_min < min)
            current_min = min;

        int current_max = Util.getIntPreference(getContext(), key_max, defaultMax);
        if (current_max > max)
            current_max = max;

        rangebar.setThumbIndices(current_min, current_max);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                settings.edit()
                        .putInt(key_min, rangebar.getLeftIndex() + min)
                        .putInt(key_max, rangebar.getRightIndex() + min)
                        .apply();
            }
        });

        builder.setView(view);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
