package com.example.rahul.audiochat.login

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.example.rahul.audiochat.R
import com.example.rahul.audiochat.profile.ProfileActivity
import com.example.rahul.audiochat.shared_prefences.PrefManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {

    lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //startActivity<ProfileActivity>()
        prefManager= PrefManager(this)
        if (FirebaseAuth.getInstance()==null){
            toast("user does not exist")
        }

        btn_send_otp.setOnClickListener {
            if (!TextUtils.isEmpty(et_phone.text) && et_phone.text.length==10){
                startActivity<OtpActivity>("PHONENO" to "+91${et_phone.text}")
            }else{
                et_phone.error = getString(R.string.invalid_phone_number)
            }
        }

    }
}
