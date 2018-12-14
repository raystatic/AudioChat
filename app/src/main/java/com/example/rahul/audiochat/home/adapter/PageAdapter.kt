package com.example.rahul.audiochat.home.adapter


import android.app.FragmentManager
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.example.rahul.audiochat.R
import com.example.rahul.audiochat.home.fragments.ContactsFragment
import com.example.rahul.audiochat.home.fragments.FavFragment
import com.example.rahul.audiochat.home.fragments.MainFragment

/**
 * Created by rahul on 14/12/18.
 */
class PageAdapter(fm: android.support.v4.app.FragmentManager, private val context:Context): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> return MainFragment.newInstance()
            1 -> return FavFragment.newInstance()
            2 -> return ContactsFragment.newInstance()
        }

        return MainFragment.newInstance()
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position){
            0 -> return context.getString(R.string.tab1_title)
            1 -> return context.getString(R.string.tab2_title)
            2 -> return context.getString(R.string.tab3_title)
        }

        return null
    }
}