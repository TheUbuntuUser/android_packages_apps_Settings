/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.display;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.view.RotationPolicy;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

import com.cherish.settings.preferences.SystemSettingSwitchPreference;

public class AutoRotatePreferenceController extends TogglePreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener, LifecycleObserver,
        OnResume, OnPause {

    private final String FLOATING_BUTTON_PREF_KEY = "enable_floating_rotation_button";

    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private Preference mPreference;
    private SystemSettingSwitchPreference mFloatingPref;
    private RotationPolicy.RotationPolicyListener mRotationPolicyListener;

    public AutoRotatePreferenceController(Context context, String key) {
        super(context, key);
        mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mFloatingPref = screen.findPreference(FLOATING_BUTTON_PREF_KEY);
    }

    @Override
    public void updateState(Preference preference) {
        mPreference = preference;
        if (mFloatingPref != null) mFloatingPref.setEnabled(!isChecked());
        super.updateState(preference);
    }

    @Override
    public void onResume() {
        if (mRotationPolicyListener == null) {
            mRotationPolicyListener = new RotationPolicy.RotationPolicyListener() {
                @Override
                public void onChange() {
                    if (mPreference != null) {
                        updateState(mPreference);
                    }
                }
            };
        }
        RotationPolicy.registerRotationPolicyListener(mContext,
                mRotationPolicyListener);
    }

    @Override
    public void onPause() {
        if (mRotationPolicyListener != null) {
            RotationPolicy.unregisterRotationPolicyListener(mContext, mRotationPolicyListener);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return RotationPolicy.isRotationLockToggleVisible(mContext)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "auto_rotate");
    }

    @Override
    public boolean isPublicSlice() {
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }

    @Override
    public boolean isChecked() {
        return !RotationPolicy.isRotationLocked(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        final boolean isLocked = !isChecked;
        mMetricsFeatureProvider.action(mContext, SettingsEnums.ACTION_ROTATION_LOCK,
                isLocked);
        RotationPolicy.setRotationLock(mContext, isLocked);
        if (mFloatingPref != null) mFloatingPref.setEnabled(!isChecked);
        return true;
    }
}
