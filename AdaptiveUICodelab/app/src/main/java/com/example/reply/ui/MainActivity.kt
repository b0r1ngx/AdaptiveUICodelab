/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.reply.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.example.reply.data.local.LocalEmailsDataProvider
import com.example.reply.ui.theme.ReplyTheme
import com.example.reply.ui.utils.DevicePosture
import com.example.reply.ui.utils.isBookPosture
import com.example.reply.ui.utils.isSeparating
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainActivity : ComponentActivity() {

    private val viewModel: ReplyHomeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Flow of [DevicePosture] that emits every time there's a change in the windowLayoutInfo
        val devicePostureFlow = WindowInfoTracker.getOrCreate(this)
            .windowLayoutInfo(this).flowWithLifecycle(this.lifecycle).map {
                val foldingFeature = it.displayFeatures
                    .filterIsInstance<FoldingFeature>().firstOrNull()
                when {
                    isBookPosture(foldingFeature) -> DevicePosture.BookPosture(foldingFeature.bounds)
                    isSeparating(foldingFeature) -> DevicePosture.Separating(
                        foldingFeature.bounds,
                        foldingFeature.orientation
                    )
                    else -> DevicePosture.NormalPosture
                }
            }.stateIn(
                scope = lifecycleScope,
                started = SharingStarted.Eagerly,
                initialValue = DevicePosture.NormalPosture
            )

        setContent {
            ReplyTheme {
                val uiState = viewModel.uiState.collectAsState().value
                val windowSize = calculateWindowSizeClass(activity = this)
                val devicePosture = devicePostureFlow.collectAsState().value
                ReplyApp(uiState, windowSize.widthSizeClass, devicePosture)
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
fun ReplyAppPreview() {
    ReplyTheme {
        ReplyApp(
            replyHomeUIState = ReplyHomeUIState(
                emails = LocalEmailsDataProvider.allEmails
            ),
            windowWidthSize = WindowWidthSizeClass.Compact,
            foldingDevicePosture = DevicePosture.NormalPosture,
        )
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    device = Devices.FOLDABLE
)
@Composable
fun ReplyAppPreviewFoldable() {
    ReplyTheme {
        ReplyApp(
            replyHomeUIState = ReplyHomeUIState(
                emails = LocalEmailsDataProvider.allEmails
            ),
            windowWidthSize = WindowWidthSizeClass.Medium,
            foldingDevicePosture = DevicePosture.NormalPosture
        )
    }
}

@Preview(
    widthDp = 800,
    heightDp = 1280,
    showBackground = true
)
@Composable
fun ReplyAppPreviewTablet() {
    ReplyTheme {
        ReplyApp(
            replyHomeUIState = ReplyHomeUIState(
                emails = LocalEmailsDataProvider.allEmails
            ),
            windowWidthSize = WindowWidthSizeClass.Medium,
            foldingDevicePosture = DevicePosture.NormalPosture
        )
    }
}

@Preview(
    widthDp = 1280,
    heightDp = 720,
    showBackground = true
)
@Composable
fun ReplyAppPreviewDesktop() {
    ReplyTheme {
        ReplyApp(
            replyHomeUIState = ReplyHomeUIState(
                emails = LocalEmailsDataProvider.allEmails
            ),
            windowWidthSize = WindowWidthSizeClass.Expanded,
            foldingDevicePosture = DevicePosture.NormalPosture
        )
    }
}