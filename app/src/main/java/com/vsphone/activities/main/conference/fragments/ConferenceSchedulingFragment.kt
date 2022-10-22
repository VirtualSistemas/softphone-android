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

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.View
import androidx.navigation.navGraphViewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.vsphone.R
import com.vsphone.VSPhoneApplication.Companion.coreContext
import com.vsphone.activities.GenericFragment
import com.vsphone.activities.main.conference.viewmodels.ConferenceSchedulingViewModel
import com.vsphone.activities.navigateToParticipantsList
import org.linphone.core.Factory
import org.linphone.core.tools.Log
import org.linphone.databinding.ConferenceSchedulingFragmentBinding

class ConferenceSchedulingFragment : GenericFragment<ConferenceSchedulingFragmentBinding>() {
    private val viewModel: ConferenceSchedulingViewModel by navGraphViewModels(R.id.conference_scheduling_nav_graph)

    override fun getLayoutId(): Int = R.layout.conference_scheduling_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        sharedViewModel.participantsListForNextScheduledMeeting.observe(
            viewLifecycleOwner
        ) {
            it.consume { participants ->
                Log.i("[Conference Scheduling] Found participants (${participants.size}) to pre-populate for meeting schedule")
                viewModel.prePopulateParticipantsList(participants, true)
            }
        }

        sharedViewModel.addressOfConferenceInfoToEdit.observe(
            viewLifecycleOwner
        ) {
            it.consume { address ->
                val conferenceAddress = Factory.instance().createAddress(address)
                if (conferenceAddress != null) {
                    Log.i("[Conference Scheduling] Trying to edit conference info using address: $address")
                    val conferenceInfo =
                        coreContext.core.findConferenceInformationFromUri(conferenceAddress)
                    if (conferenceInfo != null) {
                        viewModel.populateFromConferenceInfo(conferenceInfo)
                    } else {
                        Log.e("[Conference Scheduling] Failed to find ConferenceInfo matching address: $address")
                    }
                } else {
                    Log.e("[Conference Scheduling] Failed to parse conference address: $address")
                }
            }
        }

        binding.setNextClickListener {
            navigateToParticipantsList()
        }

        binding.setDatePickerClickListener {
            val constraintsBuilder =
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
            val picker =
                MaterialDatePicker.Builder.datePicker()
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setTitleText(R.string.conference_schedule_date)
                    .setSelection(viewModel.dateTimestamp)
                    .build()
            picker.addOnPositiveButtonClickListener {
                val selection = picker.selection
                if (selection != null) {
                    viewModel.setDate(selection)
                }
            }
            picker.show(requireFragmentManager(), "Date picker")
        }

        binding.setTimePickerClickListener {
            val isSystem24Hour = is24HourFormat(requireContext())
            val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            val picker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(clockFormat)
                    .setTitleText(R.string.conference_schedule_time)
                    .setHour(viewModel.hour)
                    .setMinute(viewModel.minutes)
                    .build()
            picker.addOnPositiveButtonClickListener {
                viewModel.setTime(picker.hour, picker.minute)
            }
            picker.show(requireFragmentManager(), "Time picker")
        }
    }
}
