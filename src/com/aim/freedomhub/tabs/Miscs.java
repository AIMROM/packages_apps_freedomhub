/*Copyright (C) 2017 AIM ROM
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
          http://www.apache.org/licenses/LICENSE2.0
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
*/
package com.aim.freedomhub.tabs;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.os.Bundle;
import com.android.settings.R;

import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.Utils;

import android.telephony.TelephonyManager;
public class Miscs extends SettingsPreferenceFragment {

private static final String AIM_INCALL = "aim_incall";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.miscs);
        PreferenceScreen prefScreen = getPreferenceScreen();
        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(AIM_INCALL);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.AMIFY;
    }
}
