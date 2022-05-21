package com.i69app.gifts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.i69app.data.models.ModelGifts
import com.i69app.databinding.FragmentRealGiftsBinding
import com.i69app.ui.adapters.AdapterGifts
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.viewModels.UserViewModel
import kotlinx.coroutines.launch

class FragmentRealGifts: BaseFragment<FragmentRealGiftsBinding>() {
    var giftsAdapter: AdapterGifts ?= null
    var list : MutableList<ModelGifts.Data.AllRealGift> = mutableListOf()

    private val viewModel: UserViewModel by activityViewModels()
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRealGiftsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        giftsAdapter = AdapterGifts(requireContext(), list)
        binding.recyclerViewGifts.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerViewGifts.setHasFixedSize(true)
        binding.recyclerViewGifts.adapter = giftsAdapter
        lifecycleScope.launch {
            showProgressView()
            viewModel.getRealGifts(getCurrentUserToken()!!).observe(this@FragmentRealGifts, {
                list.clear()
                list.addAll(it)
                giftsAdapter?.notifyDataSetChanged()
                hideProgressView()
            })
        }
    }

    override fun setupClickListeners() {}
}