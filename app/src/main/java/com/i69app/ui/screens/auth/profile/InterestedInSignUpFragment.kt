package com.i69app.ui.screens.auth.profile

import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import com.i69app.R
import com.i69app.ui.base.profile.BaseInterestedInFragment
import com.i69app.ui.viewModels.AuthViewModel

@AndroidEntryPoint
class InterestedInSignUpFragment : BaseInterestedInFragment() {

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onSaveClick() {
        if (checkInterestedInputs()) {
            viewModel.getAuthUser()!!.interestedIn = getInterestedInValues()
            moveTo(R.id.action_interested_in_to_select_tags)
        }
    }

}