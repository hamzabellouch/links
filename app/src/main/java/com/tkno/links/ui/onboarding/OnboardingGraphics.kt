package com.tkno.links.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tkno.links.R

// Official Links Brand Color
val LinksBlue = Color(0xFF1A73E8)

/**
 * Screen 1 Illustration: 1.svg Hero Vector Graphic
 * Spans full width from far right to far left.
 */
@Composable
fun WelcomeIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_onboarding_welcome_hero),
        contentDescription = null,
        modifier = modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
}

/**
 * Screen 2 Illustration: 2.svg Vector Graphic (QR Code + Verified Shield)
 * Centered illustration above "Scan & verify in seconds".
 */
@Composable
fun FeaturesIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_onboarding_features_hero),
            contentDescription = null,
            modifier = Modifier.size(240.dp),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Screen 3 Illustration: 3.svg Vector Graphic (Level Up Growth Arrow)
 * Centered illustration above "Level up your experience".
 */
@Composable
fun LevelUpIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_onboarding_level_up_hero),
            contentDescription = null,
            modifier = Modifier.size(240.dp),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Links Official Brand Chain Badge - Exact matching app icon (Link at 45 degrees in #1A73E8)
 */
@Composable
fun LinksBrandBadge(
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp
) {
    Icon(
        imageVector = Icons.Rounded.Link,
        contentDescription = null,
        tint = LinksBlue,
        modifier = modifier
            .size(iconSize)
            .rotate(45f)
    )
}
