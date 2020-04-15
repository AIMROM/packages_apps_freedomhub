/*
 * Copyright (C) 2020 AIM ROM
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

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

import com.aim.freedomhub.preferences.SystemSettingMasterSwitchPreference;

public class Notifications extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String ALERT_SLIDER_PREF = "alert_slider_notifications";
    private static final String PULSE_AMBIENT_LIGHT = "pulse_ambient_light";

    private Preference mAlertSlider;
    private SystemSettingMasterSwitchPreference mEdgePulse;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.freedomhub_notifications);

        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();

        mAlertSlider = (Preference) prefScreen.findPreference(ALERT_SLIDER_PREF);
        boolean mAlertSliderAvailable = res.getBoolean(
                com.android.internal.R.bool.config_hasAlertSlider);
        if (!mAlertSliderAvailable)
            prefScreen.removePreference(mAlertSlider);

        mEdgePulse = (SystemSettingMasterSwitchPreference) findPreference(PULSE_AMBIENT_LIGHT);
        mEdgePulse.setOnPreferenceChangeListener(this);
        int edgePulse = Settings.System.getInt(getContentResolver(),
                PULSE_AMBIENT_LIGHT, 0);
        mEdgePulse.setChecked(edgePulse != 0);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.FREEDOMHUB;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        } else if (preference == mEdgePulse) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
		            PULSE_AMBIENT_LIGHT, value ? 1 : 0);
            return true;
        }
        return false;
    }
}

