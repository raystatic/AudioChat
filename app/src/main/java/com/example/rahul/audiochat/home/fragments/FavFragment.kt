package com.example.rahul.audiochat.home.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.rahul.audiochat.R


/**
 * A simple [Fragment] subclass.
 */
class FavFragment : Fragment() {

    lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_fav, container, false)


        return rootView
    }

    companion object {
        fun newInstance(): FavFragment {
            val fragment = FavFragment()
            return fragment
        }
    }

}// Required empty public constructor
