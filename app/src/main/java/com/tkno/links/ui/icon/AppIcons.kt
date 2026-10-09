package com.tkno.links.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val LocalFireDepartment: ImageVector
    get() {
        if (_localFireDepartment != null) {
            return _localFireDepartment!!
        }
        _localFireDepartment = ImageVector.Builder(
            name = "local_fire_department",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(6f, 14f)
                quadToRelative(0f, 1.3f, 0.53f, 2.46f)
                reflectiveQuadToRelative(1.5f, 2.04f)
                quadTo(8f, 18.38f, 8f, 18.27f)
                quadToRelative(0f, -0.1f, 0f, -0.22f)
                quadToRelative(0f, -0.8f, 0.3f, -1.5f)
                reflectiveQuadTo(9.18f, 15.28f)
                lineTo(12f, 12.5f)
                lineToRelative(2.83f, 2.78f)
                quadToRelative(0.57f, 0.57f, 0.88f, 1.28f)
                reflectiveQuadToRelative(0.3f, 1.5f)
                quadToRelative(0f, 0.13f, 0f, 0.22f)
                quadToRelative(0f, 0.1f, -0.02f, 0.23f)
                quadToRelative(0.97f, -0.88f, 1.5f, -2.04f)
                reflectiveQuadTo(18f, 14f)
                quadToRelative(0f, -1.25f, -0.46f, -2.36f)
                reflectiveQuadTo(16.2f, 9.65f)
                quadToRelative(-0.5f, 0.33f, -1.05f, 0.49f)
                reflectiveQuadTo(14.03f, 10.3f)
                quadToRelative(-1.55f, 0f, -2.69f, -1.03f)
                reflectiveQuadTo(10.03f, 6.75f)
                quadTo(9.05f, 7.57f, 8.3f, 8.46f)
                reflectiveQuadToRelative(-1.26f, 1.8f)
                reflectiveQuadTo(6.26f, 12.13f)
                reflectiveQuadTo(6f, 14f)
                close()
                moveToRelative(6f, 1.3f)
                lineToRelative(-1.42f, 1.4f)
                quadToRelative(-0.28f, 0.28f, -0.43f, 0.63f)
                quadTo(10f, 17.68f, 10f, 18.05f)
                quadToRelative(0f, 0.8f, 0.59f, 1.38f)
                reflectiveQuadTo(12f, 20f)
                reflectiveQuadToRelative(1.41f, -0.57f)
                reflectiveQuadTo(14f, 18.05f)
                quadToRelative(0f, -0.4f, -0.15f, -0.74f)
                reflectiveQuadTo(13.43f, 16.7f)
                lineTo(12f, 15.3f)
                close()
                moveTo(12f, 3f)
                verticalLineTo(6.3f)
                quadToRelative(0f, 0.85f, 0.59f, 1.42f)
                reflectiveQuadTo(14.03f, 8.3f)
                quadToRelative(0.45f, 0f, 0.84f, -0.19f)
                quadTo(15.25f, 7.93f, 15.55f, 7.55f)
                lineTo(16f, 7f)
                quadToRelative(1.85f, 1.05f, 2.93f, 2.92f)
                reflectiveQuadTo(20f, 14f)
                quadToRelative(0f, 3.35f, -2.32f, 5.68f)
                reflectiveQuadTo(12f, 22f)
                reflectiveQuadTo(6.33f, 19.68f)
                reflectiveQuadTo(4f, 14f)
                quadTo(4f, 10.77f, 6.16f, 7.88f)
                quadTo(8.33f, 4.97f, 12f, 3f)
                close()
            }
        }.build()
        return _localFireDepartment!!
    }

private var _localFireDepartment: ImageVector? = null

public val Policy: ImageVector
    get() {
        if (_policy != null) {
            return _policy!!
        }
        _policy = ImageVector.Builder(
            name = "policy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 22f)
                quadTo(8.53f, 21.13f, 6.26f, 18.01f)
                reflectiveQuadTo(4f, 11.1f)
                verticalLineTo(5f)
                lineTo(12f, 2f)
                lineToRelative(8f, 3f)
                verticalLineToRelative(6.1f)
                quadToRelative(0f, 2.13f, -0.72f, 4.09f)
                reflectiveQuadTo(17.2f, 18.65f)
                lineTo(14f, 15.45f)
                quadToRelative(-0.45f, 0.28f, -0.96f, 0.41f)
                reflectiveQuadTo(12f, 16f)
                quadTo(10.35f, 16f, 9.18f, 14.83f)
                reflectiveQuadTo(8f, 12f)
                reflectiveQuadTo(9.18f, 9.17f)
                reflectiveQuadTo(12f, 8f)
                reflectiveQuadToRelative(2.83f, 1.17f)
                reflectiveQuadTo(16f, 12f)
                quadToRelative(0f, 0.55f, -0.14f, 1.06f)
                reflectiveQuadToRelative(-0.41f, 0.99f)
                lineToRelative(1.5f, 1.5f)
                quadToRelative(0.5f, -1.02f, 0.78f, -2.15f)
                reflectiveQuadTo(18f, 11.1f)
                verticalLineTo(6.38f)
                lineTo(12f, 4.13f)
                lineTo(6f, 6.38f)
                verticalLineTo(11.1f)
                quadToRelative(0f, 3.03f, 1.7f, 5.5f)
                reflectiveQuadTo(12f, 19.9f)
                quadToRelative(0.65f, -0.2f, 1.24f, -0.51f)
                reflectiveQuadTo(14.4f, 18.65f)
                lineToRelative(1.4f, 1.4f)
                quadToRelative(-0.82f, 0.68f, -1.79f, 1.18f)
                reflectiveQuadTo(12f, 22f)
                close()
                moveToRelative(1.41f, -8.59f)
                quadTo(14f, 12.83f, 14f, 12f)
                reflectiveQuadTo(13.41f, 10.59f)
                reflectiveQuadTo(12f, 10f)
                reflectiveQuadToRelative(-1.41f, 0.59f)
                quadTo(10f, 11.18f, 10f, 12f)
                reflectiveQuadToRelative(0.59f, 1.41f)
                reflectiveQuadTo(12f, 14f)
                reflectiveQuadToRelative(1.41f, -0.59f)
                close()
                moveTo(12.2f, 12.08f)
                close()
            }
        }.build()
        return _policy!!
    }

private var _policy: ImageVector? = null

public val StarIcon: ImageVector
    get() {
        if (_starIcon != null) {
            return _starIcon!!
        }
        _starIcon = ImageVector.Builder(
            name = "star",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(8.85f, 16.83f)
                lineTo(12f, 14.93f)
                lineToRelative(3.15f, 1.93f)
                lineToRelative(-0.82f, -3.6f)
                lineToRelative(2.78f, -2.4f)
                lineTo(13.45f, 10.52f)
                lineTo(12f, 7.13f)
                lineTo(10.55f, 10.5f)
                lineTo(6.9f, 10.83f)
                lineToRelative(2.78f, 2.43f)
                lineTo(8.85f, 16.83f)
                close()
                moveTo(5.83f, 21f)
                lineTo(7.45f, 13.98f)
                lineTo(2f, 9.25f)
                lineTo(9.2f, 8.63f)
                lineTo(12f, 2f)
                lineToRelative(2.8f, 6.63f)
                lineTo(22f, 9.25f)
                lineToRelative(-5.45f, 4.72f)
                lineTo(18.18f, 21f)
                lineTo(12f, 17.27f)
                lineTo(5.83f, 21f)
                close()
                moveTo(12f, 12.25f)
                close()
            }
        }.build()
        return _starIcon!!
    }

private var _starIcon: ImageVector? = null

public val Dashboard2: ImageVector
    get() {
        if (_dashboard2 != null) {
            return _dashboard2!!
        }
        _dashboard2 = ImageVector.Builder(
            name = "dashboard_2",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(15f, 20f)
                verticalLineTo(13f)
                horizontalLineToRelative(7f)
                verticalLineToRelative(7f)
                horizontalLineTo(15f)
                close()
                moveTo(11f, 11f)
                verticalLineTo(4f)
                horizontalLineTo(22f)
                verticalLineToRelative(7f)
                horizontalLineTo(11f)
                close()
                moveTo(2f, 20f)
                verticalLineTo(13f)
                horizontalLineTo(13f)
                verticalLineToRelative(7f)
                horizontalLineTo(2f)
                close()
                moveTo(2f, 11f)
                verticalLineTo(4f)
                horizontalLineTo(9f)
                verticalLineToRelative(7f)
                horizontalLineTo(2f)
                close()
                moveTo(13f, 9f)
                horizontalLineToRelative(7f)
                verticalLineTo(6f)
                horizontalLineTo(13f)
                verticalLineTo(9f)
                close()
                moveTo(4f, 18f)
                horizontalLineToRelative(7f)
                verticalLineTo(15f)
                horizontalLineTo(4f)
                verticalLineToRelative(3f)
                close()
                moveToRelative(13f, 0f)
                horizontalLineToRelative(3f)
                verticalLineTo(15f)
                horizontalLineTo(17f)
                verticalLineToRelative(3f)
                close()
                moveTo(4f, 9f)
                horizontalLineTo(7f)
                verticalLineTo(6f)
                horizontalLineTo(4f)
                verticalLineTo(9f)
                close()
            }
        }.build()
        return _dashboard2!!
    }

private var _dashboard2: ImageVector? = null

public val PictureAsPdf: ImageVector
    get() {
        if (_pictureAsPdf != null) {
            return _pictureAsPdf!!
        }
        _pictureAsPdf = ImageVector.Builder(
            name = "picture_as_pdf",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(9f, 12.5f)
                horizontalLineToRelative(1f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(1f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                reflectiveQuadTo(12f, 9.5f)
                verticalLineToRelative(-1f)
                quadTo(12f, 8.07f, 11.71f, 7.79f)
                reflectiveQuadTo(11f, 7.5f)
                horizontalLineTo(9f)
                verticalLineToRelative(5f)
                close()
                moveToRelative(1f, -3f)
                verticalLineToRelative(-1f)
                horizontalLineToRelative(1f)
                verticalLineToRelative(1f)
                horizontalLineTo(10f)
                close()
                moveToRelative(3f, 3f)
                horizontalLineToRelative(2f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                quadTo(16f, 11.93f, 16f, 11.5f)
                verticalLineToRelative(-3f)
                quadTo(16f, 8.07f, 15.71f, 7.79f)
                reflectiveQuadTo(15f, 7.5f)
                horizontalLineTo(13f)
                verticalLineToRelative(5f)
                close()
                moveToRelative(1f, -1f)
                verticalLineToRelative(-3f)
                horizontalLineToRelative(1f)
                verticalLineToRelative(3f)
                horizontalLineTo(14f)
                close()
                moveToRelative(3f, 1f)
                horizontalLineToRelative(1f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(1f)
                verticalLineToRelative(-1f)
                horizontalLineTo(18f)
                verticalLineToRelative(-1f)
                horizontalLineToRelative(1f)
                verticalLineToRelative(-1f)
                horizontalLineTo(17f)
                verticalLineToRelative(5f)
                close()
                moveTo(8f, 18f)
                quadTo(7.18f, 18f, 6.59f, 17.41f)
                reflectiveQuadTo(6f, 16f)
                verticalLineTo(4f)
                quadTo(6f, 3.17f, 6.59f, 2.59f)
                reflectiveQuadTo(8f, 2f)
                horizontalLineTo(20f)
                quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                reflectiveQuadTo(22f, 4f)
                verticalLineTo(16f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(20f, 18f)
                horizontalLineTo(8f)
                close()
                moveTo(8f, 16f)
                horizontalLineTo(20f)
                verticalLineTo(4f)
                horizontalLineTo(8f)
                verticalLineTo(16f)
                close()
                moveTo(4f, 22f)
                quadTo(3.18f, 22f, 2.59f, 21.41f)
                reflectiveQuadTo(2f, 20f)
                verticalLineTo(6f)
                horizontalLineTo(4f)
                verticalLineTo(20f)
                horizontalLineTo(18f)
                verticalLineToRelative(2f)
                horizontalLineTo(4f)
                close()
                moveTo(8f, 4f)
                verticalLineTo(16f)
                verticalLineTo(4f)
                close()
            }
        }.build()
        return _pictureAsPdf!!
    }

private var _pictureAsPdf: ImageVector? = null

public val CenterFocusWeak: ImageVector
    get() {
        if (_centerFocusWeak != null) {
            return _centerFocusWeak!!
        }
        _centerFocusWeak = ImageVector.Builder(
            name = "center_focus_weak",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(9.18f, 14.83f)
                quadTo(8f, 13.65f, 8f, 12f)
                reflectiveQuadTo(9.18f, 9.17f)
                reflectiveQuadTo(12f, 8f)
                reflectiveQuadToRelative(2.83f, 1.17f)
                reflectiveQuadTo(16f, 12f)
                reflectiveQuadToRelative(-1.17f, 2.82f)
                reflectiveQuadTo(12f, 16f)
                reflectiveQuadTo(9.18f, 14.83f)
                close()
                moveToRelative(4.24f, -1.41f)
                quadTo(14f, 12.83f, 14f, 12f)
                reflectiveQuadTo(13.41f, 10.59f)
                reflectiveQuadTo(12f, 10f)
                reflectiveQuadToRelative(-1.41f, 0.59f)
                quadTo(10f, 11.18f, 10f, 12f)
                reflectiveQuadToRelative(0.59f, 1.41f)
                reflectiveQuadTo(12f, 14f)
                reflectiveQuadToRelative(1.41f, -0.59f)
                close()
                moveTo(12f, 12f)
                close()
                moveTo(5f, 21f)
                quadTo(4.18f, 21f, 3.59f, 20.41f)
                reflectiveQuadTo(3f, 19f)
                verticalLineTo(15f)
                horizontalLineTo(5f)
                verticalLineToRelative(4f)
                horizontalLineTo(9f)
                verticalLineToRelative(2f)
                horizontalLineTo(5f)
                close()
                moveToRelative(10f, 0f)
                verticalLineTo(19f)
                horizontalLineToRelative(4f)
                verticalLineTo(15f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(4f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(19f, 21f)
                horizontalLineTo(15f)
                close()
                moveTo(3f, 9f)
                verticalLineTo(5f)
                quadTo(3f, 4.17f, 3.59f, 3.59f)
                reflectiveQuadTo(5f, 3f)
                horizontalLineTo(9f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineTo(9f)
                horizontalLineTo(3f)
                close()
                moveTo(19f, 9f)
                verticalLineTo(5f)
                horizontalLineTo(15f)
                verticalLineTo(3f)
                horizontalLineToRelative(4f)
                quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                reflectiveQuadTo(21f, 5f)
                verticalLineTo(9f)
                horizontalLineTo(19f)
                close()
            }
        }.build()
        return _centerFocusWeak!!
    }

private var _centerFocusWeak: ImageVector? = null

public val QrCodeIcon: ImageVector
    get() {
        if (_qrCodeIcon != null) {
            return _qrCodeIcon!!
        }
        _qrCodeIcon = ImageVector.Builder(
            name = "qr_code",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(3f, 11f)
                verticalLineTo(3f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(8f)
                horizontalLineTo(3f)
                close()
                moveTo(5f, 9f)
                horizontalLineTo(9f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineTo(9f)
                close()
                moveTo(3f, 21f)
                verticalLineTo(13f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(8f)
                horizontalLineTo(3f)
                close()
                moveTo(5f, 19f)
                horizontalLineTo(9f)
                verticalLineTo(15f)
                horizontalLineTo(5f)
                verticalLineToRelative(4f)
                close()
                moveToRelative(8f, -8f)
                verticalLineTo(3f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(8f)
                horizontalLineTo(13f)
                close()
                moveTo(15f, 9f)
                horizontalLineToRelative(4f)
                verticalLineTo(5f)
                horizontalLineTo(15f)
                verticalLineTo(9f)
                close()
                moveToRelative(4f, 12f)
                verticalLineTo(19f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineTo(19f)
                close()
                moveTo(13f, 15f)
                verticalLineTo(13f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineTo(13f)
                close()
                moveToRelative(2f, 2f)
                verticalLineTo(15f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineTo(15f)
                close()
                moveToRelative(-2f, 2f)
                verticalLineTo(17f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineTo(13f)
                close()
                moveToRelative(2f, 2f)
                verticalLineTo(19f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineTo(15f)
                close()
                moveToRelative(2f, -2f)
                verticalLineTo(17f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineTo(17f)
                close()
                moveToRelative(0f, -4f)
                verticalLineTo(13f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineTo(17f)
                close()
                moveToRelative(2f, 2f)
                verticalLineTo(15f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineTo(19f)
                close()
            }
        }.build()
        return _qrCodeIcon!!
    }

private var _qrCodeIcon: ImageVector? = null

public val Report: ImageVector
    get() {
        if (_report != null) {
            return _report!!
        }
        _report = ImageVector.Builder(
            name = "report",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 17f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                quadTo(13f, 16.43f, 13f, 16f)
                reflectiveQuadTo(12.71f, 15.29f)
                reflectiveQuadTo(12f, 15f)
                reflectiveQuadToRelative(-0.71f, 0.29f)
                reflectiveQuadTo(11f, 16f)
                reflectiveQuadToRelative(0.29f, 0.71f)
                reflectiveQuadTo(12f, 17f)
                close()
                moveTo(11f, 13f)
                horizontalLineToRelative(2f)
                verticalLineTo(7f)
                horizontalLineTo(11f)
                verticalLineToRelative(6f)
                close()
                moveTo(8.25f, 21f)
                lineTo(3f, 15.75f)
                verticalLineTo(8.25f)
                lineTo(8.25f, 3f)
                horizontalLineToRelative(7.5f)
                lineTo(21f, 8.25f)
                verticalLineToRelative(7.5f)
                lineTo(15.75f, 21f)
                horizontalLineTo(8.25f)
                close()
                moveTo(9.1f, 19f)
                horizontalLineToRelative(5.8f)
                lineTo(19f, 14.9f)
                verticalLineTo(9.1f)
                lineTo(14.9f, 5f)
                horizontalLineTo(9.1f)
                lineTo(5f, 9.1f)
                verticalLineToRelative(5.8f)
                lineTo(9.1f, 19f)
                close()
                moveTo(12f, 12f)
                close()
            }
        }.build()
        return _report!!
    }

private var _report: ImageVector? = null

public val Delete: ImageVector
    get() {
        if (_delete != null) {
            return _delete!!
        }
        _delete = ImageVector.Builder(
            name = "delete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(7f, 21f)
                quadTo(6.18f, 21f, 5.59f, 20.41f)
                reflectiveQuadTo(5f, 19f)
                verticalLineTo(6f)
                horizontalLineTo(4f)
                verticalLineTo(4f)
                horizontalLineTo(9f)
                verticalLineTo(3f)
                horizontalLineToRelative(6f)
                verticalLineTo(4f)
                horizontalLineToRelative(5f)
                verticalLineTo(6f)
                horizontalLineTo(19f)
                verticalLineTo(19f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(17f, 21f)
                horizontalLineTo(7f)
                close()
                moveTo(17f, 6f)
                horizontalLineTo(7f)
                verticalLineTo(19f)
                horizontalLineTo(17f)
                verticalLineTo(6f)
                close()
                moveTo(9f, 17f)
                horizontalLineToRelative(2f)
                verticalLineTo(8f)
                horizontalLineTo(9f)
                verticalLineToRelative(9f)
                close()
                moveToRelative(4f, 0f)
                horizontalLineToRelative(2f)
                verticalLineTo(8f)
                horizontalLineTo(13f)
                verticalLineToRelative(9f)
                close()
                moveTo(7f, 6f)
                verticalLineTo(19f)
                verticalLineTo(6f)
                close()
            }
        }.build()
        return _delete!!
    }

private var _delete: ImageVector? = null

public val StarShine: ImageVector
    get() {
        if (_starShine != null) {
            return _starShine!!
        }
        _starShine = ImageVector.Builder(
            name = "star_shine",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(21.3f, 18.7f)
                lineToRelative(-3f, -3f)
                lineToRelative(1.4f, -1.4f)
                lineToRelative(3f, 3f)
                lineToRelative(-1.4f, 1.4f)
                close()
                moveTo(17.7f, 6.7f)
                lineTo(16.3f, 5.3f)
                lineToRelative(3f, -3f)
                lineToRelative(1.4f, 1.4f)
                lineToRelative(-3f, 3f)
                close()
                moveTo(6.3f, 6.7f)
                lineToRelative(-3f, -3f)
                lineTo(4.7f, 2.3f)
                lineToRelative(3f, 3f)
                lineTo(6.3f, 6.7f)
                close()
                moveToRelative(-3.6f, 12f)
                lineTo(1.3f, 17.3f)
                lineToRelative(3f, -3f)
                lineToRelative(1.4f, 1.4f)
                lineToRelative(-3f, 3f)
                close()
                moveTo(8.85f, 16.83f)
                lineTo(12f, 14.93f)
                lineToRelative(3.15f, 1.93f)
                lineToRelative(-0.82f, -3.6f)
                lineToRelative(2.78f, -2.4f)
                lineTo(13.45f, 10.52f)
                lineTo(12f, 7.13f)
                lineTo(10.55f, 10.5f)
                lineTo(6.9f, 10.83f)
                lineToRelative(2.78f, 2.43f)
                lineTo(8.85f, 16.83f)
                close()
                moveTo(5.83f, 21f)
                lineTo(7.45f, 13.98f)
                lineTo(2f, 9.25f)
                lineTo(9.2f, 8.63f)
                lineTo(12f, 2f)
                lineToRelative(2.8f, 6.63f)
                lineTo(22f, 9.25f)
                lineToRelative(-5.45f, 4.72f)
                lineTo(18.18f, 21f)
                lineTo(12f, 17.27f)
                lineTo(5.83f, 21f)
                close()
                moveTo(12f, 11.98f)
                close()
            }
        }.build()
        return _starShine!!
    }

private var _starShine: ImageVector? = null

public val star_shine: ImageVector
    get() = StarShine

public val ArrowDownward: ImageVector
    get() {
        if (_arrowDownward != null) {
            return _arrowDownward!!
        }
        _arrowDownward = ImageVector.Builder(
            name = "arrow_downward",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(11f, 4f)
                verticalLineTo(16.18f)
                lineTo(5.4f, 10.58f)
                lineTo(4f, 12f)
                lineToRelative(8f, 8f)
                lineToRelative(8f, -8f)
                lineTo(18.6f, 10.58f)
                lineTo(13f, 16.18f)
                verticalLineTo(4f)
                horizontalLineTo(11f)
                close()
            }
        }.build()
        return _arrowDownward!!
    }

private var _arrowDownward: ImageVector? = null

public val arrow_downward: ImageVector
    get() = ArrowDownward


