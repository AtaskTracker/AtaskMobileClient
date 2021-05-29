package org.hse.ataskmobileclient.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.*
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.SingleLiveEvent
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskMember
import org.hse.ataskmobileclient.services.BitmapConverter
import java.text.SimpleDateFormat
import java.util.*


class EditTaskViewModel(application: Application) : AndroidViewModel(application) {

    private var id: UUID? = null

    var taskName = ""
    var description = ""
    val members : MutableLiveData<ArrayList<TaskMember>> = MutableLiveData(arrayListOf())
    var dueDate : MutableLiveData<Date?> = MutableLiveData(null)
    val pickDateClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()
    val selectPictureClickedEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    var selectedAccount : TaskMember? = null
    var taskPicture : MutableLiveData<Bitmap> = MutableLiveData(null)

    val taskPictureSelected = Transformations.map(taskPicture) { it != null }

    private var isCompleted : MutableLiveData<Boolean> = MutableLiveData(false)
    private var label : String? = null

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

    fun initializeFromTask(task: Task) {
        id = task.id
        taskName = task.taskName
        description = task.description
        isCompleted.value = task.isCompleted
        dueDate.value = task.dueDate
        members.value!!.clear()
        members.value!!.addAll(task.members)
        label = task.label
        taskPicture.value =
            if (task.taskPictureBase64 == null) null
            else BitmapConverter.fromBase64(task.taskPictureBase64)
    }

    fun getEditedTask(): Task {
        val taskPictureBitmap = taskPicture.value
        val taskPictureBase64 =
            if (taskPictureBitmap == null) ""
            else BitmapConverter.toBase64(taskPictureBitmap)

        return Task(
            id ?: UUID.randomUUID(),
            isCompleted.value!!,
            taskName,
            description,
            dueDate.value,
            members.value!!,
            label,
            taskPictureBase64)
    }

    fun onPickDateClicked() = pickDateClickedEvent.call()

    fun removeDueDate() { dueDate.value = null }
    fun removeTaskPicture() { taskPicture.value = null }
    fun switchIsCompletedState() { isCompleted.value = !isCompleted.value!! }
    fun selectTaskPicture() { selectPictureClickedEvent.call() }

    fun addSelectedMember() {
        if (selectedAccount != null) {
            members.value?.add(selectedAccount!!)
            members.value = members.value
        }
    }

    fun removeMemberAt(position: Int) {
        if (members.value == null || position >= members.value!!.count())
            return

        members.value!!.removeAt(position)
        members.value = members.value
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