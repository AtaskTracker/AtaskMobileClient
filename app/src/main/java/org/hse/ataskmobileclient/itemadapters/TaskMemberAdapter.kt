package org.hse.ataskmobileclient.itemadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.models.TaskMember

class TaskMemberAdapter(
        private val tasksMembers : ArrayList<TaskMember>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.task_member_item, parent, false)
        return TaskMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val taskMember = tasksMembers[position]
        if (holder is TaskMemberViewHolder)
            holder.bind(taskMember)
    }

    override fun getItemCount(): Int = tasksMembers.count()

}

class TaskMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tvMemberName : TextView = itemView.findViewById(R.id.tv_member_name)

    fun bind(taskMember: TaskMember) {
        tvMemberName.text = taskMember.username
    }
}