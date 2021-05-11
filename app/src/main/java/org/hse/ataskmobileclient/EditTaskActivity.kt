package org.hse.ataskmobileclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hse.ataskmobileclient.itemadapters.TaskMemberAdapter
import org.hse.ataskmobileclient.models.TaskMember

class EditTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val members : ArrayList<TaskMember> = arrayListOf(
                TaskMember("Егор Карташов", true),
                TaskMember("Роман Салахов", true),
        )

        val taskMembersAdapter = TaskMemberAdapter(members)
        val taskMembersList = findViewById<RecyclerView>(R.id.rv_task_members)
        taskMembersList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        taskMembersList.layoutManager = LinearLayoutManager(this)
        taskMembersList.adapter = taskMembersAdapter

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }
}