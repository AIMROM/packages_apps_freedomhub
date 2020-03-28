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

package com.aim.freedomhub.fragments.themes;

import static android.os.UserHandle.USER_SYSTEM;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;

import com.android.internal.util.aim.ThemesUtils;
import com.android.internal.util.aim.Utils;
import com.android.settings.SettingsPreferenceFragment;


public class ThemePreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_THEME_SWITCH = "theme_switch";

    private IOverlayManager mOverlayService;
    private UiModeManager mUiModeManager = mContext.getSystemService(UiModeManager.class);

    private ListPreference mThemeSwitch;

    public ThemePreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return PREF_THEME_SWITCH;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
        mThemeSwitch = (ListPreference) screen.findPreference(PREF_THEME_SWITCH);
        mThemeSwitch.setOnPreferenceChangeListener(this);
        if (Utils.isThemeEnabled("com.android.theme.darkgrey.system")) {
            mThemeSwitch.setValue("7");
        } else if (Utils.isThemeEnabled("com.android.theme.pitchblack.system")) {
            mThemeSwitch.setValue("6");
        } else if (Utils.isThemeEnabled("com.android.theme.chocox.system")) {
            mThemeSwitch.setValue("5");
        } else if (Utils.isThemeEnabled("com.android.theme.bakedgreen.system")) {
            mThemeSwitch.setValue("4");
        } else if (Utils.isThemeEnabled("com.android.theme.solarizeddark.system")) {
            mThemeSwitch.setValue("3");
        } else if (mUiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
            mThemeSwitch.setValue("2");
        } else {
            mThemeSwitch.setValue("1");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mThemeSwitch) {
            String theme_switch = (String) objValue;
            switch (theme_switch) {
                case "1":
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_NO, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_NO, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_NO, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_NO, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_NO, ThemesUtils.DARK_GREY);
                    break;
                case "2":
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    break;
                case "3":
                    handleBackgrounds(true, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    break;
                case "4":
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(true, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    break;
                case "5":
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(true, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    break;
                case "6":
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(true, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    break;
                case "7":
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(true, mContext, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    break;
            }
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
        }
        return true;
    }

    private void handleBackgrounds(Boolean state, Context context, int mode, String[] overlays) {
        if (context != null) {
            Objects.requireNonNull(context.getSystemService(UiModeManager.class))
                    .setNightMode(mode);
        }
        for (int i = 0; i < overlays.length; i++) {
            String background = overlays[i];
            try {
                mOverlayService.setEnabled(background, state, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
