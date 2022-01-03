package com.woods.blinkreader.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.woods.blinkreader.R
import com.woods.blinkreader.databinding.FragmentReadingSeekbarBindingImpl
import com.woods.blinkreader.viewmodel.BlinkReaderViewModel

@Suppress("unused")
class ReadingSeekbarFragment : Fragment() {
    lateinit var blinkReaderViewModel: BlinkReaderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val toReturnView = inflater.inflate(R.layout.fragment_reading_seekbar, container, false)
        blinkReaderViewModel = ViewModelProvider(
            viewModelStore,
            BlinkReaderViewModel.BlinkReaderViewModelFactory(activity!!.application)
        )[BlinkReaderViewModel.implClass]

        val fragmentReadingSeekbarBinding: FragmentReadingSeekbarBindingImpl? = DataBindingUtil.bind(toReturnView)
        fragmentReadingSeekbarBinding?.lifecycleOwner = this
        fragmentReadingSeekbarBinding?.readingViewModel = blinkReaderViewModel
        return toReturnView
    }
}