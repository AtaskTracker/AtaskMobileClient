package org.hse.ataskmobileclient.viewmodels

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import org.hse.ataskmobileclient.SingleLiveEvent
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.models.TaskMember
import java.text.SimpleDateFormat
import java.util.*

class EditTaskViewModel(application: Application) : AndroidViewModel(application) {

    private var id: UUID? = null

    var taskName = ""
    var description = ""
    var isCompleted : MutableLiveData<Boolean> = MutableLiveData(false)
    val members : MutableLiveData<ArrayList<TaskMember>> = MutableLiveData(arrayListOf())
    var dueDate : MutableLiveData<Date?> = MutableLiveData(null)
    var pickDateClickedEvent : SingleLiveEvent<Any> = SingleLiveEvent()
    var selectedAccount : TaskMember? = null
    private var label : String? = null

    val dueDateStr : LiveData<String> = Transformations.map(dueDate) { newDate ->
        if (newDate == null) {
            "Без срока"
        } else {
            val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            "Срок: ${simpleDateFormat.format(newDate)}"
        }
    }

    val removeDueDateButtonVisible : LiveData<Int> = Transformations.map(dueDate) {
        if (it != null)
            View.VISIBLE
        else
            View.GONE
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
    }

    fun getEditedTask(): Task {
        return Task(
            id ?: UUID.randomUUID(),
            isCompleted.value!!,
            taskName,
            description,
            dueDate.value,
            members.value!!,
            label)
    }

    fun onPickDateClicked() = pickDateClickedEvent.call()

    fun removeDueDate() { dueDate.value = null }

    fun switchIsCompletedState() { isCompleted.value = !isCompleted.value!! }

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
}