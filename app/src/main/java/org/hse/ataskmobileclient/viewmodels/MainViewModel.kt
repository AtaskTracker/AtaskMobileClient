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
import org.hse.ataskmobileclient.SingleLiveEvent
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.services.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val ungroupedDeadlineTasks : MutableLiveData<ArrayList<Task>> = MutableLiveData(arrayListOf())
    private val ungroupedBacklogTasks : MutableLiveData<ArrayList<Task>> = MutableLiveData(arrayListOf())
    private val filterStartTime : MutableLiveData<Date?> = MutableLiveData(null)
    private val filterEndTime : MutableLiveData<Date?> = MutableLiveData(null)
    private val filterLabel : MutableLiveData<String?> = MutableLiveData(null)

    private val tasksService : ITasksService = FakeTasksService()
    private val labelsService : ILabelsService = FakeLabelsService()

    val deadlineTasks = Transformations.map(ungroupedDeadlineTasks) {
        TasksGroupingUtil.getGroupedDeadlineTasks(application, it)
    }

    val backlogTasks = Transformations.map(ungroupedBacklogTasks) {
        TasksGroupingUtil.getGroupedBacklogTasks(application, it)
    }

    val isShowingDeadlineTasks : MutableLiveData<Boolean> = MutableLiveData(true)
    val filterStartTimeString = Transformations.map(filterStartTime) {
        val fromDateString = application.getString(R.string.from_date_filter)
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val startTimeString =
            if (it == null) application.getString(R.string.filter_not_set)
            else sdf.format(it)

        "$fromDateString: $startTimeString"
    }
    val filterEndTimeString = Transformations.map(filterEndTime) {
        val toDateString = application.getString(R.string.to_date_filter)
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val endTimeString =
            if (it == null) application.getString(R.string.filter_not_set)
            else sdf.format(it)

        "$toDateString: $endTimeString"
    }
    val filterLabelString = Transformations.map(filterLabel) {
        val labelString = it ?: application.getString(R.string.filter_not_set)
        val labelFilterString = application.getString(R.string.label_filter)

        "$labelFilterString: $labelString"
    }

    var photoUrl : String = ""
    var userName : String = ""

    val pickStartTimeClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()
    val pickEndTimeClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()
    val pickLabelClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()

    var availableLabels : List<String> = listOf()

    fun pickStartTime() = pickStartTimeClickedEvent.call()
    fun pickEndTime() = pickEndTimeClickedEvent.call()
    fun pickLabel() = pickLabelClickedEvent.call()

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

    fun reloadData() {
        viewModelScope.launch {
            reloadTasks()
            availableLabels = labelsService.getAvailableLabelsAsync()
        }
    }

    private suspend fun reloadTasks() {
        val allTasks = tasksService.getAllTasksAsync(filterStartTime.value,
            filterEndTime.value, filterLabel.value)

        ungroupedDeadlineTasks.value = ArrayList(allTasks.filter { it.dueDate != null })
        ungroupedBacklogTasks.value = ArrayList(allTasks.filter { it.dueDate == null })
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

    fun setFilterStartTime(date: Date?) {
        filterStartTime.value = date
        viewModelScope.launch { reloadTasks() }
    }

    fun setFilterEndTime(date: Date?) {
        filterEndTime.value = date
        viewModelScope.launch { reloadTasks() }
    }

    fun setFilterLabel(label: String?) {
        filterLabel.value = label
        viewModelScope.launch { reloadTasks() }
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