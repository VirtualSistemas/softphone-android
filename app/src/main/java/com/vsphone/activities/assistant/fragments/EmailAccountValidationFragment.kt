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
package com.vsphone.activities.assistant.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vsphone.R
import com.vsphone.VSPhoneApplication.Companion.coreContext
import com.vsphone.activities.GenericFragment
import com.vsphone.activities.assistant.AssistantActivity
import com.vsphone.activities.assistant.viewmodels.EmailAccountValidationViewModel
import com.vsphone.activities.assistant.viewmodels.EmailAccountValidationViewModelFactory
import com.vsphone.activities.assistant.viewmodels.SharedAssistantViewModel
import com.vsphone.activities.navigateToAccountLinking
import com.vsphone.databinding.AssistantEmailAccountValidationFragmentBinding

class EmailAccountValidationFragment :
    GenericFragment<AssistantEmailAccountValidationFragmentBinding>() {
    private lateinit var sharedAssistantViewModel: SharedAssistantViewModel
    private lateinit var viewModel: EmailAccountValidationViewModel

    override fun getLayoutId(): Int = R.layout.assistant_email_account_validation_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        sharedAssistantViewModel = requireActivity().run {
            ViewModelProvider(this)[SharedAssistantViewModel::class.java]
        }

        viewModel = ViewModelProvider(
            this,
            EmailAccountValidationViewModelFactory(sharedAssistantViewModel.getAccountCreator())
        )[EmailAccountValidationViewModel::class.java]
        binding.viewModel = viewModel

        viewModel.leaveAssistantEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume {
                coreContext.newAccountConfigured(true)

                val args = Bundle()
                args.putBoolean("AllowSkip", true)
                args.putString("Username", viewModel.accountCreator.username)
                args.putString("Password", viewModel.accountCreator.password)
                navigateToAccountLinking(args)
            }
        }

        viewModel.onErrorEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { message ->
                (requireActivity() as AssistantActivity).showSnackBar(message)
            }
        }
    }
}
