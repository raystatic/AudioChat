package com.example.rahul.audiochat.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Telephony
import android.support.v4.content.ContextCompat
import android.telephony.SmsMessage
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.rahul.audiochat.R
import com.example.rahul.audiochat.home.HomeActivity
import com.example.rahul.audiochat.profile.ProfileActivity
import com.example.rahul.audiochat.shared_prefences.PrefManager
import com.example.rahul.audiochat.util.FirestoreUtil
import com.example.rahul.audiochat.utils.Otpreciver
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_otp2.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {

    lateinit var phoneNo:String
    lateinit var mAuth:FirebaseAuth
    var isAllowedToRead:Boolean=false
    lateinit var intentFilter: IntentFilter
    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mVerificationId:String=""
    private var mVerificationProgress=false
    lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken
    var mtimer: CountDownTimer? = null
    lateinit var messageotp:String
    lateinit var otpmessage:String
    lateinit var reciever: Otpreciver
    lateinit var pdus: Array<Any>
    private var ISSIGNEDIN : Boolean = false
    private lateinit var db:FirebaseFirestore
    private lateinit var prefManager:PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp2)

        val i =intent
        phoneNo=i.getStringExtra("PHONENO")

        otp_sent_to_number.text=getString(R.string.we_have_sent_otp_to)+"${phoneNo}"

        mAuth= FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        prefManager= PrefManager(this)

        if(!checkPermission()){
            requestPermission()
        }

        intentFilter= IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        intentFilter.priority=100

        mCallbacks=object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onCodeSent(p0: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                mVerificationId= p0!!
                mResendToken= p1!!
                mVerificationProgress=true
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
                signInWithPhoneAuthCredential(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                Log.d("Verification FAILED",p0.toString())
                if (p0 is FirebaseTooManyRequestsException) {
                    toast(getString(R.string.quota_exceeded))
                    mVerificationProgress = false

                }
            }
        }
        status_card.isEnabled=false
        status_card.setOnClickListener{
            settimer()
            toast(getString(R.string.otp_has_been_sent))
            resendVerificationCode(phoneNo,mResendToken)
            status_card.isEnabled=false
        }

        phoneAuth(phoneNo)
        settimer()
        verifyOtp()

        btn_verify.setOnClickListener {
            if(!TextUtils.isEmpty(et_otp.text)){
                verifyPhoneWithCode(mVerificationId,et_otp.text.toString())
            }else{
                et_otp.error = getString(R.string.otp_cannot_be_empty)
            }
        }

//        backButton.setOnClickListener{
//            finish()
//        }


    }

    private fun verifyPhoneWithCode(mVerificationId: String, text: String) {
        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(mVerificationId,text))
    }

    private fun phoneAuth(phoneNo: String?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNo!!,  //phone number
                60, // time to wait
                TimeUnit.SECONDS, // unit of time
                this, //context
                mCallbacks //callback on verification changed
        )
    }

    private fun verifyOtp() {
        reciever=object : Otpreciver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                super.onReceive(p0, p1)
                val data = p1?.extras
                if (data != null) {
                    try {
                        pdus = data.get("pdus") as Array<Any>
                    } catch (e: Exception) {
                    }
                }
                for (pdu in pdus) { // loop through and pick up the SMS of interest
                    val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                    val abcd = smsMessage.messageBody.replace("[^0-9]", "")
                    messageotp = abcd
                    otpmessage=messageotp.split(" ")[0]
                }
                Toast.makeText(this@OtpActivity,messageotp,Toast.LENGTH_SHORT).show()
                Log.d("OTP","MESSAGE "+messageotp.split(" ")[0])
                et_otp.setText(otpmessage)
                mtimer!!.cancel()
                tv_status.text="OTP Received"
                status_card.isEnabled=false
            }
        }
        this@OtpActivity.registerReceiver(reciever,intentFilter)
    }

    private fun resendVerificationCode(phoneNumber: String,
                                       token: PhoneAuthProvider.ForceResendingToken) {
        toast(getString(R.string.otp_request_sent))
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this, // Activity (for callback binding)
                mCallbacks, // OnVerificationStateChangedCallbacks
                token)             // ForceResendingToken from callbacks
    }

    private fun checkPermission() : Boolean{
        val readSMS= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        isAllowedToRead=readSMS== PackageManager.PERMISSION_GRANTED
        return isAllowedToRead
    }

    private fun requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_SMS), 2501)
        }
    }

    private fun signInWithPhoneAuthCredential(p0: PhoneAuthCredential?) {
        mAuth.signInWithCredential(p0!!)
                .addOnCompleteListener({
                    if (it.isSuccessful){
                        prefManager.saveString(PrefManager.USER_UID, mAuth.uid!!)
                        db.collection(getString(R.string.users)).document(mAuth.uid!!)
                                .get()
                                .addOnCompleteListener({
                                    if(it.result!!.exists()){
                                        tv_status.text=getString(R.string.otp_verified)
                                        status_card.isEnabled=false
                                        //showToast(getString(R.string.user_exist))
                                        startActivity(intentFor<HomeActivity>().newTask().clearTask())
                                    }else{
                                        FirestoreUtil.initCurrentUserIfFirstTime {
                                            startActivity(intentFor<ProfileActivity>().newTask().clearTask())
                                        }
                                    }
                                })
                        mtimer!!.cancel()
                        prefManager.saveString(PrefManager.USER_PHONE_NO,phoneNo)
                        tv_status.text = getString(R.string.sign_in_success)
                    }else{
                        if (it.exception is FirebaseAuthInvalidCredentialsException){
                            mtimer!!.cancel()
                            tv_status.text = getString(R.string.sign_in_failure)
                        }
                    }
                })
    }

    private fun settimer() {
        mtimer = object : CountDownTimer(90000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                tv_status.text="Resend (" + String.format("%02d", seconds) + " sec)"
            }

            override fun onFinish() {
                tv_status.text=getString(R.string.resend_otp)
                status_card.isEnabled=true
            }
        }.start()

    }

    override fun onDestroy() {
        super.onDestroy()
        mtimer?.cancel()
        this@OtpActivity.unregisterReceiver(reciever)

    }
}
