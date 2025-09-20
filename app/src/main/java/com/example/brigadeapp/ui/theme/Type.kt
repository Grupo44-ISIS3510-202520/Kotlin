package com.example.brigadeapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.brigadeapp.R

val SplineSans = FontFamily(
    Font(R.font.splinesans_regular,  weight = FontWeight.Normal),
    Font(R.font.splinesans_medium,   weight = FontWeight.Medium),
    Font(R.font.splinesans_semibold, weight = FontWeight.SemiBold),
    Font(R.font.splinesans_bold,     weight = FontWeight.Bold),
    Font(R.font.splinesans_light,    weight = FontWeight.Light),
)

val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
)
