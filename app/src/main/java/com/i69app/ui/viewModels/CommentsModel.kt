package com.i69app.ui.viewModels

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CommentsModel(
    @SerializedName("uid")
    var uid: String? = null,
    @SerializedName("cmtlikes")
    var cmtlikes: String? = null,
    @SerializedName("momentID")
    var momentID: String? = null,
    @SerializedName("cmtID")
    var cmtID: String? = null,
    @SerializedName("userurl")
    var userurl: String? = null,
    @SerializedName("username")
    var username: String? = null,
    @SerializedName("timeago")
    var timeago: String? = null,
    @SerializedName("commenttext")
        var commenttext: String? = null,
    @SerializedName("replylist")
        var replylist: List<ReplysModel>? = null,
    @SerializedName("is_expanded")
        var isExpanded: Boolean? = true
) : Serializable {
    override
    fun toString(): String {
        return GsonBuilder().serializeNulls().create().toJson(this)
    }
}