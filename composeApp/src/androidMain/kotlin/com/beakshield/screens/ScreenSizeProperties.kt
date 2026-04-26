package com.example.vectorapp.screens

import androidx.compose.runtime.compositionLocalOf
import com.beakshield.screens.AppDeviceType

val LocalDeviceType = compositionLocalOf<AppDeviceType> { AppDeviceType.Compact(0, 0) }
