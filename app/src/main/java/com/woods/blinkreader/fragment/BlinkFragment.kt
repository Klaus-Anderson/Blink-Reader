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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_blink, container, false)?.also {
                DataBindingUtil.bind<FragmentBlinkBindingImpl>(it)?.apply {
                    lifecycleOwner = this@BlinkFragment
                    readingViewModel = ViewModelProvider(
                        requireActivity()
                    )[BlinkReaderViewModel.implClass]
                }
            }
    }
}