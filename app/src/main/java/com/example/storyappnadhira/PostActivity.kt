package com.example.storyappnadhira

import android.content.Intent
import android.Manifest
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.storyappnadhira.api.ApiConfig
import com.example.storyappnadhira.api.FileUploadResponse
import com.example.storyappnadhira.api.StoryAPI
import com.example.storyappnadhira.databinding.ActivityPostBinding
import com.example.storyappnadhira.utils.PreferencesHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import reduceFileImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rotateFile
import uriToFile
import java.io.File

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding
    private var getFile: File? = null
    private lateinit var storyApi: StoryAPI
    private lateinit var preferencesHelper: PreferencesHelper

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "No Permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storyApi = ApiConfig.getApiService()

        preferencesHelper = PreferencesHelper(this)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        val imagePath = intent.getStringExtra("imageFile")
        if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.previewImageView.setImageBitmap(bitmap)
            }
        }


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.cameraXButton.setOnClickListener { startCameraX() }
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.uploadButton.setOnClickListener { uploadImage() }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun startCameraX() {
        if (allPermissionsGranted()) {
            val intent = Intent(this, CameraActivity::class.java)
            launcherIntentCameraX.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as? File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            myFile?.let { file ->
                rotateFile(file, isBackCamera)
                getFile = file
                binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@PostActivity)
                getFile = myFile
                binding.previewImageView.setImageURI(uri)
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }


    private fun uploadImage() {
        val token = "Bearer ${preferencesHelper.token}"
        val description = binding.descriptionEditText.text

        if (getFile != null && !description.isNullOrEmpty()) {
            val file = reduceFileImage(getFile as File)
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
            val description = binding.descriptionEditText.text.toString().toRequestBody("text/plain".toMediaType())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )
            val uploadImageRequest = storyApi.uploadImage(imageMultipart, description, token)
            uploadImageRequest.enqueue(object : Callback<FileUploadResponse> {
                override fun onResponse(
                    call: Call<FileUploadResponse>,
                    response: Response<FileUploadResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error) {
                            Toast.makeText(this@PostActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@PostActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@PostActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                    Toast.makeText(this@PostActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }else if(getFile != null){
            Toast.makeText(this@PostActivity, "Silakan masukkan deskripsi gambar.", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this@PostActivity, "Silakan masukkan berkas gambar terlebih dahulu.", Toast.LENGTH_SHORT).show()
        }
    }
}