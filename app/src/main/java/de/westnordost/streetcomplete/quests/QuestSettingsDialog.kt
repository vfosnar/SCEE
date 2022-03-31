package de.westnordost.streetcomplete.quests

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import java.text.ParseException

fun singleTypeElementSelectionDialog(context: Context, prefs: SharedPreferences, pref: String, defaultValue: String, message: String): AlertDialog {
    var dialog: AlertDialog? = null
    val textInput = EditText(context)
    textInput.addTextChangedListener {
        val button = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        button?.isEnabled = textInput.text.toString().let {
            it.lowercase().matches("[a-z0-9_?,\\s]+".toRegex())
                && !it.trim().endsWith(',')
                && !it.contains(",,")
                && it.isNotEmpty() }
    }
    dialog = dialog(context, message, prefs.getString(pref, defaultValue)?.replace("|",", ") ?: "", textInput)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            prefs.edit().putString(pref, textInput.text.toString().split(",").joinToString("|") { it.trim() }).apply()
        }
        .setNeutralButton("reset") { _, _ ->
            prefs.edit().remove(pref).apply()
        }
        .create()
    return dialog
}

fun fullElementSelectionDialog(context: Context, prefs: SharedPreferences, pref: String, message: String): AlertDialog {
    var dialog: AlertDialog? = null
    val textInput = EditText(context)
    textInput.addTextChangedListener {
        val button = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        button?.isEnabled = textInput.text.toString().let {
            it.lowercase().matches("[a-z0-9_=!~()|<>\\s+-]+".toRegex())
            && it.count { c -> c == '('} == it.count { c -> c == ')'}
            && (it.contains('=') || it.contains('~'))
            && try {"nodes with $it".toElementFilterExpression()
                    true }
                catch(e: ParseException) {
                    Toast.makeText(context, "error: ${e.message}", Toast.LENGTH_LONG).show()
                    false
                }
        }
    }

    dialog = dialog(context, message, prefs.getString(pref, "") ?: "", textInput)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            prefs.edit().putString(pref, textInput.text.toString()).apply()
        }
        .setNeutralButton("reset") { _, _ ->
            prefs.edit().remove(pref).apply()
        }
        .create()
    return dialog
}

private fun dialog(context: Context, message: String, initialValue: String, input: EditText): AlertDialog.Builder {
    // need to set both InputTypes to work, https://developer.android.com/reference/android/widget/TextView#attr_android:inputType
    input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    input.setPaddingRelative(30,10,30,10)
    input.setText(initialValue)
    return AlertDialog.Builder(context)
        .setMessage(message)
        .setView(input)
        .setNegativeButton(android.R.string.cancel, null)
}

fun getStringFor(prefs: SharedPreferences, pref: String) = prefs.getString(pref, "")?.let { if (it.isEmpty()) "" else "or $it"}
