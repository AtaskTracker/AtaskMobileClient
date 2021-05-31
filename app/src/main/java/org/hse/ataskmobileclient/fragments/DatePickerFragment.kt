package org.hse.ataskmobileclient.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(private val onFinishListener: (date : Date?) -> Unit)
    : DialogFragment(), DatePickerDialog.OnDateSetListener
{

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(requireActivity(), this, year, month, day)
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Clear") { _, _ ->
            onFinishListener(null)
            dismiss()
        }

        return dialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val selectedDate = cal.time

        onFinishListener(selectedDate)
        dismiss()
    }
}