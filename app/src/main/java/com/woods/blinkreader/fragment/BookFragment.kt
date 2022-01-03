package com.woods.blinkreader.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.woods.blinkreader.R
import com.woods.blinkreader.databinding.FragmentBookBindingImpl
import com.woods.blinkreader.viewmodel.BlinkReaderViewModel
import kotlin.math.max

class BookFragment : Fragment() {
    lateinit var blinkReaderViewModel: BlinkReaderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val toReturnView = inflater.inflate(R.layout.fragment_book, container, false)

        blinkReaderViewModel = ViewModelProvider(
            viewModelStore,
            BlinkReaderViewModel.BlinkReaderViewModelFactory(activity!!.application)
        )[BlinkReaderViewModel.implClass]

        val fragmentBookBinding: FragmentBookBindingImpl? = DataBindingUtil.bind(toReturnView)
        fragmentBookBinding?.lifecycleOwner = this
        fragmentBookBinding?.readingViewModel = blinkReaderViewModel

        blinkReaderViewModel.scrollToPercentageLiveData.observe(viewLifecycleOwner) {
            val scrollView = fragmentBookBinding?.bookScrollView as ScrollView
            val maxScrollAmount = max(0, scrollView.getChildAt(0).height - (scrollView.height))
            val scrollToAmount = it * maxScrollAmount
            scrollView.scrollTo(0, scrollToAmount.toInt())
        }
        return toReturnView
    }
}