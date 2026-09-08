package com.tkno.links

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap
import java.util.concurrent.Executors

@Composable
fun QrScannerView(
    onQrCodeScanned: (String) -> Unit,
    isTorchEnabled: Boolean = false,
    cameraLensFacing: Int = CameraSelector.LENS_FACING_BACK,
    zoomRatio: Float = 0f,
    modifier: Modifier = Modifier
) {
    val currentOnQrCodeScanned by rememberUpdatedState(onQrCodeScanned)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProviderState by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var currentBoundLensFacing by remember { mutableIntStateOf(-1) }

    var viewWidth by remember { mutableIntStateOf(0) }
    var viewHeight by remember { mutableIntStateOf(0) }
    val boxSizePx = remember(context) { context.resources.displayMetrics.density * 240f }

    // Manage scanner state to avoid repeating scans of the same code too quickly
    var lastScannedText by remember { mutableStateOf("") }
    var lastScanTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isTorchEnabled, cameraControl) {
        try {
            cameraControl?.enableTorch(isTorchEnabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(zoomRatio, cameraControl) {
        try {
            cameraControl?.setLinearZoom(zoomRatio)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraControl?.enableTorch(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                cameraProviderState?.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
                    val w = right - left
                    val h = bottom - top
                    if (w > 0 && h > 0) {
                        viewWidth = w
                        viewHeight = h
                    }
                }
            }

            // Tap to focus listener
            previewView.setOnTouchListener { view, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(event.x, event.y)
                    val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                        .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                    cameraControl?.startFocusAndMetering(action)
                    view.performClick()
                    true
                } else {
                    false
                }
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderState = cameraProvider

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor,
                    QrCodeAnalyzer(
                        boxSizePx = boxSizePx,
                        getViewWidth = { viewWidth },
                        getViewHeight = { viewHeight }
                    ) { result ->
                        val now = System.currentTimeMillis()
                        if (result != lastScannedText || now - lastScanTime > 3000) {
                            lastScannedText = result
                            lastScanTime = now
                            currentOnQrCodeScanned(result)
                        }
                    }
                )

                val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraLensFacing).build()

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    cameraControl = camera.cameraControl
                    cameraControl?.setLinearZoom(zoomRatio)
                    currentBoundLensFacing = cameraLensFacing
                } catch (exc: Exception) {
                    Log.e("QrScannerView", "Use case binding failed", exc)
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        update = { previewView ->
            val cameraProvider = cameraProviderState ?: return@AndroidView

            if (currentBoundLensFacing != cameraLensFacing) {
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor,
                    QrCodeAnalyzer(
                        boxSizePx = boxSizePx,
                        getViewWidth = { viewWidth },
                        getViewHeight = { viewHeight }
                    ) { result ->
                        val now = System.currentTimeMillis()
                        if (result != lastScannedText || now - lastScanTime > 3000) {
                            lastScannedText = result
                            lastScanTime = now
                            currentOnQrCodeScanned(result)
                        }
                    }
                )

                val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraLensFacing).build()

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    cameraControl = camera.cameraControl
                    cameraControl?.setLinearZoom(zoomRatio)
                    currentBoundLensFacing = cameraLensFacing
                } catch (exc: Exception) {
                    Log.e("QrScannerView", "Use case binding failed", exc)
                }
            } else {
                cameraControl?.setLinearZoom(zoomRatio)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

private class QrCodeAnalyzer(
    private val boxSizePx: Float,
    private val getViewWidth: () -> Int,
    private val getViewHeight: () -> Int,
    private val onQrCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(BarcodeFormat.QR_CODE)
        setHints(hints)
    }

    override fun analyze(image: ImageProxy) {
        val planes = image.planes
        if (planes.isEmpty()) {
            image.close()
            return
        }

        val buffer = planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val imgWidth = image.width
        val imgHeight = image.height
        val rotation = image.imageInfo.rotationDegrees

        val viewW = getViewWidth()
        val viewH = getViewHeight()

        val source: PlanarYUVLuminanceSource = if (viewW > 0 && viewH > 0 && boxSizePx > 0) {
            val isRotated = (rotation == 90 || rotation == 270)
            val portraitCamW = if (isRotated) imgHeight else imgWidth
            val portraitCamH = if (isRotated) imgWidth else imgHeight

            // Scale for PreviewView.ScaleType.FILL_CENTER
            val scale = maxOf(viewW.toFloat() / portraitCamW, viewH.toFloat() / portraitCamH)

            // Visible camera region in portrait coordinates
            val visibleCamW = viewW / scale
            val visibleCamH = viewH / scale
            val camVisibleLeft = (portraitCamW - visibleCamW) / 2f
            val camVisibleTop = (portraitCamH - visibleCamH) / 2f

            // Viewfinder box in screen center
            val boxLeftScreen = (viewW - boxSizePx) / 2f
            val boxTopScreen = (viewH - boxSizePx) / 2f

            // Map box to portrait camera coordinates
            val boxLeftCam = (camVisibleLeft + (boxLeftScreen / scale)).coerceIn(0f, portraitCamW - 1f)
            val boxTopCam = (camVisibleTop + (boxTopScreen / scale)).coerceIn(0f, portraitCamH - 1f)
            val boxWCam = (boxSizePx / scale).coerceIn(1f, portraitCamW - boxLeftCam)
            val boxHCam = (boxSizePx / scale).coerceIn(1f, portraitCamH - boxTopCam)

            // Map portrait camera coordinates to raw buffer coordinates based on rotation
            val rawLeft: Int
            val rawTop: Int
            val rawCropW: Int
            val rawCropH: Int

            when (rotation) {
                90 -> {
                    rawLeft = (imgWidth - (boxTopCam + boxHCam)).toInt().coerceIn(0, imgWidth - 1)
                    rawTop = boxLeftCam.toInt().coerceIn(0, imgHeight - 1)
                    rawCropW = boxHCam.toInt().coerceIn(1, imgWidth - rawLeft)
                    rawCropH = boxWCam.toInt().coerceIn(1, imgHeight - rawTop)
                }
                270 -> {
                    rawLeft = boxTopCam.toInt().coerceIn(0, imgWidth - 1)
                    rawTop = (imgHeight - (boxLeftCam + boxWCam)).toInt().coerceIn(0, imgHeight - 1)
                    rawCropW = boxHCam.toInt().coerceIn(1, imgWidth - rawLeft)
                    rawCropH = boxWCam.toInt().coerceIn(1, imgHeight - rawTop)
                }
                180 -> {
                    rawLeft = (imgWidth - (boxLeftCam + boxWCam)).toInt().coerceIn(0, imgWidth - 1)
                    rawTop = (imgHeight - (boxTopCam + boxHCam)).toInt().coerceIn(0, imgHeight - 1)
                    rawCropW = boxWCam.toInt().coerceIn(1, imgWidth - rawLeft)
                    rawCropH = boxHCam.toInt().coerceIn(1, imgHeight - rawTop)
                }
                else -> { // 0
                    rawLeft = boxLeftCam.toInt().coerceIn(0, imgWidth - 1)
                    rawTop = boxTopCam.toInt().coerceIn(0, imgHeight - 1)
                    rawCropW = boxWCam.toInt().coerceIn(1, imgWidth - rawLeft)
                    rawCropH = boxHCam.toInt().coerceIn(1, imgHeight - rawTop)
                }
            }

            PlanarYUVLuminanceSource(
                data, imgWidth, imgHeight,
                rawLeft, rawTop, rawCropW, rawCropH,
                false
            )
        } else {
            // Fallback to center 50% crop if view dimensions not measured yet
            val cropW = (imgWidth * 0.5f).toInt()
            val cropH = (imgHeight * 0.5f).toInt()
            val left = (imgWidth - cropW) / 2
            val top = (imgHeight - cropH) / 2
            PlanarYUVLuminanceSource(
                data, imgWidth, imgHeight,
                left, top, cropW, cropH,
                false
            )
        }

        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decodeWithState(bitmap)
            onQrCodeDetected(result.text)
        } catch (e: ReaderException) {
            // No QR code found in cropped box
        } finally {
            reader.reset()
            image.close()
        }
    }
}
