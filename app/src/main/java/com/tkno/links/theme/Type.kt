package com.tkno.links.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tkno.links.R

val NotoSansArabic =
    FontFamily(
        Font(R.font.noto_sans_arabic_light, FontWeight.Light),
        Font(R.font.noto_sans_arabic_regular, FontWeight.Normal),
        Font(R.font.noto_sans_arabic_medium, FontWeight.Medium),
        Font(R.font.noto_sans_arabic_semibold, FontWeight.SemiBold),
        Font(R.font.noto_sans_arabic_bold, FontWeight.Bold),
    )

// Set of Material typography styles to start with
val Typography =
    Typography(
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            )
    )

fun Typography.withArabicFont(fontFamily: FontFamily = NotoSansArabic): Typography {
    return Typography(
        displayLarge = displayLarge.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        displayMedium = displayMedium.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        displaySmall = displaySmall.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        titleLarge = titleLarge.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        titleMedium = titleMedium.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        titleSmall = titleSmall.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        bodyLarge = bodyLarge.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        bodySmall = bodySmall.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        labelLarge = labelLarge.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        labelMedium = labelMedium.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
        labelSmall = labelSmall.copy(fontFamily = fontFamily, letterSpacing = 0.sp),
    )
}

val ArabicTypography = Typography.withArabicFont(NotoSansArabic)

