package com.i69app.ui.screens.interest

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.i69app.R
import com.i69app.data.config.Constants.EXTRA_INTEREST_TYPE
import com.i69app.data.config.Constants.EXTRA_INTEREST_VALUE
import com.i69app.data.config.Constants.INTEREST_MOVIE
import com.i69app.data.config.Constants.INTEREST_MUSIC
import com.i69app.data.config.Constants.INTEREST_SPORT_TEAM
import com.i69app.data.config.Constants.INTEREST_TV_SHOW
import com.i69app.databinding.FragmentTagsBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.adapters.InterestListAdapter
import com.i69app.ui.screens.interest.add.getAddInterestIntent
import com.i69app.ui.viewModels.InterestsListViewModel
import timber.log.Timber

class InterestsListFragment : BaseFragment<FragmentTagsBinding>(), InterestListAdapter.InterestListener {

    companion object {
        fun newInstance(interestType: Int, interestedInValues: ArrayList<String>) = InterestsListFragment().apply {
            val args = Bundle().apply {
                putInt(EXTRA_INTEREST_TYPE, interestType)
                putStringArrayList(EXTRA_INTEREST_VALUE, interestedInValues)
            }
            arguments = args
        }
    }

    private val viewModel: InterestsListViewModel by activityViewModels()

    private val addTagIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        viewModel.handleOnActivityResult(result.resultCode, result.data)
    }

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTagsBinding = FragmentTagsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        binding.titleLabel.text = getString(R.string.profile_edit_title)
        binding.toolbar.inflateMenu(R.menu.save_menu)
        initButtonText()

        val adapter = InterestListAdapter(this)
        binding.recyclerView.adapter = adapter

        viewModel.getInterests().observe(viewLifecycleOwner, { interestList ->
            interestList?.let {
                Timber.d("Callback ${it.size}")
                adapter.updateList(it)
            }
        })

        viewModel.loadInterests(arguments?.getInt(EXTRA_INTEREST_TYPE), arguments?.getStringArrayList(EXTRA_INTEREST_VALUE))
    }

    override fun setupClickListeners() {
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_save) save()
            true
        }
        binding.buttonAddTag.setOnClickListener {
            addTagIntent.launch(getAddInterestIntent(requireActivity(), arguments?.getInt(EXTRA_INTEREST_TYPE)!!))
        }
    }

    private fun initButtonText() {
        when (arguments?.getInt(EXTRA_INTEREST_TYPE)) {
            INTEREST_MUSIC -> {
                binding.buttonAddTagText.text = getString(R.string.add_music_tags)
                binding.description.text = getString(R.string.music)
            }
            INTEREST_MOVIE -> {
                binding.buttonAddTagText.text = getString(R.string.add_movies_tags)
                binding.description.text = getString(R.string.movies)
            }
            INTEREST_TV_SHOW -> {
                binding.buttonAddTagText.text = getString(R.string.add_tv_tags)
                binding.description.text = getString(R.string.tv_shows)
            }
            INTEREST_SPORT_TEAM -> {
                binding.buttonAddTagText.text = getString(R.string.add_sport_teams_tags)
                binding.description.text = getString(R.string.sport_teams)
            }
        }
    }

    private fun save() {
        val intent = Intent().apply {
            putExtra(EXTRA_INTEREST_TYPE, arguments?.getInt(EXTRA_INTEREST_TYPE))
            putStringArrayListExtra(EXTRA_INTEREST_VALUE, viewModel.getInterests().value)
        }
        activity?.setResult(RESULT_OK, intent)
        activity?.finish()
    }

    override fun onViewClick(pos: Int) {

    }

    override fun onRemoveBtnClick(pos: Int) {
        viewModel.removeInterest(pos)
    }

}