package org.hse.ataskmobileclient.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import org.hse.ataskmobileclient.MockData
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.databinding.ActivityEditTaskBinding
import org.hse.ataskmobileclient.itemadapters.OnItemRemoveClick
import org.hse.ataskmobileclient.itemadapters.TaskMembersAdapter
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.viewmodels.EditTaskViewModel
import java.util.*


class EditTaskActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(EditTaskViewModel::class.java) }

    private val binding : ActivityEditTaskBinding by lazy {
        val binding : ActivityEditTaskBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_edit_task)

        binding.lifecycleOwner = this@EditTaskActivity
        binding.viewmodel = viewModel
        binding
    }

    private lateinit var taskMembersAdapter : TaskMembersAdapter
    private var oldTask : Task? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskJson = intent.getStringExtra(TASK_JSON)
        if (taskJson != null) {
            oldTask = gson.fromJson(taskJson, Task::class.java)
            binding.viewmodel!!.initializeFromTask(oldTask!!)
        }

        val myCalendar = Calendar.getInstance()
        val onDateSetListener =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewModel.dueDate.value = myCalendar.time
            }

        viewModel.pickDateClickedEvent.observe(this,
            {
                val calendar = Calendar.getInstance()
                if (viewModel.dueDate.value != null) {
                    calendar.time = viewModel.dueDate.value!!
                }

                DatePickerDialog(
                    this@EditTaskActivity, onDateSetListener,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            })

        taskMembersAdapter = TaskMembersAdapter(
            object : OnItemRemoveClick {
                override fun onClick(position: Int) {
                    viewModel.removeMemberAt(position)
                }
            })

        viewModel.members.observe(this, {
            it?.let {
                taskMembersAdapter.submitList(ArrayList(it))
            }
        })

        binding.taskMembersList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.taskMembersList.adapter = taskMembersAdapter

        binding.backButton.setOnClickListener { finishEditingWithoutSaving() }
        binding.btnSave.setOnClickListener { finishEditingWithSaving() }

        initAddMembersSpinner()
    }

    private fun initAddMembersSpinner() {
        val memberOptions = MockData.AvailableTaskMembers.map { it.username }
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            memberOptions)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.etMemberNameSpinner.adapter = adapter
        binding.etMemberNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.selectedAccount = null
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int, id: Long)
            {
                viewModel.selectedAccount = MockData.AvailableTaskMembers[position]
            }
        }
    }

    private fun finishEditingWithoutSaving() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun finishEditingWithSaving() {
        val task = viewModel.getEditedTask()
        val taskJson = gson.toJson(task)

        val intent = Intent().apply {
            putExtra(TASK_RESULT_JSON, taskJson)

        }
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val TASK_JSON = "TASK_JSON"
        const val TASK_RESULT_JSON = "TASK_RESULT_JSON"
    }
}
