package org.hse.ataskmobileclient.viewmodels

import android.app.Application
import android.view.MenuItem
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.services.FakeTasksService
import org.hse.ataskmobileclient.services.ITasksService
import org.hse.ataskmobileclient.services.TasksGroupingUtil


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val ungroupedDeadlineTasks : MutableLiveData<ArrayList<Task>> = MutableLiveData(arrayListOf())
    private val ungroupedBacklogTasks : MutableLiveData<ArrayList<Task>> = MutableLiveData(arrayListOf())

    private val tasksService : ITasksService = FakeTasksService()

    val deadlineTasks = Transformations.map(ungroupedDeadlineTasks) {
        TasksGroupingUtil.getGroupedDeadlineTasks(application, it)
    }

    val backlogTasks = Transformations.map(ungroupedBacklogTasks) {
        TasksGroupingUtil.getGroupedBacklogTasks(application, it)
    }

    val isShowingDeadlineTasks : MutableLiveData<Boolean> = MutableLiveData(true)

    var userName : String = ""
    var photoUrl : String = ""

    fun onNavigationItemSelected(item: MenuItem) : Boolean {
        when (item.itemId){
            R.id.navigation_tasks_with_deadline -> {
                isShowingDeadlineTasks.value = true
                return true
            }
            R.id.navigation_backlog_tasks -> {
                isShowingDeadlineTasks.value = false
                return true
            }
        }
        return false
    }

    fun reloadTasks() {
        viewModelScope.launch {
            val allTasks = tasksService.getAllTasksAsync()
            ungroupedDeadlineTasks.value = ArrayList(allTasks.filter { it.dueDate != null })
            ungroupedBacklogTasks.value = ArrayList(allTasks.filter { it.dueDate == null })
        }
    }

    fun addTask(task: Task) {
        val taskListToAddTo =
            if (task.dueDate != null) ungroupedDeadlineTasks
            else ungroupedBacklogTasks

        val currentTaskList = taskListToAddTo.value ?: arrayListOf()
        currentTaskList.add(task)
        taskListToAddTo.value = currentTaskList
    }

    fun updateTask(task : Task) {
        val deadlineTasks = ungroupedDeadlineTasks.value ?: arrayListOf()
        val backlogTasks = ungroupedBacklogTasks.value ?: arrayListOf()
        val oldPositionInDeadlineTasks = deadlineTasks.indexOfFirst { (it as? Task)?.id == task.id }
        val oldPositionInBackLogTasks = backlogTasks.indexOfFirst { (it as? Task)?.id == task.id }

        val newTaskHasDeadline = task.dueDate != null
        if (newTaskHasDeadline) {
            if (oldPositionInDeadlineTasks >= 0)
                deadlineTasks[oldPositionInDeadlineTasks] = task
            else
                deadlineTasks.add(task)

            if (oldPositionInBackLogTasks >= 0)
                backlogTasks.removeAt(oldPositionInBackLogTasks)
        }
        else {
            if (oldPositionInBackLogTasks >= 0)
                backlogTasks[oldPositionInBackLogTasks] = task
            else
                backlogTasks.add(task)

            if (oldPositionInDeadlineTasks >= 0)
                deadlineTasks.removeAt(oldPositionInDeadlineTasks)
        }

        this.ungroupedDeadlineTasks.value = deadlineTasks
        this.ungroupedBacklogTasks.value = backlogTasks
    }

    fun deleteTask(task: Task) {
        val deadlineTasks = this.ungroupedDeadlineTasks.value ?: arrayListOf()
        val backlogTasks = this.ungroupedBacklogTasks.value ?: arrayListOf()
        val oldPositionInDeadlineTasks = deadlineTasks.indexOfFirst { (it as? Task)?.id == task.id }
        val oldPositionInBackLogTasks = backlogTasks.indexOfFirst { (it as? Task)?.id == task.id }

        if (oldPositionInDeadlineTasks >= 0)
            deadlineTasks.removeAt(oldPositionInDeadlineTasks)

        if (oldPositionInBackLogTasks >= 0)
            backlogTasks.removeAt(oldPositionInBackLogTasks)

        this.ungroupedDeadlineTasks.value = deadlineTasks
        this.ungroupedBacklogTasks.value = backlogTasks
    }

    companion object {
        @JvmStatic
        @BindingAdapter("imageUrl")
        fun loadImage(view: ImageView, imageUrl: String?) {
            if (imageUrl != null && imageUrl.isNotEmpty())
                Glide.with(view.context).load(imageUrl).into(view)
        }

        @JvmStatic
        @BindingAdapter("onNavigationItemSelected")
        fun setOnNavigationItemSelected(
            view: BottomNavigationView,
            listener: BottomNavigationView.OnNavigationItemSelectedListener?
        ) {
            view.setOnNavigationItemSelectedListener(listener)
        }
    }
}