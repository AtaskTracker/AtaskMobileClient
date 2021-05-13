package org.hse.ataskmobileclient.itemadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.models.TaskMember

class TaskMemberAdapter(
        private var tasksMembers : ArrayList<TaskMember>,
        private val onItemRemoveClick: OnItemRemoveClick)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    fun getMembers() = tasksMembers

    fun setMembers(members : ArrayList<TaskMember>) { this.tasksMembers = members }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.task_member_item, parent, false)
        return TaskMemberViewHolder(view, onItemRemoveClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val taskMember = tasksMembers[position]
        if (holder is TaskMemberViewHolder)
            holder.bind(taskMember)
    }

    override fun getItemCount(): Int = tasksMembers.count()

}

class TaskMemberViewHolder(
    itemView: View,
    private val onItemRemoveClick : OnItemRemoveClick) : RecyclerView.ViewHolder(itemView) {

    private val tvMemberName : TextView = itemView.findViewById(R.id.tv_member_name)

    init {
        //TODO достать "кнопку" удаления мембера и повесить обработчик на нее, а не на всю view
        itemView.setOnClickListener { onItemRemoveClick.onClick(adapterPosition) }
    }

    fun bind(taskMember: TaskMember) {
        tvMemberName.text = taskMember.username
    }
}

interface OnItemRemoveClick {
    fun onClick(position: Int)
}