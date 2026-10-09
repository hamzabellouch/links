package com.tkno.links

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tkno.links.util.VirusTotalScanner
import kotlinx.coroutines.launch

private val SecurityReportSaver = Saver<VirusTotalScanner.SecurityReport?, List<Any>>(
    save = { report ->
        report?.let {
            listOf(
                it.sourceUrl,
                it.destinationUrl,
                it.harmlessCount,
                it.maliciousCount,
                it.suspiciousCount,
                it.undetectedCount,
                it.timeoutCount,
                it.reputation,
                it.title ?: "",
                it.categories,
                it.safetyStatus.name
            )
        }
    },
    restore = { list ->
        VirusTotalScanner.SecurityReport(
            sourceUrl = list[0] as String,
            destinationUrl = list[1] as String,
            harmlessCount = list[2] as Int,
            maliciousCount = list[3] as Int,
            suspiciousCount = list[4] as Int,
            undetectedCount = list[5] as Int,
            timeoutCount = list[6] as Int,
            reputation = list[7] as Int,
            title = (list[8] as String).ifEmpty { null },
            categories = (list[9] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            safetyStatus = try {
                VirusTotalScanner.SafetyStatus.valueOf(list[10] as String)
            } catch (e: Exception) {
                VirusTotalScanner.SafetyStatus.UNKNOWN
            }
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    initialUrl: String? = null,
    onInitialUrlConsumed: () -> Unit = {}
) {
    var urlInput by rememberSaveable { mutableStateOf("") }
    var report by rememberSaveable(stateSaver = SecurityReportSaver) { mutableStateOf<VirusTotalScanner.SecurityReport?>(null) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Top menu & dialog state
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    val softBlue = MaterialTheme.colorScheme.primary
    val borderGrey = MaterialTheme.colorScheme.outlineVariant
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    val onSurface = MaterialTheme.colorScheme.onSurface

    val isResolved = report != null

    fun handleScan(overrideUrl: String? = null) {
        val rawInput = overrideUrl ?: urlInput
        val trimmedInput = rawInput.trim()
        if (trimmedInput.isEmpty()) {
            errorMessage = context.getString(R.string.enter_url_error)
            return
        }

        if (!LinkResolver.isValidUrl(trimmedInput)) {
            errorMessage = context.getString(R.string.invalid_url_format)
            return
        }

        if (!VirusTotalScanner.hasApiKey(context)) {
            showApiKeyDialog = true
            errorMessage = context.getString(R.string.api_key_required)
            return
        }

        urlInput = trimmedInput
        errorMessage = null
        keyboardController?.hide()
        focusManager.clearFocus()
        isLoading = true

        coroutineScope.launch {
            try {
                when (val result = VirusTotalScanner.scanUrl(context, trimmedInput)) {
                    is VirusTotalScanner.ScanResult.Success -> {
                        report = result.report
                        errorMessage = null
                        focusManager.clearFocus()
                    }
                    is VirusTotalScanner.ScanResult.Error -> {
                        errorMessage = result.error.getLocalizedMessage(context)
                        if (result.error is VirusTotalScanner.ScanError.MissingApiKey) {
                            showApiKeyDialog = true
                        }
                        report = null
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: context.getString(R.string.unknown_error)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(initialUrl) {
        if (!initialUrl.isNullOrBlank()) {
            urlInput = initialUrl
            report = null
            handleScan(initialUrl)
            onInitialUrlConsumed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 96.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row with Title and 3-dots Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 12.dp)
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.security),
                    color = onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.virustotal_api_key)) },
                            onClick = {
                                isMenuExpanded = false
                                showApiKeyDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = softBlue
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // URL Input Field
            OutlinedTextField(
                value = urlInput,
                onValueChange = {
                    urlInput = it
                    if (errorMessage != null) {
                        errorMessage = null
                    }
                },
                readOnly = isResolved,
                label = { Text(text = stringResource(R.string.security_scan)) },
                placeholder = { Text(text = stringResource(R.string.security_url_placeholder)) },
                singleLine = true,
                isError = errorMessage != null,
                supportingText = if (errorMessage != null) {
                    {
                        Text(
                            text = errorMessage!!,
                            color = errorColor,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = { handleScan() }
                ),
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = softBlue,
                            strokeWidth = 2.dp
                        )
                    } else if (isResolved || (errorMessage != null && urlInput.trim().isNotEmpty())) {
                        IconButton(
                            onClick = {
                                urlInput = ""
                                report = null
                                errorMessage = null
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = if (errorMessage != null) errorColor else softBlue
                            )
                        }
                    } else {
                        IconButton(onClick = { handleScan() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Scan Security",
                                tint = if (errorMessage != null) errorColor else if (urlInput.isNotEmpty()) softBlue else textMuted
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = softBlue,
                    unfocusedBorderColor = borderGrey,
                    focusedLabelColor = softBlue,
                    unfocusedLabelColor = textMuted,
                    focusedPlaceholderColor = textMuted,
                    unfocusedPlaceholderColor = textMuted,
                    focusedTextColor = onSurface,
                    unfocusedTextColor = onSurface,
                    cursorColor = softBlue,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorBorderColor = errorColor,
                    errorLabelColor = errorColor,
                    errorTrailingIconColor = errorColor,
                    errorSupportingTextColor = errorColor,
                    errorCursorColor = errorColor
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Results Section (Always visible)
            Spacer(modifier = Modifier.height(24.dp))

            // Security Verdict Card
            SecurityVerdictCard(report = report)

            Spacer(modifier = Modifier.height(16.dp))

            // Engine Statistics Breakdown Chips
            SecurityStatsBreakdown(report = report)

            // Categories / Tags (if available)
            if (report != null && report!!.categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    report!!.categories.take(3).forEach { category ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            // Guide card when no API key is configured
            if (report == null && !VirusTotalScanner.hasApiKey(context)) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    onClick = { showApiKeyDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.set_api_key),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.virustotal_api_key_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }

        // Paste FAB
        FloatingActionButton(
            onClick = {
                clipboardManager.getText()?.text?.let { clipText ->
                    if (clipText.isNotBlank()) {
                        urlInput = clipText.toString()
                        errorMessage = null
                        Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                }
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentPaste,
                contentDescription = "Paste from clipboard",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // VirusTotal API Key Dialog
    if (showApiKeyDialog) {
        ApiKeyConfigDialog(
            onDismiss = { showApiKeyDialog = false },
            onKeySaved = {
                showApiKeyDialog = false
                if (errorMessage == context.getString(R.string.api_key_required)) {
                    errorMessage = null
                }
            }
        )
    }
}

@Composable
private fun SecurityVerdictCard(
    report: VirusTotalScanner.SecurityReport?
) {
    val (backgroundColor, contentColor, title, description) = when {
        // الحالة 1: لم يتم الفحص بعد (Idle)
        report == null -> {
            val color = MaterialTheme.colorScheme.onSurfaceVariant
            VerdictVisuals(
                backgroundColor = color.copy(alpha = 0.08f),
                contentColor = color,
                title = stringResource(R.string.security_no_url_scanned),
                description = stringResource(R.string.security_no_url_scanned_desc)
            )
        }
        // الحالة 2: روابط خبيثة (Malicious)
        report.maliciousCount > 0 -> {
            val color = Color(0xFFD32F2F)
            VerdictVisuals(
                backgroundColor = color.copy(alpha = 0.1f),
                contentColor = color,
                title = stringResource(R.string.malicious_url),
                description = "${report.maliciousCount} / ${report.totalEngines.coerceAtLeast(1)} ${stringResource(R.string.malicious_url_desc)}"
            )
        }
        // الحالة 3: روابط مشبوهة (Suspicious)
        report.suspiciousCount > 0 -> {
            val color = Color(0xFFF57C00)
            VerdictVisuals(
                backgroundColor = color.copy(alpha = 0.1f),
                contentColor = color,
                title = stringResource(R.string.suspicious_url),
                description = "${report.suspiciousCount} / ${report.totalEngines.coerceAtLeast(1)} ${stringResource(R.string.suspicious_url_desc)}"
            )
        }
        // الحالة 4: رابط آمن / سليم (Harmless)
        report.harmlessCount > 0 -> {
            val color = Color(0xFF2E7D32)
            VerdictVisuals(
                backgroundColor = color.copy(alpha = 0.1f),
                contentColor = color,
                title = stringResource(R.string.safe_url),
                description = stringResource(R.string.safe_url_desc)
            )
        }
        // الحالة 5: غير مصنف / غير مكتشف (Undetected / Unrated)
        else -> {
            val color = MaterialTheme.colorScheme.onSurfaceVariant
            VerdictVisuals(
                backgroundColor = color.copy(alpha = 0.1f),
                contentColor = color,
                title = stringResource(R.string.unrated_url),
                description = stringResource(R.string.unrated_url_desc)
            )
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecurityScoreGauge(
                report = report,
                contentColor = contentColor,
                size = 64.dp,
                strokeWidth = 6.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun SecurityScoreGauge(
    report: VirusTotalScanner.SecurityReport?,
    contentColor: Color,
    size: androidx.compose.ui.unit.Dp = 64.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 6.dp,
    modifier: Modifier = Modifier
) {
    val topText: String
    val bottomText: String
    val topColor: Color
    val bottomColor = contentColor.copy(alpha = 0.75f)

    if (report == null) {
        topText = "-"
        bottomText = "/-"
        topColor = contentColor
    } else {
        val total = report.totalEngines
        topText = when {
            report.maliciousCount > 0 -> "${report.maliciousCount}"
            report.suspiciousCount > 0 -> "${report.suspiciousCount}"
            else -> "0"
        }
        bottomText = "/$total"
        topColor = when {
            report.maliciousCount > 0 -> Color(0xFFD32F2F)
            report.suspiciousCount > 0 -> Color(0xFFF57C00)
            report.harmlessCount > 0 -> Color(0xFF2E7D32)
            else -> contentColor
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val radius = diameter / 2f
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(
                (this.size.width - diameter) / 2f,
                (this.size.height - diameter) / 2f
            )

            if (report == null) {
                // Idle state: subtle neutral ring
                drawCircle(
                    color = contentColor.copy(alpha = 0.35f),
                    radius = radius,
                    style = Stroke(width = strokePx)
                )
            } else {
                val total = report.totalEngines.coerceAtLeast(1)
                val isThreat = report.maliciousCount > 0 || report.suspiciousCount > 0

                if (!isThreat) {
                    if (report.harmlessCount > 0) {
                        // Safe state: solid vibrant green ring
                        drawCircle(
                            color = Color(0xFF2E7D32),
                            radius = radius,
                            style = Stroke(width = strokePx)
                        )
                    } else {
                        // Undetected / unrated: subtle grey ring
                        drawCircle(
                            color = contentColor.copy(alpha = 0.4f),
                            radius = radius,
                            style = Stroke(width = strokePx)
                        )
                    }
                } else {
                    // Base background track
                    drawCircle(
                        color = Color(0xFFE0E0E0),
                        radius = radius,
                        style = Stroke(width = strokePx)
                    )

                    val totalF = total.toFloat()
                    var startAngle = -90f

                    // 1. Malicious arc (Red)
                    if (report.maliciousCount > 0) {
                        val sweep = (report.maliciousCount.toFloat() / totalF) * 360f
                        drawArc(
                            color = Color(0xFFD32F2F),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx)
                        )
                        startAngle += sweep
                    }

                    // 2. Suspicious arc (Orange)
                    if (report.suspiciousCount > 0) {
                        val sweep = (report.suspiciousCount.toFloat() / totalF) * 360f
                        drawArc(
                            color = Color(0xFFF57C00),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx)
                        )
                        startAngle += sweep
                    }

                    // 3. Harmless arc (Green)
                    if (report.harmlessCount > 0) {
                        val sweep = (report.harmlessCount.toFloat() / totalF) * 360f
                        drawArc(
                            color = Color(0xFF388E3C),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx)
                        )
                        startAngle += sweep
                    }

                    // 4. Undetected arc (Grey)
                    if (report.undetectedCount > 0) {
                        val sweep = (report.undetectedCount.toFloat() / totalF) * 360f
                        drawArc(
                            color = Color(0xFF9E9E9E).copy(alpha = 0.5f),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx)
                        )
                        startAngle += sweep
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = topText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = topColor,
                maxLines = 1
            )
            Text(
                text = bottomText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = bottomColor,
                maxLines = 1
            )
        }
    }
}

private data class VerdictVisuals(
    val backgroundColor: Color,
    val contentColor: Color,
    val title: String,
    val description: String
)

@Composable
private fun SecurityStatsBreakdown(
    report: VirusTotalScanner.SecurityReport?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBadge(
            label = stringResource(R.string.malicious),
            count = report?.maliciousCount,
            accentColor = Color(0xFFD32F2F),
            modifier = Modifier.weight(1f)
        )
        StatBadge(
            label = stringResource(R.string.suspicious),
            count = report?.suspiciousCount,
            accentColor = Color(0xFFF57C00),
            modifier = Modifier.weight(1f)
        )
        StatBadge(
            label = stringResource(R.string.harmless),
            count = report?.harmlessCount,
            accentColor = Color(0xFF388E3C),
            modifier = Modifier.weight(1f)
        )
        StatBadge(
            label = stringResource(R.string.undetected),
            count = report?.undetectedCount,
            accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatBadge(
    label: String,
    count: Int?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count?.toString() ?: "-",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ApiKeyConfigDialog(
    onDismiss: () -> Unit,
    onKeySaved: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    var currentKey by remember { mutableStateOf(VirusTotalScanner.getApiKey(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.VpnKey,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.virustotal_api_key),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.virustotal_api_key_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = currentKey,
                    onValueChange = { currentKey = it },
                    label = { Text(stringResource(R.string.virustotal_api_key)) },
                    placeholder = { Text(stringResource(R.string.api_key_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (currentKey.isNotEmpty()) {
                            IconButton(onClick = { currentKey = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                clipboardManager.getText()?.text?.let {
                                    if (it.isNotBlank()) currentKey = it.toString().trim()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentPaste,
                                    contentDescription = "Paste",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = {
                        uriHandler.openUri("https://www.virustotal.com/gui/my-apikey")
                    },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.get_free_api_key),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = currentKey.trim()
                    VirusTotalScanner.saveApiKey(context, trimmed)
                    Toast.makeText(context, context.getString(R.string.api_key_saved), Toast.LENGTH_SHORT).show()
                    onKeySaved()
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (VirusTotalScanner.hasApiKey(context)) {
                    TextButton(
                        onClick = {
                            VirusTotalScanner.clearApiKey(context)
                            currentKey = ""
                            Toast.makeText(context, context.getString(R.string.api_key_cleared), Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.remove_api_key),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}
