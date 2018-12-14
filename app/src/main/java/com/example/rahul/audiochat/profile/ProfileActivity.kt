package com.example.rahul.audiochat.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.example.rahul.audiochat.R
import com.example.rahul.audiochat.util.FirestoreUtil
import com.example.rahul.audiochat.util.StorageUtil
import com.example.rahul.audiochat.shared_prefences.PrefManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.profile_activity.*
import java.io.ByteArrayOutputStream
import com.bumptech.glide.request.RequestOptions
import com.example.rahul.audiochat.glide.ImageRequestListener
import com.example.rahul.audiochat.home.HomeActivity
import com.google.android.gms.tasks.OnSuccessListener
import org.jetbrains.anko.*


class ProfileActivity : AppCompatActivity(){

    lateinit var mAuth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    private val userInfo = mutableMapOf<String, String>()
    lateinit var userName:String
    lateinit var userPhone:String
    lateinit var userStatus:String
    lateinit var prefManager:PrefManager
    lateinit var mCropImageUri:Uri

    private val RC_SELECT_IMAGE = 2
    var selectedImageBytes:ByteArray? = null
    private var pictureJustChanged = false

    val requestOptions = RequestOptions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        prefManager = PrefManager(this)

        requestOptions.placeholder(R.drawable.ic_action_profile)

        progress_submit.isEnabled=false
        progress_submit.visibility=View.INVISIBLE

        edit_image.setOnClickListener {
            val intent = Intent().apply {
                type="image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg","image/png"))
            }
            startActivityForResult(Intent.createChooser(intent,"Select Image"), RC_SELECT_IMAGE)
        }

        btn_submit_profile.setOnClickListener {

            userName = et_name.text.toString()
            userStatus = et_status.text.toString()
            userPhone = prefManager.getString(PrefManager.USER_PHONE_NO)

            if (userPhone.isNotBlank()){
                if (userName.isNotBlank()){
                    if (userStatus.isBlank()){
                        userStatus = getString(R.string.default_status)
                    }
                    if (selectedImageBytes != null){
                        progress_submit.isEnabled=true
                        progress_submit.visibility=View.VISIBLE
                        StorageUtil.uploadProfilePhoto(selectedImageBytes!!){ imagePath ->
                             FirestoreUtil.updateCurrentUser(userName, userStatus,
                                    prefManager.getString(PrefManager.USER_PHONE_NO), imagePath)
                            Log.e("UPLOAD",userName+"\n"+userStatus+"\n"+userPhone+"\n"+imagePath)
                            if (progress_submit.visibility==View.VISIBLE){
                                progress_submit.isEnabled=false
                                progress_submit.visibility = View.INVISIBLE
                                toast("profile updated")
                                startActivity(intentFor<HomeActivity>().newTask().clearTask())
                            }
                        }
                    }else{
                        FirestoreUtil.updateCurrentUser(userName, userStatus,
                                prefManager.getString(PrefManager.USER_PHONE_NO), null)
                    }
                }else{
                    et_name.error = getString(R.string.please_enter_user_name)
                }
            }else{
                toast(getString(R.string.something_went_wrong))
            }

        }

    }

    private fun onSelectImageClick(){
        CropImage.startPickImageActivity(this)
    }



    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent) {

        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data.data != null){
            Log.d("debug","RC_SELECT_IMAGE")
            val selectedImagePath = data.data
            val selectedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream)
            selectedImageBytes = outputStream.toByteArray()

            Glide.with(this)
                    .load(selectedImageBytes)
                    .apply(requestOptions)
                    .into(profile_image)

            pictureJustChanged = true
        }

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.d("debug","PICK_IMAGE_CHOOSER_REQUEST_CODE")
            val imageUri = CropImage.getPickImageResultUri(this, data)
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri
                requestPermissions(arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            }
            else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri)
            }
        }
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.d("debug","CROP_IMAGE_ACTIVITY_REQUEST_CODE")
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                Glide.with(this)
                        .load(result.uri)
                        .into(profile_image)
                toast("Cropping successful, Sample: " + result.getSampleSize())
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                toast("Cropping failed: " + result.error.toString())
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<String>, grantResults:IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug","PERMISSION_GRANTED_CROP_ACTIVITY")
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri)
        }
        else {
            toast("Cancelling, required permissions are not granted")
        }
    }
    /**
     * Start crop image activity for the given image.
     */
    private fun startCropImageActivity(imageUri: Uri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this)
    }

    override fun onStart() {
        super.onStart()
        FirestoreUtil.getCurrentUser { user ->
            et_name.setText(user.name)
            et_status.setText(user.status)

            if (!pictureJustChanged && user.profilePicture!=null){
                Glide.with(this)
                        .load(StorageUtil.pathToReference(user.profilePicture))
                        .apply(requestOptions)
                        .into(profile_image)

            }
        }
    }
}
