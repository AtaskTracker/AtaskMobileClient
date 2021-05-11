package org.hse.ataskmobileclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hse.ataskmobileclient.itemadapters.OnListItemClick
import org.hse.ataskmobileclient.itemadapters.TaskAdapter
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel : MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel = MainViewModel()

        val tasksAdapter = TaskAdapter(MockData.Tasks, object : OnListItemClick {
            override fun onClick(view: View, position: Int) {
                val intent = Intent(view.context, EditTaskActivity::class.java)
                startActivity(intent)
            }
        })
        val recyclerView = findViewById<RecyclerView>(R.id.rv_tasks_list)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = tasksAdapter

        initAppBar()

        val ivLogout = findViewById<ImageView>(R.id.iv_main_logout)
        ivLogout.setOnClickListener { logout() }
    }

    private fun initAppBar() {
        val fullName = intent.getStringExtra(FULL_NAME_EXTRA)
        val appbarUsername = findViewById<TextView>(R.id.appbar_username)
        appbarUsername.text = fullName

//        val profilePicture = findViewById<ImageView>(R.id.profile_picture)
//        viewModel.profilePicture.observe(this, {
//            try
//            {
//                profilePicture.setImageBitmap(it)
//            }
//            catch (ex : Exception){
//                Log.e(TAG, ex.toString())
//            }
//        })
//
//        val profilePhotoUrl = intent.getStringExtra(PHOTO_URL_EXTRA)!!
//        viewModel.loadProfilePicture(profilePhotoUrl)
    }

    private fun logout(){
        Log.i(TAG, "Logout clicked")
        finish()
    }

    companion object {
        const val FULL_NAME_EXTRA = "Username"
        const val PHOTO_URL_EXTRA = "ProfilePictureUrl"

        private const val TAG = "MainActivity"
    }
}