/*
 * Copyright (C) 2019 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aim.freedomhub.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.aim.Utils;
import com.android.settings.SettingsPreferenceFragment;

import com.aim.freedomhub.R;
import com.aim.freedomhub.preferences.CustomSeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import lineageos.providers.LineageSettings;

public class Visualizer extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener  {

    public static final String TAG = "Visualizer";

    private static final String KEY_AUTOCOLOR = "lockscreen_visualizer_autocolor";
    private static final String KEY_LAVALAMP = "lockscreen_lavalamp_enabled";
    private static final String KEY_COLOR = "lockscreen_visualizer_color";

    private static final int DEFAULT_COLOR = 0xffffffff;

    private SwitchPreference mAutoColor;
    private SwitchPreference mLavaLamp;
    private ColorPickerPreference mColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.visualizer);

        ContentResolver resolver = getActivity().getContentResolver();

        boolean mLavaLampEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1,
                UserHandle.USER_CURRENT) != 0;

        mAutoColor = (SwitchPreference) findPreference(KEY_AUTOCOLOR);
        mAutoColor.setEnabled(!mLavaLampEnabled);

        if (mLavaLampEnabled) {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_lavalamp));
        } else {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_summary));
        }

        mLavaLamp = (SwitchPreference) findPreference(KEY_LAVALAMP);
        mLavaLamp.setOnPreferenceChangeListener(this);

        mColor = (ColorPickerPreference) findPreference(KEY_COLOR);
        mColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, DEFAULT_COLOR);
        String hexColor = String.format("#%08x", (DEFAULT_COLOR & intColor));
        mColor.setSummary(hexColor);
        mColor.setNewPreviewColor(intColor);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mLavaLamp) {
            boolean mLavaLampEnabled = (Boolean) newValue;
            mAutoColor.setEnabled(!mLavaLampEnabled);

            if (mLavaLampEnabled) {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_lavalamp));
            } else {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_summary));
            }
            mAutoColor.setEnabled(!mLavaLampEnabled);
            return true;
        } else if (preference == mColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.Secure.putInt(resolver,
                    Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, intHex);
            return true;
        }

        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_SPEED, 10000, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_COUNT, 32, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_FUDGE_FACTOR, 16, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_OPACITY, 140, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.FREEDOMHUB;
    }
}
