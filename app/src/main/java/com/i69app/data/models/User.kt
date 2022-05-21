package com.i69app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.i69app.GetAllUserMomentsQuery
import com.i69app.profile.db.converters.UserConverters
import com.i69app.type.UserPhotoType

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey
    @SerializedName("id")
    var id: String = "",
    val username: String = "",
    var fullName: String = "",
    var email: String = "",
    var photosQuota: Int = 3,
    @TypeConverters(UserConverters::class)
    @Expose
    var avatarPhotos: MutableList<Photo>? = mutableListOf(),
    var purchaseCoins: Int = 0,
    var giftCoins: Int = 0,

    @SerializedName("isOnline")
    var isOnline: Boolean = false,
    var gender: Int? = 0,
    var age: Int? = 0,
    var avatarIndex: Int? = 0,
    var ageValue: String = "",
    var height: Int? = 0,
    var heightValue: String = "",
    var about: String? = "",
    @TypeConverters(UserConverters::class)
    var location: MutableList<Double>? = mutableListOf(), /// First Index will be Latitude and Second One will be Longitude
    var familyPlans: Int? = 0,
    var religion: Int? = 0,
    var politics: Int? = 0,
    @TypeConverters(UserConverters::class)
    var interestedIn: MutableList<Int> = mutableListOf(),
    @TypeConverters(UserConverters::class)
    var tags: MutableList<Int> = mutableListOf(),
    var education: String? = "",
    @TypeConverters(UserConverters::class)
    var sportsTeams: MutableList<String>? = mutableListOf(),
    @TypeConverters(UserConverters::class)
    var movies: MutableList<String>? = mutableListOf(),
    @TypeConverters(UserConverters::class)
    var books: MutableList<String>? = mutableListOf(),
    @TypeConverters(UserConverters::class)
    var music: MutableList<String>? = mutableListOf(),
    @SerializedName("ethinicity")
    var ethnicity: Int? = 0,
    @TypeConverters(UserConverters::class)
    var tvShows: MutableList<String>? = mutableListOf(),
    var work: String? = "",
    var zodiacSign: Int? = 0,
    @TypeConverters(UserConverters::class)
    var likes: MutableList<BlockedUser> = mutableListOf(),
    @TypeConverters(UserConverters::class)
    var blockedUsers: MutableList<BlockedUser> = mutableListOf()
)