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
package com.vsphone.activities.main.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.vsphone.R
import com.vsphone.VSPhoneApplication.Companion.corePreferences
import com.vsphone.activities.*
import com.vsphone.activities.main.viewmodels.TabsViewModel
import com.vsphone.databinding.TabsFragmentBinding
import com.vsphone.utils.Event

class TabsFragment :
    GenericFragment<TabsFragmentBinding>(),
    NavController.OnDestinationChangedListener {
    private lateinit var viewModel: TabsViewModel

    override fun getLayoutId(): Int = R.layout.tabs_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        useMaterialSharedAxisXForwardAnimation = false

        viewModel = requireActivity().run {
            ViewModelProvider(this)[TabsViewModel::class.java]
        }
        binding.viewModel = viewModel

        binding.setHistoryClickListener {
            when (findNavController().currentDestination?.id) {
                R.id.masterContactsFragment ->
                    sharedViewModel.updateContactsAnimationsBasedOnDestination.value =
                        Event(R.id.masterCallLogsFragment)
                R.id.dialerFragment ->
                    sharedViewModel.updateDialerAnimationsBasedOnDestination.value =
                        Event(R.id.masterCallLogsFragment)
            }
            navigateToCallHistory()
        }

        binding.setContactsClickListener {
            when (findNavController().currentDestination?.id) {
                R.id.dialerFragment ->
                    sharedViewModel.updateDialerAnimationsBasedOnDestination.value =
                        Event(R.id.masterContactsFragment)
            }
            sharedViewModel.updateContactsAnimationsBasedOnDestination.value =
                Event(findNavController().currentDestination?.id ?: -1)
            navigateToContacts()
        }

        binding.setDialerClickListener {
            when (findNavController().currentDestination?.id) {
                R.id.masterContactsFragment ->
                    sharedViewModel.updateContactsAnimationsBasedOnDestination.value =
                        Event(R.id.dialerFragment)
            }
            sharedViewModel.updateDialerAnimationsBasedOnDestination.value =
                Event(findNavController().currentDestination?.id ?: -1)
            navigateToDialer()
        }

        binding.setChatClickListener {
            when (findNavController().currentDestination?.id) {
                R.id.masterContactsFragment ->
                    sharedViewModel.updateContactsAnimationsBasedOnDestination.value =
                        Event(R.id.masterChatRoomsFragment)
                R.id.dialerFragment ->
                    sharedViewModel.updateDialerAnimationsBasedOnDestination.value =
                        Event(R.id.masterChatRoomsFragment)
            }
            navigateToChatRooms()
        }
    }

    override fun onStart() {
        super.onStart()
        findNavController().addOnDestinationChangedListener(this)
    }

    override fun onStop() {
        findNavController().removeOnDestinationChangedListener(this)
        super.onStop()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (corePreferences.enableAnimations) {
            when (destination.id) {
                R.id.masterCallLogsFragment -> binding.motionLayout.transitionToState(R.id.call_history)
                R.id.masterContactsFragment -> binding.motionLayout.transitionToState(R.id.contacts)
                R.id.dialerFragment -> binding.motionLayout.transitionToState(R.id.dialer)
                R.id.masterChatRoomsFragment -> binding.motionLayout.transitionToState(R.id.chat_rooms)
            }
        } else {
            when (destination.id) {
                R.id.masterCallLogsFragment -> binding.motionLayout.setTransition(
                    R.id.call_history,
                    R.id.call_history
                )
                R.id.masterContactsFragment -> binding.motionLayout.setTransition(
                    R.id.contacts,
                    R.id.contacts
                )
                R.id.dialerFragment -> binding.motionLayout.setTransition(R.id.dialer, R.id.dialer)
                R.id.masterChatRoomsFragment -> binding.motionLayout.setTransition(
                    R.id.chat_rooms,
                    R.id.chat_rooms
                )
            }
        }
    }
}
