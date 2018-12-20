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

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.aim.Utils;
import com.aim.freedomhub.preferences.SystemSettingMasterSwitchPreference;

import com.aim.freedomhub.fragments.ImeSettings;
import com.aim.freedomhub.fragments.GamingMode;

public class Misc extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SHOW_CPU_INFO_KEY = "show_cpu_info";
    private static final String FLASHLIGHT_ON_CALL = "flashlight_on_call";
    private static final String PREF_STOCK_RECENTS_CATEGORY = "stock_recents_category";
    private static final String PREF_ALTERNATIVE_RECENTS_CATEGORY = "alternative_recents_category";
    private static final String PREF_SWIPE_UP_ENABLED = "swipe_up_enabled_warning";

    private SwitchPreference mShowCpuInfo;
    private ListPreference mFlashlightOnCall;
    private PreferenceCategory mStockRecentsCategory;
    private PreferenceCategory mAlternativeRecentsCategory;
    private Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.freedomhub_misc);
        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Context mContext = getActivity().getApplicationContext();

        mShowCpuInfo = (SwitchPreference) prefScreen.findPreference(SHOW_CPU_INFO_KEY);
        mShowCpuInfo.setChecked(Settings.Global.getInt(resolver,
                Settings.Global.SHOW_CPU_OVERLAY, 0) == 1);
        mShowCpuInfo.setOnPreferenceChangeListener(this);

        mFlashlightOnCall = (ListPreference) findPreference(FLASHLIGHT_ON_CALL);
        if (!Utils.deviceSupportsFlashLight(mContext))
            prefScreen.removePreference(mFlashlightOnCall);

        mStockRecentsCategory = (PreferenceCategory) findPreference(PREF_STOCK_RECENTS_CATEGORY);
        mAlternativeRecentsCategory =
                (PreferenceCategory) findPreference(PREF_ALTERNATIVE_RECENTS_CATEGORY);

        // Alternative recents en-/disabling
        Preference.OnPreferenceChangeListener alternativeRecentsChangeListener =
                new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateDependencies(preference, (Boolean) newValue);
                return true;
            }
        };
        for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
            Preference preference = mAlternativeRecentsCategory.getPreference(i);
            if (preference instanceof SystemSettingMasterSwitchPreference) {
                preference.setOnPreferenceChangeListener(alternativeRecentsChangeListener);
            }
        }

        updateDependencies();

        // Warning for alternative recents when gesture navigation is enabled,
        // which directly controls quickstep (launcher) recents.
        final int navigationMode = getActivity().getResources()
                .getInteger(com.android.internal.R.integer.config_navBarInteractionMode);
        // config_navBarInteractionMode:
        // 0: 3 button mode (supports slim recents)
        // 1: 2 button mode (currently does not support alternative recents)
        // 2: gesture only (currently does not support alternative recents)
        if (navigationMode != 0) {
            for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
                Preference preference = mAlternativeRecentsCategory.getPreference(i);
                if (PREF_SWIPE_UP_ENABLED.equals(preference.getKey())) {
                    // We want to have that one enabled
                    continue;
                }
                preference.setEnabled(false);
            }
        } else {
            mAlternativeRecentsCategory.removePreference(findPreference(PREF_SWIPE_UP_ENABLED));
        }
    }

    private void updateDependencies() {
        updateDependencies(null, null);
    }

    private void updateDependencies(Preference updatedPreference, Boolean newValue) {
        // Disable stock recents category if alternative enabled
        boolean alternativeRecentsEnabled = newValue != null && newValue;
        if (!alternativeRecentsEnabled) {
            for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
                Preference preference = mAlternativeRecentsCategory.getPreference(i);
                if (preference == updatedPreference) {
                    // Already used newValue
                    continue;
                }
                if (preference instanceof SystemSettingMasterSwitchPreference
                        && ((SystemSettingMasterSwitchPreference) preference).isChecked()) {
                    alternativeRecentsEnabled = true;
                    break;
                }
            }
        }
        if (mStockRecentsCategory != null) {
            mStockRecentsCategory.setEnabled(!alternativeRecentsEnabled);
        }
    }

    private static void writeCpuInfoOptions(Context mContext, boolean value) {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.SHOW_CPU_OVERLAY, value ? 1 : 0);
        Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.CPUInfoService");
        if (value) {
            mContext.startService(service);
        } else {
            mContext.stopService(service);
        }
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
    public void onPause() {
        super.onPause();
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.FLASHLIGHT_ON_CALL, 0, UserHandle.USER_CURRENT);
        writeCpuInfoOptions(mContext, false);
        ImeSettings.reset(mContext);
        GamingMode.reset(mContext);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        Context mContext = getActivity().getApplicationContext();
        if (preference == mShowCpuInfo) {
            writeCpuInfoOptions(mContext, (Boolean) newValue);
            return true;
        }
        return false;
    }
}

