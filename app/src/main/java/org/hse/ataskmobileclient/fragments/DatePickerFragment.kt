package org.hse.ataskmobileclient.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import org.hse.ataskmobileclient.R
import java.util.*

class DatePickerFragment(private val initialDate: Date? = null,
                         private val onFinishListener: (date : Date?) -> Unit)
    : DialogFragment(), DatePickerDialog.OnDateSetListener
{

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        if (initialDate != null)
            calendar.time = initialDate

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(requireActivity(), this, year, month, day)
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.clear_datetime)) { _, _ ->
            onFinishListener(null)
            dismiss()
        }

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), dialog)
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_option), dialog)

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