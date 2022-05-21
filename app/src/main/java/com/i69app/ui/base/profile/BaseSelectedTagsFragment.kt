package com.i69app.ui.base.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import com.i69app.R
import com.i69app.data.models.IdWithValue
import com.i69app.databinding.FragmentSelectTagsBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.views.chipcloud.ChipListener
import com.i69app.utils.snackbar

private const val MIN_TAGS = 3

abstract class BaseSelectedTagsFragment : BaseFragment<FragmentSelectTagsBinding>(),
    com.i69app.ui.views.chipcloud.ChipListener {

    protected var selectedTags: ArrayList<IdWithValue> = arrayListOf()

    abstract fun loadTags()

    abstract fun onNextClick()

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSelectTagsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        binding.toolbar.inflateMenu(R.menu.next_menu)

        showProgressView()
        loadTags()
        binding.chips.setChipListener(this)
    }

    override fun setupClickListeners() {
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_next) onNextClick()
            true
        }
        binding.toolbarHamburger.setOnClickListener { moveUp() }
    }

    protected fun restoreTags(tags: List<Int>) {
        if (tags.isNotEmpty()) {
            tags.forEach {
                binding.chips.setSelectedChip(it)
            }
        }
    }

    protected fun isTagsValid(): Boolean {
        if (selectedTags.size < MIN_TAGS) {
            binding.root.snackbar(getString(R.string.select_tags_error_message))
            return false
        }
        return true
    }

    override fun chipSelected(index: Int) {
        val tag = binding.chips.chipsList[index]
        selectedTags.add(tag)
    }

    override fun chipDeselected(index: Int) {
        val tag = binding.chips.chipsList[index]
        if (selectedTags.contains(tag)) selectedTags.remove(tag)
    }

    override fun chipMaxCountLimit(count: Int) {
    }


}