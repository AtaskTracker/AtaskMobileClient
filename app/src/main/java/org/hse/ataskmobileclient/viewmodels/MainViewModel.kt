package org.hse.ataskmobileclient.viewmodels

import android.view.MenuItem
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.hse.ataskmobileclient.MockData
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.models.TaskListItem


class MainViewModel : ViewModel() {
    val currentTasks : MutableLiveData<ArrayList<TaskListItem>> = MutableLiveData(MockData.DeadlineTasks)
    var userName : String = ""
    var photoUrl : String = ""

    fun onNavigationItemSelected(item: MenuItem) : Boolean {
        when (item.itemId){
            R.id.navigation_tasks_with_deadline -> {
                MockData.BacklogTasks = currentTasks.value ?: arrayListOf()
                currentTasks.value = MockData.DeadlineTasks
                return true
            }
            R.id.navigation_backlog_tasks -> {
                MockData.DeadlineTasks = currentTasks.value ?: arrayListOf()
                currentTasks.value = MockData.BacklogTasks
                return true
            }
        }
        return false
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