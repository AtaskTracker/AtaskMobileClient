package org.hse.ataskmobileclient.viewmodels

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.services.ITasksService

class MainViewModel(
//    private val tasksService: ITasksService
    )
    : ViewModel()
{
    var deadlineTasks : MutableLiveData<ArrayList<Task>> = MutableLiveData()
    var backlogTasks : MutableLiveData<ArrayList<Task>> = MutableLiveData()
    var userName : String = ""
    var photoUrl : String = ""

    fun reloadTasks() {
//        viewModelScope.launch {
//            deadlineTasks.value = ArrayList(tasksService.getTasksForWeekAsync())
//            backlogTasks.value = ArrayList(tasksService.getBacklogTasksAsync())
//        }
    }

    companion object {
        @JvmStatic
        @BindingAdapter("imageUrl")
        fun loadImage(view: ImageView, imageUrl: String?) {
            if (imageUrl != null && imageUrl.isNotEmpty())
                Glide.with(view.context).load(imageUrl).into(view)
        }
    }
}