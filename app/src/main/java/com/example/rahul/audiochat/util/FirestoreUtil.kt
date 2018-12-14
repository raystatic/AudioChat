package com.example.rahul.audiochat.util

import com.example.rahul.audiochat.profile.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Created by rahul on 29/11/18.
 */
object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currendUserDocRef: DocumentReference
            get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().uid
                ?: throw NullPointerException("UID is null")}")

    fun initCurrentUserIfFirstTime(onComplete: () -> Unit){
        currendUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()){
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                        "","", null)
                currendUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }else{
                onComplete()
            }
        }
    }

    fun updateCurrentUser(name:String = "", status:String = "", phone:String = "", profilePicture: String?= null){
        val userInfo = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userInfo["name"] = name
        if (status.isNotBlank()) userInfo["status"] = status
        if (phone.isNotBlank()) userInfo["phone"] = phone
        if (profilePicture !=null){
            userInfo["profile_picture"]
        }
    }

    fun getCurrentUser(onComplete: (User) -> Unit){
        currendUserDocRef.get()
                .addOnSuccessListener {
                    onComplete(it.toObject(User::class.java)!!)
                }
    }
}