package com.i69app.gifts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.i69app.data.models.ModelGifts
import com.i69app.databinding.FragmentVirtualGiftsBinding
import com.i69app.ui.adapters.AdapterGifts
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.viewModels.UserViewModel
import kotlinx.coroutines.launch

class FragmentVirtualGifts: BaseFragment<FragmentVirtualGiftsBinding>() {
    lateinit var giftsAdapter: AdapterGifts
    var list : MutableList<ModelGifts.Data.AllRealGift> = mutableListOf()

    private val viewModel: UserViewModel by activityViewModels()
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentVirtualGiftsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        giftsAdapter = AdapterGifts(requireContext(), list)
        binding.recyclerViewGifts.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerViewGifts.setHasFixedSize(true)
        binding.recyclerViewGifts.adapter = giftsAdapter

        lifecycleScope.launch {
            viewModel.getVirtualGifts(getCurrentUserToken()!!).observe(this@FragmentVirtualGifts, {
                list.clear()
                list.addAll(it)
                giftsAdapter.notifyDataSetChanged()
            })
        }
    }

    override fun setupClickListeners() {}
}