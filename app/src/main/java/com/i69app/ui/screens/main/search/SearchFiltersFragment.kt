package com.i69app.ui.screens.main.search

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.cachapa.expandablelayout.ExpandableLayout
import com.i69app.R
import com.i69app.data.enums.InterestedInGender
import com.i69app.data.remote.requests.SearchRequest
import com.i69app.databinding.FragmentSearchFiltersBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.viewModels.SearchViewModel
import com.i69app.ui.views.ToggleImageView
import com.i69app.utils.isCurrentLanguageFrench
import com.i69app.utils.snackbar
import java.util.*

@AndroidEntryPoint
class SearchFiltersFragment : BaseFragment<FragmentSearchFiltersBinding>() {

    private val mViewModel: SearchViewModel by activityViewModels()
    private var userId: String? = null
    private var userToken: String? = null

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSearchFiltersBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        navController = findNavController()
        val interestedIn = arguments?.getSerializable("interested_in") as InterestedInGender
        binding.model = mViewModel
        val lookingFor = getString(R.string.looking_for)

        lifecycleScope.launch {
            userId = getCurrentUserId()!!
            userToken = getCurrentUserToken()!!

            mViewModel.getDefaultPickers(userToken!!).observe(viewLifecycleOwner) {
                it?.let { defaultPicker ->
                    mViewModel.updateDefaultPicker(lookingFor, defaultPicker)
                    val agePicker = defaultPicker.agePicker
                    binding.ageRangeSeekBar.setRange(
                        agePicker[0].value.toFloat(),
                        agePicker[agePicker.size - 1].value.toFloat()
                    )
                }
            }
        }

        updateTags()
        binding.personalLayoutItem.tagsBtn.setOnChipClickListener { tag, position ->
            if (mViewModel.tags.size - 1 >= position) {
                mViewModel.tags.removeAt(position)
                updateTags()
            }
        }

        mViewModel.btnTagsAddListener = View.OnClickListener {
            navController.navigate(R.id.action_searchFiltersFragment_to_selectTagsFragment)
        }

        mViewModel.searchBtnClickListener = View.OnClickListener {
            showProgressView()

            Permissions.check(requireActivity(), permission.ACCESS_COARSE_LOCATION, null, object : PermissionHandler() {
                @SuppressLint("MissingPermission")
                override fun onGranted() {
                    val locationService = LocationServices.getFusedLocationProviderClient(activity!!)

                    locationService.lastLocation.addOnSuccessListener { location: Location? ->
                        val searchKey: String = binding.keyInput.text.toString()
                        var lat: Double? = null
                        var lon: Double? = null
                        if (searchKey.isEmpty()) {
                            lat = location?.latitude
                            lon = location?.longitude
                        }
                        val searchRequest = SearchRequest(
                            interestedIn = interestedIn.id,
                            id = userId!!,
                            searchKey = searchKey,
                            lat = lat,
                            long = lon
                        )

                        mViewModel.getSearchUsers(_searchRequest = searchRequest, token = userToken!!) { error ->
                            if (error == null) {
                                hideProgressView()
                                navController.navigate(R.id.action_searchFiltersFragment_to_searchResultFragment)
                            } else {
                                hideProgressView()
                                binding.root.snackbar(error)
                            }
                        }
                    }
                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    binding.root.snackbar(getString(R.string.search_permission))
                    hideProgressView()
                }
            })
        }
        initGroups()
    }

    override fun setupClickListeners() {}

    private fun updateTags() {
        binding.personalLayoutItem.tagsBtn.setInterests(mViewModel.tags.map { if (isCurrentLanguageFrench()) it.valueFr else it.value })
    }

    private fun initGroups() {
        initExpandableLayout(binding.groupsExpand, binding.toggleGroupsExpand, binding.groups)
        initExpandableLayout(binding.personalExpand, binding.togglePersonalExpand, binding.personal)
    }

    private fun initExpandableLayout(button: View, toggleImageView: ToggleImageView, expandableLayout: ExpandableLayout) {
        toggleImageView.onCheckedChangeListener =
            CompoundButton.OnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    expandableLayout.expand(false)
                } else {
                    expandableLayout.collapse()
                }
            }

        button.setOnClickListener {
            toggleImageView.toggle()
        }
    }

}