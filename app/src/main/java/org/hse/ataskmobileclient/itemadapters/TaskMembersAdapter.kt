package org.hse.ataskmobileclient.itemadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.databinding.TaskMemberItemBinding
import org.hse.ataskmobileclient.models.TaskMember
import org.hse.ataskmobileclient.viewmodels.TaskMemberViewModel

class TaskMembersAdapter(
    private val onItemRemoveClick: OnItemRemoveClick)
    : ListAdapter<TaskMember, TaskMembersAdapter.ViewHolder>(TaskMemberCallback())
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<TaskMemberItemBinding>(inflater,
            R.layout.task_member_item, parent,false)

        return ViewHolder(binding, onItemRemoveClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taskMember = getItem(position)
        holder.bind(taskMember)
    }

    class ViewHolder(
        private val binding: TaskMemberItemBinding,
        private val onItemRemoveClick : OnItemRemoveClick)
        : RecyclerView.ViewHolder(binding.root)
    {
        init {
            binding.tvMemberRemove.setOnClickListener { onItemRemoveClick.onClick(adapterPosition) }
        }

        fun bind(taskMember: TaskMember) {
            binding.viewModel = TaskMemberViewModel(taskMember)
        }
    }
}

interface OnItemRemoveClick {
    fun onClick(position: Int)
}

class TaskMemberCallback : DiffUtil.ItemCallback<TaskMember>() {
    override fun areItemsTheSame(oldItem: TaskMember, newItem: TaskMember): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TaskMember, newItem: TaskMember): Boolean {
        return  oldItem.id == newItem.id &&
                oldItem.username == newItem.username &&
                oldItem.photoUrl == newItem.photoUrl
    }

}