package com.i69app.ui.screens.main.search

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.i69app.ui.base.profile.BaseSelectedTagsFragment
import com.i69app.ui.viewModels.SearchViewModel

@AndroidEntryPoint
class SearchSelectTagsFragment : BaseSelectedTagsFragment() {

    private val viewModel: SearchViewModel by activityViewModels()

    override fun loadTags() {
        lifecycleScope.launch {
            val userToken = getCurrentUserToken()!!
            viewModel.getDefaultPickers(userToken).observe(viewLifecycleOwner, { defaultPicker ->
                defaultPicker?.let {
                    hideProgressView()
                    binding.chips.addChips(it.tagsPicker)
                    selectedTags = viewModel.tags
                    restoreTags(viewModel.tags.map { tag -> tag.id })
                }
            })
        }
    }

    override fun onNextClick() {
        if (isTagsValid()) {
            viewModel.updateTags(selectedTags)
            moveUp()
        }
    }
}