package org.hse.ataskmobileclient

import org.hse.ataskmobileclient.models.*
import org.hse.ataskmobileclient.viewmodels.TaskListItem
import org.hse.ataskmobileclient.viewmodels.TasksHeader
import java.util.*
import kotlin.collections.ArrayList

class MockData {
    companion object {

        val AvailableTaskMembers : ArrayList<TaskMember> = generateTaskMembers()

        private fun generateTaskMembers() : ArrayList<TaskMember> {
            return arrayListOf(
                    TaskMember(
                        "pavlov@gmail.com",
                        "https://i.natgeofe.com/n/4f5aaece-3300-41a4-b2a8-ed2708a0a27c/domestic-dog_thumb_square.jpg?w=136&h=136"),
                    TaskMember(
                        "petrov@gmail.com",
                        "https://www.dogstrust.org.uk/help-advice/_images/164742v800_puppy-1.jpg"),
                    TaskMember(
                        "ivanov@gmail.com",
                        "https://i.guim.co.uk/img/media/7a633730f5f90db3c12f6efc954a2d5b475c3d4a/0_138_5544_3327/master/5544.jpg?width=1200&height=1200&quality=85&auto=format&fit=crop&s=27c09d27ccbd139fd0f7d1cef8f7d41d"),
                    TaskMember(
                        "alekseev@gmail.com",
                        "https://media.wired.com/photos/5e1e646743940d0008009167/master/w_2560%2Cc_limit/Science_Cats-84873657.jpg"),
            )
        }

    }
}