package com.beakshield.screens

import android.util.Log
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import com.beakshield.screens.AppDeviceType.Companion.HEIGHT_DP_LOWER_BOUND_MEDIUM
import com.beakshield.screens.AppDeviceType.Companion.WIDTH_DP_LOWER_BOUND_EXPANDED
import com.beakshield.screens.AppDeviceType.Companion.WIDTH_DP_LOWER_BOUND_EXTRA_LARGE
import com.beakshield.screens.AppDeviceType.Companion.WIDTH_DP_LOWER_BOUND_LARGE
import com.beakshield.screens.AppDeviceType.Companion.WIDTH_DP_LOWER_BOUND_MEDIUM

sealed class AppDeviceType : Comparable<AppDeviceType> {
    /** NOTE: Original DeviceType class developed by https://gist.github.com/stevdza-san/d85974f0e7159c33118d1c116b3fc0ff. */

    abstract val minWidth: Int
    abstract val minHeight: Int
    abstract val rank: Int

    override fun compareTo(other: AppDeviceType): Int = this.rank - other.rank

    data class Compact(
        override val minWidth: Int,
        override val minHeight: Int,
        override val rank: Int = 0,
    ) : AppDeviceType()

    data class Medium(
        override val minWidth: Int,
        override val minHeight: Int,
        override val rank: Int = 1,
    ) : AppDeviceType()

    data class Expanded(
        override val minWidth: Int,
        override val minHeight: Int,
        override val rank: Int = 2,
    ) : AppDeviceType()

    data class Large(
        override val minWidth: Int,
        override val minHeight: Int,
        override val rank: Int = 3,
    ) : AppDeviceType()

    data class ExtraLarge(
        override val minWidth: Int,
        override val minHeight: Int,
        override val rank: Int = 4,
    ) : AppDeviceType()

    companion object {
        const val WIDTH_DP_LOWER_BOUND_EXTRA_LARGE = 1600
        const val WIDTH_DP_LOWER_BOUND_LARGE = 1200
        const val WIDTH_DP_LOWER_BOUND_EXPANDED = 840
        const val WIDTH_DP_LOWER_BOUND_MEDIUM = 600
        const val WIDTH_DP_LOWER_BOUND_COMPACT = 0

        const val HEIGHT_DP_LOWER_BOUND_EXPANDED = 900
        const val HEIGHT_DP_LOWER_BOUND_MEDIUM = 480
        const val HEIGHT_DP_LOWER_BOUND_COMPACT = 0
    }
}

fun getDeviceType(
    windowAdaptiveInfo: WindowAdaptiveInfo
): AppDeviceType {
    val deviceType = declassifyFromSize(
        windowAdaptiveInfo.windowSizeClass.minWidthDp,
        windowAdaptiveInfo.windowSizeClass.minHeightDp
    )
    Log.e("DEVICE TYPE", "DeviceType: $deviceType")
    return deviceType
}
fun declassifyFromSize(width: Int, height: Int): AppDeviceType {
    // Device classification based on width and aspect ratio
    val aspectRatio = (width.toFloat() / height.toFloat())
    return when {
        width >= WIDTH_DP_LOWER_BOUND_EXTRA_LARGE -> AppDeviceType.ExtraLarge(width, height)
        width >= WIDTH_DP_LOWER_BOUND_LARGE -> AppDeviceType.Large(width, height)
        width >= WIDTH_DP_LOWER_BOUND_EXPANDED -> {
            if (aspectRatio >= 1.8f && height < HEIGHT_DP_LOWER_BOUND_MEDIUM) {
                AppDeviceType.Compact(width, height)
            } else {
                AppDeviceType.Expanded(width, height)
            }
        }
        width >= WIDTH_DP_LOWER_BOUND_MEDIUM -> {
            if (aspectRatio >= 1.8f) {
                AppDeviceType.Compact(width, height)
            } else {
                AppDeviceType.Medium(width, height)
            }
        }
        else -> AppDeviceType.Compact(width, height)
    }
}

infix fun AppDeviceType.greaterThan(other: AppDeviceType): Boolean = this > other
infix fun AppDeviceType.greaterThanOrEqual(other: AppDeviceType): Boolean = this >= other
infix fun AppDeviceType.smallerThan(other: AppDeviceType): Boolean = this < other
infix fun AppDeviceType.smallerThanOrEqual(other: AppDeviceType): Boolean = this <= other

val AppDeviceType.isNonCompactLandscape: Boolean
    get() = this.isLandscape && !this.isLandscapeCompact
val AppDeviceType.isLandscapeCompact: Boolean
    get() = this.isLandscape
            && minHeight < HEIGHT_DP_LOWER_BOUND_MEDIUM
            && (minWidth.toFloat() / minHeight.toFloat()) >= 1.8f
val AppDeviceType.isLandscape: Boolean
    get() = minWidth > minHeight
val AppDeviceType.isPortraitCompact: Boolean
    get() = ((this is AppDeviceType.Compact) && !this.isLandscapeCompact)

//@Composable
//fun PreviewInMainApp(
//    content: @Composable (AppDeviceType) -> Unit = {}
//) {
//    val configuration = LocalConfiguration.current
//    val deviceType = declassifyFromSize(configuration.screenWidthDp, configuration.screenHeightDp)
//
//    CompositionLocalProvider(LocalDeviceType provides deviceType) {
//        MainActivityBase(
//            isBannerHidden = isBannerHidden,
//            curNavBarState = curNavBarState
//        ) {
//            Text(    // USED FOR DEBUGGING
//                modifier = Modifier,
//                text = "$deviceType",
//                textAlign = TextAlign.Center,
//                color = Color.White,
//                fontSize = 30.sp)
//            content(deviceType)
//        }
//    }
//}