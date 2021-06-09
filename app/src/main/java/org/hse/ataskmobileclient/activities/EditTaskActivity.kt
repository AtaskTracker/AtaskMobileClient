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
            val currentTask = gson.fromJson(taskJson, Task::class.java)
            binding.viewmodel!!.initializeFromTask(currentTask!!)
            oldTask = currentTask
        }

        viewModel.pickDateClickedEvent.observe(this, {
            val datePickerFragment = DatePickerFragment(viewModel.dueDate.value) { newDate ->
                viewModel.dueDate.value = newDate
            }

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

        viewModel.onUserNotFoundEvent.observe(this, {
            Toast.makeText(this, getString(R.string.user_with_email_not_found), Toast.LENGTH_SHORT).show()
        })

        viewModel.onUserAlreadyAddedEvent.observe(this, {
            Toast.makeText(this, getString(R.string.user_already_added_to_task), Toast.LENGTH_SHORT).show()
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
        else if (requestCode == READ_EXTERNAL_STORAGE_CODE) {
            val pickPhotoFromGallery = Intent(Intent.ACTION_PICK)
            pickPhotoFromGallery.type = "image/*"
            startActivityForResult(pickPhotoFromGallery, REQUEST_PHOTO_FROM_STORAGE)
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
                    getString(R.string.need_to_grant_camera_access),
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
                    getString(R.string.need_to_grant_gallery_access),
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
                .setMessage(getString(R.string.are_you_sure_you_want_to_delete_task))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes_option)) { _, _ -> this.deleteTaskAndFinishActivity() }
                .setNegativeButton(getString(R.string.no_option)) { dialog, _ -> dialog.dismiss() }
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
            .setTitle(getString(R.string.choose_label_for_task))
            .setItems(availableLabels) { _, position ->
                viewModel.taskLabel.value = availableLabels[position]
            }
            .setPositiveButton(getString(R.string.new_task_label)) { _, _ ->
                askUserForNewLabel()
            }
            .setNeutralButton(getString(R.string.clear_task_label)) { _, _ ->
                viewModel.taskLabel.value = null
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()

        dialog.show()
    }

    private fun askUserForNewLabel() {

        val newLabelInput = EditText(this@EditTaskActivity)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        newLabelInput.layoutParams = lp
        val alertDialog = AlertDialog
            .Builder(this)
            .setView(newLabelInput)
            .setPositiveButton(getString(R.string.ok_option)) { _, _ ->
                viewModel.taskLabel.value = newLabelInput.text.toString()
            }
            .create()

        alertDialog.show()

        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = false
        newLabelInput.addTextChangedListener { newLabel ->
            positiveButton.isEnabled = newLabel.toString().isNotEmpty()
        }

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
