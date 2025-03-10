/*
 * Copyright (C) 2022 AOSP-Krypton Project
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

package com.android.settings.security.applock

import android.content.Context
import android.os.SystemProperties
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import com.android.settings.R
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.core.lifecycle.Lifecycle
import com.android.settingslib.search.SearchIndexable
import com.cherish.settings.fragment.CherishDashboardFragment

@SearchIndexable
class AppLockSettingsFragment : CherishDashboardFragment(),
    MenuItem.OnMenuItemClickListener {

    private var debugEnabled = SystemProperties.get(DEBUG_PROPERTY, null) == LEVEL_DEBUG

    override protected fun getPreferenceScreenResId() = R.xml.app_lock_settings

    override protected fun getLogTag() = TAG

    override protected fun createPreferenceControllers(context: Context) =
        buildPreferenceControllers(context, settingsLifecycle)

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menu.add(
            0 /* groupId */,
            MENU_ITEM_DEBUG_ID,
            0 /* order */,
            getDebugMenuItemTitle(),
        ).setOnMenuItemClickListener(this)
    }

    private fun getDebugMenuItemTitle(): Int =
        if (debugEnabled)
            R.string.disable_debugging
        else
            R.string.enable_debugging

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == MENU_ITEM_DEBUG_ID) {
            debugEnabled = !debugEnabled
            SystemProperties.set(DEBUG_PROPERTY,
                if (debugEnabled) LEVEL_DEBUG else null)
            item.setTitle(getDebugMenuItemTitle())
            return true
        }
        return false
    }

    companion object {
        private const val TAG = "AppLockSettingsFragment"

        private const val DEBUG_PROPERTY = "log.tag.AppLockManagerService"
        private const val LEVEL_DEBUG = "DEBUG"
        private const val MENU_ITEM_DEBUG_ID = 101

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER = object : BaseSearchIndexProvider(
            R.xml.app_lock_settings
        ) {
            override fun createPreferenceControllers(context: Context) =
                buildPreferenceControllers(context)
        }

        fun buildPreferenceControllers(
            context: Context,
            lifecycle: Lifecycle? = null
        ): List<AbstractPreferenceController> =
            listOf(
                AppLockNotificationPreferenceController(
                    context,
                    lifecycle,
                )
            )
    }
}
