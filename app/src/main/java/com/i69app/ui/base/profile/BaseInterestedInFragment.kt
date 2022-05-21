package com.i69app.ui.base.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import com.i69app.R
import com.i69app.data.enums.InterestedInGender
import com.i69app.databinding.FragmentInterestedInBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.utils.setButtonState
import com.i69app.utils.snackbar

abstract class BaseInterestedInFragment : BaseFragment<FragmentInterestedInBinding>() {

    private var selectedSeriousRelationship: com.i69app.data.enums.InterestedInGender? = null
    private var selectedCausalDating: com.i69app.data.enums.InterestedInGender? = null
    private var selectedNewFriends: com.i69app.data.enums.InterestedInGender? = null
    private var selectedRoommates: com.i69app.data.enums.InterestedInGender? = null
    private var selectedBusinessContacts: com.i69app.data.enums.InterestedInGender? = null

    abstract fun onSaveClick()


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentInterestedInBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        binding.toolbar.inflateMenu(R.menu.save_menu)
        restoreButtonState()
    }

    override fun setupClickListeners() {
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_save) onSaveClick()
            true
        }
        binding.toolbarHamburger.setOnClickListener { moveUp() }

        binding.seriousRelationshipGender.genderMan.setOnClickListener { updateSeriousRelationshipLayout(
            com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_ONLY_MALE) }
        binding.seriousRelationshipGender.genderWoman.setOnClickListener { updateSeriousRelationshipLayout(
            com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_ONLY_FEMALE) }

        binding.interestedInDatingGender.genderMan.setOnClickListener { updateCausalDatingLayout(com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_ONLY_MALE) }
        binding.interestedInDatingGender.genderWoman.setOnClickListener { updateCausalDatingLayout(
            com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_ONLY_FEMALE) }

        binding.newFriendsGender.genderMan.setOnClickListener { updateNewFriendsLayout(com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_ONLY_MALE) }
        binding.newFriendsGender.genderWoman.setOnClickListener { updateNewFriendsLayout(com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_ONLY_FEMALE) }

        binding.roommatesGender.genderMan.setOnClickListener { updateRoommatesLayout(com.i69app.data.enums.InterestedInGender.ROOM_MATES_ONLY_MALE) }
        binding.roommatesGender.genderWoman.setOnClickListener { updateRoommatesLayout(com.i69app.data.enums.InterestedInGender.ROOM_MATES_ONLY_FEMALE) }

        binding.businessGender.genderMan.setOnClickListener { updateBusinessContactsLayout(com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_ONLY_MALE) }
        binding.businessGender.genderWoman.setOnClickListener { updateBusinessContactsLayout(com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_ONLY_FEMALE) }
    }

    private fun updateSeriousRelationshipLayout(updated: com.i69app.data.enums.InterestedInGender) {
        selectedSeriousRelationship = when {
            selectedSeriousRelationship == updated -> null
            selectedSeriousRelationship != updated -> {
                when (selectedSeriousRelationship) {
                    null -> updated
                    com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_BOTH ->
                        if (updated == com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_ONLY_MALE)
                            com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_ONLY_FEMALE
                        else
                            com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_ONLY_MALE
                    else -> com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_BOTH
                }
            }
            else -> null
        }
    }

    private fun updateCausalDatingLayout(updated: com.i69app.data.enums.InterestedInGender) {
        selectedCausalDating = when {
            selectedCausalDating == updated -> null
            selectedCausalDating != updated -> {
                when (selectedCausalDating) {
                    null -> updated
                    com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_BOTH ->
                        if (updated == com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_ONLY_MALE)
                            com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_ONLY_FEMALE
                        else
                            com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_ONLY_MALE
                    else -> com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_BOTH
                }
            }
            else -> null
        }
    }

    private fun updateNewFriendsLayout(updated: com.i69app.data.enums.InterestedInGender) {
        selectedNewFriends = when {
            selectedNewFriends == updated -> null
            selectedSeriousRelationship != updated -> {
                when (selectedSeriousRelationship) {
                    null -> updated
                    com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_BOTH ->
                        if (updated == com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_ONLY_MALE)
                            com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_ONLY_FEMALE
                        else
                            com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_ONLY_MALE
                    else -> com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_BOTH
                }
            }
            else -> null
        }
    }

    private fun updateRoommatesLayout(updated: com.i69app.data.enums.InterestedInGender) {
        selectedRoommates = when {
            selectedRoommates == updated -> null
            selectedRoommates != updated -> {
                when (selectedRoommates) {
                    null -> updated
                    com.i69app.data.enums.InterestedInGender.ROOM_MATES_BOTH ->
                        if (updated == com.i69app.data.enums.InterestedInGender.ROOM_MATES_ONLY_MALE)
                            com.i69app.data.enums.InterestedInGender.ROOM_MATES_ONLY_FEMALE
                        else
                            com.i69app.data.enums.InterestedInGender.ROOM_MATES_ONLY_MALE
                    else -> com.i69app.data.enums.InterestedInGender.ROOM_MATES_BOTH
                }
            }
            else -> null
        }
    }

    private fun updateBusinessContactsLayout(updated: com.i69app.data.enums.InterestedInGender) {
        selectedBusinessContacts = when {
            selectedBusinessContacts == updated -> null
            selectedBusinessContacts != updated -> {
                when (selectedBusinessContacts) {
                    null -> updated
                    com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_BOTH ->
                        if (updated == com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_ONLY_MALE)
                            com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_ONLY_FEMALE
                        else
                            com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_ONLY_MALE
                    else -> com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_BOTH
                }
            }
            else -> null
        }
    }


    protected fun checkInterestedInputs(): Boolean {
        if (selectedSeriousRelationship != null ||
            selectedCausalDating != null ||
            selectedNewFriends != null ||
            selectedRoommates != null ||
            selectedBusinessContacts != null
        ) {
            return true
        }
        binding.root.snackbar(getString(R.string.select_option_error))
        return false
    }

    protected fun getInterestedInValues(): ArrayList<Int> {
        val listOfInterestedIn = ArrayList<Int>()
        selectedSeriousRelationship?.let {
            listOfInterestedIn.add(it.id)
        }
        selectedCausalDating?.let {
            listOfInterestedIn.add(it.id)
        }
        selectedNewFriends?.let {
            listOfInterestedIn.add(it.id)
        }
        selectedRoommates?.let {
            listOfInterestedIn.add(it.id)
        }
        selectedBusinessContacts?.let {
            listOfInterestedIn.add(it.id)
        }
        return listOfInterestedIn
    }

    private fun restoreButtonState() {
        when (selectedSeriousRelationship) {
            com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_ONLY_MALE -> {
                binding.seriousRelationshipGender.genderMan.setButtonState(true)
                binding.seriousRelationshipGender.genderWoman.setButtonState(false)
            }
            com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_ONLY_FEMALE -> {
                binding.seriousRelationshipGender.genderMan.setButtonState(false)
                binding.seriousRelationshipGender.genderWoman.setButtonState(true)
            }
            com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_BOTH -> {
                binding.seriousRelationshipGender.genderMan.setButtonState(true)
                binding.seriousRelationshipGender.genderWoman.setButtonState(true)
            }
        }

        when (selectedCausalDating) {
            com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_ONLY_MALE -> {
                binding.interestedInDatingGender.genderMan.setButtonState(true)
                binding.interestedInDatingGender.genderWoman.setButtonState(false)
            }
            com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_ONLY_FEMALE -> {
                binding.interestedInDatingGender.genderMan.setButtonState(false)
                binding.interestedInDatingGender.genderWoman.setButtonState(true)
            }
            com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_BOTH -> {
                binding.interestedInDatingGender.genderMan.setButtonState(true)
                binding.interestedInDatingGender.genderWoman.setButtonState(true)
            }
        }

        when (selectedNewFriends) {
            com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_ONLY_MALE -> {
                binding.newFriendsGender.genderMan.setButtonState(true)
                binding.newFriendsGender.genderWoman.setButtonState(false)
            }
            com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_ONLY_FEMALE -> {
                binding.newFriendsGender.genderMan.setButtonState(false)
                binding.newFriendsGender.genderWoman.setButtonState(true)
            }
            com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_BOTH -> {
                binding.newFriendsGender.genderMan.setButtonState(true)
                binding.newFriendsGender.genderWoman.setButtonState(true)
            }
        }

        when (selectedRoommates) {
            com.i69app.data.enums.InterestedInGender.ROOM_MATES_ONLY_MALE -> {
                binding.roommatesGender.genderMan.setButtonState(true)
                binding.roommatesGender.genderWoman.setButtonState(false)
            }
            com.i69app.data.enums.InterestedInGender.ROOM_MATES_ONLY_FEMALE -> {
                binding.roommatesGender.genderMan.setButtonState(false)
                binding.roommatesGender.genderWoman.setButtonState(true)
            }
            com.i69app.data.enums.InterestedInGender.ROOM_MATES_BOTH -> {
                binding.roommatesGender.genderMan.setButtonState(true)
                binding.roommatesGender.genderWoman.setButtonState(true)
            }
        }

        when (selectedBusinessContacts) {
            com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_ONLY_MALE -> {
                binding.businessGender.genderMan.setButtonState(true)
                binding.businessGender.genderWoman.setButtonState(false)
            }
            com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_ONLY_FEMALE -> {
                binding.businessGender.genderMan.setButtonState(false)
                binding.businessGender.genderWoman.setButtonState(true)
            }
            com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_BOTH -> {
                binding.businessGender.genderMan.setButtonState(true)
                binding.businessGender.genderWoman.setButtonState(true)
            }
        }

    }


}