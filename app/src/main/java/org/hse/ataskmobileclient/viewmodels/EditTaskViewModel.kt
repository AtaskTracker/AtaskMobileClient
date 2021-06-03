package org.hse.ataskmobileclient.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.SingleLiveEvent
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskMember
import org.hse.ataskmobileclient.services.*
import java.text.SimpleDateFormat
import java.util.*


class EditTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val usersService : IUsersService = UsersService()
    private val labelsService : ILabelsService = FakeLabelsService()
    private val isCompleted : MutableLiveData<Boolean> = MutableLiveData(false)

    var id: UUID? = null
        private set

    var taskName = ""
    var description = ""
    var newMemberEmail : String = ""
    val taskLabel : MutableLiveData<String?> = MutableLiveData(null)

    val members : MutableLiveData<ArrayList<TaskMember>> = MutableLiveData(arrayListOf())
    var dueDate : MutableLiveData<Date?> = MutableLiveData(null)
    var taskPicture : MutableLiveData<Bitmap> = MutableLiveData(null)

    val isLoading : MutableLiveData<Boolean> = MutableLiveData(false)

    val pickDateClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()
    val selectPictureClickedEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    val onUserAlreadyAddedEvent : SingleLiveEvent<Any> = SingleLiveEvent()
    val onUserNotFoundEvent : SingleLiveEvent<Any> = SingleLiveEvent()
    val onPickLabelClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()

    private val areAvailableLabelsLoaded : Boolean = false
    var availableLabels : List<String> = arrayListOf()
        private set

    val isTaskPictureSelected = Transformations.map(taskPicture) { taskPicture -> taskPicture != null }

    val dueDateStr : LiveData<String> = Transformations.map(dueDate) { newDate ->
        if (newDate == null) {
            "Без срока"
        } else {
            val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            "Срок: ${simpleDateFormat.format(newDate)}"
        }
    }

    val isCompletedStateStr : LiveData<String>  = Transformations.map(isCompleted) { isCompleted ->
        if (isCompleted) "Не сделано!" else "Сделано!"
    }

    val taskLabelString = Transformations.map(taskLabel) { taskLabel ->
        val selectedLabel = taskLabel ?: "Не задано"
        val stringPrefix = application.getString(R.string.task_label_prefix)
        "$stringPrefix: $selectedLabel"
    }

    fun initializeFromTask(task: Task) {
        id = task.id
        taskName = task.taskName
        description = task.description
        isCompleted.value = task.isCompleted
        dueDate.value = task.dueDate
        members.value!!.clear()
        members.value!!.addAll(task.members)
        taskLabel.value = task.label
        taskPicture.value =
            if (task.taskPictureBase64 == null) null
            else BitmapConverter.fromBase64(task.taskPictureBase64)
    }

    fun getEditedTask(): Task {
        val taskPictureBitmap = taskPicture.value
        val taskPictureBase64 =
            if (taskPictureBitmap == null) null
            else BitmapConverter.toBase64(taskPictureBitmap)

        return Task(
            id ?: UUID.randomUUID(),
            isCompleted.value!!,
            taskName,
            description,
            dueDate.value,
            members.value!!,
            taskLabel.value,
            taskPictureBase64)
    }

    fun onPickDateClicked() = pickDateClickedEvent.call()
    fun onTaskPictureClicked() = selectPictureClickedEvent.call()
    fun onPickLabelClicked() {

        val authToken = getAuthToken()
        if (!areAvailableLabelsLoaded) {
            viewModelScope.launch {
                isLoading.value = true
                availableLabels = labelsService.getAvailableLabels(authToken)
                isLoading.value = false
            }
        }

        onPickLabelClickedEvent.call()
    }
    
    fun switchIsCompletedState() { isCompleted.value = !isCompleted.value!! }

    fun addSelectedMember() {

        val currentTaskMembers = members.value ?: arrayListOf()
        if (currentTaskMembers.any { it.email == newMemberEmail }) {
            onUserAlreadyAddedEvent.call()
        }
        else viewModelScope.launch {
            val authToken = getAuthToken()
            isLoading.value = true
            val user = usersService.getUserByEmail(authToken, newMemberEmail)
            if (user == null)
                onUserNotFoundEvent.call()
            else{
                val newTaskMember = TaskMember(user.id, user.email, user.photoUrl)
                currentTaskMembers.add(newTaskMember)
                members.value = currentTaskMembers
            }

            isLoading.value = false
        }
    }

    fun removeMemberAt(position: Int) {
        if (members.value == null || position >= members.value!!.count())
            return

        members.value!!.removeAt(position)
        members.value = members.value
    }

    private fun getAuthToken(): String {
        val application = getApplication<Application>()
        return SessionManager(application).fetchAuthToken() ?: ""
    }

    companion object {
        @JvmStatic
        @BindingAdapter("imageBitmap")
        fun setImageFromBitmap(imageView: ImageView, bitmap: Bitmap?) {
            if (bitmap != null)
                imageView.setImageBitmap(bitmap)
            else
                imageView.setImageResource(R.drawable.task_picture_placeholder)
        }
    }
}