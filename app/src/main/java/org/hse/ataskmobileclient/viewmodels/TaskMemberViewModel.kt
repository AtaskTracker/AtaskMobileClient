package org.hse.ataskmobileclient.viewmodels

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import org.hse.ataskmobileclient.models.TaskMember


class TaskMemberViewModel(taskMember: TaskMember)
{
    val username = taskMember.email
    val photoUrl = taskMember.photoUrl

    companion object {
        @JvmStatic
        @BindingAdapter("imageUrl")
        fun loadImage(view: ImageView, imageUrl: String?) {
            if (imageUrl != null && imageUrl.isNotEmpty())
                Glide.with(view.context).load(imageUrl).into(view)
        }
    }
}