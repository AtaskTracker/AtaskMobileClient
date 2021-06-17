package org.hse.ataskmobileclient.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import org.hse.ataskmobileclient.EditTaskResult
import org.hse.ataskmobileclient.EditTaskStatusCode
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.databinding.ActivityMainBinding
import org.hse.ataskmobileclient.fragments.DatePickerFragment
import org.hse.ataskmobileclient.itemadapters.TasksAdapter
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private val gson = Gson()

    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    private val binding : ActivityMainBinding by lazy {
        val binding : ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.lifecycleOwner = this@MainActivity
        binding.viewModel = viewModel
        binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(binding.myToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel.reloadData()

        val deadlineTasks = viewModel.deadlineTasks.value ?: arrayListOf()
        val deadlineTasksAdapter = TasksAdapter(deadlineTasks,
            this@MainActivity::openDeadlineTask, this@MainActivity::toggleTaskStatus)
        viewModel.deadlineTasks.observe(this, { deadlineTasksAdapter.tasks = it })
        binding.deadlineTasksList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.deadlineTasksList.adapter = deadlineTasksAdapter

        val backlogTasks = viewModel.backlogTasks.value ?: arrayListOf()
        val backlogTasksAdapter = TasksAdapter(backlogTasks,
            this@MainActivity::openBacklogTask, this@MainActivity::toggleTaskStatus)

        viewModel.backlogTasks.observe(this, { backlogTasksAdapter.tasks = it })
        binding.backlogTasksList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.backlogTasksList.adapter = backlogTasksAdapter

        val photoUrlString = intent.getStringExtra(PHOTO_URL_EXTRA)!!
        val fullName = intent.getStringExtra(FULL_NAME_EXTRA)!!
        viewModel.userName = fullName
        viewModel.photoUrl = photoUrlString

        binding.ivMainLogout.setOnClickListener { finish() }
        binding.btnAddTask.setOnClickListener { openAddTaskScreen() }

        viewModel.pickStartTimeClickedEvent.observe(this, {
            pickDateViaDialog(PICK_START_TIME_FILTER_CODE)
        })

        viewModel.pickEndTimeClickedEvent.observe(this, {
            pickDateViaDialog(PICK_END_TIME_FILTER_CODE)
        })

        viewModel.pickLabelClickedEvent.observe(this, {
            pickLabelForFilterViaDialog()
        })

        viewModel.isLoading.observe(this, { isLoading ->
            binding.pullToRefreshTasks.isRefreshing = isLoading
        })

        binding.pullToRefreshTasks.setOnRefreshListener {
            viewModel.reloadData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TASK_EDIT_CODE) {
            if (resultCode != RESULT_OK || data == null)
                return

            val editTaskResultJson = data.getStringExtra(EditTaskActivity.EDIT_TASK_RESULT_JSON)
            val editTaskResult = gson.fromJson(editTaskResultJson, EditTaskResult::class.java)
            val editedTask = editTaskResult.editedTask

            when (editTaskResult.statusCode) {
                EditTaskStatusCode.DELETE -> viewModel.deleteTask(editedTask)
                EditTaskStatusCode.UPDATE -> viewModel.updateTask(editedTask)
                EditTaskStatusCode.ADD -> viewModel.addTask(editedTask)
            }
        }
    }

    private fun openDeadlineTask(view : View, taskPosition: Int) {
        val clickedTask = viewModel.deadlineTasks.value!![taskPosition]
        if (clickedTask !is Task)
            return

        openTaskForEditing(view, clickedTask)
    }

    private fun openBacklogTask(view : View, taskPosition: Int) {
        val clickedTask = viewModel.backlogTasks.value!![taskPosition]
        if (clickedTask !is Task)
            return

        openTaskForEditing(view, clickedTask)
    }

    private fun toggleTaskStatus(taskId: String?) {
        if (taskId != null)
            viewModel.toggleTaskCompletedStatus(taskId)
    }

    private fun openTaskForEditing(view : View, task : Task) {
        val taskJson = gson.toJson(task)
        val intent = Intent(view.context, EditTaskActivity::class.java).apply {
            putExtra(EditTaskActivity.TASK_JSON, taskJson)
        }
        startActivityForResult(intent, TASK_EDIT_CODE)
    }

    private fun openAddTaskScreen() {
        val intent = Intent(this@MainActivity, EditTaskActivity::class.java)
        startActivityForResult(intent, TASK_EDIT_CODE)
    }

    private fun pickDateViaDialog(dialogCode : Int) {
        val currentDate =
            when (dialogCode){
                PICK_START_TIME_FILTER_CODE -> viewModel.getFilterStartTime()
                PICK_END_TIME_FILTER_CODE -> viewModel.getFilterEndTime()
                else -> null
            }

        val fragment = DatePickerFragment(currentDate) {
            when (dialogCode) {
                PICK_START_TIME_FILTER_CODE -> viewModel.setFilterStartTime(it)
                PICK_END_TIME_FILTER_CODE -> viewModel.setFilterEndTime(it)
            }
        }

        fragment.show(supportFragmentManager, "datePicker")
    }

    private fun pickLabelForFilterViaDialog() {
        val labels = viewModel.availableLabels.toTypedArray()

        val dialog = AlertDialog.Builder(this@MainActivity)
            .setTitle(getString(R.string.pick_label_for_filter))
            .setItems(labels) { _, position ->
                val pickedLabel = labels.getOrNull(position)
                viewModel.setFilterLabel(pickedLabel)
            }
            .setNeutralButton(getString(R.string.clear_label_filter)) { _, _ ->
                viewModel.setFilterLabel(null)
            }
            .create()

        dialog.show()
    }

    companion object {
        const val FULL_NAME_EXTRA = "Username"
        const val PHOTO_URL_EXTRA = "ProfilePictureUrl"

        private const val TAG = "MainActivity"
        private const val TASK_EDIT_CODE = 1
        private const val PICK_START_TIME_FILTER_CODE = 2
        private const val PICK_END_TIME_FILTER_CODE = 3
    }
}
