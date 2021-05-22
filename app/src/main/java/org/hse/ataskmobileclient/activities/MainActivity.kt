package org.hse.ataskmobileclient.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import org.hse.ataskmobileclient.MockData
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.itemadapters.OnListItemClick
import org.hse.ataskmobileclient.itemadapters.TaskAdapter
import org.hse.ataskmobileclient.models.Task

class MainActivity : AppCompatActivity() {

    private lateinit var tasksAdapter: TaskAdapter
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.navigation_bar)

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

        val recyclerView = findViewById<RecyclerView>(R.id.rv_tasks_list)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = tasksAdapter
        recyclerView.isNestedScrollingEnabled = false


        val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.navigation_tasks_with_deadline -> {
                    (recyclerView.adapter as TaskAdapter).setTasks(MockData.DeadlineTasks)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_backlog_tasks -> {
                    (recyclerView.adapter as TaskAdapter).setTasks(MockData.BacklogTasks)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

        bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val photoUrlString = intent.getStringExtra(PHOTO_URL_EXTRA)
        val fullName = intent.getStringExtra(FULL_NAME_EXTRA)!!
        initAppBar(fullName, Uri.parse(photoUrlString))

        val ivLogout = findViewById<ImageView>(R.id.iv_main_logout)
        ivLogout.setOnClickListener { logout() }
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
            tasks[oldTaskPosition] = editedTask
            tasksAdapter.setTasks(tasks)
        }
    }

    private fun initAppBar(fullName: String, profilePhotoUri: Uri) {
        val appbarUsername = findViewById<TextView>(R.id.appbar_username)
        appbarUsername.text = fullName

        val ivProfilePicture = findViewById<ImageView>(R.id.profile_picture)
        Glide.with(ivProfilePicture).load(profilePhotoUri).into(ivProfilePicture)
    }

    private fun logout(){
        Log.i(TAG, "Logout clicked")
        finish()
    }

    companion object {
        const val FULL_NAME_EXTRA = "Username"
        const val PHOTO_URL_EXTRA = "ProfilePictureUrl"

        private const val TAG = "MainActivity"
        private const val TASK_EDIT_CODE = 1
    }
}