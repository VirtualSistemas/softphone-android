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
package com.vsphone.activities.main.history.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vsphone.R
import com.vsphone.activities.GenericFragment
import com.vsphone.activities.main.MainActivity
import com.vsphone.activities.main.history.viewmodels.CallLogViewModel
import com.vsphone.databinding.HistoryConfDetailFragmentBinding
import org.linphone.core.tools.Log

class DetailConferenceCallLogFragment : GenericFragment<HistoryConfDetailFragmentBinding>() {
    private lateinit var viewModel: CallLogViewModel

    override fun getLayoutId(): Int = R.layout.history_conf_detail_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.sharedMainViewModel = sharedViewModel

        val callLogGroup = sharedViewModel.selectedCallLogGroup.value
        if (callLogGroup == null) {
            Log.e("[History] Call log group is null, aborting!")
            findNavController().navigateUp()
            return
        }

        viewModel = callLogGroup.lastCallLogViewModel
        binding.viewModel = viewModel
        viewModel.addRelatedCallLogs(callLogGroup.callLogs)

        useMaterialSharedAxisXForwardAnimation =
            sharedViewModel.isSlidingPaneSlideable.value == false

        viewModel.onMessageToNotifyEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { messageResourceId ->
                (activity as MainActivity).showSnackBar(messageResourceId)
            }
        }
    }
}
