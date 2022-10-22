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
package com.vsphone.activities.main.contact.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vsphone.R
import com.vsphone.activities.main.adapters.SelectionListAdapter
import com.vsphone.activities.main.contact.viewmodels.ContactViewModel
import com.vsphone.activities.main.viewmodels.ListTopBarViewModel
import com.vsphone.databinding.ContactListCellBinding
import com.vsphone.databinding.GenericListHeaderBinding
import com.vsphone.utils.AppUtils
import com.vsphone.utils.Event
import com.vsphone.utils.HeaderAdapter
import org.linphone.core.Friend

class ContactsListAdapter(
    selectionVM: ListTopBarViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : SelectionListAdapter<ContactViewModel, RecyclerView.ViewHolder>(
    selectionVM,
    ContactDiffCallback()
),
    HeaderAdapter {
    val selectedContactEvent: MutableLiveData<Event<Friend>> by lazy {
        MutableLiveData<Event<Friend>>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ContactListCellBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.contact_list_cell, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(getItem(position))
    }

    inner class ViewHolder(
        val binding: ContactListCellBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contactViewModel: ContactViewModel) {
            with(binding) {
                viewModel = contactViewModel

                lifecycleOwner = viewLifecycleOwner

                // This is for item selection through ListTopBarFragment
                selectionListViewModel = selectionViewModel
                selectionViewModel.isEditionEnabled.observe(
                    viewLifecycleOwner
                ) {
                    position = bindingAdapterPosition
                }

                setClickListener {
                    if (selectionViewModel.isEditionEnabled.value == true) {
                        selectionViewModel.onToggleSelect(bindingAdapterPosition)
                    } else {
                        val friend = contactViewModel.contact.value
                        // TODO FIXME !!!
                        if (friend != null) selectedContactEvent.value = Event(friend)
                    }
                }

                setLongClickListener {
                    if (selectionViewModel.isEditionEnabled.value == false) {
                        selectionViewModel.isEditionEnabled.value = true
                        // Selection will be handled by click listener
                        true
                    }
                    false
                }

                executePendingBindings()
            }
        }
    }

    override fun displayHeaderForPosition(position: Int): Boolean {
        if (position >= itemCount) return false
        val contact = getItem(position)
        val firstLetter = contact.fullName.firstOrNull().toString()
        val previousPosition = position - 1
        return if (previousPosition >= 0) {
            val previousItemFirstLetter =
                getItem(previousPosition).fullName.firstOrNull().toString()
            !firstLetter.equals(previousItemFirstLetter, ignoreCase = true)
        } else true
    }

    override fun getHeaderViewForPosition(context: Context, position: Int): View {
        val contact = getItem(position)
        val firstLetter = AppUtils.getInitials(contact.fullName, 1)
        val binding: GenericListHeaderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.generic_list_header, null, false
        )
        binding.title = firstLetter
        binding.executePendingBindings()
        return binding.root
    }
}

private class ContactDiffCallback : DiffUtil.ItemCallback<ContactViewModel>() {
    override fun areItemsTheSame(
        oldItem: ContactViewModel,
        newItem: ContactViewModel
    ): Boolean {
        return oldItem.fullName.compareTo(newItem.fullName) == 0
    }

    override fun areContentsTheSame(
        oldItem: ContactViewModel,
        newItem: ContactViewModel
    ): Boolean {
        return true
    }
}
