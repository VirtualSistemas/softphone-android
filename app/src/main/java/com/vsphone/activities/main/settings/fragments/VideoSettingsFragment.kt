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
package com.vsphone.activities.main.settings.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.vsphone.R
import com.vsphone.VSPhoneApplication.Companion.coreContext
import com.vsphone.activities.main.settings.SettingListenerStub
import com.vsphone.activities.main.settings.viewmodels.VideoSettingsViewModel
import com.vsphone.databinding.SettingsVideoFragmentBinding
import com.vsphone.utils.PermissionHelper
import org.linphone.core.tools.Log

class VideoSettingsFragment : GenericSettingFragment<SettingsVideoFragmentBinding>() {
    private lateinit var viewModel: VideoSettingsViewModel

    override fun getLayoutId(): Int = R.layout.settings_video_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.sharedMainViewModel = sharedViewModel

        viewModel = ViewModelProvider(this)[VideoSettingsViewModel::class.java]
        binding.viewModel = viewModel

        initVideoCodecsList()

        if (!PermissionHelper.required(requireContext()).hasCameraPermission()) {
            Log.i("[Video Settings] Asking for CAMERA permission")
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 0)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val granted =
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (granted) {
            Log.i("[Video Settings] CAMERA permission granted")
            coreContext.core.reloadVideoDevices()
            viewModel.initCameraDevicesList()
        } else {
            Log.w("[Video Settings] CAMERA permission denied")
        }
    }

    private fun initVideoCodecsList() {
        val list = arrayListOf<ViewDataBinding>()
        for (payload in coreContext.core.videoPayloadTypes) {
            val binding = DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(requireContext()),
                R.layout.settings_widget_switch_and_text,
                null,
                false
            )
            binding.setVariable(com.vsphone.BR.switch_title, payload.mimeType)
            binding.setVariable(com.vsphone.BR.switch_subtitle, "")
            binding.setVariable(com.vsphone.BR.text_title, "recv-fmtp")
            binding.setVariable(com.vsphone.BR.text_subtitle, "")
            binding.setVariable(com.vsphone.BR.defaultValue, payload.recvFmtp)
            binding.setVariable(com.vsphone.BR.checked, payload.enabled())
            binding.setVariable(
                com.vsphone.BR.listener,
                object : SettingListenerStub() {
                    override fun onBoolValueChanged(newValue: Boolean) {
                        payload.enable(newValue)
                    }

                    override fun onTextValueChanged(newValue: String) {
                        payload.recvFmtp = newValue
                    }
                }
            )
            binding.lifecycleOwner = viewLifecycleOwner
            list.add(binding)
        }
        viewModel.videoCodecs.value = list
    }
}
