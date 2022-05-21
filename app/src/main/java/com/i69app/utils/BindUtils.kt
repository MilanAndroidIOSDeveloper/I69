package com.i69app.utils

import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatSpinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textview.MaterialTextView
import com.i69app.BuildConfig
import com.i69app.R
import com.i69app.data.config.Constants
import com.i69app.data.models.IdWithValue
import com.i69app.data.models.Photo
import com.i69app.ui.views.InterestsView
import com.i69app.ui.views.rangeseekbar.OnRangeChangedListener
import com.i69app.ui.views.rangeseekbar.RangeSeekBar

@BindingAdapter("profileImage")
fun ImageView.profileImage(imageUrls: List<Photo>?) {
    if (!imageUrls.isNullOrEmpty()) loadCircleImage(imageUrls.first().url)
}

@BindingAdapter("items")
fun setItems(v: AppCompatSpinner, items: List<String>?) {
    if (items != null) {
        val adapter = ArrayAdapter(v.context, R.layout.item_no_default_spinner, R.id.textSpinner, items.toMutableList())
        adapter.setDropDownViewResource(R.layout.item_no_default_spinner_dropdown_menu)
        v.adapter = adapter
    }
}

@BindingAdapter("spinnerItems")
fun setSpinnerItems(v: AppCompatSpinner, itemsList: List<IdWithValue>?) {
    if (itemsList != null) {
        val items = itemsList.map { if (isCurrentLanguageFrench()) it.valueFr else it.value }
        val adapter = ArrayAdapter(v.context, R.layout.item_no_default_spinner, R.id.textSpinner, items.toMutableList())
        adapter.setDropDownViewResource(R.layout.item_no_default_spinner_dropdown_menu)
        v.adapter = adapter
    }
}

@BindingAdapter("itemsHeightGenderRes")
fun setItemsHeightGender(v: AppCompatSpinner, forGender: Boolean) {
    if (forGender) {
        setItemsRes(v, R.array.gender)
    }
}

@BindingAdapter("itemsRes")
fun setItemsRes(v: AppCompatSpinner, @ArrayRes items: Int) {
    val adapter = ArrayAdapter(v.context, R.layout.item_no_default_spinner, R.id.textSpinner, v.resources.getStringArray(items).toMutableList())
    adapter.setDropDownViewResource(R.layout.item_no_default_spinner_dropdown_menu)
    v.adapter = adapter
}

@BindingAdapter("leftValue", "rightValue")
fun setValues(v: com.i69app.ui.views.rangeseekbar.RangeSeekBar, setLeftValue: Float, setRightValue: Float) {
    v.setValue(setLeftValue, setRightValue)
}

@BindingAdapter("android:onClick")
fun onClick(v: InterestsView, list: View.OnClickListener) {
    v.setOnAddButtonClickListener { list.onClick(v) }
}

@BindingAdapter(value = ["leftValueAttrChanged", "rightValueAttrChanged"], requireAll = false)
fun setValuesAdapterLeft(v: com.i69app.ui.views.rangeseekbar.RangeSeekBar, setLeftValueAttrChanged: InverseBindingListener?, setRightValueAttrChanged: InverseBindingListener?) {
    v.setOnRangeChangedListener(object : com.i69app.ui.views.rangeseekbar.OnRangeChangedListener {
        override fun onRangeChanged(view: com.i69app.ui.views.rangeseekbar.RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
            setLeftValueAttrChanged?.onChange()
            setRightValueAttrChanged?.onChange()
        }

        override fun onStartTrackingTouch(view: com.i69app.ui.views.rangeseekbar.RangeSeekBar?, isLeft: Boolean) {}

        override fun onStopTrackingTouch(view: com.i69app.ui.views.rangeseekbar.RangeSeekBar?, isLeft: Boolean) {}
    })
}

@InverseBindingAdapter(attribute = "leftValue", event = "leftValueAttrChanged")
fun getValuesAdapterLeft(v: com.i69app.ui.views.rangeseekbar.RangeSeekBar): Float {
    return v.leftValue
}

@InverseBindingAdapter(attribute = "rightValue")
fun getValuesAdapterRight(v: com.i69app.ui.views.rangeseekbar.RangeSeekBar): Float {
    return v.rightValue
}

@BindingAdapter(value = ["singleValue", "singleValueAttrChanged"], requireAll = false)
fun singleValue(v: com.i69app.ui.views.rangeseekbar.RangeSeekBar, value: Float?, singleValueAttrChanged: InverseBindingListener?) {
    v.setOnRangeChangedListener(object : com.i69app.ui.views.rangeseekbar.OnRangeChangedListener {
        override fun onRangeChanged(view: com.i69app.ui.views.rangeseekbar.RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
            singleValueAttrChanged?.onChange()
        }

        override fun onStartTrackingTouch(view: com.i69app.ui.views.rangeseekbar.RangeSeekBar?, isLeft: Boolean) {}

        override fun onStopTrackingTouch(view: com.i69app.ui.views.rangeseekbar.RangeSeekBar?, isLeft: Boolean) {}
    })
    if (value != null && value >= 0) v.setValue(value)
}

@InverseBindingAdapter(attribute = "singleValue", event = "singleValueAttrChanged")
fun getSingleValueAdapter(v: com.i69app.ui.views.rangeseekbar.RangeSeekBar): Float {
    return v.leftValue
}

@BindingAdapter(value = ["selectedValue", "selectedValueAttrChanged"], requireAll = false)
fun bindSpinnerData(pAppCompatSpinner: AppCompatSpinner, newSelectedValue: String?, newTextAttrChanged: InverseBindingListener) {
    pAppCompatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            newTextAttrChanged.onChange()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
    if (newSelectedValue != null && pAppCompatSpinner.adapter != null) {
        val pos = (pAppCompatSpinner.adapter as ArrayAdapter<String>).getPosition(newSelectedValue)
        pAppCompatSpinner.setSelection(pos, true)
    }
}

@BindingAdapter(value = ["spinnerSelectedValue", "defaultPicker", "spinnerSelectedValueListener"], requireAll = false)
fun spinnerSelectedValue(v: AppCompatSpinner, pos: Int?, defaultPickerItems: List<IdWithValue>?, textAttrChanged: InverseBindingListener?) {
    v.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            textAttrChanged?.onChange()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
    if (defaultPickerItems.isNullOrEmpty()) {
        if (pos != null && pos >= 0) v.setSelection(pos, false)
    } else {
        var selectedPos: Int = -1
        defaultPickerItems.forEachIndexed { index, idWithValue ->
            if (idWithValue.id == pos) {
                selectedPos = index
                return@forEachIndexed
            }
        }

        if (selectedPos != -1) v.setSelection(selectedPos, false)
    }
}

@InverseBindingAdapter(attribute = "spinnerSelectedValue", event = "spinnerSelectedValueListener")
fun getSpinnerSelectedValue(view: AppCompatSpinner): Int {
    return view.selectedItemPosition
}

@InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
fun captureSelectedValue(pAppCompatSpinner: AppCompatSpinner): String {
    return pAppCompatSpinner.selectedItem as String
}

@BindingAdapter("prepareTermsAndConditions")
fun MaterialTextView.prepareTermsAndConditionsText(terms: Boolean) {
    val htmlText = if (isCurrentLanguageFrench())
        "Rien ne sera publié sur Facebook ou sur Linkedin. Accepter nos <a href=\"${com.i69app.data.config.Constants.URL_TERMS_AND_CONDITION}\">Termes, conditions</a> et <a href=\"${com.i69app.data.config.Constants.URL_PRIVACY_POLICY}\">la Politique de confidentialité</a>"
    else
        "We don\'t post anything to Facebook and LinkedIn. By signing in, you agree with our <a href=\"${com.i69app.data.config.Constants.URL_TERMS_AND_CONDITION}\">Terms, Conditions</a> and <a href=\"${com.i69app.data.config.Constants.URL_PRIVACY_POLICY}\">Privacy Policy</a>"
    this.text = spannedFromHtml(htmlText)
    this.isClickable = true
    this.movementMethod = LinkMovementMethod.getInstance()
}

@BindingAdapter(value = ["loadImage", "placeholder"], requireAll = false)
fun loadImage(view: ImageView, url: String, drawable: Drawable?){

    Glide.with(view.context)
        .load("${BuildConfig.BASE_URL}media/${url}")
        .placeholder(drawable)
        .into(view)
}
@BindingAdapter(value = ["loadGiftImage", "placeholder"], requireAll = false)
fun loadGiftImage(view: ImageView, url: String, drawable: Drawable?){
    val requestOptions = RequestOptions().override(93,93).centerCrop()

    Glide.with(view.context)
        .load("${BuildConfig.BASE_URL}media/${url}")
        .apply(requestOptions)
        .placeholder(drawable)
        .into(view)
}