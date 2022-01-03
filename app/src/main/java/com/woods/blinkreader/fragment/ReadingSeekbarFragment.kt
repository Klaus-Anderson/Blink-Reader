package com.woods.blinkreader.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.woods.blinkreader.R
import com.woods.blinkreader.databinding.FragmentReadingSeekbarBindingImpl
import com.woods.blinkreader.viewmodel.BlinkReaderViewModel

class ReadingSeekbarFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reading_seekbar, container, false)?.also {
            DataBindingUtil.bind<FragmentReadingSeekbarBindingImpl>(it)?.apply {
                lifecycleOwner = this@ReadingSeekbarFragment
                readingViewModel = ViewModelProvider(
                    viewModelStore,
                    BlinkReaderViewModel.BlinkReaderViewModelFactory(activity!!.application)
                )[BlinkReaderViewModel.implClass]
            }

        }
    }
}