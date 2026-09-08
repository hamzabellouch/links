package com.tkno.links.ui.page.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.tkno.links.ui.component.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasePreferencePage(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit) = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val finalTopBar =
        topBar
            ?: {
                val typography = MaterialTheme.typography
                val overrideTypography =
                    remember(typography) {
                        typography.copy(headlineMedium = typography.displaySmall)
                    }
                MaterialTheme(typography = overrideTypography) {
                    LargeTopAppBar(
                        title = { Text(text = title) },
                        scrollBehavior = scrollBehavior,
                        navigationIcon = { BackButton(onClick = onBack) },
                        windowInsets = WindowInsets(0.dp),
                    )
                }
            }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = finalTopBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}
