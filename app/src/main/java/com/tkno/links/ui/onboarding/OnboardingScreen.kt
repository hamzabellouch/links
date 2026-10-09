package com.tkno.links.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tkno.links.R
import kotlinx.coroutines.launch

/**
 * Onboarding Flow with 3 screens matching the user's Photoshop concept,
 * fully branded for Links.
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val pageCount = 3
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    // Dark sleek Google/Links background
    val darkBg = Color(0xFF111315)
    val onBgText = Color(0xFFF1F3F4)
    val subtitleText = Color(0xFFC4C7C5)
    val buttonPrimary = Color(0xFFA8C7FA)
    val buttonText = Color(0xFF041E49)
    val outlinedBorder = Color(0xFF444746)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = darkBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Pager with 3 Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> WelcomePage(
                        onBgText = onBgText,
                        subtitleText = subtitleText
                    )
                    1 -> FeaturesPage(
                        onBgText = onBgText,
                        subtitleText = subtitleText
                    )
                    2 -> LevelUpPage(
                        onBgText = onBgText,
                        subtitleText = subtitleText
                    )
                }
            }

            // 2. Bottom Controls Area (Buttons + Dots)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Buttons Row
                AnimatedContent(
                    targetState = pagerState.currentPage,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "button_transition"
                ) { currentPage ->
                    when (currentPage) {
                        0 -> {
                            // Screen 1: Full-width "Get started" button
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonPrimary,
                                    contentColor = buttonText
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.get_started),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }
                        1 -> {
                            // Screen 2: "Skip" and "Continue" buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(2)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = buttonPrimary
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, outlinedBorder)
                                ) {
                                    Text(
                                        text = stringResource(R.string.skip),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(2)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = buttonPrimary,
                                        contentColor = buttonText
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.continue_button),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }
                        }
                        2 -> {
                            // Screen 3: "Not now" and "I'm in!" buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onFinished,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = subtitleText
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, outlinedBorder)
                                ) {
                                    Text(
                                        text = stringResource(R.string.not_now),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }

                                Button(
                                    onClick = onFinished,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = buttonPrimary,
                                        contentColor = buttonText
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.lets_go),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Dots Pagination Indicator (3 dots at the bottom)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pageCount) { index ->
                        val isSelected = pagerState.currentPage == index
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 8.dp else 8.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "dot_width"
                        )
                        val dotColor = if (isSelected) Color.White else Color(0xFF444746)

                        Box(
                            modifier = Modifier
                                .size(width = dotWidth, height = 8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Screen 1: Welcome to Links
 */
@Composable
private fun WelcomePage(
    onBgText: Color,
    subtitleText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Graphic - Edge to Edge from Far Right to Far Left
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            WelcomeIllustration(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bottom Typography & Branding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Links Badge Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinksBrandBadge(iconSize = 22.dp)
                Text(
                    text = stringResource(R.string.links_brand),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = onBgText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Title
            Text(
                text = stringResource(R.string.welcome_to_links),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 38.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                ),
                color = onBgText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Description
            Text(
                text = stringResource(R.string.welcome_desc),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                color = subtitleText
            )
        }
    }
}

/**
 * Screen 2: Fast Scan & Privacy Features
 */
@Composable
private fun FeaturesPage(
    onBgText: Color,
    subtitleText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Graphic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            FeaturesIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }

        // Bottom Typography
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Links Badge Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinksBrandBadge(iconSize = 22.dp)
                Text(
                    text = stringResource(R.string.links_brand),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = onBgText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Title
            Text(
                text = stringResource(R.string.onboarding_features_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 38.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                ),
                color = onBgText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Description
            Text(
                text = stringResource(R.string.onboarding_features_desc),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                color = subtitleText
            )
        }
    }
}

/**
 * Screen 3: Level Up Experience
 */
@Composable
private fun LevelUpPage(
    onBgText: Color,
    subtitleText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Graphic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            LevelUpIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }

        // Bottom Typography & Branding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Links Badge Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinksBrandBadge(iconSize = 22.dp)
                Text(
                    text = stringResource(R.string.links_brand),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = onBgText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Title
            Text(
                text = stringResource(R.string.onboarding_level_up_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 38.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                ),
                color = onBgText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Description
            Text(
                text = stringResource(R.string.onboarding_level_up_desc),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                color = subtitleText
            )
        }
    }
}
