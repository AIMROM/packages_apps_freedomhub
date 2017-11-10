/*
 * Copyright (C) 2014-2016 The Dirty Unicorns Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aim.freedomhub.categories;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.Utils;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class statusbar_logos extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private ListPreference mCustomLogos;
	private ColorPickerPreference mStatusBarLogoColor;
    static final int DEFAULT_LOGO_COLOR = 0xff009688;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statusbar_logos);

        mCustomLogos = (ListPreference) findPreference("status_bar_custom_logos");
        mCustomLogos.setOnPreferenceChangeListener(this);
        int customLogos = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_LOGOS,
                0, UserHandle.USER_CURRENT);
        mCustomLogos.setValue(String.valueOf(customLogos));
        mCustomLogos.setSummary(mCustomLogos.getEntry());
		
		 mStatusBarLogoColor = (ColorPickerPreference) findPreference("status_bar_logo_color");
         mStatusBarLogoColor.setOnPreferenceChangeListener(this);
         int intColor = Settings.System.getInt(getContentResolver(),
                 Settings.System.STATUS_BAR_LOGO_COLOR, DEFAULT_LOGO_COLOR);
         String hexColor = String.format("#%08x", (DEFAULT_LOGO_COLOR & intColor));
         mStatusBarLogoColor.setSummary(hexColor);
         mStatusBarLogoColor.setNewPreviewColor(intColor);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.AIM;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        
         if (preference.equals(mCustomLogos)) {
            int customLogos = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_CUSTOM_LOGOS, customLogos, UserHandle.USER_CURRENT);
            int index = mCustomLogos.findIndexOfValue((String) newValue);
            mCustomLogos.setSummary(
                    mCustomLogos.getEntries()[index]);
            return true;
		} else if (preference.equals(mStatusBarLogoColor)) {
             String hex = ColorPickerPreference.convertToARGB(
                     Integer.valueOf(String.valueOf(newValue)));
             preference.setSummary(hex);
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(resolver,
                     Settings.System.STATUS_BAR_LOGO_COLOR, intHex);
             return true;
        }
        return false;
    }
}


