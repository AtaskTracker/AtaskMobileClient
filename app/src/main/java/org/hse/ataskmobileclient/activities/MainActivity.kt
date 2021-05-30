package org.hse.ataskmobileclient.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import org.hse.ataskmobileclient.itemadapters.TasksAdapter
import org.hse.ataskmobileclient.dto.Task
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

        viewModel.reloadTasks()

        val deadlineTasks = viewModel.deadlineTasks.value ?: arrayListOf()
        val deadlineTasksAdapter = TasksAdapter(deadlineTasks, this@MainActivity::openDeadlineTask)
        viewModel.deadlineTasks.observe(this, { deadlineTasksAdapter.tasks = it })
        binding.deadlineTasksList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.deadlineTasksList.adapter = deadlineTasksAdapter

        val backlogTasks = viewModel.backlogTasks.value ?: arrayListOf()
        val backlogTasksAdapter = TasksAdapter(backlogTasks, this@MainActivity::openBacklogTask)
        viewModel.backlogTasks.observe(this, { backlogTasksAdapter.tasks = it })
        binding.backlogTasksList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.backlogTasksList.adapter = backlogTasksAdapter

        val photoUrlString = intent.getStringExtra(PHOTO_URL_EXTRA)!!
        val fullName = intent.getStringExtra(FULL_NAME_EXTRA)!!
        viewModel.userName = fullName
        viewModel.photoUrl = photoUrlString

        binding.ivMainLogout.setOnClickListener { finish() }
        binding.btnAddTask.setOnClickListener { openAddTaskScreen() }
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

    private fun openTaskForEditing(view : View, task : Task) {
        val taskJson = gson.toJson(task)
        val intent = Intent(view.context, EditTaskActivity::class.java).apply {
            putExtra(EditTaskActivity.TASK_JSON, taskJson)
        }
        startActivityForResult(intent, TASK_EDIT_CODE)
    }

    private fun openAddTaskScreen() {
        Log.i(TAG, "========== Add task button clicked! ========")
        val intent = Intent(this@MainActivity, EditTaskActivity::class.java)
        startActivityForResult(intent, TASK_EDIT_CODE)
    }

    companion object {
        const val FULL_NAME_EXTRA = "Username"
        const val PHOTO_URL_EXTRA = "ProfilePictureUrl"

        private const val TAG = "MainActivity"
        private const val TASK_EDIT_CODE = 1
    }
}
