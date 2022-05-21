package com.i69app.ui.screens.auth.profile

import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import com.i69app.R
import com.i69app.ui.base.profile.BaseSelectedTagsFragment
import com.i69app.ui.viewModels.AuthViewModel

@AndroidEntryPoint
class SelectTagsSignUpFragment : BaseSelectedTagsFragment() {

    private val viewModel: AuthViewModel by activityViewModels()

    override fun getStatusBarColor() = R.color.container_color

    override fun loadTags() {
        viewModel.getDefaultPickers(viewModel.token!!).observe(viewLifecycleOwner, { defaultPicker ->
            defaultPicker?.let {
                hideProgressView()
                binding.chips.addChips(it.tagsPicker)
                restoreTags(selectedTags.map { tag -> tag.id })
            }
        })
    }

    override fun onNextClick() {
        if (isTagsValid()) {
            viewModel.getAuthUser()!!.tags = selectedTags.map { it.id }.toMutableList()
            moveTo(R.id.action_select_tags_to_about_setup)
        }
    }

}