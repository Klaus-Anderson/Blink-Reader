package com.woods.blinkreader.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.woods.blinkreader.R
import com.woods.blinkreader.databinding.FragmentBlinkBindingImpl
import com.woods.blinkreader.viewmodel.BlinkReaderViewModel

class BlinkFragment : Fragment() {
    lateinit var blinkReaderViewModel: BlinkReaderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val toReturnView = inflater.inflate(R.layout.fragment_blink, container, false)

        blinkReaderViewModel = ViewModelProvider(
            viewModelStore,
            BlinkReaderViewModel.BlinkReaderViewModelFactory(activity!!.application)
        )[BlinkReaderViewModel.implClass]

        val fragmentBlinkBinding: FragmentBlinkBindingImpl? = DataBindingUtil.bind(toReturnView)
        fragmentBlinkBinding?.lifecycleOwner = this
        fragmentBlinkBinding?.readingViewModel = blinkReaderViewModel
        return toReturnView
    }
}