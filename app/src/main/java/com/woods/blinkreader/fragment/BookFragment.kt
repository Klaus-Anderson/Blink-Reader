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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book, container, false).also { view ->
            DataBindingUtil.bind<FragmentBookBindingImpl>(view)?.also { binding ->
                binding.lifecycleOwner = this
                binding.readingViewModel = ViewModelProvider(
                    viewModelStore,
                    BlinkReaderViewModel.BlinkReaderViewModelFactory(activity!!.application)
                )[BlinkReaderViewModel.implClass].apply {
                    scrollToPercentageLiveData.observe(viewLifecycleOwner) {
                        val scrollView = binding.bookScrollView as ScrollView
                        val maxScrollAmount = max(0, scrollView.getChildAt(0).height - (scrollView.height))
                        val scrollToAmount = it * maxScrollAmount
                        scrollView.scrollTo(0, scrollToAmount.toInt())
                    }
                }
            }
        }
    }
}