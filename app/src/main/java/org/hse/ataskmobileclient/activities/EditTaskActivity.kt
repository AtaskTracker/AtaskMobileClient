package org.hse.ataskmobileclient.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.itemadapters.OnItemRemoveClick
import org.hse.ataskmobileclient.itemadapters.TaskMemberAdapter
import org.hse.ataskmobileclient.models.Task
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class EditTaskActivity : AppCompatActivity() {

    private lateinit var etTaskDescription: EditText
    private lateinit var etTaskName: EditText
    private lateinit var tvTaskDueDate : TextView
    private lateinit var ivDatePicker : ImageView
    private lateinit var btnChangeCompletedState : Button

    private var completed : Boolean = false
    set(value) {
        field = value
        if (field) {
            btnChangeCompletedState.text = "Не сделано!"
        }
        else {
            btnChangeCompletedState.text = "Сделано!"
        }
    }

    private var dueDate : Date? = null
    set(value) {
        field = value
        val dueDateString =
            if (dueDate == null)
            {
                "Без срока"
            }
            else
            {
                val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                "Срок: ${simpleDateFormat.format(dueDate!!)}"
            }

        tvTaskDueDate.text = dueDateString
    }

    private lateinit var taskMembersAdapter : TaskMemberAdapter
    private var oldTask : Task? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        etTaskName = findViewById(R.id.edittask_name)
        etTaskDescription = findViewById(R.id.edittask_description)
        tvTaskDueDate = findViewById(R.id.edittask_duedate)
        ivDatePicker = findViewById(R.id.edittask_datepicker)
        btnChangeCompletedState = findViewById(R.id.edittask_btn_changetaskcompleted)

        btnChangeCompletedState.setOnClickListener {
            completed = !completed
        }

        val myCalendar = Calendar.getInstance()
        val onDateSetListener =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                dueDate = myCalendar.time
            }

        ivDatePicker.setOnClickListener {
            DatePickerDialog(
                this@EditTaskActivity, onDateSetListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val taskJson = intent.getStringExtra(TASK_JSON)
        if (taskJson != null) {
            oldTask = gson.fromJson(taskJson, Task::class.java)
            fillFieldsWithData(oldTask!!)
        }

//        val members : ArrayList<TaskMember> = arrayListOf(
//            TaskMember("Егор Карташов", true),
//            TaskMember("Роман Салахов", true),
//            TaskMember("Иван Иванов", true),
//            TaskMember("Петр Петров", true),
//            TaskMember("Сергей Сергеев", true),
//        )

        val taskMembers = oldTask?.members ?: arrayListOf()

        taskMembersAdapter = TaskMemberAdapter(ArrayList(taskMembers), object : OnItemRemoveClick {
            override fun onClick(position: Int) {
                val members = taskMembersAdapter.getMembers()
                members.removeAt(position)
                taskMembersAdapter.setMembers(members)
            }
        })
        val taskMembersList = findViewById<RecyclerView>(R.id.rv_task_members)
        taskMembersList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        taskMembersList.layoutManager = LinearLayoutManager(this)
        taskMembersList.adapter = taskMembersAdapter

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finishEditing()
        }

        val addButton = findViewById<ImageView>(R.id.et_member_add)
        addButton.setOnClickListener {
            val spinner = findViewById<Spinner>(R.id.et_member_name_spinner);
            val username = spinner.selectedItem.toString()
            taskMembersAdapter.addMember(username);
        }

        initAddMembersSpinner()
    }

       private fun initAddMembersSpinner() {
           val memberOptions =
               arrayOf("Владимир Путин", "Гордон Фриман", "Джуди Хоппс", "Павел Дуров")
           val spinner = findViewById<Spinner>(R.id.et_member_name_spinner);
           val adapter =
               ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, memberOptions)
           adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
           spinner.adapter = adapter
       }


    private fun fillFieldsWithData(task: Task) {
        dueDate = task.dueDate
        etTaskName.setText(task.taskName)
        etTaskDescription.setText(task.description)
        completed = task.isCompleted
    }

    private fun finishEditing() {
        val taskName = etTaskName.text.toString()
        val taskDescription = etTaskDescription.text.toString()
        val oldTask = oldTask!!
        val task = Task(
            oldTask.id,
            completed,
            taskName,
            taskDescription,
            dueDate,
            taskMembersAdapter.getMembers()
        )
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