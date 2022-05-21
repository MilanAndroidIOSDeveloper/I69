package com.i69app.data.remote.requests

data class SearchRequest(
    val interestedIn: Int,
    val id: String,
    val searchKey: String? = null,
    val minHeight: Int? = null,
    val maxHeight: Int? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val lat: Double? = 0.0,
    val long: Double? = 0.0,
    val tags: List<Int>? = null,
    val familyPlans: Int? = null,
    val politics: Int? = null,
    val religious: Int? = null,
    val zodiacSign: Int? = null,
    val maxDistance: Int? = null
)