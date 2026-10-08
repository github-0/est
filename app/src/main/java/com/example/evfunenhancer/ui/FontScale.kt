package com.example.evfunenhancer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Grows a fixed size that has to hold text with the system font size, so larger fonts don't
 * wrap or clip. Never shrinks below the given size, so the default font size is unaffected.
 */
@Composable
@ReadOnlyComposable
fun Dp.fontScaled(): Dp = this * LocalDensity.current.fontScale.coerceAtLeast(1f)
