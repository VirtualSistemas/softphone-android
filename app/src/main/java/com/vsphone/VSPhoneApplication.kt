/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
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
package com.vsphone

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.vsphone.core.CoreContext
import com.vsphone.core.CorePreferences
import com.vsphone.core.CoreService
import org.linphone.core.*
import org.linphone.core.tools.Log
import org.linphone.mediastream.Version

class VSPhoneApplication : Application(), ImageLoaderFactory {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var corePreferences: CorePreferences

        @SuppressLint("StaticFieldLeak")
        lateinit var coreContext: CoreContext

        private fun createConfig(context: Context) {
            if (com.vsphone.VSPhoneApplication.Companion::corePreferences.isInitialized) {
                return
            }

            Factory.instance().setLogCollectionPath(context.filesDir.absolutePath)
            Factory.instance().enableLogCollection(LogCollectionState.Enabled)

            // For VFS
            Factory.instance().setCacheDir(context.cacheDir.absolutePath)

            com.vsphone.VSPhoneApplication.Companion.corePreferences = CorePreferences(context)
            com.vsphone.VSPhoneApplication.Companion.corePreferences.copyAssetsFromPackage()

            if (com.vsphone.VSPhoneApplication.Companion.corePreferences.vfsEnabled) {
                CoreContext.activateVFS()
            }

            val config = Factory.instance().createConfigWithFactory(
                com.vsphone.VSPhoneApplication.Companion.corePreferences.configPath,
                com.vsphone.VSPhoneApplication.Companion.corePreferences.factoryConfigPath
            )
            com.vsphone.VSPhoneApplication.Companion.corePreferences.config = config

            val appName = context.getString(org.linphone.R.string.app_name)
            Factory.instance().setLoggerDomain(appName)
            Factory.instance()
                .enableLogcatLogs(com.vsphone.VSPhoneApplication.Companion.corePreferences.logcatLogsOutput)
            if (com.vsphone.VSPhoneApplication.Companion.corePreferences.debugLogs) {
                Factory.instance().loggingService.setLogLevel(LogLevel.Message)
            }

            Log.i("[Application] Core config & preferences created")
        }

        fun ensureCoreExists(
            context: Context,
            pushReceived: Boolean = false,
            service: CoreService? = null,
            useAutoStartDescription: Boolean = false
        ): Boolean {
            if (com.vsphone.VSPhoneApplication.Companion::coreContext.isInitialized && !com.vsphone.VSPhoneApplication.Companion.coreContext.stopped) {
                Log.d("[Application] Skipping Core creation (push received? $pushReceived)")
                return false
            }

            Log.i("[Application] Core context is being created ${if (pushReceived) "from push" else ""}")
            com.vsphone.VSPhoneApplication.Companion.coreContext = CoreContext(
                context,
                com.vsphone.VSPhoneApplication.Companion.corePreferences.config,
                service,
                useAutoStartDescription
            )
            com.vsphone.VSPhoneApplication.Companion.coreContext.start()
            return true
        }

        fun contextExists(): Boolean {
            return com.vsphone.VSPhoneApplication.Companion::coreContext.isInitialized
        }
    }

    override fun onCreate() {
        super.onCreate()
        val appName = getString(org.linphone.R.string.app_name)
        android.util.Log.i("[$appName]", "Application is being created")
        com.vsphone.VSPhoneApplication.Companion.createConfig(applicationContext)
        Log.i("[Application] Created")
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
                add(SvgDecoder.Factory())
                if (Version.sdkAboveOrEqual(Version.API28_PIE_90)) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }
}
