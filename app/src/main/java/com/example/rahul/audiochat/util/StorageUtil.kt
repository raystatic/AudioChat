package com.example.rahul.audiochat.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

/**
 * Created by rahul on 29/11/18.
 */
object StorageUtil {
    private val storageInstance:FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val currentUserRef: StorageReference
        get() = storageInstance.reference
                .child(FirebaseAuth.getInstance().uid ?:
                    throw NullPointerException("UID is null"))
    fun uploadProfilePhoto(imageBytes: ByteArray,
                           onSuccess: (imagePath:String) -> Unit){
        val ref = currentUserRef.child("profilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes)
                .addOnSuccessListener {
                    onSuccess(ref.path)
                }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)
}