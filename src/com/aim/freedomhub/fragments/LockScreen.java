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

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.aim.freedomhub.preferences.CustomSeekBarPreference;
import com.aim.freedomhub.fragments.Visualizer;
import com.aim.freedomhub.preferences.SystemSettingListPreference;
import com.aim.freedomhub.preferences.SystemSettingSeekBarPreference;
import com.aim.freedomhub.R;
import lineageos.providers.LineageSettings;
import lineageos.app.LineageContextConstants;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

public class LockScreen extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String LOCKSCREEN_ALBUM_ART_FILTER = "lockscreen_album_art_filter";
    private static final String LOCKSCREEN_MEDIA_BLUR = "lockscreen_media_blur";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";

    private SystemSettingListPreference mArtFilter;
    private SystemSettingSeekBarPreference mBlurSeekbar;
    private PreferenceCategory mFODIconPickerCategory;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.freedomhub_lockscreen);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Context mContext = getContext();

        mArtFilter = (SystemSettingListPreference) findPreference(LOCKSCREEN_ALBUM_ART_FILTER);
        mArtFilter.setOnPreferenceChangeListener(this);
        int artFilter = Settings.System.getInt(resolver,
                LOCKSCREEN_ALBUM_ART_FILTER, 0);

        mBlurSeekbar = (SystemSettingSeekBarPreference) findPreference(LOCKSCREEN_MEDIA_BLUR);
        mBlurSeekbar.setEnabled(artFilter > 2);

        PackageManager packageManager = mContext.getPackageManager();
        boolean hasFod = packageManager.hasSystemFeature(LineageContextConstants.Features.FOD);

        mFODIconPickerCategory = (PreferenceCategory) findPreference(FOD_ICON_PICKER_CATEGORY);
        if (mFODIconPickerCategory != null && !hasFod) {
            prefSet.removePreference(mFODIconPickerCategory);
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mArtFilter) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALBUM_ART_FILTER, value);
            mBlurSeekbar.setEnabled(value > 2);
            return true;
        }
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        LineageSettings.Secure.putIntForUser(resolver,
                LineageSettings.Secure.LOCKSCREEN_MEDIA_METADATA, 1, UserHandle.USER_CURRENT);
        Visualizer.reset(mContext);
    }
}
