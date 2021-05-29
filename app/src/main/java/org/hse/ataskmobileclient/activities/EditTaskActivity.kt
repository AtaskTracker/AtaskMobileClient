package org.hse.ataskmobileclient.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import org.hse.ataskmobileclient.MockData
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.databinding.ActivityEditTaskBinding
import org.hse.ataskmobileclient.itemadapters.OnItemRemoveClick
import org.hse.ataskmobileclient.itemadapters.TaskMembersAdapter
import org.hse.ataskmobileclient.models.Task
import org.hse.ataskmobileclient.viewmodels.EditTaskViewModel
import java.util.*


class EditTaskActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(EditTaskViewModel::class.java) }

    private val binding : ActivityEditTaskBinding by lazy {
        val binding : ActivityEditTaskBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_edit_task)

        binding.lifecycleOwner = this@EditTaskActivity
        binding.viewmodel = viewModel
        binding
    }

    private lateinit var taskMembersAdapter : TaskMembersAdapter
    private var oldTask : Task? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskJson = intent.getStringExtra(TASK_JSON)
        if (taskJson != null) {
            oldTask = gson.fromJson(taskJson, Task::class.java)
            binding.viewmodel!!.initializeFromTask(oldTask!!)
        }

        val myCalendar = Calendar.getInstance()
        val onDateSetListener =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewModel.dueDate.value = myCalendar.time
            }

        viewModel.pickDateClickedEvent.observe(this,
            {
                val calendar = Calendar.getInstance()
                if (viewModel.dueDate.value != null) {
                    calendar.time = viewModel.dueDate.value!!
                }

                DatePickerDialog(
                    this@EditTaskActivity, onDateSetListener,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            })

        viewModel.selectPictureClickedEvent.observe(this, {
            requestTaskPhotoFromUser()
        })

        taskMembersAdapter = TaskMembersAdapter(
            object : OnItemRemoveClick {
                override fun onClick(position: Int) {
                    viewModel.removeMemberAt(position)
                }
            })

        viewModel.members.observe(this, {
            it?.let {
                taskMembersAdapter.submitList(ArrayList(it))
            }
        })

        binding.taskMembersList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.taskMembersList.adapter = taskMembersAdapter

        binding.backButton.setOnClickListener { finishEditingWithoutSaving() }
        binding.btnSave.setOnClickListener { saveResultsAndFinish() }

        initAddMembersSpinner()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
            else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val taskPicture = data?.extras?.get("data") as Bitmap
            viewModel.taskPicture.value = taskPicture
        }
        else if (requestCode == REQUEST_PHOTO_FROM_STORAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data ?: return
            viewModel.taskPicture.value = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }
    }

    private fun initAddMembersSpinner() {
        val memberOptions = MockData.AvailableTaskMembers.map { it.username }
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            memberOptions)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.etMemberNameSpinner.adapter = adapter
        binding.etMemberNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.selectedAccount = null
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int, id: Long)
            {
                viewModel.selectedAccount = MockData.AvailableTaskMembers[position]
            }
        }
    }

    private fun requestTaskPhotoFromUser() {
        val options = arrayOf<CharSequence>(
            getString(R.string.action_take_photo),
            getString(R.string.action_pick_photo_from_gallery)
        )

        val builder = AlertDialog.Builder(this@EditTaskActivity)
        builder.setTitle(R.string.title_pick_photo_for_task)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> askUserToTakePhoto()
                    1 -> askUserToSelectPhotoFromGallery()
                    else -> throw NotImplementedError()
                }
            }
        builder.create()

        builder.show()
    }

    private fun askUserToTakePhoto() {
        val permissionResult = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)

        if (permissionResult == PackageManager.PERMISSION_DENIED){
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                Toast.makeText(this,
                    "You need to grant access to camera to take a photo",
                    Toast.LENGTH_LONG).show()
            }

            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            return
        }

        if (permissionResult != PackageManager.PERMISSION_GRANTED)
            return

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun askUserToSelectPhotoFromGallery() {
        val permissionResult = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permissionResult == PackageManager.PERMISSION_DENIED){
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(this,
                    "You need to grant access to external storage to select a photo from gallery",
                    Toast.LENGTH_LONG).show()
            }

            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_CODE)
            return
        }

        if (permissionResult != PackageManager.PERMISSION_GRANTED)
            return

        val pickPhotoFromGallery = Intent(Intent.ACTION_PICK)
        pickPhotoFromGallery.type = "image/*"
        startActivityForResult(pickPhotoFromGallery, REQUEST_PHOTO_FROM_STORAGE)
    }

    private fun finishEditingWithoutSaving() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun saveResultsAndFinish() {
        val task = viewModel.getEditedTask()
        val taskJson = gson.toJson(task)

        val intent = Intent().apply {
            putExtra(TASK_RESULT_JSON, taskJson)

        }
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val TASK_JSON = "TASK_JSON"
        const val TASK_RESULT_JSON = "TASK_RESULT_JSON"
        const val TAG = "EditTaskActivity"
        const val CAMERA_PERMISSION_CODE = 1
        const val REQUEST_IMAGE_CAPTURE = 2
        const val READ_EXTERNAL_STORAGE_CODE = 3
        const val REQUEST_PHOTO_FROM_STORAGE = 4
    }
}
