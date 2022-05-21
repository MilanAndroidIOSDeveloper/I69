package com.i69app.data.models


import com.google.gson.annotations.SerializedName
import kotlin.math.cos
import kotlin.math.roundToInt

data class ModelGifts(
    @SerializedName("data")
    var `data`: Data
) {
    data class Data(
        @SerializedName("allRealGift")
        var allRealGift: MutableList<AllRealGift>
    ) {
        data class AllRealGift(
            @SerializedName("cost")
            var cost: Double,
            @SerializedName("giftName")
            var giftName: String,
            @SerializedName("id")
            var id: String,
            @SerializedName("picture")
            var picture: String,
            @SerializedName("type")
            var type: String,

            var isSelected: Boolean = false
        ){
            fun getRoundedCost(): String?{
                return cost.roundToInt().toString()
            }
        }
    }
}