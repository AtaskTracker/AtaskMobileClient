package org.hse.ataskmobileclient.viewmodels

import android.app.Application
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.SingleLiveEvent
import org.hse.ataskmobileclient.apis.PhotosApi
import org.hse.ataskmobileclient.dto.GoogleImageDto
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.services.*
import org.hse.ataskmobileclient.utils.TasksGroupingUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val ungroupedDeadlineTasks : MutableLiveData<ArrayList<Task>> = MutableLiveData(arrayListOf())
    private val ungroupedBacklogTasks : MutableLiveData<ArrayList<Task>> = MutableLiveData(arrayListOf())
    private val startTimeFilter : MutableLiveData<Date?> = MutableLiveData(null)
    private val endTimeFilter : MutableLiveData<Date?> = MutableLiveData(null)
    private val filterLabel : MutableLiveData<String?> = MutableLiveData(null)

    private val tasksService : ITasksService = TasksService()
    private val labelsService : ILabelsService = LabelsService()
    private val statsService : IStatsService = StatsService()

    val isLoading : MutableLiveData<Boolean> = MutableLiveData(false)

    val deadlineTasks = Transformations.map(ungroupedDeadlineTasks) {
        TasksGroupingUtil.getGroupedDeadlineTasks(application, it)
    }

    val backlogTasks = Transformations.map(ungroupedBacklogTasks) {
        TasksGroupingUtil.getGroupedBacklogTasks(application, it)
    }

    val isShowingDeadlineTasks : MutableLiveData<Boolean> = MutableLiveData(true)
    val filterStartTimeString = Transformations.map(startTimeFilter) {
        val fromDateString = application.getString(R.string.from_date_filter)
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val startTimeString =
            if (it == null) application.getString(R.string.not_set)
            else sdf.format(it)

        "$fromDateString: $startTimeString"
    }
    val filterEndTimeString = Transformations.map(endTimeFilter) {
        val toDateString = application.getString(R.string.to_date_filter)
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val endTimeString =
            if (it == null) application.getString(R.string.not_set)
            else sdf.format(it)

        "$toDateString: $endTimeString"
    }
    val filterLabelString = Transformations.map(filterLabel) {
        val labelString = it ?: application.getString(R.string.not_set)
        val labelFilterString = application.getString(R.string.label_filter)

        "$labelFilterString: $labelString"
    }

    var photoUrl : String = ""
    var userName : String = ""

    val pickStartTimeClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()
    val pickEndTimeClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()
    val pickLabelClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()

    var availableLabels : List<String> = listOf()

    private val deadlineTasksCompletedPercentage : MutableLiveData<Float?> = MutableLiveData(null)
    private val backlogTasksCompletedPercentage : MutableLiveData<Float?> = MutableLiveData(null)

    val currentCompletedPercentage = Transformations
        .switchMap(deadlineTasksCompletedPercentage) { deadlineTasksCompletedPercentage ->
            Transformations.map(backlogTasksCompletedPercentage) { backlogTasksCompletedPercentage ->
                val progressFloat =
                    if (isShowingDeadlineTasks.value!!) deadlineTasksCompletedPercentage
                    else backlogTasksCompletedPercentage

                progressFloat?.toInt() ?: 0
            }
    }

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
            val authToken = getAuthToken()
            isLoading.value = true
            reloadTasks(authToken)
            availableLabels = labelsService.getAvailableLabels(authToken)
            isLoading.value = false

            deadlineTasksCompletedPercentage.value = statsService.getCompletedDeadlineTasksStats(
                authToken, startTimeFilter.value, endTimeFilter.value)

            backlogTasksCompletedPercentage.value = statsService.getCompletedBacklogTasksStats(
                authToken, filterLabel.value)

            Log.i(TAG, "Stats: " +
                    "deadline - ${deadlineTasksCompletedPercentage.value}, " +
                    "backlog - ${backlogTasksCompletedPercentage.value}")
        }
    }

    private suspend fun reloadTasksWithLoading() {
        isLoading.value = true
        reloadTasks()
        isLoading.value = false
    }

    private suspend fun reloadTasks(token: String? = null) {
        val authToken = token ?: getAuthToken()
        val allTasks = tasksService.getAllTasks(
            authToken,
            startTimeFilter.value, endTimeFilter.value, filterLabel.value
        )

        ungroupedDeadlineTasks.value = ArrayList(allTasks.filter { it.dueDate != null })
        ungroupedBacklogTasks.value = ArrayList(allTasks.filter { it.dueDate == null })
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            if (task.photoBase64 != null)
                task.photoUrl = task.photoBase64

            val addedTask = tasksService.addTask(getAuthToken(), task)
                ?: return@launch

            val taskListToAddTo =
                if (addedTask.dueDate != null) ungroupedDeadlineTasks
                else ungroupedBacklogTasks

            val currentTaskList = taskListToAddTo.value ?: arrayListOf()
            currentTaskList.add(addedTask)
            taskListToAddTo.value = currentTaskList
        }
    }

    fun updateTask(task : Task) {
        viewModelScope.launch {

            if (task.photoBase64 != null) {
                val addedPhotoDto = PhotosApi().postPhotoAsync(GoogleImageDto(task.id!!, task.photoBase64))
                val photoUrl = addedPhotoDto?.url
                if (photoUrl != null && photoUrl.isNotEmpty())
                    task.photoUrl = photoUrl
            }

            val updatedTask = tasksService.updateTask(getAuthToken(), task)
                ?: return@launch

            val deadlineTasks = ungroupedDeadlineTasks.value ?: arrayListOf()
            val backlogTasks = ungroupedBacklogTasks.value ?: arrayListOf()
            val oldPositionInDeadlineTasks = deadlineTasks.indexOfFirst { (it as? Task)?.id == updatedTask.id }
            val oldPositionInBackLogTasks = backlogTasks.indexOfFirst { (it as? Task)?.id == updatedTask.id }

            val newTaskHasDeadline = updatedTask.dueDate != null
            if (newTaskHasDeadline) {
                if (oldPositionInDeadlineTasks >= 0)
                    deadlineTasks[oldPositionInDeadlineTasks] = updatedTask
                else
                    deadlineTasks.add(updatedTask)

                if (oldPositionInBackLogTasks >= 0)
                    backlogTasks.removeAt(oldPositionInBackLogTasks)
            }
            else {
                if (oldPositionInBackLogTasks >= 0)
                    backlogTasks[oldPositionInBackLogTasks] = updatedTask
                else
                    backlogTasks.add(updatedTask)

                if (oldPositionInDeadlineTasks >= 0)
                    deadlineTasks.removeAt(oldPositionInDeadlineTasks)
            }

            ungroupedDeadlineTasks.value = deadlineTasks
            ungroupedBacklogTasks.value = backlogTasks
        }
    }

    fun deleteTask(task: Task) {

        viewModelScope.launch {
            val isDeletedOnBackend: Boolean = tasksService.deleteTask(getAuthToken(), task)
            if (!isDeletedOnBackend)
                return@launch

            val deadlineTasks = ungroupedDeadlineTasks.value ?: arrayListOf()
            val backlogTasks = ungroupedBacklogTasks.value ?: arrayListOf()
            val oldPositionInDeadlineTasks = deadlineTasks.indexOfFirst { (it as? Task)?.id == task.id }
            val oldPositionInBackLogTasks = backlogTasks.indexOfFirst { (it as? Task)?.id == task.id }

            if (oldPositionInDeadlineTasks >= 0)
                deadlineTasks.removeAt(oldPositionInDeadlineTasks)

            if (oldPositionInBackLogTasks >= 0)
                backlogTasks.removeAt(oldPositionInBackLogTasks)

            ungroupedDeadlineTasks.value = deadlineTasks
            ungroupedBacklogTasks.value = backlogTasks
        }
    }

    fun getFilterStartTime() = startTimeFilter.value
    fun getFilterEndTime() = endTimeFilter.value

    fun setFilterStartTime(newStartTimeFilter: Date?) {
        startTimeFilter.value = newStartTimeFilter

        if (newStartTimeFilter != null) {
            val currentEndTimeFilter = endTimeFilter.value
            if (currentEndTimeFilter != null && newStartTimeFilter > currentEndTimeFilter)
                endTimeFilter.value = newStartTimeFilter
        }

        viewModelScope.launch { reloadTasksWithLoading() }
    }

    fun setFilterEndTime(newEndTimeFilter: Date?) {
        endTimeFilter.value = newEndTimeFilter

        if (newEndTimeFilter != null) {
            val currentStartTimeFilter = startTimeFilter.value
            if (currentStartTimeFilter != null && currentStartTimeFilter > newEndTimeFilter)
                startTimeFilter.value = newEndTimeFilter
        }

        viewModelScope.launch { reloadTasksWithLoading() }
    }

    fun setFilterLabel(label: String?) {
        filterLabel.value = label
        viewModelScope.launch { reloadTasksWithLoading() }
    }

    private fun getAuthToken(): String {
        val application = getApplication<Application>()
        return SessionManager(application).fetchAuthToken() ?: ""
    }

    companion object {
        private val TAG = "MainViewModel"

        @JvmStatic
        @BindingAdapter("imageUrl")
        fun loadImage(view: ImageView, imageUrl: String?) {
            if (imageUrl != null && imageUrl.isNotEmpty()) {
                val circularProgressDrawable = CircularProgressDrawable(view.context)
                circularProgressDrawable.strokeWidth = 5f
                circularProgressDrawable.centerRadius = 30f
                circularProgressDrawable.start()
                Glide
                    .with(view.context)
                    .load(imageUrl)
                    .apply(RequestOptions()
                        .placeholder(R.drawable.task_picture_placeholder)
                    )
                    .listener(object: RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.i(TAG, "Error loading picture: $imageUrl")
                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.i(TAG, "Loaded picture: $imageUrl")
                            return false
                        }
                    })
                    .into(view)
            }
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