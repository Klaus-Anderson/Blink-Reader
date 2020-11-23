package com.woods.blinkreader.fragment

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.woods.blinkreader.R
import com.woods.blinkreader.databinding.FragmentReadingBinding
import com.woods.blinkreader.viewmodel.ReadingViewModel

class ReadingFragment : Fragment() {
    private var readingViewModel: ReadingViewModel? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        readingViewModel = ViewModelProviders.of(activity!!)
                .get(ReadingViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val toReturnView = inflater.inflate(R.layout.fragment_reading, container, false)
        val fragmentReadingBinding: FragmentReadingBinding = DataBindingUtil.bind(toReturnView)!!
        fragmentReadingBinding.lifecycleOwner = this
        fragmentReadingBinding.readingViewModel = readingViewModel
        fragmentReadingBinding.context = activity
        return toReturnView
    }
}