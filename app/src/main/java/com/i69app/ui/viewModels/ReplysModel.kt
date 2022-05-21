package com.i69app.ui.viewModels

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ReplysModel(
    @SerializedName("uid")
    var uid: String? = null,
    @SerializedName("userurl")
    var userurl: String? = null,
    @SerializedName("usernames")
    var usernames: String? = null,
    @SerializedName("timeago")
    var timeago: String? = null,
    @SerializedName("replytext")
    var replytext: String? = null
) : Serializable