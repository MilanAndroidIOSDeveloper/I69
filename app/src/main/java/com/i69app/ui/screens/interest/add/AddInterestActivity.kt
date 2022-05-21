package com.i69app.ui.screens.interest.add

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import com.i69app.R
import com.i69app.data.config.Constants.EXTRA_INTEREST_TYPE
import com.i69app.data.config.Constants.INTEREST_MOVIE
import com.i69app.data.config.Constants.INTEREST_MUSIC
import com.i69app.data.config.Constants.INTEREST_SPORT_TEAM
import com.i69app.data.config.Constants.INTEREST_TV_SHOW
import com.i69app.databinding.FragmentInputValueBinding
import com.i69app.ui.base.BaseActivity
import com.i69app.utils.*
import com.i69app.utils.EXTRA_FIELD_VALUE

fun getAddInterestIntent(context: Context, interestType: Int) = Intent(context, AddInterestActivity::class.java).apply {
    putExtra(EXTRA_INTEREST_TYPE, interestType)
}

class AddInterestActivity : BaseActivity<FragmentInputValueBinding>() {

    override fun getActivityBinding(inflater: LayoutInflater) = FragmentInputValueBinding.inflate(inflater)

    override fun setupTheme() {
        initToolbar()
        initFields()
    }

    override fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_save) saveFieldValue()
            true
        }
        binding.addButton.setOnClickListener {
            saveFieldValue()
        }
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_keyboard_right_arrow)
        binding.toolbar.inflateMenu(R.menu.save_menu)
    }

    private fun saveFieldValue() {
        val fieldValue = binding.field.text.toString()
        if (fieldValue.isNotEmpty()) {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(EXTRA_FIELD_VALUE, fieldValue)
            })
            finish()
        } else {
            binding.root.snackbar(getString(R.string.add_custom_tags_error_message))
        }
    }

    private fun initFields() {
        when (intent?.extras?.getInt(EXTRA_INTEREST_TYPE)) {
            INTEREST_MUSIC -> {
                binding.titleLabel.text = getString(R.string.add_artist)
                binding.description.text = getString(R.string.music)
                binding.addButton.text = getString(R.string.add_artist)
                binding.field.hint = getString(R.string.add_artist_hint)
            }
            INTEREST_MOVIE -> {
                binding.titleLabel.text = getString(R.string.add_movie)
                binding.description.text = getString(R.string.add_movie)
                binding.addButton.text = getString(R.string.add_movie)
                binding.field.hint = getString(R.string.add_movie_hint)
            }
            INTEREST_TV_SHOW -> {
                binding.titleLabel.text = getString(R.string.add_tv_show)
                binding.description.text = getString(R.string.tv_show)
                binding.addButton.text = getString(R.string.add_tv_show)
                binding.field.hint = getString(R.string.add_tv_show_hint)
            }
            INTEREST_SPORT_TEAM -> {
                binding.titleLabel.text = getString(R.string.add_sport_team)
                binding.description.text = getString(R.string.sport_team)
                binding.addButton.text = getString(R.string.add_sport_team)
                binding.field.hint = getString(R.string.add_sport_team_hint)
            }
        }
    }

}