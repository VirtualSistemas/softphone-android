/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of vsphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vsphone.activities.main.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vsphone.R
import com.vsphone.activities.main.fragments.SecureFragment
import com.vsphone.databinding.AboutFragmentBinding
import org.linphone.core.tools.Log

class AboutFragment : SecureFragment<AboutFragmentBinding>() {
    private lateinit var viewModel: AboutViewModel

    override fun getLayoutId(): Int = R.layout.about_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this)[AboutViewModel::class.java]
        binding.viewModel = viewModel

        binding.setPrivacyPolicyClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.about_privacy_policy_link))
            )
            try {
                startActivity(browserIntent)
            } catch (se: SecurityException) {
                Log.e("[About] Failed to start browser intent, $se")
            }
        }

        binding.setLicenseClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.about_license_link))
            )
            try {
                startActivity(browserIntent)
            } catch (se: SecurityException) {
                Log.e("[About] Failed to start browser intent, $se")
            }
        }

        binding.setWeblateClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.about_weblate_link))
            )
            try {
                startActivity(browserIntent)
            } catch (se: SecurityException) {
                Log.e("[About] Failed to start browser intent, $se")
            }
        }
    }
}
