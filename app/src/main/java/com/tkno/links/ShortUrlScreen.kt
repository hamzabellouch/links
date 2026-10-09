package com.tkno.links

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortUrlScreen() {
    var urlInput by rememberSaveable { mutableStateOf("") }
    var resolvedDestination by rememberSaveable { mutableStateOf("") }
    var resolvedSource by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    val softBlue = MaterialTheme.colorScheme.primary
    val borderGrey = MaterialTheme.colorScheme.outlineVariant
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    val onSurface = MaterialTheme.colorScheme.onSurface

    val isResolved = resolvedDestination.isNotEmpty() && resolvedSource.isNotEmpty()

    fun handleResolve() {
        val trimmedInput = urlInput.trim()
        if (trimmedInput.isEmpty()) {
            errorMessage = context.getString(R.string.enter_url_error)
            return
        }

        if (!LinkResolver.isValidUrl(trimmedInput)) {
            errorMessage = context.getString(R.string.invalid_url_format)
            return
        }

        errorMessage = null
        keyboardController?.hide()
        focusManager.clearFocus()
        isLoading = true
        coroutineScope.launch {
            try {
                when (val result = LinkResolver.resolveDetailed(context, trimmedInput)) {
                    is LinkResolver.Result.Success -> {
                        resolvedDestination = result.destination
                        resolvedSource = result.source
                        errorMessage = null
                        focusManager.clearFocus()
                    }
                    is LinkResolver.Result.Error -> {
                        errorMessage = result.getLocalizedMessage(context)
                        resolvedDestination = ""
                        resolvedSource = ""
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: context.getString(R.string.unknown_error)
            } finally {
                isLoading = false
            }
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
                .padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 12.dp)
                    .height(48.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.short_url),
                    color = onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

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
                label = { Text(text = stringResource(R.string.short_url)) },
                placeholder = { Text(text = stringResource(R.string.url_placeholder)) },
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
                    onGo = { handleResolve() }
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
                                resolvedDestination = ""
                                resolvedSource = ""
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
                        IconButton(onClick = { handleResolve() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Unshorten",
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

            Spacer(modifier = Modifier.height(40.dp))

            // Source Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = softBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.source),
                        color = if (isResolved) softBlue else onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (isResolved) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = resolvedSource,
                            color = onSurface,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                if (isResolved) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(resolvedSource))
                            Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Source",
                            tint = onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Destination Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = softBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.destination),
                        color = if (isResolved) softBlue else onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (isResolved) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = resolvedDestination,
                            color = onSurface,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                if (isResolved) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(resolvedDestination))
                            Toast.makeText(context, "Destination URL copied", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Destination",
                            tint = onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Floating Action Button (FAB) for Paste in bottom right
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
}

