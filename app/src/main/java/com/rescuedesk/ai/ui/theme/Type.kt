package com.rescuedesk.ai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * PRD section 4.3 typography scale — body defaults to 18 sp and emergency
 * instructions to 20–24 sp. System font scaling is layered on top by
 * the platform; screens must reflow, not clip, at large scales (section 4.3).
 */
val RescueDeskTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )
)

/**
 * Applies the in-app text size multiplier (PRD section 5.3) on top of the base scale.
 * System font scaling still layers on top; layouts must reflow, not clip.
 */
fun scaledTypography(factor: Float): Typography {
    if (factor == 1.0f) return RescueDeskTypography
    fun TextStyle.scaled() = copy(fontSize = fontSize * factor, lineHeight = lineHeight * factor)
    return with(RescueDeskTypography) {
        copy(
            displaySmall = displaySmall.scaled(),
            headlineMedium = headlineMedium.scaled(),
            titleLarge = titleLarge.scaled(),
            bodyLarge = bodyLarge.scaled(),
            bodyMedium = bodyMedium.scaled(),
            labelLarge = labelLarge.scaled()
        )
    }
}
