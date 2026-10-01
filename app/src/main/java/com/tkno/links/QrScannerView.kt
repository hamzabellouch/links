package com.tkno.links

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap
import java.util.concurrent.Executors

data class QrBox(
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val rotationDegrees: Float = 0f
)

@Composable
fun QrScannerView(
    onQrCodeScanned: (result: String, box: QrBox) -> Unit,
    isTorchEnabled: Boolean = false,
    cameraLensFacing: Int = CameraSelector.LENS_FACING_BACK,
    zoomRatio: Float = 0f,
    modifier: Modifier = Modifier
) {
    val currentOnQrCodeScanned by rememberUpdatedState(onQrCodeScanned)
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProviderState by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var currentBoundLensFacing by remember { mutableIntStateOf(-1) }

    var viewWidth by remember { mutableIntStateOf(0) }
    var viewHeight by remember { mutableIntStateOf(0) }

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
                        isFrontCamera = { currentBoundLensFacing == CameraSelector.LENS_FACING_FRONT },
                        getViewWidth = { viewWidth },
                        getViewHeight = { viewHeight }
                    ) { result, box ->
                        mainHandler.post {
                            val now = System.currentTimeMillis()
                            if (result != lastScannedText || now - lastScanTime > 3000) {
                                lastScannedText = result
                                lastScanTime = now
                                currentOnQrCodeScanned(result, box)
                            }
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
                        isFrontCamera = { cameraLensFacing == CameraSelector.LENS_FACING_FRONT },
                        getViewWidth = { viewWidth },
                        getViewHeight = { viewHeight }
                    ) { result, box ->
                        mainHandler.post {
                            val now = System.currentTimeMillis()
                            if (result != lastScannedText || now - lastScanTime > 3000) {
                                lastScannedText = result
                                lastScanTime = now
                                currentOnQrCodeScanned(result, box)
                            }
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
    private val isFrontCamera: () -> Boolean,
    private val getViewWidth: () -> Int,
    private val getViewHeight: () -> Int,
    private val onQrCodeDetected: (String, QrBox) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(BarcodeFormat.QR_CODE)
        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
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

        val viewW = getViewWidth().toFloat()
        val viewH = getViewHeight().toFloat()

        // Full frame luminance source - enables detecting QR codes anywhere on the entire screen
        val source = PlanarYUVLuminanceSource(
            data, imgWidth, imgHeight,
            0, 0, imgWidth, imgHeight,
            false
        )

        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decodeWithState(bitmap)
            val text = result.text
            if (!text.isNullOrEmpty()) {
                val box = computeScreenQrBox(
                    points = result.resultPoints,
                    imgWidth = imgWidth,
                    imgHeight = imgHeight,
                    rotation = rotation,
                    isFront = isFrontCamera(),
                    viewW = viewW,
                    viewH = viewH
                )
                onQrCodeDetected(text, box)
            }
        } catch (e: ReaderException) {
            // No QR code detected in current frame
        } catch (e: Exception) {
            Log.e("QrScannerView", "Frame analysis error", e)
        } finally {
            reader.reset()
            image.close()
        }
    }

    private fun computeScreenQrBox(
        points: Array<ResultPoint>?,
        imgWidth: Int,
        imgHeight: Int,
        rotation: Int,
        isFront: Boolean,
        viewW: Float,
        viewH: Float
    ): QrBox {
        if (viewW <= 0f || viewH <= 0f) {
            return QrBox(0f, 0f, 0f, 0f, 0f)
        }

        val isRotated = (rotation == 90 || rotation == 270)
        val portraitCamW = if (isRotated) imgHeight.toFloat() else imgWidth.toFloat()
        val portraitCamH = if (isRotated) imgWidth.toFloat() else imgHeight.toFloat()

        val scale = maxOf(viewW / portraitCamW, viewH / portraitCamH)
        val scaledW = portraitCamW * scale
        val scaledH = portraitCamH * scale
        val offsetX = (viewW - scaledW) / 2f
        val offsetY = (viewH - scaledH) / 2f

        fun mapRawPoint(rawX: Float, rawY: Float): Offset {
            val xPort: Float
            val yPort: Float
            when (rotation) {
                90 -> {
                    xPort = imgHeight - rawY
                    yPort = rawX
                }
                270 -> {
                    xPort = rawY
                    yPort = imgWidth - rawX
                }
                180 -> {
                    xPort = imgWidth - rawX
                    yPort = imgHeight - rawY
                }
                else -> { // 0
                    xPort = rawX
                    yPort = rawY
                }
            }

            var screenX = xPort * scale + offsetX
            val screenY = yPort * scale + offsetY

            if (isFront) {
                screenX = viewW - screenX
            }
            return Offset(screenX, screenY)
        }

        if (points == null || points.size < 3) {
            val defSize = minOf(viewW, viewH) * 0.55f
            return QrBox(
                centerX = viewW / 2f,
                centerY = viewH / 2f,
                width = defSize,
                height = defSize,
                rotationDegrees = 0f
            )
        }

        // In ZXing: points[0] = Bottom-Left, points[1] = Top-Left, points[2] = Top-Right
        val pBL = mapRawPoint(points[0].x, points[0].y)
        val pTL = mapRawPoint(points[1].x, points[1].y)
        val pTR = mapRawPoint(points[2].x, points[2].y)

        // Vector along top edge (TL -> TR)
        val uX = pTR.x - pTL.x
        val uY = pTR.y - pTL.y
        val lenU = kotlin.math.hypot(uX.toDouble(), uY.toDouble()).toFloat()

        // Vector along left edge (TL -> BL)
        val vX = pBL.x - pTL.x
        val vY = pBL.y - pTL.y
        val lenV = kotlin.math.hypot(vX.toDouble(), vY.toDouble()).toFloat()

        // Center of the QR finder patterns
        val centerX = pTL.x + (uX + vX) / 2f
        val centerY = pTL.y + (uY + vY) / 2f

        // Expand size from finder patterns distance (~70-75% of full size) to outer QR code with padding
        val side = maxOf(lenU, lenV)
        val rawSize = side * 1.38f + 24f
        val qrSize = rawSize.coerceIn(80f, minOf(viewW, viewH) * 0.95f)

        // Angle of vector u (Top edge of QR code in screen coordinates)
        var angleDegrees = Math.toDegrees(kotlin.math.atan2(uY.toDouble(), uX.toDouble())).toFloat()
        if (angleDegrees > 180f) angleDegrees -= 360f
        if (angleDegrees < -180f) angleDegrees += 360f

        // Clamp center so it stays within visible screen bounds
        val half = qrSize / 2f
        val clampedCenterX = centerX.coerceIn(half, (viewW - half).coerceAtLeast(half))
        val clampedCenterY = centerY.coerceIn(half, (viewH - half).coerceAtLeast(half))

        return QrBox(
            centerX = clampedCenterX,
            centerY = clampedCenterY,
            width = qrSize,
            height = qrSize,
            rotationDegrees = angleDegrees
        )
    }
}
