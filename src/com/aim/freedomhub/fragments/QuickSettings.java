/*
 * Copyright (C) 2020 AIMROM
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

package com.aim.freedomhub.fragments;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

import com.aim.freedomhub.preferences.SystemSettingEditTextPreference;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String FOOTER_TEXT_STRING = "footer_text_string";

    private SystemSettingEditTextPreference mFooterString;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.freedomhub_qs);
        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mFooterString = (SystemSettingEditTextPreference) findPreference(FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(getContentResolver(),
                FOOTER_TEXT_STRING);
        if (TextUtils.isEmpty(footerString) || footerString == null) {
            mFooterString.setText("#LetsAIMify");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.FOOTER_TEXT_STRING, "#LetsAIMify");
        } else {
            mFooterString.setText(footerString);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFooterString) {
            String value = (String) newValue;
            if (TextUtils.isEmpty(value) || value == null) {
                mFooterString.setText("#LetsAIMify");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.FOOTER_TEXT_STRING, "#LetsAIMify");
            } else {
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.FOOTER_TEXT_STRING, value);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.FREEDOMHUB;
    }
}
