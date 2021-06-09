package org.hse.ataskmobileclient.itemadapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.viewmodels.TaskListItem
import org.hse.ataskmobileclient.viewmodels.TasksHeader
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class TasksAdapter(
    tasks : ArrayList<TaskListItem>,
    private val onListItemClick: (view : View, position: Int) -> Unit
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    var tasks = tasks
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        if (viewType == TYPE_SPECIFIC_TASK) {
            val contactView = inflater.inflate(R.layout.task_item, parent, false)
            return TaskViewHolder(contactView, onListItemClick)
        } else if (viewType == TYPE_TASKS_BLOCK_HEADER) {
            val contactView = inflater.inflate(R.layout.tasks_block_header, parent, false)
            return TasksHeaderViewHolder(contactView, onListItemClick)
        }
        throw IllegalArgumentException("Invalid view type")

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = tasks[position]

        if (holder is TaskViewHolder) {
            holder.bind(data as Task)
        }
        else if (holder is TasksHeaderViewHolder) {
            holder.bind(data as TasksHeader)
        }
    }

    override fun getItemCount(): Int {
        return tasks.count()
    }

    override fun getItemViewType(position: Int): Int {
        if (position >= tasks.count())
            return -1

        val item = tasks[position]
        if (item is Task)
            return TYPE_SPECIFIC_TASK

        if (item is TasksHeader)
            return TYPE_TASKS_BLOCK_HEADER

        return -1
    }

    companion object{
        const val TYPE_SPECIFIC_TASK = 0
        const val TYPE_TASKS_BLOCK_HEADER = 1
    }
}

class TaskViewHolder(
        itemView: View,
        private val onListItemClick: (view : View, position: Int) -> Unit)
    : RecyclerView.ViewHolder(itemView) {

    private val cbTaskCompleted : CheckBox = itemView.findViewById(R.id.cb_task)
    private val tvTaskName : TextView = itemView.findViewById(R.id.tv_task_name)
    private val tvTaskLabel : TextView = itemView.findViewById(R.id.tv_task_label)
    private val tvDueDate : TextView = itemView.findViewById(R.id.due_date_string)

    init {
        itemView.setOnClickListener { onListItemClick(it, adapterPosition) }
    }

    fun bind(taskItem: Task) {
        cbTaskCompleted.isChecked = taskItem.isCompleted
        tvTaskName.text = taskItem.taskName
        tvTaskLabel.text = taskItem.label ?: ""
        if (taskItem.dueDate != null)
            tvDueDate.text = sdf.format(taskItem.dueDate)
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        private val sdf = SimpleDateFormat("dd.MM.yyyy")
    }
}

class TasksHeaderViewHolder(
        itemView: View,
        private val onListItemClick: (view : View, position: Int) -> Unit)
    : RecyclerView.ViewHolder(itemView)
{
    private val tvTasksBlockName : TextView = itemView.findViewById(R.id.tv_tasks_block_name)

    init {
        itemView.setOnClickListener { onListItemClick(it, adapterPosition) }
    }

    fun bind(tasksHeader: TasksHeader) {
        tvTasksBlockName.text = tasksHeader.headerString
    }
}