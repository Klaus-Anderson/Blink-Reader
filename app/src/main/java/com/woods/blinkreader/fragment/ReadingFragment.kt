package com.woods.blinkreader.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.woods.blinkreader.R
import com.woods.blinkreader.databinding.FragmentReadingBinding
import com.woods.blinkreader.viewmodel.BlinkReaderViewModel

class ReadingFragment : Fragment() {
    private val blinkReaderViewModel: BlinkReaderViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val toReturnView = inflater.inflate(R.layout.fragment_reading, container, false)
        val fragmentReadingBinding: FragmentReadingBinding? = DataBindingUtil.bind(toReturnView)
        fragmentReadingBinding?.lifecycleOwner = this
        fragmentReadingBinding?.readingViewModel = blinkReaderViewModel
        fragmentReadingBinding?.context = activity
        return toReturnView
    }
}