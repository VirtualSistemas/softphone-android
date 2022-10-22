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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.vsphone.R
import com.vsphone.activities.assistant.adapters.CountryPickerAdapter
import com.vsphone.databinding.AssistantCountryPickerFragmentBinding
import org.linphone.core.DialPlan

class CountryPickerFragment(private val listener: CountryPickedListener) : DialogFragment() {
    private var _binding: AssistantCountryPickerFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CountryPickerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.assistant_country_dialog_style)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AssistantCountryPickerFragmentBinding.inflate(inflater, container, false)

        adapter = CountryPickerAdapter()
        binding.countryList.adapter = adapter

        binding.countryList.setOnItemClickListener { _, _, position, _ ->
            if (position >= 0 && position < adapter.count) {
                val dialPlan = adapter.getItem(position)
                listener.onCountryClicked(dialPlan)
            }
            dismiss()
        }

        binding.searchCountry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter.filter(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.setCancelClickListener {
            dismiss()
        }

        return binding.root
    }

    interface CountryPickedListener {
        fun onCountryClicked(dialPlan: DialPlan)
    }
}
