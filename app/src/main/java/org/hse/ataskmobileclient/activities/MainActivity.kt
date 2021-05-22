package org.hse.ataskmobileclient.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import org.hse.ataskmobileclient.MockData
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.databinding.ActivityMainBinding
import org.hse.ataskmobileclient.itemadapters.OnListItemClick
import org.hse.ataskmobileclient.itemadapters.TaskAdapter
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var tasksAdapter: TaskAdapter
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
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        tasksAdapter = TaskAdapter(MockData.DeadlineTasks, object : OnListItemClick {
            override fun onClick(view: View, position: Int) {
                val tasks = tasksAdapter.getTasks()
                val clickedTask = tasks[position]
                val clickedTaskJson = gson.toJson(clickedTask)
                val intent = Intent(view.context, EditTaskActivity::class.java).apply {
                    putExtra(EditTaskActivity.TASK_JSON, clickedTaskJson)
                }
                startActivityForResult(intent, TASK_EDIT_CODE)
            }
        })

        binding.tasksList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.tasksList.adapter = tasksAdapter

        val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val taskAdapter = binding.tasksList.adapter as TaskAdapter
            when (item.itemId){
                R.id.navigation_tasks_with_deadline -> {
                    MockData.BacklogTasks = taskAdapter.getTasks()
                    taskAdapter.setTasks(MockData.DeadlineTasks)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_backlog_tasks -> {
                    MockData.DeadlineTasks = taskAdapter.getTasks()
                    taskAdapter.setTasks(MockData.BacklogTasks)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

        binding.navigationBar.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val photoUrlString = intent.getStringExtra(PHOTO_URL_EXTRA)!!
        val fullName = intent.getStringExtra(FULL_NAME_EXTRA)!!
        viewModel.userName = fullName
        viewModel.photoUrl = photoUrlString

        binding.ivMainLogout.setOnClickListener { finish() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TASK_EDIT_CODE) {
            if (resultCode != RESULT_OK || data == null)
                return

            val editedTaskJson = data.getStringExtra(EditTaskActivity.TASK_RESULT_JSON)
            val editedTask = gson.fromJson(editedTaskJson, Task::class.java)

            val tasks = tasksAdapter.getTasks()
            val oldTaskPosition = tasks.indexOfFirst { taskListItem -> taskListItem is Task && taskListItem.id == editedTask.id }
            if (oldTaskPosition >= 0)
                tasks[oldTaskPosition] = editedTask
            else
                tasks.add(editedTask)

            tasksAdapter.setTasks(tasks)
        }
    }

    companion object {
        const val FULL_NAME_EXTRA = "Username"
        const val PHOTO_URL_EXTRA = "ProfilePictureUrl"

        private const val TAG = "MainActivity"
        private const val TASK_EDIT_CODE = 1
    }
}
