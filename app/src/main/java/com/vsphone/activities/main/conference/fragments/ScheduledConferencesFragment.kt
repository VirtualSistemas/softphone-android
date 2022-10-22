/*
 * Copyright (c) 2010-2021 Belledonne Communications SARL.
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
package com.vsphone.activities.main.conference.fragments

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vsphone.R
import com.vsphone.activities.main.MainActivity
import com.vsphone.activities.main.conference.adapters.ScheduledConferencesAdapter
import com.vsphone.activities.main.conference.data.ScheduledConferenceData
import com.vsphone.activities.main.conference.viewmodels.ScheduledConferencesViewModel
import com.vsphone.activities.main.fragments.MasterFragment
import com.vsphone.activities.main.viewmodels.DialogViewModel
import com.vsphone.activities.navigateToConferenceScheduling
import com.vsphone.activities.navigateToConferenceWaitingRoom
import com.vsphone.utils.*
import org.linphone.core.tools.Log
import org.linphone.databinding.ConferencesScheduledFragmentBinding

class ScheduledConferencesFragment :
    MasterFragment<ConferencesScheduledFragmentBinding, ScheduledConferencesAdapter>() {
    override val dialogConfirmationMessageBeforeRemoval =
        R.plurals.conference_scheduled_delete_dialog
    private lateinit var listViewModel: ScheduledConferencesViewModel

    override fun getLayoutId(): Int = R.layout.conferences_scheduled_fragment

    private var deleteConferenceInfoDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        listViewModel = ViewModelProvider(
            this
        )[ScheduledConferencesViewModel::class.java]
        binding.viewModel = listViewModel

        _adapter = ScheduledConferencesAdapter(listSelectionViewModel, viewLifecycleOwner)
        binding.conferenceInfoList.adapter = adapter

        val layoutManager = LinearLayoutManager(requireContext())
        binding.conferenceInfoList.layoutManager = layoutManager

        // Swipe action
        val swipeConfiguration = RecyclerViewSwipeConfiguration()
        val white = ContextCompat.getColor(requireContext(), R.color.white_color)

        swipeConfiguration.rightToLeftAction = RecyclerViewSwipeConfiguration.Action(
            requireContext().getString(R.string.dialog_delete),
            white,
            ContextCompat.getColor(requireContext(), R.color.red_color)
        )
        val swipeListener = object : RecyclerViewSwipeListener {
            override fun onLeftToRightSwipe(viewHolder: RecyclerView.ViewHolder) {}

            override fun onRightToLeftSwipe(viewHolder: RecyclerView.ViewHolder) {
                val index = viewHolder.bindingAdapterPosition
                if (index < 0 || index >= adapter.currentList.size) {
                    Log.e("[Scheduled Conferences] Index is out of bound, can't delete conference info")
                } else {
                    val deletedConfInfo = adapter.currentList[index]
                    showConfInfoDeleteConfirmationDialog(deletedConfInfo, index)
                }
            }
        }
        RecyclerViewSwipeUtils(ItemTouchHelper.LEFT, swipeConfiguration, swipeListener)
            .attachToRecyclerView(binding.conferenceInfoList)

        // Displays date header
        val headerItemDecoration = RecyclerViewHeaderDecoration(requireContext(), adapter)
        binding.conferenceInfoList.addItemDecoration(headerItemDecoration)

        listViewModel.conferences.observe(
            viewLifecycleOwner
        ) {
            adapter.submitList(it)
        }

        adapter.copyAddressToClipboardEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { address ->
                val clipboard =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Conference address", address)
                clipboard.setPrimaryClip(clip)

                (activity as MainActivity).showSnackBar(R.string.conference_schedule_address_copied_to_clipboard)
            }
        }

        adapter.joinConferenceEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { pair ->
                navigateToConferenceWaitingRoom(pair.first, pair.second)
            }
        }

        adapter.editConferenceEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { address ->
                sharedViewModel.addressOfConferenceInfoToEdit.value = Event(address)
                navigateToConferenceScheduling()
            }
        }

        adapter.deleteConferenceInfoEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { data ->
                showConfInfoDeleteConfirmationDialog(data, -1)
            }
        }

        binding.setNewConferenceClickListener {
            navigateToConferenceScheduling()
        }
    }

    override fun deleteItems(indexesOfItemToDelete: ArrayList<Int>) {
        val list = ArrayList<ScheduledConferenceData>()
        for (index in indexesOfItemToDelete) {
            val conferenceData = adapter.currentList[index]
            list.add(conferenceData)
        }
        listViewModel.deleteConferencesInfo(list)
    }

    private fun showConfInfoDeleteConfirmationDialog(data: ScheduledConferenceData, index: Int) {
        val dialogViewModel =
            DialogViewModel(AppUtils.getString(R.string.conference_scheduled_delete_one_dialog))
        deleteConferenceInfoDialog =
            DialogUtils.getVoipDialog(requireContext(), dialogViewModel)

        dialogViewModel.showCancelButton(
            {
                if (index != -1) {
                    adapter.notifyItemChanged(index)
                }
                deleteConferenceInfoDialog?.dismiss()
            },
            getString(R.string.dialog_cancel)
        )

        dialogViewModel.showDeleteButton(
            {
                listViewModel.deleteConferenceInfo(data)
                deleteConferenceInfoDialog?.dismiss()
                (requireActivity() as MainActivity).showSnackBar(R.string.conference_info_removed)
            },
            getString(R.string.dialog_delete)
        )

        deleteConferenceInfoDialog?.show()
    }
}
