package com.example.rahul.audiochat.home

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import com.example.rahul.audiochat.R
import com.example.rahul.audiochat.home.adapter.PageAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.toast

class HomeActivity : AppCompatActivity() {

    var pagerAdapter: PageAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (FirebaseAuth.getInstance().currentUser==null){
            startActivity(intentFor<HomeActivity>().newTask().clearTask())
        }

        pagerAdapter = PageAdapter(supportFragmentManager,this)
        viewPager.adapter = pagerAdapter

        tabs.setupWithViewPager(viewPager)
    }
}
