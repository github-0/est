package com.example.evfunenhancer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.ui.theme.CommentAccent
import kotlinx.coroutines.delay

const val COMMENT_MAX_LENGTH = 100

// Strips C0 control characters (including newline/carriage-return/tab, which would
// otherwise break the single-line "truncated to one line" display) and Unicode
// bidi-control characters (which could be used to visually reverse/spoof the text).
// This is a client-side UX safeguard, not the security boundary — Firestore rules
// independently enforce type/length/no-newlines server-side.
private val DISALLOWED_CHARS_REGEX =
    Regex("[\\u0000-\\u001F\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]")

internal fun sanitizeComment(input: String): String =
    DISALLOWED_CHARS_REGEX.replace(input, "")

@Composable
fun CommentEntryDialog(
    countryName: String,
    initialText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    // Cursor starts at the end of any existing text (the plain-String TextField overload
    // would otherwise default it to position 0, which looked like the field couldn't be
    // scrolled to see the rest of a full-length comment).
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }
    val focusRequester = remember { FocusRequester() }
    // Filled in from inside the dialog window, whose focus and keyboard are separate from the
    // activity's. Closing the dialog with the field still focused left the keyboard "requested",
    // so Android re-showed it on every later window change (picker opening and closing).
    val keyboardCloser = remember { KeyboardCloser() }

    Dialog(onDismissRequest = { keyboardCloser.close(); onDismiss() }) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        keyboardCloser.close = { focusManager.clearFocus(); keyboardController?.hide() }
        val confirm = { keyboardCloser.close(); onConfirm(fieldValue.text.trim()) }

        LaunchedEffect(Unit) {
            if (initialText.isEmpty()) {
                // The dialog's own window needs a moment to attach before it can take focus.
                delay(100)
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(20.dp).width(280.dp)) {
                Text(
                    text = "${s.commentDialogTitle} — $countryName",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = { new ->
                        val sanitizedText = sanitizeComment(new.text)
                        // Reject the change outright once it would exceed the limit, rather
                        // than truncating — truncating always cuts from the end regardless of
                        // where the cursor is, which silently deletes characters elsewhere in
                        // the string when typing mid-text at the cap. Rejecting just refuses
                        // the keystroke, so the user has to delete before they can type more.
                        if (sanitizedText.length <= COMMENT_MAX_LENGTH) {
                            fieldValue = if (sanitizedText == new.text) {
                                new
                            } else {
                                val clamp = { v: Int -> v.coerceIn(0, sanitizedText.length) }
                                new.copy(
                                    text = sanitizedText,
                                    selection = TextRange(clamp(new.selection.start), clamp(new.selection.end))
                                )
                            }
                        }
                    },
                    // Wraps across multiple lines instead of a single scrolling line — a
                    // single-line field with cursor-following horizontal scroll turned out to
                    // be awkward to navigate once the text got close to the 100-char limit.
                    // The comment is still stored/displayed elsewhere as a single line; this
                    // wrapping is purely how it's edited.
                    minLines = 2,
                    maxLines = 4,
                    textStyle = LocalTextStyle.current.copy(fontStyle = FontStyle.Italic),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CommentAccent,
                        cursorColor = CommentAccent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { confirm() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${fieldValue.text.length}/$COMMENT_MAX_LENGTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (fieldValue.text.length >= COMMENT_MAX_LENGTH - 10)
                            MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    TextButton(onClick = confirm) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

private class KeyboardCloser {
    var close: () -> Unit = {}
}
