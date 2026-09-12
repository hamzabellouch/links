package com.tkno.links

import android.Manifest
import android.graphics.Bitmap
import android.widget.Toast
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.tkno.links.R
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material.icons.outlined.QrCodeScanner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.History
import androidx.compose.ui.text.style.TextOverflow
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.tkno.links.ui.component.BackButton
import kotlinx.coroutines.launch

fun decodeQrFromUri(context: Context, uri: Uri): String? {
    return try {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            android.graphics.BitmapFactory.decodeStream(stream)
        } ?: return null

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.recycle()

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader().apply {
            val hints = java.util.EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
            hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(BarcodeFormat.QR_CODE)
            setHints(hints)
        }
        val result = reader.decode(binaryBitmap)
        result.text
    } catch (e: Exception) {
        null
    }
}

object ScanHistoryManager {
    private const val PREFS_NAME = "scan_history_prefs"
    private const val KEY_HISTORY = "scan_history_list"

    fun getHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val jsonArray = org.json.JSONArray(jsonString)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addHistoryItem(context: Context, item: String) {
        if (item.isBlank()) return
        val current = getHistory(context).toMutableList()
        current.remove(item)
        current.add(0, item)
        if (current.size > 50) current.removeAt(current.lastIndex)

        val jsonArray = org.json.JSONArray()
        current.forEach { jsonArray.put(it) }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryPage(
    historyList: List<String>,
    onClearHistory: () -> Unit,
    onSelectItem: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val softBlue = MaterialTheme.colorScheme.primary
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderGrey = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.scan_history),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    if (historyList.isNotEmpty()) {
                        IconButton(onClick = onClearHistory) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.clear_history),
                                tint = softBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = textMuted,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_scan_history_yet),
                            color = textMuted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(historyList) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderGrey),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectItem(item) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(item))
                                        Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.copy),
                                        tint = softBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class QrMode {
    Scan, Generate, Costume
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QrCodeScreen() {
    var currentMode by remember { mutableStateOf(QrMode.Scan) }
    var selectedHistoryItem by remember { mutableStateOf<String?>(null) }
    var isHistoryPageOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var historyList by remember { mutableStateOf(ScanHistoryManager.getHistory(context)) }

    val softBlue = MaterialTheme.colorScheme.primary
    val borderGrey = MaterialTheme.colorScheme.outlineVariant
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    BackHandler(enabled = isHistoryPageOpen) {
        isHistoryPageOpen = false
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isHistoryPageOpen) {
            ScanHistoryPage(
                historyList = historyList,
                onClearHistory = {
                    ScanHistoryManager.clearHistory(context)
                    historyList = emptyList()
                },
                onSelectItem = { item ->
                    selectedHistoryItem = item
                    isHistoryPageOpen = false
                },
                onBack = { isHistoryPageOpen = false }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 12.dp)
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentMode) {
                            QrMode.Scan -> stringResource(R.string.scan_qr_code)
                            QrMode.Generate -> stringResource(R.string.qr_code_generator)
                            QrMode.Costume -> stringResource(R.string.costume_qr_code)
                        },
                        color = onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (currentMode == QrMode.Scan) {
                        IconButton(
                            onClick = {
                                historyList = ScanHistoryManager.getHistory(context)
                                isHistoryPageOpen = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "Scan History",
                                tint = softBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (currentMode) {
                        QrMode.Scan -> {
                            ScanContent(
                                softBlue = softBlue,
                                borderGrey = borderGrey,
                                textMuted = textMuted,
                                externalScannedResult = selectedHistoryItem,
                                onClearExternalScannedResult = { selectedHistoryItem = null }
                            )
                        }
                        QrMode.Generate -> {
                            GenerateContent(softBlue = softBlue, borderGrey = borderGrey, textMuted = textMuted)
                        }
                        QrMode.Costume -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = textMuted,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.option_currently_unavailable),
                                        color = textMuted,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Mode Selection Segmented Bar
                QrModeSelectionBar(
                    selectedMode = currentMode,
                    onSelect = { currentMode = it },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrModeSelectionBar(
    selectedMode: QrMode,
    onSelect: (QrMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = remember { listOf(QrMode.Scan, QrMode.Generate, QrMode.Costume) }
    val softBlue = MaterialTheme.colorScheme.primary
    val borderGrey = MaterialTheme.colorScheme.outlineVariant
    
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, mode ->
            val isSelected = selectedMode == mode
            
            val colors = SegmentedButtonDefaults.colors(
                activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                inactiveContainerColor = Color.Transparent,
                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                activeBorderColor = borderGrey,
                inactiveBorderColor = borderGrey
            )
            
            SegmentedButton(
                selected = isSelected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                colors = colors,
                label = {
                    Text(
                        text = when (mode) {
                            QrMode.Scan -> stringResource(R.string.scan)
                            QrMode.Generate -> stringResource(R.string.generate)
                            QrMode.Costume -> stringResource(R.string.costume)
                        },
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}

@Composable
fun GoogleStyleViewfinder(modifier: Modifier = Modifier) {
    val softBlue = Color(0xFF8AB4F8)
    Canvas(modifier = modifier) {
        val strokeWidth = 5.dp.toPx()
        val cornerLength = 40.dp.toPx()
        val cornerRadius = 24.dp.toPx()

        val w = size.width
        val h = size.height

        // Top-Left
        drawPath(
            path = Path().apply {
                moveTo(0f, cornerLength)
                lineTo(0f, cornerRadius)
                quadraticTo(0f, 0f, cornerRadius, 0f)
                lineTo(cornerLength, 0f)
            },
            color = softBlue,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Top-Right
        drawPath(
            path = Path().apply {
                moveTo(w - cornerLength, 0f)
                lineTo(w - cornerRadius, 0f)
                quadraticTo(w, 0f, w, cornerRadius)
                lineTo(w, cornerLength)
            },
            color = softBlue,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Bottom-Left
        drawPath(
            path = Path().apply {
                moveTo(0f, h - cornerLength)
                lineTo(0f, h - cornerRadius)
                quadraticTo(0f, h, cornerRadius, h)
                lineTo(cornerLength, h)
            },
            color = softBlue,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Bottom-Right
        drawPath(
            path = Path().apply {
                moveTo(w - cornerLength, h)
                lineTo(w - cornerRadius, h)
                quadraticTo(w, h, w, h - cornerRadius)
                lineTo(w, h - cornerLength)
            },
            color = softBlue,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun FullScreenScannerModal(
    onClose: () -> Unit,
    onQrCodeScanned: (String) -> Unit,
) {
    var isTorchEnabled by remember { mutableStateOf(false) }
    var cameraLensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(0f) }
    val softBlue = Color(0xFF8AB4F8)
    val context = LocalContext.current

    // Resolve the host Activity even when context is wrapped (e.g. inside a Dialog)
    val activity = remember(context) {
        var ctx: android.content.Context = context
        while (ctx is android.content.ContextWrapper && ctx !is android.app.Activity) {
            ctx = ctx.baseContext
        }
        ctx as? android.app.Activity
    }

    // True if the front camera has a hardware flash unit
    val hasFrontCameraFlash = remember {
        try {
            val cm = context.getSystemService(android.content.Context.CAMERA_SERVICE)
                    as android.hardware.camera2.CameraManager
            val frontId = cm.cameraIdList.firstOrNull { id ->
                cm.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.LENS_FACING) ==
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT
            }
            frontId?.let {
                cm.getCameraCharacteristics(it)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: false
        } catch (e: Exception) { false }
    }

    // Active when front camera has no hardware flash and the user presses the flash button
    var isScreenFlashEnabled by remember { mutableStateOf(false) }

    // Raise / restore screen brightness to simulate a flash via the display
    LaunchedEffect(isScreenFlashEnabled) {
        activity?.window?.let { win ->
            val lp = win.attributes
            lp.screenBrightness = if (isScreenFlashEnabled) 0.33f
                                   else android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            win.attributes = lp
        }
    }

    // Reset all flash states whenever the user flips the camera
    LaunchedEffect(cameraLensFacing) {
        isTorchEnabled = false
        isScreenFlashEnabled = false
    }

    // Restore normal screen brightness and reset torch when the scanner dialog is dismissed
    DisposableEffect(Unit) {
        onDispose {
            isTorchEnabled = false
            isScreenFlashEnabled = false
            activity?.window?.let { win ->
                val lp = win.attributes
                lp.screenBrightness = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                win.attributes = lp
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val scanned = decodeQrFromUri(context, it)
            if (scanned != null) {
                onQrCodeScanned(scanned)
                onClose()
            } else {
                Toast.makeText(context, "No QR code found in selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            // Camera Stream
            QrScannerView(
                onQrCodeScanned = { result ->
                    onQrCodeScanned(result)
                    onClose()
                },
                isTorchEnabled = isTorchEnabled,
                cameraLensFacing = cameraLensFacing,
                zoomRatio = zoomRatio,
                modifier = Modifier.fillMaxSize(),
            )

            // Screen Flash Overlay: white border shown when front camera has no hardware flash
            if (isScreenFlashEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(width = 18.dp, color = Color.White)
                )
            }

            // Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                // Close button (white circle with black X)
                Surface(
                    onClick = onClose,
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                // Title
                Text(
                    text = "Scan code",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Actions Column: Flash, Image Picker, Flip Camera
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Flashlight toggle button (white circle)
                    Surface(
                        onClick = {
                            val isFront = cameraLensFacing == CameraSelector.LENS_FACING_FRONT
                            if (isFront && !hasFrontCameraFlash) {
                                // Front camera without hardware flash → use screen flash
                                isScreenFlashEnabled = !isScreenFlashEnabled
                            } else {
                                // Back camera or front camera with hardware flash → use torch
                                isTorchEnabled = !isTorchEnabled
                            }
                        },
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isTorchEnabled || isScreenFlashEnabled)
                                    Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                                contentDescription = "Flashlight",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }

                    // 2. Flip Camera circle button (under Flash button)
                    Surface(
                        onClick = {
                            cameraLensFacing = if (cameraLensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Cameraswitch,
                                contentDescription = "Flip camera",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }

            // Center Viewfinder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                GoogleStyleViewfinder(
                    modifier = Modifier.size(240.dp),
                )
            }

            // Bottom Control Area: Zoom Slider + Scanned by Links Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom Control Pill Slider
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ZoomOut,
                            contentDescription = "Zoom out",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { zoomRatio = 0f }
                        )

                        Slider(
                            value = zoomRatio,
                            onValueChange = { zoomRatio = it },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = softBlue,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        Icon(
                            imageVector = Icons.Outlined.ZoomIn,
                            contentDescription = "Zoom in",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { zoomRatio = 1f }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Footer ("Scanned by Links")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp),
                    )

                    Text(
                        text = "Scanned by Links",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )

                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanContent(
    softBlue: Color,
    borderGrey: Color,
    textMuted: Color,
    externalScannedResult: String? = null,
    onClearExternalScannedResult: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var scannedResult by remember { mutableStateOf("") }
    var resolvedDestination by remember { mutableStateOf("") }
    var resolvedSource by remember { mutableStateOf("") }
    var isResolving by remember { mutableStateOf(false) }
    var showFullScreenScanner by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(externalScannedResult) {
        if (!externalScannedResult.isNullOrEmpty()) {
            scannedResult = externalScannedResult
            resolvedDestination = ""
            resolvedSource = ""
            onClearExternalScannedResult()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val scanned = decodeQrFromUri(context, it)
            if (scanned != null) {
                scannedResult = scanned
                resolvedDestination = ""
                resolvedSource = ""
                ScanHistoryManager.addHistoryItem(context, scanned)
            } else {
                Toast.makeText(context, "No QR code found in selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val isResolved = resolvedDestination.isNotEmpty() && resolvedSource.isNotEmpty()

    if (showFullScreenScanner && cameraPermissionState.status.isGranted) {
        FullScreenScannerModal(
            onClose = { showFullScreenScanner = false },
            onQrCodeScanned = { result ->
                scannedResult = result
                resolvedDestination = ""
                resolvedSource = ""
                ScanHistoryManager.addHistoryItem(context, result)
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (scannedResult.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = null,
                    tint = softBlue,
                    modifier = Modifier.size(56.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Scan QR codes using camera or import from gallery",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!cameraPermissionState.status.isGranted) {
                            cameraPermissionState.launchPermissionRequest()
                        }
                        showFullScreenScanner = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(text = stringResource(R.string.scan_now), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).padding(end = 6.dp),
                    )
                    Text(text = stringResource(R.string.image), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            // Scanned Link Display
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderGrey),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.scanned_content),
                            color = softBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        IconButton(
                            onClick = {
                                scannedResult = ""
                                resolvedDestination = ""
                                resolvedSource = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Scan again",
                                tint = textMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = scannedResult,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isResolving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(top = 8.dp),
                                color = softBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(scannedResult))
                                    Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = softBlue, contentColor = Color.Black),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(text = stringResource(R.string.copy), fontSize = 14.sp)
                            }
                        }
                    }

                    if (isResolved) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = borderGrey)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Resolved Destination Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = softBlue,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.destination),
                                    color = softBlue,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = resolvedDestination,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(resolvedDestination))
                                    Toast.makeText(context, "Destination copied", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Destination",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    try {
        val filename = "QR_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Links")
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            Toast.makeText(context, "Saved QR code to Pictures/Links", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateContent(
    softBlue: Color,
    borderGrey: Color,
    textMuted: Color,
) {
    var qrInput by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(qrInput) {
        val trimmed = qrInput.trim()
        qrBitmap = if (trimmed.isNotEmpty()) {
            QrGenerator.generate(trimmed, size = 512)
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Text Input Field
        OutlinedTextField(
            value = qrInput,
            onValueChange = { qrInput = it },
            label = { Text(text = stringResource(R.string.generate_qr_for)) },
            placeholder = { Text(text = stringResource(R.string.enter_text_or_url)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() },
            ),
            trailingIcon = {
                if (qrInput.isNotEmpty()) {
                    IconButton(onClick = { qrInput = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = softBlue,
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            val pasted = clipboardManager.getText()?.text
                            if (!pasted.isNullOrBlank()) qrInput = pasted
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = "Paste",
                            tint = softBlue,
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = softBlue,
                unfocusedBorderColor = borderGrey,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = softBlue,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display QR Code Box
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(if (qrBitmap != null) Color.White else MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(16.dp))
                .border(1.dp, borderGrey, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val bitmap = qrBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        tint = softBlue,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.enter_text_above_to_generate),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                    )
                }
            }
        }

        // Save QR Code Button — always visible, disabled when no content
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (qrBitmap != null) saveBitmapToGallery(context, qrBitmap!!)
            },
            enabled = qrBitmap != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.35f),
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = null,
                modifier = Modifier.size(20.dp).padding(end = 6.dp),
            )
            Text(text = stringResource(R.string.save_qr_code), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
