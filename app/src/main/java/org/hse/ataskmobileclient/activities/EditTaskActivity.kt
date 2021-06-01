package org.hse.ataskmobileclient.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import org.hse.ataskmobileclient.EditTaskResult
import org.hse.ataskmobileclient.EditTaskStatusCode
import org.hse.ataskmobileclient.MockData
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.databinding.ActivityEditTaskBinding
import org.hse.ataskmobileclient.fragments.DatePickerFragment
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

        viewModel.pickDateClickedEvent.observe(this, {
            val datePickerFragment = DatePickerFragment { viewModel.dueDate.value = it }
            datePickerFragment.show(supportFragmentManager, "datePicker")
        })

        viewModel.selectPictureClickedEvent.observe(this, {
            requestTaskPhotoFromUser()
        })

        binding.btnDeleteTask.setOnClickListener { askDeleteTaskConfirmation() }

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

        viewModel.onShowUserNotFoundEvent.observe(this, {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show()
        })

        viewModel.onUserAlreadyAddedEvent.observe(this, {
            Toast.makeText(this, "Такой пользователь уже добавлен", Toast.LENGTH_SHORT).show()
        })

        viewModel.onPickLabelClickedEvent.observe(this, {
            pickLabelViaDialog()
        })
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
        val memberOptions = MockData.AvailableTaskMembers.map { it.email }
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            memberOptions)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    private fun requestTaskPhotoFromUser() {
        val options = arrayOf<CharSequence>(
            getString(R.string.action_take_photo),
            getString(R.string.action_pick_photo_from_gallery),
            getString(R.string.not_set),
        )

        val builder = AlertDialog.Builder(this@EditTaskActivity)
        builder.setTitle(R.string.title_pick_photo_for_task)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> askUserToTakePhoto()
                    1 -> askUserToSelectPhotoFromGallery()
                    2 -> viewModel.taskPicture.value = null
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

    private fun askDeleteTaskConfirmation() {
        val confirmationDialog =
            AlertDialog.Builder(this@EditTaskActivity)
                .setMessage("Are you sure you want to delete current task?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ -> this.deleteTaskAndFinishActivity() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .create()

        confirmationDialog.show()
    }

    private fun deleteTaskAndFinishActivity() {
        val oldTask = oldTask ?: throw UnknownError("oldTask was null, so delete action is not allowed")
        val editTaskResult = EditTaskResult(EditTaskStatusCode.DELETE, oldTask)
        val editTaskResultJson = gson.toJson(editTaskResult)

        val intent = Intent().apply {
            putExtra(EDIT_TASK_RESULT_JSON, editTaskResultJson)
        }

        setResult(RESULT_OK, intent)
        finish()
    }

    private fun finishEditingWithoutSaving() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun saveResultsAndFinish() {
        val task = viewModel.getEditedTask()
        if (viewModel.isLabelNew)
            viewModel.saveLabel()

        val statusCode =
            if (oldTask == null) EditTaskStatusCode.ADD
            else EditTaskStatusCode.UPDATE

        val editTaskResult = EditTaskResult(statusCode, task)
        val editTaskResultJson = gson.toJson(editTaskResult)

        val intent = Intent().apply {
            putExtra(EDIT_TASK_RESULT_JSON, editTaskResultJson)

        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun pickLabelViaDialog() {
        val availableLabels = viewModel.availableLabels.toTypedArray()
        val dialog = AlertDialog.Builder(this)
            .setItems(availableLabels) { _, position ->
                viewModel.taskLabel.value = availableLabels[position]
            }
            .setPositiveButton("Новый") { _, _ ->
                askUserForNewLabel()
            }
            .setNeutralButton("Очистить") { _, _ ->
                viewModel.taskLabel.value = null
            }
            .setNegativeButton("Отмена") { _, _ -> }
            .create()

        dialog.show()
    }

    private fun askUserForNewLabel() {

        val input = EditText(this@EditTaskActivity)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        val alertDialog = AlertDialog
            .Builder(this)
            .setView(input)
            .setPositiveButton("ОК") { _, _ ->
                viewModel.taskLabel.value = input.text.toString()
            }
            .create()

        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = false
        input.addTextChangedListener {
            positiveButton.isEnabled = it.toString().isNotEmpty()
        }

        alertDialog.show()
    }

    companion object {
        const val TASK_JSON = "TASK_JSON"
        const val EDIT_TASK_RESULT_JSON = "TASK_RESULT_JSON"
        const val TAG = "EditTaskActivity"
        const val CAMERA_PERMISSION_CODE = 1
        const val REQUEST_IMAGE_CAPTURE = 2
        const val READ_EXTERNAL_STORAGE_CODE = 3
        const val REQUEST_PHOTO_FROM_STORAGE = 4
    }
}
